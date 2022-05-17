package mods.thecomputerizer.musictriggers.util;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import mods.thecomputerizer.musictriggers.util.packets.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import top.theillusivec4.champions.api.IAffix;
import top.theillusivec4.champions.common.capability.ChampionCapability;

import java.util.*;

import static mods.thecomputerizer.musictriggers.MusicTriggers.stringBreaker;

public class CalculateFeatures {

    public static List<String> allTriggers = new ArrayList<>();

    public static HashMap<String, Map<LivingEntity, Integer>> victoryMobs = new HashMap<>();
    public static HashMap<String, Float> bossInfo = new HashMap<>();

    public static void calculateServerTriggers(String[] triggers, UUID playerUUID) {
        allTriggers = getTriggers(triggers[0].replaceAll("&",""));
        ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID);
        if(player!=null) {
            StringBuilder toSend = new StringBuilder();
            String[] allSnowTriggers = stringBreaker(triggers[1], "\\$");
            boolean removeLast = false;
            for (String snow : allSnowTriggers) {
                snow = snow.replaceAll("&", "");
                if (!snow.isEmpty()) {
                    removeLast = true;
                    toSend.append(calculateSnow(toPos(snow), playerUUID));
                }
            }
            if (removeLast) toSend = new StringBuilder(toSend.substring(0, toSend.length() - 1));
            toSend.append("&#");
            removeLast = false;
            String[] allHomeTriggers = stringBreaker(triggers[2], "\\$");
            for (String home : allHomeTriggers) {
                home = home.replaceAll("&", "");
                if (!home.isEmpty()) {
                    removeLast = true;
                    toSend.append(calculateHome(toInt(home), playerUUID));
                }
            }
            if (removeLast) toSend = new StringBuilder(toSend.substring(0, toSend.length() - 1));
            toSend.append("&#");
            removeLast = false;
            String[] allBiomeTriggers = stringBreaker(triggers[3], "\\$");
            for (String biome : allBiomeTriggers) {
                biome = biome.replaceAll("&", "");
                if (!biome.isEmpty()) {
                    removeLast = true;
                    String[] biomeParameters = stringBreaker(biome, "@");
                    toSend.append(calculateBiome(biomeParameters[0], biomeParameters[1], toPos(biomeParameters[2]), playerUUID, biomeParameters[3], biomeParameters[4], toFloat(biomeParameters[5]), toBool(biomeParameters[6]), toFloat(biomeParameters[7]), toBool(biomeParameters[8])));
                }
            }
            if (removeLast) toSend = new StringBuilder(toSend.substring(0, toSend.length() - 1));
            toSend.append("&#");
            removeLast = false;
            String[] allStructureTriggers = stringBreaker(triggers[4], "\\$");
            for (String structure : allStructureTriggers) {
                structure = structure.replaceAll("&", "");
                if (!structure.isEmpty()) {
                    removeLast = true;
                    String[] structureParameters = stringBreaker(structure, "@");
                    toSend.append(calculateStruct(structureParameters[0], structureParameters[1], toPos(structureParameters[2]), playerUUID));
                }
            }
            if (removeLast) toSend = new StringBuilder(toSend.substring(0, toSend.length() - 1));
            toSend.append("&#");
            removeLast = false;
            String[] allMobTriggers = stringBreaker(triggers[5], "\\$");
            for (String mob : allMobTriggers) {
                mob = mob.replaceAll("&", "");
                if (!mob.isEmpty()) {
                    removeLast = true;
                    String[] mobParameters = stringBreaker(mob, "@");
                    toSend.append(calculateMobs(mobParameters[0], playerUUID, mobParameters[1], toInt(mobParameters[2]), toBool(mobParameters[3]), toInt(mobParameters[4]), toInt(mobParameters[5]), toInt(mobParameters[6]), toBool(mobParameters[7]), toInt(mobParameters[8]), mobParameters[9], toInt(mobParameters[10]), toInt(mobParameters[11]), mobParameters[12], mobParameters[13]));
                }
            }
            if (removeLast) toSend = new StringBuilder(toSend.substring(0, toSend.length() - 1));
            toSend.append("&#");
            removeLast = false;
            String[] allRaidTriggers = stringBreaker(triggers[6], "\\$");
            for (String raids : allRaidTriggers) {
                raids = raids.replaceAll("&", "");
                if (!raids.isEmpty()) {
                    removeLast = true;
                    String[] raidParameters = stringBreaker(raids, "@");
                    toSend.append(calculateRaid(raidParameters[0], toInt(raidParameters[1]), toPos(raidParameters[2]), playerUUID));
                }
            }
            toSend.append("&");
            if (removeLast) toSend = new StringBuilder(toSend.substring(0, toSend.length() - 1));
            PacketHandler.sendTo(new ReturnTriggerData(toSend.toString()), player);
        }
    }

    private static String calculateStruct(String triggerID, String struct, BlockPos pos, UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        String curStruct = null;
        boolean pass = false;
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            ServerWorld world = server.getLevel(Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)).level.dimension());
            if (world != null) {
                for (Structure<?> structureFeature : net.minecraftforge.registries.ForgeRegistries.STRUCTURE_FEATURES) {
                    if(world.structureFeatureManager().getStructureAt(pos,true,structureFeature.getStructure()).isValid()) {
                        if(structureFeature.getRegistryName()!=null) {
                            curStruct = structureFeature.getRegistryName().toString();
                            if(checkResourceList(curStruct,struct,false)) {
                                pass = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return triggerID+"@"+pass+"@"+curStruct+"$";
    }

    private static String calculateBiome(String triggerID, String biome, BlockPos pos, UUID uuid, String category, String rainType, float temperature, boolean cold, float rainfall, boolean togglerainfall) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        boolean pass = false;
        String currentBiome = "";
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            ServerWorld world = server.getLevel(Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)).level.dimension());
            if (world != null) {
                Biome curBiome = world.getBiome(pos);
                pass = checkBiome(curBiome,biome,category,rainType,temperature,cold,rainfall,togglerainfall);
                currentBiome = Objects.requireNonNull(curBiome.getRegistryName()).toString();
            }
        }
        return triggerID+"@"+pass+"@"+currentBiome+"$";
    }

    private static String calculateSnow(BlockPos pos, UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerPlayerEntity player = server.getPlayerList().getPlayer(uuid);
        boolean pass = false;
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            assert player != null;
            ServerWorld world = server.getLevel(player.level.dimension());
            if (world != null) {
                pass = world.getBiome(pos).getTemperature(pos)<0.2f;
            }
        }
        return pass+"$";
    }

    private static String calculateHome(int range, UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerPlayerEntity player = server.getPlayerList().getPlayer(uuid);
        boolean pass = false;
        if(player!=null) {
            ServerWorld world = server.getLevel(player.level.dimension());
            BlockPos pos = player.blockPosition();
            if (world != null && player.getRespawnPosition()!=null)
                pass = Objects.requireNonNull(player.getRespawnPosition()).closerThan(pos,range) && player.getRespawnDimension()==world.dimension() && !world.getSharedSpawnPos().closerThan(pos,range);
        }
        return pass+"$";
    }

    private static String calculateRaid(String triggerID, int wave, BlockPos pos, UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        boolean pass = false;
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            ServerPlayerEntity player = server.getPlayerList().getPlayer(uuid);
            assert player != null;
            ServerWorld world = server.getLevel(player.level.dimension());
            if (world != null) {
                Raid raid = world.getRaidAt(pos);
                pass = raid!=null && raid.getGroupsSpawned()>=wave;
            }
        }
        return triggerID+"@"+pass+"$";
    }


    private static boolean checkBiome(Biome b, String name, String category, String rainType, float temperature, boolean cold, float rainfall, boolean togglerainfall) {
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

    private static String calculateMobs(String triggerID, UUID uuid, String mobname, int detectionrange, boolean targeting, int targetingpercentage, int health, int healthpercentage, boolean victory, int victoryID, String i, int num, int timeout, String nbtKey, String c) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerPlayerEntity player = server.getPlayerList().getPlayer(uuid);
        boolean victoryRet = false;
        boolean pass = false;
        if(player!=null) {
            ServerWorld world = server.getLevel(player.level.dimension());
            assert world != null;
            List<LivingEntity> mobTempList = world.getEntitiesOfClass(LivingEntity.class, new AxisAlignedBB(player.getX() - detectionrange, player.getY() - (detectionrange / 2f), player.getZ() - detectionrange, player.getX() + detectionrange, player.getY() + (detectionrange / 2f), player.getZ() + detectionrange));
            List<MobEntity> mobList = new ArrayList<>();
            for (LivingEntity e : mobTempList) {
                if (e instanceof MobEntity && nbtChecker(e, nbtKey)) mobList.add((MobEntity) e);
            }
            int trackingCounter = 0;
            int healthCounter = 0;
            boolean infernal = true;
            boolean champion = true;
            if (mobname.matches("MOB") || stringBreaker(mobname, ";")[0].matches("MOB")) {
                List<MobEntity> mobsWithBlacklist = new ArrayList<>();
                for (Iterator<MobEntity> it = mobList.iterator(); it.hasNext(); ) {
                    MobEntity e = it.next();
                    boolean isMonster = true;
                    if (e instanceof AnimalEntity) {
                        it.remove();
                        isMonster = false;
                    }
                    if (isMonster && checkMobBlacklist(e, mobname)) {
                        mobsWithBlacklist.add(e);
                        if (e.getTarget() instanceof PlayerEntity) trackingCounter++;
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
                }
                if (mobsWithBlacklist.size() >= num &&
                        ((!targeting || (float) trackingCounter / num >= targetingpercentage / 100F) &&
                                infernal && champion &&
                                (float) healthCounter / num >= healthpercentage / 100F)) {
                    pass = true;
                }
                if (victoryMobs.get(triggerID) != null) {
                    if (victoryMobs.get(triggerID).keySet().size() < num) {
                        victoryMobs = new HashMap<>();
                    } else {
                        for (LivingEntity en : victoryMobs.get(triggerID).keySet()) {
                            if (en.isDeadOrDying() || en.getHealth()<=0) {
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
                            if (health / 100f >= bossInfo.get(name)) {
                                healthCounter++;
                            }
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
                List<MobEntity> mobListSpecific = new ArrayList<>();
                for (LivingEntity e : mobTempList) {
                    if ((checkResourceList(e.getDisplayName().getString(), mobname, true) || checkResourceList(Objects.requireNonNull(e.getType().getRegistryName()).toString(), mobname, false)) && nbtChecker(e, nbtKey)) {
                        if (e instanceof MobEntity) {
                            mobCounter++;
                            mobListSpecific.add((MobEntity) e);
                        }
                    }
                }
                for (MobEntity e : mobListSpecific) {
                    if (e.getTarget() instanceof PlayerEntity) trackingCounter++;
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
                if (mobCounter >= num &&
                        ((!targeting || (float) trackingCounter / num >= targetingpercentage / 100F) &&
                                infernal && champion &&
                                (float) healthCounter / num >= healthpercentage / 100F)) {
                    pass = true;
                }
                if (victoryMobs.get(triggerID) != null) {
                    if (victoryMobs.get(triggerID).keySet().size() < num) {
                        victoryMobs = new HashMap<>();
                    } else {
                        for (LivingEntity en : victoryMobs.get(triggerID).keySet()) {
                            if (en.isDeadOrDying() || en.getHealth()<=0) {
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

    private static boolean infernalChecker(LivingEntity m, String s) {
        if (ModList.get().isLoaded("infernalmobs")) {
            if (s == null || s.matches("minecraft")) return true;
            if(InfernalMobsCore.getMobModifiers(m)!=null) return InfernalMobsCore.getMobModifiers(m).getModName().matches(s);
            return false;
        }
        return true;
    }

    private static boolean championChecker(LivingEntity m, String s) {
        if (ModList.get().isLoaded("champions")) {
            if (s == null || s.matches("minecraft")) return true;
            if(ChampionCapability.getCapability(m).isPresent() && ChampionCapability.getCapability(m).resolve().isPresent()) {
                for(IAffix afix : ChampionCapability.getCapability(m).resolve().get().getServer().getAffixes()) {
                    return afix.getIdentifier().matches(s);
                }
            }
            return false;
        }
        return true;
    }

    private static boolean nbtChecker(LivingEntity e, String nbt) {
        String[] splitNBT = nbt.split(":");
        if(splitNBT.length==1) return e.serializeNBT().contains(nbt) || nbt.matches("_");
        else {
            if(e.serializeNBT().contains(splitNBT[0])) {
                CompoundNBT compound = e.serializeNBT().getCompound(splitNBT[0]);
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

    public static boolean checkMobBlacklist(MobEntity e, String resourceList) {
        for(String resource : stringBreaker(resourceList,";")) {
            if(!resource.matches("MOB")) {
                if (e.getName().getString().matches(resource)) return false;
                else if (Objects.requireNonNull(e.getType().getRegistryName()).toString().contains(resource)) return false;
            }
        }
        return true;
    }

    public static List<String> getTriggers(String triggers) {
        return new ArrayList<>(Arrays.asList(stringBreaker(triggers,",")));
    }

    public static int toInt(String s) {
        return Integer.parseInt(s);
    }

    public static BlockPos toPos(String s) {
        return BlockPos.of(Long.parseLong(s));
    }

    public static boolean toBool(String s) {
        return Boolean.parseBoolean(s);
    }

    public static float toFloat(String s) {
        return Float.parseFloat(s);
    }
}
