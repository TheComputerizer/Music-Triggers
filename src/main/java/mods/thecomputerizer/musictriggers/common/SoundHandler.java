package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.config;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.EnumHelperClient;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mod.EventBusSubscriber(modid=MusicTriggers.MODID)
public class SoundHandler {

    public static HashMap<String, PositionedSoundRecord> songsRecords = new HashMap<>();

    public static List<String> allSongs = new ArrayList<>();

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

    public static HashMap<String, List<SoundEvent>> biomeSongs = new HashMap<>();
    public static HashMap<String, List<String>> biomeSongsString = new HashMap<>();
    public static HashMap<String, Integer> biomePriorities = new HashMap<>();

    public static HashMap<String, List<SoundEvent>> structureSongs = new HashMap<>();
    public static HashMap<String, List<String>> structureSongsString = new HashMap<>();
    public static HashMap<String, Integer> structurePriorities = new HashMap<>();

    public static HashMap<String, List<SoundEvent>> mobSongs = new HashMap<>();
    public static HashMap<String, List<String>> mobSongsString = new HashMap<>();
    public static HashMap<String, Integer> mobPriorities = new HashMap<>();
    public static HashMap<String, Integer> mobNumber = new HashMap<>();

    public static HashMap<String, List<String>> songCombos = new HashMap<>();

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> evt) {
        IForgeRegistry<SoundEvent> r = evt.getRegistry();
        if (config.menu.menuSongs!=null) {
            for (int i = 0; i < config.menu.menuSongs.length; i++) {
                String songName = config.menu.menuSongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("menu");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                menu.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.generic.genericSongs!=null) {
            for (int i = 0; i < config.generic.genericSongs.length; i++) {
                String songName = config.generic.genericSongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("generic");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                generic.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.day.daySongs!=null) {
            for (int i = 0; i < config.day.daySongs.length; i++) {
                String songName = config.day.daySongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("day");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                day.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.night.nightSongs!=null) {
            for (int i = 0; i < config.night.nightSongs.length; i++) {
                String songName = config.night.nightSongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("night");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                night.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.sunrise.sunriseSongs!=null) {
            for (int i = 0; i < config.sunrise.sunriseSongs.length; i++) {
                String songName = config.sunrise.sunriseSongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("sunrise");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                sunrise.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.sunset.sunsetSongs!=null) {
            for (int i = 0; i < config.sunset.sunsetSongs.length; i++) {
                String songName = config.sunset.sunsetSongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("sunset");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                sunset.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.light.lightSongs!=null) {
            for (int i = 0; i < config.light.lightSongs.length; i++) {
                String songName = config.light.lightSongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("light");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                light.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.underground.undergroundSongs!=null) {
            for (int i = 0; i < config.underground.undergroundSongs.length; i++) {
                String songName = config.underground.undergroundSongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("underground");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                underground.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.deepUnder.deepUnderSongs!=null) {
            for (int i = 0; i < config.deepUnder.deepUnderSongs.length; i++) {
                String songName = config.deepUnder.deepUnderSongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("deepUnder");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                deepUnder.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.raining.rainingSongs!=null) {
            for (int i = 0; i < config.raining.rainingSongs.length; i++) {
                String songName = config.raining.rainingSongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("raining");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                raining.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.storming.stormingSongs!=null) {
            for (int i = 0; i < config.storming.stormingSongs.length; i++) {
                String songName = config.storming.stormingSongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("storming");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                storming.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.snowing.snowingSongs!=null) {
            for (int i = 0; i < config.snowing.snowingSongs.length; i++) {
                String songName = config.snowing.snowingSongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("snowing");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                snowing.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.lowHP.lowHPSongs!=null) {
            for (int i = 0; i < config.lowHP.lowHPSongs.length; i++) {
                String songName = config.lowHP.lowHPSongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("lowHP");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                lowHP.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.dead.deadSongs!=null) {
            for (int i = 0; i < config.dead.deadSongs.length; i++) {
                String songName = config.dead.deadSongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("dead");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                dead.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.inVoid.inVoidSongs!=null) {
            for (int i = 0; i < config.inVoid.inVoidSongs.length; i++) {
                String songName = config.inVoid.inVoidSongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("inVoid");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                inVoid.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.spectator.spectatorSongs!=null) {
            for (int i = 0; i < config.spectator.spectatorSongs.length; i++) {
                String songName = config.spectator.spectatorSongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("spectator");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                spectator.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.creative.creativeSongs!=null) {
            for (int i = 0; i < config.creative.creativeSongs.length; i++) {
                String songName = config.creative.creativeSongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("creative");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                creative.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.riding.ridingSongs!=null) {
            for (int i = 0; i < config.riding.ridingSongs.length; i++) {
                String songName = config.riding.ridingSongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("riding");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                riding.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.pet.petSongs!=null) {
            for (int i = 0; i < config.pet.petSongs.length; i++) {
                String songName = config.pet.petSongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("pet");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                pet.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.high.highSongs!=null) {
            for (int i = 0; i < config.high.highSongs.length; i++) {
                String songName = config.high.highSongs[i];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("high");
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                high.add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.dimension.dimensionSongs!=null) {
            for (int i = 0; i < config.dimension.dimensionSongs.length; i++) {
                String[] broken = stringBreaker(config.dimension.dimensionSongs[i]);
                int extractedID = Integer.parseInt(broken[0]);
                dimensionPriorities.computeIfAbsent(extractedID, k -> config.dimension.dimensionPriority);
                if(broken.length==3) {
                    int extractedPriority = Integer.parseInt(broken[2]);
                    dimensionPriorities.put(extractedID,extractedPriority);
                }
                String songName = broken[1];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("dimension"+extractedID);
                }
                dimensionSongs.computeIfAbsent(extractedID, k -> new ArrayList<>());
                dimensionSongsString.computeIfAbsent(extractedID, k -> new ArrayList<>());
                dimensionSongsString.get(extractedID).add(songName);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                dimensionSongs.get(extractedID).add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.biome.biomeSongs!=null) {
            for (int i = 0; i < config.biome.biomeSongs.length; i++) {
                String[] broken = stringBreaker(config.biome.biomeSongs[i]);
                String extractedBiome = broken[0];
                biomePriorities.computeIfAbsent(extractedBiome, k -> config.biome.biomePriority);
                if(broken.length==3) {
                    int extractedPriority = Integer.parseInt(broken[2]);
                    biomePriorities.put(extractedBiome,extractedPriority);
                }
                String songName = broken[1];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add(extractedBiome);
                }
                biomeSongs.computeIfAbsent(extractedBiome, k -> new ArrayList<>());
                biomeSongsString.computeIfAbsent(extractedBiome, k -> new ArrayList<>());
                biomeSongsString.get(extractedBiome).add(songName);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                biomeSongs.get(extractedBiome).add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.structure.structureSongs!=null) {
            for (int i = 0; i < config.structure.structureSongs.length; i++) {
                String[] broken = stringBreaker(config.structure.structureSongs[i]);
                String extractedStructName = broken[0];
                structurePriorities.computeIfAbsent(extractedStructName, k -> config.structure.structurePriority);
                if(broken.length==3) {
                    int extractedPriority = Integer.parseInt(broken[2]);
                    structurePriorities.put(extractedStructName,extractedPriority);
                }
                String songName = broken[1];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add("structure:"+extractedStructName);
                }
                structureSongs.computeIfAbsent(extractedStructName, k -> new ArrayList<>());
                structureSongsString.computeIfAbsent(extractedStructName, k -> new ArrayList<>());
                structureSongsString.get(extractedStructName).add(songName);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                structureSongs.get(extractedStructName).add(sound);
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
        if (config.mob.mobSongs!=null) {
            for (int i = 0; i < config.mob.mobSongs.length; i++) {
                String[] broken = stringBreaker(config.mob.mobSongs[i]);
                String extractedMobName = broken[0];
                mobPriorities.computeIfAbsent(extractedMobName, k -> config.mob.mobPriority);
                if(broken.length==4) {
                    int extractedPriority = Integer.parseInt(broken[3]);
                    mobPriorities.put(extractedMobName,extractedPriority);
                }
                mobNumber.put(extractedMobName,Integer.parseInt(broken[1]));
                String songName = broken[2];
                if(songName.startsWith("+")) {
                    songName = songName.substring(1);
                    songCombos.computeIfAbsent(songName, k -> new ArrayList<>());
                    songCombos.get(songName).add(extractedMobName);
                }
                mobSongs.computeIfAbsent(extractedMobName, k -> new ArrayList<>());
                mobSongsString.computeIfAbsent(extractedMobName, k -> new ArrayList<>());
                mobSongsString.get(extractedMobName).add(songName);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                mobSongs.get(extractedMobName).add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                if(!allSongs.contains(songName)) {
                    allSongs.add(songName);
                    EnumHelperClient.addMusicType(songName, sound, 0, 0);
                    songsRecords.put(songName, PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                    r.register(sound);
                }
            }
        }
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
