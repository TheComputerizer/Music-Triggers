package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.config.configToml;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.EnumHelperClient;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;

import java.util.*;

public class SoundHandler {

    public static List<SoundEvent> allSoundEvents = new ArrayList<>();
    public static HashMap<SoundEvent,String> allSoundEventsTriggers = new HashMap<>();

    public static Map<String, Map<String, List<String>>> TriggerIdentifierMap = new HashMap<>();
    public static Map<String, String[]> TriggerInfoMap = new HashMap<>();

    public static HashMap<String, List<String>> songCombos = new HashMap<>();
    public static HashMap<String, List<String>> antiSongs = new HashMap<>();
    public static HashMap<List<String>, List<String>> instantiatedCombos = new HashMap<>();

    public static void registerSounds() {

        for(int i=0;i<configToml.songholder.entrySet().size();i++) {
            String songEntry = "song"+i;
            MusicTriggers.logger.info("Registering sound: "+configToml.songholder.get(songEntry));
            SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." +configToml.songholder.get(songEntry))).setRegistryName(new ResourceLocation(MusicTriggers.MODID, configToml.songholder.get(songEntry)));
            List<String> triggers = new ArrayList<>();
            for (String trigger : configToml.triggerholder.get(songEntry).keySet()) {
                String decoded = decode(songEntry,trigger);
                if(configToml.triggerholder.get(songEntry).get(trigger)[6].matches("not")) {
                    antiSongs.computeIfAbsent(songEntry, k -> new ArrayList<>());
                    if(configToml.triggerholder.get(songEntry).get(trigger)[10].matches("_")) antiSongs.get(songEntry).add(decoded);
                    else antiSongs.get(songEntry).add(decoded+"-"+configToml.triggerholder.get(songEntry).get(trigger)[10]);
                }
                triggers.add(trigger);
            }
            if(triggers.size()==1) {
                String trigger = triggers.get(0);
                String decoded = decode(songEntry,trigger);
                TriggerIdentifierMap.putIfAbsent(decoded, new HashMap<>());
                TriggerIdentifierMap.get(decoded).putIfAbsent(configToml.triggerholder.get(songEntry).get(trigger)[10], new ArrayList<>());
                TriggerIdentifierMap.get(decoded).get(configToml.triggerholder.get(songEntry).get(trigger)[10]).add(songEntry);
                TriggerInfoMap.putIfAbsent(decoded, configToml.triggerholder.get(songEntry).get(trigger));
                if(!configToml.triggerholder.get(songEntry).get(trigger)[10].matches("") || configToml.triggerholder.get(songEntry).get(trigger)[10].matches("_")) {
                    if (!TriggerInfoMap.containsKey(decoded + "-" + configToml.triggerholder.get(songEntry).get(trigger)[10])) {
                        TriggerInfoMap.put(decoded + "-" + configToml.triggerholder.get(songEntry).get(trigger)[10], configToml.triggerholder.get(songEntry).get(trigger));
                    }
                }
                if(checkResourceLocation(sound)) {
                    allSoundEvents.add(sound);
                    allSoundEventsTriggers.put(sound, decoded);
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) EnumHelperClient.addMusicType(songEntry, sound, 0, 0);
                }
            }
            else {
                List<String> values = new ArrayList<>();
                for(String trigger : triggers) {
                    String decoded = decode(songEntry,trigger);
                    if(configToml.triggerholder.get(songEntry).get(trigger)[6].matches("and")) {
                        songCombos.computeIfAbsent(songEntry, k -> new ArrayList<>());
                        if(configToml.triggerholder.get(songEntry).get(trigger)[10].matches("") || configToml.triggerholder.get(songEntry).get(trigger)[10].matches("_")) {
                            songCombos.get(songEntry).add(decoded);
                        }
                        else songCombos.get(songEntry).add(decoded+"-"+configToml.triggerholder.get(songEntry).get(trigger)[10]);
                        if(!Boolean.parseBoolean(configToml.triggerholder.get(songEntry).get(trigger)[32])) {
                            TriggerIdentifierMap.putIfAbsent(decoded, new HashMap<>());
                            TriggerIdentifierMap.get(decoded).putIfAbsent(configToml.triggerholder.get(songEntry).get(trigger)[10], new ArrayList<>());
                            TriggerIdentifierMap.get(decoded).get(configToml.triggerholder.get(songEntry).get(trigger)[10]).add("@"+songEntry);
                            TriggerInfoMap.putIfAbsent(decoded, configToml.triggerholder.get(songEntry).get(trigger));
                            if (!configToml.triggerholder.get(songEntry).get(trigger)[10].matches("") || configToml.triggerholder.get(songEntry).get(trigger)[10].matches("_")) {
                                if (!TriggerInfoMap.containsKey(decoded + "-" + configToml.triggerholder.get(songEntry).get(trigger)[10]))
                                    TriggerInfoMap.put(decoded + "-" + configToml.triggerholder.get(songEntry).get(trigger)[10], configToml.triggerholder.get(songEntry).get(trigger));
                            }
                        }
                    }
                    else if(configToml.triggerholder.get(songEntry).get(trigger)[6].matches("not")) {
                        if(!Boolean.parseBoolean(configToml.triggerholder.get(songEntry).get(trigger)[32])) {
                            TriggerIdentifierMap.putIfAbsent(decoded, new HashMap<>());
                            TriggerIdentifierMap.get(decoded).putIfAbsent(configToml.triggerholder.get(songEntry).get(trigger)[10], new ArrayList<>());
                            TriggerIdentifierMap.get(decoded).get(configToml.triggerholder.get(songEntry).get(trigger)[10]).add("#"+songEntry);
                            TriggerInfoMap.putIfAbsent(decoded, configToml.triggerholder.get(songEntry).get(trigger));
                            if (!configToml.triggerholder.get(songEntry).get(trigger)[10].matches("") || configToml.triggerholder.get(songEntry).get(trigger)[10].matches("_")) {
                                if (!TriggerInfoMap.containsKey(decoded + "-" + configToml.triggerholder.get(songEntry).get(trigger)[10]))
                                    TriggerInfoMap.put(decoded + "-" + configToml.triggerholder.get(songEntry).get(trigger)[10], configToml.triggerholder.get(songEntry).get(trigger));
                            }
                        }
                    }
                    else if(configToml.triggerholder.get(songEntry).get(trigger)[6].matches("instantiated")) {
                        if(configToml.triggerholder.get(songEntry).get(trigger)[10].matches("") || configToml.triggerholder.get(songEntry).get(trigger)[10].matches("_")) {
                            values.add(decoded);
                        }
                        else values.add(decoded+"-"+configToml.triggerholder.get(songEntry).get(trigger)[10]);
                    }
                    if(checkResourceLocation(sound)) {
                        allSoundEvents.add(sound);
                        allSoundEventsTriggers.put(sound, decoded);
                        if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) EnumHelperClient.addMusicType(songEntry, sound, 0, 0);
                    }
                }
                instantiatedCombos.put(songCombos.get(songEntry),values);
            }
            if(configToml.triggerlinking.get(songEntry) !=null) {
                int triggerCounter=0;
                for(String song : configToml.triggerlinking.get(songEntry).keySet()) {
                    if(triggerCounter!=0) {
                        SoundEvent soundLink = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + song)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, song));
                        boolean shouldBeAdded = true;
                        for(SoundEvent s : allSoundEvents) {
                            if(Objects.requireNonNull(soundLink.getRegistryName()).toString().matches(Objects.requireNonNull(s.getRegistryName()).toString())) shouldBeAdded = false;
                        }
                        if(shouldBeAdded) {
                            if(checkResourceLocation(soundLink)) {
                                allSoundEvents.add(soundLink);
                                allSoundEventsTriggers.put(soundLink, configToml.triggerlinking.get(songEntry).get(song)[0].split("-")[0]);
                                if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                                    EnumHelperClient.addMusicType(songEntry, soundLink, 0, 0);
                                }
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
                    triggerChecker.append(s).append("(").append(configToml.songholder.get(s.replaceAll("@", ""))).append(") ");
                }
            }
            MusicTriggers.logger.info(triggerChecker.toString());
        }
    }

    public static boolean checkResourceLocation(SoundEvent sound) {
        for(SoundEvent s : allSoundEvents) if(Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) return false;
        return true;
    }

    public static String decode(String code, String triggerID) {
        return configToml.triggerMapper.get(code).get(triggerID);
    }

    public static void emptyListsAndMaps() {
        allSoundEvents = new ArrayList<>();
        allSoundEventsTriggers = new HashMap<>();
        TriggerIdentifierMap = new HashMap<>();
        TriggerInfoMap = new HashMap<>();
        songCombos = new HashMap<>();
        antiSongs = new HashMap<>();
    }
}