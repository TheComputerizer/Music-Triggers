package mods.thecomputerizer.musictriggers.util;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.common.ServerChannelData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Level;

import java.util.*;

import static mods.thecomputerizer.musictriggers.MusicTriggers.stringBreaker;

public class CalculateFeatures {

    public static List<String> allTriggers = new ArrayList<>();

    public static HashMap<String, Map<LivingEntity, Integer>> victoryMobs = new HashMap<>();
    public static HashMap<UUID, HashMap<String, Float>> perPlayerBossInfo = new HashMap<>();

    public static ServerChannelData calculateServerTriggers(ServerChannelData serverData) {
        allTriggers = serverData.getAllTriggers();
        EventsCommon.recordMenu.putIfAbsent(serverData.getPlayerUUID(), new ArrayList<>());
        EventsCommon.recordMenu.get(serverData.getPlayerUUID()).addAll(serverData.getMenuSongs());
        EventsCommon.activeTriggerList.get(serverData.getPlayerUUID()).put(serverData.getChannel(), serverData.getActiveTriggers());
        if(!serverData.getCurrentSong().matches("placeholder"))
            EventsCommon.currentChannelSongs.get(serverData.getPlayerUUID()).put(serverData.getChannel(), serverData.getCurrentSong());
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server!=null) {
            ServerPlayer player = server.getPlayerList().getPlayer(serverData.getPlayerUUID());
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
            for (ServerChannelData.Raid raid : serverData.getRaidTriggers())
                raid.setActive(calculateRaid(raid, server, player));
        }
        return serverData;
    }

    public static boolean calculateSnow(MinecraftServer server, ServerPlayer player) {
        if(player!=null && server.getLevel(player.level.dimension())!=null) {
            BlockPos pos = roundedPos(player);
            return Objects.requireNonNull(server.getLevel(player.level.dimension())).getBiome(pos).value().coldEnoughToSnow(pos);
        } return false;
    }

    public static boolean calculateHome(ServerChannelData.Home home, ServerPlayer player) {
        if(player!=null && player.getRespawnPosition()!=null && player.getRespawnDimension()==player.level.dimension())
            return player.getRespawnPosition().closerThan(roundedPos(player),home.getRange());
        return false;
    }

    public static boolean calculateBiome(ServerChannelData.Biome biome, MinecraftServer server, ServerPlayer player) {
        if(player!=null) {
            ServerLevel world = server.getLevel(player.level.dimension());
            if (Objects.nonNull(world)) {
                BlockPos pos = roundedPos(player);
                Holder<Biome> curBiomeHolder = world.getBiome(pos);
                if(curBiomeHolder.unwrapKey().isPresent()) {
                    ResourceLocation id = curBiomeHolder.unwrapKey().get().location();
                    biome.setCurrentBiome(id.toString());
                    return checkBiome(curBiomeHolder, id, biome);
                }
            }
        } return false;
    }

    private static boolean checkBiome(Holder<Biome> curBiomeHolder, ResourceLocation id, ServerChannelData.Biome biome) {
        if(Objects.nonNull(id)) {
            if (biome.getBiome().matches("any") ||
                    checkResourceList(id.toString(), biome.getBiome(), false)) {
                //if (biome.getCategory().matches("any") || checkResourceList(Biome.(curBiomeHolder).getName(), biome.getCategory(), false)) {
                    if (biome.getRainType().matches("any") || curBiomeHolder.value().getPrecipitation().getName().contains(biome.getRainType())) {
                        boolean pass = false;
                        if (biome.getRainfall()==Float.MIN_VALUE) pass = true;
                        else if (curBiomeHolder.value().getDownfall() > biome.getRainfall() && biome.isTogglerainfall()) pass = true;
                        else if (curBiomeHolder.value().getDownfall() < biome.getRainfall() && !biome.isTogglerainfall()) pass = true;
                        if (pass) {
                            float bt = curBiomeHolder.value().getBaseTemperature();
                            if (biome.getTemperature()==Float.MIN_VALUE) return true;
                            else if (bt >= biome.getTemperature() && !biome.isCold()) return true;
                            else return bt <= biome.getTemperature() && biome.isCold();
                        }
                    }
                //}
            }
        }
        return false;
    }

    public static boolean calculateStructure(ServerChannelData.Structure structure, MinecraftServer server, ServerPlayer player) {
        if(player!=null) {
            ServerLevel world = server.getLevel(player.level.dimension());
            if (Objects.nonNull(world)) {
                BlockPos pos = roundedPos(player);
                for (Structure feature : world.getChunkAt(pos).getAllReferences().keySet()) {
                    if (world.structureManager().getStructureAt(pos, feature).isValid()) {
                        if(Registry.STRUCTURE_TYPES.getKey(feature.type())!=null) {
                            structure.setCurrentStructure(Objects.requireNonNull(Registry.STRUCTURE_TYPES.getKey(feature.type())).toString());
                            return checkResourceList(structure.getCurrentStructure(), structure.getStructure(), false);
                        }
                    }
                }
            }
        } return false;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static boolean calculateMob(ServerChannelData.Mob mob, MinecraftServer server, ServerPlayer player) {
        boolean pass = false;
        boolean victoryRet = false;
        if(player!=null) {
            ServerLevel world = server.getLevel(player.level.dimension());
            if(world!=null) {
                List<LivingEntity> mobTempList = world.getEntitiesOfClass(LivingEntity.class, new AABB(player.getX() - (double) mob.getRange(), player.getY() - ((double) mob.getRange() / 2), player.getZ() - (double) mob.getRange(), player.getX() + (double) mob.getRange(), player.getY() + ((double) mob.getRange() / 2), player.getZ() + (double) mob.getRange()));
                List<Mob> mobList = new ArrayList<>();
                for (LivingEntity e : mobTempList)
                    if (e instanceof Mob && nbtChecker(e, mob.getNbtKey())) mobList.add((Mob) e);
                int trackingCounter = 0;
                int healthCounter = 0;
                boolean infernal = true;
                if (mob.getName().matches("MOB") || stringBreaker(mob.getName(), ";")[0].matches("MOB")) {
                    List<Mob> mobsWithBlacklist = new ArrayList<>();
                    for (Iterator<Mob> it = mobList.iterator(); it.hasNext(); ) {
                        Mob e = it.next();
                        boolean isMonster = true;
                        if (e instanceof Animal) {
                            it.remove();
                            isMonster = false;
                        }
                        if (isMonster && checkMobBlacklist(e, mob.getName(), server)) {
                            mobsWithBlacklist.add(e);
                            if (e.getTarget() instanceof Player) trackingCounter++;
                            if (e.getHealth() / e.getMaxHealth() <= (float) mob.getHealth() / 100F) healthCounter++;
                            infernal = infernalChecker(e, mob.getInfernal());
                            if (mob.getVictoryID()>0) {
                                victoryMobs.computeIfAbsent(mob.getTrigger(), k -> new HashMap<>());
                                if (victoryMobs.get(mob.getTrigger()).size() < mob.getMobLevel())
                                    victoryMobs.get(mob.getTrigger()).put(e, mob.getVictoryTimeout());
                            }
                        }
                    }
                    if (mobsWithBlacklist.size() >= mob.getMobLevel() &&
                            ((!mob.getTargetting() || (float) trackingCounter / mob.getMobLevel() >= mob.getTargettingPercentage() / 100F) &&
                                    infernal &&
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
                    perPlayerBossInfo.putIfAbsent(player.getUUID(), new HashMap<>());
                    HashMap<String, Float> infoMap = perPlayerBossInfo.get(player.getUUID());
                    if (!infoMap.isEmpty() && EventsCommon.bossTimers.containsKey(player.getUUID())) {
                        List<String> correctBosses = new ArrayList<>();
                        for (String name : infoMap.keySet()) {
                            if (checkResourceList(name, mob.getName(), true)) {
                                correctBosses.add(name);
                                if (mob.getHealth() / 100f >= infoMap.get(name)) healthCounter++;
                            }
                        }
                        if (correctBosses.size() >= mob.getMobLevel() &&
                                (float) healthCounter / infoMap.size() <= 100f / mob.getHealthPercentage())
                            pass = true;
                        infoMap.entrySet().removeIf(entry -> entry.getValue()<=0f);
                    }
                } else {
                    int mobCounter = 0;
                    List<Mob > mobListSpecific = new ArrayList<>();
                    for (LivingEntity e : mobTempList) {
                        if ((checkResourceList(e.getDisplayName().getString(), mob.getName(), true) ||
                                checkResourceList(Objects.requireNonNull(server.registryAccess().registry(
                                        Registry.ENTITY_TYPE_REGISTRY).get().getKey(e.getType())).toString(),
                                        mob.getName(), false)) && nbtChecker(e, mob.getNbtKey())) {
                            mobCounter++;
                            mobListSpecific.add((Mob) e);
                        }
                    }
                    for (Mob  e : mobListSpecific) {
                        if (e.getTarget() instanceof Player) trackingCounter++;
                        if (e.getHealth() / e.getMaxHealth() <= mob.getHealth() / 100F) healthCounter++;
                        infernal = infernalChecker(e, mob.getInfernal());
                        if (mob.getVictoryID()>0) {
                            victoryMobs.computeIfAbsent(mob.getTrigger(), k -> new HashMap<>());
                            if (victoryMobs.get(mob.getTrigger()).size() < mob.getMobLevel()) {
                                victoryMobs.get(mob.getTrigger()).put(e, mob.getVictoryTimeout());
                            }
                        }
                    }
                    if (mobCounter >= mob.getMobLevel() && ((!mob.getTargetting() ||
                            (float) trackingCounter / mob.getMobLevel() >= mob.getTargettingPercentage() / 100F) &&
                            infernal && (float) healthCounter / mob.getMobLevel() >= mob.getHealthPercentage() / 100F)) {
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

    private static boolean calculateRaid(ServerChannelData.Raid data, MinecraftServer server, ServerPlayer player) {
        if(Objects.nonNull(player)) {
            ServerLevel level = server.getLevel(player.level.dimension());
            if (Objects.nonNull(level)) {
                Raid raid = level.getRaidAt(player.blockPosition());
                return Objects.nonNull(raid) && raid.getGroupsSpawned() >= data.getWave();
            }
        }
        return false;
    }

    private static boolean infernalChecker(LivingEntity m, String s) {
        if (ModList.get().isLoaded("infernalmobs")) {
            if (Objects.isNull(s) || s.matches("minecraft")) return true;
            if(InfernalMobsCore.getMobModifiers(m)!=null) return InfernalMobsCore.getMobModifiers(m).getModName().matches(s);
            return false;
        }
        return true;
    }

    private static boolean nbtChecker(LivingEntity e, String nbt) {
        String[] splitNBT = nbt.split(":");
        if(splitNBT.length==1) return e.serializeNBT().contains(nbt) || nbt.matches("_");
        else {
            if(e.serializeNBT().contains(splitNBT[0])) {
                CompoundTag compound = e.serializeNBT().getCompound(splitNBT[0]);
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

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static boolean checkMobBlacklist(Mob e, String resourceList, MinecraftServer server) {
        for(String resource : stringBreaker(resourceList,";")) {
            if(!resource.matches("MOB")) {
                if (e.getName().getString().matches(resource)) return false;
                else if (Objects.requireNonNull(server.registryAccess().registry(
                        Registry.ENTITY_TYPE_REGISTRY).get().getKey(e.getType())).toString().contains(resource))
                    return false;
            }
        }
        return true;
    }

    public static BlockPos roundedPos(Player p) {
        return new BlockPos((Math.round(p.getX() * 2) / 2.0), (Math.round(p.getY() * 2) / 2.0), (Math.round(p.getZ() * 2) / 2.0));
    }
}
