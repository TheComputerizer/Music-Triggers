package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.config.configToml;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

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
            SoundEvent sound = new SoundEvent(new Identifier(MusicTriggersCommon.MODID, configToml.songholder.get(songEntry)));
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
                        }
                        else {
                            songCombos.get(songEntry).add(trigger+"-"+configToml.triggerholder.get(songEntry).get(trigger)[10]);
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
                        SoundEvent soundLink = new SoundEvent(new Identifier(MusicTriggersCommon.MODID, song));
                        boolean shouldBeAdded = true;
                        for(SoundEvent s : allSoundEvents) {
                            if(soundLink.getId().toString().matches(s.getId().toString())) {
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
