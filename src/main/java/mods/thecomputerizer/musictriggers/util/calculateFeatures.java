package mods.thecomputerizer.musictriggers.util;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import mods.thecomputerizer.musictriggers.util.packets.packetGetMobInfo;
import mods.thecomputerizer.musictriggers.util.packets.packetToClient;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;

import java.util.*;

public class calculateFeatures {

    public static HashMap<Integer, Map<EntityLiving, Integer>> victoryMobs = new HashMap<>();
    public static HashMap<Integer, Map<BossInfoServer, Integer>> victoryBosses = new HashMap<>();
    public static List<BossInfoServer> bossInfo = new ArrayList<>();

    public static void calculateStructAndSend(String triggerID, String struct, BlockPos pos, Integer dimID, UUID uuid) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        WorldServer world = server.getWorld(dimID);
        if(world!=null) {
            if (world.getChunkProvider().isInsideStructure(world, struct, pos)) {
                RegistryHandler.network.sendTo(new packetToClient.packetToClientMessage(true +","+triggerID), server.getPlayerList().getPlayerByUUID(uuid));
            } else {
                RegistryHandler.network.sendTo(new packetToClient.packetToClientMessage(false +","+triggerID), server.getPlayerList().getPlayerByUUID(uuid));
            }
        }
    }

    public static void calculateMobAndSend(String triggerID, UUID uuid, String mobname, int detectionrange, boolean targetting, int targettingpercentage, int health, int healthpercentage, boolean victory, int victoryID, String i, int num, int persistence, int timeout, String nbtKey) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        EntityPlayerMP player = (EntityPlayerMP)server.getEntityFromUuid(uuid);
        assert player != null;
        WorldServer world = server.getWorld(player.dimension);
        boolean pass = false;
        List<EntityLiving> mobTempList = world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(player.posX - (double) detectionrange, player.posY - ((double) detectionrange / 2), player.posZ - (double) detectionrange, player.posX + (double) detectionrange, player.posY + ((double) detectionrange / 2), player.posZ + (double) detectionrange));
        List<EntityLiving> mobList = new ArrayList<>();
        for (EntityLiving e : mobTempList) {
            if ((e instanceof EntityMob || e instanceof EntityDragon) && (e.serializeNBT().hasKey(nbtKey) || nbtKey.matches("_"))) {
                mobList.add(e);
            }
        }
        boolean victoryRet = true;
        int trackingCounter = 0;
        int healthCounter = 0;
        boolean infernal = true;
        boolean infernalChecked = false;
        boolean infernalDone = false;
        if (mobname.matches("MOB")) {
            for (EntityLiving e : mobList) {
                if (e.getAttackTarget()==player) {
                    trackingCounter++;
                }
                if (e.getHealth() / e.getMaxHealth() <= (float)health / 100F) {
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
            if (mobList.size() >= num && ((!targetting || (float) trackingCounter / num >= targettingpercentage / 100F) && infernalDone && (float) healthCounter / num >= healthpercentage / 100F)) {
                pass = true;
            }
            if(victoryMobs.get(victoryID).keySet().size()<num) {
                victoryMobs = new HashMap<>();
                victoryRet = false;
            } else {
                for(EntityLiving el : victoryMobs.get(victoryID).keySet()) {
                    if (!el.isDead) {
                        victoryRet = false;
                        break;
                    }
                }
            }
        } else if (mobname.matches("BOSS")) {
            List<BossInfoServer> tempBoss = bossInfo;
            for(BossInfoServer b : tempBoss) {
                if(b.getPercent()<=0f) {
                    bossInfo.remove(b);
                }
            }
            if(!bossInfo.isEmpty()) {
                for (BossInfoServer e : bossInfo) {
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
                    for(BossInfoServer bis : victoryBosses.get(victoryID).keySet()) {
                        if(bis.getPercent()!=0) {
                            victoryRet = false;
                            break;
                        }
                    }
                }
            }
        } else {
            int mobCounter = 0;
            List<EntityLiving> mobListSpecific = new ArrayList<>();
            for (EntityLiving e : mobTempList) {
                if ((e.getName().matches(mobname) || Objects.requireNonNull(EntityList.getKey(e)).toString().matches(mobname)) && (e.serializeNBT().hasKey(nbtKey) || nbtKey.matches(""))) {
                    mobCounter++;
                    mobListSpecific.add(e);
                }
            }
            for (EntityLiving e : mobListSpecific) {
                if (e.getAttackTarget()==player) {
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
                for(EntityLiving el : victoryMobs.get(victoryID).keySet()) {
                    if (!el.isDead) {
                        victoryRet = false;
                        break;
                    }
                }
            }
        }
        if (persistence > 0) {
            pass = true;
        }
        if(pass) {
            victoryRet = false;
        }
        RegistryHandler.network.sendTo(new packetGetMobInfo.packetGetMobInfoMessage(triggerID,pass,victoryID,victoryRet),player);
    }

    @Optional.Method(modid = "infernalmobs")
    private static boolean infernalChecker(EntityLiving m, String s) {
        if (s == null) {
            return true;
        }
        return InfernalMobsCore.getMobModifiers(m).getModName().matches(s);
    }
}
