package mods.thecomputerizer.musictriggers.util;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import mods.thecomputerizer.musictriggers.MusicTriggers;
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
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;

import java.util.*;

public class calculateFeatures {

    public static List<String> allTriggers = new ArrayList<>();

    public static HashMap<String, Map<UUID, Integer>> victoryMobs = new HashMap<>();
    public static HashMap<String, Map<String, Integer>> victoryBosses = new HashMap<>();
    public static HashMap<String, Float> bossInfo = new HashMap<>();

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
                MusicTriggers.logger.info("Bounding Box: "+e.getName());
            }
        }
        boolean victoryRet = true;
        int trackingCounter = 0;
        int healthCounter = 0;
        boolean infernal = true;
        boolean infernalChecked = false;
        boolean infernalDone = false;
        if (mobname.matches("MOB")) {
            MusicTriggers.logger.info("MOB match");
            for (EntityLiving e : mobList) {
                MusicTriggers.logger.info("Now checking "+e.getName());
                if (e.getAttackTarget()==player) {
                    MusicTriggers.logger.info("Correct attack target");
                    trackingCounter++;
                }
                if (e.getHealth() / e.getMaxHealth() <= (float)health / 100F) {
                    MusicTriggers.logger.info("Correct health");
                    healthCounter++;
                }
                try {
                    infernalChecked = infernalChecker(e, i);
                } catch (NoSuchMethodError ignored) {
                    infernal = false;
                }
                if (!infernal || infernalChecked) {
                    MusicTriggers.logger.info("Correct infernal");
                    infernalDone = true;
                }
                if (victory) {
                    MusicTriggers.logger.info("Checking victory");
                    victoryMobs.computeIfAbsent(triggerID, k -> new HashMap<>());
                    if (victoryMobs.get(triggerID).size() < num) victoryMobs.get(triggerID).put(e.getUniqueID(), timeout);
                }
            }
            if (mobList.size() >= num) MusicTriggers.logger.info("NUM PASS");
            if (!targetting || (float) trackingCounter / num >= targettingpercentage / 100F) MusicTriggers.logger.info("TARGETTING PASS");
            if (infernalDone) MusicTriggers.logger.info("INFERNAL PASS");
            if ((float) healthCounter / num >= healthpercentage / 100F) MusicTriggers.logger.info("HEALTH PASS");
            if (mobList.size() >= num &&
                    ((!targetting || (float) trackingCounter / num >= targettingpercentage / 100F) &&
                            infernalDone &&
                            (float) healthCounter / num >= healthpercentage / 100F)) {
                pass = true;
                MusicTriggers.logger.info("ALL PASS");
            }
            if(victoryMobs.get(triggerID)!=null) {
                MusicTriggers.logger.info("Victory mobs not null");
                if (victoryMobs.get(triggerID).keySet().size() < num) {
                    victoryMobs = new HashMap<>();
                    victoryRet = false;
                } else {
                    for (UUID u : victoryMobs.get(triggerID).keySet()) {
                        if (!Objects.requireNonNull(server.getEntityFromUuid(u)).isDead) {
                            victoryRet = false;
                            break;
                        }
                    }
                }
            }
            else victoryRet = false;
        } else if (mobname.matches("BOSS")) {
            MusicTriggers.logger.info("Checking BOSS");
            HashMap<String, Float> tempBoss = bossInfo;
            if(!bossInfo.isEmpty()) {
                MusicTriggers.logger.info("NOT EMPTY");
                for(String name : tempBoss.keySet()) {
                    MusicTriggers.logger.info("loop: " + name);
                    if (health / 100f >= bossInfo.get(name)) {
                        healthCounter++;
                        MusicTriggers.logger.info("Correct health: "+bossInfo.get(name));
                    }
                    if (victory) {
                        victoryBosses.computeIfAbsent(triggerID, k -> new HashMap<>());
                        if (victoryBosses.get(triggerID).keySet().size() < num) victoryBosses.get(triggerID).put(name, timeout);
                    }
                }
                if (bossInfo.size()>=num) MusicTriggers.logger.info("NUM PASS");
                if ((float)healthCounter/bossInfo.size()<=100f/healthpercentage) MusicTriggers.logger.info("HEALTH PASS");
                if(bossInfo.size()>=num && (float)healthCounter/bossInfo.size()<=100f/healthpercentage) {
                    pass = true;
                    MusicTriggers.logger.info("ALL PASS");
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
            List<EntityLiving> mobListSpecific = new ArrayList<>();
            for (EntityLiving e : mobTempList) {
                if ((e.getName().matches(mobname) || Objects.requireNonNull(EntityList.getKey(e)).toString().matches(mobname)) && (e.serializeNBT().hasKey(nbtKey) || nbtKey.matches("_"))) {
                    mobCounter++;
                    mobListSpecific.add(e);
                    MusicTriggers.logger.info("Found name match "+e.getName());
                }
            }
            for (EntityLiving e : mobListSpecific) {
                MusicTriggers.logger.info("Now checking "+e.getName());
                if (e.getAttackTarget()==player) {
                    MusicTriggers.logger.info("Correct attack target");
                    trackingCounter++;
                }
                if (e.getHealth() / e.getMaxHealth() <= health / 100F) {
                    MusicTriggers.logger.info("Correct health");
                    healthCounter++;
                }
                try {
                    infernalChecked = infernalChecker(e, i);
                } catch (NoSuchMethodError ignored) {
                    infernal = false;
                }
                if (!infernal || infernalChecked) {
                    MusicTriggers.logger.info("Correct infernal");
                    infernalDone = true;
                }
                if (victory) {
                    MusicTriggers.logger.info("Checking victory");
                    victoryMobs.computeIfAbsent(triggerID, k -> new HashMap<>());
                    if (victoryMobs.get(triggerID).size() < num) {
                        MusicTriggers.logger.info("Victory Size");
                        victoryMobs.get(triggerID).put(e.getUniqueID(), timeout);
                    }
                }
            }
            if (mobList.size() >= num) MusicTriggers.logger.info("NUM PASS");
            if (!targetting || (float) trackingCounter / num >= targettingpercentage / 100F) MusicTriggers.logger.info("TARGETTING PASS");
            if (infernalDone) MusicTriggers.logger.info("INFERNAL PASS");
            if ((float) healthCounter / num >= healthpercentage / 100F) MusicTriggers.logger.info("HEALTH PASS");
            if (mobCounter >= num && ((!targetting || (float) trackingCounter / num >= targettingpercentage / 100F) && infernalDone && (float) healthCounter / num >= healthpercentage / 100F)) {
                pass = true;
            }
            if(victoryMobs.get(triggerID)!=null) {
                if (victoryMobs.get(triggerID).keySet().size() < num) {
                    MusicTriggers.logger.info("Remove bad maps");
                    victoryMobs = new HashMap<>();
                    victoryRet = false;
                } else {
                    MusicTriggers.logger.info("Checking good maps");
                    for (UUID u : victoryMobs.get(triggerID).keySet()) {
                        if ((server.getEntityFromUuid(u)!=null && !Objects.requireNonNull(server.getEntityFromUuid(u)).isDead)) {
                            victoryRet = false;
                            break;
                        }
                    }
                }
            }
            else victoryRet = false;
        }
        if (persistence > 0) pass = true;
        if(pass) victoryRet = false;
        MusicTriggers.logger.info("Sending packet to with trigger ID: "+triggerID+" PASS: "+pass+" Victory ID: "+victoryID+" and Victory Ret: "+victoryRet+" to player: "+player.getName());
        RegistryHandler.network.sendTo(new packetGetMobInfo.packetGetMobInfoMessage(triggerID,pass,victoryID,victoryRet),player);
    }

    @Optional.Method(modid = "infernalmobs")
    private static boolean infernalChecker(EntityLiving m, String s) {
        if (s == null || s.matches("minecraft")) {
            return true;
        }
        if(InfernalMobsCore.getMobModifiers(m)!=null) return InfernalMobsCore.getMobModifiers(m).getModName().matches(s);
        return false;
    }
}
