package mods.thecomputerizer.musictriggers.common;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.musictriggers.util.packetGetMobInfo;
import mods.thecomputerizer.musictriggers.util.packetToClient;
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

public class calculateFeature {

    public static HashMap<Integer, Map<EntityLiving, Integer>> victoryMobs = new HashMap<>();
    private static boolean infernalLoaded = false;
    private static final HashMap<Integer, Boolean> dead = new HashMap<>();
    public static boolean boss;

    public static void calculateStructAndSend(String struct, BlockPos pos, Integer dimID, UUID uuid) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        WorldServer world = server.getWorld(dimID);
        if(world!=null) {
            if (world.getChunkProvider().isInsideStructure(world, struct, pos)) {
                RegistryHandler.network.sendTo(new packetToClient.packetToClientMessage(true +","+struct), server.getPlayerList().getPlayerByUUID(uuid));
            } else {
                RegistryHandler.network.sendTo(new packetToClient.packetToClientMessage(false +","+struct), server.getPlayerList().getPlayerByUUID(uuid));
            }
        }
    }

    public static void calculateMobAndSend(UUID uuid, String mobname, int detectionrange, boolean targetting, int targettingpercentage, int health, int healthpercentage, boolean victory, int victoryID, String i, int num, int persistence, int timeout) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        EntityPlayerMP player = (EntityPlayerMP)server.getEntityFromUuid(uuid);
        assert player != null;
        WorldServer world = server.getWorld(player.dimension);
        boolean pass = false;
        List<EntityLiving> mobTempList = world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(player.posX - (double) detectionrange, player.posY - ((double) detectionrange / 2), player.posZ - (double) detectionrange, player.posX + (double) detectionrange, player.posY + ((double) detectionrange / 2), player.posZ + (double) detectionrange));
        List<EntityLiving> mobList = new ArrayList<>();
        for (EntityLiving e : mobTempList) {
            if (e instanceof EntityMob || e instanceof EntityDragon) {
                mobList.add(e);
            }
        }
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
        } else if (mobname.matches("BOSS")) {
            int mobCounter = 0;
            for (EntityLiving e : mobList) {
                if(boss) {
                    mobCounter++;
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
            }
            if (mobCounter >= num && ((!targetting || (float) trackingCounter / num >= targettingpercentage / 100F) && infernalDone && (float) healthCounter / num >= healthpercentage / 100F)) {
                pass = true;
            }
        } else {
            int mobCounter = 0;
            List<EntityLiving> mobListSpecific = new ArrayList<>();
            for (EntityLiving e : mobTempList) {
                if (e.getName().matches(mobname) || Objects.requireNonNull(EntityList.getKey(e)).toString().matches(mobname)) {
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
        }
        if (persistence > 0) {
            pass = true;
        } else {
            for (Map.Entry<Integer, Map<EntityLiving, Integer>> integerMapEntry : victoryMobs.entrySet()) {
                boolean alldead = true;
                for (Map.Entry<EntityLiving, Integer> entityLivingIntegerEntry : victoryMobs.get((integerMapEntry).getKey()).entrySet()) {
                    if (!entityLivingIntegerEntry.getKey().isDead) {
                        alldead = false;
                        break;
                    }
                }
                dead.put(victoryID, alldead);
            }
        }
        RegistryHandler.network.sendTo(new packetGetMobInfo.packetGetMobInfoMessage(mobname,pass),player);
    }

    @Optional.Method(modid = "infernalmobs")
    private static boolean infernalChecker(EntityLiving m, String s) {
        infernalLoaded = true;
        if (s == null) {
            return true;
        }
        return InfernalMobsCore.getMobModifiers(m).getModName().matches(s);
    }
}
