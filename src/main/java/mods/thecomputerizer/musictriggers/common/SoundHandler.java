package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.config;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class SoundHandler {

    public static List<String> allSongs = new ArrayList<>();
    public static List<SoundEvent> allSoundEvents = new ArrayList<>();
    public static HashMap<SoundEvent,String> allSoundEventsWithTriggers = new HashMap<>();

    public static List<SoundEvent> menu = new ArrayList<>();
    public static List<SoundEvent> generic = new ArrayList<>();
    public static List<SoundEvent> day = new ArrayList<>();
    public static List<SoundEvent> sunrise = new ArrayList<>();
    public static List<SoundEvent> sunset = new ArrayList<>();
    public static HashMap<String, List<SoundEvent>> lightSongs = new HashMap<>();
    public static HashMap<String, List<String>> lightSongsString = new HashMap<>();
    public static HashMap<String, Integer> lightLevel = new HashMap<>();
    public static HashMap<String, Boolean> lightSky = new HashMap<>();
    public static HashMap<String, Integer> lightTime = new HashMap<>();
    public static HashMap<String, Integer> lightPriorities = new HashMap<>();
    public static HashMap<String, Integer> lightFade = new HashMap<>();
    public static List<SoundEvent> underground = new ArrayList<>();
    public static List<SoundEvent> deepUnder = new ArrayList<>();
    public static List<SoundEvent> raining = new ArrayList<>();
    public static List<SoundEvent> storming = new ArrayList<>();
    public static List<SoundEvent> snowing = new ArrayList<>();
    public static List<SoundEvent> lowHP = new ArrayList<>();
    public static List<SoundEvent> dead = new ArrayList<>();
    public static List<SoundEvent> inVoid = new ArrayList<>();
    public static List<SoundEvent> spectator = new ArrayList<>();
    public static List<SoundEvent> creative = new ArrayList<>();
    public static List<SoundEvent> riding = new ArrayList<>();
    public static List<SoundEvent> pet = new ArrayList<>();
    public static List<SoundEvent> high = new ArrayList<>();
    public static List<SoundEvent> underwater = new ArrayList<>();
    public static List<SoundEvent> pvp = new ArrayList<>();
    public static List<SoundEvent> elytra = new ArrayList<>();
    public static List<SoundEvent> fishing = new ArrayList<>();

    public static HashMap<Integer, List<SoundEvent>> difficultySongs = new HashMap<>();
    public static HashMap<Integer, List<String>> difficultySongsString = new HashMap<>();
    public static HashMap<Integer, Integer> difficultyPriorities = new HashMap<>();
    public static HashMap<Integer, Integer> difficultyFade = new HashMap<>();

    public static HashMap<Integer, List<SoundEvent>> seasonsSongs = new HashMap<>();
    public static HashMap<Integer, List<String>> seasonsSongsString = new HashMap<>();
    public static HashMap<Integer, Integer> seasonsPriorities = new HashMap<>();
    public static HashMap<Integer, Integer> seasonsFade = new HashMap<>();

    public static HashMap<Integer, List<SoundEvent>> nightSongs = new HashMap<>();
    public static HashMap<Integer, List<String>> nightSongsString = new HashMap<>();
    public static HashMap<Integer, Integer> nightFade = new HashMap<>();

    public static HashMap<String, List<SoundEvent>> dimensionSongs = new HashMap<>();
    public static HashMap<String, List<String>> dimensionSongsString = new HashMap<>();
    public static HashMap<String, Integer> dimensionPriorities = new HashMap<>();
    public static HashMap<String, Integer> dimensionFade = new HashMap<>();

    public static HashMap<String, List<SoundEvent>> biomeSongs = new HashMap<>();
    public static HashMap<String, List<String>> biomeSongsString = new HashMap<>();
    public static HashMap<String, Integer> biomePriorities = new HashMap<>();
    public static HashMap<String, Integer> biomeFade = new HashMap<>();
    public static HashMap<String, Integer> biomePersistence = new HashMap<>();

    public static HashMap<String, List<SoundEvent>> structureSongs = new HashMap<>();
    public static HashMap<String, List<String>> structureSongsString = new HashMap<>();
    public static HashMap<String, Integer> structurePriorities = new HashMap<>();
    public static HashMap<String, Integer> structureFade = new HashMap<>();

    public static HashMap<String, List<SoundEvent>> mobSongs = new HashMap<>();
    public static HashMap<String, List<String>> mobSongsString = new HashMap<>();
    public static HashMap<String, Integer> mobPriorities = new HashMap<>();
    public static HashMap<String, Integer> mobNumber = new HashMap<>();
    public static HashMap<String, Integer> mobRange = new HashMap<>();
    public static HashMap<String, Integer> mobFade = new HashMap<>();
    public static HashMap<String, Boolean> mobTargetting = new HashMap<>();
    public static HashMap<String, Integer> mobHordeTargetting = new HashMap<>();
    public static HashMap<String, Integer> mobHealth = new HashMap<>();
    public static HashMap<String, Integer> mobHordeHealth = new HashMap<>();
    public static HashMap<String, Integer> mobBattle = new HashMap<>();
    public static HashMap<String, Boolean> mobVictory = new HashMap<>();
    public static HashMap<String, Integer> mobVictoryID = new HashMap<>();
    public static HashMap<String, String> mobInfernalMod = new HashMap<>();

    public static HashMap<String, List<SoundEvent>> zonesSongs = new HashMap<>();
    public static HashMap<String, List<String>> zonesSongsString = new HashMap<>();
    public static HashMap<String, Integer> zonesPriorities = new HashMap<>();
    public static HashMap<String, Integer> zonesFade = new HashMap<>();

    public static HashMap<String, List<SoundEvent>> effectSongs = new HashMap<>();
    public static HashMap<String, List<String>> effectSongsString = new HashMap<>();
    public static HashMap<String, Integer> effectPriorities = new HashMap<>();
    public static HashMap<String, Integer> effectFade = new HashMap<>();

    public static HashMap<String, List<SoundEvent>> victorySongs = new HashMap<>();
    public static HashMap<String, List<String>> victorySongsString = new HashMap<>();
    public static HashMap<String, Integer> victoryID = new HashMap<>();
    public static HashMap<String, Integer> victoryTime = new HashMap<>();
    public static HashMap<String, Integer> victoryPriorities = new HashMap<>();
    public static HashMap<String, Integer> victoryFade = new HashMap<>();

    public static HashMap<String, List<SoundEvent>> guiSongs = new HashMap<>();
    public static HashMap<String, List<String>> guiSongsString = new HashMap<>();
    public static HashMap<String, Integer> guiPriorities = new HashMap<>();
    public static HashMap<String, Integer> guiFade = new HashMap<>();

    /*public static HashMap<String, List<SoundEvent>> gamestageSongsWhitelist = new HashMap<>();
    public static HashMap<String, List<String>> gamestageSongsStringWhitelist = new HashMap<>();
    public static HashMap<String, Integer> gamestagePrioritiesWhitelist = new HashMap<>();
    public static HashMap<String, Integer> gamestageFadeWhitelist = new HashMap<>();
    public static HashMap<String, List<SoundEvent>> gamestageSongsBlacklist = new HashMap<>();
    public static HashMap<String, List<String>> gamestageSongsStringBlacklist = new HashMap<>();
    public static HashMap<String, Integer> gamestagePrioritiesBlacklist = new HashMap<>();
    public static HashMap<String, Integer> gamestageFadeBlacklist = new HashMap<>();*/

    public static List<SoundEvent> bloodmoon = new ArrayList<>();
    public static List<SoundEvent> harvestmoon = new ArrayList<>();
    public static List<SoundEvent> bluemoon = new ArrayList<>();

    //public static List<SoundEvent> acidrain = new ArrayList<>();
    //public static List<SoundEvent> blizzard = new ArrayList<>();
    //public static List<SoundEvent> cloudy = new ArrayList<>();
    //public static List<SoundEvent> lightrain = new ArrayList<>();

    public static HashMap<String, List<String>> songCombos = new HashMap<>();


    public static void registerSounds() {

        if (config.menuSongs.size() != 0) {
            for (int i = 0; i < config.menuSongs.size(); i++) {
                String songName = config.menuSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("menu");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));

                menu.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"menu");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.genericSongs.size() != 0) {
            for (int i = 0; i < config.genericSongs.size(); i++) {
                String songName = config.genericSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("generic");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                generic.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"generic");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.daySongs.size() != 0) {
            for (int i = 0; i < config.daySongs.size(); i++) {
                String songName = config.daySongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("day");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                day.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"day");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.nightSongs.size() != 0) {
            for (int i = 0; i < config.nightSongs.size(); i++) {
                String[] broken = stringBreaker(config.nightSongs.get(i),",");
                List<Integer> phasesPerSong = new ArrayList<>();
                for (int j = 1; j < broken.length; j++) {
                    int phase = Integer.parseInt(broken[j].trim());
                    if (phase <= 8) {
                        phasesPerSong.add(Integer.parseInt(broken[j].trim()));
                    }
                    nightFade.put(Integer.parseInt(broken[j].trim()), Integer.parseInt(broken[broken.length - 1].trim()));
                }
                String songName = broken[0].toLowerCase().trim();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    for (int p : phasesPerSong) {
                        songCombos.get(songName).add("night" + p);
                    }
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                for (int p : phasesPerSong) {
                    nightSongs.computeIfAbsent(p, k -> new ArrayList<>());
                    nightSongsString.computeIfAbsent(p, k -> new ArrayList<>());
                    nightSongsString.get(p).add(songNamePlus);
                    nightSongs.get(p).add(sound);
                }
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"night");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.sunriseSongs.size() != 0) {
            for (int i = 0; i < config.sunriseSongs.size(); i++) {
                String songName = config.sunriseSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("sunrise");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                sunrise.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"sunrise");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.sunsetSongs.size() != 0) {
            for (int i = 0; i < config.sunsetSongs.size(); i++) {
                String songName = config.sunsetSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("sunset");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                sunset.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"sunset");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.lightSongs.size() != 0) {
            for (int i = 0; i < config.lightSongs.size(); i++) {
                String[] broken = stringBreaker(config.lightSongs.get(i),",");
                String extractedName = broken[1];
                lightLevel.put("Light-"+extractedName,Integer.parseInt(broken[2]));
                lightSky.putIfAbsent("Light-"+extractedName, true);
                if (broken.length >= 4) {
                    boolean extractedSky = Boolean.parseBoolean(broken[3]);
                    lightSky.put("Light-"+extractedName, extractedSky);
                }
                lightTime.putIfAbsent("Light-"+extractedName, 20);
                if (broken.length >= 5) {
                    int extractedTime = Integer.parseInt(broken[4]);
                    lightPriorities.put("Light-"+extractedName, extractedTime);
                }
                lightPriorities.computeIfAbsent("Light-"+extractedName, k -> config.lightPriority);
                if (broken.length >= 6) {
                    int extractedPriority = Integer.parseInt(broken[5]);
                    lightPriorities.put("Light-"+extractedName, extractedPriority);
                }
                lightFade.putIfAbsent("Light-"+extractedName, 0);
                if(broken.length==7) {
                    int extractedFade = Integer.parseInt(broken[6]);
                    lightFade.put("Light-"+extractedName, extractedFade);
                }
                String songName = broken[0].toLowerCase().trim();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("Light-"+extractedName);
                    songName = songName.substring(1);
                }
                lightSongs.computeIfAbsent("Light-"+extractedName, k -> new ArrayList<>());
                lightSongsString.computeIfAbsent("Light-"+extractedName, k -> new ArrayList<>());
                lightSongsString.get("Light-"+extractedName).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                lightSongs.get("Light-"+extractedName).add(sound);
                boolean cont = false;
                for(SoundEvent s: allSoundEvents) {
                    if(Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont=true;
                    }
                }
                if(!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"lightlevel");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.undergroundSongs.size() != 0) {
            for (int i = 0; i < config.undergroundSongs.size(); i++) {
                String songName = config.undergroundSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("underground");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                underground.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"underground");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.deepUnderSongs.size() != 0) {
            for (int i = 0; i < config.deepUnderSongs.size(); i++) {
                String songName = config.deepUnderSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("deepUnder");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                deepUnder.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"deepunder");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.rainingSongs.size() != 0) {
            for (int i = 0; i < config.rainingSongs.size(); i++) {
                String songName = config.rainingSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("raining");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                raining.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"rain");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.stormingSongs.size() != 0) {
            for (int i = 0; i < config.stormingSongs.size(); i++) {
                String songName = config.stormingSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("storming");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                storming.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"storming");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.snowingSongs.size() != 0) {
            for (int i = 0; i < config.snowingSongs.size(); i++) {
                String songName = config.snowingSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("snowing");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                snowing.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"snowing");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.lowHPSongs.size() != 0) {
            for (int i = 0; i < config.lowHPSongs.size(); i++) {
                String songName = config.lowHPSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("lowHP");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                lowHP.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"lowhp");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.deadSongs.size() != 0) {
            for (int i = 0; i < config.deadSongs.size(); i++) {
                String songName = config.deadSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("dead");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                dead.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"dead");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.inVoidSongs.size() != 0) {
            for (int i = 0; i < config.inVoidSongs.size(); i++) {
                String songName = config.inVoidSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("inVoid");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                inVoid.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"invoid");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.spectatorSongs.size() != 0) {
            for (int i = 0; i < config.spectatorSongs.size(); i++) {
                String songName = config.spectatorSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("spectator");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                spectator.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"spectator");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.creativeSongs.size() != 0) {
            for (int i = 0; i < config.creativeSongs.size(); i++) {
                String songName = config.creativeSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("creative");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                creative.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"creative");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.ridingSongs.size() != 0) {
            for (int i = 0; i < config.ridingSongs.size(); i++) {
                String songName = config.ridingSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("riding");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                riding.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"riding");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.petSongs.size() != 0) {
            for (int i = 0; i < config.petSongs.size(); i++) {
                String songName = config.petSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("pet");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                pet.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"pet");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.highSongs.size() != 0) {
            for (int i = 0; i < config.highSongs.size(); i++) {
                String songName = config.highSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("high");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                high.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"high");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.dimensionSongs.size() != 0) {
            for (int i = 0; i < config.dimensionSongs.size(); i++) {
                String[] broken = stringBreaker(config.dimensionSongs.get(i),",");
                String extractedID = broken[0].trim();
                dimensionPriorities.computeIfAbsent(extractedID, k -> config.dimensionPriority);
                if (broken.length >= 3) {
                    int extractedPriority = Integer.parseInt(broken[2].trim());
                    dimensionPriorities.put(extractedID, extractedPriority);
                }
                dimensionFade.putIfAbsent(extractedID, 0);
                if (broken.length == 4) {
                    int extractedFade = Integer.parseInt(broken[3].trim());
                    dimensionFade.put(extractedID, extractedFade);
                }
                String songName = broken[1].toLowerCase().trim();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("dimension" + extractedID);
                    songName = songName.substring(1);
                }
                dimensionSongs.computeIfAbsent(extractedID, k -> new ArrayList<>());
                dimensionSongsString.computeIfAbsent(extractedID, k -> new ArrayList<>());
                dimensionSongsString.get(extractedID).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                dimensionSongs.get(extractedID).add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"dimension");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.biomeSongs.size() != 0) {
            for (int i = 0; i < config.biomeSongs.size(); i++) {
                String[] broken = stringBreaker(config.biomeSongs.get(i),",");
                String extractedBiome = broken[0].trim();
                biomePriorities.computeIfAbsent(extractedBiome, k -> config.biomePriority);
                if (broken.length >= 3) {
                    int extractedPriority = Integer.parseInt(broken[2].trim());
                    biomePriorities.put(extractedBiome, extractedPriority);
                }
                biomeFade.putIfAbsent(extractedBiome, 0);
                if (broken.length >= 4) {
                    int extractedFade = Integer.parseInt(broken[3].trim());
                    biomeFade.put(extractedBiome, extractedFade);
                }
                biomePersistence.putIfAbsent(extractedBiome, 0);
                if(broken.length==5) {
                    int extractedPersistence = Integer.parseInt(broken[4].trim());
                    biomePersistence.put(extractedBiome, extractedPersistence);
                }
                String songName = broken[1].toLowerCase().trim();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add(extractedBiome);
                    songName = songName.substring(1);
                }
                biomeSongs.computeIfAbsent(extractedBiome, k -> new ArrayList<>());
                biomeSongsString.computeIfAbsent(extractedBiome, k -> new ArrayList<>());
                biomeSongsString.get(extractedBiome).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                biomeSongs.get(extractedBiome).add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"biome");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.structureSongs.size() != 0) {
            for (int i = 0; i < config.structureSongs.size(); i++) {
                String[] broken = stringBreaker(config.structureSongs.get(i),",");
                String extractedStructName = broken[0].trim();
                structurePriorities.computeIfAbsent(extractedStructName, k -> config.structurePriority);
                if (broken.length >= 3) {
                    int extractedPriority = Integer.parseInt(broken[2].trim());
                    structurePriorities.put(extractedStructName, extractedPriority);
                }
                structureFade.putIfAbsent(extractedStructName, 0);
                if (broken.length == 4) {
                    int extractedFade = Integer.parseInt(broken[3].trim());
                    structureFade.put(extractedStructName, extractedFade);
                }
                String songName = broken[1].toLowerCase().trim();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("structure:" + extractedStructName);
                    songName = songName.substring(1);
                }
                structureSongs.computeIfAbsent(extractedStructName, k -> new ArrayList<>());
                structureSongsString.computeIfAbsent(extractedStructName, k -> new ArrayList<>());
                structureSongsString.get(extractedStructName).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                structureSongs.get(extractedStructName).add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"structure");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.zonesSongs.size() != 0) {
            for (int i = 0; i < config.zonesSongs.size(); i++) {
                String[] broken = stringBreaker(config.zonesSongs.get(i),",");
                String extractedRange = broken[0].trim() + "," + broken[1].trim() + "," + broken[2].trim() + "," + broken[3].trim() + "," + broken[4].trim() + "," + broken[5].trim();
                zonesPriorities.computeIfAbsent(extractedRange, k -> config.zonesPriority);
                if (broken.length >= 8) {
                    int extractedPriority = Integer.parseInt(broken[7].trim());
                    zonesPriorities.put(extractedRange, extractedPriority);
                }
                zonesFade.putIfAbsent(extractedRange, config.zonesFade);
                if (broken.length == 9) {
                    int extractedFade = Integer.parseInt(broken[8].trim());
                    zonesFade.put(extractedRange, extractedFade);
                }
                String songName = broken[6].toLowerCase().trim();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("zones" + extractedRange);
                    songName = songName.substring(1);
                }
                zonesSongs.computeIfAbsent(extractedRange, k -> new ArrayList<>());
                zonesSongsString.computeIfAbsent(extractedRange, k -> new ArrayList<>());
                zonesSongsString.get(extractedRange).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                zonesSongs.get(extractedRange).add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"zone");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.mobSongs.size() != 0) {
            for (int i = 0; i < config.mobSongs.size(); i++) {
                String[] broken = stringBreaker(config.mobSongs.get(i),",");
                String extractedMobName = broken[0].trim();
                mobPriorities.computeIfAbsent(extractedMobName, k -> config.mobPriority);
                mobRange.putIfAbsent(extractedMobName, 16);
                if (broken.length >= 4) {
                    int extractedRange = Integer.parseInt(broken[3].trim());
                    mobRange.put(extractedMobName, extractedRange);
                }
                if (broken.length >= 5) {
                    int extractedPriority = Integer.parseInt(broken[4].trim());
                    mobPriorities.put(extractedMobName, extractedPriority);
                }
                mobFade.putIfAbsent(extractedMobName, 0);
                if(broken.length>=6) {
                    int extractedFade = Integer.parseInt(broken[5].trim());
                    mobFade.put(extractedMobName, extractedFade);
                }
                mobTargetting.putIfAbsent(extractedMobName,false);
                if(broken.length>=7) {
                    boolean extractedTargetting = Boolean.parseBoolean(broken[6].trim());
                    mobTargetting.put(extractedMobName, extractedTargetting);
                }
                mobHordeTargetting.putIfAbsent(extractedMobName,100);
                if(broken.length>=8) {
                    int extractedHordeTargetting = Integer.parseInt(broken[7].trim());
                    mobHordeTargetting.put(extractedMobName, extractedHordeTargetting);
                }
                mobHealth.putIfAbsent(extractedMobName,100);
                if(broken.length>=9) {
                    int extractedHealth = Integer.parseInt(broken[8].trim());
                    mobHealth.put(extractedMobName, extractedHealth);
                }
                mobHordeHealth.putIfAbsent(extractedMobName,100);
                if(broken.length>=10) {
                    int extractedHordeHealth = Integer.parseInt(broken[9].trim());
                    mobHordeHealth.put(extractedMobName, extractedHordeHealth);
                }
                mobBattle.putIfAbsent(extractedMobName,0);
                if(broken.length>=11) {
                    int extractedBattleTime = Integer.parseInt(broken[10].trim());
                    mobBattle.put(extractedMobName, extractedBattleTime);
                }
                mobVictory.putIfAbsent(extractedMobName,false);
                if(broken.length>=12) {
                    boolean extractedVictory = Boolean.parseBoolean(broken[11].trim());
                    mobVictory.put(extractedMobName, extractedVictory);
                }
                mobVictoryID.putIfAbsent(extractedMobName,100);
                if(broken.length>=13) {
                    int extractedVictoryID = Integer.parseInt(broken[12].trim());
                    mobVictoryID.put(extractedMobName, extractedVictoryID);
                }
                mobInfernalMod.putIfAbsent(extractedMobName,null);
                if(broken.length>=14) {
                    String extractedInfernalMod = broken[13];
                    mobInfernalMod.put(extractedMobName, extractedInfernalMod);
                }
                mobNumber.put(extractedMobName, Integer.parseInt(broken[1].trim()));
                String songName = broken[2].toLowerCase().trim();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add(extractedMobName);
                    songName = songName.substring(1);
                }
                mobSongs.computeIfAbsent(extractedMobName, k -> new ArrayList<>());
                mobSongsString.computeIfAbsent(extractedMobName, k -> new ArrayList<>());
                mobSongsString.get(extractedMobName).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                mobSongs.get(extractedMobName).add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"mob");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        /*if (config.gamestageSongs.size() != 0) {
            for (int i = 0; i < config.gamestageSongs.size(); i++) {
                String[] broken = stringBreaker(config.gamestageSongs.get(i),",");
                String extractedStageName = broken[0].trim();
                boolean checkWhitelist = Boolean.parseBoolean(broken[1].trim());
                String songName = broken[2].toLowerCase().trim();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add(extractedStageName + checkWhitelist);
                    songName = songName.substring(1);
                }
                SoundEvent sound;
                if (checkWhitelist) {
                    gamestagePrioritiesWhitelist.computeIfAbsent(extractedStageName, k -> config.gamestagePriority);
                    if (broken.length >= 4) {
                        int extractedPriority = Integer.parseInt(broken[3].trim());
                        gamestagePrioritiesWhitelist.put(extractedStageName, extractedPriority);
                    }
                    gamestageFadeWhitelist.putIfAbsent(extractedStageName, 0);
                    if (broken.length == 5) {
                        int extractedFade = Integer.parseInt(broken[4].trim());
                        gamestageFadeWhitelist.put(extractedStageName, extractedFade);
                    }
                    gamestageSongsWhitelist.computeIfAbsent(extractedStageName, k -> new ArrayList<>());
                    gamestageSongsStringWhitelist.computeIfAbsent(extractedStageName, k -> new ArrayList<>());
                    gamestageSongsStringWhitelist.get(extractedStageName).add(songNamePlus);
                    sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                    gamestageSongsWhitelist.get(extractedStageName).add(sound);
                } else {
                    gamestagePrioritiesBlacklist.computeIfAbsent(extractedStageName, k -> config.gamestagePriority);
                    if (broken.length >= 4) {
                        int extractedPriority = Integer.parseInt(broken[3].trim());
                        gamestagePrioritiesBlacklist.put(extractedStageName, extractedPriority);
                    }
                    gamestageFadeBlacklist.putIfAbsent(extractedStageName, 0);
                    if (broken.length == 5) {
                        int extractedFade = Integer.parseInt(broken[4].trim());
                        gamestageFadeBlacklist.put(extractedStageName, extractedFade);
                    }
                    gamestageSongsBlacklist.computeIfAbsent(extractedStageName, k -> new ArrayList<>());
                    gamestageSongsStringBlacklist.computeIfAbsent(extractedStageName, k -> new ArrayList<>());
                    gamestageSongsStringBlacklist.get(extractedStageName).add(songNamePlus);
                    sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                    gamestageSongsBlacklist.get(extractedStageName).add(sound);
                }
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }*/
        if (config.bloodmoonSongs.size() != 0) {
            for (int i = 0; i < config.bloodmoonSongs.size(); i++) {
                String songName = config.bloodmoonSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("bloodmoon");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                bloodmoon.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"bloodmooon");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.harvestmoonSongs.size() != 0) {
            for (int i = 0; i < config.harvestmoonSongs.size(); i++) {
                String songName = config.harvestmoonSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("harvestmoon");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                harvestmoon.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"harvestmoon");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.bluemoonSongs.size() != 0) {
            for (int i = 0; i < config.bluemoonSongs.size(); i++) {
                String songName = config.bluemoonSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("bluemoon");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                bluemoon.add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"bluemoon");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);

                }
            }
        }
        if (config.underwaterSongs != null) {
            for (int i = 0; i < config.underwaterSongs.size(); i++) {
                String songName = config.underwaterSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("underwater");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                underwater.add(sound);
                boolean cont = false;
                for(SoundEvent s: allSoundEvents) {
                    if(Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont=true;
                    }
                }
                if(!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"underwater");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.effectSongs != null) {
            for (int i = 0; i < config.effectSongs.size(); i++) {
                String[] broken = stringBreaker(config.effectSongs.get(i),",");
                String effectName = broken[0].trim();
                effectPriorities.computeIfAbsent(effectName, k -> config.effectPriority);
                if (broken.length >= 3) {
                    int extractedPriority = Integer.parseInt(broken[2].trim());
                    effectPriorities.put(effectName, extractedPriority);
                }
                effectFade.putIfAbsent(effectName, 0);
                if(broken.length==4) {
                    int extractedFade = Integer.parseInt(broken[3].trim());
                    effectFade.put(effectName, extractedFade);
                }
                String songName = broken[1].toLowerCase().trim();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add(effectName);
                    songName = songName.substring(1);
                }
                effectSongs.computeIfAbsent(effectName, k -> new ArrayList<>());
                effectSongsString.computeIfAbsent(effectName, k -> new ArrayList<>());
                effectSongsString.get(effectName).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                effectSongs.get(effectName).add(sound);
                boolean cont = false;
                for(SoundEvent s: allSoundEvents) {
                    if(Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont=true;
                    }
                }
                if(!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"effect");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.pvpSongs != null) {
            for (int i = 0; i < config.pvpSongs.size(); i++) {
                String songName = config.pvpSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("pvp");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                pvp.add(sound);
                boolean cont = false;
                for(SoundEvent s: allSoundEvents) {
                    if(Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont=true;
                    }
                }
                if(!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"pvp");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.victorySongs != null) {
            for (int i = 0; i < config.victorySongs.size(); i++) {
                String[] broken = stringBreaker(config.victorySongs.get(i),",");
                int extractedID = Integer.parseInt(broken[1].trim());
                victoryID.put("Victory"+extractedID,extractedID);
                victoryTime.putIfAbsent("Victory"+extractedID, 200);
                if (broken.length >= 3) {
                    int extractedTime = Integer.parseInt(broken[2].trim());
                    victoryTime.put("Victory"+extractedID, extractedTime);
                }
                victoryPriorities.computeIfAbsent("Victory"+extractedID, k -> config.victoryPriority);
                if (broken.length >= 4) {
                    int extractedPriority = Integer.parseInt(broken[3].trim());
                    victoryPriorities.put("Victory"+extractedID, extractedPriority);
                }
                victoryFade.putIfAbsent("Victory"+extractedID, 0);
                if(broken.length==5) {
                    int extractedFade = Integer.parseInt(broken[4].trim());
                    victoryFade.put("Victory"+extractedID, extractedFade);
                }
                String songName = broken[0].toLowerCase().trim();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("Victory"+extractedID);
                    songName = songName.substring(1);
                }
                victorySongs.computeIfAbsent("Victory"+extractedID, k -> new ArrayList<>());
                victorySongsString.computeIfAbsent("Victory"+extractedID, k -> new ArrayList<>());
                victorySongsString.get("Victory"+extractedID).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                victorySongs.get("Victory"+extractedID).add(sound);
                boolean cont = false;
                for(SoundEvent s: allSoundEvents) {
                    if(Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont=true;
                    }
                }
                if(!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"victory");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        /*if (config.acidrainSongs != null) {
            for (int i = 0; i < config.acidrainSongs.size(); i++) {
                String songName = config.acidrainSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("acidrain");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                acidrain.add(sound);
                boolean cont = false;
                for(SoundEvent s: allSoundEvents) {
                    if(Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont=true;
                    }
                }
                if(!cont) {
                    allSoundEvents.add(sound);
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.blizzardSongs != null) {
            for (int i = 0; i < config.blizzardSongs.size(); i++) {
                String songName = config.blizzardSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("blizzard");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                blizzard.add(sound);
                boolean cont = false;
                for(SoundEvent s: allSoundEvents) {
                    if(Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont=true;
                    }
                }
                if(!cont) {
                    allSoundEvents.add(sound);
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.cloudySongs != null) {
            for (int i = 0; i < config.cloudySongs.size(); i++) {
                String songName = config.cloudySongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("cloudy");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                cloudy.add(sound);
                boolean cont = false;
                for(SoundEvent s: allSoundEvents) {
                    if(Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont=true;
                    }
                }
                if(!cont) {
                    allSoundEvents.add(sound);
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.lightrainSongs != null) {
            for (int i = 0; i < config.lightrainSongs.size(); i++) {
                String songName = config.lightrainSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("lightrain");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                lightrain.add(sound);
                boolean cont = false;
                for(SoundEvent s: allSoundEvents) {
                    if(Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont=true;
                    }
                }
                if(!cont) {
                    allSoundEvents.add(sound);
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }*/
        if (config.guiSongs != null) {
            for (int i = 0; i < config.guiSongs.size(); i++) {
                String[] broken = stringBreaker(config.guiSongs.get(i),",");
                String extractedName = broken[0];
                guiPriorities.computeIfAbsent(extractedName, k -> config.guiPriority);
                if (broken.length >= 3) {
                    int extractedPriority = Integer.parseInt(broken[2]);
                    guiPriorities.put(extractedName, extractedPriority);
                }
                guiFade.putIfAbsent(extractedName, 0);
                if(broken.length==4) {
                    int extractedFade = Integer.parseInt(broken[3]);
                    guiFade.put(extractedName, extractedFade);
                }
                String songName = broken[1].toLowerCase().trim();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add(extractedName);
                    songName = songName.substring(1);
                }
                guiSongs.computeIfAbsent(extractedName, k -> new ArrayList<>());
                guiSongsString.computeIfAbsent(extractedName, k -> new ArrayList<>());
                guiSongsString.get(extractedName).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                guiSongs.get(extractedName).add(sound);
                boolean cont = false;
                for(SoundEvent s: allSoundEvents) {
                    if(Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont=true;
                    }
                }
                if(!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"gui");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.difficultySongs != null) {
            for (int i = 0; i < config.difficultySongs.size(); i++) {
                String[] broken = stringBreaker(config.difficultySongs.get(i), ",");
                int extractedID = Integer.parseInt(broken[1]);
                difficultyPriorities.computeIfAbsent(extractedID, k -> config.difficultyPriority);
                if (broken.length >= 3) {
                    int extractedPriority = Integer.parseInt(broken[2]);
                    difficultyPriorities.put(extractedID, extractedPriority);
                }
                difficultyFade.putIfAbsent(extractedID, 0);
                if (broken.length == 4) {
                    int extractedFade = Integer.parseInt(broken[3]);
                    difficultyFade.put(extractedID, extractedFade);
                }
                String songName = broken[0].toLowerCase().trim();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("difficulty:" + extractedID);
                    songName = songName.substring(1);
                }
                difficultySongs.computeIfAbsent(extractedID, k -> new ArrayList<>());
                difficultySongsString.computeIfAbsent(extractedID, k -> new ArrayList<>());
                difficultySongsString.get(extractedID).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName, ";")[0], "/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName, ";")[0], "/")[0]));
                difficultySongs.get(extractedID).add(sound);
                boolean cont = false;
                for (SoundEvent s : allSoundEvents) {
                    if (Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont = true;
                    }
                }
                if (!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"difficulty");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.seasonsSongs != null) {
            for (int i = 0; i < config.seasonsSongs.size(); i++) {
                String[] broken = stringBreaker(config.seasonsSongs.get(i),",");
                int extractedID = Integer.parseInt(broken[1]);
                seasonsPriorities.computeIfAbsent(extractedID, k -> config.seasonsPriority);
                if (broken.length >= 3) {
                    int extractedPriority = Integer.parseInt(broken[2]);
                    seasonsPriorities.put(extractedID, extractedPriority);
                }
                seasonsFade.putIfAbsent(extractedID, 0);
                if(broken.length==4) {
                    int extractedFade = Integer.parseInt(broken[3]);
                    seasonsFade.put(extractedID, extractedFade);
                }
                String songName = broken[0].toLowerCase().trim();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("season:"+extractedID);
                    songName = songName.substring(1);
                }
                seasonsSongs.computeIfAbsent(extractedID, k -> new ArrayList<>());
                seasonsSongsString.computeIfAbsent(extractedID, k -> new ArrayList<>());
                seasonsSongsString.get(extractedID).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                seasonsSongs.get(extractedID).add(sound);
                boolean cont = false;
                for(SoundEvent s: allSoundEvents) {
                    if(Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont=true;
                    }
                }
                if(!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"season");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.elytraSongs != null) {
            for (int i = 0; i < config.elytraSongs.size(); i++) {
                String songName = config.elytraSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("elytra");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                elytra.add(sound);
                boolean cont = false;
                for(SoundEvent s: allSoundEvents) {
                    if(Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont=true;
                    }
                }
                if(!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"elytra");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.fishingSongs != null) {
            for (int i = 0; i < config.fishingSongs.size(); i++) {
                String songName = config.fishingSongs.get(i).toLowerCase().trim();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("fishing");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"/")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, stringBreaker(stringBreaker(songName,";")[0],"/")[0]));
                fishing.add(sound);
                boolean cont = false;
                for(SoundEvent s: allSoundEvents) {
                    if(Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) {
                        cont=true;
                    }
                }
                if(!cont) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound,"fishing");
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
    }

    public static String[] stringBreaker(String s, String regex) {
        return s.split(regex);
    }
    
    public static void emptyListsAndMaps() {
        allSongs = new ArrayList<>();
        allSoundEvents = new ArrayList<>();
        allSoundEventsWithTriggers = new HashMap<>();

        menu = new ArrayList<>();
        generic = new ArrayList<>();
        day = new ArrayList<>();
        sunrise = new ArrayList<>();
        sunset = new ArrayList<>();
        lightSongs = new HashMap<>();
        lightSongsString = new HashMap<>();
        lightLevel = new HashMap<>();
        lightSky = new HashMap<>();
        lightTime = new HashMap<>();
        lightPriorities = new HashMap<>();
        lightFade = new HashMap<>();
        underground = new ArrayList<>();
        deepUnder = new ArrayList<>();
        raining = new ArrayList<>();
        storming = new ArrayList<>();
        snowing = new ArrayList<>();
        lowHP = new ArrayList<>();
        dead = new ArrayList<>();
        inVoid = new ArrayList<>();
        spectator = new ArrayList<>();
        creative = new ArrayList<>();
        riding = new ArrayList<>();
        pet = new ArrayList<>();
        high = new ArrayList<>();
        underwater = new ArrayList<>();
        pvp = new ArrayList<>();
        elytra = new ArrayList<>();
        fishing = new ArrayList<>();

        difficultySongs = new HashMap<>();
        difficultySongsString = new HashMap<>();
        difficultyPriorities = new HashMap<>();
        difficultyFade = new HashMap<>();

        seasonsSongs = new HashMap<>();
        seasonsSongsString = new HashMap<>();
        seasonsPriorities = new HashMap<>();
        seasonsFade = new HashMap<>();

        nightSongs = new HashMap<>();
        nightSongsString = new HashMap<>();
        nightFade = new HashMap<>();

        dimensionSongs = new HashMap<>();
        dimensionSongsString = new HashMap<>();
        dimensionPriorities = new HashMap<>();
        dimensionFade = new HashMap<>();

        biomeSongs = new HashMap<>();
        biomeSongsString = new HashMap<>();
        biomePriorities = new HashMap<>();
        biomeFade = new HashMap<>();
        biomePersistence = new HashMap<>();

        structureSongs = new HashMap<>();
        structureSongsString = new HashMap<>();
        structurePriorities = new HashMap<>();
        structureFade = new HashMap<>();

        mobSongs = new HashMap<>();
        mobSongsString = new HashMap<>();
        mobPriorities = new HashMap<>();
        mobNumber = new HashMap<>();
        mobRange = new HashMap<>();
        mobFade = new HashMap<>();
        mobTargetting = new HashMap<>();
        mobHordeTargetting = new HashMap<>();
        mobHealth = new HashMap<>();
        mobHordeHealth = new HashMap<>();
        mobBattle = new HashMap<>();
        mobVictory = new HashMap<>();
        mobVictoryID = new HashMap<>();
        mobInfernalMod = new HashMap<>();

        zonesSongs = new HashMap<>();
        zonesSongsString = new HashMap<>();
        zonesPriorities = new HashMap<>();
        zonesFade = new HashMap<>();

        effectSongs = new HashMap<>();
        effectSongsString = new HashMap<>();
        effectPriorities = new HashMap<>();
        effectFade = new HashMap<>();

        victorySongs = new HashMap<>();
        victorySongsString = new HashMap<>();
        victoryID = new HashMap<>();
        victoryTime = new HashMap<>();
        victoryPriorities = new HashMap<>();
        victoryFade = new HashMap<>();

        guiSongs = new HashMap<>();
        guiSongsString = new HashMap<>();
        guiPriorities = new HashMap<>();
        guiFade = new HashMap<>();

        /*gamestageSongsWhitelist = new HashMap<>();
        gamestageSongsStringWhitelist = new HashMap<>();
        gamestagePrioritiesWhitelist = new HashMap<>();
        gamestageFadeWhitelist = new HashMap<>();
        gamestageSongsBlacklist = new HashMap<>();
        gamestageSongsStringBlacklist = new HashMap<>();
        gamestagePrioritiesBlacklist = new HashMap<>();
        gamestageFadeBlacklist = new HashMap<>();*/

        bloodmoon = new ArrayList<>();
        harvestmoon = new ArrayList<>();
        bluemoon = new ArrayList<>();

        //acidrain = new ArrayList<>();
        //blizzard = new ArrayList<>();
        //cloudy = new ArrayList<>();
        //lightrain = new ArrayList<>();

        songCombos = new HashMap<>();
    }
}
