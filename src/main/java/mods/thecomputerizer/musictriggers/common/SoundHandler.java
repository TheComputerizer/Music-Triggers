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

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> evt) {
        IForgeRegistry<SoundEvent> r = evt.getRegistry();
        if (config.menu.menuSongs!=null) {
            for (int i = 0; i < config.menu.menuSongs.length; i++) {
                String songName = config.menu.menuSongs[i];
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
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
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                generic.add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(generic.get(i));
            }
        }
        if (config.day.daySongs!=null) {
            for (int i = 0; i < config.day.daySongs.length; i++) {
                String songName = config.day.daySongs[i];
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                day.add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(day.get(i));
            }
        }
        if (config.night.nightSongs!=null) {
            for (int i = 0; i < config.night.nightSongs.length; i++) {
                String songName = config.night.nightSongs[i];
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                night.add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(night.get(i));
            }
        }
        if (config.sunrise.sunriseSongs!=null) {
            for (int i = 0; i < config.sunrise.sunriseSongs.length; i++) {
                String songName = config.sunrise.sunriseSongs[i];
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                sunrise.add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(sunrise.get(i));
            }
        }
        if (config.sunset.sunsetSongs!=null) {
            for (int i = 0; i < config.sunset.sunsetSongs.length; i++) {
                String songName = config.sunset.sunsetSongs[i];
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                sunset.add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(sunset.get(i));
            }
        }
        if (config.light.lightSongs!=null) {
            for (int i = 0; i < config.light.lightSongs.length; i++) {
                String songName = config.light.lightSongs[i];
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                light.add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(light.get(i));
            }
        }
        if (config.underground.undergroundSongs!=null) {
            for (int i = 0; i < config.underground.undergroundSongs.length; i++) {
                String songName = config.underground.undergroundSongs[i];
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                underground.add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(underground.get(i));
            }
        }
        if (config.deepUnder.deepUnderSongs!=null) {
            for (int i = 0; i < config.deepUnder.deepUnderSongs.length; i++) {
                String songName = config.deepUnder.deepUnderSongs[i];
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                deepUnder.add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(deepUnder.get(i));
            }
        }
        if (config.raining.rainingSongs!=null) {
            for (int i = 0; i < config.raining.rainingSongs.length; i++) {
                String songName = config.raining.rainingSongs[i];
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                raining.add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(raining.get(i));
            }
        }
        if (config.storming.stormingSongs!=null) {
            for (int i = 0; i < config.storming.stormingSongs.length; i++) {
                String songName = config.storming.stormingSongs[i];
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                storming.add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(storming.get(i));
            }
        }
        if (config.snowing.snowingSongs!=null) {
            for (int i = 0; i < config.snowing.snowingSongs.length; i++) {
                String songName = config.snowing.snowingSongs[i];
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                snowing.add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(snowing.get(i));
            }
        }
        if (config.lowHP.lowHPSongs!=null) {
            for (int i = 0; i < config.lowHP.lowHPSongs.length; i++) {
                String songName = config.lowHP.lowHPSongs[i];
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                lowHP.add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(lowHP.get(i));
            }
        }
        if (config.dead.deadSongs!=null) {
            for (int i = 0; i < config.dead.deadSongs.length; i++) {
                String songName = config.dead.deadSongs[i];
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                dead.add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(dead.get(i));
            }
        }
        if (config.inVoid.inVoidSongs!=null) {
            for (int i = 0; i < config.inVoid.inVoidSongs.length; i++) {
                String songName = config.inVoid.inVoidSongs[i];
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                inVoid.add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(inVoid.get(i));
            }
        }
        if (config.spectator.spectatorSongs!=null) {
            for (int i = 0; i < config.spectator.spectatorSongs.length; i++) {
                String songName = config.spectator.spectatorSongs[i];
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                spectator.add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(spectator.get(i));
            }
        }
        if (config.creative.creativeSongs!=null) {
            for (int i = 0; i < config.creative.creativeSongs.length; i++) {
                String songName = config.creative.creativeSongs[i];
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                creative.add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(creative.get(i));
            }
        }
        if (config.riding.ridingSongs!=null) {
            for (int i = 0; i < config.riding.ridingSongs.length; i++) {
                String songName = config.riding.ridingSongs[i];
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                riding.add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(riding.get(i));
            }
        }
        if (config.pet.petSongs!=null) {
            for (int i = 0; i < config.pet.petSongs.length; i++) {
                String songName = config.pet.petSongs[i];
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                pet.add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(pet.get(i));
            }
        }
        if (config.high.highSongs!=null) {
            for (int i = 0; i < config.high.highSongs.length; i++) {
                String songName = config.high.highSongs[i];
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                high.add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(high.get(i));
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
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                dimensionSongs.computeIfAbsent(extractedID, k -> new ArrayList<>());
                dimensionSongsString.computeIfAbsent(extractedID, k -> new ArrayList<>());
                dimensionSongsString.get(extractedID).add(songName);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                dimensionSongs.get(extractedID).add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(sound);
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
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                biomeSongs.computeIfAbsent(extractedBiome, k -> new ArrayList<>());
                biomeSongsString.computeIfAbsent(extractedBiome, k -> new ArrayList<>());
                biomeSongsString.get(extractedBiome).add(songName);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                biomeSongs.get(extractedBiome).add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(sound);
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
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
                }
                structureSongs.computeIfAbsent(extractedStructName, k -> new ArrayList<>());
                structureSongsString.computeIfAbsent(extractedStructName, k -> new ArrayList<>());
                structureSongsString.get(extractedStructName).add(songName);
                SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music."+songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName));
                structureSongs.get(extractedStructName).add(sound);
                EnumHelperClient.addMusicType(songName, sound, 0, 0);
                songsRecords.put(songName,PositionedSoundRecord.getMusicRecord(new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + songName)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, songName))));
                r.register(sound);
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
                if(songName.startsWith("+") || songName.startsWith("-")) {
                    songName = songName.substring(1);
                    if(songName.startsWith("+") || songName.startsWith("-")) {
                        songName = songName.substring(1);
                    }
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
