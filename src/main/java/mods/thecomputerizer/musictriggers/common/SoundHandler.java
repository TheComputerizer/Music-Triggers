package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.config.ConfigToml;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.*;

public class SoundHandler {

    public static List<SoundEvent> allSoundEvents = new ArrayList<>();
    public static HashMap<SoundEvent,String> allSoundEventsWithTriggers = new HashMap<>();

    public static Map<String, Map<String, List<String>>> TriggerIdentifierMap = new HashMap<>();
    public static Map<String, String[]> TriggerInfoMap = new HashMap<>();

    public static HashMap<String, List<String>> songCombos = new HashMap<>();
    public static HashMap<String, List<String>> antiSongs = new HashMap<>();
    public static HashMap<List<String>, List<String>> instantiatedCombos = new HashMap<>();


    public static void registerSounds() {
        for(int i = 0; i< ConfigToml.songholder.entrySet().size(); i++) {
            String songEntry = "song"+i;
            SoundEvent sound = new SoundEvent(new Identifier(MusicTriggersCommon.MODID, ConfigToml.songholder.get(songEntry)));
            List<String> triggers = new ArrayList<>();
            for (String trigger : ConfigToml.triggerholder.get(songEntry).keySet()) {
                String decoded = decode(songEntry,trigger);
                if(ConfigToml.triggerholder.get(songEntry).get(trigger)[6].matches("not")) {
                    antiSongs.computeIfAbsent(songEntry, k -> new ArrayList<>());
                    if(ConfigToml.triggerholder.get(songEntry).get(trigger)[10].matches("_")) antiSongs.get(songEntry).add(decoded);
                    else antiSongs.get(songEntry).add(decoded+"-"+ ConfigToml.triggerholder.get(songEntry).get(trigger)[10]);
                }
                triggers.add(trigger);
            }
            if(triggers.size()==1) {
                String trigger = triggers.get(0);
                String decoded = decode(songEntry,trigger);
                TriggerIdentifierMap.putIfAbsent(decoded, new HashMap<>());
                TriggerIdentifierMap.get(decoded).putIfAbsent(ConfigToml.triggerholder.get(songEntry).get(trigger)[10], new ArrayList<>());
                TriggerIdentifierMap.get(decoded).get(ConfigToml.triggerholder.get(songEntry).get(trigger)[10]).add(songEntry);
                TriggerInfoMap.putIfAbsent(decoded, ConfigToml.triggerholder.get(songEntry).get(trigger));
                if(!ConfigToml.triggerholder.get(songEntry).get(trigger)[10].matches("") || ConfigToml.triggerholder.get(songEntry).get(trigger)[10].matches("_")) {
                    if (!TriggerInfoMap.containsKey(decoded + "-" + ConfigToml.triggerholder.get(songEntry).get(trigger)[10])) {
                        TriggerInfoMap.put(decoded + "-" + ConfigToml.triggerholder.get(songEntry).get(trigger)[10], ConfigToml.triggerholder.get(songEntry).get(trigger));
                    }
                }
                if(checkResourceLocation(sound)) {
                    allSoundEvents.add(sound);
                    allSoundEventsWithTriggers.put(sound, decoded);
                }
            }
            else {
                List<String> values = new ArrayList<>();
                for(String trigger : triggers) {
                    String decoded = decode(songEntry,trigger);
                    if(ConfigToml.triggerholder.get(songEntry).get(trigger)[6].matches("and")) {
                        songCombos.computeIfAbsent(songEntry, k -> new ArrayList<>());
                        if(ConfigToml.triggerholder.get(songEntry).get(trigger)[10].matches("") || ConfigToml.triggerholder.get(songEntry).get(trigger)[10].matches("_")) {
                            songCombos.get(songEntry).add(decoded);
                        }
                        else songCombos.get(songEntry).add(decoded+"-"+ ConfigToml.triggerholder.get(songEntry).get(trigger)[10]);
                        if(!Boolean.parseBoolean(ConfigToml.triggerholder.get(songEntry).get(trigger)[32])) {
                            TriggerIdentifierMap.putIfAbsent(decoded, new HashMap<>());
                            TriggerIdentifierMap.get(decoded).putIfAbsent(ConfigToml.triggerholder.get(songEntry).get(trigger)[10], new ArrayList<>());
                            TriggerIdentifierMap.get(decoded).get(ConfigToml.triggerholder.get(songEntry).get(trigger)[10]).add("@"+songEntry);
                            TriggerInfoMap.putIfAbsent(decoded, ConfigToml.triggerholder.get(songEntry).get(trigger));
                            if (!ConfigToml.triggerholder.get(songEntry).get(trigger)[10].matches("") || ConfigToml.triggerholder.get(songEntry).get(trigger)[10].matches("_")) {
                                if (!TriggerInfoMap.containsKey(decoded + "-" + ConfigToml.triggerholder.get(songEntry).get(trigger)[10]))
                                    TriggerInfoMap.put(decoded + "-" + ConfigToml.triggerholder.get(songEntry).get(trigger)[10], ConfigToml.triggerholder.get(songEntry).get(trigger));
                            }
                        }
                    }
                    else if(ConfigToml.triggerholder.get(songEntry).get(trigger)[6].matches("not")) {
                        if(!Boolean.parseBoolean(ConfigToml.triggerholder.get(songEntry).get(trigger)[32])) {
                            TriggerIdentifierMap.putIfAbsent(decoded, new HashMap<>());
                            TriggerIdentifierMap.get(decoded).putIfAbsent(ConfigToml.triggerholder.get(songEntry).get(trigger)[10], new ArrayList<>());
                            TriggerIdentifierMap.get(decoded).get(ConfigToml.triggerholder.get(songEntry).get(trigger)[10]).add("#"+songEntry);
                            TriggerInfoMap.putIfAbsent(decoded, ConfigToml.triggerholder.get(songEntry).get(trigger));
                            if (!ConfigToml.triggerholder.get(songEntry).get(trigger)[10].matches("") || ConfigToml.triggerholder.get(songEntry).get(trigger)[10].matches("_")) {
                                if (!TriggerInfoMap.containsKey(decoded + "-" + ConfigToml.triggerholder.get(songEntry).get(trigger)[10]))
                                    TriggerInfoMap.put(decoded + "-" + ConfigToml.triggerholder.get(songEntry).get(trigger)[10], ConfigToml.triggerholder.get(songEntry).get(trigger));
                            }
                        }
                    }
                    else if(ConfigToml.triggerholder.get(songEntry).get(trigger)[6].matches("instantiated")) {
                        if(ConfigToml.triggerholder.get(songEntry).get(trigger)[10].matches("") || ConfigToml.triggerholder.get(songEntry).get(trigger)[10].matches("_")) {
                            values.add(decoded);
                        }
                        else values.add(decoded+"-"+ ConfigToml.triggerholder.get(songEntry).get(trigger)[10]);
                    }
                    if(checkResourceLocation(sound)) {
                        allSoundEvents.add(sound);
                        allSoundEventsWithTriggers.put(sound, decoded);
                    }
                }
                instantiatedCombos.put(songCombos.get(songEntry),values);
            }
            if(ConfigToml.triggerlinking.get(songEntry) !=null) {
                int triggerCounter=0;
                for(String song : ConfigToml.triggerlinking.get(songEntry).keySet()) {
                    if(triggerCounter!=0) {
                        SoundEvent soundLink = new SoundEvent(new Identifier(MusicTriggersCommon.MODID, song));
                        boolean shouldBeAdded = true;
                        for(SoundEvent s : allSoundEvents) {
                            if(Objects.requireNonNull(soundLink.getId()).toString().matches(Objects.requireNonNull(s.getId()).toString())) {
                                shouldBeAdded = false;
                            }
                        }
                        if(shouldBeAdded) {
                            if(checkResourceLocation(soundLink)) {
                                allSoundEvents.add(soundLink);
                                allSoundEventsWithTriggers.put(soundLink, ConfigToml.triggerlinking.get(songEntry).get(song)[0]);
                            }
                        }
                    }
                    triggerCounter++;
                }
            }
        }
        for(String t : TriggerIdentifierMap.keySet()) {
            StringBuilder triggerChecker = new StringBuilder();
            triggerChecker.append("Final song check for trigger ").append(t).append(": ");
            for(String i : TriggerIdentifierMap.get(t).keySet()) {
                for(String s : TriggerIdentifierMap.get(t).get(i)) {
                    triggerChecker.append(s).append("(").append(ConfigToml.songholder.get(s.replaceAll("@", "").replaceAll("#", ""))).append(") ");
                }
            }
            MusicTriggersCommon.logger.info(triggerChecker.toString());
        }
    }

    public static boolean checkResourceLocation(SoundEvent sound) {
        for(SoundEvent s : allSoundEvents) {
            if(Objects.requireNonNull(s.getId()).toString().matches(Objects.requireNonNull(sound.getId()).toString())) return false;
        }
        return true;
    }

    public static String decode(String code, String triggerID) {
        return ConfigToml.triggerMapper.get(code).get(triggerID);
    }

    public static void emptyListsAndMaps() {
        allSoundEvents = new ArrayList<>();
        allSoundEventsWithTriggers = new HashMap<>();
        TriggerIdentifierMap = new HashMap<>();
        TriggerInfoMap = new HashMap<>();
        songCombos = new HashMap<>();
        antiSongs = new HashMap<>();
    }
}
