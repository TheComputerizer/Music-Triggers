package mods.thecomputerizer.musictriggers.util;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import c4.champions.common.capability.CapabilityChampionship;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.util.packets.PacketReturnTriggers;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;

import java.util.*;

import static mods.thecomputerizer.musictriggers.MusicTriggers.stringBreaker;

public class CalculateFeatures {

    public static List<String> allTriggers = new ArrayList<>();

    public static HashMap<String, Map<EntityLiving, Integer>> victoryMobs = new HashMap<>();
    public static HashMap<String, Float> bossInfo = new HashMap<>();

    public static void calculateServerTriggers(String[] triggers, UUID playerUUID) {
        allTriggers = getTriggers(triggers[0]);
        StringBuilder toSend = new StringBuilder();
        String[] allHomeTriggers = stringBreaker(triggers[1],"\\$");
        boolean removeLast = false;
        for(String home : allHomeTriggers) {
            home = home.replaceAll("&","");
            if(!home.isEmpty()) {
                removeLast = true;
                toSend.append(calculateHome(toInt(home), playerUUID));
            }
        }
        if(removeLast) toSend = new StringBuilder(toSend.substring(0, toSend.length() - 1));
        toSend.append("&#");
        removeLast = false;
        String[] allStructureTriggers = stringBreaker(triggers[2],"\\$");
        for(String structure : allStructureTriggers) {
            structure = structure.replaceAll("&","");
            if(!structure.isEmpty()) {
                removeLast = true;
                String[] structureParameters = stringBreaker(structure, "@");
                toSend.append(calculateStruct(structureParameters[0], structureParameters[1], toPos(structureParameters[2]), toInt(structureParameters[3])));
            }
        }
        if(removeLast) toSend = new StringBuilder(toSend.substring(0, toSend.length() - 1));
        toSend.append("&#");
        removeLast = false;
        String[] allMobTriggers = stringBreaker(triggers[3],"\\$");
        for(String mob : allMobTriggers) {
            mob = mob.replaceAll("&","");
            if(!mob.isEmpty()) {
                removeLast = true;
                String[] mobParameters = stringBreaker(mob, "@");
                toSend.append(calculateMob(mobParameters[0], playerUUID, mobParameters[1], toInt(mobParameters[2]), toBool(mobParameters[3]), toInt(mobParameters[4]), toInt(mobParameters[5]), toInt(mobParameters[6]), toBool(mobParameters[7]), toInt(mobParameters[8]), mobParameters[9], toInt(mobParameters[10]), toInt(mobParameters[11]), mobParameters[12], mobParameters[13]));
            }
        }
        if(removeLast) toSend = new StringBuilder(toSend.substring(0, toSend.length() - 1));
        toSend.append("&");
        RegistryHandler.network.sendTo(new PacketReturnTriggers.PacketReturnTriggersMessage(toSend.toString()),FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(playerUUID));
    }

    public static String calculateHome(Integer range, UUID uuid) {
        EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(uuid);
        boolean pass = player.getBedLocation(player.dimension).getDistance(roundedPos(player).getX(), roundedPos(player).getY(), roundedPos(player).getZ())<=range;
        return pass+"$";
    }

    public static String calculateStruct(String triggerID, String struct, BlockPos pos, Integer dimID) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        WorldServer world = server.getWorld(dimID);
        boolean pass = false;
        for(String actualStructure : stringBreaker(struct,";")) {
            pass = world.getChunkProvider().isInsideStructure(world, actualStructure, pos);
            if(pass) break;
        }
        return triggerID+"@"+pass+"$";
    }

    public static String calculateMob(String triggerID, UUID uuid, String mobname, int detectionrange, boolean targetting, int targettingpercentage, int health, int healthpercentage, boolean victory, int victoryID, String i, int num, int timeout, String nbtKey, String c) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        EntityPlayerMP player = (EntityPlayerMP)server.getEntityFromUuid(uuid);
        boolean pass = false;
        boolean victoryRet = false;
        if(player!=null) {
            WorldServer world = server.getWorld(player.dimension);
            List<EntityLiving> mobTempList = world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(player.posX - (double) detectionrange, player.posY - ((double) detectionrange / 2), player.posZ - (double) detectionrange, player.posX + (double) detectionrange, player.posY + ((double) detectionrange / 2), player.posZ + (double) detectionrange));
            List<EntityLiving> mobList = new ArrayList<>();
            for (EntityLiving e : mobTempList) {
                if ((e instanceof EntityMob || e instanceof EntityDragon) && nbtChecker(e, nbtKey)) mobList.add(e);
            }
            int trackingCounter = 0;
            int healthCounter = 0;
            boolean infernal = true;
            boolean champion = true;
            if (mobname.matches("MOB") || stringBreaker(mobname, ";")[0].matches("MOB")) {
                List<EntityLiving> mobsWithBlacklist = new ArrayList<>();
                for (EntityLiving e : mobList) {
                    if (checkMobBlacklist(e, mobname)) {
                        mobsWithBlacklist.add(e);
                        if (e.getAttackTarget() == player) trackingCounter++;
                        if (e.getHealth() / e.getMaxHealth() <= (float) health / 100F) healthCounter++;
                        infernal = infernalChecker(e,i);
                        champion = championChecker(e,c);
                        if (victory) {
                            victoryMobs.computeIfAbsent(triggerID, k -> new HashMap<>());
                            if (victoryMobs.get(triggerID).size() < num)
                                victoryMobs.get(triggerID).put(e, timeout);
                        }
                    }
                }
                if (mobsWithBlacklist.size() >= num &&
                        ((!targetting || (float) trackingCounter / num >= targettingpercentage / 100F) &&
                                infernal && champion &&
                                (float) healthCounter / num >= healthpercentage / 100F)) {
                    pass = true;
                }
                if (victoryMobs.get(triggerID) != null) {
                    if (victoryMobs.get(triggerID).keySet().size() < num) {
                        victoryMobs = new HashMap<>();
                    } else {
                        for (EntityLiving e : victoryMobs.get(triggerID).keySet()) {
                            if (e.isDead || e.getHealth()==0) {
                                victoryRet = true;
                                break;
                            }
                        }
                    }
                }
            } else if (mobname.matches("BOSS") || stringBreaker(mobname, ";")[0].matches("BOSS")) {
                HashMap<String, Float> tempBoss = bossInfo;
                if (!bossInfo.isEmpty()) {
                    List<String> correctBosses = new ArrayList<>();
                    for (String name : tempBoss.keySet()) {
                        if (checkResourceList(name, mobname, true)) {
                            correctBosses.add(name);
                            if (health / 100f >= bossInfo.get(name)) healthCounter++;
                        }
                    }
                    if (correctBosses.size() >= num && (float) healthCounter / bossInfo.size() <= 100f / healthpercentage) {
                        pass = true;
                    }
                    for (String name : tempBoss.keySet()) {
                        if (tempBoss.get(name) <= 0f) bossInfo.remove(name);
                    }
                }
            } else {
                int mobCounter = 0;
                List<EntityLiving> mobListSpecific = new ArrayList<>();
                for (EntityLiving e : mobTempList) {
                    if ((checkResourceList(e.getName(), mobname, true) || checkResourceList(Objects.requireNonNull(EntityList.getKey(e)).toString(), mobname, true)) && nbtChecker(e, nbtKey)) {
                        mobCounter++;
                        mobListSpecific.add(e);
                    }
                }
                for (EntityLiving e : mobListSpecific) {
                    if (e.getAttackTarget() == player) trackingCounter++;
                    if (e.getHealth() / e.getMaxHealth() <= health / 100F) healthCounter++;
                    infernal = infernalChecker(e,i);
                    champion = championChecker(e,c);
                    if (victory) {
                        victoryMobs.computeIfAbsent(triggerID, k -> new HashMap<>());
                        if (victoryMobs.get(triggerID).size() < num) {
                            victoryMobs.get(triggerID).put(e, timeout);
                        }
                    }
                }
                if (mobCounter >= num && ((!targetting || (float) trackingCounter / num >= targettingpercentage / 100F) && infernal && champion && (float) healthCounter / num >= healthpercentage / 100F)) {
                    pass = true;
                }
                if (victoryMobs.get(triggerID) != null) {
                    if (victoryMobs.get(triggerID).keySet().size() < num) {
                        victoryMobs = new HashMap<>();
                    } else {
                        for (EntityLiving e : victoryMobs.get(triggerID).keySet()) {
                            if (e.isDead || e.getHealth()==0) {
                                victoryRet = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return triggerID+"@"+pass+"@"+victoryID+"@"+victoryRet+"$";
    }

    private static boolean infernalChecker(EntityLiving m, String s) {
        if(Loader.isModLoaded("infernalmobs")) {
            if (s == null || s.matches("minecraft")) return true;
            if (InfernalMobsCore.getMobModifiers(m) != null)
                return InfernalMobsCore.getMobModifiers(m).getModName().matches(s);
            return false;
        }
        return true;
    }

    private static boolean championChecker(EntityLiving m, String s) {
        if(Loader.isModLoaded("champions")) {
            if (s == null || s.matches("minecraft")) return true;
            if (CapabilityChampionship.getChampionship(m)!=null)
                return Objects.requireNonNull(CapabilityChampionship.getChampionship(m)).getName().matches(s);
            return false;
        }
        return true;
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
            if(!resource.matches("BOSS"))
                if ((match && type.matches(resource)) || (!match && type.contains(resource))) return true;
        }
        return false;
    }

    public static boolean checkMobBlacklist(EntityLiving e, String resourceList) {
        for(String resource : stringBreaker(resourceList,";")) {
            if(!resource.matches("MOB"))
                if ((e.getName().matches(resource)) || (Objects.requireNonNull(EntityList.getKey(e)).toString().matches(resource))) return false;
        }
        return true;
    }

    public static BlockPos roundedPos(EntityPlayer p) {
        return new BlockPos((Math.round(p.posX * 2) / 2.0), (Math.round(p.posY * 2) / 2.0), (Math.round(p.posZ * 2) / 2.0));
    }

    public static List<String> getTriggers(String triggers) {
        return new ArrayList<>(Arrays.asList(stringBreaker(triggers,",")));
    }

    public static int toInt(String s) {
        return Integer.parseInt(s);
    }

    public static BlockPos toPos(String s) {
        return BlockPos.fromLong(Long.parseLong(s));
    }

    public static boolean toBool(String s) {
        return Boolean.parseBoolean(s);
    }
}
