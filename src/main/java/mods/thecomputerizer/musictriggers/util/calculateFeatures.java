package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.common.ServerChannelData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;

import java.util.*;

import static mods.thecomputerizer.musictriggers.MusicTriggers.stringBreaker;

public class CalculateFeatures {

    public static List<String> allTriggers = new ArrayList<>();

    public static HashMap<String, Map<LivingEntity, Integer>> victoryMobs = new HashMap<>();
    public static HashMap<String, Float> bossInfo = new HashMap<>();

    public static ServerChannelData calculateServerTriggers(ServerChannelData serverData, MinecraftServer server) {
        allTriggers = serverData.getAllTriggers();
        if(!serverData.getCurrentSong().matches("placeholder")) EventsCommon.currentSongs.get(serverData.getPlayerUUID()).add(serverData.getCurrentSong());
        if(server!=null) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(serverData.getPlayerUUID());
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
        if(player!=null && server.getWorld(player.world.getRegistryKey())!=null) {
            BlockPos pos = roundedPos(player);
            return !Objects.requireNonNull(server.getWorld(player.world.getRegistryKey())).getBiome(pos).value().doesNotSnow(pos);
        } return false;
    }

    public static boolean calculateHome(ServerChannelData.Home home, ServerPlayerEntity player) {
        if(player!=null && player.getSpawnPointPosition()!=null && player.getSpawnPointDimension()==player.world.getRegistryKey())
            return player.getSpawnPointPosition().isWithinDistance(roundedPos(player),home.getRange());
        return false;
    }

    public static boolean calculateBiome(ServerChannelData.Biome biome, MinecraftServer server, ServerPlayerEntity player) {
        if(player!=null) {
            ServerWorld world = server.getWorld(player.world.getRegistryKey());
            if (world != null) {
                BlockPos pos = roundedPos(player);
                RegistryEntry<Biome> curBiomeHolder = world.getBiome(pos);
                if(curBiomeHolder.getKey().isPresent() && curBiomeHolder.getKey().get().getValue()!=null) biome.setCurrentBiome(Objects.requireNonNull(curBiomeHolder.getKey().get().getValue()).toString());
                return checkBiome(curBiomeHolder, biome);
            }
        } return false;
    }

    @SuppressWarnings("deprecation")
    private static boolean checkBiome(RegistryEntry<Biome> curBiomeHolder, ServerChannelData.Biome biome) {
        if(curBiomeHolder.getKey().isPresent() && curBiomeHolder.getKey().get().getValue()!=null) {
            if (biome.getBiome().matches("minecraft") || checkResourceList(Objects.requireNonNull(curBiomeHolder.getKey().get().getValue()).toString(), biome.getBiome(), false)) {
                if (biome.getCategory().matches("nope") || checkResourceList(Biome.getCategory(curBiomeHolder).getName(), biome.getCategory(), false)) {
                    if (biome.getRainType().matches("nope") || curBiomeHolder.value().getPrecipitation().getName().contains(biome.getRainType())) {
                        boolean pass = false;
                        if (biome.getRainfall() == -111f) pass = true;
                        else if (curBiomeHolder.value().getDownfall() > biome.getRainfall() && biome.isTogglerainfall()) pass = true;
                        else if (curBiomeHolder.value().getDownfall() < biome.getRainfall() && !biome.isTogglerainfall()) pass = true;
                        if (pass) {
                            float bt = curBiomeHolder.value().getTemperature();
                            if (biome.getTemperature() == -111) return true;
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
            ServerWorld world = server.getWorld(player.world.getRegistryKey());
            if (world != null) {
                BlockPos pos = roundedPos(player);
                for (ConfiguredStructureFeature<?,?> feature : world.getChunk(pos).getStructureReferences().keySet()) {
                    if (world.getStructureAccessor().getStructureAt(pos, feature).isInExistingChunk()) {
                        if(Registry.STRUCTURE_FEATURE.getId(feature.feature)!=null) {
                            structure.setCurrentStructure(Objects.requireNonNull(Registry.STRUCTURE_FEATURE.getId(feature.feature)).toString());
                            return checkResourceList(structure.getCurrentStructure(), structure.getStructure(), false);
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
            ServerWorld world = server.getWorld(player.world.getRegistryKey());
            if(world!=null) {
                List<LivingEntity> mobTempList = world.getEntitiesByClass(LivingEntity.class, new Box(player.getX() - (double) mob.getRange(), player.getY() - ((double) mob.getRange() / 2), player.getZ() - (double) mob.getRange(), player.getX() + (double) mob.getRange(), player.getY() + ((double) mob.getRange() / 2), player.getZ() + (double) mob.getRange()), EntityPredicates.VALID_LIVING_ENTITY);
                List<MobEntity> mobList = new ArrayList<>();
                for (LivingEntity e : mobTempList)
                    if (e instanceof MobEntity && nbtChecker(e, mob.getNbtKey())) mobList.add((MobEntity) e);
                int trackingCounter = 0;
                int healthCounter = 0;
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
                            if (mob.getVictory()) {
                                victoryMobs.computeIfAbsent(mob.getTrigger(), k -> new HashMap<>());
                                if (victoryMobs.get(mob.getTrigger()).size() < mob.getMobLevel())
                                    victoryMobs.get(mob.getTrigger()).put(e, mob.getVictoryTimeout());
                            }
                        }
                    }
                    if (mobsWithBlacklist.size() >= mob.getMobLevel() &&
                            ((!mob.getTargetting() || (float) trackingCounter / mob.getMobLevel() >= mob.getTargettingPercentage() / 100F) &&
                                    (float) healthCounter / mob.getMobLevel() >= mob.getHealthPercentage() / 100F)) {
                        pass = true;
                    }
                    if (victoryMobs.get(mob.getTrigger()) != null) {
                        if (victoryMobs.get(mob.getTrigger()).keySet().size() < mob.getMobLevel()) {
                            victoryMobs = new HashMap<>();
                        } else {
                            for (LivingEntity e : victoryMobs.get(mob.getTrigger()).keySet()) {
                                if (e.isDead() || e.getHealth() == 0) {
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
                    List<MobEntity> mobListSpecific = new ArrayList<>();
                    for (LivingEntity e : mobTempList) {
                        if ((checkResourceList(e.getDisplayName().getString(), mob.getName(), true) || checkResourceList(Objects.requireNonNull(e.getType().getName()).getString(), mob.getName(), false)) && nbtChecker(e, mob.getNbtKey())) {
                            mobCounter++;
                            mobListSpecific.add((MobEntity) e);
                        }
                    }
                    for (MobEntity  e : mobListSpecific) {
                        if (e.getTarget() instanceof PlayerEntity) trackingCounter++;
                        if (e.getHealth() / e.getMaxHealth() <= mob.getHealth() / 100F) healthCounter++;
                        if (mob.getVictory()) {
                            victoryMobs.computeIfAbsent(mob.getTrigger(), k -> new HashMap<>());
                            if (victoryMobs.get(mob.getTrigger()).size() < mob.getMobLevel()) {
                                victoryMobs.get(mob.getTrigger()).put(e, mob.getVictoryTimeout());
                            }
                        }
                    }
                    if (mobCounter >= mob.getMobLevel() && ((!mob.getTargetting() || (float) trackingCounter / mob.getMobLevel() >= mob.getTargettingPercentage() / 100F) && (float) healthCounter / mob.getMobLevel() >= mob.getHealthPercentage() / 100F)) {
                        pass = true;
                    }
                    if (victoryMobs.get(mob.getTrigger()) != null) {
                        if (victoryMobs.get(mob.getTrigger()).keySet().size() < mob.getMobLevel()) {
                            victoryMobs = new HashMap<>();
                        } else {
                            for (LivingEntity e : victoryMobs.get(mob.getTrigger()).keySet()) {
                                if (e.isDead() || e.getHealth() == 0) {
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
    private static boolean nbtChecker(LivingEntity e, String nbt) {
        String[] splitNBT = nbt.split(":");
        if(splitNBT.length==1) return e.writeNbt(new NbtCompound()).contains(nbt) || nbt.matches("_");
        else {
            if(e.writeNbt(new NbtCompound()).contains(splitNBT[0])) {
                NbtCompound compound = e.writeNbt(new NbtCompound()).getCompound(splitNBT[0]);
                if(splitNBT.length==2) return e.writeNbt(new NbtCompound()).getString(splitNBT[0]).matches(splitNBT[1]);
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
                else if (Objects.requireNonNull(e.getType().getName()).getString().contains(resource)) return false;
            }
        }
        return true;
    }

    public static BlockPos roundedPos(ServerPlayerEntity p) {
        return new BlockPos((Math.round(p.getX() * 2) / 2.0), (Math.round(p.getY() * 2) / 2.0), (Math.round(p.getZ() * 2) / 2.0));
    }
}
