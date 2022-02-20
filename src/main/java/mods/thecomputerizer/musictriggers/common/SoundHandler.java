package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.config.configToml;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.EnumHelperClient;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoundHandler {

    public static List<SoundEvent> allSoundEvents = new ArrayList<>();
    public static HashMap<SoundEvent,String> allSoundEventsTriggers = new HashMap<>();

    public static Map<String, Map<String, String>> TriggerSongMap = new HashMap<>();
    public static Map<String, String[]> TriggerInfoMap = new HashMap<>();

    public static HashMap<String, List<String>> songCombos = new HashMap<>();
    public static HashMap<String, List<String>> antiSongs = new HashMap<>();

    public static void registerSounds() {

        for (Map.Entry<String, Map<String, String[]>> stringListEntry : configToml.triggerholder.entrySet()) {
            String songEntry = ((Map.Entry) stringListEntry).getKey().toString();
            SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." +configToml.songholder.get(songEntry))).setRegistryName(new ResourceLocation(MusicTriggers.MODID, configToml.songholder.get(songEntry)));
            List<String> triggers = new ArrayList<>();
            MusicTriggers.logger.info("song "+songEntry+" has "+configToml.triggerholder.get(songEntry).keySet().size()+" trigger(s)");
            for (Map.Entry<String, String[]> nestedStringListEntry : configToml.triggerholder.get(songEntry).entrySet()) {
                String temp = ((Map.Entry) nestedStringListEntry).getKey().toString();
                if(configToml.triggerholder.get(songEntry).get(temp)[6].matches("not")) {
                    MusicTriggers.logger.info("anti trigger for "+temp);
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
                MusicTriggers.logger.info("size was 1 so putting "+songEntry+" in "+trigger);
                TriggerInfoMap.putIfAbsent(trigger, configToml.triggerholder.get(songEntry).get(trigger));
                if(!configToml.triggerholder.get(songEntry).get(trigger)[10].matches("")) {
                    if (!TriggerInfoMap.containsKey(trigger + "-" + configToml.triggerholder.get(songEntry).get(trigger)[10])) {
                        TriggerInfoMap.put(trigger + "-" + configToml.triggerholder.get(songEntry).get(trigger)[10], configToml.triggerholder.get(songEntry).get(trigger));
                    }
                }
                if(!allSoundEvents.contains(sound)) {
                    allSoundEvents.add(sound);
                    allSoundEventsTriggers.put(sound, trigger);
                    if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                        EnumHelperClient.addMusicType(songEntry, sound, 0, 0);
                    }
                }
            }
            else {
                for(String trigger : triggers) {
                    if(configToml.triggerholder.get(songEntry).get(trigger)[6].matches("and")) {
                        MusicTriggers.logger.info("Building song combo with trigger: "+trigger);
                        songCombos.computeIfAbsent(songEntry, k -> new ArrayList<>());
                        songCombos.computeIfAbsent(songEntry, k -> new ArrayList<>());
                        if(configToml.triggerholder.get(songEntry).get(trigger)[10].matches("")) {
                            songCombos.get(songEntry).add(trigger);
                            MusicTriggers.logger.info("Added some combo with blank identifier");
                        }
                        else {
                            songCombos.get(songEntry).add(trigger+"-"+configToml.triggerholder.get(songEntry).get(trigger)[10]);
                            MusicTriggers.logger.info("Added some combo with identifier of "+configToml.triggerholder.get(songEntry).get(trigger)[10]);
                        }
                        TriggerSongMap.putIfAbsent(trigger, new HashMap<>());
                        TriggerSongMap.get(trigger).putIfAbsent("@"+songEntry,configToml.triggerholder.get(songEntry).get(trigger)[10]);
                        MusicTriggers.logger.info("size wasn't 1 so putting "+songEntry+" in "+trigger);
                        TriggerInfoMap.putIfAbsent(trigger, configToml.triggerholder.get(songEntry).get(trigger));
                        if(!configToml.triggerholder.get(songEntry).get(trigger)[10].matches("")) {
                            if (!TriggerInfoMap.containsKey(trigger + "-" + configToml.triggerholder.get(songEntry).get(trigger)[10])) {
                                TriggerInfoMap.put(trigger + "-" + configToml.triggerholder.get(songEntry).get(trigger)[10], configToml.triggerholder.get(songEntry).get(trigger));
                            }
                        }
                    }
                    if(!allSoundEvents.contains(sound)) {
                        allSoundEvents.add(sound);
                        allSoundEventsTriggers.put(sound, trigger);
                        if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
                            EnumHelperClient.addMusicType(songEntry, sound, 0, 0);
                        }
                    }
                }
            }
        }
    }

    public static void emptyListsAndMaps() {
        allSoundEvents = new ArrayList<>();
        allSoundEventsTriggers = new HashMap<>();

        songCombos = new HashMap<>();
        antiSongs = new HashMap<>();
    }
}