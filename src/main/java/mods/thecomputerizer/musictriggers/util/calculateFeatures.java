package mods.thecomputerizer.musictriggers.util;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import mods.thecomputerizer.musictriggers.util.packets.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.*;

public class calculateFeatures {

    public static HashMap<Integer, Map<LivingEntity, Integer>> victoryMobs = new HashMap<>();
    private static boolean infernalLoaded = false;
    public static HashMap<Integer, Map<ServerBossInfo, Integer>> victoryBosses = new HashMap<>();
    public static List<ServerBossInfo> bossInfo = new ArrayList<>();

    public static void calculateStructAndSend(String triggerID, String struct, BlockPos pos, UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            ServerWorld world = server.getLevel(Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)).level.dimension());
            if (world != null) {
                boolean good = false;
                String curStruct = null;
                for (Structure<?> structureFeature : net.minecraftforge.registries.ForgeRegistries.STRUCTURE_FEATURES) {
                    if(world.structureFeatureManager().getStructureAt(pos,true,structureFeature.getStructure()).isValid()) {
                        if(structureFeature.getRegistryName()!=null) {
                            curStruct = structureFeature.getRegistryName().toString().replace("minecraft:", "");
                            if(curStruct.matches(struct)) {
                                good = true;
                            }
                        }
                    }
                }
                PacketHandler.sendTo(new InfoFromStructure(good,triggerID,curStruct), Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)));
            }
        }
    }

    public static void calculateBiomeAndSend(String triggerID, String biome, BlockPos pos, UUID uuid, String category, String rainType, float temperature, boolean cold) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            ServerWorld world = server.getLevel(Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)).level.dimension());
            if (world != null) {
                Biome curBiome = world.getBiome(pos);
                boolean pass = checkBiome(curBiome,biome,category,rainType,temperature,cold);
                if (pass) {
                    PacketHandler.sendTo(new InfoFromBiome(true,triggerID, Objects.requireNonNull(curBiome.getRegistryName()).toString()), Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)));
                } else {
                    PacketHandler.sendTo(new InfoFromBiome(false,triggerID, Objects.requireNonNull(curBiome.getRegistryName()).toString()), Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)));
                }
            }
        }
    }

    public static void calculateSnowAndSend(String triggerID, BlockPos pos, UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerPlayerEntity player = server.getPlayerList().getPlayer(uuid);
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            assert player != null;
            ServerWorld world = server.getLevel(player.level.dimension());
            if (world != null) {
                Biome biome = world.getBiome(pos);
                if (biome.getTemperature(pos)<0.15f) {
                    PacketHandler.sendTo(new InfoFromSnow(true, triggerID), player);
                } else {
                    PacketHandler.sendTo(new InfoFromSnow(false, triggerID), player);
                }
            }
        }
    }

    public static void calculateHomeAndSend(String triggerID, BlockPos pos, UUID uuid, int range) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerPlayerEntity player = server.getPlayerList().getPlayer(uuid);
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            assert player != null;
            ServerWorld world = server.getLevel(player.level.dimension());
            if (world != null) {
                if (Objects.requireNonNull(player.getRespawnPosition()).closerThan(pos,range) && player.getRespawnDimension()==world.dimension() && !world.getSharedSpawnPos().closerThan(pos,range)) {
                    PacketHandler.sendTo(new InfoFromHome(true, triggerID), player);
                } else {
                    PacketHandler.sendTo(new InfoFromHome(false, triggerID), player);
                }
            }
        }
    }

    public static void calculateRaidAndSend(String triggerID, int wave, BlockPos pos, UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            ServerPlayerEntity player = server.getPlayerList().getPlayer(uuid);
            assert player != null;
            ServerWorld world = server.getLevel(player.level.dimension());
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


    public static boolean checkBiome(Biome b, String name, String category, String rainType, float temperature, boolean cold) {
        if(Objects.requireNonNull(b.getRegistryName()).toString().contains(name) || name.matches("minecraft")) {
            if(b.getBiomeCategory().getName().contains(category) || category.matches("nope")) {
                if(b.getPrecipitation().getName().contains(rainType) || rainType.matches("nope")) {
                    float bt = b.getBaseTemperature();
                    if(temperature==-111) return true;
                    else if(bt>=temperature && !cold) return true;
                    else return bt <= temperature && cold;
                }
            }
        }
        return false;
    }

    public static void calculateMobAndSend(String triggerID, UUID uuid, String mobname, int detectionrange, boolean targetting, int targettingpercentage, int health, int healthpercentage, boolean victory, int victoryID, String i, int num, int persistence, int timeout, String nbtKey) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerPlayerEntity player = server.getPlayerList().getPlayer(uuid);
        assert player != null;
        ServerWorld world = server.getLevel(player.level.dimension());
        boolean pass = false;
        assert world != null;
        List<LivingEntity> mobTempList = world.getEntitiesOfClass(LivingEntity.class, new AxisAlignedBB(player.getX() - detectionrange, player.getY() - (detectionrange / 2f), player.getZ() - detectionrange, player.getX() + detectionrange, player.getY() + (detectionrange / 2f), player.getZ() + detectionrange));
        List<MobEntity> mobList = new ArrayList<>();
        for (LivingEntity e : mobTempList) {
            if (e instanceof MobEntity && (e.serializeNBT().contains(nbtKey) || nbtKey.matches(""))) {
                mobList.add((MobEntity) e);
            }
        }
        boolean victoryRet = true;
        int trackingCounter = 0;
        int healthCounter = 0;
        boolean infernal = true;
        boolean infernalChecked = false;
        boolean infernalDone = false;
        if (mobname.matches("MOB")) {
            for (Iterator<MobEntity> it = mobList.iterator(); it.hasNext(); ) {
                MobEntity e = it.next();
                boolean isMonster = true;
                if (e instanceof AnimalEntity) {
                    it.remove();
                    isMonster = false;
                }
                if (isMonster) {
                    if (e.getTarget() instanceof PlayerEntity) {
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
                        victoryMobs.computeIfAbsent(victoryID, k -> new HashMap<>());
                        if (!victoryMobs.get(victoryID).containsKey(e) && victoryMobs.get(victoryID).size() < num) {
                            victoryMobs.get(victoryID).put(e, timeout);
                        }
                    }
                }
            }
            if (mobList.size() >= num && ((!targetting || (float) trackingCounter / num >= targettingpercentage / 100F) && infernalDone && (float) healthCounter / num >= healthpercentage / 100F)) {
                pass = true;
            }
            if(victoryMobs.get(victoryID).keySet().size()<num) {
                victoryMobs = new HashMap<>();
                victoryRet = false;
            } else {
                for(LivingEntity el : victoryMobs.get(victoryID).keySet()) {
                    if (!el.isDeadOrDying()) {
                        victoryRet = false;
                        break;
                    }
                }
            }
        } else if (mobname.matches("BOSS")) {
            List<ServerBossInfo> tempBoss = bossInfo;
            for(ServerBossInfo b : tempBoss) {
                if(b.getPercent()<=0f) {
                    bossInfo.remove(b);
                }
            }
            if(!bossInfo.isEmpty()) {
                for (ServerBossInfo e : bossInfo) {
                    if (e.getPlayers().contains(player)) {
                        if (health / 100f >= e.getPercent()) {
                            healthCounter++;
                        }
                        if (victory) {
                            victoryBosses.computeIfAbsent(victoryID, k -> new HashMap<>());
                            if(!victoryBosses.get(victoryID).containsKey(e) && victoryBosses.keySet().size()<num) {
                                victoryBosses.get(victoryID).put(e,timeout);
                            }
                        }
                    }
                }
                if(bossInfo.size()>=num && (float)healthCounter/bossInfo.size()<=1f/healthpercentage) {
                    pass = true;
                }
                if(victoryBosses.get(victoryID).keySet().size()<num) {
                    victoryBosses = new HashMap<>();
                    victoryRet = false;
                } else {
                    for(ServerBossInfo bis : victoryBosses.get(victoryID).keySet()) {
                        if(bis.getPercent()!=0) {
                            victoryRet = false;
                            break;
                        }
                    }
                }
            }
        } else {
            int mobCounter = 0;
            List<MobEntity> mobListSpecific = new ArrayList<>();
            for (LivingEntity e : mobTempList) {
                if ((e.getDisplayName().getString().matches(mobname) || Objects.requireNonNull(e.getType().getRegistryName()).toString().matches(mobname)) && (e.serializeNBT().contains(nbtKey) || nbtKey.matches(""))) {
                    if(e instanceof MobEntity) {
                        mobCounter++;
                        mobListSpecific.add((MobEntity) e);
                    }
                }
            }
            for (MobEntity e : mobListSpecific) {
                if (e.getTarget() instanceof PlayerEntity) {
                    trackingCounter++;
                }
                if (e.getHealth() / e.getMaxHealth() <= health / 100F) {
                    healthCounter++;
                }
                try {
                    infernalChecked = infernalChecker(e, i);
                } catch (NoSuchMethodError ignored) {
                    infernal = false;
                }
                if (!infernal || infernalChecked) {
                    infernalDone = true;
                }
                if (victory) {
                    victoryMobs.computeIfAbsent(victoryID, k -> new HashMap<>());
                    if (!victoryMobs.get(victoryID).containsKey(e) && victoryMobs.get(victoryID).size() < num) {
                        victoryMobs.get(victoryID).put(e, timeout);
                    }
                }
            }
            if (mobCounter >= num && ((!targetting || (float) trackingCounter / num >= targettingpercentage / 100F) && infernalDone && (float) healthCounter / num >= healthpercentage / 100F)) {
                pass = true;
            }
            if(victoryMobs.get(victoryID).keySet().size()<num) {
                victoryMobs = new HashMap<>();
                victoryRet = false;
            } else {
                for(LivingEntity el : victoryMobs.get(victoryID).keySet()) {
                    if (!el.isDeadOrDying()) {
                        victoryRet = false;
                        break;
                    }
                }
            }
        }
        if (persistence > 0) {
            pass = true;
        }
        if(pass) victoryRet = false;
        PacketHandler.sendTo(new InfoFromMob(triggerID,pass,victoryID,victoryRet),player);
    }

    private static boolean infernalChecker(LivingEntity m, String s) {
        if (ModList.get().isLoaded("infernalmobs")) {
            infernalLoaded = true;
            if (s == null) {
                return true;
            }
            return InfernalMobsCore.getMobModifiers(m).getModName().matches(s);
        }
        return false;
    }
}
