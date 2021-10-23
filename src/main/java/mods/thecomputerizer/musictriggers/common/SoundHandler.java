package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.config;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class SoundHandler {

    public static List<String> allSongs = new ArrayList<>();
    public static final List<SoundEvent> allSoundEvents = new ArrayList<>();

    public static List<SoundEvent> menu = new ArrayList<>();
    public static List<SoundEvent> generic = new ArrayList<>();
    public static List<SoundEvent> day = new ArrayList<>();
    public static List<SoundEvent> sunrise = new ArrayList<>();
    public static List<SoundEvent> sunset = new ArrayList<>();
    public static List<SoundEvent> light = new ArrayList<>();
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

    public static HashMap<String, List<SoundEvent>> zonesSongs = new HashMap<>();
    public static HashMap<String, List<String>> zonesSongsString = new HashMap<>();
    public static HashMap<String, Integer> zonesPriorities = new HashMap<>();
    public static HashMap<String, Integer> zonesFade = new HashMap<>();

    public static HashMap<String, List<SoundEvent>> gamestageSongsWhitelist = new HashMap<>();
    public static HashMap<String, List<String>> gamestageSongsStringWhitelist = new HashMap<>();
    public static HashMap<String, Integer> gamestagePrioritiesWhitelist = new HashMap<>();
    public static HashMap<String, Integer> gamestageFadeWhitelist = new HashMap<>();
    public static HashMap<String, List<SoundEvent>> gamestageSongsBlacklist = new HashMap<>();
    public static HashMap<String, List<String>> gamestageSongsStringBlacklist = new HashMap<>();
    public static HashMap<String, Integer> gamestagePrioritiesBlacklist = new HashMap<>();
    public static HashMap<String, Integer> gamestageFadeBlacklist = new HashMap<>();

    public static List<SoundEvent> bloodmoon = new ArrayList<>();
    public static List<SoundEvent> harvestmoon = new ArrayList<>();
    public static List<SoundEvent> bluemoon = new ArrayList<>();


    public static HashMap<String, List<String>> songCombos = new HashMap<>();


    public static void registerSounds() {

        if (config.menuSongs.size() != 0) {
            for (int i = 0; i < config.menuSongs.size(); i++) {
                String songName = config.menuSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("menu");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));

                menu.add(sound);
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
        }
        if (config.genericSongs.size() != 0) {
            for (int i = 0; i < config.genericSongs.size(); i++) {
                String songName = config.genericSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("generic");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                generic.add(sound);
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
        }
        if (config.daySongs.size() != 0) {
            for (int i = 0; i < config.daySongs.size(); i++) {
                String songName = config.daySongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("day");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                day.add(sound);
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
        }
        if (config.nightSongs.size() != 0) {
            for (int i = 0; i < config.nightSongs.size(); i++) {
                String[] broken = stringBreaker(config.nightSongs.get(i));
                List<Integer> phasesPerSong = new ArrayList<>();
                for (int j = 1; j < broken.length; j++) {
                    int phase = Integer.parseInt(broken[j]);
                    if (phase <= 8) {
                        phasesPerSong.add(Integer.parseInt(broken[j]));
                    }
                    nightFade.put(Integer.parseInt(broken[j]), Integer.parseInt(broken[broken.length - 1]));
                }
                String songName = broken[0].toLowerCase();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    for (int p : phasesPerSong) {
                        songCombos.get(songName).add("night" + p);
                    }
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                }
                if (!allSongs.contains(songName)) {
                    allSongs.add(songName);
                }
            }
        }
        if (config.sunriseSongs.size() != 0) {
            for (int i = 0; i < config.sunriseSongs.size(); i++) {
                String songName = config.sunriseSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("sunrise");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                sunrise.add(sound);
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
        }
        if (config.sunsetSongs.size() != 0) {
            for (int i = 0; i < config.sunsetSongs.size(); i++) {
                String songName = config.sunsetSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("sunset");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                sunset.add(sound);
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
        }
        if (config.lightSongs.size() != 0) {
            for (int i = 0; i < config.lightSongs.size(); i++) {
                String songName = config.lightSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("light");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                light.add(sound);
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
        }
        if (config.undergroundSongs.size() != 0) {
            for (int i = 0; i < config.undergroundSongs.size(); i++) {
                String songName = config.undergroundSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("underground");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                underground.add(sound);
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
        }
        if (config.deepUnderSongs.size() != 0) {
            for (int i = 0; i < config.deepUnderSongs.size(); i++) {
                String songName = config.deepUnderSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("deepUnder");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                deepUnder.add(sound);
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
        }
        if (config.rainingSongs.size() != 0) {
            for (int i = 0; i < config.rainingSongs.size(); i++) {
                String songName = config.rainingSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("raining");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                raining.add(sound);
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
        }
        if (config.stormingSongs.size() != 0) {
            for (int i = 0; i < config.stormingSongs.size(); i++) {
                String songName = config.stormingSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("storming");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                storming.add(sound);
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
        }
        if (config.snowingSongs.size() != 0) {
            for (int i = 0; i < config.snowingSongs.size(); i++) {
                String songName = config.snowingSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("snowing");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                snowing.add(sound);
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
        }
        if (config.lowHPSongs.size() != 0) {
            for (int i = 0; i < config.lowHPSongs.size(); i++) {
                String songName = config.lowHPSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("lowHP");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                lowHP.add(sound);
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
        }
        if (config.deadSongs.size() != 0) {
            for (int i = 0; i < config.deadSongs.size(); i++) {
                String songName = config.deadSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("dead");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                dead.add(sound);
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
        }
        if (config.inVoidSongs.size() != 0) {
            for (int i = 0; i < config.inVoidSongs.size(); i++) {
                String songName = config.inVoidSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("inVoid");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                inVoid.add(sound);
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
        }
        if (config.spectatorSongs.size() != 0) {
            for (int i = 0; i < config.spectatorSongs.size(); i++) {
                String songName = config.spectatorSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("spectator");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                spectator.add(sound);
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
        }
        if (config.creativeSongs.size() != 0) {
            for (int i = 0; i < config.creativeSongs.size(); i++) {
                String songName = config.creativeSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("creative");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                creative.add(sound);
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
        }
        if (config.ridingSongs.size() != 0) {
            for (int i = 0; i < config.ridingSongs.size(); i++) {
                String songName = config.ridingSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("riding");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                riding.add(sound);
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
        }
        if (config.petSongs.size() != 0) {
            for (int i = 0; i < config.petSongs.size(); i++) {
                String songName = config.petSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("pet");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                pet.add(sound);
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
        }
        if (config.highSongs.size() != 0) {
            for (int i = 0; i < config.highSongs.size(); i++) {
                String songName = config.highSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("high");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                high.add(sound);
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
        }
        if (config.dimensionSongs.size() != 0) {
            for (int i = 0; i < config.dimensionSongs.size(); i++) {
                String[] broken = stringBreaker(config.dimensionSongs.get(i));
                String extractedID = broken[0];
                dimensionPriorities.computeIfAbsent(extractedID, k -> config.dimensionPriority);
                if (broken.length >= 3) {
                    int extractedPriority = Integer.parseInt(broken[2]);
                    dimensionPriorities.put(extractedID, extractedPriority);
                }
                dimensionFade.putIfAbsent(extractedID, 0);
                if (broken.length == 4) {
                    int extractedFade = Integer.parseInt(broken[3]);
                    dimensionFade.put(extractedID, extractedFade);
                }
                String songName = broken[1].toLowerCase();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("dimension" + extractedID);
                    songName = songName.substring(1);
                }
                dimensionSongs.computeIfAbsent(extractedID, k -> new ArrayList<>());
                dimensionSongsString.computeIfAbsent(extractedID, k -> new ArrayList<>());
                dimensionSongsString.get(extractedID).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                dimensionSongs.get(extractedID).add(sound);
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
        }
        if (config.biomeSongs.size() != 0) {
            for (int i = 0; i < config.biomeSongs.size(); i++) {
                String[] broken = stringBreaker(config.biomeSongs.get(i));
                String extractedBiome = broken[0];
                biomePriorities.computeIfAbsent(extractedBiome, k -> config.biomePriority);
                if (broken.length >= 3) {
                    int extractedPriority = Integer.parseInt(broken[2]);
                    biomePriorities.put(extractedBiome, extractedPriority);
                }
                biomeFade.putIfAbsent(extractedBiome, 0);
                if (broken.length == 4) {
                    int extractedFade = Integer.parseInt(broken[3]);
                    biomeFade.put(extractedBiome, extractedFade);
                }
                String songName = broken[1].toLowerCase();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add(extractedBiome);
                    songName = songName.substring(1);
                }
                biomeSongs.computeIfAbsent(extractedBiome, k -> new ArrayList<>());
                biomeSongsString.computeIfAbsent(extractedBiome, k -> new ArrayList<>());
                biomeSongsString.get(extractedBiome).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                biomeSongs.get(extractedBiome).add(sound);
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
        }
        if (config.structureSongs.size() != 0) {
            for (int i = 0; i < config.structureSongs.size(); i++) {
                String[] broken = stringBreaker(config.structureSongs.get(i));
                String extractedStructName = broken[0];
                structurePriorities.computeIfAbsent(extractedStructName, k -> config.structurePriority);
                if (broken.length >= 3) {
                    int extractedPriority = Integer.parseInt(broken[2]);
                    structurePriorities.put(extractedStructName, extractedPriority);
                }
                structureFade.putIfAbsent(extractedStructName, 0);
                if (broken.length == 4) {
                    int extractedFade = Integer.parseInt(broken[3]);
                    structureFade.put(extractedStructName, extractedFade);
                }
                String songName = broken[1].toLowerCase();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("structure:" + extractedStructName);
                    songName = songName.substring(1);
                }
                structureSongs.computeIfAbsent(extractedStructName, k -> new ArrayList<>());
                structureSongsString.computeIfAbsent(extractedStructName, k -> new ArrayList<>());
                structureSongsString.get(extractedStructName).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                structureSongs.get(extractedStructName).add(sound);
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
        }
        if (config.zonesSongs.size() != 0) {
            for (int i = 0; i < config.zonesSongs.size(); i++) {
                String[] broken = stringBreaker(config.zonesSongs.get(i));
                String extractedRange = broken[0] + "," + broken[1] + "," + broken[2] + "," + broken[3] + "," + broken[4] + "," + broken[5];
                zonesPriorities.computeIfAbsent(extractedRange, k -> config.zonesPriority);
                if (broken.length >= 8) {
                    int extractedPriority = Integer.parseInt(broken[7]);
                    zonesPriorities.put(extractedRange, extractedPriority);
                }
                zonesFade.putIfAbsent(extractedRange, config.zonesFade);
                if (broken.length == 9) {
                    int extractedFade = Integer.parseInt(broken[8]);
                    zonesFade.put(extractedRange, extractedFade);
                }
                String songName = broken[6].toLowerCase();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("zones" + extractedRange);
                    songName = songName.substring(1);
                }
                zonesSongs.computeIfAbsent(extractedRange, k -> new ArrayList<>());
                zonesSongsString.computeIfAbsent(extractedRange, k -> new ArrayList<>());
                zonesSongsString.get(extractedRange).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                zonesSongs.get(extractedRange).add(sound);
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
        }
        if (config.mobSongs.size() != 0) {
            for (int i = 0; i < config.mobSongs.size(); i++) {
                String[] broken = stringBreaker(config.mobSongs.get(i));
                String extractedMobName = broken[0];
                mobPriorities.computeIfAbsent(extractedMobName, k -> config.mobPriority);
                mobRange.putIfAbsent(extractedMobName, 16);
                if (broken.length >= 4) {
                    int extractedRange = Integer.parseInt(broken[3]);
                    mobRange.put(extractedMobName, extractedRange);
                }
                if (broken.length >= 5) {
                    int extractedPriority = Integer.parseInt(broken[4]);
                    mobPriorities.put(extractedMobName, extractedPriority);
                }
                mobFade.putIfAbsent(extractedMobName, 0);
                if (broken.length == 6) {
                    int extractedFade = Integer.parseInt(broken[5]);
                    mobFade.put(extractedMobName, extractedFade);
                }
                mobNumber.put(extractedMobName, Integer.parseInt(broken[1]));
                String songName = broken[2].toLowerCase();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add(extractedMobName);
                    songName = songName.substring(1);
                }
                mobSongs.computeIfAbsent(extractedMobName, k -> new ArrayList<>());
                mobSongsString.computeIfAbsent(extractedMobName, k -> new ArrayList<>());
                mobSongsString.get(extractedMobName).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                mobSongs.get(extractedMobName).add(sound);
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
        }
        if (config.gamestageSongs.size() != 0) {
            for (int i = 0; i < config.gamestageSongs.size(); i++) {
                String[] broken = stringBreaker(config.gamestageSongs.get(i));
                String extractedStageName = broken[0];
                boolean checkWhitelist = Boolean.parseBoolean(broken[1]);
                String songName = broken[2].toLowerCase();
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
                        int extractedPriority = Integer.parseInt(broken[3]);
                        gamestagePrioritiesWhitelist.put(extractedStageName, extractedPriority);
                    }
                    gamestageFadeWhitelist.putIfAbsent(extractedStageName, 0);
                    if (broken.length == 5) {
                        int extractedFade = Integer.parseInt(broken[4]);
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
                        int extractedPriority = Integer.parseInt(broken[3]);
                        gamestagePrioritiesBlacklist.put(extractedStageName, extractedPriority);
                    }
                    gamestageFadeBlacklist.putIfAbsent(extractedStageName, 0);
                    if (broken.length == 5) {
                        int extractedFade = Integer.parseInt(broken[4]);
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
        }
        if (config.bloodmoonSongs.size() != 0) {
            for (int i = 0; i < config.bloodmoonSongs.size(); i++) {
                String songName = config.bloodmoonSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("bloodmoon");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                bloodmoon.add(sound);
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
        }
        if (config.harvestmoonSongs.size() != 0) {
            for (int i = 0; i < config.harvestmoonSongs.size(); i++) {
                String songName = config.harvestmoonSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("harvestmoon");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                harvestmoon.add(sound);
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
        }
        if (config.bluemoonSongs.size() != 0) {
            for (int i = 0; i < config.bluemoonSongs.size(); i++) {
                String songName = config.bluemoonSongs.get(i).toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("fallingstars");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                bluemoon.add(sound);
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
        }
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
