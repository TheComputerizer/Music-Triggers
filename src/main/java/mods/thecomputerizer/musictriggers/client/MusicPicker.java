package mods.thecomputerizer.musictriggers.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.weather.event.AcidRain;
import corgitaco.betterweather.weather.event.Blizzard;
import corgitaco.betterweather.weather.event.Cloudy;
import corgitaco.betterweather.weather.event.Rain;
import corgitaco.enhancedcelestials.EnhancedCelestialsWorldData;
import corgitaco.enhancedcelestials.LunarContext;
import corgitaco.enhancedcelestials.lunarevent.BloodMoon;
import corgitaco.enhancedcelestials.lunarevent.BlueMoon;
import corgitaco.enhancedcelestials.lunarevent.HarvestMoon;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config;
import mods.thecomputerizer.musictriggers.configRegistry;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.packets.InfoForBiome;
import mods.thecomputerizer.musictriggers.util.packets.InfoForStructure;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ClientBossInfo;
import net.minecraft.client.gui.overlay.BossOverlayGui;
import net.minecraft.client.gui.screen.WinGameScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BossInfo;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.orecruncher.lib.WorldUtils;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

import java.util.*;

public class MusicPicker {
    public static Minecraft mc;
    public static PlayerEntity player;
    public static World world;

    public static HashMap<String, Integer> lightPersistence = new HashMap<>();
    public static HashMap<String, Integer> persistentMob = new HashMap<>();
    public static HashMap<String, Integer> persistentBiome = new HashMap<>();
    public static HashMap<Integer, Integer> persistentVictory = new HashMap<>();
    public static HashMap<Integer, List<LivingEntity>> victoryMobs = new HashMap<>();
    public static HashMap<Integer, PlayerEntity> victoryPlayer = new HashMap<>();
    public static HashMap<Integer, Boolean> victory = new HashMap<>();
    public static int persistentPVP = 0;
    public static int victoryID = 0;
    public static boolean fishBool = false;
    public static int fishingStart = 0;
    public static int persistentFishing = 0;
    public static boolean setPVP = false;
    public static PlayerEntity otherPVP;
    public static boolean infernalLoaded = false;

    public static HashMap<String, List<String>> dynamicSongs = new HashMap<>();
    public static HashMap<String, Integer> dynamicPriorities = new HashMap<>();
    public static HashMap<String, Integer> dynamicFade = new HashMap<>();

    public static List<String> playableList = new ArrayList<>();
    public static List<String> titleCardEvents = new ArrayList<>();

    public static List<String> effectList = new ArrayList<>();

    public static int curFade = 0;
    public static boolean shouldChange = false;

    public static float musicVolSave;
    public static float masterVolSave;

    public static List<String> playThese() {
        if (!MusicPlayer.fading) {
            titleCardEvents = new ArrayList<>();
        }
        mc = Minecraft.getInstance();
        player = mc.player;
        if (player != null) {
            world = player.getCommandSenderWorld();
        }
        if (player == null) {
            return config.menuSongs;
        }
        List<String> res = comboChecker(priorityHandler(playableEvents()));
        if (res != null && !res.isEmpty()) {
            dynamicSongs = new HashMap<>();
            dynamicPriorities = new HashMap<>();
            dynamicFade = new HashMap<>();
            return res;
        }
        dynamicSongs = new HashMap<>();
        dynamicPriorities = new HashMap<>();
        dynamicFade = new HashMap<>();
        curFade = config.genericFade;
        if(!config.genericSongs.isEmpty()) {
            playableList.add("generic");
        }
        return config.genericSongs;
    }

    @SuppressWarnings("rawtypes")
    public static List<String> comboChecker(String st) {
        if (st == null) {
            return null;
        }
        List<String> playableSongs = new ArrayList<>();
        for (String s : dynamicSongs.get(st)) {
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.songCombos.entrySet()) {
                String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                if (s.matches(checkThis)) {
                    if (playableList.containsAll(SoundHandler.songCombos.get(s)) && SoundHandler.songCombos.get(s).size() != 1) {
                        playableSongs.add(s.substring(1));
                        if (!titleCardEvents.contains(st)) {
                            titleCardEvents.addAll(SoundHandler.songCombos.get(s));
                        }
                    }
                }
            }
        }
        if (playableSongs.isEmpty()) {
            for (String s : dynamicSongs.get(st)) {
                if (!s.startsWith("@")) {
                    playableSongs.add(s);
                    if (!titleCardEvents.contains(st)) {
                        titleCardEvents.add(st);
                    }
                }
            }
        }
        if (playableSongs.isEmpty()) {
            List<String> tryAgain = playableList;
            tryAgain.remove(st);
            playableList = tryAgain;
            if (playableList.isEmpty()) {
                return null;
            }
            playableSongs = comboChecker(priorityHandler(playableList));
        }
        return playableSongs;
    }

    public static String priorityHandler(List<String> sta) {
        if (sta == null) {
            return null;
        }
        int highest = -100;
        String trueHighest = "";
        for (String list : sta) {
            if (dynamicPriorities.get(list) > highest && !dynamicSongs.get(list).isEmpty()) {
                highest = dynamicPriorities.get(list);
                trueHighest = list;
            }
        }
        while (dynamicSongs.get(trueHighest) == null) {
            sta.remove(trueHighest);
            if (sta.isEmpty()) {
                return null;
            }
            for (String list : sta) {
                if (dynamicPriorities.get(list) > highest) {
                    highest = dynamicPriorities.get(list);
                    trueHighest = list;
                }
            }
        }
        if (dynamicFade != null && !dynamicFade.isEmpty()) {
            if (dynamicFade.get(trueHighest) != null) {
                curFade = dynamicFade.get(trueHighest);
            } else {
                curFade = 0;
            }
        }
        return trueHighest;
    }

    @SuppressWarnings({"rawtypes", "ConstantConditions"})
    public static List<String> playableEvents() {
        List<String> events = new ArrayList<>();
        double time = (double) world.dayTime() / 24000.0;
        if (time > 1) {
            time = time - (long) time;
        }
        if (time < 0.54166666666) {
            events.add("day");
            dynamicSongs.put("day", config.daySongs);
            dynamicPriorities.put("day", config.dayPriority);
            dynamicFade.put("day", config.dayFade);
        } else {
            if (SoundHandler.nightSongs.get(0) != null) {
                events.add("night" + 0);
                List<String> tempNight = new ArrayList<>();
                if (SoundHandler.nightSongsString.get((world.getMoonPhase() + 1)) != null && !SoundHandler.nightSongsString.get((world.getMoonPhase() + 1)).isEmpty()) {
                    tempNight.addAll(SoundHandler.nightSongsString.get((world.getMoonPhase() + 1)));
                }
                tempNight.addAll(SoundHandler.nightSongsString.get(0));
                dynamicSongs.put("night" + 0, tempNight);
                dynamicPriorities.put("night" + 0, config.nightPriority);
                dynamicFade.put("night" + 0, SoundHandler.nightFade.get(0));
            } else {
                if (SoundHandler.nightSongs.get((world.getMoonPhase() + 1)) != null) {
                    events.add("night" + (world.getMoonPhase() + 1));
                    dynamicSongs.put("night" + (world.getMoonPhase() + 1), SoundHandler.nightSongsString.get((world.getMoonPhase() + 1)));
                    dynamicPriorities.put("night" + (world.getMoonPhase() + 1), config.nightPriority);
                    dynamicFade.put("night" + (world.getMoonPhase() + 1), SoundHandler.nightFade.get((world.getMoonPhase() + 1)));
                }
            }
        }
        if (time < 0.54166666666 && time >= 0.5) {
            events.add("sunset");
            dynamicSongs.put("sunset", config.sunsetSongs);
            dynamicPriorities.put("sunset", config.sunsetPriority);
            dynamicFade.put("sunset", config.sunsetFade);
        } else if (time >= 0.95833333333 && time < 1) {
            events.add("sunrise");
            dynamicSongs.put("sunrise", config.sunriseSongs);
            dynamicPriorities.put("sunrise", config.sunrisePriority);
            dynamicFade.put("sunrise", config.sunriseFade);
        }
        for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.lightSongsString.entrySet()) {
            String lightName = ((Map.Entry) stringListEntry).getKey().toString();
            if (averageLight(roundedPos(player), SoundHandler.lightSky.get(lightName)) <= SoundHandler.lightLevel.get(lightName)) {
                events.add("light");
                dynamicSongs.put("light", config.lightSongs);
                dynamicPriorities.put("light", config.lightPriority);
                dynamicFade.put("light", config.lightFade);
                lightPersistence.put(lightName, SoundHandler.lightTime.get(lightName));
            } else if (lightPersistence.get(lightName) > 0) {
                events.add("light");
                dynamicSongs.put("light", config.lightSongs);
                dynamicPriorities.put("light", config.lightPriority);
                dynamicFade.put("light", config.lightFade);
            }
        }
        if (player.getY() < config.deepUnderLevel && !world.canSeeSky(roundedPos(player))) {
            events.add("deepUnder");
            dynamicSongs.put("deepUnder", config.deepUnderSongs);
            dynamicPriorities.put("deepUnder", config.deepUnderPriority);
            dynamicFade.put("deepUnder", config.deepUnderFade);
        }
        if (player.getY() < config.undergroundLevel && !world.canSeeSky(roundedPos(player))) {
            events.add("underground");
            dynamicSongs.put("underground", config.undergroundSongs);
            dynamicPriorities.put("underground", config.undergroundPriority);
            dynamicFade.put("underground", config.undergroundFade);
        }
        if (player.getY() < config.inVoidLevel) {
            events.add("inVoid");
            dynamicSongs.put("inVoid", config.inVoidSongs);
            dynamicPriorities.put("inVoid", config.inVoidPriority);
            dynamicFade.put("inVoid", config.inVoidFade);
        }
        if (player.getY() >= config.highLevel) {
            events.add("high");
            dynamicSongs.put("high", config.highSongs);
            dynamicPriorities.put("high", config.highPriority);
            dynamicFade.put("high", config.highFade);
        }
        if (player.getFallFlyingTicks()>=config.elytraStart) {
            events.add("elytra");
            dynamicSongs.put("elytra", config.elytraSongs);
            dynamicPriorities.put("elytra", config.elytraPriority);
            dynamicFade.put("elytra", config.elytraFade);
        }
        if(player.fishing!=null) {
            fishBool = true;
        }
        else {
            fishingStart=0;
        }
        if(fishingStart>config.fishingStart) {
            events.add("fishing");
            dynamicSongs.put("fishing", config.fishingSongs);
            dynamicPriorities.put("fishing", config.fishingPriority);
            dynamicFade.put("fishing", config.fishingFade);
            persistentFishing = config.fishingPersistence;
        }
        else if(persistentFishing>0) {
            events.add("fishing");
            dynamicSongs.put("fishing", config.fishingSongs);
            dynamicPriorities.put("fishing", config.fishingPriority);
            dynamicFade.put("fishing", config.fishingFade);
        }
        if (world.isRaining()) {
            events.add("raining");
            dynamicSongs.put("raining", config.rainingSongs);
            dynamicPriorities.put("raining", config.rainingPriority);
            dynamicFade.put("raining", config.rainingFade);
            if (world.getBiome(player.blockPosition()).shouldSnow(world, player.blockPosition())) {
                events.add("snowing");
                dynamicSongs.put("snowing", config.snowingSongs);
                dynamicPriorities.put("snowing", config.snowingPriority);
                dynamicFade.put("snowing", config.snowingFade);
            }
        }
        if (world.isThundering()) {
            events.add("storming");
            dynamicSongs.put("storming", config.stormingSongs);
            dynamicPriorities.put("storming", config.stormingPriority);
            dynamicFade.put("storming", config.stormingFade);
        }
        if (player.getHealth() < player.getMaxHealth() * ((float) config.lowHPLevel / 100F)) {
            events.add("lowHP");
            dynamicSongs.put("lowHP", config.lowHPSongs);
            dynamicPriorities.put("lowHP", config.lowHPPriority);
            dynamicFade.put("lowHP", config.lowHPFade);
        }
        if (player.isDeadOrDying()) {
            events.add("dead");
            dynamicSongs.put("dead", config.deadSongs);
            dynamicPriorities.put("dead", config.deadPriority);
            dynamicFade.put("dead", config.deadFade);
            for (Map.Entry<Integer, Boolean> integerListEntry : victory.entrySet()) {
                int key = integerListEntry.getKey();
                victory.put(key, false);
            }
        }
        if (player.isSpectator()) {
            events.add("spectator");
            dynamicSongs.put("spectator", config.spectatorSongs);
            dynamicPriorities.put("spectator", config.spectatorPriority);
            dynamicFade.put("spectator", config.spectatorFade);
        }
        if (player.isCreative()) {
            events.add("creative");
            dynamicSongs.put("creative", config.creativeSongs);
            dynamicPriorities.put("creative", config.creativePriority);
            dynamicFade.put("creative", config.creativeFade);
        }
        if (player.isPassenger()) {
            events.add("riding");
            dynamicSongs.put("riding", config.ridingSongs);
            dynamicPriorities.put("riding", config.ridingPriority);
            dynamicFade.put("riding", config.ridingFade);
        }
        if ((world.getBlockState(roundedPos(player)).getMaterial() == Material.WATER || world.getBlockState(roundedPos(player)).getMaterial() == Material.WATER_PLANT || world.getBlockState(roundedPos(player)).getMaterial() == Material.REPLACEABLE_WATER_PLANT) && (world.getBlockState(roundedPos(player).above()).getMaterial() == Material.WATER || world.getBlockState(roundedPos(player).above()).getMaterial() == Material.WATER_PLANT || world.getBlockState(roundedPos(player).above()).getMaterial() == Material.REPLACEABLE_WATER_PLANT)) {
            events.add("underwater");
            dynamicSongs.put("underwater", config.underwaterSongs);
            dynamicPriorities.put("underwater", config.underwaterPriority);
            dynamicFade.put("underwater", config.underwaterFade);
        }
        for (LivingEntity ent : world.getEntitiesOfClass(LivingEntity.class, new AxisAlignedBB(player.getX() - 16, player.getY() - 8, player.getZ() - 16, player.getX() + 16, player.getY() + 8, player.getZ() + 16))) {
            if (ent instanceof TameableEntity && ent.serializeNBT()!=null && ent.serializeNBT().getString("Owner").matches(player.getStringUUID())) {
                events.add("pet");
                dynamicSongs.put("pet", config.petSongs);
                dynamicPriorities.put("pet", config.petPriority);
                dynamicFade.put("pet", config.petFade);
                break;
            }
        }
        if (SoundHandler.dimensionSongs.get(player.level.dimension().location().toString()) != null) {
            events.add("dimension" + player.level.dimension().location());
            dynamicSongs.put("dimension" + player.level.dimension().location(), SoundHandler.dimensionSongsString.get(player.level.dimension().location().toString()));
            dynamicPriorities.put("dimension" + player.level.dimension().location(), SoundHandler.dimensionPriorities.get(player.level.dimension().location().toString()));
            dynamicFade.put("dimension" + player.level.dimension().location(), SoundHandler.dimensionFade.get(player.level.dimension().location().toString()));
        }
        if (world.getBiome(roundedPos(player)).getRegistryName() != null) {
            fromServer.curBiome = world.getBiome(roundedPos(player)).getRegistryName().toString();
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.biomeSongsString.entrySet()) {
                String biomeRegex = ((Map.Entry) stringListEntry).getKey().toString();
                if (Objects.requireNonNull(world.getBiome(roundedPos(player)).getRegistryName()).toString().contains(biomeRegex)) {
                    events.add(biomeRegex);
                    dynamicSongs.put(biomeRegex, SoundHandler.biomeSongsString.get(biomeRegex));
                    dynamicPriorities.put(biomeRegex, SoundHandler.biomePriorities.get(biomeRegex));
                    dynamicFade.put(biomeRegex, SoundHandler.biomeFade.get(biomeRegex));
                    persistentBiome.put(biomeRegex, SoundHandler.biomePersistence.get(biomeRegex));
                } else if (persistentBiome.get(biomeRegex) > 0) {
                    events.add(biomeRegex);
                    dynamicSongs.put(biomeRegex, SoundHandler.biomeSongsString.get(biomeRegex));
                    dynamicPriorities.put(biomeRegex, SoundHandler.biomePriorities.get(biomeRegex));
                    dynamicFade.put(biomeRegex, SoundHandler.biomeFade.get(biomeRegex));
                }
            }
        } else if (!configRegistry.clientSideOnly) {
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.biomeSongsString.entrySet()) {
                String biomeRegex = ((Map.Entry) stringListEntry).getKey().toString();
                PacketHandler.sendToServer(new InfoForBiome(biomeRegex, roundedPos(player), player.getUUID()));
                if (fromServer.inBiome.containsKey(biomeRegex)) {
                    if (fromServer.inBiome.get(biomeRegex)) {
                        events.add(biomeRegex);
                        dynamicSongs.put(biomeRegex, SoundHandler.biomeSongsString.get(biomeRegex));
                        dynamicPriorities.put(biomeRegex, SoundHandler.biomePriorities.get(biomeRegex));
                        dynamicFade.put(biomeRegex, SoundHandler.biomeFade.get(biomeRegex));
                        persistentBiome.put(biomeRegex, SoundHandler.biomePersistence.get(biomeRegex));
                    } else if (persistentBiome.get(biomeRegex) > 0) {
                        events.add(biomeRegex);
                        dynamicSongs.put(biomeRegex, SoundHandler.biomeSongsString.get(biomeRegex));
                        dynamicPriorities.put(biomeRegex, SoundHandler.biomePriorities.get(biomeRegex));
                        dynamicFade.put(biomeRegex, SoundHandler.biomeFade.get(biomeRegex));
                    }
                }
            }
        }
        if (!configRegistry.clientSideOnly) {
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.structureSongsString.entrySet()) {
                String structName = ((Map.Entry) stringListEntry).getKey().toString();
                PacketHandler.sendToServer(new InfoForStructure(structName, player.blockPosition(), player.getUUID()));
                if (fromServer.inStructure.containsKey(structName)) {
                    if (fromServer.inStructure.get(structName)) {
                        events.add("structure:" + structName);
                        dynamicSongs.put("structure:" + structName, SoundHandler.structureSongsString.get(structName));
                        dynamicPriorities.put("structure:" + structName, SoundHandler.structurePriorities.get(structName));
                        dynamicFade.put("structure:" + structName, SoundHandler.structureFade.get(structName));
                    }
                }
            }
        }
        for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.mobSongsString.entrySet()) {
            String mobName = ((Map.Entry) stringListEntry).getKey().toString();
            double range = SoundHandler.mobRange.get(mobName);
            List<LivingEntity> mobTempList = world.getEntitiesOfClass(LivingEntity.class, new AxisAlignedBB(player.getX() - range, player.getY() - (range / 2), player.getZ() - range, player.getX() + range, player.getY() + (range / 2), player.getZ() + range));
            List<MobEntity> mobList = new ArrayList<>();
            for (LivingEntity e : mobTempList) {
                if (e instanceof MobEntity) {
                    mobList.add((MobEntity) e);
                }
            }
            int trackingCounter = 0;
            int healthCounter = 0;
            boolean infernal = true;
            boolean infernalChecked = false;
            boolean infernalDone = false;
            if (mobName.matches("MOB")) {
                for (Iterator<MobEntity> it = mobList.iterator(); it.hasNext(); ) {
                    MobEntity e = it.next();
                    boolean isMonster = true;
                    if (e instanceof AnimalEntity) {
                        it.remove();
                        isMonster = false;
                    }
                    if (isMonster) {
                        if (player.canSee(e)) {
                            trackingCounter++;
                        }
                        if (e.getHealth() / e.getMaxHealth() <= SoundHandler.mobHealth.get(mobName) / 100F) {
                            healthCounter++;
                        }
                        infernalChecked = infernalChecker(e, SoundHandler.mobInfernalMod.get(mobName));
                        if (!infernalLoaded || (infernalLoaded && infernalChecked)) {
                            infernalDone = true;
                        }
                        if (SoundHandler.mobVictory.get(mobName)) {
                            victoryID = SoundHandler.mobVictoryID.get(mobName);
                            victoryMobs.computeIfAbsent(victoryID, k -> new ArrayList<>());
                            if (!victoryMobs.get(victoryID).contains(e) && victoryMobs.get(victoryID).size() < SoundHandler.mobNumber.get(mobName)) {
                                victoryMobs.get(victoryID).add(e);
                            }
                        }
                    }
                }
                if (mobList.size() >= SoundHandler.mobNumber.get(mobName) && (((SoundHandler.mobTargetting.get(mobName) && (float) trackingCounter / SoundHandler.mobNumber.get(mobName) >= SoundHandler.mobHordeTargetting.get(mobName) / 100F) || !SoundHandler.mobTargetting.get(mobName)) && infernalDone && (float) healthCounter / SoundHandler.mobNumber.get(mobName) >= SoundHandler.mobHordeHealth.get(mobName) / 100F)) {
                    events.add(mobName);
                    dynamicSongs.put(mobName, SoundHandler.mobSongsString.get(mobName));
                    dynamicPriorities.put(mobName, SoundHandler.mobPriorities.get(mobName));
                    dynamicFade.put(mobName, SoundHandler.mobFade.get(mobName));
                    persistentMob.put(mobName, SoundHandler.mobBattle.get(mobName));
                    victory.put(victoryID, SoundHandler.mobVictory.get(mobName));
                }
            } else if (mobName.matches("BOSS")) {
                mc.gui.getBossOverlay();
                Map<UUID, ClientBossInfo> info = ObfuscationReflectionHelper.getPrivateValue(BossOverlayGui.class, mc.gui.getBossOverlay(), "field_184060_g");
                if (!info.isEmpty()) {
                    events.add(mobName);
                    dynamicSongs.put(mobName, SoundHandler.mobSongsString.get(mobName));
                    dynamicPriorities.put(mobName, SoundHandler.mobPriorities.get(mobName));
                    dynamicFade.put(mobName, SoundHandler.mobFade.get(mobName));
                    persistentMob.put(mobName, SoundHandler.mobBattle.get(mobName));
                    victory.put(victoryID, SoundHandler.mobVictory.get(mobName));
                }
            } else {
                int mobCounter = 0;
                List<MobEntity> mobListSpecific = new ArrayList<>();
                for (LivingEntity e : mobTempList) {
                    if (e.getDisplayName().getString().matches(mobName) || e.getType().getRegistryName().toString().matches(mobName)) {
                        if(e instanceof  MobEntity) {
                            mobCounter++;
                            mobListSpecific.add((MobEntity) e);
                        }
                    }
                }
                for (MobEntity e : mobListSpecific) {
                    if (player.canSee(e)) {
                        trackingCounter++;
                    }
                    if (e.getHealth() / e.getMaxHealth() <= SoundHandler.mobHealth.get(mobName) / 100F) {
                        healthCounter++;
                    }
                    try {
                        infernalChecked = infernalChecker(e, SoundHandler.mobInfernalMod.get(mobName));
                    } catch (NoSuchMethodError ignored) {
                        infernal = false;
                    }
                    if (!infernal || (infernal && infernalChecked)) {
                        infernalDone = true;
                    }
                    if (SoundHandler.mobVictory.get(mobName) && (((SoundHandler.mobTargetting.get(mobName) && (float) trackingCounter / SoundHandler.mobNumber.get(mobName) >= SoundHandler.mobHordeTargetting.get(mobName) / 100F) || !SoundHandler.mobTargetting.get(mobName)) && infernalDone && (float) healthCounter / SoundHandler.mobNumber.get(mobName) >= SoundHandler.mobHordeHealth.get(mobName) / 100F)) {
                        victoryID = SoundHandler.mobVictoryID.get(mobName);
                        victoryMobs.computeIfAbsent(victoryID, k -> new ArrayList<>());
                        if (!victoryMobs.get(victoryID).contains(e) && victoryMobs.get(victoryID).size() < SoundHandler.mobNumber.get(mobName)) {
                            victoryMobs.get(victoryID).add(e);
                        }
                    }
                }
                if (mobCounter >= SoundHandler.mobNumber.get(mobName)) {
                    events.add(mobName);
                    dynamicSongs.put(mobName, SoundHandler.mobSongsString.get(mobName));
                    dynamicPriorities.put(mobName, SoundHandler.mobPriorities.get(mobName));
                    dynamicFade.put(mobName, SoundHandler.mobFade.get(mobName));
                    persistentMob.put(mobName, SoundHandler.mobBattle.get(mobName));
                    victory.put(victoryID, SoundHandler.mobVictory.get(mobName));
                }
            }
            if (persistentMob.get(mobName) > 0) {
                if (!events.contains(mobName)) {
                    events.add(mobName);
                    dynamicSongs.put(mobName, SoundHandler.mobSongsString.get(mobName));
                    dynamicPriorities.put(mobName, SoundHandler.mobPriorities.get(mobName));
                    dynamicFade.put(mobName, SoundHandler.mobFade.get(mobName));
                }
            } else {
                victory.put(victoryID, SoundHandler.mobVictory.get(mobName));
            }
        }
        if (!SoundHandler.zonesSongs.isEmpty()) {
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.zonesSongsString.entrySet()) {
                String zoneRange = ((Map.Entry) stringListEntry).getKey().toString();
                String[] broken = SoundHandler.stringBreaker(zoneRange,",");
                BlockPos bp = player.blockPosition();
                int x1 = Integer.parseInt(broken[0]);
                int y1 = Integer.parseInt(broken[1]);
                int z1 = Integer.parseInt(broken[2]);
                int x2 = Integer.parseInt(broken[3]);
                int y2 = Integer.parseInt(broken[4]);
                int z2 = Integer.parseInt(broken[5]);
                if (bp.getX() > x1 && bp.getX() < x2 && bp.getY() > y1 && bp.getY() < y2 && bp.getZ() > z1 && bp.getZ() < z2) {
                    events.add(zoneRange);
                    String[] zonesSongsArray = new String[SoundHandler.zonesSongsString.get(zoneRange).size()];
                    dynamicSongs.put(zoneRange, Arrays.asList(SoundHandler.zonesSongsString.get(zoneRange).toArray(zonesSongsArray)));
                    dynamicPriorities.put(zoneRange, SoundHandler.zonesPriorities.get(zoneRange));
                    dynamicFade.put(zoneRange, SoundHandler.zonesFade.get(zoneRange));
                }
            }
        }
        if (!SoundHandler.effectSongs.isEmpty()) {
            effectList = new ArrayList<>();
            for (Effect p : player.getActiveEffectsMap().keySet()) {
                effectList.add(p.getRegistryName().toString());
                if (SoundHandler.effectSongsString.containsKey(p.getRegistryName().toString())) {
                    events.add(p.getRegistryName().toString());
                    dynamicSongs.put(p.getRegistryName().toString(), SoundHandler.effectSongsString.get(p.getRegistryName().toString()));
                    dynamicPriorities.put(p.getRegistryName().toString(), SoundHandler.effectPriorities.get(p.getRegistryName().toString()));
                    dynamicFade.put(p.getRegistryName().toString(), SoundHandler.effectFade.get(p.getRegistryName().toString()));
                }
            }
        }
        if (config.pvpSongs.size() != 0) {
            if (eventsClient.playerSource.getUUID() == player.getUUID()) {
                otherPVP = eventsClient.playerHurt;
                setPVP = true;
            } else if (eventsClient.playerHurt.getUUID() == player.getUUID()) {
                otherPVP = eventsClient.playerSource;
                setPVP = true;
            }
            if (setPVP && player.distanceTo(otherPVP) <= config.pvpRange) {
                events.add("PVP");
                dynamicSongs.put("PVP", config.pvpSongs);
                dynamicPriorities.put("PVP", config.pvpPriority);
                dynamicFade.put("PVP", config.pvpFade);
                persistentPVP = config.pvpTime;
                victoryID = config.pvpVictoryID;
                if (config.pvpVictory) {
                    victoryPlayer.put(victoryID, otherPVP);
                    victory.put(victoryID, config.pvpVictory);
                }
            } else if (persistentPVP > 0) {
                events.add("PVP");
                dynamicSongs.put("PVP", config.pvpSongs);
                dynamicPriorities.put("PVP", config.pvpPriority);
                dynamicFade.put("PVP", config.pvpFade);
                victoryID = config.pvpVictoryID;

            } else {
                setPVP = false;
            }
        }
        persistentVictory.putIfAbsent(victoryID, 0);
        victory.putIfAbsent(victoryID, false);
        if (victory.get(victoryID)) {
            boolean victoryTempM = true;
            boolean victoryTempP = true;
            if (victoryMobs.get(victoryID) != null && !victoryMobs.get(victoryID).isEmpty()) {
                for (LivingEntity e : victoryMobs.get(victoryID)) {
                    if (!e.isDeadOrDying()) {
                        victoryTempM = false;
                        break;
                    }
                }
            } else {
                victoryTempM = false;
            }
            if ((victoryPlayer.get(victoryID) != null && !victoryPlayer.get(victoryID).isDeadOrDying()) || victoryPlayer.get(victoryID) == null) {
                victoryTempP = false;
            }
            if (victoryTempM || victoryTempP) {
                persistentVictory.put(victoryID, SoundHandler.victoryTime.get("Victory" + victoryID));
            }
        }
        if (!SoundHandler.victorySongsString.isEmpty() && SoundHandler.victorySongsString.get("Victory" + victoryID) != null && persistentVictory.get(victoryID) > 0) {
            for (Map.Entry<Integer, Boolean> integerListEntry : victory.entrySet()) {
                int key = integerListEntry.getKey();
                victory.put(key, false);
                victoryMobs.put(key, new ArrayList<>());
                victoryPlayer.put(key, null);
            }
            events.add("Victory" + victoryID);
            dynamicSongs.put("Victory" + victoryID, SoundHandler.victorySongsString.get("Victory" + victoryID));
            dynamicPriorities.put("Victory" + victoryID, SoundHandler.victoryPriorities.get("Victory" + victoryID));
            dynamicFade.put("Victory" + victoryID, SoundHandler.victoryFade.get("Victory" + victoryID));
        }
        if(mc.screen!=null) {
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.guiSongsString.entrySet()) {
                String guiName = ((Map.Entry) stringListEntry).getKey().toString();
                if(mc.screen.toString().contains(guiName)) {
                    events.add(guiName);
                    dynamicSongs.put(guiName, SoundHandler.guiSongsString.get(guiName));
                    dynamicPriorities.put(guiName, SoundHandler.guiPriorities.get(guiName));
                    dynamicFade.put(guiName, SoundHandler.guiFade.get(guiName));
                }
                else if(guiName.matches("CREDITS") && mc.screen instanceof WinGameScreen) {
                    events.add(guiName);
                    dynamicSongs.put(guiName, SoundHandler.guiSongsString.get(guiName));
                    dynamicPriorities.put(guiName, SoundHandler.guiPriorities.get(guiName));
                    dynamicFade.put(guiName, SoundHandler.guiFade.get(guiName));
                }
            }
        }
        else {
            musicVolSave = mc.options.getSoundSourceVolume(SoundCategory.MUSIC);
            masterVolSave = mc.options.getSoundSourceVolume(SoundCategory.MASTER);
        }
        if(!SoundHandler.difficultySongsString.isEmpty()) {
            for (Map.Entry<Integer, List<String>> intListEntry : SoundHandler.difficultySongsString.entrySet()) {
                int diffID = intListEntry.getKey();
                if (diffID == 4 && world.getLevelData().isHardcore()) {
                    events.add("difficulty:"+diffID);
                    dynamicSongs.put("difficulty:"+diffID, SoundHandler.difficultySongsString.get(diffID));
                    dynamicPriorities.put("difficulty:"+diffID, SoundHandler.difficultyPriorities.get(diffID));
                    dynamicFade.put("difficulty:"+diffID, SoundHandler.difficultyFade.get(diffID));
                } else if (diffID == 3 && mc.level.getDifficulty() == Difficulty.HARD) {
                    events.add("difficulty:"+diffID);
                    dynamicSongs.put("difficulty:"+diffID, SoundHandler.difficultySongsString.get(diffID));
                    dynamicPriorities.put("difficulty:"+diffID, SoundHandler.difficultyPriorities.get(diffID));
                    dynamicFade.put("difficulty:"+diffID, SoundHandler.difficultyFade.get(diffID));
                } else if (diffID == 2 && mc.level.getDifficulty() == Difficulty.NORMAL) {
                    events.add("difficulty:"+diffID);
                    dynamicSongs.put("difficulty:"+diffID, SoundHandler.difficultySongsString.get(diffID));
                    dynamicPriorities.put("difficulty:"+diffID, SoundHandler.difficultyPriorities.get(diffID));
                    dynamicFade.put("difficulty:"+diffID, SoundHandler.difficultyFade.get(diffID));
                } else if (diffID == 1 && mc.level.getDifficulty() == Difficulty.EASY) {
                    events.add("difficulty:"+diffID);
                    dynamicSongs.put("difficulty:"+diffID, SoundHandler.difficultySongsString.get(diffID));
                    dynamicPriorities.put("difficulty:"+diffID, SoundHandler.difficultyPriorities.get(diffID));
                    dynamicFade.put("difficulty:"+diffID, SoundHandler.difficultyFade.get(diffID));
                } else if (diffID == 0 && mc.level.getDifficulty() == Difficulty.PEACEFUL) {
                    events.add("difficulty:"+diffID);
                    dynamicSongs.put("difficulty:"+diffID, SoundHandler.difficultySongsString.get(diffID));
                    dynamicPriorities.put("difficulty:"+diffID, SoundHandler.difficultyPriorities.get(diffID));
                    dynamicFade.put("difficulty:"+diffID, SoundHandler.difficultyFade.get(diffID));
                }
            }
        }
        List<String> whitelist = stageWhitelistChecker();
        List<String> blacklist = stageBlacklistChecker();
        if (whitelist != null && !whitelist.isEmpty()) {
            events.addAll(whitelist);
        }
        if (blacklist != null && !blacklist.isEmpty()) {
            events.addAll(blacklist);
        }
        boolean bloodmoon = bloodmoon();
        if (bloodmoon) {
            events.add("bloodmoon");
        }
        boolean harvestmoon = harvestmoon();
        if (harvestmoon) {
            events.add("harvestmoon");
        }
        boolean bluemoon = bluemoon();
        if (bluemoon) {
            events.add("bluemoon");
        }

        String dynamicrain = dynamicrain();
        if (dynamicrain != null) {
            events.add(dynamicrain);
        }

        boolean acidrain = acidrain();
        if (acidrain) {
            events.add("acidrain");
        }
        boolean blizzard = blizzard();
        if (blizzard) {
            events.add("blizzard");
        }
        boolean cloudy = cloudy();
        if (cloudy) {
            events.add("cloudy");
        }
        boolean lightrain = lightrain();
        if (lightrain) {
            events.add("lightrain");
        }
        List<String> seasons = seasons();
        if(!seasons.isEmpty()) {
            events.addAll(seasons);
        }

        playableList = events;
        return events;
    }

    @SuppressWarnings("rawtypes")
    private static List<String> stageWhitelistChecker() {
        if (ModList.get().isLoaded("gamestages")) {
            List<String> events = new ArrayList<>();
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.gamestageSongsStringWhitelist.entrySet()) {
                String stageName = ((Map.Entry) stringListEntry).getKey().toString();
                String temp = stageName;
                if (temp.startsWith("@")) {
                    temp = temp.substring(1);
                }
                if (GameStageHelper.hasStage(player, temp)) {
                    events.add(stageName + "true");
                    String[] gamestageSongsArray = new String[SoundHandler.gamestageSongsStringWhitelist.get(stageName).size()];
                    dynamicSongs.put(stageName + "true", Arrays.asList(SoundHandler.gamestageSongsStringWhitelist.get(stageName).toArray(gamestageSongsArray)));
                    dynamicPriorities.put(stageName + "true", SoundHandler.gamestagePrioritiesWhitelist.get(stageName));
                    dynamicFade.put(stageName + "true", SoundHandler.gamestageFadeWhitelist.get(stageName));
                }
            }
            return events;
        } else {
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    private static List<String> stageBlacklistChecker() {
        if (ModList.get().isLoaded("gamestages")) {
            List<String> events = new ArrayList<>();
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.gamestageSongsStringBlacklist.entrySet()) {
                String stageName = ((Map.Entry) stringListEntry).getKey().toString();
                String temp = stageName;
                if (temp.startsWith("@")) {
                    temp = temp.substring(1);
                }
                if (!GameStageHelper.hasStage(player, temp)) {
                    events.add(stageName + "false");
                    String[] gamestageSongsArray = new String[SoundHandler.gamestageSongsStringBlacklist.get(stageName).size()];
                    dynamicSongs.put(stageName + "false", Arrays.asList(SoundHandler.gamestageSongsStringBlacklist.get(stageName).toArray(gamestageSongsArray)));
                    dynamicPriorities.put(stageName + "false", SoundHandler.gamestagePrioritiesBlacklist.get(stageName));
                    dynamicFade.put(stageName + "true", SoundHandler.gamestageFadeBlacklist.get(stageName));
                }
            }
            return events;
        } else {
            return null;
        }
    }

    private static boolean bloodmoon() {
        if (ModList.get().isLoaded("enhancedcelestials")) {
            LunarContext lunarContext = ((EnhancedCelestialsWorldData) world).getLunarContext();
            if (lunarContext != null && lunarContext.getCurrentEvent() instanceof BloodMoon) {
                dynamicSongs.put("bloodmoon", config.bloodmoonSongs);
                dynamicPriorities.put("bloodmoon", config.bloodmoonPriority);
                dynamicFade.put("bloodmoon", config.bloodmoonFade);
                return true;
            }
        }
        return false;
    }

    private static boolean harvestmoon() {
        if (ModList.get().isLoaded("enhancedcelestials")) {
            LunarContext lunarContext = ((EnhancedCelestialsWorldData) world).getLunarContext();
            if (lunarContext != null && lunarContext.getCurrentEvent() instanceof HarvestMoon) {
                dynamicSongs.put("harvestmoon", config.harvestmoonSongs);
                dynamicPriorities.put("harvestmoon", config.harvestmoonPriority);
                dynamicFade.put("harvestmoon", config.harvestmoonFade);
                return true;
            }
        }
        return false;
    }

    private static boolean bluemoon() {
        if (ModList.get().isLoaded("enhancedcelestials")) {
            LunarContext lunarContext = ((EnhancedCelestialsWorldData) world).getLunarContext();
            if (lunarContext != null && lunarContext.getCurrentEvent() instanceof BlueMoon) {
                dynamicSongs.put("bluemoon", config.bluemoonSongs);
                dynamicPriorities.put("bluemoon", config.bluemoonPriority);
                dynamicFade.put("bluemoon", config.bluemoonFade);
                return true;
            }
        }
        return false;
    }

    private static String dynamicrain() {
        if (ModList.get().isLoaded("dsurround")) {
            for (Map.Entry<Integer, List<String>> integerListEntry : SoundHandler.rainintensitySongsString.entrySet()) {
                int intensity = integerListEntry.getKey();
                if (WorldUtils.getRainStrength(world, 1F) > (float) intensity / 100F) {
                    dynamicSongs.put("Rain Intensity" + intensity, SoundHandler.rainintensitySongsString.get(intensity));
                    dynamicPriorities.put("Rain Intensity" + intensity, config.rainintensityPriority);
                    dynamicFade.put("Rain Intensity" + intensity, config.rainintensityFade);
                    return intensity + "";
                }
            }
        }
        return null;
    }

    private static boolean acidrain() {
        if (ModList.get().isLoaded("betterweather")) {
            BetterWeatherWorldData weatherdata = (BetterWeatherWorldData) world;
            if (weatherdata.getWeatherEventContext() != null && weatherdata.getWeatherEventContext().getCurrentEvent() instanceof AcidRain) {
                dynamicSongs.put("acidrain", config.acidrainSongs);
                dynamicPriorities.put("acidrain", config.acidrainPriority);
                dynamicFade.put("acidrain", config.acidrainFade);
                return true;
            }
        }
        return false;
    }

    private static boolean blizzard() {
        if (ModList.get().isLoaded("betterweather")) {
            BetterWeatherWorldData weatherdata = (BetterWeatherWorldData) world;
            if (weatherdata.getWeatherEventContext() != null && weatherdata.getWeatherEventContext().getCurrentEvent() instanceof Blizzard) {
                dynamicSongs.put("blizzard", config.blizzardSongs);
                dynamicPriorities.put("blizzard", config.blizzardPriority);
                dynamicFade.put("blizzard", config.blizzardFade);
                return true;
            }
        }
        return false;
    }

    private static boolean cloudy() {
        if (ModList.get().isLoaded("betterweather")) {
            BetterWeatherWorldData weatherdata = (BetterWeatherWorldData) world;
            if (weatherdata.getWeatherEventContext() != null && weatherdata.getWeatherEventContext().getCurrentEvent() instanceof Cloudy) {
                dynamicSongs.put("cloudy", config.cloudySongs);
                dynamicPriorities.put("cloudy", config.cloudyPriority);
                dynamicFade.put("cloudy", config.cloudyFade);
                return true;
            }
        }
        return false;
    }

    private static boolean lightrain() {
        if (ModList.get().isLoaded("betterweather")) {
            BetterWeatherWorldData weatherdata = (BetterWeatherWorldData) world;
            if (weatherdata.getWeatherEventContext() != null && weatherdata.getWeatherEventContext().getCurrentEvent() instanceof Rain && !(weatherdata.getWeatherEventContext().getCurrentEvent() instanceof AcidRain)) {
                dynamicSongs.put("lightrain", config.lightrainSongs);
                dynamicPriorities.put("lightrain", config.lightrainPriority);
                dynamicFade.put("lightrain", config.lightrainFade);
                return true;
            }
        }
        return false;
    }

    private static List<String> seasons() {
        List<String> tempList = new ArrayList<>();
        if (ModList.get().isLoaded("sereneseasons")) {
            if (!SoundHandler.seasonsSongsString.isEmpty()) {
                for (Map.Entry<Integer, List<String>> intListEntry : SoundHandler.seasonsSongsString.entrySet()) {
                    int seasonID = intListEntry.getKey();
                    assert mc.level != null;
                    ISeasonState curSeason = SeasonHelper.getSeasonState(mc.level);
                    if (seasonID == 0 && curSeason.getSeason() == Season.SPRING) {
                        dynamicSongs.put("season:"+seasonID, SoundHandler.seasonsSongsString.get(seasonID));
                        dynamicPriorities.put("season:"+seasonID, SoundHandler.seasonsPriorities.get(seasonID));
                        dynamicFade.put("season:"+seasonID, SoundHandler.seasonsFade.get(seasonID));
                        if (!tempList.contains("season:"+seasonID)) {
                            tempList.add("season:"+seasonID);
                        }
                    } else if (seasonID == 1 && curSeason.getSeason() == Season.SUMMER) {
                        dynamicSongs.put("season:"+seasonID, SoundHandler.seasonsSongsString.get(seasonID));
                        dynamicPriorities.put("season:"+seasonID, SoundHandler.seasonsPriorities.get(seasonID));
                        dynamicFade.put("season:"+seasonID, SoundHandler.seasonsFade.get(seasonID));
                        if (!tempList.contains("season:"+seasonID)) {
                            tempList.add("season:"+seasonID);
                        }
                    } else if (seasonID == 2 && curSeason.getSeason() == Season.AUTUMN) {
                        dynamicSongs.put("autumn", SoundHandler.seasonsSongsString.get(seasonID));
                        dynamicPriorities.put("autumn", SoundHandler.seasonsPriorities.get(seasonID));
                        dynamicFade.put("autumn", SoundHandler.seasonsFade.get(seasonID));
                        if (!tempList.contains("autumn")) {
                            tempList.add("autumn");
                        }
                    } else if (seasonID == 3 && curSeason.getSeason() == Season.WINTER) {
                        MusicTriggers.logger.info("found winter");
                        dynamicSongs.put("season:"+seasonID, SoundHandler.seasonsSongsString.get(seasonID));
                        dynamicPriorities.put("season:"+seasonID, SoundHandler.seasonsPriorities.get(seasonID));
                        dynamicFade.put("season:"+seasonID, SoundHandler.seasonsFade.get(seasonID));
                        if (!tempList.contains("season:"+seasonID)) {
                            tempList.add("season:"+seasonID);
                        }
                    }
                }
            }
        }
        return tempList;
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

    public static BlockPos roundedPos(PlayerEntity p) {
        return new BlockPos((Math.round(p.blockPosition().getX() * 2) / 2.0), (Math.round(p.blockPosition().getY() * 2) / 2.0), (Math.round(p.blockPosition().getZ() * 2) / 2.0));
    }

    public static double averageLight(BlockPos p, boolean b) {
        return b ? world.getRawBrightness(p, 0) : world.getBrightness(LightType.BLOCK, p);
    }
}