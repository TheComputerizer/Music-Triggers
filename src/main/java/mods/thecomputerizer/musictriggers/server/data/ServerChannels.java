package mods.thecomputerizer.musictriggers.server.data;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import c4.champions.common.capability.CapabilityChampionship;
import c4.champions.common.capability.IChampionship;
import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.network.NetworkHandler;
import mods.thecomputerizer.musictriggers.network.packets.PacketSyncServerInfo;
import mods.thecomputerizer.musictriggers.registry.ItemRegistry;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.TomlPart;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
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
import java.util.stream.Stream;

public class ServerChannels {
    private static final Map<String, ServerChannels> SERVER_DATA = new HashMap<>();
    private static final Map<String, List<BossInfoServer>> QUEUED_BOSS_BARS = new HashMap<>();
    private static final List<String> NBT_MODES = Arrays.asList("KEY_PRESENT","VAL_PRESENT","GREATER","LESSER","EQUAL","INVERT");
    private static final List<String> TRIGGER_HOLDERS = Arrays.asList("structure","mob","victory","pvp");

    public static void initializePlayerChannels(ByteBuf buf) {
        ServerChannels data = new ServerChannels(buf);
        if(SERVER_DATA.containsKey(data.playerUUID.toString()))
            data.bossInfo.addAll(SERVER_DATA.get(data.playerUUID.toString()).bossInfo);
        SERVER_DATA.put(data.playerUUID.toString(),data);
        if(data.isValid() && QUEUED_BOSS_BARS.containsKey(data.playerUUID.toString())) {
            data.bossInfo.addAll(QUEUED_BOSS_BARS.get(data.playerUUID.toString()));
            QUEUED_BOSS_BARS.remove(data.playerUUID.toString());
        }
    }

    public static void decodeDynamicInfo(ByteBuf buf) {
        String playerUUID = NetworkUtil.readString(buf);
        int size = buf.readInt();
        for(int i=0;i<size;i++) {
            String channel = NetworkUtil.readString(buf);
            List<String> commands = NetworkUtil.readGenericList(buf,NetworkUtil::readString);
            boolean isPlaying = buf.readBoolean();
            String track = isPlaying ? NetworkUtil.readString(buf) : null;
            String trigger = isPlaying ? NetworkUtil.readString(buf) : null;
            ServerChannels data = SERVER_DATA.get(playerUUID);
            if(Objects.nonNull(data) && data.isValid()) data.updateDynamicInfo(channel,commands,track,trigger);
        }
    }

    public static void runServerChecks() {
        Iterator<Map.Entry<String, ServerChannels>> itr = SERVER_DATA.entrySet().iterator();
        while(itr.hasNext()) {
            ServerChannels data = itr.next().getValue();
            if(data.isValid()) data.runChecks();
            else itr.remove();
        }
    }

    public static void addBossBarTracking(UUID playerUUID, BossInfoServer info) {
        ServerChannels data = SERVER_DATA.get(playerUUID.toString());
        if(Objects.nonNull(data)) {
            if (data.isValid()) data.bossInfo.add(info);
        } else {
            QUEUED_BOSS_BARS.putIfAbsent(playerUUID.toString(),new ArrayList<>());
            if(!QUEUED_BOSS_BARS.get(playerUUID.toString()).contains(info))
                QUEUED_BOSS_BARS.get(playerUUID.toString()).add(info);
        }
    }

    public static void removeBossBarTracking(UUID playerUUID, BossInfoServer info) {
        ServerChannels data = SERVER_DATA.get(playerUUID.toString());
        if(Objects.nonNull(data)) {
            if (data.isValid()) data.bossInfo.remove(info);
        } else if(QUEUED_BOSS_BARS.containsKey(playerUUID.toString()))
            QUEUED_BOSS_BARS.get(playerUUID.toString()).remove(info);
    }

    public static ItemStack recordAudioData(UUID playerUUID, ItemStack recordStack) {
        ServerChannels data = SERVER_DATA.get(playerUUID.toString());
        if(data.isValid()) return data.recordAudioData(recordStack);
        return ItemStack.EMPTY;
    }

    public static void setPVP(EntityPlayerMP attacker, String playerUUID) {
        ServerChannels data = SERVER_DATA.get(playerUUID);
        if(Objects.nonNull(data))
            if(data.isValid()) data.setPVP(attacker);
    }

    private final Map<String, List<Table>> mappedTriggers;
    private final List<Table> allTriggers;
    private final Map<Table, Boolean> triggerStatus = new HashMap<>();
    private final Map<String, Map<String, Boolean>> updatedTriggers = new HashMap<>();
    private final Map<String, Victory> victoryTriggers;
    private final Map<String, List<String>> menuSongs;
    private final List<BossInfoServer> bossInfo = new ArrayList<>();
    private final UUID playerUUID;
    private final MinecraftServer server;
    private final List<String> commandQueue = new ArrayList<>();
    private final Map<String, String> currentSongs = new HashMap<>();
    private final Map<String, String> currentTriggers = new HashMap<>();
    private EntityPlayerMP attacker = null;
    public ServerChannels(ByteBuf buf) {
        this.mappedTriggers = NetworkUtil.readGenericMap(buf, NetworkUtil::readString, buf1 ->
                NetworkUtil.readGenericList(buf1,buf2 -> TomlPart.getByID(NetworkUtil.readString(buf2)).decode(buf2,null))
                .stream().filter(Table.class::isInstance).map(Table.class::cast).collect(Collectors.toList()));
        this.allTriggers = mappedTriggers.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        this.victoryTriggers = initVictories();
        this.menuSongs = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,
                buf1 -> NetworkUtil.readGenericList(buf1,NetworkUtil::readString));
        this.server = FMLCommonHandler.instance().getMinecraftServerInstance();
        this.playerUUID = UUID.fromString(NetworkUtil.readString(buf));
    }

    private HashMap<String, Victory> initVictories() {
        HashMap<String, Victory> ret = new HashMap<>();
        for(Table trigger : this.allTriggers) {
            if (trigger.getName().matches("victory")) {
                String id = trigger.getValOrDefault("identifier","not_set");
                if(!id.matches("not_set")) {
                    Stream<Table> references = this.allTriggers.stream().filter(table -> {
                        if(!table.getName().matches("mob") && !table.getName().matches("pvp")) return false;
                        return !table.getValOrDefault("identifier","not_set").matches("not_set");
                    });
                    if(references.findAny().isPresent()) ret.put(id,new Victory(references,
                            trigger.getValOrDefault("victory_timeout",20)));
                    else references.close();
                }
            }
        }
        return ret;
    }

    public ServerChannels() {
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
        if (this.currentSongs.isEmpty() || this.currentTriggers.isEmpty()) return ItemStack.EMPTY;
        int index = ThreadLocalRandom.current().nextInt(this.currentSongs.size());
        String channel = null;
        for (String ch : this.currentSongs.keySet()) {
            if (index == 0) {
                channel = ch;
                break;
            }
            index--;
        }
        if (Objects.isNull(channel)) return ItemStack.EMPTY;
        ItemStack ret = new ItemStack(ItemRegistry.MUSIC_TRIGGERS_RECORD);
        if (recordStack.getItem()== ItemRegistry.BLANK_RECORD) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("channelFrom", channel);
            tag.setString("trackID", this.currentSongs.get(channel));
            tag.setString("triggerID", this.currentTriggers.get(channel));
            ret.setTagCompound(tag);
            return ret;
        }
        if (this.menuSongs.get(channel).isEmpty()) return ItemStack.EMPTY;
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

    @SuppressWarnings("ConstantValue")
    private void runChecks() {
        this.updatedTriggers.clear();
        EntityPlayerMP player = this.server.getPlayerList().getPlayerByUUID(this.playerUUID);
        if(Objects.nonNull(player)) {
            runCommands();
            List<Table> toRemove = new ArrayList<>();
            BlockPos pos = roundedPos(player);
            for (Table trigger : this.allTriggers) {
                if (trigger.getName().matches("home"))
                    potentiallyUpdate(trigger, calculateHome(player, pos, trigger.getValOrDefault("detection_range", 16)));
                else if (trigger.getName().matches("structure"))
                    potentiallyUpdate(trigger, calculateStruct(player.getServerWorld(), pos, trigger.getValOrDefault("resource_name", Collections.singletonList("any"))));
                else if (trigger.getName().matches("victory"))
                    potentiallyUpdate(trigger, calculateVictory(trigger.getValOrDefault("identifier", "not_set")));
                else if (trigger.getName().matches("mob"))
                    potentiallyUpdate(trigger, calculateMob(trigger, player, pos));
                else if (trigger.getName().matches("pvp"))
                    potentiallyUpdate(trigger, calculatePVP(trigger));
                else toRemove.add(trigger);
            }
            this.attacker = null;
            this.bossInfo.removeIf(info -> info.getPercent() <= 0 || !info.visible);
            if (!toRemove.isEmpty()) this.allTriggers.removeAll(toRemove);
            if (!this.updatedTriggers.isEmpty())
                NetworkHandler.sendToPlayer(new PacketSyncServerInfo.Message(this.updatedTriggers), player);
        }
    }

    private void runCommands() {
        for(String command : this.commandQueue)
            this.server.getCommandManager().executeCommand(this.server,command);
        this.commandQueue.clear();
    }

    private void potentiallyUpdate(Table trigger, boolean pass) {
        if(trigger.getValOrDefault("not",false)) pass = !pass;
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
            } else this.allTriggers.remove(trigger);
        }
    }

    private String triggerWithID(Table trigger) {
        String name = trigger.getName();
        String id = TRIGGER_HOLDERS.contains(name) ? trigger.getValOrDefault("identifier","not_set") : null;
        if(Objects.nonNull(id) && id.matches("not_set")) return null;
        return Objects.nonNull(id) ? name+"-"+id : name;
    }

    private BlockPos roundedPos(EntityPlayer p) {
        return new BlockPos((Math.round(p.posX * 2) / 2.0), (Math.round(p.posY * 2) / 2.0), (Math.round(p.posZ * 2) / 2.0));
    }

    @SuppressWarnings("ConstantValue")
    private boolean calculateHome(EntityPlayerMP player, BlockPos pos, int range) {
        if(Objects.isNull(player.getBedLocation(player.dimension))) return false;
        return player.getBedLocation(player.dimension).getDistance(pos.getX(),pos.getY(),pos.getZ())<=range;
    }

    private boolean calculateStruct(WorldServer world, BlockPos pos, List<String> resourceMatcher) {
        for(String actualStructure : resourceMatcher)
            if(world.getChunkProvider().isInsideStructure(world, actualStructure, pos)) return true;
        return false;
    }

    private boolean calculateMob(Table mobTrigger, EntityPlayerMP player, BlockPos pos) {
        List<String> resources = mobTrigger.getValOrDefault("resource_name",Collections.singletonList("any"));
        int num = mobTrigger.getValOrDefault("level",1);
        if(resources.isEmpty() || num<=0) return false;
        List<String> infernal = mobTrigger.getValOrDefault("infernal", Collections.singletonList("any"));
        List<String> champion = mobTrigger.getValOrDefault("champion",Collections.singletonList("any"));
        boolean checkTarget = mobTrigger.getValOrDefault("mob_targeting",true);
        int hordeTarget = mobTrigger.getValOrDefault("horde_targeting_percentage",50);
        int range = mobTrigger.getValOrDefault("detection_range",16);
        int health = mobTrigger.getValOrDefault("health",100);
        int hordeHealth = mobTrigger.getValOrDefault("horde_health_percentage",50);
        String nbt = mobTrigger.getValOrDefault("mob_nbt","any");
        String victoryID = mobTrigger.getValOrDefault("victory_id","not_set");
        if (resources.contains("BOSS")) {
            HashSet<BossInfoServer> matchedBosses = new HashSet<>(resources.size() == 1 ? this.bossInfo : this.bossInfo.stream().filter(
                    info -> partiallyMatches(info.getName().getUnformattedText(),resources.stream()
                            .filter(element -> !element.matches("BOSS")).collect(Collectors.toList())))
                    .collect(Collectors.toList()));
            if(matchedBosses.size()<num) return false;
            boolean pass = checkBossHealth(matchedBosses, num, health, hordeHealth);
            Victory victory = this.victoryTriggers.get(victoryID);
            if(Objects.nonNull(victory)) {
                if(pass)
                    for(BossInfoServer info : matchedBosses)
                        victory.add(mobTrigger, info);
                victory.setActive(mobTrigger,pass);
            }
            return pass;
        }
        return checkMobs(mobTrigger,player,pos,num,range,resources,infernal,champion,checkTarget,hordeTarget,health,
                hordeHealth,nbt,victoryID);
    }

    private boolean checkMobs(Table trigger, EntityPlayerMP player, BlockPos pos, int num, int range, List<String> resources,
                              List<String> infernal, List<String> champion, boolean target, int hordeTarget, int health,
                              int hordeHealth, String nbt, String victoryID) {
        AxisAlignedBB box = new AxisAlignedBB(pos.getX()-range,pos.getY()-range,pos.getZ()-range,
                pos.getX()+range,pos.getY()+range,pos.getZ()+range);
        HashSet<EntityLiving> matchedEntities = new HashSet<>(player.getServerWorld().getEntitiesWithinAABB(
                EntityLiving.class,box,e -> entityWhitelist(e,resources,infernal,champion,nbt)));
        if(matchedEntities.size()<num) return false;
        boolean pass = checkSpecifics(matchedEntities,num,player,target,hordeTarget,health,hordeHealth);
        Victory victory = this.victoryTriggers.get(victoryID);
        if(Objects.nonNull(victory)) {
            if(pass)
                for(EntityLiving entity : matchedEntities)
                    victory.add(trigger,entity);
            victory.setActive(trigger,pass);
        }
        return pass;
    }

    private boolean entityWhitelist(EntityLiving entity, List<String> resources, List<String> infernal,
                                    List<String> champion, String nbtParser) {
        return Objects.nonNull(entity) && checkEntityName(entity, resources) &&
                checkModExtensions(entity, infernal, champion) && checkNBT(entity, nbtParser);
    }

    private boolean checkEntityName(EntityLiving entity, List<String> resources) {
        if(resources.isEmpty()) return false;
        String displayName = entity.getName();
        ResourceLocation id = EntityList.getKey(entity);
        if(resources.contains("MOB")) {
            if(!(entity instanceof IMob)) return false;
            List<String> blackList = resources.stream().filter(element -> !element.matches("MOB")).collect(Collectors.toList());
            if(blackList.isEmpty()) return true;
            return !blackList.contains(displayName) && (Objects.isNull(id) || !partiallyMatches(id.toString(),blackList));
        }
        return resources.contains(displayName) || (Objects.nonNull(id) && partiallyMatches(id.toString(),resources));
    }
    private boolean checkSpecifics(HashSet<EntityLiving> entities, int num, EntityPlayerMP player, boolean target,
                                   float targetRatio, float health, float healthRatio) {
        return checkTarget(entities,num,target,targetRatio,player) && checkHealth(entities,num,health,healthRatio);
    }

    private boolean checkTarget(HashSet<EntityLiving> entities, int num, boolean target, float ratio, EntityPlayerMP player) {
        if(!target || ratio<=0) return true;
        float counter = 0f;
        for(EntityLiving entity : entities) {
            if(Objects.isNull(entity) || Objects.isNull(entity.getAttackTarget())) continue;
            if(entity.getAttackTarget() == player)
                counter++;
        }
        return counter/num>=ratio/100f;
    }

    private boolean checkHealth(HashSet<EntityLiving> entities, int num, float health, float ratio) {
        if(health>=100 || ratio<=0) return true;
        float counter = 0f;
        for(EntityLiving entity : entities)
            if(entity.getHealth()/entity.getMaxHealth()<=health/100f)
                counter++;
        return counter/num>=ratio/100f;
    }

    private boolean checkBossHealth(HashSet<BossInfoServer> bars, int num, float health, float ratio) {
        if(health>=100 || ratio<=0) return true;
        float counter = 0f;
        for(BossInfoServer bar : bars)
            if(bar.getPercent()<=health)
                counter++;
        return counter/num>=ratio/100f;
    }

    private boolean checkNBT(EntityLiving entity, String nbt) {
        if(nbt.matches("any")) return true;
        String[] parts = nbt.split(":");
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
                        NBTBase finalVal = getFinalTag(data,Arrays.copyOfRange(parts,from,parts.length));
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

    private boolean checkModExtensions(EntityLiving entity, List<String> infernal, List<String> champion) {
        if(Loader.isModLoaded("champions")) {
            if (!champion.isEmpty() && !champion.contains("any")) {
                IChampionship champ = CapabilityChampionship.getChampionship(entity);
                if(Objects.isNull(champ)) return false;
                else if(Objects.nonNull(champ.getName()))
                    if(!champion.contains("ALL"))
                        if(!partiallyMatches(champ.getName(),champion)) return false;
            }
        }
        if(Loader.isModLoaded("infernalmobs")) {
            if(!infernal.isEmpty() && !infernal.contains("any")) {
                if(InfernalMobsCore.getIsRareEntity(entity)) {
                    if(infernal.contains("ALL")) return true;
                    String[] names = InfernalMobsCore.getMobModifiers(entity).getDisplayNames();
                    for(String name : names)
                        if(partiallyMatches(name,infernal))
                            return true;
                }
                return false;
            }
        }
        return true;
    }

    private boolean calculateVictory(String id) {
        if(id.matches("not_set") || !this.victoryTriggers.containsKey(id)) return false;
        return this.victoryTriggers.get(id).runCalculation();
    }

    private boolean calculatePVP(Table trigger) {
        boolean pass = Objects.nonNull(this.attacker);
        String victoryID = trigger.getValOrDefault("victory_id","not_set");
        Victory victory = this.victoryTriggers.get(victoryID);
        if(Objects.nonNull(this.attacker)) {
            if(Objects.nonNull(victory)) {
                victory.add(trigger,this.attacker);
                victory.setActive(trigger,true);
            }
        } else victory.setActive(trigger,false);
        return pass;
    }

    private boolean partiallyMatches(String thing, List<String> partials) {
        for(String partial : partials)
            if(thing.contains(partial)) return true;
        return false;
    }
}
