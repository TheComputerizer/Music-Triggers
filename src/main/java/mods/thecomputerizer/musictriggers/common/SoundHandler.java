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
    public static List<SoundEvent> allSoundEvents = new ArrayList<>();

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

    public static HashMap<Integer, List<SoundEvent>> nightSongs = new HashMap<>();
    public static HashMap<Integer, List<String>> nightSongsString = new HashMap<>();
    public static HashMap<Integer, Integer> nightFade = new HashMap<>();

    public static HashMap<Integer, List<SoundEvent>> dimensionSongs = new HashMap<>();
    public static HashMap<Integer, List<String>> dimensionSongsString = new HashMap<>();
    public static HashMap<Integer, Integer> dimensionPriorities = new HashMap<>();
    public static HashMap<Integer, Integer> dimensionFade = new HashMap<>();

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
    public static List<SoundEvent> fallingstars = new ArrayList<>();

    public static HashMap<Integer, List<SoundEvent>> rainintensitySongs = new HashMap<>();
    public static HashMap<Integer, List<String>> rainintensitySongsString = new HashMap<>();

    public static HashMap<String, List<SoundEvent>> tornadoSongs = new HashMap<>();
    public static HashMap<String, List<String>> tornadoSongsString = new HashMap<>();
    public static HashMap<String, Integer> tornadoIntensity = new HashMap<>();
    public static HashMap<String, Integer> tornadoPriorities = new HashMap<>();
    public static HashMap<String, Integer> tornadoFade = new HashMap<>();
    public static List<SoundEvent> hurricane = new ArrayList<>();
    public static List<SoundEvent> sandstorm = new ArrayList<>();

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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.night.nightSongs != null) {
            for (int i = 0; i < config.night.nightSongs.length; i++) {
                String[] broken = stringBreaker(config.night.nightSongs[i],",");
                List<Integer> phasesPerSong = new ArrayList<>();
                for(int j=1; j<broken.length; j++) {
                    int phase = Integer.parseInt(broken[j]);
                    if(phase<=8) {
                        phasesPerSong.add(Integer.parseInt(broken[j]));
                    }
                    nightFade.put(Integer.parseInt(broken[j]), Integer.parseInt(broken[broken.length-1]));
                }
                String songName = broken[0].toLowerCase();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    for(int p : phasesPerSong) {
                        songCombos.get(songName).add("night" + p);
                    }
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                for(int p : phasesPerSong) {
                    nightSongs.computeIfAbsent(p, k -> new ArrayList<>());
                    nightSongsString.computeIfAbsent(p, k -> new ArrayList<>());
                    nightSongsString.get(p).add(songNamePlus);
                    nightSongs.get(p).add(sound);
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.light.lightSongs != null) {
            for (int i = 0; i < config.light.lightSongs.length; i++) {
                String[] broken = stringBreaker(config.light.lightSongs[i],",");
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
                lightPriorities.computeIfAbsent("Light-"+extractedName, k -> config.light.lightPriority);
                if (broken.length >= 6) {
                    int extractedPriority = Integer.parseInt(broken[5]);
                    lightPriorities.put("Light-"+extractedName, extractedPriority);
                }
                lightFade.putIfAbsent("Light-"+extractedName, 0);
                if(broken.length==7) {
                    int extractedFade = Integer.parseInt(broken[6]);
                    lightFade.put("Light-"+extractedName, extractedFade);
                }
                String songName = broken[0].toLowerCase();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("Light-"+extractedName);
                    songName = songName.substring(1);
                }
                lightSongs.computeIfAbsent("Light-"+extractedName, k -> new ArrayList<>());
                lightSongsString.computeIfAbsent("Light-"+extractedName, k -> new ArrayList<>());
                lightSongsString.get("Light-"+extractedName).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                lightSongs.get("Light-"+extractedName).add(sound);
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.dimension.dimensionSongs != null) {
            for (int i = 0; i < config.dimension.dimensionSongs.length; i++) {
                String[] broken = stringBreaker(config.dimension.dimensionSongs[i],",");
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.biome.biomeSongs != null) {
            for (int i = 0; i < config.biome.biomeSongs.length; i++) {
                String[] broken = stringBreaker(config.biome.biomeSongs[i],",");
                String extractedBiome = broken[0];
                biomePriorities.computeIfAbsent(extractedBiome, k -> config.biome.biomePriority);
                if (broken.length >= 3) {
                    int extractedPriority = Integer.parseInt(broken[2]);
                    biomePriorities.put(extractedBiome, extractedPriority);
                }
                biomeFade.putIfAbsent(extractedBiome, 0);
                if(broken.length>=4) {
                    int extractedFade = Integer.parseInt(broken[3]);
                    biomeFade.put(extractedBiome, extractedFade);
                }
                biomePersistence.putIfAbsent(extractedBiome, 0);
                if(broken.length==5) {
                    int extractedPersistence = Integer.parseInt(broken[4]);
                    biomePersistence.put(extractedBiome, extractedPersistence);
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.structure.structureSongs != null) {
            for (int i = 0; i < config.structure.structureSongs.length; i++) {
                String[] broken = stringBreaker(config.structure.structureSongs[i],",");
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.zones.zonesSongs != null) {
            for (int i = 0; i < config.zones.zonesSongs.length; i++) {
                String[] broken = stringBreaker(config.zones.zonesSongs[i],",");
                String extractedRange = broken[0]+","+broken[1]+","+broken[2]+","+broken[3]+","+broken[4]+","+broken[5];
                zonesPriorities.computeIfAbsent(extractedRange, k -> config.zones.zonesPriority);
                if (broken.length >= 8) {
                    int extractedPriority = Integer.parseInt(broken[7]);
                    zonesPriorities.put(extractedRange, extractedPriority);
                }
                zonesFade.putIfAbsent(extractedRange, config.zones.zonesFade);
                if(broken.length==9) {
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                zonesSongs.get(extractedRange).add(sound);
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.mob.mobSongs != null) {
            for (int i = 0; i < config.mob.mobSongs.length; i++) {
                String[] broken = stringBreaker(config.mob.mobSongs[i],",");
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
                if(broken.length>=6) {
                    int extractedFade = Integer.parseInt(broken[5]);
                    mobFade.put(extractedMobName, extractedFade);
                }
                mobTargetting.putIfAbsent(extractedMobName,false);
                if(broken.length>=7) {
                    boolean extractedTargetting = Boolean.parseBoolean(broken[6]);
                    mobTargetting.put(extractedMobName, extractedTargetting);
                }
                mobHordeTargetting.putIfAbsent(extractedMobName,100);
                if(broken.length>=8) {
                    int extractedHordeTargetting = Integer.parseInt(broken[7]);
                    mobHordeTargetting.put(extractedMobName, extractedHordeTargetting);
                }
                mobHealth.putIfAbsent(extractedMobName,100);
                if(broken.length>=9) {
                    int extractedHealth = Integer.parseInt(broken[8]);
                    mobHealth.put(extractedMobName, extractedHealth);
                }
                mobHordeHealth.putIfAbsent(extractedMobName,100);
                if(broken.length>=10) {
                    int extractedHordeHealth = Integer.parseInt(broken[9]);
                    mobHordeHealth.put(extractedMobName, extractedHordeHealth);
                }
                mobBattle.putIfAbsent(extractedMobName,0);
                if(broken.length>=11) {
                    int extractedBattleTime = Integer.parseInt(broken[10]);
                    mobBattle.put(extractedMobName, extractedBattleTime);
                }
                mobVictory.putIfAbsent(extractedMobName,false);
                if(broken.length>=12) {
                    boolean extractedVictory = Boolean.parseBoolean(broken[11]);
                    mobVictory.put(extractedMobName, extractedVictory);
                }
                mobVictoryID.putIfAbsent(extractedMobName,100);
                if(broken.length>=13) {
                    int extractedVictoryID = Integer.parseInt(broken[12]);
                    mobVictoryID.put(extractedMobName, extractedVictoryID);
                }
                mobInfernalMod.putIfAbsent(extractedMobName,null);
                if(broken.length>=14) {
                    String extractedInfernalMod = broken[13];
                    mobInfernalMod.put(extractedMobName, extractedInfernalMod);
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
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.gamestage.gamestageSongs != null) {
            for (int i = 0; i < config.gamestage.gamestageSongs.length; i++) {
                String[] broken = stringBreaker(config.gamestage.gamestageSongs[i],",");
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
                    sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                    sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.bloodmoon.bloodmoonSongs != null) {
            for (int i = 0; i < config.bloodmoon.bloodmoonSongs.length; i++) {
                String songName = config.bloodmoon.bloodmoonSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("bloodmoon");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                bloodmoon.add(sound);
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.harvestmoon.harvestmoonSongs != null) {
            for (int i = 0; i < config.harvestmoon.harvestmoonSongs.length; i++) {
                String songName = config.harvestmoon.harvestmoonSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("harvestmoon");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                harvestmoon.add(sound);
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.fallingstars.fallingstarsSongs != null) {
            for (int i = 0; i < config.fallingstars.fallingstarsSongs.length; i++) {
                String songName = config.fallingstars.fallingstarsSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("fallingstars");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                fallingstars.add(sound);
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.rainintensity.rainintensitySongs != null) {
            for (int i = 0; i < config.rainintensity.rainintensitySongs.length; i++) {
                String[] broken = stringBreaker(config.rainintensity.rainintensitySongs[i],",");
                int extractedIntensity = Integer.parseInt(broken[1]);
                String songName = broken[0].toLowerCase();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("Rain Intensity"+extractedIntensity);
                    songName = songName.substring(1);
                }
                rainintensitySongs.computeIfAbsent(extractedIntensity, k -> new ArrayList<>());
                rainintensitySongsString.computeIfAbsent(extractedIntensity, k -> new ArrayList<>());
                rainintensitySongsString.get(extractedIntensity).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                rainintensitySongs.get(extractedIntensity).add(sound);
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.underwater.underwaterSongs != null) {
            for (int i = 0; i < config.underwater.underwaterSongs.length; i++) {
                String songName = config.underwater.underwaterSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("underwater");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                underwater.add(sound);
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.effect.effectSongs != null) {
            for (int i = 0; i < config.effect.effectSongs.length; i++) {
                String[] broken = stringBreaker(config.effect.effectSongs[i],",");
                String effectName = broken[0];
                effectPriorities.computeIfAbsent(effectName, k -> config.effect.effectPriority);
                if (broken.length >= 3) {
                    int extractedPriority = Integer.parseInt(broken[2]);
                    effectPriorities.put(effectName, extractedPriority);
                }
                effectFade.putIfAbsent(effectName, 0);
                if(broken.length==4) {
                    int extractedFade = Integer.parseInt(broken[3]);
                    effectFade.put(effectName, extractedFade);
                }
                String songName = broken[1].toLowerCase();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("Rain Intensity");
                    songName = songName.substring(1);
                }
                effectSongs.computeIfAbsent(effectName, k -> new ArrayList<>());
                effectSongsString.computeIfAbsent(effectName, k -> new ArrayList<>());
                effectSongsString.get(effectName).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                effectSongs.get(effectName).add(sound);
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.pvp.pvpSongs != null) {
            for (int i = 0; i < config.pvp.pvpSongs.length; i++) {
                String songName = config.pvp.pvpSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("pvp");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                pvp.add(sound);
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.victory.victorySongs != null) {
            for (int i = 0; i < config.victory.victorySongs.length; i++) {
                String[] broken = stringBreaker(config.victory.victorySongs[i],",");
                int extractedID = Integer.parseInt(broken[1]);
                victoryID.put("Victory"+extractedID,extractedID);
                victoryTime.putIfAbsent("Victory"+extractedID, 200);
                if (broken.length >= 3) {
                    int extractedTime = Integer.parseInt(broken[2]);
                    victoryTime.put("Victory"+extractedID, extractedTime);
                }
                victoryPriorities.computeIfAbsent("Victory"+extractedID, k -> config.victory.victoryPriority);
                if (broken.length >= 4) {
                    int extractedPriority = Integer.parseInt(broken[3]);
                    victoryPriorities.put("Victory"+extractedID, extractedPriority);
                }
                victoryFade.putIfAbsent("Victory"+extractedID, 0);
                if(broken.length==5) {
                    int extractedFade = Integer.parseInt(broken[4]);
                    victoryFade.put("Victory"+extractedID, extractedFade);
                }
                String songName = broken[0].toLowerCase();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("Victory"+extractedID);
                    songName = songName.substring(1);
                }
                victorySongs.computeIfAbsent("Victory"+extractedID, k -> new ArrayList<>());
                victorySongsString.computeIfAbsent("Victory"+extractedID, k -> new ArrayList<>());
                victorySongsString.get("Victory"+extractedID).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                victorySongs.get("Victory"+extractedID).add(sound);
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.tornado.tornadoSongs != null) {
            for (int i = 0; i < config.tornado.tornadoSongs.length; i++) {
                String[] broken = stringBreaker(config.tornado.tornadoSongs[i],",");
                int extractedIntensity = Integer.parseInt(broken[1]);
                tornadoIntensity.put("Tornado"+extractedIntensity,extractedIntensity);
                tornadoPriorities.computeIfAbsent("Tornado"+extractedIntensity, k -> config.tornado.tornadoPriority);
                if (broken.length >= 3) {
                    int extractedPriority = Integer.parseInt(broken[2]);
                    tornadoPriorities.put("Tornado"+extractedIntensity, extractedPriority);
                }
                tornadoFade.putIfAbsent("Tornado"+extractedIntensity, 0);
                if(broken.length==4) {
                    int extractedFade = Integer.parseInt(broken[3]);
                    tornadoFade.put("Tornado"+extractedIntensity, extractedFade);
                }
                String songName = broken[0].toLowerCase();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("Tornado"+extractedIntensity);
                    songName = songName.substring(1);
                }
                tornadoSongs.computeIfAbsent("Tornado"+extractedIntensity, k -> new ArrayList<>());
                tornadoSongsString.computeIfAbsent("Tornado"+extractedIntensity, k -> new ArrayList<>());
                tornadoSongsString.get("Tornado"+extractedIntensity).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                tornadoSongs.get("Tornado"+extractedIntensity).add(sound);
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.hurricane.hurricaneSongs != null) {
            for (int i = 0; i < config.hurricane.hurricaneSongs.length; i++) {
                String songName = config.hurricane.hurricaneSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("hurricane");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                hurricane.add(sound);
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.sandstorm.sandstormSongs != null) {
            for (int i = 0; i < config.sandstorm.sandstormSongs.length; i++) {
                String songName = config.sandstorm.sandstormSongs[i].toLowerCase();
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("sandstorm");
                    songName = songName.substring(1);
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                sandstorm.add(sound);
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
                }
            }
        }
        if (config.gui.guiSongs != null) {
            for (int i = 0; i < config.gui.guiSongs.length; i++) {
                String[] broken = stringBreaker(config.gui.guiSongs[i],",");
                String extractedName = broken[1];
                guiPriorities.computeIfAbsent("Gui-"+extractedName, k -> config.gui.guiPriority);
                if (broken.length >= 3) {
                    int extractedPriority = Integer.parseInt(broken[2]);
                    guiPriorities.put("Gui-"+extractedName, extractedPriority);
                }
                guiFade.putIfAbsent("Gui-"+extractedName, 0);
                if(broken.length==4) {
                    int extractedFade = Integer.parseInt(broken[3]);
                    guiFade.put("Gui-"+extractedName, extractedFade);
                }
                String songName = broken[0].toLowerCase();
                String songNamePlus = songName;
                if (songName.startsWith("@")) {
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("Gui-"+extractedName);
                    songName = songName.substring(1);
                }
                guiSongs.computeIfAbsent("Gui-"+extractedName, k -> new ArrayList<>());
                guiSongsString.computeIfAbsent("Gui-"+extractedName, k -> new ArrayList<>());
                guiSongsString.get("Gui-"+extractedName).add(songNamePlus);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                guiSongs.get("Gui-"+extractedName).add(sound);
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
                        songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + stringBreaker(stringBreaker(songName,";")[0],"|")[0])).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    }
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

        gamestageSongsWhitelist = new HashMap<>();
        gamestageSongsStringWhitelist = new HashMap<>();
        gamestagePrioritiesWhitelist = new HashMap<>();
        gamestageFadeWhitelist = new HashMap<>();
        gamestageSongsBlacklist = new HashMap<>();
        gamestageSongsStringBlacklist = new HashMap<>();
        gamestagePrioritiesBlacklist = new HashMap<>();
        gamestageFadeBlacklist = new HashMap<>();

        bloodmoon = new ArrayList<>();
        harvestmoon = new ArrayList<>();
        fallingstars = new ArrayList<>();

        rainintensitySongs = new HashMap<>();
        rainintensitySongsString = new HashMap<>();

        tornadoSongs = new HashMap<>();
        tornadoSongsString = new HashMap<>();
        tornadoIntensity = new HashMap<>();
        tornadoPriorities = new HashMap<>();
        tornadoFade = new HashMap<>();
        hurricane = new ArrayList<>();
        sandstorm = new ArrayList<>();


        songCombos = new HashMap<>();
    }
}