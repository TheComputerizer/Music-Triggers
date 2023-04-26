package mods.thecomputerizer.musictriggers.server.data;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.registry.ItemRegistry;
import mods.thecomputerizer.musictriggers.network.NetworkHandler;
import mods.thecomputerizer.musictriggers.network.packets.PacketSyncServerInfo;
import mods.thecomputerizer.musictriggers.server.RegUtil;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.TomlPart;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.Level;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerChannels {
    private static final Map<String, ServerChannels> SERVER_DATA = new HashMap<>();
    private static final Map<String, List<ServerBossEvent>> QUEUED_BOSS_BARS = new HashMap<>();
    private static final List<String> NBT_MODES = Arrays.asList("KEY_PRESENT","VAL_PRESENT","GREATER","LESSER","EQUAL","INVERT");
    private static final List<String> SERVER_TRIGGER_HOLDERS = Arrays.asList("biome","structure","mob","victory","pvp","raid");

    public static void initializePlayerChannels(MinecraftServer server, FriendlyByteBuf buf) {
        ServerChannels data = new ServerChannels(server,buf);
        if(SERVER_DATA.containsKey(data.playerUUID.toString()))
            data.bossInfo.addAll(SERVER_DATA.get(data.playerUUID.toString()).bossInfo);
        SERVER_DATA.put(data.playerUUID.toString(),data);
        if(data.isValid() && QUEUED_BOSS_BARS.containsKey(data.playerUUID.toString())) {
            data.bossInfo.addAll(QUEUED_BOSS_BARS.get(data.playerUUID.toString()));
            QUEUED_BOSS_BARS.remove(data.playerUUID.toString());
        }
    }

    public static void decodeDynamicInfo(FriendlyByteBuf buf) {
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

    public static void addBossBarTracking(UUID playerUUID, ServerBossEvent info) {
        ServerChannels data = SERVER_DATA.get(playerUUID.toString());
        if(Objects.nonNull(data)) {
            if (data.isValid()) data.bossInfo.add(info);
        } else {
            QUEUED_BOSS_BARS.putIfAbsent(playerUUID.toString(),new ArrayList<>());
            if(!QUEUED_BOSS_BARS.get(playerUUID.toString()).contains(info))
                QUEUED_BOSS_BARS.get(playerUUID.toString()).add(info);
        }
    }

    public static void removeBossBarTracking(UUID playerUUID, ServerBossEvent info) {
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

    public static void setPVP(ServerPlayer attacker, String playerUUID) {
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
    private final List<ServerBossEvent> bossInfo = new ArrayList<>();
    private final UUID playerUUID;
    private final MinecraftServer server;
    private final List<String> commandQueue = new ArrayList<>();
    private final Map<String, String> currentSongs = new HashMap<>();
    private final Map<String, String> currentTriggers = new HashMap<>();
    private String curStruct;
    private String prevStruct;
    private ServerPlayer attacker = null;
    public ServerChannels(MinecraftServer server, FriendlyByteBuf buf) {
        this.mappedTriggers = NetworkUtil.readGenericMap(buf, NetworkUtil::readString, buf1 ->
                NetworkUtil.readGenericList(buf1,buf2 -> TomlPart.getByID(NetworkUtil.readString(buf2)).decode(buf2,null))
                .stream().filter(Table.class::isInstance).map(Table.class::cast).collect(Collectors.toList()));
        this.allTriggers = mappedTriggers.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        this.victoryTriggers = initVictories();
        this.menuSongs = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,
                buf1 -> NetworkUtil.readGenericList(buf1,NetworkUtil::readString));
        this.server = server;
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

    public boolean isValid() {
        if(this.mappedTriggers.isEmpty() || this.menuSongs.isEmpty() || Objects.isNull(this.server)) return false;
        return Objects.nonNull(this.server.getPlayerList().getPlayer(this.playerUUID));
    }

    public void addChannelInfo(String channel, List<Table> triggers, List<String> menuSongs) {
        this.mappedTriggers.put(channel,triggers);
        this.menuSongs.put(channel,menuSongs);
    }

    public void encodeForServer(FriendlyByteBuf buf) {
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
        ItemStack ret = new ItemStack(ItemRegistry.MUSIC_TRIGGERS_RECORD);
        if (recordStack.getItem()== ItemRegistry.BLANK_RECORD) {
            CompoundTag tag = new CompoundTag();
            tag.putString("channelFrom", channel);
            tag.putString("trackID", this.currentSongs.get(channel));
            tag.putString("triggerID", this.currentTriggers.get(channel));
            ret.setTag(tag);
            return ret;
        }
        if (this.menuSongs.get(channel).isEmpty()) return ItemStack.EMPTY;
        index = ThreadLocalRandom.current().nextInt(this.menuSongs.get(channel).size());
        CompoundTag tag = new CompoundTag();
        tag.putString("channelFrom", channel);
        tag.putString("trackID", this.menuSongs.get(channel).get(index));
        tag.putString("triggerID", "menu");
        ret.setTag(tag);
        return ret;
    }

    private void setPVP(ServerPlayer attacker) {
        this.attacker = attacker;
    }

    private void runChecks() {
        try {
            runCommands();
        } catch (CommandSyntaxException ex) {
            MusicTriggers.logExternally(Level.WARN,"Unable to execute 1 or more queued commands!");
            Constants.MAIN_LOG.warn("Unable to execute 1 or more queued commands!",ex);
        }
        ServerPlayer player = this.server.getPlayerList().getPlayer(this.playerUUID);
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
            else if (trigger.getName().matches("victory"))
                potentiallyUpdate(trigger, calculateVictory(trigger.getValOrDefault("identifier", "not_set")));
            else if (trigger.getName().matches("mob"))
                potentiallyUpdate(trigger, calculateMob(trigger, player, pos));
            else if (trigger.getName().matches("pvp"))
                potentiallyUpdate(trigger, calculatePVP(trigger));
            else if (trigger.getName().matches("raid"))
                potentiallyUpdate(trigger, calculateRaid(player.getLevel(),pos,trigger.getValOrDefault("level",-1)));
            else toRemove.add(trigger);
        }
        this.bossInfo.removeIf(info -> info.getProgress()<=0 || !info.isVisible() || info.getName().getString().matches("Raid"));
        if(!toRemove.isEmpty()) this.allTriggers.removeAll(toRemove);
        if(!this.updatedTriggers.isEmpty() || (Objects.nonNull(this.prevStruct) &&
                (Objects.isNull(this.curStruct) || !this.prevStruct.matches(this.curStruct)))) {
            String structToSend = Objects.nonNull(this.curStruct) && !(this.curStruct.length()==0) ? this.curStruct :
                    "Structure has not been synced";
            NetworkHandler.sendTo(new PacketSyncServerInfo(this.updatedTriggers, structToSend), player);
            this.prevStruct = structToSend;
        }
    }

    private void runCommands() throws CommandSyntaxException {
        for(String command : this.commandQueue)
            this.server.getCommands().getDispatcher().execute(command,this.server.createCommandSourceStack());
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
        String id = SERVER_TRIGGER_HOLDERS.contains(name) ? trigger.getValOrDefault("identifier","not_set") : null;
        if(Objects.nonNull(id) && id.matches("not_set")) return null;
        return Objects.nonNull(id) ? name+"-"+id : name;
    }

    private BlockPos roundedPos(Player p) {
        return new BlockPos((Math.round(p.getX() * 2) / 2.0), (Math.round(p.getY() * 2) / 2.0), (Math.round(p.getZ() * 2) / 2.0));
    }

    private boolean calculateSnow(ServerLevel world, BlockPos pos) {
        return world.getBiome(pos).value().coldEnoughToSnow(pos);
    }

    private boolean calculateHome(ServerPlayer player, BlockPos pos, int range) {
        return player.getLevel().dimension()==player.getRespawnDimension() && Objects.nonNull(player.getRespawnPosition()) &&
                player.getRespawnPosition().closerThan(pos,range);
    }

    private boolean calculateBiome(Table biomeTrigger, ServerLevel world, BlockPos pos) {
        if(Objects.isNull(world) || Objects.isNull(pos)) return false;
        Holder<Biome> curBiomeHolder = world.getBiome(pos);
        if(curBiomeHolder.unwrapKey().isEmpty()) return false;
        Biome biome = curBiomeHolder.value();
        Optional<String> optionalBiomeName = RegUtil.get(server,Registry.BIOME_REGISTRY,biome);
        if(optionalBiomeName.isEmpty()) return false;
        List<String> resources = biomeTrigger.getValOrDefault("resource_name",Collections.singletonList("any"));
        if(resources.isEmpty()) resources.add("any");
        List<String> categories = biomeTrigger.getValOrDefault("biome_category",Collections.singletonList("any"));
        if(categories.isEmpty()) resources.add("any");
        String rainType = biomeTrigger.getValOrDefault("rain_type","any");
        float rainFall = biomeTrigger.getValOrDefault("biome_rainfall",Float.MIN_VALUE);
        float temperature = biomeTrigger.getValOrDefault("biome_temperature",Float.MIN_VALUE);
        boolean higherRain = biomeTrigger.getValOrDefault("check_higher_rainfall",true);
        boolean colderTemp = biomeTrigger.getValOrDefault("check_lower_temp",false);
        if (resources.contains("any") || partiallyMatches(optionalBiomeName.get(),resources)) {
            if (rainType.matches("any") || biome.getPrecipitation().getName().contains(rainType)) {
                boolean pass = false;
                if (rainFall == Float.MIN_VALUE) pass = true;
                else if (biome.getDownfall() > rainFall && higherRain) pass = true;
                else if (biome.getDownfall() < rainFall && !higherRain) pass = true;
                if (pass) {
                    float bt = biome.getBaseTemperature();
                    if (temperature == Float.MIN_VALUE) return true;
                    else if (bt >= temperature && !colderTemp) return true;
                    else return bt <= temperature && colderTemp;
                }
            }
        }
        return false;
    }

    private boolean calculateStruct(ServerLevel world, BlockPos pos, List<String> resourceMatcher) {
        this.curStruct = null;
        Optional<? extends Registry<Structure>> optionalReg = world.registryAccess().registry(Registry.STRUCTURE_REGISTRY);
        if(optionalReg.isPresent()) {
            Registry<Structure> reg = optionalReg.get();
            for (ResourceLocation featureID : reg.keySet()) {
                if (world.structureManager().getStructureAt(pos, reg.get(featureID)).isValid()) {
                    this.curStruct = featureID.toString();
                    break;
                }
            }
        }
        return Objects.nonNull(this.curStruct) && partiallyMatches(this.curStruct,resourceMatcher);
    }

    private boolean calculateMob(Table mobTrigger, ServerPlayer player, BlockPos pos) {
        List<String> resources = mobTrigger.getValOrDefault("resource_name",Collections.singletonList("any"));
        int num = mobTrigger.getValOrDefault("level",1);
        if(resources.isEmpty() || num<=0) return false;
        boolean checkTarget = mobTrigger.getValOrDefault("mob_targeting",true);
        int hordeTarget = mobTrigger.getValOrDefault("horde_targeting_percentage",50);
        int range = mobTrigger.getValOrDefault("detection_range",16);
        int health = mobTrigger.getValOrDefault("health",100);
        int hordeHealth = mobTrigger.getValOrDefault("horde_health_percentage",50);
        String nbt = mobTrigger.getValOrDefault("mob_nbt","any");
        String victoryID = mobTrigger.getValOrDefault("victory_id","not_set");
        if (resources.contains("BOSS")) {
            HashSet<ServerBossEvent> matchedBosses = new HashSet<>(resources.size() == 1 ? this.bossInfo : this.bossInfo.stream().filter(
                            info -> partiallyMatches(info.getName().getString(),resources.stream()
                                    .filter(element -> !element.matches("BOSS")).collect(Collectors.toList())))
                    .collect(Collectors.toList()));
            if(matchedBosses.size()<num) return false;
            boolean pass = checkBossHealth(matchedBosses,num,health,hordeHealth);
            Victory victory = this.victoryTriggers.get(victoryID);
            if(Objects.nonNull(victory)) {
                if(pass)
                    for(ServerBossEvent info : matchedBosses)
                        victory.add(mobTrigger, info);
                victory.setActive(mobTrigger,pass);
            }
            return pass;
        }
        return checkMobs(mobTrigger,player,pos,num,range,resources,checkTarget,hordeTarget,health,
                hordeHealth,nbt,victoryID);
    }

    private boolean checkMobs(Table trigger, ServerPlayer player, BlockPos pos, int num, int range, List<String> resources,
                              boolean target, int hordeTarget, int health, int hordeHealth, String nbt, String victoryID) {
        AABB box = new AABB(pos.getX()-range,pos.getY()-range,pos.getZ()-range,
                pos.getX()+range,pos.getY()+range,pos.getZ()+range);
        HashSet<LivingEntity> matchedEntities = new HashSet<>(player.getLevel().getEntitiesOfClass(
                LivingEntity.class,box,e -> entityWhitelist(e,resources,nbt)));
        if(matchedEntities.size()<num) return false;
        boolean pass = checkSpecifics(matchedEntities,num,player,target,hordeTarget,health,hordeHealth);
        Victory victory = this.victoryTriggers.get(victoryID);
        if(Objects.nonNull(victory)) {
            for(LivingEntity entity : matchedEntities)
                victory.add(trigger,entity);
            victory.setActive(trigger,pass);
        }
        return pass;
    }

    private boolean entityWhitelist(LivingEntity entity, List<String> resources, String nbtParser) {
        return Objects.nonNull(entity) && checkEntityName(entity, resources) && checkNBT(entity, nbtParser);
    }

    private boolean checkEntityName(LivingEntity entity, List<String> resources) {
        if(resources.isEmpty()) return false;
        String displayName = entity.getName().getString();
        Optional<String> potentialID = RegUtil.get(this.server,Registry.ENTITY_TYPE_REGISTRY,entity.getType());
        if(resources.contains("MOB")) {
            if(!(entity instanceof Mob)) return false;
            List<String> blackList = resources.stream().filter(element -> !element.matches("MOB")).collect(Collectors.toList());
            if(blackList.isEmpty()) return true;
            return !blackList.contains(displayName) && potentialID.isPresent() && !partiallyMatches(potentialID.get(),blackList);
        }
        return resources.contains(displayName) || (potentialID.isPresent() && partiallyMatches(potentialID.get(),resources));
    }

    private boolean checkSpecifics(HashSet<LivingEntity> entities, int num, ServerPlayer player, boolean target,
                                   float targetRatio, float health, float healthRatio) {
        return checkTarget(entities,num,target,targetRatio,player) && checkHealth(entities,num,health,healthRatio);
    }

    private boolean checkTarget(HashSet<LivingEntity> entities, int num, boolean target, float ratio, ServerPlayer player) {
        if(!target || ratio<=0) return true;
        float counter = 0f;
        for(LivingEntity entity : entities) {
            if(!(entity instanceof Mob)) continue;
            if(((Mob) entity).getTarget() == player)
                counter++;
        }
        return counter/num>=ratio/100f;
    }

    private boolean checkHealth(HashSet<LivingEntity> entities, int num, float health, float ratio) {
        if(health>=100 || ratio<=0) return true;
        float counter = 0f;
        for(LivingEntity entity : entities)
            if(entity.getHealth()/entity.getMaxHealth()<=health/100f)
                counter++;
        return counter/num>=ratio/100f;
    }

    private boolean checkBossHealth(HashSet<ServerBossEvent> bars, int num, float health, float ratio) {
        if(health>=100 || ratio<=0) return true;
        float counter = 0f;
        for(ServerBossEvent bar : bars)
            if(bar.getProgress()<=health)
                counter++;
        return counter/num>=ratio/100f;
    }

    private boolean checkNBT(LivingEntity entity, String nbt) {
        if(nbt.matches("any")) return true;
        String[] parts = nbt.split(":");
        try {
            if(parts.length==0) return true;
            if(NBT_MODES.contains(parts[0])) {
                if(parts.length==1) return false;
                CompoundTag data = entity.saveWithoutId(new CompoundTag());
                switch (parts[0]) {
                    case "KEY_PRESENT" -> {
                        if (parts[1].matches("INVERT")) {
                            if (parts.length == 2) return false;
                            Tag finalKey = getFinalTag(data, Arrays.copyOfRange(parts, 2, parts.length));
                            return Objects.isNull(finalKey) || !(finalKey instanceof CompoundTag);
                        }
                        Tag finalKey = getFinalTag(data, Arrays.copyOfRange(parts, 1, parts.length));
                        return Objects.nonNull(finalKey) && finalKey instanceof CompoundTag;
                    }
                    case "VAL_PRESENT" -> {
                        if (parts[1].matches("INVERT")) {
                            if (parts.length == 2) return false;
                            Tag finalVal = getFinalTag(data, Arrays.copyOfRange(parts, 2, parts.length));
                            return Objects.isNull(finalVal) || finalVal instanceof CompoundTag;
                        }
                        Tag finalVal = getFinalTag(data, Arrays.copyOfRange(parts, 1, parts.length));
                        return Objects.nonNull(finalVal) && !(finalVal instanceof CompoundTag);
                    }
                    case "INVERT" -> {
                        boolean compare = Boolean.parseBoolean(parts[parts.length - 1]);
                        Tag finalVal = getFinalTag(data, Arrays.copyOfRange(parts, 1, parts.length - 1));
                        if (finalVal instanceof ByteTag)
                            return (compare && ((ByteTag) finalVal).getAsByte() == 1) ||
                                    (!compare && ((ByteTag) finalVal).getAsByte() == 0);
                        return false;
                    }
                    case "GREATER" -> {
                        double compare = Double.parseDouble(parts[parts.length - 1]);
                        int from = 1;
                        if (parts[1].matches("EQUAL")) {
                            if (parts.length == 2) return false;
                            from = 2;
                        }
                        return numNBT(getFinalTag(data, Arrays.copyOfRange(parts, from, parts.length - 1)), compare, true, from == 2);
                    }
                    case "LESSER" -> {
                        double compare = Double.parseDouble(parts[parts.length - 1]);
                        int from = 1;
                        if (parts[1].matches("EQUAL")) {
                            if (parts.length == 2) return false;
                            from = 2;
                        }
                        return numNBT(getFinalTag(data, Arrays.copyOfRange(parts, from, parts.length - 1)), compare, false, from == 2);
                    }
                    default -> {
                        int from = 1;
                        boolean pass = false;
                        if (parts[1].matches("INVERT")) {
                            if (parts.length == 2) return false;
                            from = 2;
                        }
                        Tag finalVal = getFinalTag(data, Arrays.copyOfRange(parts, from, parts.length));
                        if (finalVal instanceof CompoundTag) return false;
                        if (finalVal instanceof ByteTag) {
                            boolean compare = Boolean.parseBoolean(parts[parts.length - 1]);
                            pass = (compare && ((ByteTag) finalVal).getAsByte() == 0) ||
                                    (!compare && ((ByteTag) finalVal).getAsByte() == 1);
                        } else if (finalVal instanceof NumericTag)
                            pass = ((NumericTag) finalVal).getAsDouble() == Double.parseDouble(parts[parts.length - 1]);
                        else if (finalVal instanceof StringTag) {
                            pass = finalVal.getAsString().trim().toLowerCase()
                                    .matches(parts[parts.length - 1].trim().toLowerCase());
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

    private Tag getFinalTag(CompoundTag tag, String[] parts) {
        if(!tag.contains(parts[0])) return null;
        if(parts.length==1) return tag.get(parts[0]);
        try {
            return getFinalTag(tag.getCompound(parts[0]), Arrays.copyOfRange(parts,1,parts.length));
        } catch (ClassCastException ignored) {
            return null;
        }
    }

    private boolean numNBT(Tag tag, double comp, boolean greater, boolean equal) {
        if(!(tag instanceof NumericTag)) return false;
        double val = ((NumericTag)tag).getAsDouble();
        return greater ? equal ? val>=comp : val>comp : equal ? val<=comp : val<comp;
    }

    private boolean calculateRaid(ServerLevel world, BlockPos pos, int wave) {
        if (Objects.isNull(world) || Objects.isNull(pos) || wave < 0) return false;
        Raid raid = world.getRaidAt(pos);
        if(Objects.isNull(raid)) return false;
        return raid.getGroupsSpawned() >= wave;
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
