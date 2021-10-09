package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.config;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.EnumHelperClient;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;

import java.util.*;

public class SoundHandler {

    public static HashMap<String, PositionedSoundRecord> songsRecords = new HashMap<>();

    public static List<String> allSongs = new ArrayList<>();
    public static final List<SoundEvent> allSoundEvents = new ArrayList<>();

    public static List<SoundEvent> menu = new ArrayList<>();
    public static List<SoundEvent> generic = new ArrayList<>();
    public static List<SoundEvent> day = new ArrayList<>();
    public static List<SoundEvent> night = new ArrayList<>();
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

    public static HashMap<Integer, List<SoundEvent>> dimensionSongs = new HashMap<>();
    public static HashMap<Integer, List<String>> dimensionSongsString = new HashMap<>();
    public static HashMap<Integer, Integer> dimensionPriorities = new HashMap<>();
    public static HashMap<Integer, Integer> dimensionFade = new HashMap<>();

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

    public static HashMap<String, List<SoundEvent>> gamestageSongsWhitelist = new HashMap<>();
    public static HashMap<String, List<String>> gamestageSongsStringWhitelist = new HashMap<>();
    public static HashMap<String, Integer> gamestagePrioritiesWhitelist = new HashMap<>();
    public static HashMap<String, Integer> gamestageFadeWhitelist = new HashMap<>();
    public static HashMap<String, List<SoundEvent>> gamestageSongsBlacklist = new HashMap<>();
    public static HashMap<String, List<String>> gamestageSongsStringBlacklist = new HashMap<>();
    public static HashMap<String, Integer> gamestagePrioritiesBlacklist = new HashMap<>();
    public static HashMap<String, Integer> gamestageFadeBlacklist = new HashMap<>();

    public static HashMap<String, List<String>> songCombos = new HashMap<>();


    public static void registerSounds() {

        if (config.menu.menuSongs != null) {
            for (int i = 0; i < config.menu.menuSongs.length; i++) {
                String songName = config.menu.menuSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("menu");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                menu.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        EnumHelperClient.addMusicType(songName, sound, 0, 0);
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.generic.genericSongs != null) {
            for (int i = 0; i < config.generic.genericSongs.length; i++) {
                String songName = config.generic.genericSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("generic");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                generic.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.day.daySongs != null) {
            for (int i = 0; i < config.day.daySongs.length; i++) {
                String songName = config.day.daySongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("day");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                day.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.night.nightSongs != null) {
            for (int i = 0; i < config.night.nightSongs.length; i++) {
                String songName = config.night.nightSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("night");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                night.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.sunrise.sunriseSongs != null) {
            for (int i = 0; i < config.sunrise.sunriseSongs.length; i++) {
                String songName = config.sunrise.sunriseSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("sunrise");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                sunrise.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.sunset.sunsetSongs != null) {
            for (int i = 0; i < config.sunset.sunsetSongs.length; i++) {
                String songName = config.sunset.sunsetSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("sunset");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                sunset.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.light.lightSongs != null) {
            for (int i = 0; i < config.light.lightSongs.length; i++) {
                String songName = config.light.lightSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("light");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                light.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.underground.undergroundSongs != null) {
            for (int i = 0; i < config.underground.undergroundSongs.length; i++) {
                String songName = config.underground.undergroundSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("underground");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                underground.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.deepUnder.deepUnderSongs != null) {
            for (int i = 0; i < config.deepUnder.deepUnderSongs.length; i++) {
                String songName = config.deepUnder.deepUnderSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("deepUnder");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                deepUnder.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.raining.rainingSongs != null) {
            for (int i = 0; i < config.raining.rainingSongs.length; i++) {
                String songName = config.raining.rainingSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("raining");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                raining.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.storming.stormingSongs != null) {
            for (int i = 0; i < config.storming.stormingSongs.length; i++) {
                String songName = config.storming.stormingSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("storming");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                storming.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.snowing.snowingSongs != null) {
            for (int i = 0; i < config.snowing.snowingSongs.length; i++) {
                String songName = config.snowing.snowingSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("snowing");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                snowing.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.lowHP.lowHPSongs != null) {
            for (int i = 0; i < config.lowHP.lowHPSongs.length; i++) {
                String songName = config.lowHP.lowHPSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("lowHP");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                lowHP.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.dead.deadSongs != null) {
            for (int i = 0; i < config.dead.deadSongs.length; i++) {
                String songName = config.dead.deadSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("dead");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                dead.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.inVoid.inVoidSongs != null) {
            for (int i = 0; i < config.inVoid.inVoidSongs.length; i++) {
                String songName = config.inVoid.inVoidSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("inVoid");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                inVoid.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.spectator.spectatorSongs != null) {
            for (int i = 0; i < config.spectator.spectatorSongs.length; i++) {
                String songName = config.spectator.spectatorSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("spectator");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                spectator.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.creative.creativeSongs != null) {
            for (int i = 0; i < config.creative.creativeSongs.length; i++) {
                String songName = config.creative.creativeSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("creative");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                creative.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.riding.ridingSongs != null) {
            for (int i = 0; i < config.riding.ridingSongs.length; i++) {
                String songName = config.riding.ridingSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("riding");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                riding.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.pet.petSongs != null) {
            for (int i = 0; i < config.pet.petSongs.length; i++) {
                String songName = config.pet.petSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("pet");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                pet.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.high.highSongs != null) {
            for (int i = 0; i < config.high.highSongs.length; i++) {
                String songName = config.high.highSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("high");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                high.add(sound);
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.dimension.dimensionSongs != null) {
            for (int i = 0; i < config.dimension.dimensionSongs.length; i++) {
                String[] broken = stringBreaker(config.dimension.dimensionSongs[i]);
                int extractedID = Integer.parseInt(broken[0]);
                dimensionPriorities.computeIfAbsent(extractedID, k -> config.dimension.dimensionPriority);
                if (broken.length >= 3) {
                    int extractedPriority = Integer.parseInt(broken[2]);
                    dimensionPriorities.put(extractedID, extractedPriority);
                }
                dimensionFade.putIfAbsent(extractedID, 0);
                if(broken.length==4) {
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.biome.biomeSongs != null) {
            for (int i = 0; i < config.biome.biomeSongs.length; i++) {
                String[] broken = stringBreaker(config.biome.biomeSongs[i]);
                String extractedBiome = broken[0];
                biomePriorities.computeIfAbsent(extractedBiome, k -> config.biome.biomePriority);
                if (broken.length >= 3) {
                    int extractedPriority = Integer.parseInt(broken[2]);
                    biomePriorities.put(extractedBiome, extractedPriority);
                }
                biomeFade.putIfAbsent(extractedBiome, 0);
                if(broken.length==4) {
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.structure.structureSongs != null) {
            for (int i = 0; i < config.structure.structureSongs.length; i++) {
                String[] broken = stringBreaker(config.structure.structureSongs[i]);
                String extractedStructName = broken[0];
                structurePriorities.computeIfAbsent(extractedStructName, k -> config.structure.structurePriority);
                if (broken.length >= 3) {
                    int extractedPriority = Integer.parseInt(broken[2]);
                    structurePriorities.put(extractedStructName, extractedPriority);
                }
                structureFade.putIfAbsent(extractedStructName, 0);
                if(broken.length==4) {
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.mob.mobSongs != null) {
            for (int i = 0; i < config.mob.mobSongs.length; i++) {
                String[] broken = stringBreaker(config.mob.mobSongs[i]);
                String extractedMobName = broken[0];
                mobPriorities.computeIfAbsent(extractedMobName, k -> config.mob.mobPriority);
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
                if(broken.length==6) {
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.gamestage.gamestageSongs != null) {
            for (int i = 0; i < config.gamestage.gamestageSongs.length; i++) {
                String[] broken = stringBreaker(config.gamestage.gamestageSongs[i]);
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
                    gamestagePrioritiesWhitelist.computeIfAbsent(extractedStageName, k -> config.gamestage.gamestagePriority);
                    if (broken.length >= 4) {
                        int extractedPriority = Integer.parseInt(broken[3]);
                        gamestagePrioritiesWhitelist.put(extractedStageName, extractedPriority);
                    }
                    gamestageFadeWhitelist.putIfAbsent(extractedStageName, 0);
                    if(broken.length==5) {
                        int extractedFade = Integer.parseInt(broken[4]);
                        gamestageFadeWhitelist.put(extractedStageName, extractedFade);
                    }
                    gamestageSongsWhitelist.computeIfAbsent(extractedStageName, k -> new ArrayList<>());
                    gamestageSongsStringWhitelist.computeIfAbsent(extractedStageName, k -> new ArrayList<>());
                    gamestageSongsStringWhitelist.get(extractedStageName).add(songNamePlus);
                    sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                    gamestageSongsWhitelist.get(extractedStageName).add(sound);
                } else {
                    gamestagePrioritiesBlacklist.computeIfAbsent(extractedStageName, k -> config.gamestage.gamestagePriority);
                    if (broken.length >= 4) {
                        int extractedPriority = Integer.parseInt(broken[3]);
                        gamestagePrioritiesBlacklist.put(extractedStageName, extractedPriority);
                    }
                    gamestageFadeBlacklist.putIfAbsent(extractedStageName, 0);
                    if(broken.length==5) {
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
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
