package mods.thecomputerizer.musictriggers.server;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.registry.ItemRegistry;
import mods.thecomputerizer.musictriggers.network.NetworkHandler;
import mods.thecomputerizer.musictriggers.network.packets.PacketSyncServerInfo;
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

public class ServerData {
    private static final Map<String, ServerData> SERVER_DATA = new HashMap<>();
    private static final Map<String, List<ServerBossInfo>> QUEUED_BOSS_BARS = new HashMap<>();
    private static final List<String> NBT_MODES = Arrays.asList("KEY_PRESENT","VAL_PRESENT","GREATER","LESSER","EQUAL","INVERT");
    private static final List<String> TRIGGER_HOLDERS = Arrays.asList("difficulty","time","light","height","riding",
            "dimension","biome","structure","mob","victory","gui","zones","pvp","advancement","statistic","command","raid",
            "gamestage","rainintensity","tornado","moon","season");

    public static void initializePlayerChannels(PacketBuffer buf) {
        ServerData data = new ServerData(buf);
        if(SERVER_DATA.containsKey(data.playerUUID.toString()))
            data.bossInfo.addAll(SERVER_DATA.get(data.playerUUID.toString()).bossInfo);
        SERVER_DATA.put(data.playerUUID.toString(),data);
        if(data.isValid() && QUEUED_BOSS_BARS.containsKey(data.playerUUID.toString())) {
            data.bossInfo.addAll(QUEUED_BOSS_BARS.get(data.playerUUID.toString()));
            QUEUED_BOSS_BARS.remove(data.playerUUID.toString());
        }
    }

    public static void decodeDynamicInfo(PacketBuffer buf) {
        String playerUUID = NetworkUtil.readString(buf);
        int size = buf.readInt();
        for(int i=0;i<size;i++) {
            String channel = NetworkUtil.readString(buf);
            List<String> commands = NetworkUtil.readGenericList(buf,NetworkUtil::readString);
            boolean isPlaying = buf.readBoolean();
            String track = isPlaying ? NetworkUtil.readString(buf) : null;
            String trigger = isPlaying ? NetworkUtil.readString(buf) : null;
            ServerData data = SERVER_DATA.get(playerUUID);
            if(Objects.nonNull(data) && data.isValid()) data.updateDynamicInfo(channel,commands,track,trigger);
        }
    }

    public static void runServerChecks() {
        Iterator<Map.Entry<String,ServerData>> itr = SERVER_DATA.entrySet().iterator();
        while(itr.hasNext()) {
            ServerData data = itr.next().getValue();
            if(data.isValid()) data.runChecks();
            else itr.remove();
        }
    }

    public static void addBossBarTracking(UUID playerUUID, ServerBossInfo info) {
        ServerData data = SERVER_DATA.get(playerUUID.toString());
        if(Objects.nonNull(data)) {
            if (data.isValid()) data.bossInfo.add(info);
        } else {
            QUEUED_BOSS_BARS.putIfAbsent(playerUUID.toString(),new ArrayList<>());
            if(!QUEUED_BOSS_BARS.get(playerUUID.toString()).contains(info))
                QUEUED_BOSS_BARS.get(playerUUID.toString()).add(info);
        }
    }

    public static void removeBossBarTracking(UUID playerUUID, ServerBossInfo info) {
        ServerData data = SERVER_DATA.get(playerUUID.toString());
        if(Objects.nonNull(data)) {
            if (data.isValid()) data.bossInfo.remove(info);
        } else if(QUEUED_BOSS_BARS.containsKey(playerUUID.toString()))
            QUEUED_BOSS_BARS.get(playerUUID.toString()).remove(info);
    }

    public static ItemStack recordAudioData(UUID playerUUID, ItemStack recordStack) {
        ServerData data = SERVER_DATA.get(playerUUID.toString());
        if(data.isValid()) return data.recordAudioData(recordStack);
        return ItemStack.EMPTY;
    }

    private final Map<String, List<Table>> mappedTriggers;
    private final List<Table> allTriggers;
    private final Map<Table, Boolean> triggerStatus = new HashMap<>();
    private final Map<String, Map<String, Boolean>> updatedTriggers = new HashMap<>();
    private final Map<String, List<String>> menuSongs;
    private final List<ServerBossInfo> bossInfo = new ArrayList<>();
    private final UUID playerUUID;
    private final MinecraftServer server;
    private final List<String> commandQueue = new ArrayList<>();
    private final Map<String, String> currentSongs = new HashMap<>();
    private final Map<String, String> currentTriggers = new HashMap<>();
    private String curStruct;
    private String prevStruct;
    public ServerData(PacketBuffer buf) {
        this.mappedTriggers = NetworkUtil.readGenericMap(buf, NetworkUtil::readString, buf1 ->
                NetworkUtil.readGenericList(buf1,buf2 -> TomlPart.getByID(NetworkUtil.readString(buf2)).decode(buf2,null))
                .stream().filter(Table.class::isInstance).map(Table.class::cast).collect(Collectors.toList()));
        this.allTriggers = mappedTriggers.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        this.menuSongs = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,
                buf1 -> NetworkUtil.readGenericList(buf1,NetworkUtil::readString));
        this.server = ServerLifecycleHooks.getCurrentServer();
        this.playerUUID = UUID.fromString(NetworkUtil.readString(buf));
    }

    public ServerData() {
        this.mappedTriggers = new HashMap<>();
        this.allTriggers = new ArrayList<>();
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
        ItemStack ret = new ItemStack(ItemRegistry.MUSIC_TRIGGERS_RECORD.get());
        if (recordStack.getItem()== ItemRegistry.BLANK_RECORD.get()) {
            CompoundNBT tag = new CompoundNBT();
            tag.putString("channelFrom", channel);
            tag.putString("trackID", this.currentSongs.get(channel));
            tag.putString("triggerID", this.currentTriggers.get(channel));
            ret.setTag(tag);
            return ret;
        }
        if (this.menuSongs.get(channel).isEmpty()) return ItemStack.EMPTY;
        index = ThreadLocalRandom.current().nextInt(this.menuSongs.get(channel).size());
        CompoundNBT tag = new CompoundNBT();
        tag.putString("channelFrom", channel);
        tag.putString("trackID", this.menuSongs.get(channel).get(index));
        tag.putString("triggerID", "menu");
        ret.setTag(tag);
        return ret;
    }

    private void runChecks() {
        runCommands();
        ServerPlayerEntity player = this.server.getPlayerList().getPlayer(this.playerUUID);
        if(Objects.isNull(player)) return;
        this.updatedTriggers.clear();
        List<Table> toRemove = new ArrayList<>();
        BlockPos pos = roundedPos(player);
        for (Table trigger : this.allTriggers) {
            if(trigger.getName().matches("snow"))
                potentiallyUpdate(trigger, calculateSnow(player.getLevel(),pos));
            else if (trigger.getName().matches("home"))
                potentiallyUpdate(trigger, calculateHome(player, pos, trigger.getValOrDefault("detection_range", 16)));
            else if (trigger.getName().matches("biome"))
                potentiallyUpdate(trigger, calculateBiome(trigger,player.getLevel(),pos));
            else if (trigger.getName().matches("structure"))
                potentiallyUpdate(trigger, calculateStruct(player.getLevel(), pos, trigger.getValOrDefault("resource_name", Collections.singletonList("any"))));
            else if (trigger.getName().matches("mob"))
                potentiallyUpdate(trigger, calculateMob(trigger, player, pos));
            else if (trigger.getName().matches("raid"))
                potentiallyUpdate(trigger, calculateRaid(player.getLevel(),pos,trigger.getValOrDefault("level",-1)));
            else toRemove.add(trigger);
        }
        this.bossInfo.removeIf(info -> info.getPercent()<=0 || !info.visible || info.getName().getString().matches("Raid"));
        if(!toRemove.isEmpty()) this.allTriggers.removeAll(toRemove);
        if(!this.updatedTriggers.isEmpty() || (Objects.nonNull(this.prevStruct) &&
                (Objects.isNull(this.curStruct) || !this.prevStruct.matches(this.curStruct)))) {
            String structToSend = Objects.nonNull(this.curStruct) && !(this.curStruct.length()==0) ? this.curStruct :
                    "Structure has not been synced";
            NetworkHandler.sendTo(new PacketSyncServerInfo(this.updatedTriggers, structToSend), player);
            this.prevStruct = structToSend;
        }
    }

    private void runCommands() {
        for(String command : this.commandQueue)
            this.server.getCommands().performCommand(this.server.createCommandSourceStack(),command);
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

    private BlockPos roundedPos(PlayerEntity p) {
        return new BlockPos((Math.round(p.getX() * 2) / 2.0), (Math.round(p.getY() * 2) / 2.0), (Math.round(p.getZ() * 2) / 2.0));
    }

    private boolean calculateSnow(ServerWorld world, BlockPos pos) {
        return world.getBiome(pos).getTemperature(pos)<0.2f;
    }

    private boolean calculateHome(ServerPlayerEntity player, BlockPos pos, int range) {
        return player.getLevel().dimension()==player.getRespawnDimension() && Objects.nonNull(player.getRespawnPosition()) &&
                player.getRespawnPosition().closerThan(pos,range);
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
                if(structureFeature.getRegistryName()!=null) {
                    this.curStruct = structureFeature.getRegistryName().toString();
                    break;
                }
            }
        }
        return Objects.nonNull(this.curStruct) && partiallyMatches(this.curStruct,resourceMatcher);
    }

    private boolean calculateMob(Table mobTrigger, ServerPlayerEntity player, BlockPos pos) {
        List<String> resources = mobTrigger.getValOrDefault("resource_name",Collections.singletonList("any"));
        if(resources.isEmpty()) return false;
        List<String> infernal = mobTrigger.getValOrDefault("infernal", Collections.singletonList("any"));
        List<String> champion = mobTrigger.getValOrDefault("champion",Collections.singletonList("any"));
        boolean checkTarget = mobTrigger.getValOrDefault("mob_targeting",true);
        int hordeTarget = mobTrigger.getValOrDefault("horde_targeting_percentage",50);
        int num = mobTrigger.getValOrDefault("level",1);
        int range = mobTrigger.getValOrDefault("detection_range",16);
        int health = mobTrigger.getValOrDefault("health",100);
        int hordeHealth = mobTrigger.getValOrDefault("horde_health_percentage",50);
        String nbt = mobTrigger.getValOrDefault("mob_nbt","any");
        if (resources.contains("BOSS")) {
            List<ServerBossInfo> correctBosses = this.bossInfo.stream().filter(
                            info -> !info.getName().getString().matches("Raid") && (resources.size()==1 || partiallyMatches(info.getName().getString(),resources
                                    .stream().filter(element -> !element.matches("BOSS")).collect(Collectors.toList()))))
                    .collect(Collectors.toList());
            ServerBossInfo[] passedBosses = new ServerBossInfo[num];
            for (int i = 0; i < num; i++) {
                if (i >= correctBosses.size())
                    return false;
                passedBosses[i] = correctBosses.get(i);
            }
            return checkBossHealth(passedBosses, health, hordeHealth);
        }
        return checkMobs(player,pos,num,range,resources,infernal,champion,checkTarget,hordeTarget,health,hordeHealth,nbt);
    }

    private boolean checkMobs(ServerPlayerEntity player, BlockPos pos, int num, int range, List<String> resources,
                              List<String> infernal, List<String> champion, boolean target, int hordeTarget, int health,
                              int hordeHealth, String nbt) {
        if(num<=0) return false;
        LivingEntity[] passedEntities = new LivingEntity[num];
        AxisAlignedBB box = new AxisAlignedBB(pos.getX()-range,pos.getY()-range,pos.getZ()-range,
                pos.getX()+range,pos.getY()+range,pos.getZ()+range);
        List<LivingEntity> livingWithBlacklist = player.getLevel().getEntitiesOfClass(
                LivingEntity.class,box,e -> e!=player && checkEntityName(e,resources));
        livingWithBlacklist.removeIf(living -> !checkNBT(living,nbt) || !checkModExtensions(living,infernal,champion));
        for(int i=0;i<num;i++) {
            if(i>=livingWithBlacklist.size())
                return false;
            passedEntities[i] = livingWithBlacklist.get(i);
        }
        return checkTarget(passedEntities,target,hordeTarget,player) && checkHealth(passedEntities,health,hordeHealth);
    }

    private boolean checkEntityName(LivingEntity entity, List<String> resources) {
        String displayName = entity.getName().getString();
        ResourceLocation id = ForgeRegistries.ENTITIES.getKey(entity.getType());
        if(resources.contains("MOB")) {
            if(!(entity instanceof IMob)) return false;
            List<String> blackList = resources.stream().filter(element -> !element.matches("MOB")).collect(Collectors.toList());
            return !blackList.contains(displayName) && (Objects.isNull(id) || !partiallyMatches(id.toString(),blackList));
        }
        return resources.contains(displayName) || (Objects.nonNull(id) && partiallyMatches(id.toString(),resources));
    }

    private boolean partiallyMatches(String thing, List<String> partials) {
        for(String partial : partials)
            if(thing.contains(partial)) return true;
        return false;
    }

    private boolean checkTarget(LivingEntity[] entities, boolean target, float ratio, ServerPlayerEntity player) {
        if(!target || ratio<=0) return true;
        float size = entities.length;
        float counter = 0f;
        for(LivingEntity entity : entities)
            if(entity instanceof MobEntity)
                if(((MobEntity)entity).getTarget()==player)
                    counter++;
        return counter/size>=ratio/100f;
    }

    private boolean checkHealth(LivingEntity[] entities, float health, float ratio) {
        if(health>=100 || ratio<=0) return true;
        float size = entities.length;
        float counter = 0f;
        for(LivingEntity entity : entities)
            if(entity.getHealth()/entity.getMaxHealth()<=health)
                counter++;
        return counter/size>=ratio/100f;
    }

    private boolean checkBossHealth(ServerBossInfo[] info, float health, float ratio) {
        if(health>=100 || ratio<=0) return true;
        float size = info.length;
        float counter = 0f;
        for(ServerBossInfo inf : info)
            if(inf.getPercent()<=health)
                counter++;
        return counter/size>=ratio/100f;
    }

    private boolean checkNBT(LivingEntity entity, String nbt) {
        if(nbt.matches("any")) return true;
        String[] parts = nbt.split(":");
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
                        INBT finalVal = getFinalTag(data,Arrays.copyOfRange(parts,from,parts.length));
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
    private boolean checkModExtensions(LivingEntity entity, List<String> infernal, List<String> champion) {
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
        if(ModList.get().isLoaded("infernalmobs")) {
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

    private boolean calculateRaid(ServerWorld world, BlockPos pos, int wave) {
        if (Objects.isNull(world) || Objects.isNull(pos) || wave < 0) return false;
        Raid raid = world.getRaidAt(pos);
        if(Objects.isNull(raid)) return false;
        return raid.getGroupsSpawned() >= wave;
    }
}
