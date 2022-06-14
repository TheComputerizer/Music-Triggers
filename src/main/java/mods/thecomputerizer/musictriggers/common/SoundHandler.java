package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.config.ConfigMain;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.EnumHelperClient;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;

import java.util.*;

public class SoundHandler {

    private final String channel;
    public final List<SoundEvent> allSoundEvents = new ArrayList<>();
    public final HashMap<SoundEvent,String> allSoundEventsTriggers = new HashMap<>();

    public final Map<String, Map<String, List<String>>> TriggerIdentifierMap = new HashMap<>();
    public final Map<String, String[]> TriggerInfoMap = new HashMap<>();

    public final HashMap<String, List<String>> songCombos = new HashMap<>();
    public final HashMap<String, List<String>> antiSongs = new HashMap<>();
    public final HashMap<List<String>, List<String>> instantiatedCombos = new HashMap<>();

    public SoundHandler(String channel) {
        this.channel = channel;
    }

    public void registerSounds(ConfigMain main, String channel) {
        for(int i = 0; i< main.songholder.entrySet().size(); i++) {
            String songEntry = "song"+i;
            MusicTriggers.logger.info("Registering sound: "+ main.songholder.get(songEntry)+" in channel "+channel);
            SoundEvent sound = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + main.songholder.get(songEntry))).setRegistryName(new ResourceLocation(MusicTriggers.MODID, main.songholder.get(songEntry)));
            List<String> triggers = new ArrayList<>();
            for (String trigger : main.triggerholder.get(songEntry).keySet()) {
                String decoded = decode(main,songEntry,trigger);
                if(main.triggerholder.get(songEntry).get(trigger)[6].matches("not")) {
                    antiSongs.computeIfAbsent(songEntry, k -> new ArrayList<>());
                    if(main.triggerholder.get(songEntry).get(trigger)[10].matches("_")) antiSongs.get(songEntry).add(decoded);
                    else antiSongs.get(songEntry).add(decoded+"-"+ main.triggerholder.get(songEntry).get(trigger)[10]);
                }
                triggers.add(trigger);
            }
            if(triggers.size()==1) {
                String trigger = triggers.get(0);
                String decoded = decode(main,songEntry,trigger);
                TriggerIdentifierMap.putIfAbsent(decoded, new HashMap<>());
                TriggerIdentifierMap.get(decoded).putIfAbsent(main.triggerholder.get(songEntry).get(trigger)[10], new ArrayList<>());
                TriggerIdentifierMap.get(decoded).get(main.triggerholder.get(songEntry).get(trigger)[10]).add(songEntry);
                TriggerInfoMap.putIfAbsent(decoded, main.triggerholder.get(songEntry).get(trigger));
                if(!main.triggerholder.get(songEntry).get(trigger)[10].matches("") || main.triggerholder.get(songEntry).get(trigger)[10].matches("_")) {
                    if (!TriggerInfoMap.containsKey(decoded + "-" + main.triggerholder.get(songEntry).get(trigger)[10])) {
                        TriggerInfoMap.put(decoded + "-" + main.triggerholder.get(songEntry).get(trigger)[10], main.triggerholder.get(songEntry).get(trigger));
                    }
                }
                if(checkResourceLocation(sound)) {
                    //allSoundEvents.add(sound);
                    //allSoundEventsTriggers.put(sound, decoded);
                    //if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) EnumHelperClient.addMusicType(songEntry, sound, 0, 0);
                }
            }
            else {
                List<String> values = new ArrayList<>();
                for(String trigger : triggers) {
                    String decoded = decode(main,songEntry,trigger);
                    if(main.triggerholder.get(songEntry).get(trigger)[6].matches("and")) {
                        songCombos.computeIfAbsent(songEntry, k -> new ArrayList<>());
                        if(main.triggerholder.get(songEntry).get(trigger)[10].matches("") || main.triggerholder.get(songEntry).get(trigger)[10].matches("_")) {
                            songCombos.get(songEntry).add(decoded);
                        }
                        else songCombos.get(songEntry).add(decoded+"-"+ main.triggerholder.get(songEntry).get(trigger)[10]);
                        if(!Boolean.parseBoolean(main.triggerholder.get(songEntry).get(trigger)[32])) {
                            TriggerIdentifierMap.putIfAbsent(decoded, new HashMap<>());
                            TriggerIdentifierMap.get(decoded).putIfAbsent(main.triggerholder.get(songEntry).get(trigger)[10], new ArrayList<>());
                            TriggerIdentifierMap.get(decoded).get(main.triggerholder.get(songEntry).get(trigger)[10]).add("@"+songEntry);
                            TriggerInfoMap.putIfAbsent(decoded, main.triggerholder.get(songEntry).get(trigger));
                            if (!main.triggerholder.get(songEntry).get(trigger)[10].matches("") || main.triggerholder.get(songEntry).get(trigger)[10].matches("_")) {
                                if (!TriggerInfoMap.containsKey(decoded + "-" + main.triggerholder.get(songEntry).get(trigger)[10]))
                                    TriggerInfoMap.put(decoded + "-" + main.triggerholder.get(songEntry).get(trigger)[10], main.triggerholder.get(songEntry).get(trigger));
                            }
                        }
                    }
                    else if(main.triggerholder.get(songEntry).get(trigger)[6].matches("not")) {
                        if(!Boolean.parseBoolean(main.triggerholder.get(songEntry).get(trigger)[32])) {
                            TriggerIdentifierMap.putIfAbsent(decoded, new HashMap<>());
                            TriggerIdentifierMap.get(decoded).putIfAbsent(main.triggerholder.get(songEntry).get(trigger)[10], new ArrayList<>());
                            TriggerIdentifierMap.get(decoded).get(main.triggerholder.get(songEntry).get(trigger)[10]).add("#"+songEntry);
                            TriggerInfoMap.putIfAbsent(decoded, main.triggerholder.get(songEntry).get(trigger));
                            if (!main.triggerholder.get(songEntry).get(trigger)[10].matches("") || main.triggerholder.get(songEntry).get(trigger)[10].matches("_")) {
                                if (!TriggerInfoMap.containsKey(decoded + "-" + main.triggerholder.get(songEntry).get(trigger)[10]))
                                    TriggerInfoMap.put(decoded + "-" + main.triggerholder.get(songEntry).get(trigger)[10], main.triggerholder.get(songEntry).get(trigger));
                            }
                        }
                    }
                    else if(main.triggerholder.get(songEntry).get(trigger)[6].matches("instantiated")) {
                        if(main.triggerholder.get(songEntry).get(trigger)[10].matches("") || main.triggerholder.get(songEntry).get(trigger)[10].matches("_")) {
                            values.add(decoded);
                        }
                        else values.add(decoded+"-"+ main.triggerholder.get(songEntry).get(trigger)[10]);
                    }
                    if(checkResourceLocation(sound)) {
                        //allSoundEvents.add(sound);
                        //allSoundEventsTriggers.put(sound, decoded);
                        //if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) EnumHelperClient.addMusicType(songEntry, sound, 0, 0);
                    }
                }
                instantiatedCombos.put(songCombos.get(songEntry),values);
            }
            if(main.triggerlinking.get(songEntry) !=null) {
                int triggerCounter=0;
                for(String song : main.triggerlinking.get(songEntry).keySet()) {
                    if(triggerCounter!=0) {
                        SoundEvent soundLink = new SoundEvent(new ResourceLocation(MusicTriggers.MODID, "music." + song)).setRegistryName(new ResourceLocation(MusicTriggers.MODID, song));
                        boolean shouldBeAdded = true;
                        for(SoundEvent s : allSoundEvents) {
                            if(Objects.requireNonNull(soundLink.getRegistryName()).toString().matches(Objects.requireNonNull(s.getRegistryName()).toString())) shouldBeAdded = false;
                        }
                        if(shouldBeAdded) {
                            if(checkResourceLocation(soundLink)) {
                                //allSoundEvents.add(soundLink);
                                //allSoundEventsTriggers.put(soundLink, main.triggerlinking.get(songEntry).get(song)[0].split("-")[0]);
                                //if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) EnumHelperClient.addMusicType(songEntry, soundLink, 0, 0);
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
                    triggerChecker.append(s).append("(").append(main.songholder.get(s.replaceAll("@", ""))).append(") ");
                }
            }
            MusicTriggers.logger.info(triggerChecker.toString());
        }
    }

    public boolean checkResourceLocation(SoundEvent sound) {
        for(SoundEvent s : allSoundEvents) if(Objects.requireNonNull(s.getRegistryName()).toString().matches(Objects.requireNonNull(sound.getRegistryName()).toString())) return false;
        return true;
    }

    public String decode(ConfigMain main, String code, String triggerID) {
        return main.triggerMapper.get(code).get(triggerID);
    }

    public void clearListsAndMaps() {
        this.allSoundEvents.clear();
        this.allSoundEventsTriggers.clear();
        this.TriggerIdentifierMap.clear();
        this.TriggerInfoMap.clear();
        this.songCombos.clear();
        this.antiSongs.clear();
        this.instantiatedCombos.clear();
    }
}