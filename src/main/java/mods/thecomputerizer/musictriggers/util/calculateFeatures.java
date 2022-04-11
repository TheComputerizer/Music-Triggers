package mods.thecomputerizer.musictriggers.util;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import mods.thecomputerizer.musictriggers.util.packets.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;

import static mods.thecomputerizer.musictriggers.client.MusicPicker.stringBreaker;

public class calculateFeatures {

    public static List<String> allTriggers = new ArrayList<>();

    private static boolean infernalLoaded = false;
    public static HashMap<String, Map<UUID, Integer>> victoryMobs = new HashMap<>();
    public static HashMap<String, Map<String, Integer>> victoryBosses = new HashMap<>();
    public static HashMap<String, Float> bossInfo = new HashMap<>();

    public static void calculateStructAndSend(String triggerID, String struct, BlockPos pos, UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            ServerLevel world = server.getLevel(Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)).level.dimension());
            if (world != null) {
                boolean good = false;
                String curStruct = null;
                for (StructureFeature<?> structureFeature : net.minecraftforge.registries.ForgeRegistries.STRUCTURE_FEATURES) {
                    if(world.structureFeatureManager().getStructureAt(pos, structureFeature).isValid()) {
                        if(structureFeature.getRegistryName()!=null) {
                            curStruct = structureFeature.getRegistryName().toString();
                            if(checkResourceList(curStruct,struct,false)) {
                                good = true;
                                break;
                            }
                        }
                    }
                }
                PacketHandler.sendTo(new InfoFromStructure(good,triggerID,curStruct), Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)));
            }
        }
    }

    public static void calculateSnowAndSend(String triggerID, BlockPos pos, UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            assert player != null;
            ServerLevel world = server.getLevel(player.level.dimension());
            if (world != null) {
                Biome biome = world.getBiome(pos);
                PacketHandler.sendTo(new InfoFromSnow(biome.coldEnoughToSnow(pos), triggerID), player);
            }
        }
    }

    public static void calculateHomeAndSend(String triggerID, BlockPos pos, UUID uuid, int range) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            assert player != null;
            ServerLevel world = server.getLevel(player.level.dimension());
            if (world != null) {
                if (Objects.requireNonNull(player.getRespawnPosition()).closerThan(pos,range) && player.getRespawnDimension()==world.dimension() && !world.getSharedSpawnPos().closerThan(pos,range)) {
                    PacketHandler.sendTo(new InfoFromHome(true, triggerID), player);
                } else {
                    PacketHandler.sendTo(new InfoFromHome(false, triggerID), player);
                }
            }
        }
    }

    public static void calculateBiomeAndSend(String triggerID, String biome, BlockPos pos, UUID uuid, String category, String rainType, float temperature, boolean cold, float rainfall, boolean togglerainfall) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            ServerLevel world = server.getLevel(Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)).level.dimension());
            if (world != null) {
                Biome curBiome = world.getBiome(pos);
                boolean pass = checkBiome(curBiome,biome,category,rainType,temperature,cold,rainfall,togglerainfall);
                if (pass) {
                    PacketHandler.sendTo(new InfoFromBiome(true,triggerID, Objects.requireNonNull(curBiome.getRegistryName()).toString()), Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)));
                } else {
                    PacketHandler.sendTo(new InfoFromBiome(false,triggerID, Objects.requireNonNull(curBiome.getRegistryName()).toString()), Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)));
                }
            }
        }
    }

    public static void calculateRaidAndSend(String triggerID, int wave, BlockPos pos, UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            assert player != null;
            ServerLevel world = server.getLevel(player.level.dimension());
            if (world != null) {
                Raid raid = world.getRaidAt(pos);
                if (raid!=null && raid.getGroupsSpawned()>=wave) {
                    PacketHandler.sendTo(new InfoFromRaid(triggerID,true),player);
                } else {
                    PacketHandler.sendTo(new InfoFromRaid(triggerID,false),player);
                }
            }
        }
    }

    public static boolean checkBiome(Biome b, String name, String category, String rainType, float temperature, boolean cold, float rainfall, boolean togglerainfall) {
        if(checkResourceList(Objects.requireNonNull(b.getRegistryName()).toString(), name, false) || name.matches("minecraft")) {
            if(checkResourceList(b.getBiomeCategory().getName(), category, false) || category.matches("nope")) {
                if(b.getPrecipitation().getName().contains(rainType) || rainType.matches("nope")) {
                    boolean pass = false;
                    if(rainfall==-111f) pass = true;
                    else if(b.getDownfall()>rainfall && togglerainfall) pass = true;
                    else if(b.getDownfall()<rainfall && !togglerainfall) pass = true;
                    if(pass) {
                        float bt = b.getBaseTemperature();
                        if (temperature == -111) return true;
                        else if (bt >= temperature && !cold) return true;
                        else return bt <= temperature && cold;
                    }
                }
            }
        }
        return false;
    }
    public static void calculateMobAndSend(String triggerID, UUID uuid, String mobname, int detectionrange, boolean targetting, int targettingpercentage, int health, int healthpercentage, boolean victory, int victoryID, String i, int num, int timeout, String nbtKey) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        assert player != null;
        ServerLevel world = server.getLevel(player.level.dimension());
        boolean pass = false;
        assert world != null;
        List<LivingEntity> mobTempList = world.getEntitiesOfClass(LivingEntity.class, new AABB(player.getX() - detectionrange, player.getY() - (detectionrange / 2f), player.getZ() - detectionrange, player.getX() + detectionrange, player.getY() + (detectionrange / 2f), player.getZ() + detectionrange));
        List<Mob> mobList = new ArrayList<>();
        for (LivingEntity e : mobTempList) {
            if (e instanceof Mob && nbtChecker(e, nbtKey)) {
                mobList.add((Mob) e);
            }
        }
        boolean victoryRet = true;
        int trackingCounter = 0;
        int healthCounter = 0;
        boolean infernalChecked;
        boolean infernalDone = false;
        if (mobname.matches("MOB") || stringBreaker(mobname, ";")[0].matches("MOB")) {
            List<Mob> mobsWithBlacklist = new ArrayList<>();
            for (Iterator<Mob> it = mobList.iterator(); it.hasNext(); ) {
                Mob e = it.next();
                boolean isMonster = true;
                if (e instanceof Animal) {
                    it.remove();
                    isMonster = false;
                }
                if (isMonster && checkMobBlacklist(e,mobname)) {
                    mobsWithBlacklist.add(e);
                    if (e.getTarget() instanceof Player) {
                        trackingCounter++;
                    }
                    if (e.getHealth() / e.getMaxHealth() <= health / 100F) {
                        healthCounter++;
                    }
                    infernalChecked = infernalChecker(e, i);
                    if (!infernalLoaded || (infernalLoaded && infernalChecked)) {
                        infernalDone = true;
                    }
                    if (victory) {
                        victoryMobs.computeIfAbsent(triggerID, k -> new HashMap<>());
                        if (victoryMobs.get(triggerID).size() < num) victoryMobs.get(triggerID).put(e.getUUID(), timeout);
                    }
                }
            }
            if (mobsWithBlacklist.size() >= num && ((!targetting || (float) trackingCounter / num >= targettingpercentage / 100F) && infernalDone && (float) healthCounter / num >= healthpercentage / 100F)) {
                pass = true;
            }
            if(victoryMobs.get(triggerID)!=null) {
                if (victoryMobs.get(triggerID).keySet().size() < num) {
                    victoryMobs = new HashMap<>();
                    victoryRet = false;
                } else {
                    for (UUID u : victoryMobs.get(triggerID).keySet()) {
                        if (player.getLevel().getEntity(u) != null && !Objects.requireNonNull((LivingEntity) player.getLevel().getEntity(u)).isDeadOrDying()) {
                            victoryRet = false;
                            break;
                        }
                    }
                }
            } else victoryRet = false;
        } else if (mobname.matches("BOSS") || stringBreaker(mobname, ";")[0].matches("BOSS")) {
            HashMap<String, Float> tempBoss = bossInfo;
            if(!bossInfo.isEmpty()) {
                List<String> correctBosses = new ArrayList<>();
                for(String name : tempBoss.keySet()) {
                    if(checkResourceList(name, mobname, true)) {
                        correctBosses.add(name);
                        if (health / 100f >= bossInfo.get(name)) {
                            healthCounter++;
                        }
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
            List<Mob> mobListSpecific = new ArrayList<>();
            for (LivingEntity e : mobTempList) {
                if ((checkResourceList(e.getDisplayName().getString(),mobname,true) || checkResourceList(Objects.requireNonNull(e.getType().getRegistryName()).toString(),mobname,true)) && nbtChecker(e, nbtKey)) {
                    if(e instanceof  Mob) {
                        mobCounter++;
                        mobListSpecific.add((Mob) e);
                    }
                }
            }
            for (Mob e : mobListSpecific) {
                if (e.getTarget() instanceof Player) {
                    trackingCounter++;
                }
                if (e.getHealth() / e.getMaxHealth() <= health / 100F) {
                    healthCounter++;
                }
                infernalChecked = infernalChecker(e, i);
                if (!infernalLoaded || (infernalLoaded && infernalChecked)) {
                    infernalDone = true;
                }
                if (victory) {
                    victoryMobs.computeIfAbsent(triggerID, k -> new HashMap<>());
                    if (victoryMobs.get(triggerID).size() < num) victoryMobs.get(triggerID).put(e.getUUID(), timeout);
                }
            }
            if (mobCounter >= num && ((!targetting || (float) trackingCounter / num >= targettingpercentage / 100F) && infernalDone && (float) healthCounter / num >= healthpercentage / 100F)) {
                pass = true;
            }
            if(victoryMobs.get(triggerID)!=null) {
                if (victoryMobs.get(triggerID).keySet().size() < num) {
                    victoryMobs = new HashMap<>();
                    victoryRet = false;
                } else {
                    for (UUID u : victoryMobs.get(triggerID).keySet()) {
                        if (player.getLevel().getEntity(u) != null && !Objects.requireNonNull((LivingEntity) player.getLevel().getEntity(u)).isDeadOrDying()) {
                            victoryRet = false;
                            break;
                        }
                    }
                }
            } else victoryRet = false;
        }
        if(pass) victoryRet = false;
        PacketHandler.sendTo(new InfoFromMob(triggerID,pass,victoryID,victoryRet),player);
    }

    private static boolean infernalChecker(LivingEntity m, String s) {
        if (ModList.get().isLoaded("infernalmobs")) {
            infernalLoaded = true;
            if (s == null || s.matches("minecraft")) {
                return true;
            }
            if(InfernalMobsCore.getMobModifiers(m)!=null) return InfernalMobsCore.getMobModifiers(m).getModName().matches(s);
        }
        return false;
    }

    private static boolean nbtChecker(LivingEntity e, String nbt) {
        String[] splitNBT = nbt.split(":");
        if(splitNBT.length==1) return e.serializeNBT().contains(nbt) || nbt.matches("_");
        else {
            if(e.serializeNBT().contains(splitNBT[0])) {
                CompoundTag compound = e.serializeNBT().getCompound(splitNBT[0]);
                if(splitNBT.length==2) return e.serializeNBT().getString(splitNBT[0]).matches(splitNBT[1]);
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

    public static boolean checkMobBlacklist(Mob e, String resourceList) {
        for(String resource : stringBreaker(resourceList,";")) {
            if(!resource.matches("MOB")) {
                if (e.getName().getString().matches(resource)) return false;
                else if (Objects.requireNonNull(e.getType().getRegistryName()).toString().matches(resource)) return false;
            }
        }
        return true;
    }
}
