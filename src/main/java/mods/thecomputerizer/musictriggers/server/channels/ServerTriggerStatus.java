package mods.thecomputerizer.musictriggers.server.channels;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import c4.champions.common.capability.CapabilityChampionship;
import c4.champions.common.capability.IChampionship;
import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.network.PacketFinishedServerInit;
import mods.thecomputerizer.musictriggers.network.PacketSyncServerInfo;
import mods.thecomputerizer.musictriggers.registry.ItemRegistry;
import mods.thecomputerizer.musictriggers.server.PersistentDataHandler;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.TomlPart;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.Level;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ServerTriggerStatus {
    private static final Map<String,ServerTriggerStatus> SERVER_DATA = new HashMap<>();
    private static final Map<BossInfoServer,Class<? extends EntityLivingBase>> INSTANTIATED_BOSS_BARS = Collections.synchronizedMap(new HashMap<>());
    private static final Map<BossInfoServer,EntityLivingBase> BOSS_BAR_ENTITIES = Collections.synchronizedMap(new HashMap<>());
    private static final List<String> NBT_MODES = Arrays.asList("KEY_PRESENT","VAL_PRESENT","GREATER","LESSER","EQUAL","INVERT");
    private static final List<String> TRIGGER_HOLDERS = Arrays.asList("structure","mob","victory","pvp");

    public static void initializePlayerChannels(ByteBuf buf) {
        ServerTriggerStatus data = new ServerTriggerStatus(buf);
        SERVER_DATA.put(data.playerUUID.toString(),data);
        EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList()
                .getPlayerByUUID(data.playerUUID);
        new PacketFinishedServerInit().addPlayers(player).send();
    }

    @SuppressWarnings("ConstantValue")
    public static void decodeDynamicInfo(ByteBuf buf) {
        String playerUUID = NetworkUtil.readString(buf);
        EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList()
                .getPlayerByUUID(UUID.fromString(playerUUID));
        int size = buf.readInt();
        for(int i=0;i<size;i++) {
            String channel = NetworkUtil.readString(buf);
            boolean isTriggerUpdate = buf.readBoolean();
            if(isTriggerUpdate) {
                List<String> commands = NetworkUtil.readGenericList(buf, NetworkUtil::readString);
                boolean isPlaying = buf.readBoolean();
                String track = isPlaying ? NetworkUtil.readString(buf) : null;
                String trigger = isPlaying ? NetworkUtil.readString(buf) : null;
                ServerTriggerStatus data = SERVER_DATA.get(playerUUID);
                if (Objects.nonNull(data) && data.isValid()) data.updateDynamicInfo(channel, commands, track, trigger);
            }
            boolean isStorageUpdate = buf.readBoolean();
            if(isStorageUpdate) {
                PersistentDataHandler.getDataCapability(player).initChannel(channel);
                Map<String,Boolean> toggleMap = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,
                        ByteBuf::readBoolean);
                Map<String, Tuple<List<String>,Integer>> playedOnceMap = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,
                        buf1 -> new Tuple<>(NetworkUtil.readGenericList(buf1,NetworkUtil::readString),buf1.readInt()));
                if(Objects.nonNull(player)) {
                    for(Map.Entry<String,Boolean> toggleEntry : toggleMap.entrySet())
                        PersistentDataHandler.getDataCapability(player).writeToggleStatus(channel,toggleEntry.getKey(),
                                toggleEntry.getValue());
                    for(Map.Entry<String,Tuple<List<String>,Integer>> playedOnceEntry : playedOnceMap.entrySet())
                        PersistentDataHandler.getDataCapability(player).setAudioPlayed(channel,playedOnceEntry.getKey(),
                                playedOnceEntry.getValue().getFirst(),playedOnceEntry.getValue().getSecond());
                }
            }
        }
        int preferredSortType = buf.readInt();
        if(Objects.nonNull(player))
            PersistentDataHandler.getDataCapability(player).writePreferredSort(preferredSortType);
    }

    public static void runServerChecks() {
        synchronized (INSTANTIATED_BOSS_BARS) {
            BOSS_BAR_ENTITIES.entrySet().removeIf(entry -> {
                if (Objects.isNull(entry)) return true;
                boolean ret = entry.getKey().getPercent() <= 0;
                if (ret) {
                    INSTANTIATED_BOSS_BARS.remove(entry.getKey());
                    Constants.debugErrorServer("BOSS BAR REMOVED");
                }
                return ret;
            });
        }
        Iterator<Map.Entry<String, ServerTriggerStatus>> itr = SERVER_DATA.entrySet().iterator();
        while(itr.hasNext()) {
            ServerTriggerStatus data = itr.next().getValue();
            if(data.isValid()) data.runChecks();
            else itr.remove();
        }
    }

    public static void bossBarInstantiated(BossInfoServer info, Class<? extends EntityLivingBase> entityClass) {
        synchronized (INSTANTIATED_BOSS_BARS) {
            Constants.debugErrorServer("BOSS BAR MADE");
            INSTANTIATED_BOSS_BARS.put(info, entityClass);
        }
    }

    public static void checkIfBossSpawned(EntityLivingBase entity) {
        synchronized (INSTANTIATED_BOSS_BARS) {
            for (Map.Entry<BossInfoServer, Class<? extends EntityLivingBase>> entry : INSTANTIATED_BOSS_BARS.entrySet())
                if (entry.getValue() == entity.getClass())
                    BOSS_BAR_ENTITIES.put(entry.getKey(), entity);
        }
    }

    public static ItemStack recordAudioData(UUID playerUUID, ItemStack recordStack) {
        ServerTriggerStatus data = SERVER_DATA.get(playerUUID.toString());
        if(data.isValid()) return data.recordAudioData(recordStack);
        return ItemStack.EMPTY;
    }

    public static void setPVP(EntityPlayerMP attacker, String playerUUID) {
        ServerTriggerStatus data = SERVER_DATA.get(playerUUID);
        if(Objects.nonNull(data))
            if(data.isValid()) data.setPVP(attacker);
    }

    private final Map<String,Trigger.DefaultParameter> defaultParameterMap;
    private final Map<String, List<Table>> mappedTriggers;
    private final List<Table> allTriggers;
    private final Map<Table, Boolean> triggerStatus = new HashMap<>();
    private final Map<String, Map<String, Boolean>> updatedTriggers = new HashMap<>();
    private final Map<String, Victory> victoryTriggers;
    private final Map<String, List<String>> menuSongs;
    private final UUID playerUUID;
    private final MinecraftServer server;
    private final List<String> commandQueue = new ArrayList<>();
    private final Map<String, String> currentSongs = new HashMap<>();
    private final Map<String, String> currentTriggers = new HashMap<>();
    private EntityPlayerMP attacker = null;
    public ServerTriggerStatus(ByteBuf buf) {
        this.defaultParameterMap = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,Trigger.DefaultParameter::new);
        this.mappedTriggers = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,buf1 ->
                NetworkUtil.readGenericList(buf1,buf2 -> TomlPart.getByID(NetworkUtil.readString(buf2)).decode(buf2,null))
                .stream().filter(Table.class::isInstance).map(Table.class::cast).collect(Collectors.toList()));
        this.allTriggers = Collections.synchronizedList(this.mappedTriggers.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList()));
        this.victoryTriggers = initVictories();
        this.menuSongs = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,
                buf1 -> NetworkUtil.readGenericList(buf1,NetworkUtil::readString));
        this.server = FMLCommonHandler.instance().getMinecraftServerInstance();
        this.playerUUID = UUID.fromString(NetworkUtil.readString(buf));
    }

    private HashMap<String, Victory> initVictories() {
        HashMap<String, Victory> ret = new HashMap<>();
        synchronized (this.allTriggers) {
            for (Table trigger : this.allTriggers) {
                if (trigger.getName().matches("victory")) {
                    String id = getParameterString(trigger, "identifier");
                    if (!id.matches("not_set")) {
                        List<Table> references = this.allTriggers.stream().filter(table -> {
                            if (!table.getName().matches("mob") && !table.getName().matches("pvp")) return false;
                            return getParameterString(table, "victory_id").matches(id);
                        }).collect(Collectors.toList());
                        if (!references.isEmpty()) ret.put(id, new Victory(references,
                                getParameterInt(trigger, "victory_timeout")));
                    }
                }
            }
        }
        return ret;
    }

    public ServerTriggerStatus() {
        this.defaultParameterMap = new HashMap<>();
        this.mappedTriggers = new HashMap<>();
        this.allTriggers = new ArrayList<>();
        this.victoryTriggers = new HashMap<>();
        this.menuSongs = new HashMap<>();
        this.server = null;
        this.playerUUID = null;
    }

    @SuppressWarnings("ConstantValue")
    public boolean isValid() {
        if(this.mappedTriggers.isEmpty() || this.menuSongs.isEmpty() || Objects.isNull(this.server)) return false;
        return Objects.nonNull(this.server.getPlayerList().getPlayerByUUID(this.playerUUID));
    }

    public void addChannelInfo(String channel, List<Table> triggers, List<String> menuSongs) {
        this.mappedTriggers.put(channel,triggers);
        this.menuSongs.put(channel,menuSongs);
    }

    public void encodeForServer(ByteBuf buf) {
        Trigger.encodeDefaultParameters(buf);
        NetworkUtil.writeGenericMap(buf,this.mappedTriggers,NetworkUtil::writeString,(buf1, list) ->
                NetworkUtil.writeGenericList(buf1,list,(buf2, table) -> table.write(buf2)));
        NetworkUtil.writeGenericMap(buf,this.menuSongs,NetworkUtil::writeString,(buf1, list) ->
                NetworkUtil.writeGenericList(buf1,list,NetworkUtil::writeString));
        NetworkUtil.writeString(buf, Minecraft.getMinecraft().player.getUniqueID().toString());
    }

    private void updateDynamicInfo(String channel, List<String> commands, String playing, String trigger) {
        for(String command : commands)
            if(!this.commandQueue.contains(command)) this.commandQueue.add(command);
        if(Objects.nonNull(playing)) {
            this.currentSongs.put(channel,playing);
            this.currentTriggers.put(channel,trigger);
        } else {
            this.currentSongs.remove(channel);
            this.currentTriggers.remove(channel);
        }
    }

    private ItemStack recordAudioData(ItemStack recordStack) {
        if(this.currentSongs.isEmpty() || this.currentTriggers.isEmpty()) return ItemStack.EMPTY;
        int index = ThreadLocalRandom.current().nextInt(this.currentSongs.size());
        String channel = null;
        for(String ch : this.currentSongs.keySet()) {
            if(index == 0) {
                channel = ch;
                break;
            }
            index--;
        }
        if(Objects.isNull(channel)) return ItemStack.EMPTY;
        ItemStack ret = new ItemStack(ItemRegistry.MUSIC_TRIGGERS_RECORD);
        if(recordStack.getItem()==ItemRegistry.BLANK_RECORD) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("channelFrom", channel);
            tag.setString("trackID", this.currentSongs.get(channel));
            tag.setString("triggerID", this.currentTriggers.get(channel));
            ret.setTagCompound(tag);
            return ret;
        }
        if(this.menuSongs.get(channel).isEmpty()) return ItemStack.EMPTY;
        index = ThreadLocalRandom.current().nextInt(this.menuSongs.get(channel).size());
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("channelFrom", channel);
        tag.setString("trackID", this.menuSongs.get(channel).get(index));
        tag.setString("triggerID", "menu");
        ret.setTagCompound(tag);
        return ret;
    }

    private void setPVP(EntityPlayerMP attacker) {
        this.attacker = attacker;
    }

    public String getParameterString(Table trigger, String parameter) {
        String defVal = this.defaultParameterMap.get(parameter).getValue(trigger.getName()).toString();
        return trigger.getValOrDefault(parameter,defVal);
    }

    public boolean getParameterBool(Table trigger, String parameter) {
        boolean defVal = Boolean.parseBoolean(this.defaultParameterMap.get(parameter).getValue(trigger.getName()).toString());
        return trigger.getValOrDefault(parameter,defVal);
    }

    public int getParameterInt(Table trigger, String parameter) {
        int defVal = 0;
        try {
            defVal = this.defaultParameterMap.get(parameter).getAsInt(trigger.getName());
        } catch (NumberFormatException ex) {
            MusicTriggers.logExternally(Level.ERROR,"Tried to access default value of parameter {} incorrectly " +
                    "as an integer! Using substitute value of 0",parameter);
        }
        String randomRange = trigger.getValOrDefault(parameter,String.valueOf(defVal));
        return MusicTriggers.randomInt(parameter,randomRange,defVal);
    }

    public float getParameterFloat(Table trigger, String parameter) {
        float defVal = 0;
        try {
            defVal = this.defaultParameterMap.get(parameter).getAsFloat(trigger.getName());
        } catch (NumberFormatException ex) {
            MusicTriggers.logExternally(Level.ERROR,"Tried to access default value of parameter {} incorrectly " +
                    "as a float! Using substitute value of 0",parameter);
        }
        String randomRange = trigger.getValOrDefault(parameter,String.valueOf(defVal));
        return MusicTriggers.randomFloat(parameter,randomRange,defVal);
    }

    private List<?> getDefaultListParameter(String trigger, String parameter) {
        try {
            return this.defaultParameterMap.get(parameter).getAsList(trigger);
        } catch (IllegalArgumentException ex) {
            MusicTriggers.logExternally(Level.ERROR,"Tried to access default value of parameter {} incorrectly " +
                    "as a list! Using substitute list with element ANY",parameter);
        }
        return Collections.singletonList("ANY");
    }

    public List<String> getParameterStringList(Table trigger, String parameter) {
        List<?> defVal = getDefaultListParameter(trigger.getName(),parameter);
        Object val = trigger.getVarMap().get(parameter);
        if(val instanceof String) return Collections.singletonList((String)val);
        if(!(val instanceof List<?>)) {
            if(Objects.nonNull(val)) MusicTriggers.logExternally(Level.ERROR,"Tried to access parameter {} as " +
                    "a list when it was not stored as a list or string! Using default value {}",parameter,defVal);
            return makeStringList(defVal);
        }
        List<?> ret = (List<?>)val;
        if(ret.isEmpty()) {
            MusicTriggers.logExternally(Level.ERROR,"Parameter {} was stored as an empty list! Using default " +
                    "value {}",parameter,defVal);
            return makeStringList(defVal);
        }
        return makeStringList(ret);
    }

    private List<String> makeStringList(List<?> genericList) {
        List<String> ret = new ArrayList<>();
        for(Object element : genericList) {
            String asString = element.toString();
            if(!ret.contains(asString)) ret.add(asString);
        }
        return ret;
    }


    @SuppressWarnings("ConstantValue")
    private void runChecks() {
        this.updatedTriggers.clear();
        EntityPlayerMP player = this.server.getPlayerList().getPlayerByUUID(this.playerUUID);
        if(Objects.nonNull(player)) {
            runCommands();
            List<Table> toRemove = new ArrayList<>();
            BlockPos pos = roundedPos(player);
            synchronized (this.allTriggers) {
                for (Table trigger : this.allTriggers) {
                    if (trigger.getName().matches("home"))
                        potentiallyUpdate(trigger, calculateHome(player, pos,
                                getParameterInt(trigger, "detection_range"),
                                getParameterFloat(trigger, "detection_y_ratio")));
                    else if (trigger.getName().matches("structure"))
                        potentiallyUpdate(trigger, calculateStruct(player.getServerWorld(), pos,
                                getParameterStringList(trigger, "resource_name")));
                    else if (trigger.getName().matches("victory"))
                        potentiallyUpdate(trigger, calculateVictory(getParameterString(trigger, "identifier")));
                    else if (trigger.getName().matches("mob"))
                        potentiallyUpdate(trigger, calculateMob(trigger, player, pos));
                    else if (trigger.getName().matches("pvp"))
                        potentiallyUpdate(trigger, calculatePVP(trigger));
                    else toRemove.add(trigger);
                }
            }
            this.attacker = null;
            synchronized (this.allTriggers) {
                if (!toRemove.isEmpty()) this.allTriggers.removeAll(toRemove);
            }
            if (!this.updatedTriggers.isEmpty())
                new PacketSyncServerInfo(this.updatedTriggers).addPlayers(player).send();
        }
    }

    private void runCommands() {
        for(String command : this.commandQueue)
            this.server.getCommandManager().executeCommand(this.server,command);
        this.commandQueue.clear();
    }

    private void potentiallyUpdate(Table trigger, boolean pass) {
        if(!this.triggerStatus.containsKey(trigger) || this.triggerStatus.get(trigger) != pass) {
            String nameWithID = triggerWithID(trigger);
            if(Objects.nonNull(nameWithID)) {
                String channel = null;
                for(Map.Entry<String, List<Table>> entry : this.mappedTriggers.entrySet()) {
                    if(entry.getValue().contains(trigger)) {
                        channel = entry.getKey();
                        break;
                    }
                }
                if(Objects.nonNull(channel)) {
                    this.updatedTriggers.putIfAbsent(channel,new HashMap<>());
                    this.updatedTriggers.get(channel).put(triggerWithID(trigger), pass);
                    this.triggerStatus.put(trigger, pass);
                }
            } else {
                synchronized (this.allTriggers) {
                    this.allTriggers.remove(trigger);
                }
            }
        }
    }

    private String triggerWithID(Table trigger) {
        String name = trigger.getName();
        String id = TRIGGER_HOLDERS.contains(name) ? getParameterString(trigger,"identifier") : null;
        if(Objects.nonNull(id) && id.matches("not_set")) return null;
        return Objects.nonNull(id) ? name+"-"+id : name;
    }

    private BlockPos roundedPos(EntityPlayer p) {
        return new BlockPos((Math.round(p.posX * 2) / 2.0), (Math.round(p.posY * 2) / 2.0), (Math.round(p.posZ * 2) / 2.0));
    }

    @SuppressWarnings("ConstantValue")
    private boolean calculateHome(EntityPlayerMP player, BlockPos pos, float range, float yRatio) {
        BlockPos bedPos = player.getBedLocation(player.dimension);
        if(Objects.isNull(bedPos)) return false;
        AxisAlignedBB box = new AxisAlignedBB(pos.getX()-range,pos.getY()-(range*yRatio), pos.getZ()-range,
                pos.getX()+range,pos.getY()+(range*yRatio),pos.getZ()+range);
        return bedPos.getX()<=box.maxX && bedPos.getX()>=box.minX && bedPos.getY()<=box.maxX &&
                bedPos.getY()>=box.minY && bedPos.getZ()<=box.maxZ && bedPos.getZ()>=box.minZ;
    }

    private boolean calculateStruct(WorldServer world, BlockPos pos, List<String> resourceMatcher) {
        for(String actualStructure : resourceMatcher)
            if(world.getChunkProvider().isInsideStructure(world, actualStructure, pos)) return true;
        return false;
    }

    private boolean calculateMob(Table mobTrigger, EntityPlayerMP player, BlockPos pos) {
        List<String> resources = getParameterStringList(mobTrigger,"resource_name");
        int num = getParameterInt(mobTrigger,"level");
        if(resources.isEmpty() || num<=0) return false;
        List<String> infernal = getParameterStringList(mobTrigger,"infernal");
        List<String> champion = getParameterStringList(mobTrigger,"champion");
        boolean checkTarget = getParameterBool(mobTrigger,"mob_targeting");
        float hordeTarget = getParameterFloat(mobTrigger,"horde_targeting_percentage");
        float range = getParameterFloat(mobTrigger,"detection_range");
        float yRatio = getParameterFloat(mobTrigger,"detection_y_ratio");
        float health = getParameterFloat(mobTrigger,"health");
        float hordeHealth = getParameterFloat(mobTrigger,"horde_health_percentage");
        String nbt = getParameterString(mobTrigger,"mob_nbt");
        Victory victory = this.victoryTriggers.get(getParameterString(mobTrigger,"victory_id"));
        boolean pass;
        if (resources.contains("BOSS")) {
            synchronized (BOSS_BAR_ENTITIES) {
                String id = getParameterString(mobTrigger,"identifier");
                Set<EntityLivingBase> bossEntities = BOSS_BAR_ENTITIES.entrySet().stream()
                        .filter(entry -> entry.getKey().visible && entry.getKey().players.contains(player)).map(Map.Entry::getValue)
                        .filter(entity -> entityWhitelist(entity, resources, infernal, champion, nbt)).collect(Collectors.toSet());
                if (bossEntities.size() < num) return false;
                pass = checkSpecifics(bossEntities, num, player, checkTarget, hordeTarget, health, hordeHealth);
                if (id.matches("genericBoss")) Constants.debugErrorServer("[{}] DID IT PASS? {}", pass);
                if (Objects.nonNull(victory)) {
                    if (pass)
                        for (EntityLivingBase entity : bossEntities)
                            victory.add(mobTrigger, true, entity);
                    victory.setActive(mobTrigger, pass);
                }
            }
            return pass;
        }
        pass = checkMobs(mobTrigger,player,pos,num,range,yRatio,resources,infernal,champion,checkTarget,hordeTarget,
                health,hordeHealth,nbt,victory);
        if(Objects.nonNull(victory)) victory.setActive(mobTrigger,pass);
        return pass;
    }

    private boolean checkMobs(Table trigger, EntityPlayerMP player, BlockPos pos, int num, float range, float yRatio,
                              List<String> resources, List<String> infernal, List<String> champion, boolean target,
                              float hordeTarget, float health, float hordeHealth, String nbt, Victory victory) {
        AxisAlignedBB box = new AxisAlignedBB(pos.getX()-range,pos.getY()-(range*yRatio),pos.getZ()-range,
                pos.getX()+range,pos.getY()+(range*yRatio),pos.getZ()+range);
        HashSet<EntityLivingBase> matchedEntities = new HashSet<>(player.getServerWorld().getEntitiesWithinAABB(
                EntityLiving.class,box,e -> entityWhitelist(e,resources,infernal,champion,nbt)));
        if(matchedEntities.size()<num) return false;
        boolean pass = checkSpecifics(matchedEntities,num,player,target,hordeTarget,health,hordeHealth);
        if(Objects.nonNull(victory) && pass) {
            for(EntityLivingBase entity : matchedEntities)
                victory.add(trigger,true,entity);
        }
        return pass;
    }

    private boolean entityWhitelist(EntityLivingBase entity, List<String> resources, List<String> infernal,
                                    List<String> champion, String nbtParser) {
        return Objects.nonNull(entity) && checkEntityName(entity, resources) &&
                checkModExtensions(entity, infernal, champion) && checkNBT(entity, nbtParser);
    }

    private boolean checkEntityName(EntityLivingBase entity, List<String> resources) {
        if(resources.isEmpty()) return false;
        String displayName = entity.getName();
        ResourceLocation id = EntityList.getKey(entity);
        if(resources.contains("MOB")) {
            if(!(entity instanceof IMob)) return false;
            List<String> blackList = resources.stream().filter(element -> !element.matches("MOB")).collect(Collectors.toList());
            if(blackList.isEmpty()) return true;
            return !blackList.contains(displayName) && (Objects.isNull(id) || !partiallyMatches(id.toString(),blackList));
        }
        else if(resources.contains("BOSS")) {
            if(resources.size()==1) return true;
            List<String> whitelist = resources.stream().filter(element -> !element.matches("BOSS")).collect(Collectors.toList());
            return whitelist.contains(displayName) || (Objects.nonNull(id) && partiallyMatches(id.toString(),whitelist));
        }
        return resources.contains(displayName) || (Objects.nonNull(id) && partiallyMatches(id.toString(),resources));
    }
    private boolean checkSpecifics(Collection<EntityLivingBase> entities, int num, EntityPlayerMP player, boolean target,
                                   float targetRatio, float health, float healthRatio) {
        return checkTarget(entities,num,target,targetRatio,player) && checkHealth(entities,num,health,healthRatio);
    }

    private boolean checkTarget(Collection<EntityLivingBase> entities, int num, boolean target, float ratio, EntityPlayerMP player) {
        if(!target || ratio<=0) return true;
        float counter = 0f;
        for(EntityLivingBase entity : entities) {
            if(!(entity instanceof EntityLiving) || Objects.isNull(((EntityLiving)entity).getAttackTarget())) continue;
            if(((EntityLiving)entity).getAttackTarget() == player)
                counter++;
        }
        return counter/num>=ratio/100f;
    }

    private boolean checkHealth(Collection<EntityLivingBase> entities, int num, float health, float ratio) {
        if(health>=100 || ratio<=0) return true;
        float counter = 0f;
        for(EntityLivingBase entity : entities)
            if(entity.getHealth()/entity.getMaxHealth()<=health/100f)
                counter++;
        return counter/num>=ratio/100f;
    }

    private boolean checkNBT(EntityLivingBase entity, String nbt) {
        if(nbt.matches("ANY")) return true;
        String[] parts = nbt.split(";");
        try {
            if(parts.length==0) return true;
            if(NBT_MODES.contains(parts[0])) {
                if(parts.length==1) return false;
                NBTTagCompound data = entity.serializeNBT();
                switch (parts[0]) {
                    case "KEY_PRESENT" : {
                        if(parts[1].matches("INVERT")) {
                            if(parts.length==2) return false;
                            NBTBase finalKey = getFinalTag(data,Arrays.copyOfRange(parts,2,parts.length));
                            return Objects.isNull(finalKey) || !(finalKey instanceof NBTTagCompound);
                        }
                        NBTBase finalKey = getFinalTag(data,Arrays.copyOfRange(parts,1,parts.length));
                        return Objects.nonNull(finalKey) && finalKey instanceof NBTTagCompound;
                    }
                    case "VAL_PRESENT" : {
                        if(parts[1].matches("INVERT")) {
                            if(parts.length==2) return false;
                            NBTBase finalVal = getFinalTag(data,Arrays.copyOfRange(parts,2,parts.length));
                            return Objects.isNull(finalVal) || finalVal instanceof NBTTagCompound;
                        }
                        NBTBase finalVal = getFinalTag(data,Arrays.copyOfRange(parts,1,parts.length));
                        return Objects.nonNull(finalVal) && !(finalVal instanceof NBTTagCompound);
                    }
                    case "INVERT" : {
                        boolean compare = Boolean.parseBoolean(parts[parts.length-1]);
                        NBTBase finalVal = getFinalTag(data,Arrays.copyOfRange(parts,1,parts.length-1));
                        if(finalVal instanceof NBTTagByte)
                            return (compare && ((NBTTagByte)finalVal).getByte()==1) ||
                                    (!compare && ((NBTTagByte)finalVal).getByte()==0);
                        return false;
                    }
                    case "GREATER" : {
                        double compare = Double.parseDouble(parts[parts.length-1]);
                        int from = 1;
                        if(parts[1].matches("EQUAL")) {
                            if(parts.length==2) return false;
                            from = 2;
                        }
                        return numNBT(getFinalTag(data,Arrays.copyOfRange(parts,from,parts.length-1)),compare,true,from==2);
                    }
                    case "LESSER" : {
                        double compare = Double.parseDouble(parts[parts.length-1]);
                        int from = 1;
                        if(parts[1].matches("EQUAL")) {
                            if(parts.length==2) return false;
                            from = 2;
                        }
                        return numNBT(getFinalTag(data,Arrays.copyOfRange(parts,from,parts.length-1)),compare,false,from==2);
                    }
                    default: {
                        int from = 1;
                        boolean pass = false;
                        if(parts[1].matches("INVERT")) {
                            if(parts.length==2) return false;
                            from = 2;
                        }
                        NBTBase finalVal = getFinalTag(data,Arrays.copyOfRange(parts,from,parts.length-1));
                        if(finalVal instanceof NBTTagCompound) return false;
                        if(finalVal instanceof  NBTTagByte) {
                            boolean compare = Boolean.parseBoolean(parts[parts.length-1]);
                            pass = (compare && ((NBTTagByte) finalVal).getByte() == 0) ||
                                    (!compare && ((NBTTagByte) finalVal).getByte() == 1);
                        }
                        else if(finalVal instanceof NBTPrimitive)
                            pass = ((NBTPrimitive)finalVal).getDouble()==Double.parseDouble(parts[parts.length-1]);
                        else if(finalVal instanceof NBTTagString) {
                            pass = ((NBTTagString)finalVal).getString().trim().toLowerCase()
                                    .matches(parts[parts.length-1].trim().toLowerCase());
                        }
                        return pass || from==2;
                    }
                }
            }
            return false;
        } catch (NumberFormatException e) {
            MusicTriggers.logExternally(Level.ERROR, "Tried to check numerical value of NBT data against a non " +
                    "numerical value of {}",parts[parts.length-1]);
        } catch (Exception ignored) {
        }
        return false;
    }

    private NBTBase getFinalTag(NBTTagCompound tag, String[] parts) {
        if(!tag.hasKey(parts[0])) return null;
        if(parts.length==1) return tag.getTag(parts[0]);
        try {
            return getFinalTag(tag.getCompoundTag(parts[0]), Arrays.copyOfRange(parts,1,parts.length));
        } catch (ClassCastException ignored) {
            return null;
        }
    }

    private boolean numNBT(NBTBase tag, double comp, boolean greater, boolean equal) {
        if(!(tag instanceof NBTPrimitive)) return false;
        double val = ((NBTPrimitive)tag).getDouble();
        return greater ? equal ? val>=comp : val>comp : equal ? val<=comp : val<comp;
    }

    private boolean checkModExtensions(EntityLivingBase entity, List<String> infernal, List<String> champion) {
        if(Loader.isModLoaded("champions") && entity instanceof EntityLiving && !champion.isEmpty() &&
                !champion.contains("ANY")) {
            IChampionship champ = CapabilityChampionship.getChampionship((EntityLiving)entity);
            if (Objects.isNull(champ)) return false;
            else if (Objects.nonNull(champ.getName()))
                if (!champion.contains("ALL"))
                    if (!partiallyMatches(champ.getName(), champion)) return false;
        }
        if(Loader.isModLoaded("infernalmobs") && !infernal.isEmpty() && !infernal.contains("ANY")) {
            if (InfernalMobsCore.getIsRareEntity(entity)) {
                if (infernal.contains("ALL")) return true;
                String[] names = InfernalMobsCore.getMobModifiers(entity).getDisplayNames();
                for (String name : names)
                    if (partiallyMatches(name, infernal))
                        return true;
            }
            return false;
        }
        return true;
    }

    private boolean calculateVictory(String id) {
        if(id.matches("not_set") || !this.victoryTriggers.containsKey(id)) return false;
        return this.victoryTriggers.get(id).runCalculation(this);
    }

    private boolean calculatePVP(Table trigger) {
        boolean pass = Objects.nonNull(this.attacker);
        String victoryID = getParameterString(trigger,"victory_id");
        Victory victory = this.victoryTriggers.get(victoryID);
        if(Objects.nonNull(victory)) {
            if(pass) victory.add(trigger,false,this.attacker);
            victory.setActive(trigger,pass);
        }
        return pass;
    }

    private boolean partiallyMatches(String thing, List<String> partials) {
        for(String partial : partials)
            if(thing.contains(partial)) return true;
        return false;
    }
}
