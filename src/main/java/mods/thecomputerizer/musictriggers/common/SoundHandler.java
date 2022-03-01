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
        MusicTriggersCommon.logger.info("Actually registering songs");
        for (Map.Entry<String, Map<String, String[]>> stringListEntry : configToml.triggerholder.entrySet()) {
            String songEntry = ((Map.Entry) stringListEntry).getKey().toString();
            MusicTriggersCommon.logger.info("Actually registering song: "+songEntry);
            SoundEvent sound = new SoundEvent(new Identifier(MusicTriggersCommon.MODID, configToml.songholder.get(songEntry)));
            List<String> triggers = new ArrayList<>();
            MusicTriggersCommon.logger.info("song "+configToml.songholder.get(songEntry)+" has "+configToml.triggerholder.get(songEntry).keySet().size()+" trigger(s)");
            for (Map.Entry<String, String[]> nestedStringListEntry : configToml.triggerholder.get(songEntry).entrySet()) {
                String temp = ((Map.Entry) nestedStringListEntry).getKey().toString();
                if(configToml.triggerholder.get(songEntry).get(temp)[6].matches("not")) {
                    MusicTriggersCommon.logger.info("Registered "+temp+" as an anti trigger for "+configToml.songholder.get(songEntry));
                    antiSongs.computeIfAbsent(songEntry, k -> new ArrayList<>());
                    if(configToml.triggerholder.get(songEntry).get(temp)[10].matches("")) {
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
                MusicTriggersCommon.logger.info("Detected the single trigger of "+trigger+" for song "+configToml.songholder.get(songEntry));
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
                            MusicTriggersCommon.logger.info("Added trigger combination for trigger "+trigger+" with blank identifier for song "+configToml.songholder.get(songEntry));
                        }
                        else {
                            songCombos.get(songEntry).add(trigger+"-"+configToml.triggerholder.get(songEntry).get(trigger)[10]);
                            MusicTriggersCommon.logger.info("Added trigger combination for trigger "+trigger+" with identifier of "+configToml.triggerholder.get(songEntry).get(trigger)[10]+" for song "+configToml.songholder.get(songEntry));
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
