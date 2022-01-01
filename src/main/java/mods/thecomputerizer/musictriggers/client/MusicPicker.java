package mods.thecomputerizer.musictriggers.client;

import CoroUtil.util.Vec3;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.lunarevents.BloodMoon;
import de.ellpeck.nyx.lunarevents.HarvestMoon;
import de.ellpeck.nyx.lunarevents.StarShower;
import lumien.bloodmoon.Bloodmoon;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config;
import mods.thecomputerizer.musictriggers.configRegistry;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.musictriggers.util.packet;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Optional;
import org.orecruncher.dsurround.client.weather.Weather;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;
import weather2.api.WeatherDataHelper;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObjectSandstorm;

import java.util.*;

public class MusicPicker {
    public static Minecraft mc;
    public static EntityPlayer player;
    public static World world;

    public static HashMap<String, Integer> lightPersistence = new HashMap<>();
    public static HashMap<String, Integer> persistentMob = new HashMap<>();
    public static HashMap<String, Integer> persistentBiome = new HashMap<>();
    public static HashMap<Integer, Integer> persistentVictory = new HashMap<>();
    public static HashMap<Integer, List<EntityLiving>> victoryMobs = new HashMap<>();
    public static HashMap<Integer, EntityPlayer> victoryPlayer = new HashMap<>();
    public static HashMap<Integer, Boolean> victory = new HashMap<>();
    public static int persistentPVP = 0;
    public static int victoryID = 0;
    public static boolean setPVP = false;
    public static EntityPlayer otherPVP;
    public static boolean infernalLoaded = false;

    public static HashMap<String, String[]> dynamicSongs = new HashMap<>();
    public static HashMap<String, Integer> dynamicPriorities = new HashMap<>();
    public static HashMap<String, Integer> dynamicFade = new HashMap<>();

    public static List<String> playableList = new ArrayList<>();
    public static List<String> titleCardEvents = new ArrayList<>();

    public static List<String> effectList = new ArrayList<>();

    public static int curFade = 0;
    public static boolean shouldChange = false;

    public static float musicVolSave;
    public static float masterVolSave;

    public static String[] playThese() {
        if (!MusicPlayer.fading) {
            titleCardEvents = new ArrayList<>();
        }
        mc = Minecraft.getMinecraft();
        player = mc.player;
        if (player != null) {
            world = player.getEntityWorld();
        }
        if (player == null) {
            musicVolSave = mc.gameSettings.getSoundLevel(SoundCategory.MUSIC);
            masterVolSave = mc.gameSettings.getSoundLevel(SoundCategory.MASTER);
            return config.menu.menuSongs;
        }
        List<String> res = comboChecker(priorityHandler(playableEvents()));
        if (res != null && !res.isEmpty()) {
            dynamicSongs = new HashMap<>();
            dynamicPriorities = new HashMap<>();
            dynamicFade = new HashMap<>();
            return res.toArray(new String[0]);
        }
        dynamicSongs = new HashMap<>();
        dynamicPriorities = new HashMap<>();
        dynamicFade = new HashMap<>();
        curFade = config.generic.genericFade;
        return config.generic.genericSongs;
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
                if (SoundHandler.stringBreaker(SoundHandler.stringBreaker(s,";")[0],"|")[0].matches(SoundHandler.stringBreaker(SoundHandler.stringBreaker(checkThis,";")[0],"|")[0])) {
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
            if (dynamicPriorities.get(list) > highest && !Arrays.asList(dynamicSongs.get(list)).isEmpty()) {
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

    @SuppressWarnings("rawtypes")
    public static List<String> playableEvents() {
        List<String> events = new ArrayList<>();
        double time = (double) world.getWorldTime() / 24000.0;
        if (time > 1) {
            time = time - (long) time;
        }
        if (time < 0.54166666666) {
            events.add("day");
            dynamicSongs.put("day", config.day.daySongs);
            dynamicPriorities.put("day", config.day.dayPriority);
            dynamicFade.put("day", config.day.dayFade);
        } else {
            if (SoundHandler.nightSongs.get(0) != null) {
                events.add("night" + 0);
                String[] dimSongsArray;
                if (SoundHandler.nightSongsString.get((world.getMoonPhase() + 1)) != null && !SoundHandler.nightSongsString.get((world.getMoonPhase() + 1)).isEmpty()) {
                    dimSongsArray = new String[(SoundHandler.nightSongsString.get(0).size()) + (SoundHandler.nightSongsString.get((world.getMoonPhase() + 1)).size())];
                } else {
                    dimSongsArray = new String[(SoundHandler.nightSongsString.get(0).size())];
                }
                List<String> tempNight = new ArrayList<>();
                if (SoundHandler.nightSongsString.get((world.getMoonPhase() + 1)) != null && !SoundHandler.nightSongsString.get((world.getMoonPhase() + 1)).isEmpty()) {
                    tempNight.addAll(SoundHandler.nightSongsString.get((world.getMoonPhase() + 1)));
                }
                tempNight.addAll(SoundHandler.nightSongsString.get(0));
                dynamicSongs.put("night" + 0, tempNight.toArray(dimSongsArray));
                dynamicPriorities.put("night" + 0, config.night.nightPriority);
                dynamicFade.put("night" + 0, SoundHandler.nightFade.get(0));
            } else {
                if (SoundHandler.nightSongs.get((world.getMoonPhase() + 1)) != null) {
                    events.add("night" + (world.getMoonPhase() + 1));
                    String[] dimSongsArray = new String[SoundHandler.nightSongsString.get((world.getMoonPhase() + 1)).size()];
                    dynamicSongs.put("night" + (world.getMoonPhase() + 1), SoundHandler.nightSongsString.get((world.getMoonPhase() + 1)).toArray(dimSongsArray));
                    dynamicPriorities.put("night" + (world.getMoonPhase() + 1), config.night.nightPriority);
                    dynamicFade.put("night" + (world.getMoonPhase() + 1), SoundHandler.nightFade.get((world.getMoonPhase() + 1)));
                }
            }
        }
        if (time < 0.54166666666 && time >= 0.5) {
            events.add("sunset");
            dynamicSongs.put("sunset", config.sunset.sunsetSongs);
            dynamicPriorities.put("sunset", config.sunset.sunsetPriority);
            dynamicFade.put("sunset", config.sunset.sunsetFade);
        } else if (time >= 0.95833333333 && time < 1) {
            events.add("sunrise");
            dynamicSongs.put("sunrise", config.sunrise.sunriseSongs);
            dynamicPriorities.put("sunrise", config.sunrise.sunrisePriority);
            dynamicFade.put("sunrise", config.sunrise.sunriseFade);
        }
        for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.lightSongsString.entrySet()) {
            String lightName = ((Map.Entry) stringListEntry).getKey().toString();
            if (averageLight(roundedPos(player), SoundHandler.lightSky.get(lightName)) <= SoundHandler.lightLevel.get(lightName)) {
                events.add(lightName);
                String[] lightSongsArray = new String[SoundHandler.lightSongsString.get(lightName).size()];
                dynamicSongs.put(lightName, SoundHandler.lightSongsString.get(lightName).toArray(lightSongsArray));
                dynamicPriorities.put(lightName, SoundHandler.lightPriorities.get(lightName));
                dynamicFade.put(lightName, SoundHandler.lightFade.get(lightName));
                lightPersistence.put(lightName, SoundHandler.lightTime.get(lightName));
            } else if (lightPersistence.get(lightName) > 0) {
                events.add(lightName);
                String[] lightSongsArray = new String[SoundHandler.lightSongsString.get(lightName).size()];
                dynamicSongs.put(lightName, SoundHandler.lightSongsString.get(lightName).toArray(lightSongsArray));
                dynamicPriorities.put(lightName, SoundHandler.lightPriorities.get(lightName));
                dynamicFade.put(lightName, SoundHandler.lightFade.get(lightName));
            }
        }
        if (player.posY < config.deepUnder.deepUnderLevel && !world.canSeeSky(roundedPos(player))) {
            events.add("deepUnder");
            dynamicSongs.put("deepUnder", config.deepUnder.deepUnderSongs);
            dynamicPriorities.put("deepUnder", config.deepUnder.deepUnderPriority);
            dynamicFade.put("deepUnder", config.deepUnder.deepUnderFade);
        }
        if (player.posY < config.underground.undergroundLevel && !world.canSeeSky(roundedPos(player))) {
            events.add("underground");
            dynamicSongs.put("underground", config.underground.undergroundSongs);
            dynamicPriorities.put("underground", config.underground.undergroundPriority);
            dynamicFade.put("underground", config.underground.undergroundFade);
        }
        if (player.posY < config.inVoid.inVoidLevel) {
            events.add("inVoid");
            dynamicSongs.put("inVoid", config.inVoid.inVoidSongs);
            dynamicPriorities.put("inVoid", config.inVoid.inVoidPriority);
            dynamicFade.put("inVoid", config.inVoid.inVoidFade);
        }
        if (player.posY >= config.high.highLevel) {
            events.add("high");
            dynamicSongs.put("high", config.high.highSongs);
            dynamicPriorities.put("high", config.high.highPriority);
            dynamicFade.put("high", config.high.highFade);
        }
        if (world.isRaining()) {
            events.add("raining");
            dynamicSongs.put("raining", config.raining.rainingSongs);
            dynamicPriorities.put("raining", config.raining.rainingPriority);
            dynamicFade.put("raining", config.raining.rainingFade);
            if (world.canSnowAt(player.getPosition(), true)) {
                events.add("snowing");
                dynamicSongs.put("snowing", config.snowing.snowingSongs);
                dynamicPriorities.put("snowing", config.snowing.snowingPriority);
                dynamicFade.put("snowing", config.snowing.snowingFade);
            }
        }
        if (world.isThundering()) {
            events.add("storming");
            dynamicSongs.put("storming", config.storming.stormingSongs);
            dynamicPriorities.put("storming", config.storming.stormingPriority);
            dynamicFade.put("storming", config.storming.stormingFade);
        }
        if (player.getHealth() < player.getMaxHealth() * (config.lowHP.lowHPLevel)) {
            events.add("lowHP");
            dynamicSongs.put("lowHP", config.lowHP.lowHPSongs);
            dynamicPriorities.put("lowHP", config.lowHP.lowHPPriority);
            dynamicFade.put("lowHP", config.lowHP.lowHPFade);
        }
        if (player.isDead) {
            events.add("dead");
            dynamicSongs.put("dead", config.dead.deadSongs);
            dynamicPriorities.put("dead", config.dead.deadPriority);
            dynamicFade.put("dead", config.dead.deadFade);
            for (Map.Entry<Integer, Boolean> integerListEntry : victory.entrySet()) {
                int key = integerListEntry.getKey();
                victory.put(key, false);
            }
        }
        if (player.isSpectator()) {
            events.add("spectator");
            dynamicSongs.put("spectator", config.spectator.spectatorSongs);
            dynamicPriorities.put("spectator", config.spectator.spectatorPriority);
            dynamicFade.put("spectator", config.spectator.spectatorFade);
        }
        if (player.isCreative()) {
            events.add("creative");
            dynamicSongs.put("creative", config.creative.creativeSongs);
            dynamicPriorities.put("creative", config.creative.creativePriority);
            dynamicFade.put("creative", config.creative.creativeFade);
        }
        if (player.isRiding()) {
            events.add("riding");
            dynamicSongs.put("riding", config.riding.ridingSongs);
            dynamicPriorities.put("riding", config.riding.ridingPriority);
            dynamicFade.put("riding", config.riding.ridingFade);
        }
        if (world.getBlockState(roundedPos(player)).getMaterial() == Material.WATER && world.getBlockState(roundedPos(player).up()).getMaterial() == Material.WATER) {
            events.add("underwater");
            dynamicSongs.put("underwater", config.underwater.underwaterSongs);
            dynamicPriorities.put("underwater", config.underwater.underwaterPriority);
            dynamicFade.put("underwater", config.underwater.underwaterFade);
        }
        for (EntityLiving ent : world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(player.posX - 16, player.posY - 8, player.posZ - 16, player.posX + 16, player.posY + 8, player.posZ + 16))) {
            if (ent instanceof EntityTameable && ent.serializeNBT().getString("Owner").matches(player.getName())) {
                events.add("pet");
                dynamicSongs.put("pet", config.pet.petSongs);
                dynamicPriorities.put("pet", config.pet.petPriority);
                dynamicFade.put("pet", config.pet.petFade);
                break;
            }
        }
        if (SoundHandler.dimensionSongs.get(player.dimension) != null) {
            events.add("dimension" + player.dimension);
            String[] dimSongsArray = new String[SoundHandler.dimensionSongsString.get(player.dimension).size()];
            dynamicSongs.put("dimension" + player.dimension, SoundHandler.dimensionSongsString.get(player.dimension).toArray(dimSongsArray));
            dynamicPriorities.put("dimension" + player.dimension, SoundHandler.dimensionPriorities.get(player.dimension));
            dynamicFade.put("dimension" + player.dimension, SoundHandler.dimensionFade.get(player.dimension));
        }
        if (SoundHandler.biomeSongs != null && !SoundHandler.biomeSongs.isEmpty()) {
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.biomeSongsString.entrySet()) {
                String biomeRegex = ((Map.Entry) stringListEntry).getKey().toString();
                if (Objects.requireNonNull(world.getBiome(roundedPos(player)).getRegistryName()).toString().contains(biomeRegex)) {
                    events.add(biomeRegex);
                    String[] biomeSongsArray = new String[SoundHandler.biomeSongsString.get(biomeRegex).size()];
                    dynamicSongs.put(biomeRegex, SoundHandler.biomeSongsString.get(biomeRegex).toArray(biomeSongsArray));
                    dynamicPriorities.put(biomeRegex, SoundHandler.biomePriorities.get(biomeRegex));
                    dynamicFade.put(biomeRegex, SoundHandler.biomeFade.get(biomeRegex));
                    persistentBiome.put(biomeRegex, SoundHandler.biomePersistence.get(biomeRegex));
                } else if (persistentBiome.get(biomeRegex) > 0) {
                    events.add(biomeRegex);
                    String[] biomeSongsArray = new String[SoundHandler.biomeSongsString.get(biomeRegex).size()];
                    dynamicSongs.put(biomeRegex, SoundHandler.biomeSongsString.get(biomeRegex).toArray(biomeSongsArray));
                    dynamicPriorities.put(biomeRegex, SoundHandler.biomePriorities.get(biomeRegex));
                    dynamicFade.put(biomeRegex, SoundHandler.biomeFade.get(biomeRegex));
                }
            }
        }
        if (mc.isSingleplayer()) {
            WorldServer nworld = Objects.requireNonNull(mc.getIntegratedServer()).getWorld(player.dimension);
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.structureSongsString.entrySet()) {
                String structName = ((Map.Entry) stringListEntry).getKey().toString();
                if (nworld.getChunkProvider().isInsideStructure(world, structName, player.getPosition())) {
                    events.add("structure:" + structName);
                    String[] structureSongsArray = new String[SoundHandler.structureSongsString.get(structName).size()];
                    dynamicSongs.put("structure:" + structName, SoundHandler.structureSongsString.get(structName).toArray(structureSongsArray));
                    dynamicPriorities.put("structure:" + structName, SoundHandler.structurePriorities.get(structName));
                    dynamicFade.put("structure:" + structName, SoundHandler.structureFade.get(structName));
                }
            }
        } else if (!configRegistry.registry.clientSideOnly) {
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.structureSongsString.entrySet()) {
                String structName = ((Map.Entry) stringListEntry).getKey().toString();
                RegistryHandler.network.sendToServer(new packet.packetMessage(structName, player.getPosition(), player.dimension, player.getUniqueID()));
                if (fromServer.inStructure.containsKey(structName)) {
                    if (fromServer.inStructure.get(structName)) {
                        events.add("structure:" + structName);
                        String[] structureSongsArray = new String[SoundHandler.structureSongsString.get(structName).size()];
                        dynamicSongs.put("structure:" + structName, SoundHandler.structureSongsString.get(structName).toArray(structureSongsArray));
                        dynamicPriorities.put("structure:" + structName, SoundHandler.structurePriorities.get(structName));
                        dynamicFade.put("structure:" + structName, SoundHandler.structureFade.get(structName));
                        fromServer.curStruct = structName;
                    } else {
                        fromServer.curStruct = null;
                    }
                }
            }
        }
        for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.mobSongsString.entrySet()) {
            String mobName = ((Map.Entry) stringListEntry).getKey().toString();
            double range = SoundHandler.mobRange.get(mobName);
            List<EntityLiving> mobTempList = world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(player.posX - range, player.posY - (range / 2), player.posZ - range, player.posX + range, player.posY + (range / 2), player.posZ + range));
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
            if (mobName.matches("MOB")) {
                for (EntityLiving e : mobList) {
                    if (player.canEntityBeSeen(e)) {
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
                    if (SoundHandler.mobVictory.get(mobName)) {
                        victoryID = SoundHandler.mobVictoryID.get(mobName);
                        victoryMobs.computeIfAbsent(victoryID, k -> new ArrayList<>());
                        if (!victoryMobs.get(victoryID).contains(e) && victoryMobs.get(victoryID).size() < SoundHandler.mobNumber.get(mobName)) {
                            victoryMobs.get(victoryID).add(e);
                        }
                    }
                }
                if (mobList.size() >= SoundHandler.mobNumber.get(mobName) && (((SoundHandler.mobTargetting.get(mobName) && (float) trackingCounter / SoundHandler.mobNumber.get(mobName) >= SoundHandler.mobHordeTargetting.get(mobName) / 100F) || !SoundHandler.mobTargetting.get(mobName)) && infernalDone && (float) healthCounter / SoundHandler.mobNumber.get(mobName) >= SoundHandler.mobHordeHealth.get(mobName) / 100F)) {
                    events.add(mobName);
                    String[] mobSongsArray = new String[SoundHandler.mobSongsString.get(mobName).size()];
                    dynamicSongs.put(mobName, SoundHandler.mobSongsString.get(mobName).toArray(mobSongsArray));
                    dynamicPriorities.put(mobName, SoundHandler.mobPriorities.get(mobName));
                    dynamicFade.put(mobName, SoundHandler.mobFade.get(mobName));
                    persistentMob.put(mobName, SoundHandler.mobBattle.get(mobName));
                    victory.put(victoryID, SoundHandler.mobVictory.get(mobName));
                }
            } else {
                int mobCounter = 0;
                List<EntityLiving> mobListSpecific = new ArrayList<>();
                for (EntityLiving e : mobTempList) {
                    if (e.getName().matches(mobName) || Objects.requireNonNull(EntityList.getKey(e)).toString().matches(mobName)) {
                        mobCounter++;
                        mobListSpecific.add(e);
                    }
                }
                for (EntityLiving e : mobListSpecific) {
                    if (player.canEntityBeSeen(e)) {
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
                    if (SoundHandler.mobVictory.get(mobName)) {
                        victoryID = SoundHandler.mobVictoryID.get(mobName);
                        victoryMobs.computeIfAbsent(victoryID, k -> new ArrayList<>());
                        if (!victoryMobs.get(victoryID).contains(e) && victoryMobs.get(victoryID).size() < SoundHandler.mobNumber.get(mobName)) {
                            victoryMobs.get(victoryID).add(e);
                        }
                    }
                }
                if (mobCounter >= SoundHandler.mobNumber.get(mobName) && (((SoundHandler.mobTargetting.get(mobName) && (float) trackingCounter / SoundHandler.mobNumber.get(mobName) >= SoundHandler.mobHordeTargetting.get(mobName) / 100F) || !SoundHandler.mobTargetting.get(mobName)) && infernalDone && (float) healthCounter / SoundHandler.mobNumber.get(mobName) >= SoundHandler.mobHordeHealth.get(mobName) / 100F)) {
                    events.add(mobName);
                    String[] mobSongsArray = new String[SoundHandler.mobSongsString.get(mobName).size()];
                    dynamicSongs.put(mobName, SoundHandler.mobSongsString.get(mobName).toArray(mobSongsArray));
                    dynamicPriorities.put(mobName, SoundHandler.mobPriorities.get(mobName));
                    dynamicFade.put(mobName, SoundHandler.mobFade.get(mobName));
                    persistentMob.put(mobName, SoundHandler.mobBattle.get(mobName));
                    victory.put(victoryID, SoundHandler.mobVictory.get(mobName));
                }
            }
            if (persistentMob.get(mobName) > 0) {
                if (!events.contains(mobName)) {
                    events.add(mobName);
                    String[] mobSongsArray = new String[SoundHandler.mobSongsString.get(mobName).size()];
                    dynamicSongs.put(mobName, SoundHandler.mobSongsString.get(mobName).toArray(mobSongsArray));
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
                BlockPos bp = player.getPosition();
                int x1 = Integer.parseInt(broken[0]);
                int y1 = Integer.parseInt(broken[1]);
                int z1 = Integer.parseInt(broken[2]);
                int x2 = Integer.parseInt(broken[3]);
                int y2 = Integer.parseInt(broken[4]);
                int z2 = Integer.parseInt(broken[5]);
                if (bp.getX() > x1 && bp.getX() < x2 && bp.getY() > y1 && bp.getY() < y2 && bp.getZ() > z1 && bp.getZ() < z2) {
                    events.add(zoneRange);
                    String[] zonesSongsArray = new String[SoundHandler.zonesSongsString.get(zoneRange).size()];
                    dynamicSongs.put(zoneRange, SoundHandler.zonesSongsString.get(zoneRange).toArray(zonesSongsArray));
                    dynamicPriorities.put(zoneRange, SoundHandler.zonesPriorities.get(zoneRange));
                    dynamicFade.put(zoneRange, SoundHandler.zonesFade.get(zoneRange));
                }
            }
        }
        if (!SoundHandler.effectSongs.isEmpty()) {
            effectList = new ArrayList<>();
            for (PotionEffect p : player.getActivePotionEffects()) {
                effectList.add(p.getEffectName());
                if (SoundHandler.effectSongsString.containsKey(p.getEffectName())) {
                    events.add(p.getEffectName());
                    String[] effectSongsArray = new String[SoundHandler.effectSongsString.get(p.getEffectName()).size()];
                    dynamicSongs.put(p.getEffectName(), SoundHandler.effectSongsString.get(p.getEffectName()).toArray(effectSongsArray));
                    dynamicPriorities.put(p.getEffectName(), SoundHandler.effectPriorities.get(p.getEffectName()));
                    dynamicFade.put(p.getEffectName(), SoundHandler.effectFade.get(p.getEffectName()));
                }
            }
        }
        if (config.pvp.pvpSongs.length != 0) {
            if (eventsClient.playerSource.getUniqueID() == player.getUniqueID()) {
                otherPVP = eventsClient.playerHurt;
                setPVP = true;
            } else if (eventsClient.playerHurt.getUniqueID() == player.getUniqueID()) {
                otherPVP = eventsClient.playerSource;
                setPVP = true;
            }
            if (setPVP && player.getDistance(otherPVP) <= config.pvp.pvpRange) {
                events.add("PVP");
                dynamicSongs.put("PVP", config.pvp.pvpSongs);
                dynamicPriorities.put("PVP", config.pvp.pvpPriority);
                dynamicFade.put("PVP", config.pvp.pvpFade);
                persistentPVP = config.pvp.pvpTime;
                victoryID = config.pvp.pvpVictoryID;
                if (config.pvp.pvpVictory) {
                    victoryPlayer.put(victoryID, otherPVP);
                    victory.put(victoryID, config.pvp.pvpVictory);
                }
            } else if (persistentPVP > 0) {
                events.add("PVP");
                dynamicSongs.put("PVP", config.pvp.pvpSongs);
                dynamicPriorities.put("PVP", config.pvp.pvpPriority);
                dynamicFade.put("PVP", config.pvp.pvpFade);
                victoryID = config.pvp.pvpVictoryID;

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
                for (EntityLiving e : victoryMobs.get(victoryID)) {
                    if (!e.isDead) {
                        victoryTempM = false;
                        break;
                    }
                }
            } else {
                victoryTempM = false;
            }
            if ((victoryPlayer.get(victoryID) != null && !victoryPlayer.get(victoryID).isDead) || victoryPlayer.get(victoryID) == null) {
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
            String[] victorySongsArray = new String[SoundHandler.victorySongsString.get("Victory" + victoryID).size()];
            dynamicSongs.put("Victory" + victoryID, SoundHandler.victorySongsString.get("Victory" + victoryID).toArray(victorySongsArray));
            dynamicPriorities.put("Victory" + victoryID, SoundHandler.victoryPriorities.get("Victory" + victoryID));
            dynamicFade.put("Victory" + victoryID, SoundHandler.victoryFade.get("Victory" + victoryID));
        }
        if(!mc.inGameHasFocus) {
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.guiSongsString.entrySet()) {
                String guiName = ((Map.Entry) stringListEntry).getKey().toString();
                if(eventsClient.GUIName.contains(guiName)) {
                    events.add(guiName);
                    String[] guiSongsArray = new String[SoundHandler.guiSongsString.get(guiName).size()];
                    dynamicSongs.put(guiName, SoundHandler.guiSongsString.get(guiName).toArray(guiSongsArray));
                    dynamicPriorities.put(guiName, SoundHandler.guiPriorities.get(guiName));
                    dynamicFade.put(guiName, SoundHandler.guiFade.get(guiName));
                }
            }
        }
        else {
            musicVolSave = mc.gameSettings.getSoundLevel(SoundCategory.MUSIC);
            masterVolSave = mc.gameSettings.getSoundLevel(SoundCategory.MASTER);
        }
        if(!SoundHandler.difficultySongsString.isEmpty()) {
            for (Map.Entry<Integer, List<String>> intListEntry : SoundHandler.difficultySongsString.entrySet()) {
                int diffID = intListEntry.getKey();
                if (diffID == 4 && mc.world.getWorldInfo().isHardcoreModeEnabled()) {
                    events.add("difficulty:"+diffID);
                    String[] difficultySongsArray = new String[SoundHandler.difficultySongsString.get(diffID).size()];
                    dynamicSongs.put("difficulty:"+diffID, SoundHandler.difficultySongsString.get(diffID).toArray(difficultySongsArray));
                    dynamicPriorities.put("difficulty:"+diffID, SoundHandler.difficultyPriorities.get(diffID));
                    dynamicFade.put("difficulty:"+diffID, SoundHandler.difficultyFade.get(diffID));
                } else if (diffID == 3 && mc.world.getDifficulty() == EnumDifficulty.HARD) {
                    events.add("difficulty:"+diffID);
                    String[] difficultySongsArray = new String[SoundHandler.difficultySongsString.get(diffID).size()];
                    dynamicSongs.put("difficulty:"+diffID, SoundHandler.difficultySongsString.get(diffID).toArray(difficultySongsArray));
                    dynamicPriorities.put("difficulty:"+diffID, SoundHandler.difficultyPriorities.get(diffID));
                    dynamicFade.put("difficulty:"+diffID, SoundHandler.difficultyFade.get(diffID));
                } else if (diffID == 2 && mc.world.getDifficulty() == EnumDifficulty.NORMAL) {
                    events.add("difficulty:"+diffID);
                    String[] difficultySongsArray = new String[SoundHandler.difficultySongsString.get(diffID).size()];
                    dynamicSongs.put("difficulty:"+diffID, SoundHandler.difficultySongsString.get(diffID).toArray(difficultySongsArray));
                    dynamicPriorities.put("difficulty:"+diffID, SoundHandler.difficultyPriorities.get(diffID));
                    dynamicFade.put("difficulty:"+diffID, SoundHandler.difficultyFade.get(diffID));
                } else if (diffID == 1 && mc.world.getDifficulty() == EnumDifficulty.EASY) {
                    events.add("difficulty:"+diffID);
                    String[] difficultySongsArray = new String[SoundHandler.difficultySongsString.get(diffID).size()];
                    dynamicSongs.put("difficulty:"+diffID, SoundHandler.difficultySongsString.get(diffID).toArray(difficultySongsArray));
                    dynamicPriorities.put("difficulty:"+diffID, SoundHandler.difficultyPriorities.get(diffID));
                    dynamicFade.put("difficulty:"+diffID, SoundHandler.difficultyFade.get(diffID));
                } else if (diffID == 0 && mc.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
                    events.add("difficulty:"+diffID);
                    String[] difficultySongsArray = new String[SoundHandler.difficultySongsString.get(diffID).size()];
                    dynamicSongs.put("difficulty:"+diffID, SoundHandler.difficultySongsString.get(diffID).toArray(difficultySongsArray));
                    dynamicPriorities.put("difficulty:"+diffID, SoundHandler.difficultyPriorities.get(diffID));
                    dynamicFade.put("difficulty:"+diffID, SoundHandler.difficultyFade.get(diffID));
                }
            }
        }
        try {
            List<String> whitelist = stageWhitelistChecker();
            List<String> blacklist = stageBlacklistChecker();
            if (!whitelist.isEmpty()) {
                events.addAll(whitelist);
            }
            if (!blacklist.isEmpty()) {
                events.addAll(blacklist);
            }
        } catch (NoSuchMethodError ignored) {
        }
        try {
            boolean bloodmoon = bloodmoon();
            if (bloodmoon) {
                events.add("bloodmoon");
            }
        } catch (NoSuchMethodError ignored) {
        }
        try {
            boolean nyxbloodmoon = nyxbloodmoon();
            if (nyxbloodmoon) {
                events.add("bloodmoon");
            }
        } catch (NoSuchMethodError ignored) {
        }
        try {
            boolean nyxharvestmoon = nyxharvestmoon();
            if (nyxharvestmoon) {
                events.add("harvestmoon");
            }
        } catch (NoSuchMethodError ignored) {
        }
        try {
            boolean nyxfallingstars = nyxfallingstars();
            if (nyxfallingstars) {
                events.add("fallingstars");
            }
        } catch (NoSuchMethodError ignored) {
        }
        try {
            if (dynamicrain() != null) {
                int rainIntensity = Integer.parseInt(Objects.requireNonNull(dynamicrain()));
                events.add("Rain Intensity" + rainIntensity);
            }
        } catch (NoSuchMethodError ignored) {
        }
        try {
            List<String> tornado = weatherTornado();
            if (!tornado.isEmpty()) {
                events.addAll(tornado);
            }
            if (weatherHurricane()) {
                events.add("hurricane");
            }
            if (weatherSandstorm()) {
                events.add("sandstorm");
            }
        } catch (NoSuchMethodError ignored) {}
        try {
            List<String> seasons = seasons();
            if(!seasons.isEmpty()) {
                events.addAll(seasons);
            }
        } catch (NoSuchMethodError ignored) {}

        playableList = events;
        return events;
    }

    @SuppressWarnings("rawtypes")
    @Optional.Method(modid = "gamestages")
    private static List<String> stageWhitelistChecker() {
        List<String> events = new ArrayList<>();
        for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.gamestageSongsStringWhitelist.entrySet()) {
            String stageName = ((Map.Entry) stringListEntry).getKey().toString();
            String temp = stageName;
            if (temp.startsWith("@")) {
                temp = temp.substring(1);
            }
            if (GameStageHelper.clientHasStage(player, temp)) {
                events.add(stageName + "true");
                String[] gamestageSongsArray = new String[SoundHandler.gamestageSongsStringWhitelist.get(stageName).size()];
                dynamicSongs.put(stageName + "true", SoundHandler.gamestageSongsStringWhitelist.get(stageName).toArray(gamestageSongsArray));
                dynamicPriorities.put(stageName + "true", SoundHandler.gamestagePrioritiesWhitelist.get(stageName));
                dynamicFade.put(stageName + "true", SoundHandler.gamestageFadeWhitelist.get(stageName));
            }
        }
        return events;
    }

    @SuppressWarnings("rawtypes")
    @Optional.Method(modid = "gamestages")
    private static List<String> stageBlacklistChecker() {
        List<String> events = new ArrayList<>();
        for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.gamestageSongsStringBlacklist.entrySet()) {
            String stageName = ((Map.Entry) stringListEntry).getKey().toString();
            String temp = stageName;
            if (temp.startsWith("@")) {
                temp = temp.substring(1);
            }
            if (!GameStageHelper.clientHasStage(player, temp)) {
                events.add(stageName + "false");
                String[] gamestageSongsArray = new String[SoundHandler.gamestageSongsStringBlacklist.get(stageName).size()];
                dynamicSongs.put(stageName + "false", SoundHandler.gamestageSongsStringBlacklist.get(stageName).toArray(gamestageSongsArray));
                dynamicPriorities.put(stageName + "false", SoundHandler.gamestagePrioritiesBlacklist.get(stageName));
                dynamicFade.put(stageName + "true", SoundHandler.gamestageFadeBlacklist.get(stageName));
            }
        }
        return events;
    }

    @Optional.Method(modid = "bloodmoon")
    private static boolean bloodmoon() {
        if (Bloodmoon.proxy.isBloodmoon()) {
            dynamicSongs.put("bloodmoon", config.bloodmoon.bloodmoonSongs);
            dynamicPriorities.put("bloodmoon", config.bloodmoon.bloodmoonPriority);
            dynamicFade.put("bloodmoon", config.bloodmoon.bloodmoonFade);
            return true;
        }
        return false;
    }

    @Optional.Method(modid = "nyx")
    private static boolean nyxbloodmoon() {
        if (NyxWorld.get(player.getEntityWorld()).currentEvent instanceof BloodMoon) {
            dynamicSongs.put("bloodmoon", config.bloodmoon.bloodmoonSongs);
            dynamicPriorities.put("bloodmoon", config.bloodmoon.bloodmoonPriority);
            dynamicFade.put("bloodmoon", config.bloodmoon.bloodmoonFade);
            return true;
        }
        return false;
    }

    @Optional.Method(modid = "nyx")
    private static boolean nyxharvestmoon() {
        if (NyxWorld.get(player.getEntityWorld()).currentEvent instanceof HarvestMoon) {
            dynamicSongs.put("harvestmoon", config.harvestmoon.harvestmoonSongs);
            dynamicPriorities.put("harvestmoon", config.harvestmoon.harvestmoonPriority);
            dynamicFade.put("harvestmoon", config.harvestmoon.harvestmoonFade);
            return true;
        }
        return false;
    }

    @Optional.Method(modid = "nyx")
    private static boolean nyxfallingstars() {
        if (NyxWorld.get(player.getEntityWorld()).currentEvent instanceof StarShower) {
            dynamicSongs.put("fallingstars", config.fallingstars.fallingstarsSongs);
            dynamicPriorities.put("fallingstars", config.fallingstars.fallingstarsPriority);
            dynamicFade.put("fallingstars", config.fallingstars.fallingstarsFade);
            return true;
        }
        return false;
    }

    @Optional.Method(modid = "dsurround")
    private static String dynamicrain() {
        for (Map.Entry<Integer, List<String>> integerListEntry : SoundHandler.rainintensitySongsString.entrySet()) {
            int intensity = integerListEntry.getKey();
            if (Weather.getIntensityLevel() > (float) intensity / 100F) {
                String[] rainIntensityArray = new String[SoundHandler.rainintensitySongsString.get(intensity).size()];
                dynamicSongs.put("Rain Intensity" + intensity, SoundHandler.rainintensitySongsString.get(intensity).toArray(rainIntensityArray));
                dynamicPriorities.put("Rain Intensity" + intensity, config.rainintensity.rainintensityPriority);
                dynamicFade.put("Rain Intensity" + intensity, config.rainintensity.rainintensityFade);
                return intensity + "";
            }
        }
        return null;
    }

    @Optional.Method(modid = "weather2")
    private static List<String> weatherTornado() {
        List<String> tempList = new ArrayList<>();
        for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.tornadoSongsString.entrySet()) {
            String entry = stringListEntry.getKey();
            if (WeatherDataHelper.getWeatherManagerForClient() != null && WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()), config.tornado.tornadoRange) != null) {
                StormObject storm = WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()), config.tornado.tornadoRange);
                if (storm.levelCurIntensityStage >= SoundHandler.tornadoIntensity.get(entry)) {
                    String[] tornadoSongsArray = new String[SoundHandler.tornadoSongsString.get(entry).size()];
                    dynamicSongs.put(entry, SoundHandler.tornadoSongsString.get(entry).toArray(tornadoSongsArray));
                    dynamicPriorities.put(entry, SoundHandler.tornadoPriorities.get(entry));
                    dynamicFade.put(entry, SoundHandler.tornadoFade.get(entry));
                    if(!tempList.contains(entry)) {
                        tempList.add(entry);
                    }
                }
            }
        }
        return tempList;
    }

    @Optional.Method(modid = "weather2")
    private static boolean weatherHurricane() {
        if (WeatherDataHelper.getWeatherManagerForClient() != null && WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()), config.hurricane.hurricaneRange) != null) {
            StormObject storm = WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()), config.hurricane.hurricaneRange);
            if (storm.isHurricane()) {
                dynamicSongs.put("hurricane", config.hurricane.hurricaneSongs);
                dynamicPriorities.put("hurricane", config.hurricane.hurricanePriority);
                dynamicFade.put("hurricane", config.hurricane.hurricaneFade);
                return true;
            }
        }
        return false;
    }

    @Optional.Method(modid = "weather2")
    private static boolean weatherSandstorm() {
        if (WeatherDataHelper.getWeatherManagerForClient() != null && WeatherDataHelper.getWeatherManagerForClient().getClosestSandstorm(new Vec3(player.getPosition()), config.sandstorm.sandstormRange) != null) {
            WeatherObjectSandstorm storm = WeatherDataHelper.getWeatherManagerForClient().getClosestSandstorm(new Vec3(player.getPosition()), config.sandstorm.sandstormRange);
            if (storm.age > 20) {
                dynamicSongs.put("sandstorm", config.hurricane.hurricaneSongs);
                dynamicPriorities.put("sandstorm", config.hurricane.hurricanePriority);
                dynamicFade.put("sandstorm", config.hurricane.hurricaneFade);
                return true;
            }
        }
        return false;
    }

    @Optional.Method(modid = "sereneseasons")
    private static List<String> seasons() {
        List<String> tempList = new ArrayList<>();
        if(!SoundHandler.seasonsSongsString.isEmpty()) {
            for (Map.Entry<Integer, List<String>> intListEntry : SoundHandler.seasonsSongsString.entrySet()) {
                int seasonID = intListEntry.getKey();
                ISeasonState curSeason = SeasonHelper.getSeasonState(world);
                if(seasonID==0 && curSeason.getSeason()==Season.SPRING) {
                    String[] seasonSongsArray = new String[SoundHandler.seasonsSongsString.get(seasonID).size()];
                    dynamicSongs.put("season:"+seasonID, SoundHandler.seasonsSongsString.get(seasonID).toArray(seasonSongsArray));
                    dynamicPriorities.put("season:"+seasonID, SoundHandler.seasonsPriorities.get(seasonID));
                    dynamicFade.put("season:"+seasonID, SoundHandler.seasonsFade.get(seasonID));
                    if(!tempList.contains("season:"+seasonID)) {
                        tempList.add("season:"+seasonID);
                    }
                }
                else if(seasonID==1 && curSeason.getSeason()==Season.SUMMER) {
                    String[] seasonSongsArray = new String[SoundHandler.seasonsSongsString.get(seasonID).size()];
                    dynamicSongs.put("season:"+seasonID, SoundHandler.seasonsSongsString.get(seasonID).toArray(seasonSongsArray));
                    dynamicPriorities.put("season:"+seasonID, SoundHandler.seasonsPriorities.get(seasonID));
                    dynamicFade.put("season:"+seasonID, SoundHandler.seasonsFade.get(seasonID));
                    if(!tempList.contains("season:"+seasonID)) {
                        tempList.add("season:"+seasonID);
                    }
                }
                else if(seasonID==2 && curSeason.getSeason()==Season.AUTUMN) {
                    String[] seasonSongsArray = new String[SoundHandler.seasonsSongsString.get(seasonID).size()];
                    dynamicSongs.put("season:"+seasonID, SoundHandler.seasonsSongsString.get(seasonID).toArray(seasonSongsArray));
                    dynamicPriorities.put("season:"+seasonID, SoundHandler.seasonsPriorities.get(seasonID));
                    dynamicFade.put("season:"+seasonID, SoundHandler.seasonsFade.get(seasonID));
                    if(!tempList.contains("season:"+seasonID)) {
                        tempList.add("season:"+seasonID);
                    }
                }
                else if(seasonID==3 && curSeason.getSeason()==Season.WINTER) {
                    String[] seasonSongsArray = new String[SoundHandler.seasonsSongsString.get(seasonID).size()];
                    dynamicSongs.put("season:"+seasonID, SoundHandler.seasonsSongsString.get(seasonID).toArray(seasonSongsArray));
                    dynamicPriorities.put("season:"+seasonID, SoundHandler.seasonsPriorities.get(seasonID));
                    dynamicFade.put("season:"+seasonID, SoundHandler.seasonsFade.get(seasonID));
                    if(!tempList.contains("season:"+seasonID)) {
                        tempList.add("season:"+seasonID);
                    }
                }
            }
        }
        return tempList;
    }

    @Optional.Method(modid = "infernalmobs")
    private static boolean infernalChecker(EntityLiving m, String s) {
        infernalLoaded = true;
        if (s == null) {
            return true;
        }
        return InfernalMobsCore.getMobModifiers(m).getModName().matches(s);
    }

    public static BlockPos roundedPos(EntityPlayer p) {
        return new BlockPos((Math.round(p.posX * 2) / 2.0), (Math.round(p.posY * 2) / 2.0), (Math.round(p.posZ * 2) / 2.0));
    }


    public static double averageLight(BlockPos p, boolean b) {
        return b ? world.getLight(p, true) : world.getLightFor(EnumSkyBlock.BLOCK, p);
    }
}