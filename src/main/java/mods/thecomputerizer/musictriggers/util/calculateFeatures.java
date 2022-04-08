package mods.thecomputerizer.musictriggers.util;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import mods.thecomputerizer.musictriggers.util.packets.packetGetMobInfo;
import mods.thecomputerizer.musictriggers.util.packets.packetToClient;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureMineshaftStart;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;

import java.util.*;

import static mods.thecomputerizer.musictriggers.client.MusicPicker.stringBreaker;

public class calculateFeatures {

    public static List<String> allTriggers = new ArrayList<>();

    public static HashMap<String, Map<UUID, Integer>> victoryMobs = new HashMap<>();
    public static HashMap<String, Map<String, Integer>> victoryBosses = new HashMap<>();
    public static HashMap<String, Float> bossInfo = new HashMap<>();

    public static void calculateStructAndSend(String triggerID, String struct, BlockPos pos, Integer dimID, UUID uuid) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        WorldServer world = server.getWorld(dimID);
        if(world!=null) {
            boolean pass = false;
            for(String actualStructure : stringBreaker(struct,";")) {
                pass = world.getChunkProvider().isInsideStructure(world, actualStructure, pos);
                if(pass) break;
            }
            if (pass) {
                RegistryHandler.network.sendTo(new packetToClient.packetToClientMessage(true +","+triggerID), server.getPlayerList().getPlayerByUUID(uuid));
            } else {
                RegistryHandler.network.sendTo(new packetToClient.packetToClientMessage(false +","+triggerID), server.getPlayerList().getPlayerByUUID(uuid));
            }
        }
    }

    public static void calculateMobAndSend(String triggerID, UUID uuid, String mobname, int detectionrange, boolean targetting, int targettingpercentage, int health, int healthpercentage, boolean victory, int victoryID, String i, int num, int timeout, String nbtKey) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        EntityPlayerMP player = (EntityPlayerMP)server.getEntityFromUuid(uuid);
        assert player != null;
        WorldServer world = server.getWorld(player.dimension);
        boolean pass = false;
        List<EntityLiving> mobTempList = world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(player.posX - (double) detectionrange, player.posY - ((double) detectionrange / 2), player.posZ - (double) detectionrange, player.posX + (double) detectionrange, player.posY + ((double) detectionrange / 2), player.posZ + (double) detectionrange));
        List<EntityLiving> mobList = new ArrayList<>();
        for (EntityLiving e : mobTempList) {
            if ((e instanceof EntityMob || e instanceof EntityDragon) && nbtChecker(e, nbtKey)) {
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
                    victoryMobs.computeIfAbsent(triggerID, k -> new HashMap<>());
                    if (victoryMobs.get(triggerID).size() < num) victoryMobs.get(triggerID).put(e.getUniqueID(), timeout);
                }
            }
            if (mobList.size() >= num &&
                    ((!targetting || (float) trackingCounter / num >= targettingpercentage / 100F) &&
                            infernalDone &&
                            (float) healthCounter / num >= healthpercentage / 100F)) {
                pass = true;
            }
            if(victoryMobs.get(triggerID)!=null) {
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
            HashMap<String, Float> tempBoss = bossInfo;
            if(!bossInfo.isEmpty()) {
                for(String name : tempBoss.keySet()) {
                    if (health / 100f >= bossInfo.get(name)) {
                        healthCounter++;
                    }
                    if (victory) {
                        victoryBosses.computeIfAbsent(triggerID, k -> new HashMap<>());
                        if (victoryBosses.get(triggerID).keySet().size() < num) victoryBosses.get(triggerID).put(name, timeout);
                    }
                }
                if(bossInfo.size()>=num && (float)healthCounter/bossInfo.size()<=100f/healthpercentage) {
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
            List<EntityLiving> mobListSpecific = new ArrayList<>();
            for (EntityLiving e : mobTempList) {
                if ((checkResourceList(e.getName(),mobname,true)|| checkResourceList(Objects.requireNonNull(EntityList.getKey(e)).toString(),mobname,true)) && nbtChecker(e, nbtKey)) {
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
                    victoryMobs.computeIfAbsent(triggerID, k -> new HashMap<>());
                    if (victoryMobs.get(triggerID).size() < num) {
                        victoryMobs.get(triggerID).put(e.getUniqueID(), timeout);
                    }
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
                        if ((server.getEntityFromUuid(u)!=null && !Objects.requireNonNull(server.getEntityFromUuid(u)).isDead)) {
                            victoryRet = false;
                            break;
                        }
                    }
                }
            }
            else victoryRet = false;
        }
        if(pass) victoryRet = false;
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

    private static boolean nbtChecker(EntityLiving e, String nbt) {
        String[] splitNBT = nbt.split(":");
        if(splitNBT.length==1) return e.serializeNBT().hasKey(nbt) || nbt.matches("_");
        else {
            if(e.serializeNBT().hasKey(splitNBT[0])) {
                NBTTagCompound compound = e.serializeNBT().getCompoundTag(splitNBT[0]);
                if(splitNBT.length==2) return e.serializeNBT().getTag(splitNBT[0]).toString().matches(splitNBT[1]);
                else {
                    for (int i = 1; i < splitNBT.length - 2; i++) {
                        if (compound.hasKey(splitNBT[i])) compound = compound.getCompoundTag(splitNBT[i]);
                    }
                    return compound.getTag(splitNBT[splitNBT.length-2]).toString().matches(splitNBT[splitNBT.length-1]);
                }
            }
        }
        return false;
    }

    public static boolean checkResourceList(String type, String resourceList, boolean match) {
        for(String resource : stringBreaker(resourceList,";")) {
            if(match && type.matches(resource)) return true;
            else if(!match && type.contains(resource)) return true;
        }
        return false;
    }
}
