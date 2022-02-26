package mods.thecomputerizer.musictriggers.util;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import mods.thecomputerizer.musictriggers.util.packets.InfoFromBiome;
import mods.thecomputerizer.musictriggers.util.packets.InfoFromMob;
import mods.thecomputerizer.musictriggers.util.packets.InfoFromStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;

public class calculateFeatures {

    public static HashMap<Integer, Map<LivingEntity, Integer>> victoryMobs = new HashMap<>();
    private static boolean infernalLoaded = false;
    private static final HashMap<Integer, Boolean> dead = new HashMap<>();
    public static boolean boss;

    public static void calculateStructAndSend(String struct, BlockPos pos, UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            ServerLevel world = server.getLevel(Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)).level.dimension());
            if (world != null) {
                boolean good = false;
                String curStruct = null;
                for (StructureFeature<?> structureFeature : net.minecraftforge.registries.ForgeRegistries.STRUCTURE_FEATURES) {
                    if(world.structureFeatureManager().getStructureAt(pos, structureFeature).isValid()) {
                        if(structureFeature.getRegistryName()!=null) {
                            curStruct = structureFeature.getRegistryName().toString().replace("minecraft:", "");
                            if(curStruct.matches(struct)) {
                                good = true;
                            }
                        }
                    }
                }
                PacketHandler.sendTo(new InfoFromStructure(good,struct,curStruct), Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)));
            }
        }
    }

    public static void calculateBiomeAndSend(String biome, BlockPos pos, UUID uuid, String category, String rainType, float temperature, boolean cold) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            ServerLevel world = server.getLevel(Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)).level.dimension());
            if (world != null) {
                Biome curBiome = world.getBiome(pos);
                boolean pass = checkBiome(curBiome,biome,category,rainType,temperature,cold);
                if (pass) {
                    PacketHandler.sendTo(new InfoFromBiome(true,biome, Objects.requireNonNull(curBiome.getRegistryName()).toString()), Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)));
                } else {
                    PacketHandler.sendTo(new InfoFromBiome(false,biome, Objects.requireNonNull(curBiome.getRegistryName()).toString()), Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)));
                }
            }
        }
    }

    public static boolean checkBiome(Biome b, String name, String category, String rainType, float temperature, boolean cold) {
        if(b.getRegistryName().toString().contains(name) || name.matches("minecraft")) {
            if(b.getBiomeCategory().getName().contains(category) || category.matches("nope")) {
                if(b.getPrecipitation().getName().contains(rainType) || rainType.matches("nope")) {
                    float bt = b.getBaseTemperature();
                    if(temperature==-111) return true;
                    else if(bt>=temperature && !cold) return true;
                    else if(bt<=temperature && cold) return true;
                }
            }
        }
        return false;
    }
    public static void calculateMobAndSend(UUID uuid, String mobname, int detectionrange, boolean targetting, int targettingpercentage, int health, int healthpercentage, boolean victory, int victoryID, String i, int num, int persistence, int timeout) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        assert player != null;
        ServerLevel world = server.getLevel(player.level.dimension());
        boolean pass = false;
        assert world != null;
        List<LivingEntity> mobTempList = world.getEntitiesOfClass(LivingEntity.class, new AABB(player.getX() - detectionrange, player.getY() - (detectionrange / 2), player.getZ() - detectionrange, player.getX() + detectionrange, player.getY() + (detectionrange / 2), player.getZ() + detectionrange));
        List<Mob> mobList = new ArrayList<>();
        for (LivingEntity e : mobTempList) {
            if (e instanceof Mob) {
                mobList.add((Mob) e);
            }
        }
        int trackingCounter = 0;
        int healthCounter = 0;
        boolean infernal = true;
        boolean infernalChecked = false;
        boolean infernalDone = false;
        if (mobname.matches("MOB")) {
            for (Iterator<Mob> it = mobList.iterator(); it.hasNext(); ) {
                Mob e = it.next();
                boolean isMonster = true;
                if (e instanceof Animal) {
                    it.remove();
                    isMonster = false;
                }
                if (isMonster) {
                    if (e.getTarget() instanceof Player) {
                        trackingCounter++;
                    }
                    if (e.getHealth() / e.getMaxHealth() <= health / 100F) {
                        healthCounter++;
                    }
                    infernalChecked = infernalChecker(e, i);
                    if (!infernalLoaded || (infernalLoaded && infernalChecked)) {
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
            if (mobList.size() >= num && ((!targetting || (float) trackingCounter / num >= targettingpercentage / 100F) && infernalDone && (float) healthCounter / num >= healthpercentage / 100F)) {
                pass = true;
            }
        } else if (mobname.matches("BOSS")) {
            //Map<UUID, ClientBossInfo> info = ObfuscationReflectionHelper.getPrivateValue(BossOverlayGui.class, mc.gui.getBossOverlay(), "field_184060_g");
            //if (!info.isEmpty()) {
            //pass = true;
            //}
        } else {
            int mobCounter = 0;
            List<Mob> mobListSpecific = new ArrayList<>();
            for (LivingEntity e : mobTempList) {
                if (e.getDisplayName().getString().matches(mobname) || Objects.requireNonNull(e.getType().getRegistryName()).toString().matches(mobname)) {
                    if(e instanceof  Mob) {
                        mobCounter++;
                        mobListSpecific.add((Mob) e);
                    }
                }
            }
            for (Mob e : mobListSpecific) {
                if (e.getTarget() instanceof Player) {
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
            for (Map.Entry<Integer, Map<LivingEntity, Integer>> integerMapEntry : victoryMobs.entrySet()) {
                boolean alldead = true;
                for (Map.Entry<LivingEntity, Integer> entityLivingIntegerEntry : victoryMobs.get((integerMapEntry).getKey()).entrySet()) {
                    if (!entityLivingIntegerEntry.getKey().isDeadOrDying()) {
                        alldead = false;
                        break;
                    }
                }
                dead.put(victoryID, alldead);
            }
        }
        PacketHandler.sendTo(new InfoFromMob(mobname,pass),player);
    }

    private static boolean infernalChecker(LivingEntity m, String s) {
        if (ModList.get().isLoaded("infernalmobs")) {
            infernalLoaded = true;
            if (s == null) {
                return true;
            }
            return InfernalMobsCore.getMobModifiers(m).getModName().matches(s);
        }
        return false;
    }
}
