package mods.thecomputerizer.musictriggers.util;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.common.ServerChannelData;
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

    public static ServerChannelData calculateServerTriggers(ServerChannelData serverData) {
        allTriggers = serverData.getAllTriggers();
        EventsCommon.recordMenu.putIfAbsent(serverData.getPlayerUUID(), new ArrayList<>());
        EventsCommon.recordMenu.get(serverData.getPlayerUUID()).addAll(serverData.getMenuSongs());
        EventsCommon.activeTriggerList.get(serverData.getPlayerUUID()).put(serverData.getChannel(), serverData.getActiveTriggers());
        if(!serverData.getCurrentSong().matches("placeholder"))
            EventsCommon.currentChannelSongs.get(serverData.getPlayerUUID()).put(serverData.getChannel(), serverData.getCurrentSong());
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server!=null) {
            ServerPlayerEntity player = server.getPlayerList().getPlayer(serverData.getPlayerUUID());
            for (ServerChannelData.Snow snow : serverData.getSnowTriggers())
                snow.setActive(calculateSnow(server, player));
            for (ServerChannelData.Home home : serverData.getHomeTriggers())
                home.setActive(calculateHome(home, player));
            for (ServerChannelData.Biome biome : serverData.getBiomeTriggers())
                biome.setActive(calculateBiome(biome, server, player));
            for (ServerChannelData.Structure structure : serverData.getStructureTriggers())
                structure.setActive(calculateStructure(structure, server, player));
            for (ServerChannelData.Mob mob : serverData.getMobTriggers())
                mob.setActive(calculateMob(mob, server, player));
        }
        return serverData;
    }

    public static boolean calculateSnow(MinecraftServer server, ServerPlayerEntity player) {
        if(player!=null && server.getLevel(player.level.dimension())!=null) {
            BlockPos pos = roundedPos(player);
            return Objects.requireNonNull(server.getLevel(player.level.dimension())).getBiome(pos).getTemperature(pos)<0.2f;
        } return false;
    }

    public static boolean calculateHome(ServerChannelData.Home home, ServerPlayerEntity player) {
        if(player!=null && player.getRespawnPosition()!=null && player.getRespawnDimension()==player.level.dimension())
            return player.getRespawnPosition().closerThan(roundedPos(player),home.getRange());
        return false;
    }

    public static boolean calculateBiome(ServerChannelData.Biome biome, MinecraftServer server, ServerPlayerEntity player) {
        if(player!=null) {
            ServerWorld world = server.getLevel(player.level.dimension());
            if (world != null) {
                BlockPos pos = roundedPos(player);
                Biome curBiome = world.getBiome(pos);
                if(curBiome.getRegistryName()!=null) biome.setCurrentBiome(curBiome.getRegistryName().toString());
                return checkBiome(curBiome, biome);
            }
        } return false;
    }

    private static boolean checkBiome(Biome curBiome, ServerChannelData.Biome biome) {
        if(curBiome.getRegistryName()!=null) {
            if (biome.getBiome().matches("any")
                    || checkResourceList(curBiome.getRegistryName().toString(), biome.getBiome(), false)) {
                if (biome.getCategory().matches("any")
                        || checkResourceList(curBiome.getBiomeCategory().getName(), biome.getCategory(), false)) {
                    if (biome.getRainType().matches("any")
                            || curBiome.getPrecipitation().getName().contains(biome.getRainType())) {
                        boolean pass = false;
                        if (biome.getRainfall()==Float.MIN_VALUE) pass = true;
                        else if (curBiome.getDownfall() > biome.getRainfall() && biome.isTogglerainfall()) pass = true;
                        else if (curBiome.getDownfall() < biome.getRainfall() && !biome.isTogglerainfall()) pass = true;
                        if (pass) {
                            float bt = curBiome.getBaseTemperature();
                            if (biome.getTemperature()==Float.MIN_VALUE) return true;
                            else if (bt >= biome.getTemperature() && !biome.isCold()) return true;
                            else return bt <= biome.getTemperature() && biome.isCold();
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean calculateStructure(ServerChannelData.Structure structure, MinecraftServer server, ServerPlayerEntity player) {
        if(player!=null) {
            ServerWorld world = server.getLevel(player.level.dimension());
            if (world != null) {
                for (Structure<?> structureFeature : net.minecraftforge.registries.ForgeRegistries.STRUCTURE_FEATURES) {
                    if(world.structureFeatureManager().getStructureAt(roundedPos(player),true,structureFeature.getStructure()).isValid()) {
                        if(structureFeature.getRegistryName()!=null) {
                            String curStruct = structureFeature.getRegistryName().toString();
                            structure.setCurrentStructure(curStruct);
                            return checkResourceList(curStruct,structure.getStructure(),false);
                        }
                    }
                }
            }
        } return false;
    }

    public static boolean calculateMob(ServerChannelData.Mob mob, MinecraftServer server, ServerPlayerEntity player) {
        boolean pass = false;
        boolean victoryRet = false;
        if(player!=null) {
            ServerWorld world = server.getLevel(player.level.dimension());
            if(world!=null) {
                List<LivingEntity> mobTempList = world.getEntitiesOfClass(LivingEntity.class, new AxisAlignedBB(player.getX() - (double) mob.getRange(), player.getY() - ((double) mob.getRange() / 2), player.getZ() - (double) mob.getRange(), player.getX() + (double) mob.getRange(), player.getY() + ((double) mob.getRange() / 2), player.getZ() + (double) mob.getRange()));
                List<MobEntity> mobList = new ArrayList<>();
                for (LivingEntity e : mobTempList)
                    if (e instanceof MobEntity && nbtChecker(e, mob.getNbtKey())) mobList.add((MobEntity) e);
                int trackingCounter = 0;
                int healthCounter = 0;
                boolean infernal = true;
                boolean champion = true;
                if (mob.getName().matches("MOB") || stringBreaker(mob.getName(), ";")[0].matches("MOB")) {
                    List<MobEntity> mobsWithBlacklist = new ArrayList<>();
                    for (Iterator<MobEntity> it = mobList.iterator(); it.hasNext(); ) {
                        MobEntity e = it.next();
                        boolean isMonster = true;
                        if (e instanceof AnimalEntity) {
                            it.remove();
                            isMonster = false;
                        }
                        if (isMonster && checkMobBlacklist(e, mob.getName())) {
                            mobsWithBlacklist.add(e);
                            if (e.getTarget() instanceof PlayerEntity) trackingCounter++;
                            if (e.getHealth() / e.getMaxHealth() <= (float) mob.getHealth() / 100F) healthCounter++;
                            infernal = infernalChecker(e, mob.getInfernal());
                            champion = championChecker(e, mob.getChampion());
                            if (mob.getVictoryID()>0) {
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
                            for (LivingEntity e : victoryMobs.get(mob.getTrigger()).keySet()) {
                                if (e.isDeadOrDying() || e.getHealth() == 0) {
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
                    List<MobEntity > mobListSpecific = new ArrayList<>();
                    for (LivingEntity e : mobTempList) {
                        if ((checkResourceList(e.getDisplayName().getString(), mob.getName(), true) || checkResourceList(Objects.requireNonNull(e.getType().getRegistryName()).toString(), mob.getName(), false)) && nbtChecker(e, mob.getNbtKey())) {
                            mobCounter++;
                            mobListSpecific.add((MobEntity) e);
                        }
                    }
                    for (MobEntity  e : mobListSpecific) {
                        if (e.getTarget() instanceof PlayerEntity) trackingCounter++;
                        if (e.getHealth() / e.getMaxHealth() <= mob.getHealth() / 100F) healthCounter++;
                        infernal = infernalChecker(e, mob.getInfernal());
                        champion = championChecker(e, mob.getChampion());
                        if (mob.getVictoryID()>0) {
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
                            for (LivingEntity e : victoryMobs.get(mob.getTrigger()).keySet()) {
                                if (e.isDeadOrDying() || e.getHealth() == 0) {
                                    victoryRet = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        mob.setVictory(victoryRet);
        return pass;
    }

    private static boolean infernalChecker(LivingEntity m, String s) {
        if (ModList.get().isLoaded("infernalmobs")) {
            if (s == null || s.matches("any")) return true;
            boolean foundMatch = false;
            if(InfernalMobsCore.getIsRareEntity(m)) {
                if(s.matches("ALL")) return true;
                List<String> names = Arrays.asList(InfernalMobsCore.getMobModifiers(m).getDisplayNames());
                for(String resource : stringBreaker(s,";"))
                    if (names.contains(resource)) {
                        foundMatch = true;
                        break;
                    }
            }
            return foundMatch;
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    private static boolean championChecker(LivingEntity m, String s) {
        if (ModList.get().isLoaded("champions")) {
            if (s == null || s.matches("any")) return true;
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

    public static BlockPos roundedPos(PlayerEntity p) {
        return new BlockPos((Math.round(p.getX() * 2) / 2.0), (Math.round(p.getY() * 2) / 2.0), (Math.round(p.getZ() * 2) / 2.0));
    }
}
