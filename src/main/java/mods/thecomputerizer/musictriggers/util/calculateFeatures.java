package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.util.packets.*;
import net.fabricmc.fabric.impl.structure.FabricStructureImpl;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.RegistryWorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.*;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class calculateFeatures {

    public static MinecraftServer curServer;

    public static HashMap<Integer, Map<LivingEntity, Integer>> victoryMobs = new HashMap<>();
    public static HashMap<Integer, Map<ServerBossBar, Integer>> victoryBosses = new HashMap<>();
    public static List<ServerBossBar> bossInfo = new ArrayList<>();

    public static void calculateStructAndSend(String triggerID, String struct, BlockPos pos, UUID uuid) {

        ServerPlayerEntity player = curServer.getPlayerManager().getPlayer(uuid);
        if(player!=null) {
            ServerWorld world = curServer.getWorld(player.getWorld().getRegistryKey());
            if (world != null) {
                boolean good = false;
                String curStruct = null;
                for (StructureFeature<?> structureFeature : FabricStructureImpl.STRUCTURE_TO_CONFIG_MAP.keySet()) {
                    if(world.getStructureAccessor().getStructureAt(pos, structureFeature).isInExistingChunk()) {
                        if(structureFeature.getName()!=null) {
                            curStruct = structureFeature.getName().replace("minecraft:", "");
                            if(curStruct.matches(struct)) {
                                good = true;
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
                Biome biome = world.getBiome(pos);
                if (!biome.doesNotSnow(pos)) {
                    PacketHandler.sendTo(InfoFromSnow.id, InfoFromSnow.encode(true, triggerID), player);
                } else {
                    PacketHandler.sendTo(InfoFromSnow.id, InfoFromSnow.encode(false, triggerID), player);
                }
            }
        }
    }

    public static void calculateHomeAndSend(String triggerID, BlockPos pos, UUID uuid, int range) {
        ServerPlayerEntity player = curServer.getPlayerManager().getPlayer(uuid);
        if(player!=null) {
            ServerWorld world = curServer.getWorld(player.getWorld().getRegistryKey());
            if (world != null) {
                if (Objects.requireNonNull(player.getSpawnPointPosition()).isWithinDistance(pos,range) && player.getSpawnPointDimension()==world.getRegistryKey() && !world.getSpawnPos().isWithinDistance(pos,range)) {
                    PacketHandler.sendTo(InfoFromHome.id, InfoFromHome.encode(true, triggerID), player);
                } else {
                    PacketHandler.sendTo(InfoFromHome.id, InfoFromHome.encode(false, triggerID), player);
                }
            }
        }
    }

    public static void calculateBiomeAndSend(String triggerID, String biome, BlockPos pos, UUID uuid, String category, String rainType, float temperature, boolean cold) {
        ServerPlayerEntity player = curServer.getPlayerManager().getPlayer(uuid);
        if(player!=null) {
            RegistryWorldView world = curServer.getWorld(player.getWorld().getRegistryKey());
            if (world != null) {
                String curBiome = world.getBiomeKey(pos).get().getValue().toString();
                boolean pass = checkBiome(world.getBiome(pos), curBiome,biome,category,rainType,temperature,cold);
                if (pass) {
                    PacketHandler.sendTo(InfoFromBiome.id, InfoFromBiome.encode(true,triggerID, curBiome), player);
                } else {
                    PacketHandler.sendTo(InfoFromBiome.id, InfoFromBiome.encode(false,triggerID, curBiome), player);
                }
            }
        }
    }

    public static void calculateRaidAndSend(String triggerID, int wave, BlockPos pos, UUID uuid) {
        ServerPlayerEntity player = curServer.getPlayerManager().getPlayer(uuid);
        if(player!=null) {
            ServerWorld world = curServer.getWorld(player.getWorld().getRegistryKey());
            if (world != null) {
                Raid raid = world.getRaidAt(pos);
                if (raid!=null && raid.getGroupsSpawned()>=wave) {
                    PacketHandler.sendTo(InfoFromRaid.id, InfoFromRaid.encode(triggerID,true),player);
                } else {
                    PacketHandler.sendTo(InfoFromRaid.id, InfoFromRaid.encode(triggerID,false),player);
                }
            }
        }
    }

    public static boolean checkBiome(Biome b, String biome, String name, String category, String rainType, float temperature, boolean cold) {
        if(biome.contains(name) || name.matches("minecraft")) {
            if(b.getCategory().getName().contains(category) || category.matches("nope")) {
                if(b.getPrecipitation().getName().contains(rainType) || rainType.matches("nope")) {
                    float bt = b.getTemperature();
                    if(temperature==-111) return true;
                    else if(bt>=temperature && !cold) return true;
                    else return bt <= temperature && cold;
                }
            }
        }
        return false;
    }
    public static void calculateMobAndSend(String triggerID, UUID uuid, String mobname, int detectionrange, boolean targetting, int targettingpercentage, int health, int healthpercentage, boolean victory, int victoryID, int num, int persistence, int timeout, String nbtKey) {
        ServerPlayerEntity player = curServer.getPlayerManager().getPlayer(uuid);
        assert player != null;
        ServerWorld world = curServer.getWorld(player.getWorld().getRegistryKey());
        boolean pass = false;
        assert world != null;
        List<LivingEntity> mobTempList = world.getEntitiesByClass(LivingEntity.class, new Box(player.getX() - detectionrange, player.getY() - (detectionrange / 2f), player.getZ() - detectionrange, player.getX() + detectionrange, player.getY() + (detectionrange / 2f), player.getZ() + detectionrange), EntityPredicates.VALID_LIVING_ENTITY);
        List<MobEntity> mobList = new ArrayList<>();
        for (LivingEntity e : mobTempList) {
            if (e instanceof MobEntity) {
                mobList.add((MobEntity) e);
            }
        }
        boolean victoryRet = true;
        int trackingCounter = 0;
        int healthCounter = 0;
        if (mobname.matches("MOB")) {
            for (Iterator<MobEntity> it = mobList.iterator(); it.hasNext(); ) {
                MobEntity e = it.next();
                boolean isMonster = true;
                if (e instanceof AnimalEntity || !(e.writeNbt(new NbtCompound()).contains(nbtKey) || nbtKey.matches("_"))) {
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
                    if (victory) {
                        victoryMobs.computeIfAbsent(victoryID, k -> new HashMap<>());
                        if (!victoryMobs.get(victoryID).containsKey(e) && victoryMobs.get(victoryID).size() < num) {
                            victoryMobs.get(victoryID).put(e, timeout);
                        }
                    }
                }
            }
            if (mobList.size() >= num && ((!targetting || (float) trackingCounter / num >= targettingpercentage / 100F) && (float) healthCounter / num >= healthpercentage / 100F)) {
                pass = true;
            }
            if(victoryMobs.get(victoryID).keySet().size()<num) {
                victoryMobs = new HashMap<>();
                victoryRet = false;
            } else {
                for(LivingEntity el : victoryMobs.get(victoryID).keySet()) {
                    if (!el.isDead()) {
                        victoryRet = false;
                        break;
                    }
                }
            }
        } else if (mobname.matches("BOSS")) {
            List<ServerBossBar> tempBoss = bossInfo;
            for(ServerBossBar b : tempBoss) {
                if(b.getPercent()<=0f) {
                    bossInfo.remove(b);
                }
            }
            if(!bossInfo.isEmpty()) {
                for (ServerBossBar e : bossInfo) {
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
                    for(ServerBossBar bis : victoryBosses.get(victoryID).keySet()) {
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
                if (e.getDisplayName().getString().matches(mobname) || Objects.requireNonNull(e.getType().getName()).asString().matches(mobname)) {
                    if(e instanceof  MobEntity && (e.writeNbt(new NbtCompound()).contains(nbtKey) || nbtKey.matches("_"))) {
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
                if (victory) {
                    victoryMobs.computeIfAbsent(victoryID, k -> new HashMap<>());
                    if (!victoryMobs.get(victoryID).containsKey(e) && victoryMobs.get(victoryID).size() < num) {
                        victoryMobs.get(victoryID).put(e, timeout);
                    }
                }
            }
            if (mobCounter >= num && ((!targetting || (float) trackingCounter / num >= targettingpercentage / 100F) && (float) healthCounter / num >= healthpercentage / 100F)) {
                pass = true;
            }
            if(victoryMobs.get(victoryID).keySet().size()<num) {
                victoryMobs = new HashMap<>();
                victoryRet = false;
            } else {
                for(LivingEntity el : victoryMobs.get(victoryID).keySet()) {
                    if (!el.isDead()) {
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
        PacketHandler.sendTo(InfoFromMob.id, InfoFromMob.encode(triggerID,pass,victoryID,victoryRet),player);
    }
}
