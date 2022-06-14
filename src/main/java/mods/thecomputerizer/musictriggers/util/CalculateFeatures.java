package mods.thecomputerizer.musictriggers.util;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import c4.champions.common.capability.CapabilityChampionship;
import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.common.ServerChannelData;
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

    public static ServerChannelData calculateServerTriggers(ServerChannelData serverData) {
        allTriggers = serverData.getAllTriggers();
        if(!serverData.getCurrentSong().matches("placeholder")) EventsCommon.currentSongs.get(serverData.getPlayerUUID()).add(serverData.getCurrentSong());
        for(ServerChannelData.Home home : serverData.getHomeTriggers()) home.setActive(calculateHome(home,serverData.getPlayerUUID()));
        for(ServerChannelData.Structure structure : serverData.getStructureTriggers()) structure.setActive(calculateStruct(structure));
        for(ServerChannelData.Mob mob : serverData.getMobTriggers()) mob.setActive(calculateMob(mob, serverData.getPlayerUUID()));
        return serverData;
    }

    public static boolean calculateHome(ServerChannelData.Home home, UUID uuid) {
        EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(uuid);
        return player.getBedLocation(player.dimension).getDistance(roundedPos(player).getX(), roundedPos(player).getY(), roundedPos(player).getZ())<=home.getRange();
    }

    public static boolean calculateStruct(ServerChannelData.Structure structure) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        WorldServer world = server.getWorld(structure.getDimension());
        boolean pass = false;
        for(String actualStructure : stringBreaker(structure.getStructure(), ";")) {
            pass = world.getChunkProvider().isInsideStructure(world, actualStructure, structure.getPos());
            if(pass) break;
        }
        return pass;
    }

    public static boolean calculateMob(ServerChannelData.Mob mob, UUID uuid) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        EntityPlayerMP player = (EntityPlayerMP)server.getEntityFromUuid(uuid);
        boolean pass = false;
        boolean victoryRet = false;
        if(player!=null) {
            WorldServer world = server.getWorld(player.dimension);
            List<EntityLiving> mobTempList = world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(player.posX - (double) mob.getRange(), player.posY - ((double) mob.getRange() / 2), player.posZ - (double) mob.getRange(), player.posX + (double) mob.getRange(), player.posY + ((double) mob.getRange() / 2), player.posZ + (double) mob.getRange()));
            List<EntityLiving> mobList = new ArrayList<>();
            for (EntityLiving e : mobTempList) {
                if ((e instanceof EntityMob || e instanceof EntityDragon) && nbtChecker(e, mob.getNbtKey())) mobList.add(e);
            }
            int trackingCounter = 0;
            int healthCounter = 0;
            boolean infernal = true;
            boolean champion = true;
            if (mob.getName().matches("MOB") || stringBreaker(mob.getName(), ";")[0].matches("MOB")) {
                List<EntityLiving> mobsWithBlacklist = new ArrayList<>();
                for (EntityLiving e : mobList) {
                    if (checkMobBlacklist(e, mob.getName())) {
                        mobsWithBlacklist.add(e);
                        if (e.getAttackTarget() == player) trackingCounter++;
                        if (e.getHealth() / e.getMaxHealth() <= (float) mob.getHealth() / 100F) healthCounter++;
                        infernal = infernalChecker(e,mob.getInfernal());
                        champion = championChecker(e,mob.getChampion());
                        if (mob.getVictory()) {
                            victoryMobs.computeIfAbsent(mob.getTrigger(), k -> new HashMap<>());
                            if (victoryMobs.get(mob.getTrigger()).size() < mob.getMobLevel())
                                victoryMobs.get(mob.getTrigger()).put(e, mob.getVictoryTimeout());
                        }
                    }
                }
                if (mobsWithBlacklist.size() >= mob.getMobLevel() &&
                        ((!mob.getTargetting() || (float) trackingCounter / mob.getMobLevel() >= mob.getTargettingPercentage() / 100F) &&
                                infernal && champion &&
                                (float) healthCounter / mob.getMobLevel() >= mob.getHealthPercentage() / 100F)) {
                    pass = true;
                }
                if (victoryMobs.get(mob.getTrigger()) != null) {
                    if (victoryMobs.get(mob.getTrigger()).keySet().size() < mob.getMobLevel()) {
                        victoryMobs = new HashMap<>();
                    } else {
                        for (EntityLiving e : victoryMobs.get(mob.getTrigger()).keySet()) {
                            if (e.isDead || e.getHealth()==0) {
                                victoryRet = true;
                                break;
                            }
                        }
                    }
                }
            } else if (mob.getName().matches("BOSS") || stringBreaker(mob.getName(), ";")[0].matches("BOSS")) {
                HashMap<String, Float> tempBoss = bossInfo;
                if (!bossInfo.isEmpty()) {
                    List<String> correctBosses = new ArrayList<>();
                    for (String name : tempBoss.keySet()) {
                        if (checkResourceList(name, mob.getName(), true)) {
                            correctBosses.add(name);
                            if (mob.getHealth() / 100f >= bossInfo.get(name)) healthCounter++;
                        }
                    }
                    if (correctBosses.size() >= mob.getMobLevel() && (float) healthCounter / bossInfo.size() <= 100f / mob.getHealthPercentage()) {
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
                    if ((checkResourceList(e.getName(), mob.getName(), true) || checkResourceList(Objects.requireNonNull(EntityList.getKey(e)).toString(), mob.getName(), false)) && nbtChecker(e, mob.getNbtKey())) {
                        mobCounter++;
                        mobListSpecific.add(e);
                    }
                }
                for (EntityLiving e : mobListSpecific) {
                    if (e.getAttackTarget() == player) trackingCounter++;
                    if (e.getHealth() / e.getMaxHealth() <= mob.getHealth() / 100F) healthCounter++;
                    infernal = infernalChecker(e,mob.getInfernal());
                    champion = championChecker(e,mob.getChampion());
                    if (mob.getVictory()) {
                        victoryMobs.computeIfAbsent(mob.getTrigger(), k -> new HashMap<>());
                        if (victoryMobs.get(mob.getTrigger()).size() < mob.getMobLevel()) {
                            victoryMobs.get(mob.getTrigger()).put(e, mob.getVictoryTimeout());
                        }
                    }
                }
                if (mobCounter >= mob.getMobLevel() && ((!mob.getTargetting() || (float) trackingCounter / mob.getMobLevel() >= mob.getTargettingPercentage() / 100F) && infernal && champion && (float) healthCounter / mob.getMobLevel() >= mob.getHealthPercentage() / 100F)) {
                    pass = true;
                }
                if (victoryMobs.get(mob.getTrigger()) != null) {
                    if (victoryMobs.get(mob.getTrigger()).keySet().size() < mob.getMobLevel()) {
                        victoryMobs = new HashMap<>();
                    } else {
                        for (EntityLiving e : victoryMobs.get(mob.getTrigger()).keySet()) {
                            if (e.isDead || e.getHealth()==0) {
                                victoryRet = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        mob.setVictory(victoryRet);
        return pass;
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
                if ((e.getName().matches(resource)) || (Objects.requireNonNull(EntityList.getKey(e)).toString().contains(resource))) return false;
        }
        return true;
    }

    public static BlockPos roundedPos(EntityPlayer p) {
        return new BlockPos((Math.round(p.posX * 2) / 2.0), (Math.round(p.posY * 2) / 2.0), (Math.round(p.posZ * 2) / 2.0));
    }
}
