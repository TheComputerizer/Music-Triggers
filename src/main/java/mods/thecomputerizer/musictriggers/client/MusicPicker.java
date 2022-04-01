package mods.thecomputerizer.musictriggers.client;


import corgitaco.enhancedcelestials.EnhancedCelestialsWorldData;
import corgitaco.enhancedcelestials.LunarContext;
import corgitaco.enhancedcelestials.lunarevent.BloodMoon;
import corgitaco.enhancedcelestials.lunarevent.BlueMoon;
import corgitaco.enhancedcelestials.lunarevent.HarvestMoon;
import corgitaco.enhancedcelestials.lunarevent.Moon;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config.configRegistry;
import mods.thecomputerizer.musictriggers.config.configToml;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.packets.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

import java.util.*;

public class MusicPicker {
    public static Minecraft mc;
    public static Player player;
    public static Level world;

    public static HashMap<String, Integer> triggerPersistence = new HashMap<>();
    public static HashMap<Integer, Boolean> victory = new HashMap<>();
    public static boolean fishBool = false;
    public static int fishingStart = 0;
    public static boolean setPVP = false;
    public static int pvpVictoryID = 0;
    public static boolean waterBool = false;
    public static int waterStart = 0;

    public static HashMap<String, List<String>> dynamicSongs = new HashMap<>();
    public static HashMap<String, Integer> dynamicPriorities = new HashMap<>();
    public static HashMap<String, Integer> dynamicFade = new HashMap<>();
    public static HashMap<String, Integer> dynamicDelay = new HashMap<>();

    public static List<String> playableList = new ArrayList<>();
    public static List<String> titleCardEvents = new ArrayList<>();
    public static List<String> timeSwitch = new ArrayList<>();

    public static List<String> effectList = new ArrayList<>();

    public static int curFade = 0;
    public static int curDelay = 0;
    public static boolean shouldChange = false;

    public static float musicVolSave;
    public static float masterVolSave;

    public static List<String> playThese() {
        if (!MusicPlayer.fading) titleCardEvents = new ArrayList<>();
        mc = Minecraft.getInstance();
        player = mc.player;
        if (player != null) world = player.getCommandSenderWorld();
        if(SoundHandler.TriggerSongMap.isEmpty()) return null;
        if(player == null) {
            if (SoundHandler.TriggerSongMap.get("menu") != null) return new ArrayList<>(SoundHandler.TriggerSongMap.get("menu").keySet());
            else return null;
        }
        List<String> res = comboChecker(priorityHandler(playableEvents()));
        for(String event : timeSwitch) {
            if(!titleCardEvents.contains(event) && triggerPersistence.get(event) > 0) triggerPersistence.put(event, 0);
        }
        timeSwitch = new ArrayList<>();
        if (res != null && !res.isEmpty()) {
            dynamicSongs = new HashMap<>();
            dynamicPriorities = new HashMap<>();
            dynamicFade = new HashMap<>();
            return res;
        }
        dynamicSongs = new HashMap<>();
        dynamicPriorities = new HashMap<>();
        dynamicFade = new HashMap<>();
        if(SoundHandler.TriggerInfoMap.get("generic") != null) {
            curFade = Integer.parseInt(SoundHandler.TriggerInfoMap.get("generic")[1]);
            playableList.add("generic");
            titleCardEvents.add("generic");
            return new ArrayList<>(SoundHandler.TriggerSongMap.get("generic").keySet());
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static List<String> comboChecker(String st) {
        if (st == null) return null;
        List<String> playableSongs = new ArrayList<>();
        boolean skip = false;
        for (String s : dynamicSongs.get(st)) {
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.antiSongs.entrySet()) {
                String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                if (s.startsWith("#") && s.replaceAll("#","").matches(checkThis)) {
                    skip = true;
                }
            }
            if(!skip) {
                for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.songCombos.entrySet()) {
                    String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                    if (s.startsWith("@") && s.replaceAll("@", "").matches(checkThis)) {
                        if (playableList.containsAll(SoundHandler.songCombos.get(s.replaceAll("@", ""))) && SoundHandler.songCombos.get(s.replaceAll("@", "")).size() != 1) {
                            playableSongs.add(s.substring(1));
                            if (!titleCardEvents.contains(st)) {
                                titleCardEvents.addAll(SoundHandler.songCombos.get(s.replaceAll("@", "")));
                            }
                        }
                    }
                }
            }
        }
        if (playableSongs.isEmpty() && !skip) {
            for (String s : dynamicSongs.get(st)) {
                if (!s.startsWith("@")) {
                    playableSongs.add(s);
                    if (!titleCardEvents.contains(st)) {
                        titleCardEvents.add(st);
                    }
                }
            }
        }
        if (playableSongs.isEmpty()) {
            List<String> tryAgain = playableList;
            tryAgain.remove(st);
            playableList = tryAgain;
            if (playableList.isEmpty()) {
                return null;
            }
            playableSongs = comboChecker(priorityHandler(playableList));
        }
        return playableSongs;
    }

    public static String priorityHandler(List<String> sta) {
        if (sta == null) {
            return null;
        }
        int highest = -100;
        String trueHighest = "";
        for (String list : sta) {
            if (dynamicPriorities.get(list) > highest && !dynamicSongs.get(list).isEmpty()) {
                highest = dynamicPriorities.get(list);
                trueHighest = list;
            }
        }
        while (dynamicSongs.get(trueHighest) == null) {
            sta.remove(trueHighest);
            if (sta.isEmpty()) {
                return null;
            }
            for (String list : sta) {
                if (dynamicPriorities.get(list) > highest) {
                    highest = dynamicPriorities.get(list);
                    trueHighest = list;
                }
            }
        }
        if (dynamicFade != null && !dynamicFade.isEmpty()) {
            if (dynamicFade.get(trueHighest) != null) {
                curFade = dynamicFade.get(trueHighest);
            } else {
                curFade = 0;
            }
        }
        if (dynamicDelay != null && !dynamicDelay.isEmpty()) {
            if (dynamicDelay.get(trueHighest) != null) {
                curDelay = dynamicDelay.get(trueHighest);
            } else {
                curDelay = 0;
            }
        }
        return trueHighest;
    }

    @SuppressWarnings({"rawtypes", "ConstantConditions"})
    public static List<String> playableEvents() {
        String crashHelper = "";
        List<String> events = new ArrayList<>();
        try {
            double time = (double) world.dayTime() / 24000.0;
            if (time > 1) {
                time = time - (long) time;
            }
            if (SoundHandler.TriggerSongMap.get("time") != null) {
                crashHelper = "time";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("time").entrySet()) {
                    String timeSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(timeSong.replaceAll("@", "").replaceAll("#", "")).get("time")[10];
                    String selectedStartTime = SoundHandler.TriggerInfoMap.get("time-" + identifier)[8];
                    String selectedEndTime = SoundHandler.TriggerInfoMap.get("time-" + identifier)[29];
                    double transformedTimeMin;
                    double transformedTimeMax;
                    if (selectedStartTime.matches("day")) {
                        transformedTimeMin = 0d;
                        transformedTimeMax = 0.54166666666d;
                    } else if (selectedStartTime.matches("night")) {
                        transformedTimeMin = 0.54166666666d;
                        transformedTimeMax = 1d;
                    } else if (selectedStartTime.matches("sunset")) {
                        transformedTimeMin = 0.5d;
                        transformedTimeMax = 0.54166666666d;
                    } else if (selectedStartTime.matches("sunrise")) {
                        transformedTimeMin = 0.95833333333d;
                        transformedTimeMax = 1d;
                    } else {
                        double doubleStart = Double.parseDouble(selectedStartTime);
                        double doubleEnd = Double.parseDouble(selectedEndTime);
                        if(doubleEnd==-1) {
                            if(doubleStart<=21d) doubleEnd = doubleStart+3d;
                            else doubleEnd = doubleStart-21d;
                        }
                        transformedTimeMin = doubleStart / 24d;
                        transformedTimeMax = doubleEnd / 24d;
                    }
                    boolean pass;
                    if(transformedTimeMin<transformedTimeMax) pass = time >= transformedTimeMin && time < transformedTimeMax;
                    else pass = time>=transformedTimeMin || time < transformedTimeMax;
                    if (pass) {
                        if (Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[21]) == 0) {
                            if (!events.contains("time-" + identifier)) {
                                events.add("time-" + identifier);
                                dynamicSongs.put("time-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("time"), identifier));
                                dynamicPriorities.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[0]));
                                dynamicFade.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[1]));
                                dynamicDelay.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[4]));
                                triggerPersistence.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[3]));
                                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("time-" + identifier)[33])) timeSwitch.add("time-" + identifier);
                            }
                        } else if (Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[21]) == world.getMoonPhase() + 1) {
                            if (!events.contains("time-" + identifier)) {
                                events.add("time-" + identifier);
                                dynamicSongs.put("time-" + identifier, new ArrayList<>(SoundHandler.TriggerSongMap.get("time-" + identifier).keySet()));
                                dynamicPriorities.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[0]));
                                dynamicFade.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[1]));
                                dynamicDelay.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[4]));
                                triggerPersistence.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[3]));
                                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("time-" + identifier)[33])) timeSwitch.add("time-" + identifier);
                            }
                        }
                    } else if (triggerPersistence.get("time-" + identifier) != null && triggerPersistence.get("time-" + identifier) > 0) {
                        if (!events.contains("time-" + identifier)) {
                            events.add("time-" + identifier);
                            dynamicSongs.put("time-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("time"), identifier));
                            dynamicPriorities.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[0]));
                            dynamicFade.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[1]));
                            dynamicDelay.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("time-" + identifier)[33])) timeSwitch.add("time-" + identifier);
                        }
                    }
                }
            }
            if (SoundHandler.TriggerSongMap.get("light") != null) {
                crashHelper = "light";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("light").entrySet()) {
                    String lightSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(lightSong.replaceAll("@", "").replaceAll("#", "")).get("light")[10];
                    if (averageLight(roundedPos(player), Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("light-" + identifier)[20])) <= Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[2])) {
                        if (!events.contains("light-" + identifier)) {
                            events.add("light-" + identifier);
                            dynamicSongs.put("light-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("light"), identifier));
                            dynamicPriorities.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[0]));
                            dynamicFade.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[1]));
                            dynamicDelay.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[4]));
                            triggerPersistence.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("light-" + identifier)[33])) timeSwitch.add("light-" + identifier);
                        }
                    } else if (triggerPersistence.get("light-" + identifier) != null && triggerPersistence.get("light-" + identifier) > 0) {
                        if (!events.contains("light-" + identifier)) {
                            events.add("light-" + identifier);
                            dynamicSongs.put("light-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("light"), identifier));
                            dynamicPriorities.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[0]));
                            dynamicFade.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[1]));
                            dynamicDelay.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("light-" + identifier)[33])) timeSwitch.add("light-" + identifier);
                        }
                    }
                }
            }
            if (SoundHandler.TriggerSongMap.get("height") != null) {
                crashHelper = "height";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("height").entrySet()) {
                    String heightSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(heightSong.replaceAll("@", "").replaceAll("#", "")).get("height")[10];
                    boolean pass;
                    if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("height-" + identifier)[28]))
                        pass = player.getY() < Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[2]) && !world.canSeeSky(roundedPos(player));
                    else
                        pass = player.getY() > Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[2]);
                    if (pass) {
                        if (!events.contains("height-" + identifier)) {
                            events.add("height-" + identifier);
                            dynamicSongs.put("height-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("height"), identifier));
                            dynamicPriorities.put("height-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[0]));
                            dynamicFade.put("height-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[1]));
                            dynamicDelay.put("height-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[4]));
                            triggerPersistence.put("height-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("height-" + identifier)[33])) timeSwitch.add("height-" + identifier);
                        }
                    } else if (triggerPersistence.get("height-" + identifier) != null && triggerPersistence.get("height-" + identifier) > 0) {
                        if (!events.contains("height-" + identifier)) {
                            events.add("height-" + identifier);
                            dynamicSongs.put("height-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("height"), identifier));
                            dynamicPriorities.put("height-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[0]));
                            dynamicFade.put("height-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[1]));
                            dynamicDelay.put("height-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("height-" + identifier)[33])) timeSwitch.add("height-" + identifier);
                        }
                    }
                }
            }
            if (SoundHandler.TriggerInfoMap.get("elytra") != null && player.getFallFlyingTicks() > Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[8])) {
                crashHelper = "elytra";
                events.add("elytra");
                dynamicSongs.put("elytra", new ArrayList<>(SoundHandler.TriggerSongMap.get("elytra").keySet()));
                dynamicPriorities.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[0]));
                dynamicFade.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[1]));
                dynamicDelay.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[4]));
                triggerPersistence.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("elytra")[33])) timeSwitch.add("elytra");
            } else if (triggerPersistence.get("elytra") != null && triggerPersistence.get("elytra") > 0) {
                crashHelper = "elytra";
                events.add("elytra");
                dynamicSongs.put("elytra", new ArrayList<>(SoundHandler.TriggerSongMap.get("elytra").keySet()));
                dynamicPriorities.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[0]));
                dynamicFade.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[1]));
                dynamicDelay.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("elytra")[33])) timeSwitch.add("elytra");
            }
            if (player.fishing != null && player.fishing.isInWaterOrBubble()) fishBool = true;
            else fishingStart = 0;
            if (SoundHandler.TriggerInfoMap.get("fishing") != null && fishingStart > Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[8])) {
                crashHelper = "fishing";
                events.add("fishing");
                dynamicSongs.put("fishing", new ArrayList<>(SoundHandler.TriggerSongMap.get("fishing").keySet()));
                dynamicPriorities.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[0]));
                dynamicFade.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[1]));
                dynamicDelay.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[4]));
                triggerPersistence.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("fishing")[33])) timeSwitch.add("fishing");
            } else if (triggerPersistence.get("fishing") != null && triggerPersistence.get("fishing") > 0) {
                crashHelper = "fishing";
                events.add("fishing");
                dynamicSongs.put("fishing", new ArrayList<>(SoundHandler.TriggerSongMap.get("fishing").keySet()));
                dynamicPriorities.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[0]));
                dynamicFade.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[1]));
                dynamicDelay.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("fishing")[33])) timeSwitch.add("fishing");
            }
            if (world.isRaining() && SoundHandler.TriggerSongMap.get("raining") != null) {
                crashHelper = "raining";
                events.add("raining");
                dynamicSongs.put("raining", new ArrayList<>(SoundHandler.TriggerSongMap.get("raining").keySet()));
                dynamicPriorities.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[0]));
                dynamicFade.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[1]));
                dynamicDelay.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[4]));
                triggerPersistence.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("raining")[33])) timeSwitch.add("raining");
            } else if (triggerPersistence.get("raining") != null && triggerPersistence.get("raining") > 0) {
                crashHelper = "raining";
                events.add("raining");
                dynamicSongs.put("raining", new ArrayList<>(SoundHandler.TriggerSongMap.get("raining").keySet()));
                dynamicPriorities.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[0]));
                dynamicFade.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[1]));
                dynamicDelay.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("raining")[33])) timeSwitch.add("raining");
            }
            if (SoundHandler.TriggerSongMap.get("snowing") != null && world.isRaining() && !configRegistry.clientSideOnly) {
                PacketHandler.sendToServer(new InfoForSnow("snowing", roundedPos(player), player.getUUID()));
                fromServer.inSnow.putIfAbsent("snowing", false);
                if (fromServer.inSnow.get("snowing")) {
                    crashHelper = "snowing";
                    events.add("snowing");
                    dynamicSongs.put("snowing", new ArrayList<>(SoundHandler.TriggerSongMap.get("snowing").keySet()));
                    dynamicPriorities.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[0]));
                    dynamicFade.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[1]));
                    dynamicDelay.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[4]));
                    triggerPersistence.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[3]));
                    if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("snowing")[33])) timeSwitch.add("snowing");
                } else if (triggerPersistence.get("snowing") != null && triggerPersistence.get("snowing") > 0) {
                    crashHelper = "snowing";
                    events.add("snowing");
                    dynamicSongs.put("snowing", new ArrayList<>(SoundHandler.TriggerSongMap.get("snowing").keySet()));
                    dynamicPriorities.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[0]));
                    dynamicFade.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[1]));
                    dynamicDelay.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[4]));
                    if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("snowing")[33])) timeSwitch.add("snowing");
                }
            }
            if (world.isThundering() && SoundHandler.TriggerSongMap.get("storming") != null) {
                crashHelper = "storming";
                events.add("storming");
                dynamicSongs.put("storming", new ArrayList<>(SoundHandler.TriggerSongMap.get("storming").keySet()));
                dynamicPriorities.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[0]));
                dynamicFade.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[1]));
                dynamicDelay.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[4]));
                triggerPersistence.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("storming")[33])) timeSwitch.add("storming");
            } else if (triggerPersistence.get("storming") != null && triggerPersistence.get("storming") > 0) {
                crashHelper = "storming";
                events.add("storming");
                dynamicSongs.put("storming", new ArrayList<>(SoundHandler.TriggerSongMap.get("storming").keySet()));
                dynamicPriorities.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[0]));
                dynamicFade.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[1]));
                dynamicDelay.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("storming")[33])) timeSwitch.add("storming");
            }
            if (SoundHandler.TriggerInfoMap.get("lowhp") != null && player.getHealth() < player.getMaxHealth() * (Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[2]) / 100F)) {
                crashHelper = "lowhp";
                events.add("lowhp");
                dynamicSongs.put("lowhp", new ArrayList<>(SoundHandler.TriggerSongMap.get("lowhp").keySet()));
                dynamicPriorities.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[0]));
                dynamicFade.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[1]));
                dynamicDelay.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[4]));
                triggerPersistence.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("lowhp")[33])) timeSwitch.add("lowhp");
            } else if (triggerPersistence.get("lowhp") != null && triggerPersistence.get("lowhp") > 0) {
                crashHelper = "lowhp";
                events.add("lowhp");
                dynamicSongs.put("lowhp", new ArrayList<>(SoundHandler.TriggerSongMap.get("lowhp").keySet()));
                dynamicPriorities.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[0]));
                dynamicFade.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[1]));
                dynamicDelay.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("lowhp")[33])) timeSwitch.add("lowhp");
            }
            if (player.isDeadOrDying() && SoundHandler.TriggerSongMap.get("dead") != null) {
                crashHelper = "dead";
                events.add("dead");
                dynamicSongs.put("dead", new ArrayList<>(SoundHandler.TriggerSongMap.get("dead").keySet()));
                dynamicPriorities.put("dead", Integer.parseInt(SoundHandler.TriggerInfoMap.get("dead")[0]));
                dynamicFade.put("dead", Integer.parseInt(SoundHandler.TriggerInfoMap.get("dead")[1]));
                dynamicDelay.put("dead", Integer.parseInt(SoundHandler.TriggerInfoMap.get("dead")[4]));
                for (Map.Entry<Integer, Boolean> integerListEntry : victory.entrySet()) {
                    int key = integerListEntry.getKey();
                    victory.put(key, false);
                }
                triggerPersistence.put("dead", Integer.parseInt(SoundHandler.TriggerInfoMap.get("dead")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("dead")[33])) timeSwitch.add("dead");
            } else if (triggerPersistence.get("dead") != null && triggerPersistence.get("dead") > 0) {
                crashHelper = "dead";
                events.add("dead");
                dynamicSongs.put("dead", new ArrayList<>(SoundHandler.TriggerSongMap.get("dead").keySet()));
                dynamicPriorities.put("dead", Integer.parseInt(SoundHandler.TriggerInfoMap.get("dead")[0]));
                dynamicFade.put("dead", Integer.parseInt(SoundHandler.TriggerInfoMap.get("dead")[1]));
                dynamicDelay.put("dead", Integer.parseInt(SoundHandler.TriggerInfoMap.get("dead")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("dead")[33])) timeSwitch.add("dead");
            }
            if (player.isSpectator() && SoundHandler.TriggerSongMap.get("spectator") != null) {
                crashHelper = "spectator";
                events.add("spectator");
                dynamicSongs.put("spectator", new ArrayList<>(SoundHandler.TriggerSongMap.get("spectator").keySet()));
                dynamicPriorities.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[0]));
                dynamicFade.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[1]));
                dynamicDelay.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[4]));
                triggerPersistence.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("spectator")[33])) timeSwitch.add("spectator");
            } else if (triggerPersistence.get("spectator") != null && triggerPersistence.get("spectator") > 0) {
                crashHelper = "spectator";
                events.add("spectator");
                dynamicSongs.put("spectator", new ArrayList<>(SoundHandler.TriggerSongMap.get("spectator").keySet()));
                dynamicPriorities.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[0]));
                dynamicFade.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[1]));
                dynamicDelay.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("spectator")[33])) timeSwitch.add("spectator");
            }
            if (player.isCreative() && SoundHandler.TriggerSongMap.get("creative") != null) {
                crashHelper = "creative";
                events.add("creative");
                dynamicSongs.put("creative", new ArrayList<>(SoundHandler.TriggerSongMap.get("creative").keySet()));
                dynamicPriorities.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[0]));
                dynamicFade.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[1]));
                dynamicDelay.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[4]));
                triggerPersistence.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("creative")[33])) timeSwitch.add("creative");
            } else if (triggerPersistence.get("creative") != null && triggerPersistence.get("creative") > 0) {
                crashHelper = "creative";
                events.add("creative");
                dynamicSongs.put("creative", new ArrayList<>(SoundHandler.TriggerSongMap.get("creative").keySet()));
                dynamicPriorities.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[0]));
                dynamicFade.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[1]));
                dynamicDelay.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("creative")[33])) timeSwitch.add("creative");
            }
            if (SoundHandler.TriggerSongMap.get("riding") != null && player.isPassenger()) {
                crashHelper = "riding";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("riding").entrySet()) {
                    String ridingSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(ridingSong.replaceAll("@", "").replaceAll("#", "")).get("riding")[10];
                    String ridingName = SoundHandler.TriggerInfoMap.get("riding-" + identifier)[9];
                    if (Objects.requireNonNull(player.getControllingPassenger()).getName().getString().matches(ridingName) || Objects.requireNonNull(ForgeRegistries.ENTITIES.getKey(player.getControllingPassenger().getType())).toString().matches(ridingName) || ridingName.matches("minecraft")) {
                        if (!events.contains("riding-" + identifier)) {
                            events.add("riding-" + identifier);
                            dynamicSongs.put("riding-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("riding"), identifier));
                            dynamicPriorities.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[0]));
                            dynamicFade.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[1]));
                            dynamicDelay.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[4]));
                            triggerPersistence.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[33])) timeSwitch.add("riding-" + identifier);
                        }
                    } else if (triggerPersistence.get("riding-" + identifier) != null && triggerPersistence.get("riding-" + identifier) > 0) {
                        if (!events.contains("riding-" + identifier)) {
                            events.add("riding-" + identifier);
                            dynamicSongs.put("riding-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("riding"), identifier));
                            dynamicPriorities.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[0]));
                            dynamicFade.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[1]));
                            dynamicDelay.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[33])) timeSwitch.add("riding-" + identifier);
                        }
                    }
                }
            }
            if ((world.getBlockState(roundedPos(player)).getMaterial() == Material.WATER || world.getBlockState(roundedPos(player)).getMaterial() == Material.WATER_PLANT || world.getBlockState(roundedPos(player)).getMaterial() == Material.REPLACEABLE_WATER_PLANT) && (world.getBlockState(roundedPos(player).above()).getMaterial() == Material.WATER || world.getBlockState(roundedPos(player).above()).getMaterial() == Material.WATER_PLANT || world.getBlockState(roundedPos(player).above()).getMaterial() == Material.REPLACEABLE_WATER_PLANT)) {
                waterBool = true;
            } else waterStart = 0;
            if (SoundHandler.TriggerSongMap.get("underwater") != null && waterStart > Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[8])) {
                crashHelper = "underwater";
                events.add("underwater");
                dynamicSongs.put("underwater", new ArrayList<>(SoundHandler.TriggerSongMap.get("underwater").keySet()));
                dynamicPriorities.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[0]));
                dynamicFade.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[1]));
                dynamicDelay.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[4]));
                triggerPersistence.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("underwater")[33])) timeSwitch.add("underwater");
            } else if (triggerPersistence.get("underwater") != null && triggerPersistence.get("underwater") > 0) {
                crashHelper = "underwater";
                events.add("underwater");
                dynamicSongs.put("underwater", new ArrayList<>(SoundHandler.TriggerSongMap.get("underwater").keySet()));
                dynamicPriorities.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[0]));
                dynamicFade.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[1]));
                dynamicDelay.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("underwater")[33])) timeSwitch.add("underwater");
            }
            for (LivingEntity ent : world.getEntitiesOfClass(LivingEntity.class, new AABB(player.getX() - 16, player.getY() - 8, player.getZ() - 16, player.getX() + 16, player.getY() + 8, player.getZ() + 16))) {
                if ((ent instanceof TamableAnimal && ent.serializeNBT() != null && ent.serializeNBT().getString("Owner").matches(player.getStringUUID())) && SoundHandler.TriggerSongMap.get("pet") != null) {
                    crashHelper = "pet";
                    events.add("pet");
                    dynamicSongs.put("pet", new ArrayList<>(SoundHandler.TriggerSongMap.get("pet").keySet()));
                    dynamicPriorities.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[0]));
                    dynamicFade.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[1]));
                    dynamicDelay.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[4]));
                    triggerPersistence.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[3]));
                    if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("pet")[33])) timeSwitch.add("pet");
                    break;
                }
            }
            if (triggerPersistence.get("pet") != null && triggerPersistence.get("pet") > 0) {
                crashHelper = "pet";
                events.add("pet");
                dynamicSongs.put("pet", new ArrayList<>(SoundHandler.TriggerSongMap.get("pet").keySet()));
                dynamicPriorities.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[0]));
                dynamicFade.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[1]));
                dynamicDelay.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("pet")[33])) timeSwitch.add("pet");
            }
            if (triggerPersistence.get("drowning") != null && player.getAirSupply() < Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[2])) {
                crashHelper = "drowning";
                events.add("drowning");
                dynamicSongs.put("drowning", new ArrayList<>(SoundHandler.TriggerSongMap.get("drowning").keySet()));
                dynamicPriorities.put("drowning", Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[0]));
                dynamicFade.put("drowning", Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[1]));
                dynamicDelay.put("drowning", Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[4]));
                triggerPersistence.put("drowning", Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("drowning")[33])) timeSwitch.add("drowning");
            } else if (triggerPersistence.get("drowning") != null && triggerPersistence.get("drowning") > 0) {
                crashHelper = "drowning";
                events.add("drowning");
                dynamicSongs.put("drowning", new ArrayList<>(SoundHandler.TriggerSongMap.get("drowning").keySet()));
                dynamicPriorities.put("drowning", Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[0]));
                dynamicFade.put("drowning", Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[1]));
                dynamicDelay.put("drowning", Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("drowning")[33])) timeSwitch.add("drowning");
            }
            if (triggerPersistence.get("pvp") != null && setPVP) {
                crashHelper = "pvp";
                events.add("pvp");
                dynamicSongs.put("pvp", new ArrayList<>(SoundHandler.TriggerSongMap.get("pvp").keySet()));
                dynamicPriorities.put("pvp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pvp")[0]));
                dynamicFade.put("pvp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pvp")[1]));
                dynamicDelay.put("pvp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pvp")[4]));
                triggerPersistence.put("pvp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pvp")[3]));
                triggerPersistence.put("pvp-victory_timeout", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pvp")[22]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("pvp")[33])) timeSwitch.add("pvp");
                setPVP = false;
                pvpVictoryID = Integer.parseInt(SoundHandler.TriggerInfoMap.get("pvp")[17]);
                victory.putIfAbsent(pvpVictoryID, false);
            } else if (triggerPersistence.get("pvp") != null && triggerPersistence.get("pvp") > 0) {
                crashHelper = "pvp";
                events.add("pvp");
                dynamicSongs.put("pvp", new ArrayList<>(SoundHandler.TriggerSongMap.get("pvp").keySet()));
                dynamicPriorities.put("pvp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pvp")[0]));
                dynamicFade.put("pvp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pvp")[1]));
                dynamicDelay.put("pvp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pvp")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("pvp")[33])) timeSwitch.add("pvp");
            }
            if (triggerPersistence.get("pvp") != null && eventsClient.PVPTracker != null && triggerPersistence.get("victory_timeout") <= 0) {
                eventsClient.PVPTracker = null;
            }
            if (eventsClient.PVPTracker != null && eventsClient.PVPTracker.isDeadOrDying()) {
                victory.put(pvpVictoryID, true);
                eventsClient.PVPTracker = null;
            }
            if (triggerPersistence.get("home") != null && !configRegistry.clientSideOnly) {
                crashHelper = "home";
                PacketHandler.sendToServer(new InfoForHome("home", roundedPos(player), player.getUUID(), SoundHandler.TriggerInfoMap.get("home")[11]));
                fromServer.inHomeRange.putIfAbsent("home", false);
                if (fromServer.inHomeRange.get("home")) {
                    events.add("home");
                    dynamicSongs.put("home", new ArrayList<>(SoundHandler.TriggerSongMap.get("home").keySet()));
                    dynamicPriorities.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[0]));
                    dynamicFade.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[1]));
                    dynamicDelay.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[4]));
                    triggerPersistence.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[3]));
                    if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("home")[33])) timeSwitch.add("home");
                } else if (triggerPersistence.get("home") > 0) {
                    events.add("home");
                    dynamicSongs.put("home", new ArrayList<>(SoundHandler.TriggerSongMap.get("home").keySet()));
                    dynamicPriorities.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[0]));
                    dynamicFade.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[1]));
                    dynamicDelay.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[4]));
                    if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("home")[33])) timeSwitch.add("home");
                }
            }
            if (SoundHandler.TriggerSongMap.get("dimension") != null) {
                crashHelper = "dimension";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("dimension").entrySet()) {
                    String dimensionSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(dimensionSong.replaceAll("@", "").replaceAll("#", "")).get("dimension")[10];
                    if (player.level.dimension().location().toString().contains(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[9])) {
                        if (!events.contains("dimension-" + identifier)) {
                            events.add("dimension-" + identifier);
                            dynamicSongs.put("dimension-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("dimension"), identifier));
                            dynamicPriorities.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[0]));
                            dynamicFade.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[1]));
                            dynamicDelay.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[4]));
                            triggerPersistence.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[33])) timeSwitch.add("dimension-" + identifier);
                        }
                    } else if (triggerPersistence.get("dimension-" + identifier) != null && triggerPersistence.get("dimension-" + identifier) > 0) {
                        if (!events.contains("dimension-" + identifier)) {
                            events.add("dimension-" + identifier);
                            dynamicSongs.put("dimension-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("dimension"), identifier));
                            dynamicPriorities.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[0]));
                            dynamicFade.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[1]));
                            dynamicDelay.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[33])) timeSwitch.add("dimension-" + identifier);
                        }
                    }
                }
            }
            if (SoundHandler.TriggerSongMap.get("biome") != null) {
                crashHelper = "biome";
                if (world.getBiome(roundedPos(player)).getRegistryName() != null) {
                    fromServer.curBiome = world.getBiome(roundedPos(player)).getRegistryName().toString();
                    for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("biome").entrySet()) {
                        String biomeSong = ((Map.Entry) stringListEntry).getKey().toString();
                        String identifier = configToml.triggerholder.get(biomeSong.replaceAll("@", "").replaceAll("#", "")).get("biome")[10];
                        boolean pass = checkBiome(world.getBiome(roundedPos(player)), SoundHandler.TriggerInfoMap.get("biome-" + identifier)[9],
                                SoundHandler.TriggerInfoMap.get("biome-" + identifier)[23], SoundHandler.TriggerInfoMap.get("biome-" + identifier)[24],
                                Float.parseFloat(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[25]), Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[26]),
                                Float.parseFloat(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[30]), Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[31]));
                        if (pass) {
                            if (!events.contains("biome-" + identifier)) {
                                events.add("biome-" + identifier);
                                dynamicSongs.put("biome-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("biome"), identifier));
                                dynamicPriorities.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[0]));
                                dynamicFade.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[1]));
                                dynamicDelay.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[4]));
                                triggerPersistence.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[3]));
                                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[33])) timeSwitch.add("biome-" + identifier);
                            }
                        } else if (triggerPersistence.get("biome-" + identifier) != null && triggerPersistence.get("biome-" + identifier) > 0) {
                            if (!events.contains("biome-" + identifier)) {
                                events.add("biome-" + identifier);
                                dynamicSongs.put("biome-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("biome"), identifier));
                                dynamicPriorities.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[0]));
                                dynamicFade.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[1]));
                                dynamicDelay.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[4]));
                                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[33])) timeSwitch.add("biome-" + identifier);
                            }
                        }
                    }
                } else if (!configRegistry.clientSideOnly) {
                    for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("biome").entrySet()) {
                        String biomeSong = ((Map.Entry) stringListEntry).getKey().toString();
                        String identifier = configToml.triggerholder.get(biomeSong.replaceAll("@", "").replaceAll("#", "")).get("biome")[10];
                        PacketHandler.sendToServer(new InfoForBiome("biome-" + identifier, SoundHandler.TriggerInfoMap.get("biome-" + identifier)[9], roundedPos(player), player.getUUID(),
                                SoundHandler.TriggerInfoMap.get("biome-" + identifier)[23], SoundHandler.TriggerInfoMap.get("biome-" + identifier)[24],
                                SoundHandler.TriggerInfoMap.get("biome-" + identifier)[25], SoundHandler.TriggerInfoMap.get("biome-" + identifier)[26],
                                SoundHandler.TriggerInfoMap.get("biome-" + identifier)[30], SoundHandler.TriggerInfoMap.get("biome-" + identifier)[31]));
                        fromServer.inBiome.putIfAbsent("biome-" + identifier, false);
                        if (fromServer.inBiome.get("biome-" + identifier)) {
                            if (!events.contains("biome-" + identifier)) {
                                events.add("biome-" + identifier);
                                dynamicSongs.put("biome-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("biome"), identifier));
                                dynamicPriorities.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[0]));
                                dynamicFade.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[1]));
                                dynamicDelay.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[4]));
                                triggerPersistence.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[3]));
                                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[33])) timeSwitch.add("biome-" + identifier);
                            }
                        } else if (triggerPersistence.get("biome-" + identifier) != null && triggerPersistence.get("biome-" + identifier) > 0) {
                            if (!events.contains("biome-" + identifier)) {
                                events.add("biome-" + identifier);
                                dynamicSongs.put("biome-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("biome"), identifier));
                                dynamicPriorities.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[0]));
                                dynamicFade.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[1]));
                                dynamicDelay.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[4]));
                                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[33])) timeSwitch.add("biome-" + identifier);
                            }
                        }
                    }
                }
            }
            if (!configRegistry.clientSideOnly && SoundHandler.TriggerSongMap.get("structure") != null) {
                crashHelper = "structure";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("structure").entrySet()) {
                    String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(structureSong.replaceAll("@", "").replaceAll("#", "")).get("structure")[10];
                    PacketHandler.sendToServer(new InfoForStructure("structure-" + identifier, SoundHandler.TriggerInfoMap.get("structure-" + identifier)[9], player.blockPosition(), player.getUUID()));
                    fromServer.inStructure.putIfAbsent("structure-" + identifier, false);
                    if (fromServer.inStructure.get("structure-" + identifier)) {
                        if (!events.contains("structure-" + identifier)) {
                            events.add("structure-" + identifier);
                            dynamicSongs.put("structure-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("structure"), identifier));
                            dynamicPriorities.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[0]));
                            dynamicFade.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[1]));
                            dynamicDelay.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[4]));
                            triggerPersistence.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[33])) timeSwitch.add("structure-" + identifier);
                        }
                        fromServer.curStruct = SoundHandler.TriggerInfoMap.get("structure-" + identifier)[9];
                    } else if (triggerPersistence.get("structure-" + identifier) != null && triggerPersistence.get("structure-" + identifier) > 0) {
                        if (!events.contains("structure-" + identifier)) {
                            events.add("structure-" + identifier);
                            dynamicSongs.put("structure-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("structure"), identifier));
                            dynamicPriorities.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[0]));
                            dynamicFade.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[1]));
                            dynamicDelay.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[33])) timeSwitch.add("structure-" + identifier);
                        }
                    }
                }
            }
            if (SoundHandler.TriggerSongMap.get("mob") != null && !configRegistry.clientSideOnly) {
                crashHelper = "mob";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("mob").entrySet()) {
                    String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(structureSong.replaceAll("@", "").replaceAll("#", "")).get("mob")[10];
                    triggerPersistence.putIfAbsent("mob-" + identifier, 0);
                    PacketHandler.sendToServer(new InfoForMob("mob-" + identifier, player.getUUID(),
                            SoundHandler.TriggerInfoMap.get("mob-" + identifier)[9], SoundHandler.TriggerInfoMap.get("mob-" + identifier)[11],
                            SoundHandler.TriggerInfoMap.get("mob-" + identifier)[12], SoundHandler.TriggerInfoMap.get("mob-" + identifier)[13],
                            SoundHandler.TriggerInfoMap.get("mob-" + identifier)[14], SoundHandler.TriggerInfoMap.get("mob-" + identifier)[15],
                            SoundHandler.TriggerInfoMap.get("mob-" + identifier)[16], SoundHandler.TriggerInfoMap.get("mob-" + identifier)[17],
                            SoundHandler.TriggerInfoMap.get("mob-" + identifier)[18], SoundHandler.TriggerInfoMap.get("mob-" + identifier)[2],
                            triggerPersistence.get("mob-" + identifier), Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[22]),
                            SoundHandler.TriggerInfoMap.get("mob-" + identifier)[27]));
                    fromServer.mob.putIfAbsent("mob-" + identifier, false);
                    if (fromServer.mob.get("mob-" + identifier)) {
                        if (!events.contains("mob-" + identifier)) {
                            events.add("mob-" + identifier);
                            dynamicSongs.put("mob-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("mob"), identifier));
                            dynamicPriorities.put("mob-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[0]));
                            dynamicFade.put("mob-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[1]));
                            dynamicDelay.put("mob-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[33])) timeSwitch.add("mob-" + identifier);
                        }
                    }
                }
                for (Map.Entry<Integer, Boolean> integerBooleanEntry : fromServer.mobVictory.entrySet()) {
                    int victoryIDS = (integerBooleanEntry).getKey();
                    if (fromServer.mobVictory.get(victoryIDS)) {
                        victory.put(victoryIDS, true);
                    }
                }
            }
            if (SoundHandler.TriggerSongMap.get("zones") != null) {
                crashHelper = "zones";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("zones").entrySet()) {
                    String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(structureSong.replaceAll("@", "").replaceAll("#", "")).get("zones")[10];
                    String[] broken = stringBreaker(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[7], ",");
                    BlockPos bp = roundedPos(player);
                    int x1 = Integer.parseInt(broken[0]);
                    int y1 = Integer.parseInt(broken[1]);
                    int z1 = Integer.parseInt(broken[2]);
                    int x2 = Integer.parseInt(broken[3]);
                    int y2 = Integer.parseInt(broken[4]);
                    int z2 = Integer.parseInt(broken[5]);
                    if (bp.getX() > x1 && bp.getX() < x2 && bp.getY() > y1 && bp.getY() < y2 && bp.getZ() > z1 && bp.getZ() < z2) {
                        if (!events.contains("zones-" + identifier)) {
                            events.add("zones-" + identifier);
                            dynamicSongs.put("zones-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("zones"), identifier));
                            dynamicPriorities.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[0]));
                            dynamicFade.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[1]));
                            dynamicDelay.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[4]));
                            triggerPersistence.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[33])) timeSwitch.add("zones-" + identifier);
                        }
                    } else if (triggerPersistence.get("zones-" + identifier) != null && triggerPersistence.get("zones-" + identifier) > 0) {
                        if (!events.contains("zones-" + identifier)) {
                            events.add("zones-" + identifier);
                            dynamicSongs.put("zones-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("zones"), identifier));
                            dynamicPriorities.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[0]));
                            dynamicFade.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[1]));
                            dynamicDelay.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[33])) timeSwitch.add("zones-" + identifier);
                        }
                    }
                }
            }
            if (SoundHandler.TriggerSongMap.get("effect") != null) {
                crashHelper = "effect";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("effect").entrySet()) {
                    String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(structureSong.replaceAll("@", "").replaceAll("#", "")).get("effect")[10];
                    effectList = new ArrayList<>();
                    for (MobEffect p : player.getActiveEffectsMap().keySet()) {
                        effectList.add(p.getRegistryName().toString());
                        if (p.getRegistryName().toString().contains(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[9])) {
                            if (!events.contains("effect-" + identifier)) {
                                events.add("effect-" + identifier);
                                dynamicSongs.put("effect-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("effect"), identifier));
                                dynamicPriorities.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[0]));
                                dynamicFade.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[1]));
                                dynamicDelay.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[4]));
                                triggerPersistence.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[3]));
                                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[33])) timeSwitch.add("effect-" + identifier);
                            }
                        } else if (triggerPersistence.get("effect-" + identifier) != null && triggerPersistence.get("effect-" + identifier) > 0) {
                            if (!events.contains("effect-" + identifier)) {
                                events.add("effect-" + identifier);
                                dynamicSongs.put("effect-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("effect"), identifier));
                                dynamicPriorities.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[0]));
                                dynamicFade.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[1]));
                                dynamicDelay.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[4]));
                                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[33])) timeSwitch.add("effect-" + identifier);
                            }
                        }
                    }
                }
            }
            if (SoundHandler.TriggerSongMap.get("victory") != null) {
                crashHelper = "victory";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("victory").entrySet()) {
                    String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(structureSong.replaceAll("@", "").replaceAll("#", "")).get("victory")[10];
                    if (victory.get(Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[17]))) {
                        if (!events.contains("victory-" + identifier)) {
                            events.add("victory-" + identifier);
                            dynamicSongs.put("victory-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("victory"), identifier));
                            dynamicPriorities.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[0]));
                            dynamicFade.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[1]));
                            dynamicDelay.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[4]));
                            triggerPersistence.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[33])) timeSwitch.add("victory-" + identifier);
                        }
                        victory.put(Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[17]), false);
                    } else if (triggerPersistence.get("victory-" + identifier) != null && triggerPersistence.get("victory-" + identifier) > 0) {
                        if (!events.contains("victory-" + identifier)) {
                            events.add("victory-" + identifier);
                            dynamicSongs.put("victory-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("victory"), identifier));
                            dynamicPriorities.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[0]));
                            dynamicFade.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[1]));
                            dynamicDelay.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[33])) timeSwitch.add("victory-" + identifier);
                        }
                    }
                }
            }
            if (mc.screen != null && SoundHandler.TriggerSongMap.get("gui") != null) {
                crashHelper = "gui";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("gui").entrySet()) {
                    String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(structureSong.replaceAll("@", "").replaceAll("#", "")).get("gui")[10];
                    if (mc.screen.toString().contains(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[9])) {
                        if (!events.contains("gui-" + identifier)) {
                            events.add("gui-" + identifier);
                            dynamicSongs.put("gui-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("gui"), identifier));
                            dynamicPriorities.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[0]));
                            dynamicFade.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[1]));
                            dynamicDelay.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[4]));
                            triggerPersistence.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[33])) timeSwitch.add("gui-" + identifier);
                        }
                    } else if (SoundHandler.TriggerInfoMap.get("gui-" + identifier)[9].matches("CREDITS") && mc.screen instanceof WinScreen) {
                        if (!events.contains("gui-" + identifier)) {
                            events.add("gui-" + identifier);
                            dynamicSongs.put("gui-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("gui"), identifier));
                            dynamicPriorities.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[0]));
                            dynamicFade.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[1]));
                            dynamicDelay.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[4]));
                            triggerPersistence.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[33])) timeSwitch.add("gui-" + identifier);
                        }
                    } else if (triggerPersistence.get("gui-" + identifier) != null && triggerPersistence.get("gui-" + identifier) > 0) {
                        if (!events.contains("gui-" + identifier)) {
                            events.add("gui-" + identifier);
                            dynamicSongs.put("gui-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("gui"), identifier));
                            dynamicPriorities.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[0]));
                            dynamicFade.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[1]));
                            dynamicDelay.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[33])) timeSwitch.add("gui-" + identifier);
                        }
                    }
                }
            } else {
                musicVolSave = mc.options.getSoundSourceVolume(SoundSource.MUSIC);
                masterVolSave = mc.options.getSoundSourceVolume(SoundSource.MASTER);
            }
            if (SoundHandler.TriggerSongMap.get("difficulty") != null) {
                crashHelper = "difficulty";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("difficulty").entrySet()) {
                    String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(structureSong.replaceAll("@", "").replaceAll("#", "")).get("difficulty")[10];
                    int diffID = Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[2]);
                    if (diffID == 4 && world.getLevelData().isHardcore()) {
                        if (!events.contains("difficulty-" + identifier)) {
                            events.add("difficulty-" + identifier);
                            dynamicSongs.put("difficulty-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("difficulty"), identifier));
                            dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                            dynamicFade.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                            dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                            triggerPersistence.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[33])) timeSwitch.add("difficulty-" + identifier);
                        }
                    } else if (diffID == 3 && mc.level.getDifficulty() == Difficulty.HARD) {
                        if (!events.contains("difficulty-" + identifier)) {
                            events.add("difficulty-" + identifier);
                            dynamicSongs.put("difficulty-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("difficulty"), identifier));
                            dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                            dynamicFade.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                            dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                            triggerPersistence.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[33])) timeSwitch.add("difficulty-" + identifier);
                        }
                    } else if (diffID == 2 && mc.level.getDifficulty() == Difficulty.NORMAL) {
                        if (!events.contains("difficulty-" + identifier)) {
                            events.add("difficulty-" + identifier);
                            dynamicSongs.put("difficulty-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("difficulty"), identifier));
                            dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                            dynamicFade.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                            dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                            triggerPersistence.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[33])) timeSwitch.add("difficulty-" + identifier);
                        }
                    } else if (diffID == 1 && mc.level.getDifficulty() == Difficulty.EASY) {
                        if (!events.contains("difficulty-" + identifier)) {
                            events.add("difficulty-" + identifier);
                            dynamicSongs.put("difficulty-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("difficulty"), identifier));
                            dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                            dynamicFade.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                            dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                            triggerPersistence.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[33])) timeSwitch.add("difficulty-" + identifier);
                        }
                    } else if (diffID == 0 && mc.level.getDifficulty() == Difficulty.PEACEFUL) {
                        if (!events.contains("difficulty-" + identifier)) {
                            events.add("difficulty-" + identifier);
                            dynamicSongs.put("difficulty-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("difficulty"), identifier));
                            dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                            dynamicFade.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                            dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                            triggerPersistence.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[33])) timeSwitch.add("difficulty-" + identifier);
                        }
                    } else if (triggerPersistence.get("difficulty-" + identifier) != null && triggerPersistence.get("difficulty-" + identifier) > 0) {
                        if (!events.contains("difficulty-" + identifier)) {
                            events.add("difficulty-" + identifier);
                            dynamicSongs.put("difficulty-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("difficulty"), identifier));
                            dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                            dynamicFade.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                            dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[33])) timeSwitch.add("difficulty-" + identifier);
                        }
                    }
                }
            }
            if (SoundHandler.TriggerSongMap.get("advancement") != null) {
                crashHelper = "advancement";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("advancement").entrySet()) {
                    String advancementSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(advancementSong.replaceAll("@", "").replaceAll("#", "")).get("advancement")[10];
                    if (eventsClient.advancement && (eventsClient.lastAdvancement.contains(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[5]) || SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[5].matches("YouWillNeverGuessThis"))) {
                        if (!events.contains("advancement-" + identifier)) {
                            events.add("advancement-" + identifier);
                            dynamicSongs.put("advancement-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("advancement"), identifier));
                            dynamicPriorities.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[0]));
                            dynamicFade.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[1]));
                            dynamicDelay.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[4]));
                            triggerPersistence.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[33])) timeSwitch.add("advancement-" + identifier);
                        }
                    } else if (triggerPersistence.get("advancement-" + identifier) != null && triggerPersistence.get("advancement-" + identifier) > 0) {
                        if (!events.contains("advancement-" + identifier)) {
                            events.add("advancement-" + identifier);
                            dynamicSongs.put("advancement-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("advancement"), identifier));
                            dynamicPriorities.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[0]));
                            dynamicFade.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[1]));
                            dynamicDelay.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[33])) timeSwitch.add("advancement-" + identifier);
                        }
                    }
                }
                eventsClient.advancement = false;
            }
            if (!configRegistry.clientSideOnly && SoundHandler.TriggerSongMap.get("raid") != null) {
                crashHelper = "raid";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("raid").entrySet()) {
                    String raidSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(raidSong.replaceAll("@", "").replaceAll("#", "")).get("raid")[10];
                    PacketHandler.sendToServer(new InfoForRaid("raid-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[2]), player.blockPosition(), player.getUUID()));
                    fromServer.isRaid.putIfAbsent("raid-" + identifier, false);
                    if (fromServer.isRaid.get("raid-" + identifier)) {
                        if (!events.contains("raid-" + identifier)) {
                            events.add("raid-" + identifier);
                            dynamicSongs.put("raid-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("raid"), identifier));
                            dynamicPriorities.put("raid-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[0]));
                            dynamicFade.put("raid-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[1]));
                            dynamicDelay.put("raid-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[4]));
                            triggerPersistence.put("raid-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[33])) timeSwitch.add("raid-" + identifier);
                        }
                        fromServer.curStruct = SoundHandler.TriggerInfoMap.get("raid-" + identifier)[9];
                    } else if (triggerPersistence.get("raid-" + identifier) != null && triggerPersistence.get("raid-" + identifier) > 0) {
                        if (!events.contains("raid-" + identifier)) {
                            events.add("raid-" + identifier);
                            dynamicSongs.put("raid-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("raid"), identifier));
                            dynamicPriorities.put("raid-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[0]));
                            dynamicFade.put("raid-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[1]));
                            dynamicDelay.put("raid-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[33])) timeSwitch.add("raid-" + identifier);
                        }
                    }
                }
            }
            boolean bloodmoon = bloodmoon();
            if (bloodmoon) {
                events.add("bloodmoon");
            }
            boolean harvestmoon = harvestmoon();
            if (harvestmoon) {
                events.add("harvestmoon");
            }
            boolean bluemoon = bluemoon();
            if (bluemoon) {
                events.add("bluemoon");
            }
            List<String> moon = moon();
            if (!moon.isEmpty()) {
                events.addAll(moon);
            }
            List<String> seasons = seasons();
            if (!seasons.isEmpty()) {
                events.addAll(seasons);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("There was a problem with your "+crashHelper+" trigger! See the log for the full stack trace");
        }
        playableList = events;
        PacketHandler.sendToServer(new AllTriggers(allTriggersAsSingleString()));
        return events;
    }

    private static boolean bloodmoon() {
        if (ModList.get().isLoaded("enhancedcelestials") && SoundHandler.TriggerSongMap.get("bloodmoon")!=null) {
            LunarContext lunarContext = ((EnhancedCelestialsWorldData) world).getLunarContext();
            if (lunarContext != null && lunarContext.getCurrentEvent() instanceof BloodMoon) {
                dynamicSongs.put("bloodmoon", new ArrayList<>(SoundHandler.TriggerSongMap.get("bloodmoon").keySet()));
                dynamicPriorities.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[0]));
                dynamicFade.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[1]));
                dynamicDelay.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[4]));
                triggerPersistence.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("bloodmoon")[33])) timeSwitch.add("bloodmoon");
                return true;
            }
            else if (triggerPersistence.get("bloodmoon")!=null && triggerPersistence.get("bloodmoon") > 0) {
                dynamicSongs.put("bloodmoon", new ArrayList<>(SoundHandler.TriggerSongMap.get("bloodmoon").keySet()));
                dynamicPriorities.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[0]));
                dynamicFade.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[1]));
                dynamicDelay.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("bloodmoon")[33])) timeSwitch.add("bloodmoon");
                return true;
            }
        }
        return false;
    }

    private static boolean harvestmoon() {
        if (ModList.get().isLoaded("enhancedcelestials") && SoundHandler.TriggerSongMap.get("harvestmoon")!=null) {
            LunarContext lunarContext = ((EnhancedCelestialsWorldData) world).getLunarContext();
            if (lunarContext != null && lunarContext.getCurrentEvent() instanceof HarvestMoon) {
                dynamicSongs.put("harvestmoon", new ArrayList<>(SoundHandler.TriggerSongMap.get("harvestmoon").keySet()));
                dynamicPriorities.put("harvestmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("harvestmoon")[0]));
                dynamicFade.put("harvestmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("harvestmoon")[1]));
                dynamicDelay.put("harvestmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("harvestmoon")[4]));
                triggerPersistence.put("harvestmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("harvestmoon")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("harvestmoon")[33])) timeSwitch.add("harvestmoon");
                return true;
            }
            else if (triggerPersistence.get("harvestmoon")!=null && triggerPersistence.get("harvestmoon") > 0) {
                dynamicSongs.put("harvestmoon", new ArrayList<>(SoundHandler.TriggerSongMap.get("harvestmoon").keySet()));
                dynamicPriorities.put("harvestmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("harvestmoon")[0]));
                dynamicFade.put("harvestmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("harvestmoon")[1]));
                dynamicDelay.put("harvestmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("harvestmoon")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("harvestmoon")[33])) timeSwitch.add("harvestmoon");
                return true;
            }
        }
        return false;
    }

    private static boolean bluemoon() {
        if (ModList.get().isLoaded("enhancedcelestials") && SoundHandler.TriggerSongMap.get("bluemoon")!=null) {
            LunarContext lunarContext = ((EnhancedCelestialsWorldData) world).getLunarContext();
            if (lunarContext != null && lunarContext.getCurrentEvent() instanceof BlueMoon) {
                dynamicSongs.put("bluemoon", new ArrayList<>(SoundHandler.TriggerSongMap.get("bluemoon").keySet()));
                dynamicPriorities.put("bluemoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bluemoon")[0]));
                dynamicFade.put("bluemoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bluemoon")[1]));
                dynamicDelay.put("bluemoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bluemoon")[4]));
                triggerPersistence.put("bluemoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bluemoon")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("bluemoon")[33])) timeSwitch.add("bluemoon");
                return true;
            }
            else if (triggerPersistence.get("bluemoon")!=null && triggerPersistence.get("bluemoon") > 0) {
                dynamicSongs.put("bluemoon", new ArrayList<>(SoundHandler.TriggerSongMap.get("bluemoon").keySet()));
                dynamicPriorities.put("bluemoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bluemoon")[0]));
                dynamicFade.put("bluemoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bluemoon")[1]));
                dynamicDelay.put("bluemoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bluemoon")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("bluemoon")[33])) timeSwitch.add("bluemoon");
                return true;
            }
        }
        return false;
    }

    private static List<String> moon() {
        List<String> events = new ArrayList<>();
        if (ModList.get().isLoaded("enhancedcelestials") && SoundHandler.TriggerSongMap.get("moon")!=null) {
            LunarContext lunarContext = ((EnhancedCelestialsWorldData) world).getLunarContext();
            if(SoundHandler.TriggerSongMap.get("moon")!=null && lunarContext != null && lunarContext.getCurrentEvent() instanceof Moon) {
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("moon").entrySet()) {
                    String moonSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(moonSong.replaceAll("@","")).get("moon")[10];
                    if ((lunarContext.getCurrentEvent().getKey().contains(SoundHandler.TriggerInfoMap.get("moon-"+identifier)[9]))) {
                        if (!events.contains("moon-" + identifier)) {
                            events.add("moon-" + identifier);
                            dynamicSongs.put("moon-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("moon"), identifier));
                            dynamicPriorities.put("moon-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("moon-" + identifier)[0]));
                            dynamicFade.put("moon-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("moon-" + identifier)[1]));
                            dynamicDelay.put("moon-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("moon-" + identifier)[4]));
                            triggerPersistence.put("moon-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("moon-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("moon-" + identifier)[33])) timeSwitch.add("moon-" + identifier);
                        }
                    } else if (triggerPersistence.get("moon-"+identifier) != null && triggerPersistence.get("moon-"+identifier) > 0) {
                        if (!events.contains("moon-" + identifier)) {
                            events.add("moon-" + identifier);
                            dynamicSongs.put("moon-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("moon"), identifier));
                            dynamicPriorities.put("moon-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("moon-" + identifier)[0]));
                            dynamicFade.put("moon-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("moon-" + identifier)[1]));
                            dynamicDelay.put("moon-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("moon-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("moon-" + identifier)[33])) timeSwitch.add("moon-" + identifier);
                        }
                    }
                }
            }
        }
        return events;
    }

    private static List<String> seasons() {
        List<String> tempList = new ArrayList<>();
        if (ModList.get().isLoaded("sereneseasons") && SoundHandler.TriggerSongMap.get("season")!=null) {
            if(SoundHandler.TriggerSongMap.get("season")!=null) {
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("season").entrySet()) {
                    String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(structureSong.replaceAll("@","")).get("season")[10];
                    int seasonID = Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[19]);
                    ISeasonState curSeason = SeasonHelper.getSeasonState(world);
                    if (seasonID == 0 && curSeason.getSeason() == Season.SPRING) {
                        if (!tempList.contains("season:" + seasonID)) {
                            tempList.add("season:" + seasonID);
                            dynamicSongs.put("season-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("season"), identifier));
                            dynamicPriorities.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[0]));
                            dynamicFade.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[1]));
                            dynamicDelay.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[4]));
                            triggerPersistence.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("season-" + identifier)[33])) timeSwitch.add("season-" + identifier);
                        }
                    } else if (seasonID == 1 && curSeason.getSeason() == Season.SUMMER) {
                        if (!tempList.contains("season:" + seasonID)) {
                            tempList.add("season:" + seasonID);
                            dynamicSongs.put("season-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("season"), identifier));
                            dynamicPriorities.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[0]));
                            dynamicFade.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[1]));
                            dynamicDelay.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[4]));
                            triggerPersistence.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("season-" + identifier)[33])) timeSwitch.add("season-" + identifier);
                        }
                    } else if (seasonID == 2 && curSeason.getSeason() == Season.AUTUMN) {
                        if (!tempList.contains("season:" + seasonID)) {
                            tempList.add("season:" + seasonID);
                            dynamicSongs.put("season-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("season"), identifier));
                            dynamicPriorities.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[0]));
                            dynamicFade.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[1]));
                            dynamicDelay.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[4]));
                            triggerPersistence.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("season-" + identifier)[33])) timeSwitch.add("season-" + identifier);
                        }
                    } else if (seasonID == 3 && curSeason.getSeason() == Season.WINTER) {
                        if (!tempList.contains("season:" + seasonID)) {
                            tempList.add("season:" + seasonID);
                            dynamicSongs.put("season-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("season"), identifier));
                            dynamicPriorities.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[0]));
                            dynamicFade.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[1]));
                            dynamicDelay.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[4]));
                            triggerPersistence.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("season-" + identifier)[33])) timeSwitch.add("season-" + identifier);
                        }
                    } else if (triggerPersistence.get("season-" + identifier) != null && triggerPersistence.get("season-" + identifier) > 0) {
                        if (!tempList.contains("season:" + seasonID)) {
                            tempList.add("season:" + seasonID);
                            dynamicSongs.put("season-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("season"), identifier));
                            dynamicPriorities.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[0]));
                            dynamicFade.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[1]));
                            dynamicDelay.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("season-" + identifier)[33])) timeSwitch.add("season-" + identifier);
                        }
                    }
                }
            }
            return tempList;
        }
        return tempList;
    }

    public static BlockPos roundedPos(Player p) {
        return new BlockPos((Math.round(p.blockPosition().getX() * 2) / 2.0), (Math.round(p.blockPosition().getY() * 2) / 2.0), (Math.round(p.blockPosition().getZ() * 2) / 2.0));
    }

    public static double averageLight(BlockPos p, boolean b) {
        return b ? world.getRawBrightness(p, 0) : world.getBrightness(LightLayer.BLOCK, p);
    }

    public static boolean checkBiome(Biome b, String name, String category, String rainType, float temperature, boolean cold, float rainfall, boolean togglerainfall) {
        if(Objects.requireNonNull(b.getRegistryName()).toString().contains(name) || name.matches("minecraft")) {
            if(b.getBiomeCategory().getName().contains(category) || category.matches("nope")) {
                if(b.getPrecipitation().getName().contains(rainType) || rainType.matches("nope")) {
                    boolean pass = false;
                    if(rainfall==-111f) pass = true;
                    else if(b.getDownfall()>rainfall && togglerainfall) pass = true;
                    else if(b.getDownfall()<rainfall && !togglerainfall) pass = true;
                    if(pass) {
                        float bt = b.getBaseTemperature();
                        if (temperature == -111) return true;
                        else if (bt >= temperature && !cold) return true;
                        else return bt <= temperature && cold;
                    }
                }
            }
        }
        return false;
    }

    public static String[] stringBreaker(String s, String regex) {
        return s.split(regex);
    }

    public static List<String> buildSongsFromIdentifier(Map<String, String> songs,String identifier) {
        List<String> ret = new ArrayList<>();
        for (Map.Entry<String, String> stringListEntry : songs.entrySet()) {
            String songEntry = ((Map.Entry) stringListEntry).getKey().toString();
            if(songs.get(songEntry).matches(identifier)) {
                ret.add(songEntry);
            }
        }
        return ret;
    }

    public static String allTriggersAsSingleString() {
        StringBuilder ret = new StringBuilder();
        for(String trigger : playableList) {
            ret.append(trigger).append(",");
        }
        if(ret.length()!=0) return ret.substring(0, ret.length()-1);
        return "";
    }

    public static void emptyMapsAndLists() {
        triggerPersistence = new HashMap<>();
        victory = new HashMap<>();
        dynamicSongs = new HashMap<>();
        dynamicPriorities = new HashMap<>();
        dynamicFade = new HashMap<>();
        dynamicDelay = new HashMap<>();
        playableList = new ArrayList<>();
        titleCardEvents = new ArrayList<>();
        effectList = new ArrayList<>();
    }
}