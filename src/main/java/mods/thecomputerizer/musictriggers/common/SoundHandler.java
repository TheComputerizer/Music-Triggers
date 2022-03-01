package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.config.configToml;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            MusicTriggers.logger.info("song "+configToml.songholder.get(songEntry)+" has "+configToml.triggerholder.get(songEntry).keySet().size()+" trigger(s)");
            for (Map.Entry<String, String[]> nestedStringListEntry : configToml.triggerholder.get(songEntry).entrySet()) {
                String temp = ((Map.Entry) nestedStringListEntry).getKey().toString();
                if(configToml.triggerholder.get(songEntry).get(temp)[6].matches("not")) {
                    MusicTriggers.logger.info("Registered "+temp+" as an anti trigger for "+configToml.songholder.get(songEntry));
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
                MusicTriggers.logger.info("Detected the single trigger of "+trigger+" for song "+configToml.songholder.get(songEntry));
                TriggerInfoMap.putIfAbsent(trigger, configToml.triggerholder.get(songEntry).get(trigger));
                if(!configToml.triggerholder.get(songEntry).get(trigger)[10].matches("")) {
                    if (!TriggerInfoMap.containsKey(trigger + "-" + configToml.triggerholder.get(songEntry).get(trigger)[10])) {
                        TriggerInfoMap.put(trigger + "-" + configToml.triggerholder.get(songEntry).get(trigger)[10], configToml.triggerholder.get(songEntry).get(trigger));
                    }
                }
                if(!allSoundEvents.contains(sound)) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound, trigger);
                }
            }
            else {
                for(String trigger : triggers) {
                    if(configToml.triggerholder.get(songEntry).get(trigger)[6].matches("and")) {
                        songCombos.computeIfAbsent(songEntry, k -> new ArrayList<>());
                        if(configToml.triggerholder.get(songEntry).get(trigger)[10].matches("")) {
                            songCombos.get(songEntry).add(trigger);
                            MusicTriggers.logger.info("Added trigger combination for trigger "+trigger+" with blank identifier for song "+configToml.songholder.get(songEntry));
                        }
                        else {
                            songCombos.get(songEntry).add(trigger+"-"+configToml.triggerholder.get(songEntry).get(trigger)[10]);
                            MusicTriggers.logger.info("Added trigger combination for trigger "+trigger+" with identifier of "+configToml.triggerholder.get(songEntry).get(trigger)[10]+" for song "+configToml.songholder.get(songEntry));
                        }
                        TriggerSongMap.putIfAbsent(trigger, new HashMap<>());
                        TriggerSongMap.get(trigger).putIfAbsent("@"+songEntry,configToml.triggerholder.get(songEntry).get(trigger)[10]);
                        TriggerInfoMap.putIfAbsent(trigger, configToml.triggerholder.get(songEntry).get(trigger));
                        if(!configToml.triggerholder.get(songEntry).get(trigger)[10].matches("")) {
                            if (!TriggerInfoMap.containsKey(trigger + "-" + configToml.triggerholder.get(songEntry).get(trigger)[10])) {
                                TriggerInfoMap.put(trigger + "-" + configToml.triggerholder.get(songEntry).get(trigger)[10], configToml.triggerholder.get(songEntry).get(trigger));
                            }
                        }
                    }
                    if(!allSoundEvents.contains(sound)) {
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
                            if(soundLink.getRegistryName().toString().matches(s.getRegistryName().toString())) {
                                shouldBeAdded = false;
                            }
                        }
                        if(shouldBeAdded) {
                            if(!allSoundEvents.contains(soundLink)) {
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

    public static void emptyListsAndMaps() {
        allSoundEvents = new ArrayList<>();
        allSoundEventsWithTriggers = new HashMap<>();
        TriggerSongMap = new HashMap<>();
        TriggerInfoMap = new HashMap<>();
        songCombos = new HashMap<>();
        antiSongs = new HashMap<>();
    }
}
