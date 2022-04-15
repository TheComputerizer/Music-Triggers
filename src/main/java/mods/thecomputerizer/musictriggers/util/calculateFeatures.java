package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.util.packets.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.RegistryWorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;

import java.util.*;

import static mods.thecomputerizer.musictriggers.client.MusicPicker.stringBreaker;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class calculateFeatures {

    public static MinecraftServer curServer;

    public static List<String> allTriggers = new ArrayList<>();

    public static HashMap<String, Map<UUID, Integer>> victoryMobs = new HashMap<>();
    public static HashMap<String, Map<String, Integer>> victoryBosses = new HashMap<>();
    public static HashMap<String, Float> bossInfo = new HashMap<>();

    public static void calculateStructAndSend(String triggerID, String struct, BlockPos pos, UUID uuid) {

        ServerPlayerEntity player = curServer.getPlayerManager().getPlayer(uuid);
        if(player!=null) {
            ServerWorld world = curServer.getWorld(player.getWorld().getRegistryKey());
            if (world != null) {
                boolean good = false;
                String curStruct = null;
                for (ConfiguredStructureFeature<?,?> feature : world.getChunk(pos).getStructureReferences().keySet()) {
                    if (world.getStructureAccessor().getStructureAt(pos, feature).isInExistingChunk()) {
                        if(Registry.STRUCTURE_FEATURE.getId(feature.feature)!=null) {
                            curStruct = Objects.requireNonNull(Registry.STRUCTURE_FEATURE.getId(feature.feature)).toString();
                            if (checkResourceList(curStruct, struct, false)) {
                                good = true;
                                break;
                            }
                        }
                    }
                }
                PacketHandler.sendTo(InfoFromStructure.id, InfoFromStructure.encode(good,triggerID,curStruct), player);
            }
        }
    }

    public static void calculateSnowAndSend(String triggerID, BlockPos pos, UUID uuid) {
        ServerPlayerEntity player = curServer.getPlayerManager().getPlayer(uuid);
        if(player!=null) {
            RegistryWorldView world = curServer.getWorld(player.getWorld().getRegistryKey());
            if (world != null) {
                Biome biome = world.getBiome(pos).value();
                PacketHandler.sendTo(InfoFromSnow.id, InfoFromSnow.encode(!biome.doesNotSnow(pos), triggerID), player);
            }
        }
    }

    public static void calculateHomeAndSend(String triggerID, BlockPos pos, UUID uuid, int range) {
        ServerPlayerEntity player = curServer.getPlayerManager().getPlayer(uuid);
        if(player!=null) {
            ServerWorld world = curServer.getWorld(player.getWorld().getRegistryKey());
            if (world != null && player.getSpawnPointPosition()!=null) {
                if (Objects.requireNonNull(player.getSpawnPointPosition()).isWithinDistance(pos,range) && player.getSpawnPointDimension()==world.getRegistryKey() && !world.getSpawnPos().isWithinDistance(pos,range))
                    PacketHandler.sendTo(InfoFromHome.id, InfoFromHome.encode(true, triggerID), player);
                else PacketHandler.sendTo(InfoFromHome.id, InfoFromHome.encode(false, triggerID), player);
            }
        }
    }

    public static void calculateBiomeAndSend(String triggerID, String biome, BlockPos pos, UUID uuid, String category, String rainType, float temperature, boolean cold, float rainfall, boolean togglerainfall) {
        ServerPlayerEntity player = curServer.getPlayerManager().getPlayer(uuid);
        if(player!=null) {
            RegistryWorldView world = curServer.getWorld(player.getWorld().getRegistryKey());
            if (world != null) {
                String curBiome = world.getBiome(pos).getKey().get().getValue().toString();
                PacketHandler.sendTo(InfoFromBiome.id, InfoFromBiome.encode(checkBiome(world.getBiome(pos), curBiome,biome,category,rainType,temperature,cold,rainfall,togglerainfall),triggerID, curBiome), player);
            }
        }
    }

    public static void calculateRaidAndSend(String triggerID, int wave, BlockPos pos, UUID uuid) {
        ServerPlayerEntity player = curServer.getPlayerManager().getPlayer(uuid);
        if(player!=null) {
            ServerWorld world = curServer.getWorld(player.getWorld().getRegistryKey());
            if (world != null) {
                Raid raid = world.getRaidAt(pos);
                PacketHandler.sendTo(InfoFromRaid.id, InfoFromRaid.encode(triggerID,raid!=null && raid.getGroupsSpawned()>=wave),player);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static boolean checkBiome(RegistryEntry<Biome> reg, String biome, String name, String category, String rainType, float temperature, boolean cold, float rainfall, boolean togglerainfall) {
        Biome b = reg.value();
        if(checkResourceList(biome, name, false) || name.matches("minecraft")) {
            if(checkResourceList(Biome.getCategory(reg).getName(), category, false) || category.matches("nope")) {
                if(b.getPrecipitation().getName().contains(rainType) || rainType.matches("nope")) {
                    boolean pass = false;
                    if(rainfall==-111f) pass = true;
                    else if(b.getDownfall()>rainfall && togglerainfall) pass = true;
                    else if(b.getDownfall()<rainfall && !togglerainfall) pass = true;
                    if(pass) {
                        float bt = b.getTemperature();
                        if (temperature == -111) return true;
                        else if (bt >= temperature && !cold) return true;
                        else return bt <= temperature && cold;
                    }
                }
            }
        }
        return false;
    }
    public static void calculateMobAndSend(String triggerID, UUID uuid, String mobname, int detectionrange, boolean targetting, int targettingpercentage, int health, int healthpercentage, boolean victory, int victoryID, int num, int timeout, String nbtKey) {
        ServerPlayerEntity player = curServer.getPlayerManager().getPlayer(uuid);
        assert player != null;
        ServerWorld world = curServer.getWorld(player.getWorld().getRegistryKey());
        boolean pass = false;
        assert world != null;
        List<LivingEntity> mobTempList = world.getEntitiesByClass(LivingEntity.class, new Box(player.getX() - detectionrange, player.getY() - (detectionrange / 2f), player.getZ() - detectionrange, player.getX() + detectionrange, player.getY() + (detectionrange / 2f), player.getZ() + detectionrange), EntityPredicates.VALID_LIVING_ENTITY);
        List<MobEntity> mobList = new ArrayList<>();
        for (LivingEntity e : mobTempList) {
            if (e instanceof MobEntity && nbtChecker(e, nbtKey)) {
                mobList.add((MobEntity) e);
            }
        }
        boolean victoryRet = true;
        int trackingCounter = 0;
        int healthCounter = 0;
        if (mobname.matches("MOB") || stringBreaker(mobname, ";")[0].matches("MOB")) {
            List<MobEntity> mobsWithBlacklist = new ArrayList<>();
            for (Iterator<MobEntity> it = mobList.iterator(); it.hasNext(); ) {
                MobEntity e = it.next();
                boolean isMonster = true;
                if (e instanceof AnimalEntity) {
                    it.remove();
                    isMonster = false;
                }
                if (isMonster && checkMobBlacklist(e,mobname)) {
                    mobsWithBlacklist.add(e);
                    if (e.getTarget() instanceof PlayerEntity) trackingCounter++;
                    if (e.getHealth() / e.getMaxHealth() <= health / 100F) healthCounter++;
                    if (victory) {
                        victoryMobs.computeIfAbsent(triggerID, k -> new HashMap<>());
                        if (victoryMobs.get(triggerID).size() < num) victoryMobs.get(triggerID).put(e.getUuid(), timeout);
                    }
                }
            }
            if (mobsWithBlacklist.size() >= num && ((!targetting || (float) trackingCounter / num >= targettingpercentage / 100F) && (float) healthCounter / num >= healthpercentage / 100F)) {
                pass = true;
            }
            if(victoryMobs.get(triggerID)!=null) {
                if (victoryMobs.get(triggerID).keySet().size() < num) {
                    victoryMobs = new HashMap<>();
                    victoryRet = false;
                } else {
                    for (UUID u : victoryMobs.get(triggerID).keySet()) {
                        if (player.getWorld().getEntity(u) != null && !Objects.requireNonNull((LivingEntity) player.getWorld().getEntity(u)).isDead()) {
                            victoryRet = false;
                            break;
                        }
                    }
                }
            }
        } else if (mobname.matches("BOSS") || stringBreaker(mobname, ";")[0].matches("BOSS")) {
            HashMap<String, Float> tempBoss = bossInfo;
            if(!bossInfo.isEmpty()) {
                List<String> correctBosses = new ArrayList<>();
                for(String name : tempBoss.keySet()) {
                    if(checkResourceList(name, mobname, true)) {
                        correctBosses.add(name);
                        if (health / 100f >= bossInfo.get(name)) healthCounter++;
                        if (victory) {
                            victoryBosses.computeIfAbsent(triggerID, k -> new HashMap<>());
                            if (victoryBosses.get(triggerID).keySet().size() < num)
                                victoryBosses.get(triggerID).put(name, timeout);
                        }
                    }
                }
                if(correctBosses.size()>=num && (float)healthCounter/bossInfo.size()<=100f/healthpercentage) {
                    pass = true;
                }
                if(victoryBosses.get(triggerID)!=null) {
                    if (victoryBosses.get(triggerID).keySet().size() < num) {
                        victoryBosses = new HashMap<>();
                        victoryRet = false;
                    } else {
                        for (String bis : victoryBosses.get(triggerID).keySet()) {
                            if (bossInfo.get(bis) != 0) {
                                victoryRet = false;
                                break;
                            }
                        }
                    }
                }
                else victoryRet = false;
                for(String name : tempBoss.keySet()) {
                    if(tempBoss.get(name)<=0f) bossInfo.remove(name);
                }
            }
        } else {
            int mobCounter = 0;
            List<MobEntity> mobListSpecific = new ArrayList<>();
            for (LivingEntity e : mobTempList) {
                if ((checkResourceList(e.getDisplayName().getString(),mobname,true) || checkResourceList(Objects.requireNonNull(e.getType().getName()).toString(),mobname,true)) && nbtChecker(e, nbtKey)) {
                    if(e instanceof  MobEntity) {
                        mobCounter++;
                        mobListSpecific.add((MobEntity) e);
                    }
                }
            }
            for (MobEntity e : mobListSpecific) {
                if (e.getTarget() instanceof PlayerEntity) trackingCounter++;
                if (e.getHealth() / e.getMaxHealth() <= health / 100F) healthCounter++;
                if (victory) {
                    victoryMobs.computeIfAbsent(triggerID, k -> new HashMap<>());
                    if (victoryMobs.get(triggerID).size() < num) victoryMobs.get(triggerID).put(e.getUuid(), timeout);
                }
            }
            if (mobCounter >= num && ((!targetting || (float) trackingCounter / num >= targettingpercentage / 100F) && (float) healthCounter / num >= healthpercentage / 100F))
                pass = true;
            if(victoryMobs.get(triggerID)!=null) {
                if (victoryMobs.get(triggerID).keySet().size() < num) {
                    victoryMobs = new HashMap<>();
                    victoryRet = false;
                } else {
                    for (UUID u : victoryMobs.get(triggerID).keySet()) {
                        if (player.getWorld().getEntity(u) != null && !Objects.requireNonNull((LivingEntity) player.getWorld().getEntity(u)).isDead()) {
                            victoryRet = false;
                            break;
                        }
                    }
                }
            }
        }
        if(pass) victoryRet = false;
        PacketHandler.sendTo(InfoFromMob.id, InfoFromMob.encode(triggerID,pass,victoryID,victoryRet),player);
    }

    private static boolean nbtChecker(LivingEntity e, String nbt) {
        String[] splitNBT = nbt.split(":");
        if(splitNBT.length==1) return e.writeNbt(new NbtCompound()).contains(nbt) || nbt.matches("_");
        else {
            if(e.writeNbt(new NbtCompound()).contains(splitNBT[0])) {
                NbtCompound compound = e.writeNbt(new NbtCompound()).getCompound(splitNBT[0]);
                if(splitNBT.length==2) return e.writeNbt(new NbtCompound()).getString(splitNBT[0]).matches(splitNBT[1]);
                else {
                    for (int i = 1; i < splitNBT.length - 2; i++) {
                        if (compound.contains(splitNBT[i])) compound = compound.getCompound(splitNBT[i]);
                    }
                    return compound.getString(splitNBT[splitNBT.length-2]).matches(splitNBT[splitNBT.length-1]);
                }
            }
        }
        return false;
    }

    public static boolean checkResourceList(String type, String resourceList, boolean match) {
        for(String resource : stringBreaker(resourceList,";")) {
            if(!resource.matches("BOSS")) {
                if (match && type.matches(resource)) return true;
                else if (!match && type.contains(resource)) return true;
            }
        }
        return false;
    }

    public static boolean checkMobBlacklist(MobEntity e, String resourceList) {
        for(String resource : stringBreaker(resourceList,";")) {
            if(!resource.matches("MOB")) {
                if (e.getName().getString().matches(resource)) return false;
                else if (Objects.requireNonNull(e.getType().getName()).toString().matches(resource)) return false;
            }
        }
        return true;
    }
}
