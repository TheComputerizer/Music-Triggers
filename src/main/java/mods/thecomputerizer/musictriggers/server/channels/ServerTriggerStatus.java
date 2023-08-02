package mods.thecomputerizer.musictriggers.server.channels;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.network.PacketFinishedServerInit;
import mods.thecomputerizer.musictriggers.network.PacketSyncServerInfo;
import mods.thecomputerizer.musictriggers.registry.ItemRegistry;
import mods.thecomputerizer.musictriggers.server.data.PersistentTriggerDataProvider;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.TomlPart;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Level;
import top.theillusivec4.champions.api.IAffix;
import top.theillusivec4.champions.common.capability.ChampionCapability;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ServerTriggerStatus {
    private static final Map<String,ServerTriggerStatus> SERVER_DATA = new HashMap<>();
    private static final Map<ServerBossInfo,Class<? extends LivingEntity>> INSTANTIATED_BOSS_BARS = Collections.synchronizedMap(new HashMap<>());
    private static final Map<ServerBossInfo,LivingEntity> BOSS_BAR_ENTITIES = new HashMap<>();
    private static final List<String> NBT_MODES = Arrays.asList("KEY_PRESENT","VAL_PRESENT","GREATER","LESSER","EQUAL","INVERT");
    private static final List<String> SERVER_TRIGGER_HOLDERS = Arrays.asList("biome","structure","mob","victory","pvp","raid");

    public static void initializePlayerChannels(PacketBuffer buf) {
        ServerTriggerStatus data = new ServerTriggerStatus(buf);
        SERVER_DATA.put(data.playerUUID.toString(),data);
        ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(data.playerUUID);
        new PacketFinishedServerInit().addPlayers(player).send();
    }

    @SuppressWarnings("ConstantValue")
    public static void decodeDynamicInfo(PacketBuffer buf) {
        String playerUUID = NetworkUtil.readString(buf);
        ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(UUID.fromString(playerUUID));
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
                PersistentTriggerDataProvider.getPlayerCapability(player).initChannel(channel);
                Map<String,Boolean> toggleMap = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,
                        PacketBuffer::readBoolean);
                Map<String, Tuple<List<String>,Integer>> playedOnceMap = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,
                        buf1 -> new Tuple<>(NetworkUtil.readGenericList(buf1,NetworkUtil::readString),buf1.readInt()));
                if(Objects.nonNull(player)) {
                    for(Map.Entry<String,Boolean> toggleEntry : toggleMap.entrySet())
                        PersistentTriggerDataProvider.getPlayerCapability(player).writeToggleStatus(channel,toggleEntry.getKey(),
                                toggleEntry.getValue());
                    for(Map.Entry<String,Tuple<List<String>,Integer>> playedOnceEntry : playedOnceMap.entrySet())
                        PersistentTriggerDataProvider.getPlayerCapability(player).setAudioPlayed(channel,playedOnceEntry.getKey(),
                                playedOnceEntry.getValue().getA(),playedOnceEntry.getValue().getB());
                }
            }
        }
        int preferredSortType = buf.readInt();
        if(Objects.nonNull(player))
            PersistentTriggerDataProvider.getPlayerCapability(player).writePreferredSort(preferredSortType);
    }

    public static void runServerChecks() {
        BOSS_BAR_ENTITIES.entrySet().removeIf(entry -> {
            boolean ret = entry.getKey().getPercent()<=0;
            if(ret) {
                synchronized (INSTANTIATED_BOSS_BARS) {
                    INSTANTIATED_BOSS_BARS.remove(entry.getKey());
                    Constants.debugErrorServer("BOSS BAR REMOVED");
                }
            }
            return ret;
        });
        Iterator<Map.Entry<String, ServerTriggerStatus>> itr = SERVER_DATA.entrySet().iterator();
        while(itr.hasNext()) {
            ServerTriggerStatus data = itr.next().getValue();
            if(data.isValid()) data.runChecks();
            else itr.remove();
        }
    }

    public static void bossBarInstantiated(ServerBossInfo info, Class<? extends LivingEntity> entityClass) {
        synchronized (INSTANTIATED_BOSS_BARS) {
            Constants.debugErrorServer("BOSS BAR MADE");
            INSTANTIATED_BOSS_BARS.put(info, entityClass);
        }
    }

    public static void checkIfBossSpawned(LivingEntity entity) {
        synchronized (INSTANTIATED_BOSS_BARS) {
            for (Map.Entry<ServerBossInfo, Class<? extends LivingEntity>> entry : INSTANTIATED_BOSS_BARS.entrySet())
                if (entry.getValue() == entity.getClass())
                    BOSS_BAR_ENTITIES.put(entry.getKey(), entity);
        }
    }

    public static ItemStack recordAudioData(UUID playerUUID, ItemStack recordStack) {
        ServerTriggerStatus data = SERVER_DATA.get(playerUUID.toString());
        if(data.isValid()) return data.recordAudioData(recordStack);
        return ItemStack.EMPTY;
    }

    public static void setPVP(ServerPlayerEntity attacker, String playerUUID) {
        ServerTriggerStatus data = SERVER_DATA.get(playerUUID);
        if(Objects.nonNull(data))
            if(data.isValid()) data.setPVP(attacker);
    }

    private final Map<String, Trigger.DefaultParameter> defaultParameterMap;
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
    private String curStruct;
    private String prevStruct;
    private ServerPlayerEntity attacker = null;
    public ServerTriggerStatus(PacketBuffer buf) {
        this.defaultParameterMap = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,Trigger.DefaultParameter::new);
        this.mappedTriggers = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,buf1 ->
                NetworkUtil.readGenericList(buf1,buf2 -> TomlPart.getByID(NetworkUtil.readString(buf2)).decode(buf2,null))
                .stream().filter(Table.class::isInstance).map(Table.class::cast).collect(Collectors.toList()));
        this.allTriggers = Collections.synchronizedList(this.mappedTriggers.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList()));
        this.victoryTriggers = initVictories();
        this.menuSongs = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,
                buf1 -> NetworkUtil.readGenericList(buf1,NetworkUtil::readString));
        this.server = ServerLifecycleHooks.getCurrentServer();
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

    public boolean isValid() {
        if(this.mappedTriggers.isEmpty() || this.menuSongs.isEmpty() || Objects.isNull(this.server)) return false;
        return Objects.nonNull(this.server.getPlayerList().getPlayer(this.playerUUID));
    }

    public void addChannelInfo(String channel, List<Table> triggers, List<String> menuSongs) {
        this.mappedTriggers.put(channel,triggers);
        this.menuSongs.put(channel,menuSongs);
    }

    public void encodeForServer(PacketBuffer buf) {
        Trigger.encodeDefaultParameters(buf);
        NetworkUtil.writeGenericMap(buf,this.mappedTriggers,NetworkUtil::writeString,(buf1, list) ->
                NetworkUtil.writeGenericList(buf1,list,(buf2, table) -> table.write(buf2)));
        NetworkUtil.writeGenericMap(buf,this.menuSongs,NetworkUtil::writeString,(buf1, list) ->
                NetworkUtil.writeGenericList(buf1,list,NetworkUtil::writeString));
        NetworkUtil.writeString(buf, Minecraft.getInstance().player.getUUID().toString());
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
        ItemStack ret = new ItemStack(ItemRegistry.MUSIC_TRIGGERS_RECORD.get());
        if(recordStack.getItem()== ItemRegistry.BLANK_RECORD.get()) {
            CompoundNBT tag = new CompoundNBT();
            tag.putString("channelFrom", channel);
            tag.putString("trackID", this.currentSongs.get(channel));
            tag.putString("triggerID", this.currentTriggers.get(channel));
            ret.setTag(tag);
            return ret;
        }
        if(this.menuSongs.get(channel).isEmpty()) return ItemStack.EMPTY;
        index = ThreadLocalRandom.current().nextInt(this.menuSongs.get(channel).size());
        CompoundNBT tag = new CompoundNBT();
        tag.putString("channelFrom", channel);
        tag.putString("trackID", this.menuSongs.get(channel).get(index));
        tag.putString("triggerID", "menu");
        ret.setTag(tag);
        return ret;
    }

    private void setPVP(ServerPlayerEntity attacker) {
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

    private void runChecks() {
        runCommands();
        ServerPlayerEntity player = this.server.getPlayerList().getPlayer(this.playerUUID);
        if(Objects.isNull(player)) return;
        this.updatedTriggers.clear();
        List<Table> toRemove = new ArrayList<>();
        BlockPos pos = roundedPos(player);
        synchronized (this.allTriggers) {
            for (Table trigger : this.allTriggers) {
                if (trigger.getName().matches("snow"))
                    potentiallyUpdate(trigger, calculateSnow(player.getLevel(), pos));
                else if (trigger.getName().matches("home"))
                    potentiallyUpdate(trigger, calculateHome(player, pos,
                            getParameterInt(trigger, "detection_range"),
                            getParameterFloat(trigger, "detection_y_ratio")));
                else if (trigger.getName().matches("biome"))
                    potentiallyUpdate(trigger, calculateBiome(trigger, player.getLevel(), pos));
                else if (trigger.getName().matches("structure"))
                    potentiallyUpdate(trigger, calculateStruct(player.getLevel(), pos,
                            getParameterStringList(trigger, "resource_name")));
                else if (trigger.getName().matches("victory"))
                    potentiallyUpdate(trigger, calculateVictory(getParameterString(trigger, "identifier")));
                else if (trigger.getName().matches("mob"))
                    potentiallyUpdate(trigger, calculateMob(trigger, player, pos));
                else if (trigger.getName().matches("pvp"))
                    potentiallyUpdate(trigger, calculatePVP(trigger));
                else if (trigger.getName().matches("raid"))
                    potentiallyUpdate(trigger, calculateRaid(player.getLevel(), pos, trigger.getValOrDefault("level", -1)));
                else toRemove.add(trigger);
            }
        }
        synchronized (this.allTriggers) {
            if (!toRemove.isEmpty()) this.allTriggers.removeAll(toRemove);
        }
        if(!this.updatedTriggers.isEmpty() || (Objects.nonNull(this.prevStruct) &&
                (Objects.isNull(this.curStruct) || !this.prevStruct.matches(this.curStruct)))) {
            String structToSend = Objects.nonNull(this.curStruct) && !(this.curStruct.isEmpty()) ? this.curStruct :
                    "Structure has not been synced";
            new PacketSyncServerInfo(this.updatedTriggers,structToSend).addPlayers(player).send();
            this.prevStruct = structToSend;
        }
    }

    private void runCommands() {
        for(String command : this.commandQueue)
            this.server.getCommands().performCommand(this.server.createCommandSourceStack(),command);
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
        String id = SERVER_TRIGGER_HOLDERS.contains(name) ? getParameterString(trigger,"identifier") : null;
        if(Objects.nonNull(id) && id.matches("not_set")) return null;
        return Objects.nonNull(id) ? name+"-"+id : name;
    }

    private BlockPos roundedPos(PlayerEntity p) {
        return new BlockPos((Math.round(p.getX() * 2) / 2.0), (Math.round(p.getY() * 2) / 2.0), (Math.round(p.getZ() * 2) / 2.0));
    }

    private boolean calculateSnow(ServerWorld world, BlockPos pos) {
        return world.getBiome(pos).getTemperature(pos)<0.2f;
    }

    private boolean calculateHome(ServerPlayerEntity player, BlockPos pos, float range, float yRatio) {
        if(player.getRespawnDimension()!=player.getLevel().dimension()) return false;
        BlockPos bedPos = player.getRespawnPosition();
        if(Objects.isNull(bedPos)) return false;
        AxisAlignedBB box = new AxisAlignedBB(pos.getX()-range,pos.getY()-(range*yRatio), pos.getZ()-range,
                pos.getX()+range,pos.getY()+(range*yRatio),pos.getZ()+range);
        return bedPos.getX()<=box.maxX && bedPos.getX()>=box.minX && bedPos.getY()<=box.maxX &&
                bedPos.getY()>=box.minY && bedPos.getZ()<=box.maxZ && bedPos.getZ()>=box.minZ;
    }

    private boolean calculateBiome(Table biomeTrigger, ServerWorld world, BlockPos pos) {
        if(Objects.isNull(world) || Objects.isNull(pos)) return false;
        Biome biome = world.getBiome(pos);
        if(Objects.isNull(biome.getRegistryName())) return false;
        String curBiome = biome.getRegistryName().toString();
        List<String> resources = biomeTrigger.getValOrDefault("resource_name",Collections.singletonList("any"));
        if(resources.isEmpty()) resources.add("any");
        List<String> categories = biomeTrigger.getValOrDefault("biome_category",Collections.singletonList("any"));
        if(categories.isEmpty()) resources.add("any");
        String rainType = biomeTrigger.getValOrDefault("rain_type","any");
        float rainFall = biomeTrigger.getValOrDefault("biome_rainfall",Float.MIN_VALUE);
        float temperature = biomeTrigger.getValOrDefault("biome_temperature",Float.MIN_VALUE);
        boolean higherRain = biomeTrigger.getValOrDefault("check_higher_rainfall",true);
        boolean colderTemp = biomeTrigger.getValOrDefault("check_lower_temp",false);
        if (resources.contains("any") || partiallyMatches(curBiome,resources)) {
            if (categories.contains("any") || partiallyMatches(biome.getBiomeCategory().getName(),categories)) {
                if (rainType.matches("any") || biome.getPrecipitation().getName().contains(rainType)) {
                    boolean pass = false;
                    if (rainFall==Float.MIN_VALUE) pass = true;
                    else if (biome.getDownfall() > rainFall && higherRain) pass = true;
                    else if (biome.getDownfall() < rainFall && !higherRain) pass = true;
                    if (pass) {
                        float bt = biome.getBaseTemperature();
                        if (temperature==Float.MIN_VALUE) return true;
                        else if (bt >= temperature && !colderTemp) return true;
                        else return bt <= temperature && colderTemp;
                    }
                }
            }
        }
        return false;
    }

    private boolean calculateStruct(ServerWorld world, BlockPos pos, List<String> resourceMatcher) {
        for (Structure<?> structureFeature : net.minecraftforge.registries.ForgeRegistries.STRUCTURE_FEATURES) {
            if(world.structureFeatureManager().getStructureAt(pos,true,structureFeature.getStructure()).isValid()) {
                if(Objects.nonNull(structureFeature.getRegistryName())) {
                    this.curStruct = structureFeature.getRegistryName().toString();
                    break;
                }
            }
        }
        return Objects.nonNull(this.curStruct) && partiallyMatches(this.curStruct,resourceMatcher);
    }

    private boolean calculateMob(Table mobTrigger, ServerPlayerEntity player, BlockPos pos) {
        List<String> resources = getParameterStringList(mobTrigger,"resource_name");
        int num = getParameterInt(mobTrigger,"level");
        if(resources.isEmpty() || num<=0) return false;
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
            String id = getParameterString(mobTrigger,"identifier");
            Set<LivingEntity> bossEntities = BOSS_BAR_ENTITIES.entrySet().stream()
                    .filter(entry -> entry.getKey().isVisible() && entry.getKey().getPlayers().contains(player))
                    .map(Map.Entry::getValue).filter(entity -> entityWhitelist(entity,resources,champion,nbt))
                    .collect(Collectors.toSet());
            if(bossEntities.size()<num) return false;
            pass = checkSpecifics(bossEntities,num,player,checkTarget,hordeTarget,health,hordeHealth);
            if(id.matches("genericBoss")) Constants.debugErrorServer("[{}] DID IT PASS? {}",pass);
            if(Objects.nonNull(victory)) {
                if(pass)
                    for(LivingEntity entity : bossEntities)
                        victory.add(mobTrigger,true,entity);
                victory.setActive(mobTrigger,pass);
            }
            return pass;
        }
        pass = checkMobs(mobTrigger,player,pos,num,range,yRatio,resources,champion,checkTarget,hordeTarget,health,
                hordeHealth,nbt,victory);
        if(Objects.nonNull(victory)) victory.setActive(mobTrigger,pass);
        return pass;
    }

    private boolean checkMobs(Table trigger, ServerPlayerEntity player, BlockPos pos, int num, float range, float yRatio,
                              List<String> resources, List<String> champion, boolean target,
                              float hordeTarget, float health, float hordeHealth, String nbt, Victory victory) {
        AxisAlignedBB box = new AxisAlignedBB(pos.getX()-range,pos.getY()-(range*yRatio),pos.getZ()-range,
                pos.getX()+range,pos.getY()+(range*yRatio),pos.getZ()+range);
        HashSet<LivingEntity> matchedEntities = new HashSet<>(player.getLevel().getEntitiesOfClass(
                LivingEntity.class,box,e -> entityWhitelist(e,resources,champion,nbt)));
        if(matchedEntities.size()<num) return false;
        boolean pass = checkSpecifics(matchedEntities,num,player,target,hordeTarget,health,hordeHealth);
        if(Objects.nonNull(victory) && pass) {
            for(LivingEntity entity : matchedEntities)
                victory.add(trigger,true,entity);
        }
        return pass;
    }

    private boolean entityWhitelist(LivingEntity entity, List<String> resources, List<String> champion, String nbtParser) {
        return Objects.nonNull(entity) && checkEntityName(entity, resources) &&
                checkModExtensions(entity, champion) && checkNBT(entity, nbtParser);
    }

    private boolean checkEntityName(LivingEntity entity, List<String> resources) {
        if(resources.isEmpty()) return false;
        String displayName = entity.getName().getString();
        ResourceLocation id = ForgeRegistries.ENTITIES.getKey(entity.getType());
        if(resources.contains("MOB")) {
            if(!(entity instanceof IMob)) return false;
            List<String> blackList = resources.stream().filter(element -> !element.matches("MOB")).collect(Collectors.toList());
            if(blackList.isEmpty()) return true;
            return !blackList.contains(displayName) && (Objects.isNull(id) || !partiallyMatches(id.toString(),blackList));
        } else if(resources.contains("BOSS")) {
            if(resources.size()==1) return true;
            List<String> whitelist = resources.stream().filter(element -> !element.matches("BOSS")).collect(Collectors.toList());
            return whitelist.contains(displayName) || (Objects.nonNull(id) && partiallyMatches(id.toString(),whitelist));
        }
        return resources.contains(displayName) || (Objects.nonNull(id) && partiallyMatches(id.toString(),resources));
    }

    private boolean checkSpecifics(Collection<LivingEntity> entities, int num, ServerPlayerEntity player, boolean target,
                                   float targetRatio, float health, float healthRatio) {
        return checkTarget(entities,num,target,targetRatio,player) && checkHealth(entities,num,health,healthRatio);
    }

    private boolean checkTarget(Collection<LivingEntity> entities, int num, boolean target, float ratio, ServerPlayerEntity player) {
        if(!target || ratio<=0) return true;
        float counter = 0f;
        for(LivingEntity entity : entities) {
            if(!(entity instanceof MobEntity)) continue;
            if(((MobEntity) entity).getTarget() == player)
                counter++;
        }
        return counter/num>=ratio/100f;
    }

    private boolean checkHealth(Collection<LivingEntity> entities, int num, float health, float ratio) {
        if(health>=100 || ratio<=0) return true;
        float counter = 0f;
        for(LivingEntity entity : entities)
            if(entity.getHealth()/entity.getMaxHealth()<=health/100f)
                counter++;
        return counter/num>=ratio/100f;
    }

    private boolean checkNBT(LivingEntity entity, String nbt) {
        if(nbt.matches("ANY")) return true;
        String[] parts = nbt.split(";");
        try {
            if(parts.length==0) return true;
            if(NBT_MODES.contains(parts[0])) {
                if(parts.length==1) return false;
                CompoundNBT data = entity.serializeNBT();
                switch (parts[0]) {
                    case "KEY_PRESENT" : {
                        if(parts[1].matches("INVERT")) {
                            if(parts.length==2) return false;
                            INBT finalKey = getFinalTag(data,Arrays.copyOfRange(parts,2,parts.length));
                            return Objects.isNull(finalKey) || !(finalKey instanceof CompoundNBT);
                        }
                        INBT finalKey = getFinalTag(data,Arrays.copyOfRange(parts,1,parts.length));
                        return Objects.nonNull(finalKey) && finalKey instanceof CompoundNBT;
                    }
                    case "VAL_PRESENT" : {
                        if(parts[1].matches("INVERT")) {
                            if(parts.length==2) return false;
                            INBT finalVal = getFinalTag(data,Arrays.copyOfRange(parts,2,parts.length));
                            return Objects.isNull(finalVal) || finalVal instanceof CompoundNBT;
                        }
                        INBT finalVal = getFinalTag(data,Arrays.copyOfRange(parts,1,parts.length));
                        return Objects.nonNull(finalVal) && !(finalVal instanceof CompoundNBT);
                    }
                    case "INVERT" : {
                        boolean compare = Boolean.parseBoolean(parts[parts.length-1]);
                        INBT finalVal = getFinalTag(data,Arrays.copyOfRange(parts,1,parts.length-1));
                        if(finalVal instanceof ByteNBT)
                            return (compare && ((ByteNBT)finalVal).getAsByte()==1) ||
                                    (!compare && ((ByteNBT)finalVal).getAsByte()==0);
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
                        INBT finalVal = getFinalTag(data,Arrays.copyOfRange(parts,from,parts.length-1));
                        if(finalVal instanceof CompoundNBT) return false;
                        if(finalVal instanceof ByteNBT) {
                            boolean compare = Boolean.parseBoolean(parts[parts.length-1]);
                            pass = (compare && ((ByteNBT) finalVal).getAsByte() == 0) ||
                                    (!compare && ((ByteNBT) finalVal).getAsByte() == 1);
                        }
                        else if(finalVal instanceof NumberNBT)
                            pass = ((NumberNBT)finalVal).getAsDouble()==Double.parseDouble(parts[parts.length-1]);
                        else if(finalVal instanceof StringNBT) {
                            pass = finalVal.getAsString().trim().toLowerCase()
                                    .matches(parts[parts.length-1].trim().toLowerCase());
                        }
                        return pass || from == 2;
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

    private INBT getFinalTag(CompoundNBT tag, String[] parts) {
        if(!tag.contains(parts[0])) return null;
        if(parts.length==1) return tag.get(parts[0]);
        try {
            return getFinalTag(tag.getCompound(parts[0]), Arrays.copyOfRange(parts,1,parts.length));
        } catch (ClassCastException ignored) {
            return null;
        }
    }

    private boolean numNBT(INBT tag, double comp, boolean greater, boolean equal) {
        if(!(tag instanceof NumberNBT)) return false;
        double val = ((NumberNBT)tag).getAsDouble();
        return greater ? equal ? val>=comp : val>comp : equal ? val<=comp : val<comp;
    }

    @SuppressWarnings("deprecation")
    private boolean checkModExtensions(LivingEntity entity, List<String> champion) {
        if(ModList.get().isLoaded("champions")) {
            if (!champion.isEmpty() && !champion.contains("any")) {
                if(!ChampionCapability.getCapability(entity).isPresent() || !ChampionCapability.getCapability(entity).resolve().isPresent()) return false;
                for(IAffix afix : ChampionCapability.getCapability(entity).resolve().get().getServer().getAffixes()) {
                    if(champion.contains("ALL")) break;
                    if(partiallyMatches(afix.getIdentifier(),champion)) break;
                }
                return false;
            }
        }
        return true;
    }

    private boolean calculateRaid(ServerWorld world, BlockPos pos, int wave) {
        if (Objects.isNull(world) || Objects.isNull(pos) || wave < 0) return false;
        Raid raid = world.getRaidAt(pos);
        if(Objects.isNull(raid)) return false;
        return raid.getGroupsSpawned() >= wave;
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
