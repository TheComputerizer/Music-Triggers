package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.config.configToml;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.*;

@SuppressWarnings("rawtypes")
public class SoundHandler {

    public static List<SoundEvent> allSoundEvents = new ArrayList<>();
    public static HashMap<SoundEvent,String> allSoundEventsWithTriggers = new HashMap<>();

    public static Map<String, Map<String, String>> TriggerSongMap = new HashMap<>();
    public static Map<String, String[]> TriggerInfoMap = new HashMap<>();

    public static HashMap<String, List<String>> songCombos = new HashMap<>();
    public static HashMap<String, List<String>> antiSongs = new HashMap<>();


    public static void registerSounds() {

        for (Map.Entry<String, Map<String, String[]>> stringListEntry : configToml.triggerholder.entrySet()) {
            String songEntry = ((Map.Entry) stringListEntry).getKey().toString();
            SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." +configToml.songholder.get(songEntry))).setRegistryName(new ResourceLocation(MusicTriggers.MODID, configToml.songholder.get(songEntry)));
            List<String> triggers = new ArrayList<>();
            for (Map.Entry<String, String[]> nestedStringListEntry : configToml.triggerholder.get(songEntry).entrySet()) {
                String temp = ((Map.Entry) nestedStringListEntry).getKey().toString();
                if(configToml.triggerholder.get(songEntry).get(temp)[6].matches("not")) {
                    antiSongs.computeIfAbsent(songEntry, k -> new ArrayList<>());
                    if(configToml.triggerholder.get(songEntry).get(temp)[10].matches("_")) {
                        antiSongs.get(songEntry).add(temp);
                    }
                    else {
                        antiSongs.get(songEntry).add(temp+"-"+configToml.triggerholder.get(songEntry).get(temp)[10]);
                    }
                }
                else {
                    triggers.add(temp);
                }
            }
            if(triggers.size()==1) {
                String trigger = triggers.get(0);
                TriggerSongMap.putIfAbsent(trigger, new HashMap<>());
                TriggerSongMap.get(trigger).putIfAbsent(songEntry,configToml.triggerholder.get(songEntry).get(trigger)[10]);
                TriggerInfoMap.putIfAbsent(trigger, configToml.triggerholder.get(songEntry).get(trigger));
                if(!configToml.triggerholder.get(songEntry).get(trigger)[10].matches("") || configToml.triggerholder.get(songEntry).get(trigger)[10].matches("_")) {
                    if (!TriggerInfoMap.containsKey(trigger + "-" + configToml.triggerholder.get(songEntry).get(trigger)[10])) {
                        TriggerInfoMap.put(trigger + "-" + configToml.triggerholder.get(songEntry).get(trigger)[10], configToml.triggerholder.get(songEntry).get(trigger));
                    }
                }
                if(checkResourceLocation(sound)) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound, trigger);
                }
            }
            else {
                for(String trigger : triggers) {
                    if(configToml.triggerholder.get(songEntry).get(trigger)[6].matches("and")) {
                        songCombos.computeIfAbsent(songEntry, k -> new ArrayList<>());
                        if(configToml.triggerholder.get(songEntry).get(trigger)[10].matches("") || configToml.triggerholder.get(songEntry).get(trigger)[10].matches("_")) {
                            songCombos.get(songEntry).add(trigger);
                        }
                        else {
                            songCombos.get(songEntry).add(trigger+"-"+configToml.triggerholder.get(songEntry).get(trigger)[10]);
                        }
                        TriggerSongMap.putIfAbsent(trigger, new HashMap<>());
                        TriggerSongMap.get(trigger).putIfAbsent("@"+songEntry,configToml.triggerholder.get(songEntry).get(trigger)[10]);
                        TriggerInfoMap.putIfAbsent(trigger, configToml.triggerholder.get(songEntry).get(trigger));
                        if(!(configToml.triggerholder.get(songEntry).get(trigger)[10].matches("") || configToml.triggerholder.get(songEntry).get(trigger)[10].matches("_"))) {
                            if (!TriggerInfoMap.containsKey(trigger + "-" + configToml.triggerholder.get(songEntry).get(trigger)[10])) {
                                TriggerInfoMap.put(trigger + "-" + configToml.triggerholder.get(songEntry).get(trigger)[10], configToml.triggerholder.get(songEntry).get(trigger));
                            }
                        }
                    }
                    if(checkResourceLocation(sound)) {
                        allSoundEvents.add(sound);
                        allSoundEventsWithTriggers.put(sound, trigger);
                    }
                }
            }
            if(configToml.triggerlinking.get(songEntry) !=null) {
                int triggerCounter=0;
                for(String song : configToml.triggerlinking.get(songEntry).keySet()) {
                    if(triggerCounter!=0) {
                        SoundEvent soundLink = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + song)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, song));
                        boolean shouldBeAdded = true;
                        for(SoundEvent s : allSoundEvents) {
                            if(Objects.requireNonNull(soundLink.getRegistryName()).toString().matches(Objects.requireNonNull(s.getRegistryName()).toString())) {
                                shouldBeAdded = false;
                            }
                        }
                        if(shouldBeAdded) {
                            if(checkResourceLocation(soundLink)) {
                                allSoundEvents.add(soundLink);
                                allSoundEventsWithTriggers.put(soundLink, configToml.triggerlinking.get(songEntry).get(song)[0]);
                            }
                        }
                    }
                    triggerCounter++;
                }
            }
        }
    }

    public static boolean checkResourceLocation(SoundEvent sound) {
        for(SoundEvent s : allSoundEvents) {
            if(Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) return false;
        }
        return true;
    }

    public static void emptyListsAndMaps() {
        allSoundEvents = new ArrayList<>();
        allSoundEventsWithTriggers = new HashMap<>();
        TriggerSongMap = new HashMap<>();
        TriggerInfoMap = new HashMap<>();
        songCombos = new HashMap<>();
        antiSongs = new HashMap<>();
    }
}
