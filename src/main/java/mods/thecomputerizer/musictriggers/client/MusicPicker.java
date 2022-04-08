package mods.thecomputerizer.musictriggers.client;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config.configRegistry;
import mods.thecomputerizer.musictriggers.config.configToml;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.packets.*;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.*;

@SuppressWarnings({"rawtypes"})
public class MusicPicker {
    public static MinecraftClient mc;
    public static PlayerEntity player;
    public static World world;

    public static HashMap<String, Integer> triggerPersistence = new HashMap<>();
    public static HashMap<Integer, Boolean> victory = new HashMap<>();
    public static boolean fishBool = false;
    public static int fishingStart = 0;
    public static boolean elytraBool = false;
    public static int elytraStart = 0;
    public static boolean setPVP = false;
    public static int pvpVictoryID = 0;
    public static boolean waterBool = false;
    public static int waterStart = 0;

    public static HashMap<String, List<String>> dynamicSongs = new HashMap<>();
    public static HashMap<String, Integer> dynamicPriorities = new HashMap<>();
    public static HashMap<String, Integer> dynamicFadeIn = new HashMap<>();
    public static HashMap<String, Integer> dynamicFadeOut = new HashMap<>();
    public static HashMap<String, Integer> dynamicDelay = new HashMap<>();

    public static List<String> playableList = new ArrayList<>();
    public static List<String> titleCardEvents = new ArrayList<>();
    public static List<String> timeSwitch = new ArrayList<>();

    public static List<String> effectList = new ArrayList<>();

    public static int curFadeIn = 0;
    public static int curFadeOut = 0;
    public static int curDelay = 0;
    public static boolean shouldChange = false;

    public static int universalFadeIn = 0;
    public static int universalFadeOut = 0;
    public static int universalDelay = 0;

    public static float musicVolSave;
    public static float masterVolSave;

    public static List<String> playThese() {
        if (!MusicPlayer.fadingOut) titleCardEvents = new ArrayList<>();
        mc = MinecraftClient.getInstance();
        player = mc.player;
        if (player != null) world = player.getEntityWorld();
        if(SoundHandler.TriggerIdentifierMap.isEmpty()) return null;
        if(player == null) {
            if (SoundHandler.TriggerIdentifierMap.get("menu") != null) return SoundHandler.TriggerIdentifierMap.get("menu").get("_");
            else return null;
        }
        String menuPacket = allMenuSongsAsSingleString();
        if(menuPacket!=null) PacketHandler.sendToServer(MenuSongs.id, MenuSongs.encode(menuPacket));
        List<String> res = comboChecker(priorityHandler(playableEvents()));
        for(String event : timeSwitch) {
            if(!titleCardEvents.contains(event) && triggerPersistence.get(event) > 0) triggerPersistence.put(event, 0);
        }
        timeSwitch = new ArrayList<>();
        if (res != null && !res.isEmpty()) {
            dynamicSongs = new HashMap<>();
            dynamicPriorities = new HashMap<>();
            dynamicFadeIn = new HashMap<>();
            dynamicFadeOut = new HashMap<>();
            return res;
        }
        dynamicSongs = new HashMap<>();
        dynamicPriorities = new HashMap<>();
        dynamicFadeIn = new HashMap<>();
        dynamicFadeOut = new HashMap<>();
        if(SoundHandler.TriggerInfoMap.get("generic") != null) {
            playableList.add("generic");
            titleCardEvents.add("generic");
            curDelay = Integer.parseInt(SoundHandler.TriggerInfoMap.get("generic")[4]);
            if(curDelay==0) curDelay = universalDelay;
            curFadeIn = Integer.parseInt(SoundHandler.TriggerInfoMap.get("generic")[1]);
            if(curFadeIn==0) curFadeIn = universalFadeIn;
            curFadeOut = Integer.parseInt(SoundHandler.TriggerInfoMap.get("generic")[35]);
            if(curFadeOut==0) curFadeOut = universalFadeOut;
            return SoundHandler.TriggerIdentifierMap.get("generic").get("_");
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static List<String> comboChecker(String st) {
        if (st == null) {
            return null;
        }
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
                        if (playableList.containsAll(SoundHandler.songCombos.get(s.replaceAll("@", ""))) && SoundHandler.TriggerInfoMap.keySet().containsAll(SoundHandler.instantiatedCombos.get(SoundHandler.songCombos.get(s.replaceAll("@", ""))))) {
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
        if (dynamicFadeIn != null && !dynamicFadeIn.isEmpty()) {
            if (dynamicFadeIn.get(trueHighest) != null) curFadeIn = dynamicFadeIn.get(trueHighest);
            else curFadeIn = 0;
            if(curFadeIn==0) curFadeIn = universalFadeIn;
        }
        if (dynamicFadeOut != null && !dynamicFadeOut.isEmpty()) {
            if (dynamicFadeOut.get(trueHighest) != null) curFadeOut = dynamicFadeOut.get(trueHighest);
            else curFadeOut = 0;
            if(curFadeOut==0) curFadeOut = universalFadeOut;
        }
        if (dynamicDelay != null && !dynamicDelay.isEmpty()) {
            if (dynamicDelay.get(trueHighest) != null) curDelay = dynamicDelay.get(trueHighest);
            else curDelay = 0;
            if(curDelay==0) curDelay = universalDelay;
        }
        return trueHighest;
    }

    @SuppressWarnings({"ConstantConditions"})
    public static List<String> playableEvents() {
        String crashHelper = "";
        List<String> events = new ArrayList<>();
        try {
            double time = (double) world.getTimeOfDay() / 24000.0;
            if (time > 1) {
                time = time - (long) time;
            }
            if (SoundHandler.TriggerIdentifierMap.get("time") != null) {
                crashHelper = "time";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("time").keySet()) {
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
                                dynamicSongs.put("time-" + identifier, SoundHandler.TriggerIdentifierMap.get("time").get(identifier));
                                dynamicPriorities.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[0]));
                                dynamicFadeIn.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[1]));
                                dynamicFadeOut.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[35]));
                                dynamicDelay.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[4]));
                                triggerPersistence.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[3]));
                                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("time-" + identifier)[33])) timeSwitch.add("time-" + identifier);
                            }
                        } else if (Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[21]) == world.getMoonPhase() + 1) {
                            if (!events.contains("time-" + identifier)) {
                                events.add("time-" + identifier);
                                dynamicSongs.put("time-" + identifier, SoundHandler.TriggerIdentifierMap.get("time").get(identifier));
                                dynamicPriorities.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[0]));
                                dynamicFadeIn.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[1]));
                                dynamicFadeOut.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[35]));
                                dynamicDelay.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[4]));
                                triggerPersistence.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[3]));
                                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("time-" + identifier)[33])) timeSwitch.add("time-" + identifier);
                            }
                        }
                    } else if (triggerPersistence.get("time-" + identifier) != null && triggerPersistence.get("time-" + identifier) > 0) {
                        if (!events.contains("time-" + identifier)) {
                            events.add("time-" + identifier);
                            dynamicSongs.put("time-" + identifier, SoundHandler.TriggerIdentifierMap.get("time").get(identifier));
                            dynamicPriorities.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[0]));
                            dynamicFadeIn.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[1]));
                            dynamicFadeOut.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[35]));
                            dynamicDelay.put("time-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("time-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("time-" + identifier)[33])) timeSwitch.add("time-" + identifier);
                        }
                    }
                }
            }
            if (SoundHandler.TriggerIdentifierMap.get("light") != null) {
                crashHelper = "light";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("light").keySet()) {
                    if (averageLight(roundedPos(player), Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("light-" + identifier)[20])) <= Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[2])) {
                        if (!events.contains("light-" + identifier)) {
                            events.add("light-" + identifier);
                            dynamicSongs.put("light-" + identifier, SoundHandler.TriggerIdentifierMap.get("light").get(identifier));
                            dynamicPriorities.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[0]));
                            dynamicFadeIn.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[1]));
                            dynamicFadeOut.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[35]));
                            dynamicDelay.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[4]));
                            triggerPersistence.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("light-" + identifier)[33])) timeSwitch.add("light-" + identifier);
                        }
                    } else if (triggerPersistence.get("light-" + identifier) != null && triggerPersistence.get("light-" + identifier) > 0) {
                        if (!events.contains("light-" + identifier)) {
                            events.add("light-" + identifier);
                            dynamicSongs.put("light-" + identifier, SoundHandler.TriggerIdentifierMap.get("light").get(identifier));
                            dynamicPriorities.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[0]));
                            dynamicFadeIn.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[1]));
                            dynamicFadeOut.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[35]));
                            dynamicDelay.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("light-" + identifier)[33])) timeSwitch.add("light-" + identifier);
                        }
                    }
                }
            }
            if (SoundHandler.TriggerIdentifierMap.get("height") != null) {
                crashHelper = "height";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("height").keySet()) {
                    boolean pass;
                    if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("height-" + identifier)[28])) pass = player.getY() < Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[2]) && !world.isSkyVisible(roundedPos(player));
                    else pass = player.getY() > Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[2]);
                    if (pass) {
                        if (!events.contains("height-" + identifier)) {
                            events.add("height-" + identifier);
                            dynamicSongs.put("height-" + identifier, SoundHandler.TriggerIdentifierMap.get("height").get(identifier));
                            dynamicPriorities.put("height-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[0]));
                            dynamicFadeIn.put("height-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[1]));
                            dynamicFadeOut.put("height-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[35]));
                            dynamicDelay.put("height-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[4]));
                            triggerPersistence.put("height-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("height-" + identifier)[33])) timeSwitch.add("height-" + identifier);
                        }
                    } else if (triggerPersistence.get("height-" + identifier) != null && triggerPersistence.get("height-" + identifier) > 0) {
                        if (!events.contains("height-" + identifier)) {
                            events.add("height-" + identifier);
                            dynamicSongs.put("height-" + identifier, SoundHandler.TriggerIdentifierMap.get("height").get(identifier));
                            dynamicPriorities.put("height-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[0]));
                            dynamicFadeIn.put("height-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[1]));
                            dynamicFadeOut.put("height-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[35]));
                            dynamicDelay.put("height-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("height-" + identifier)[33])) timeSwitch.add("height-" + identifier);
                        }
                    }
                }
            }
            if (SoundHandler.TriggerInfoMap.get("elytra") != null) {
                if (player.isFallFlying()) {
                    elytraBool = true;
                } else {
                    elytraStart = 0;
                }
            }
            if (SoundHandler.TriggerInfoMap.get("elytra") != null && elytraStart > Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[8])) {
                crashHelper = "elytra";
                events.add("elytra");
                dynamicSongs.put("elytra", SoundHandler.TriggerIdentifierMap.get("elytra").get("_"));
                dynamicPriorities.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[0]));
                dynamicFadeIn.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[1]));
                dynamicFadeOut.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[35]));
                dynamicDelay.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[4]));
                triggerPersistence.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("elytra")[33])) timeSwitch.add("elytra");
            } else if (triggerPersistence.get("elytra") != null && triggerPersistence.get("elytra") > 0) {
                crashHelper = "elytra";
                events.add("elytra");
                dynamicSongs.put("elytra", SoundHandler.TriggerIdentifierMap.get("elytra").get("_"));
                dynamicPriorities.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[0]));
                dynamicFadeIn.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[1]));
                dynamicFadeOut.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[35]));
                dynamicDelay.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("elytra")[33])) timeSwitch.add("elytra");
            }
            if (SoundHandler.TriggerInfoMap.get("fishing") != null) {
                if (player.fishHook != null && player.fishHook.isInOpenWater()) fishBool = true;
                else fishingStart = 0;
            }
            if (SoundHandler.TriggerInfoMap.get("fishing") != null && fishingStart > Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[8])) {
                crashHelper = "fishing";
                events.add("fishing");
                dynamicSongs.put("fishing", SoundHandler.TriggerIdentifierMap.get("fishing").get("_"));
                dynamicPriorities.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[0]));
                dynamicFadeIn.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[1]));
                dynamicFadeOut.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[35]));
                dynamicDelay.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[4]));
                triggerPersistence.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("fishing")[33])) timeSwitch.add("fishing");
            } else if (triggerPersistence.get("fishing") != null && triggerPersistence.get("fishing") > 0) {
                crashHelper = "fishing";
                events.add("fishing");
                dynamicSongs.put("fishing", SoundHandler.TriggerIdentifierMap.get("fishing").get("_"));
                dynamicPriorities.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[0]));
                dynamicFadeIn.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[1]));
                dynamicFadeOut.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[35]));
                dynamicDelay.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("fishing")[33])) timeSwitch.add("fishing");
            }
            if (world.isRaining() && SoundHandler.TriggerIdentifierMap.get("raining") != null) {
                crashHelper = "raining";
                events.add("raining");
                dynamicSongs.put("raining", SoundHandler.TriggerIdentifierMap.get("raining").get("_"));
                dynamicPriorities.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[0]));
                dynamicFadeIn.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[1]));
                dynamicFadeOut.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[35]));
                dynamicDelay.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[4]));
                triggerPersistence.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("raining")[33])) timeSwitch.add("raining");
            } else if (triggerPersistence.get("raining") != null && triggerPersistence.get("raining") > 0) {
                crashHelper = "raining";
                events.add("raining");
                dynamicSongs.put("raining", SoundHandler.TriggerIdentifierMap.get("raining").get("_"));
                dynamicPriorities.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[0]));
                dynamicFadeIn.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[1]));
                dynamicFadeOut.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[35]));
                dynamicDelay.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("raining")[33])) timeSwitch.add("raining");
            }
            if (SoundHandler.TriggerIdentifierMap.get("snowing") != null && world.isRaining() && !configRegistry.clientSideOnly) {
                PacketHandler.sendToServer(InfoForSnow.id, InfoForSnow.encode("snowing", roundedPos(player), player.getUuid()));
                fromServer.inSnow.putIfAbsent("snowing", false);
                if (fromServer.inSnow.get("snowing")) {
                    crashHelper = "snowing";
                    events.add("snowing");
                    dynamicSongs.put("snowing", SoundHandler.TriggerIdentifierMap.get("snowing").get("_"));
                    dynamicPriorities.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[0]));
                    dynamicFadeIn.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[1]));
                    dynamicFadeOut.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[35]));
                    dynamicDelay.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[4]));
                    triggerPersistence.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[3]));
                    if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("snowing")[33])) timeSwitch.add("snowing");
                } else if (triggerPersistence.get("snowing") != null && triggerPersistence.get("snowing") > 0) {
                    crashHelper = "snowing";
                    events.add("snowing");
                    dynamicSongs.put("snowing", SoundHandler.TriggerIdentifierMap.get("snowing").get("_"));
                    dynamicPriorities.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[0]));
                    dynamicFadeIn.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[1]));
                    dynamicFadeOut.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[35]));
                    dynamicDelay.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[4]));
                    if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("snowing")[33])) timeSwitch.add("snowing");
                }
            }
            if (world.isThundering() && SoundHandler.TriggerIdentifierMap.get("storming") != null) {
                crashHelper = "storming";
                events.add("storming");
                dynamicSongs.put("storming", SoundHandler.TriggerIdentifierMap.get("storming").get("_"));
                dynamicPriorities.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[0]));
                dynamicFadeIn.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[1]));
                dynamicFadeOut.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[35]));
                dynamicDelay.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[4]));
                triggerPersistence.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("storming")[33])) timeSwitch.add("storming");
            } else if (triggerPersistence.get("storming") != null && triggerPersistence.get("storming") > 0) {
                crashHelper = "storming";
                events.add("storming");
                dynamicSongs.put("storming", SoundHandler.TriggerIdentifierMap.get("storming").get("_"));
                dynamicPriorities.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[0]));
                dynamicFadeIn.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[1]));
                dynamicFadeOut.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[35]));
                dynamicDelay.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("storming")[33])) timeSwitch.add("storming");
            }
            if (SoundHandler.TriggerInfoMap.get("lowhp") != null && player.getHealth() < player.getMaxHealth() * (Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[2]) / 100F)) {
                crashHelper = "lowhp";
                events.add("lowhp");
                dynamicSongs.put("lowhp", SoundHandler.TriggerIdentifierMap.get("lowhp").get("_"));
                dynamicPriorities.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[0]));
                dynamicFadeIn.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[1]));
                dynamicFadeOut.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[35]));
                dynamicDelay.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[4]));
                triggerPersistence.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("lowhp")[33])) timeSwitch.add("lowhp");
            } else if (triggerPersistence.get("lowhp") != null && triggerPersistence.get("lowhp") > 0) {
                crashHelper = "lowhp";
                events.add("lowhp");
                dynamicSongs.put("lowhp", SoundHandler.TriggerIdentifierMap.get("lowhp").get("_"));
                dynamicPriorities.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[0]));
                dynamicFadeIn.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[1]));
                dynamicFadeOut.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[35]));
                dynamicDelay.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("lowhp")[33])) timeSwitch.add("lowhp");
            }
            if (player.isDead() && SoundHandler.TriggerIdentifierMap.get("dead") != null) {
                crashHelper = "dead";
                events.add("dead");
                dynamicSongs.put("dead", SoundHandler.TriggerIdentifierMap.get("dead").get("_"));
                dynamicPriorities.put("dead", Integer.parseInt(SoundHandler.TriggerInfoMap.get("dead")[0]));
                dynamicFadeIn.put("dead", Integer.parseInt(SoundHandler.TriggerInfoMap.get("dead")[1]));
                dynamicFadeOut.put("dead", Integer.parseInt(SoundHandler.TriggerInfoMap.get("dead")[35]));
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
                dynamicSongs.put("dead", SoundHandler.TriggerIdentifierMap.get("dead").get("_"));
                dynamicPriorities.put("dead", Integer.parseInt(SoundHandler.TriggerInfoMap.get("dead")[0]));
                dynamicFadeIn.put("dead", Integer.parseInt(SoundHandler.TriggerInfoMap.get("dead")[1]));
                dynamicFadeOut.put("dead", Integer.parseInt(SoundHandler.TriggerInfoMap.get("dead")[35]));
                dynamicDelay.put("dead", Integer.parseInt(SoundHandler.TriggerInfoMap.get("dead")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("dead")[33])) timeSwitch.add("dead");
            }
            if (player.isSpectator() && SoundHandler.TriggerIdentifierMap.get("spectator") != null) {
                crashHelper = "spectator";
                events.add("spectator");
                dynamicSongs.put("spectator", SoundHandler.TriggerIdentifierMap.get("spectator").get("_"));
                dynamicPriorities.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[0]));
                dynamicFadeIn.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[1]));
                dynamicFadeOut.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[35]));
                dynamicDelay.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[4]));
                triggerPersistence.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("spectator")[33])) timeSwitch.add("spectator");
            } else if (triggerPersistence.get("spectator") != null && triggerPersistence.get("spectator") > 0) {
                crashHelper = "spectator";
                events.add("spectator");
                dynamicSongs.put("spectator", SoundHandler.TriggerIdentifierMap.get("spectator").get("_"));
                dynamicPriorities.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[0]));
                dynamicFadeIn.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[1]));
                dynamicFadeOut.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[35]));
                dynamicDelay.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("spectator")[33])) timeSwitch.add("spectator");
            }
            if (player.isCreative() && SoundHandler.TriggerIdentifierMap.get("creative") != null) {
                crashHelper = "creative";
                events.add("creative");
                dynamicSongs.put("creative", SoundHandler.TriggerIdentifierMap.get("creative").get("_"));
                dynamicPriorities.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[0]));
                dynamicFadeIn.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[1]));
                dynamicFadeOut.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[35]));
                dynamicDelay.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[4]));
                triggerPersistence.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("creative")[33])) timeSwitch.add("creative");
            } else if (triggerPersistence.get("creative") != null && triggerPersistence.get("creative") > 0) {
                crashHelper = "creative";
                events.add("creative");
                dynamicSongs.put("creative", SoundHandler.TriggerIdentifierMap.get("creative").get("_"));
                dynamicPriorities.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[0]));
                dynamicFadeIn.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[1]));
                dynamicFadeOut.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[35]));
                dynamicDelay.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("creative")[33])) timeSwitch.add("creative");
            }
            if (SoundHandler.TriggerIdentifierMap.get("riding") != null && player.getPrimaryPassenger().hasVehicle()) {
                crashHelper = "riding";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("riding").keySet()) {
                    String ridingName = SoundHandler.TriggerInfoMap.get("riding-" + identifier)[9];
                    if (checkResourceList(Objects.requireNonNull(player.getVehicle()).getName().getString(),ridingName,true) || checkResourceList(Objects.requireNonNull(EntityType.getId(player.getVehicle().getType())).toString(),ridingName,true) || ridingName.matches("minecraft")) {
                        if (!events.contains("riding-" + identifier)) {
                            events.add("riding-" + identifier);
                            dynamicSongs.put("riding-" + identifier, SoundHandler.TriggerIdentifierMap.get("riding").get(identifier));
                            dynamicPriorities.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[0]));
                            dynamicFadeIn.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[1]));
                            dynamicFadeOut.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[35]));
                            dynamicDelay.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[4]));
                            triggerPersistence.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[33])) timeSwitch.add("riding-" + identifier);
                        }
                    } else if (triggerPersistence.get("riding-" + identifier) != null && triggerPersistence.get("riding-" + identifier) > 0) {
                        if (!events.contains("riding-" + identifier)) {
                            events.add("riding-" + identifier);
                            dynamicSongs.put("riding-" + identifier, SoundHandler.TriggerIdentifierMap.get("riding").get(identifier));
                            dynamicPriorities.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[0]));
                            dynamicFadeIn.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[1]));
                            dynamicFadeOut.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[35]));
                            dynamicDelay.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[33])) timeSwitch.add("riding-" + identifier);
                        }
                    }
                }
            }
            if(SoundHandler.TriggerIdentifierMap.get("underwater") != null && ((world.getBlockState(roundedPos(player)).getMaterial() == Material.WATER || world.getBlockState(roundedPos(player)).getMaterial() == Material.UNDERWATER_PLANT || world.getBlockState(roundedPos(player)).getMaterial() == Material.REPLACEABLE_UNDERWATER_PLANT) && (world.getBlockState(roundedPos(player).up()).getMaterial() == Material.WATER || world.getBlockState(roundedPos(player).up()).getMaterial() == Material.UNDERWATER_PLANT || world.getBlockState(roundedPos(player).up()).getMaterial() == Material.REPLACEABLE_PLANT))) {
                waterBool = true;
            } else waterStart = 0;
            if (SoundHandler.TriggerIdentifierMap.get("underwater") != null && waterStart > Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[8])) {
                crashHelper = "underwater";
                events.add("underwater");
                dynamicSongs.put("underwater", SoundHandler.TriggerIdentifierMap.get("underwater").get("_"));
                dynamicPriorities.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[0]));
                dynamicFadeIn.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[1]));
                dynamicFadeOut.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[35]));
                dynamicDelay.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[4]));
                triggerPersistence.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("underwater")[33])) timeSwitch.add("underwater");
            } else if (triggerPersistence.get("underwater") != null && triggerPersistence.get("underwater") > 0) {
                crashHelper = "underwater";
                events.add("underwater");
                dynamicSongs.put("underwater", SoundHandler.TriggerIdentifierMap.get("underwater").get("_"));
                dynamicPriorities.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[0]));
                dynamicFadeIn.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[1]));
                dynamicFadeOut.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[35]));
                dynamicDelay.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("underwater")[33])) timeSwitch.add("underwater");
            }
            for (LivingEntity ent : world.getEntitiesByClass(LivingEntity.class, new Box(player.getX() - 16, player.getY() - 8, player.getZ() - 16, player.getX() + 16, player.getY() + 8, player.getZ() + 16), EntityPredicates.VALID_LIVING_ENTITY)) {
                if ((ent instanceof TameableEntity && ent.writeNbt(new NbtCompound()) != null && ent.writeNbt(new NbtCompound()).getString("Owner").matches(player.getUuidAsString())) && SoundHandler.TriggerIdentifierMap.get("pet") != null) {
                    crashHelper = "pet";
                    events.add("pet");
                    dynamicSongs.put("pet", SoundHandler.TriggerIdentifierMap.get("pet").get("_"));
                    dynamicPriorities.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[0]));
                    dynamicFadeIn.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[1]));
                dynamicFadeOut.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[35]));
                    dynamicDelay.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[4]));
                    triggerPersistence.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[3]));
                    if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("pet")[33])) timeSwitch.add("pet");
                    break;
                }
            }
            if (triggerPersistence.get("pet") != null && triggerPersistence.get("pet") > 0) {
                crashHelper = "pet";
                events.add("pet");
                dynamicSongs.put("pet", SoundHandler.TriggerIdentifierMap.get("pet").get("_"));
                dynamicPriorities.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[0]));
                dynamicFadeIn.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[1]));
                dynamicFadeOut.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[35]));
                dynamicDelay.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("pet")[33])) timeSwitch.add("pet");
            }
            if (triggerPersistence.get("drowning") != null && player.getAir() < Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[2])) {
                crashHelper = "drowning";
                events.add("drowning");
                dynamicSongs.put("drowning", SoundHandler.TriggerIdentifierMap.get("drowning").get("_"));
                dynamicPriorities.put("drowning", Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[0]));
                dynamicFadeIn.put("drowning", Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[1]));
                dynamicFadeOut.put("drowning", Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[35]));
                dynamicDelay.put("drowning", Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[4]));
                triggerPersistence.put("drowning", Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("drowning")[33])) timeSwitch.add("drowning");
            } else if (triggerPersistence.get("drowning") != null && triggerPersistence.get("drowning") > 0) {
                crashHelper = "drowning";
                events.add("drowning");
                dynamicSongs.put("drowning", SoundHandler.TriggerIdentifierMap.get("drowning").get("_"));
                dynamicPriorities.put("drowning", Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[0]));
                dynamicFadeIn.put("drowning", Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[1]));
                dynamicFadeOut.put("drowning", Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[35]));
                dynamicDelay.put("drowning", Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("drowning")[33])) timeSwitch.add("drowning");
            }
            if (triggerPersistence.get("pvp") != null && setPVP) {
                crashHelper = "pvp";
                events.add("pvp");
                dynamicSongs.put("pvp", SoundHandler.TriggerIdentifierMap.get("pvp").get("_"));
                dynamicPriorities.put("pvp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pvp")[0]));
                dynamicFadeIn.put("pvp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pvp")[1]));
                dynamicFadeOut.put("pvp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pvp")[35]));
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
                dynamicSongs.put("pvp", SoundHandler.TriggerIdentifierMap.get("pvp").get("_"));
                dynamicPriorities.put("pvp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pvp")[0]));
                dynamicFadeIn.put("pvp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pvp")[1]));
                dynamicFadeOut.put("pvp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pvp")[35]));
                dynamicDelay.put("pvp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pvp")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("pvp")[33])) timeSwitch.add("pvp");
            }
            if (triggerPersistence.get("pvp") != null && eventsClient.PVPTracker != null && triggerPersistence.get("victory_timeout") <= 0) {
                eventsClient.PVPTracker = null;
            }
            if (eventsClient.PVPTracker != null && eventsClient.PVPTracker.isDead()) {
                victory.put(pvpVictoryID, true);
                eventsClient.PVPTracker = null;
            }
            if (triggerPersistence.get("home") != null && !configRegistry.clientSideOnly) {
                crashHelper = "home";
                PacketHandler.sendToServer(InfoForHome.id, InfoForHome.encode("home", roundedPos(player), player.getUuid(), SoundHandler.TriggerInfoMap.get("home")[11]));
                fromServer.inHomeRange.putIfAbsent("home", false);
                if (fromServer.inHomeRange.get("home")) {
                    events.add("home");
                    dynamicSongs.put("home", SoundHandler.TriggerIdentifierMap.get("home").get("_"));
                    dynamicPriorities.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[0]));
                    dynamicFadeIn.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[1]));
                    dynamicFadeOut.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[35]));
                    dynamicDelay.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[4]));
                    triggerPersistence.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[3]));
                    if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("home")[33])) timeSwitch.add("home");
                } else if (triggerPersistence.get("home") > 0) {
                    events.add("home");
                    dynamicSongs.put("home", SoundHandler.TriggerIdentifierMap.get("home").get("_"));
                    dynamicPriorities.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[0]));
                    dynamicFadeIn.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[1]));
                    dynamicFadeOut.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[35]));
                    dynamicDelay.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[4]));
                    if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("home")[33])) timeSwitch.add("home");
                }
            }
            if (SoundHandler.TriggerIdentifierMap.get("dimension") != null) {
                crashHelper = "dimension";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("dimension").keySet()) {
                    if (checkResourceList(player.world.getRegistryKey().getValue().toString(), SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[9], false)) {
                        if (!events.contains("dimension-" + identifier)) {
                            events.add("dimension-" + identifier);
                            dynamicSongs.put("dimension-" + identifier, SoundHandler.TriggerIdentifierMap.get("dimension").get(identifier));
                            dynamicPriorities.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[0]));
                            dynamicFadeIn.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[1]));
                            dynamicFadeOut.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[35]));
                            dynamicDelay.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[4]));
                            triggerPersistence.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[33])) timeSwitch.add("dimension-" + identifier);
                        }
                    } else if (triggerPersistence.get("dimension-" + identifier) != null && triggerPersistence.get("dimension-" + identifier) > 0) {
                        if (!events.contains("dimension-" + identifier)) {
                            events.add("dimension-" + identifier);
                            dynamicSongs.put("dimension-" + identifier, SoundHandler.TriggerIdentifierMap.get("dimension").get(identifier));
                            dynamicPriorities.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[0]));
                            dynamicFadeIn.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[1]));
                            dynamicFadeOut.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[35]));
                            dynamicDelay.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[33])) timeSwitch.add("dimension-" + identifier);
                        }
                    }
                }
            }
            if (SoundHandler.TriggerIdentifierMap.get("biome") != null) {
                crashHelper = "biome";
                if (!configRegistry.clientSideOnly) {
                    for (String identifier : SoundHandler.TriggerIdentifierMap.get("biome").keySet()) {
                                PacketHandler.sendToServer(InfoForBiome.id, InfoForBiome.encode("biome-" + identifier, SoundHandler.TriggerInfoMap.get("biome-" + identifier)[9], roundedPos(player), player.getUuid(),
                                SoundHandler.TriggerInfoMap.get("biome-" + identifier)[23], SoundHandler.TriggerInfoMap.get("biome-" + identifier)[24],
                                SoundHandler.TriggerInfoMap.get("biome-" + identifier)[25], SoundHandler.TriggerInfoMap.get("biome-" + identifier)[26],
                                SoundHandler.TriggerInfoMap.get("biome-" + identifier)[30], SoundHandler.TriggerInfoMap.get("biome-" + identifier)[31]));
                        fromServer.inBiome.putIfAbsent("biome-" + identifier, false);
                        if (fromServer.inBiome.get("biome-" + identifier)) {
                            if (!events.contains("biome-" + identifier)) {
                                events.add("biome-" + identifier);
                                dynamicSongs.put("biome-" + identifier, SoundHandler.TriggerIdentifierMap.get("biome").get(identifier));
                                dynamicPriorities.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[0]));
                                dynamicFadeIn.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[1]));
                                dynamicFadeOut.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[35]));
                                dynamicDelay.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[4]));
                                triggerPersistence.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[3]));
                                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[33])) timeSwitch.add("biome-" + identifier);
                            }
                        } else if (triggerPersistence.get("biome-" + identifier) != null && triggerPersistence.get("biome-" + identifier) > 0) {
                            if (!events.contains("biome-" + identifier)) {
                                events.add("biome-" + identifier);
                                dynamicSongs.put("biome-" + identifier, SoundHandler.TriggerIdentifierMap.get("biome").get(identifier));
                                dynamicPriorities.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[0]));
                                dynamicFadeIn.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[1]));
                                dynamicFadeOut.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[35]));
                                dynamicDelay.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[4]));
                                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[33])) timeSwitch.add("biome-" + identifier);
                            }
                        }
                    }
                }
            }
            if (!configRegistry.clientSideOnly && SoundHandler.TriggerIdentifierMap.get("structure") != null) {
                crashHelper = "structure";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("structure").keySet()) {
                    PacketHandler.sendToServer(InfoForStructure.id, InfoForStructure.encode("structure-" + identifier, SoundHandler.TriggerInfoMap.get("structure-" + identifier)[9], player.getBlockPos(), player.getUuid()));
                    fromServer.inStructure.putIfAbsent("structure-" + identifier, false);
                    if (fromServer.inStructure.get("structure-" + identifier)) {
                        if (!events.contains("structure-" + identifier)) {
                            events.add("structure-" + identifier);
                            dynamicSongs.put("structure-" + identifier, SoundHandler.TriggerIdentifierMap.get("structure").get(identifier));
                            dynamicPriorities.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[0]));
                            dynamicFadeIn.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[1]));
                            dynamicFadeOut.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[35]));
                            dynamicDelay.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[4]));
                            triggerPersistence.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[33])) timeSwitch.add("structure-" + identifier);
                        }
                        fromServer.curStruct = SoundHandler.TriggerInfoMap.get("structure-" + identifier)[9];
                    } else if (triggerPersistence.get("structure-" + identifier) != null && triggerPersistence.get("structure-" + identifier) > 0) {
                        if (!events.contains("structure-" + identifier)) {
                            events.add("structure-" + identifier);
                            dynamicSongs.put("structure-" + identifier, SoundHandler.TriggerIdentifierMap.get("structure").get(identifier));
                            dynamicPriorities.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[0]));
                            dynamicFadeIn.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[1]));
                            dynamicFadeOut.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[35]));
                            dynamicDelay.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[33])) timeSwitch.add("structure-" + identifier);
                        }
                    }
                }
            }
            if (SoundHandler.TriggerIdentifierMap.get("mob") != null && !configRegistry.clientSideOnly) {
                crashHelper = "mob";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("mob").keySet()) {
                    triggerPersistence.putIfAbsent("mob-" + identifier, 0);
                    PacketHandler.sendToServer(InfoForMob.id, InfoForMob.encode("mob-" + identifier, player.getUuid(),
                            SoundHandler.TriggerInfoMap.get("mob-" + identifier)[9], SoundHandler.TriggerInfoMap.get("mob-" + identifier)[11],
                            SoundHandler.TriggerInfoMap.get("mob-" + identifier)[12], SoundHandler.TriggerInfoMap.get("mob-" + identifier)[13],
                            SoundHandler.TriggerInfoMap.get("mob-" + identifier)[14], SoundHandler.TriggerInfoMap.get("mob-" + identifier)[15],
                            SoundHandler.TriggerInfoMap.get("mob-" + identifier)[16], SoundHandler.TriggerInfoMap.get("mob-" + identifier)[17],
                            SoundHandler.TriggerInfoMap.get("mob-" + identifier)[2], Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[22]),
                            SoundHandler.TriggerInfoMap.get("mob-" + identifier)[27]));
                    fromServer.mob.putIfAbsent("mob-" + identifier, false);
                    if (fromServer.mob.get("mob-" + identifier)) {
                        if (!events.contains("mob-" + identifier)) {
                            events.add("mob-" + identifier);
                            dynamicSongs.put("mob-" + identifier, SoundHandler.TriggerIdentifierMap.get("mob").get(identifier));
                            dynamicPriorities.put("mob-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[0]));
                            dynamicFadeIn.put("mob-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[1]));
                            dynamicFadeOut.put("mob-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[35]));
                            dynamicDelay.put("mob-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[33])) timeSwitch.add("mob-" + identifier);
                        }
                    } else if (triggerPersistence.get("mob-" + identifier) != null && triggerPersistence.get("mob-" + identifier) > 0) {
                        if (!events.contains("mob-" + identifier)) {
                            events.add("mob-" + identifier);
                            dynamicSongs.put("mob-" + identifier, SoundHandler.TriggerIdentifierMap.get("mob").get(identifier));
                            dynamicPriorities.put("mob-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[0]));
                            dynamicFadeIn.put("mob-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[1]));
                            dynamicFadeOut.put("mob-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[35]));
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
            if (SoundHandler.TriggerIdentifierMap.get("zones") != null) {
                crashHelper = "zones";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("zones").keySet()) {
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
                            dynamicSongs.put("zones-" + identifier, SoundHandler.TriggerIdentifierMap.get("zones").get(identifier));
                            dynamicPriorities.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[0]));
                            dynamicFadeIn.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[1]));
                            dynamicFadeOut.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[35]));
                            dynamicDelay.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[4]));
                            triggerPersistence.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[33])) timeSwitch.add("zones-" + identifier);
                        }
                    } else if (triggerPersistence.get("zones-" + identifier) != null && triggerPersistence.get("zones-" + identifier) > 0) {
                        if (!events.contains("zones-" + identifier)) {
                            events.add("zones-" + identifier);
                            dynamicSongs.put("zones-" + identifier, SoundHandler.TriggerIdentifierMap.get("zones").get(identifier));
                            dynamicPriorities.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[0]));
                            dynamicFadeIn.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[1]));
                            dynamicFadeOut.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[35]));
                            dynamicDelay.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[33])) timeSwitch.add("zones-" + identifier);
                        }
                    }
                }
            }
            if (SoundHandler.TriggerIdentifierMap.get("effect") != null) {
                crashHelper = "effect";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("effect").keySet()) {
                    effectList = new ArrayList<>();
                    for (StatusEffect p : player.getActiveStatusEffects().keySet()) {
                        effectList.add(p.getName().getString());
                        if (checkResourceList(p.getName().toString(), SoundHandler.TriggerInfoMap.get("effect-" + identifier)[9],false)) {
                            if (!events.contains("effect-" + identifier)) {
                                events.add("effect-" + identifier);
                                dynamicSongs.put("effect-" + identifier, SoundHandler.TriggerIdentifierMap.get("effect").get(identifier));
                                dynamicPriorities.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[0]));
                                dynamicFadeIn.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[1]));
                                dynamicFadeOut.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[35]));
                                dynamicDelay.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[4]));
                                triggerPersistence.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[3]));
                                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[33])) timeSwitch.add("effect-" + identifier);
                            }
                        } else if (triggerPersistence.get("effect-" + identifier) != null && triggerPersistence.get("effect-" + identifier) > 0) {
                            if (!events.contains("effect-" + identifier)) {
                                events.add("effect-" + identifier);
                                dynamicSongs.put("effect-" + identifier, SoundHandler.TriggerIdentifierMap.get("effect").get(identifier));
                                dynamicPriorities.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[0]));
                                dynamicFadeIn.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[1]));
                                dynamicFadeOut.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[35]));
                                dynamicDelay.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[4]));
                                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[33])) timeSwitch.add("effect-" + identifier);
                            }
                        }
                    }
                }
            }
            if (SoundHandler.TriggerIdentifierMap.get("victory") != null) {
                crashHelper = "victory";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("victory").keySet()) {
                    if (victory.get(Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[17]))) {
                        if (!events.contains("victory-" + identifier)) {
                            events.add("victory-" + identifier);
                            dynamicSongs.put("victory-" + identifier, SoundHandler.TriggerIdentifierMap.get("victory").get(identifier));
                            dynamicPriorities.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[0]));
                            dynamicFadeIn.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[1]));
                            dynamicFadeOut.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[35]));
                            dynamicDelay.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[4]));
                            triggerPersistence.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[33])) timeSwitch.add("victory-" + identifier);

                        }
                        victory.put(Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[17]), false);
                    } else if (triggerPersistence.get("victory-" + identifier) != null && triggerPersistence.get("victory-" + identifier) > 0) {
                        if (!events.contains("victory-" + identifier)) {
                            events.add("victory-" + identifier);
                            dynamicSongs.put("victory-" + identifier, SoundHandler.TriggerIdentifierMap.get("victory").get(identifier));
                            dynamicPriorities.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[0]));
                            dynamicFadeIn.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[1]));
                            dynamicFadeOut.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[35]));
                            dynamicDelay.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[33])) timeSwitch.add("victory-" + identifier);
                        }
                    }
                }
            }
            if (mc.currentScreen != null && SoundHandler.TriggerIdentifierMap.get("gui") != null) {
                crashHelper = "gui";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("gui").keySet()) {
                    if (checkResourceList(mc.currentScreen.toString(), SoundHandler.TriggerInfoMap.get("gui-" + identifier)[9], false)) {
                        if (!events.contains("gui-" + identifier)) {
                            events.add("gui-" + identifier);
                            dynamicSongs.put("gui-" + identifier, SoundHandler.TriggerIdentifierMap.get("gui").get(identifier));
                            dynamicPriorities.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[0]));
                            dynamicFadeIn.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[1]));
                            dynamicFadeOut.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[35]));
                            dynamicDelay.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[4]));
                            triggerPersistence.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[33])) timeSwitch.add("gui-" + identifier);
                        }
                    } else if (SoundHandler.TriggerInfoMap.get("gui-" + identifier)[9].matches("CREDITS") && mc.currentScreen instanceof CreditsScreen) {
                        if (!events.contains("gui-" + identifier)) {
                            events.add("gui-" + identifier);
                            dynamicSongs.put("gui-" + identifier, SoundHandler.TriggerIdentifierMap.get("gui").get(identifier));
                            dynamicPriorities.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[0]));
                            dynamicFadeIn.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[1]));
                            dynamicFadeOut.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[35]));
                            dynamicDelay.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[4]));
                            triggerPersistence.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[33])) timeSwitch.add("gui-" + identifier);
                        }
                    } else if (triggerPersistence.get("gui-" + identifier) != null && triggerPersistence.get("gui-" + identifier) > 0) {
                        if (!events.contains("gui-" + identifier)) {
                            events.add("gui-" + identifier);
                            dynamicSongs.put("gui-" + identifier, SoundHandler.TriggerIdentifierMap.get("gui").get(identifier));
                            dynamicPriorities.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[0]));
                            dynamicFadeIn.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[1]));
                            dynamicFadeOut.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[35]));
                            dynamicDelay.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[33])) timeSwitch.add("gui-" + identifier);
                        }
                    }
                }
            } else {
                musicVolSave = mc.options.getSoundVolume(SoundCategory.MUSIC);
                masterVolSave = mc.options.getSoundVolume(SoundCategory.MASTER);
            }
            if (SoundHandler.TriggerIdentifierMap.get("difficulty") != null) {
                crashHelper = "difficulty";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("difficulty").keySet()) {
                    int diffID = Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[2]);
                    if (diffID == 4 && world.getLevelProperties().isHardcore()) {
                        if (!events.contains("difficulty-" + identifier)) {
                            events.add("difficulty-" + identifier);
                            dynamicSongs.put("difficulty-" + identifier, SoundHandler.TriggerIdentifierMap.get("difficulty").get(identifier));
                            dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                            dynamicFadeIn.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                            dynamicFadeOut.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[35]));
                            dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                            triggerPersistence.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[33])) timeSwitch.add("difficulty-" + identifier);
                        }
                    } else if (diffID == 3 && mc.world.getDifficulty() == Difficulty.HARD) {
                        if (!events.contains("difficulty-" + identifier)) {
                            events.add("difficulty-" + identifier);
                            dynamicSongs.put("difficulty-" + identifier, SoundHandler.TriggerIdentifierMap.get("difficulty").get(identifier));
                            dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                            dynamicFadeIn.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                            dynamicFadeOut.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[35]));
                            dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                            triggerPersistence.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[33])) timeSwitch.add("difficulty-" + identifier);
                        }
                    } else if (diffID == 2 && mc.world.getDifficulty() == Difficulty.NORMAL) {
                        if (!events.contains("difficulty-" + identifier)) {
                            events.add("difficulty-" + identifier);
                            dynamicSongs.put("difficulty-" + identifier, SoundHandler.TriggerIdentifierMap.get("difficulty").get(identifier));
                            dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                            dynamicFadeIn.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                            dynamicFadeOut.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[35]));
                            dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                            triggerPersistence.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[33])) timeSwitch.add("difficulty-" + identifier);
                        }
                    } else if (diffID == 1 && mc.world.getDifficulty() == Difficulty.EASY) {
                        if (!events.contains("difficulty-" + identifier)) {
                            events.add("difficulty-" + identifier);
                            dynamicSongs.put("difficulty-" + identifier, SoundHandler.TriggerIdentifierMap.get("difficulty").get(identifier));
                            dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                            dynamicFadeIn.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                            dynamicFadeOut.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[35]));
                            dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                            triggerPersistence.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[33])) timeSwitch.add("difficulty-" + identifier);
                        }
                    } else if (diffID == 0 && mc.world.getDifficulty() == Difficulty.PEACEFUL) {
                        if (!events.contains("difficulty-" + identifier)) {
                            events.add("difficulty-" + identifier);
                            dynamicSongs.put("difficulty-" + identifier, SoundHandler.TriggerIdentifierMap.get("difficulty").get(identifier));
                            dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                            dynamicFadeIn.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                            dynamicFadeOut.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[35]));
                            dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                            triggerPersistence.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[33])) timeSwitch.add("difficulty-" + identifier);
                        }
                    } else if (triggerPersistence.get("difficulty-" + identifier) != null && triggerPersistence.get("difficulty-" + identifier) > 0) {
                        if (!events.contains("difficulty-" + identifier)) {
                            events.add("difficulty-" + identifier);
                            dynamicSongs.put("difficulty-" + identifier, SoundHandler.TriggerIdentifierMap.get("difficulty").get(identifier));
                            dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                            dynamicFadeIn.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                            dynamicFadeOut.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[35]));
                            dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[33])) timeSwitch.add("difficulty-" + identifier);
                        }
                    }
                }
            }
            if (SoundHandler.TriggerIdentifierMap.get("advancement") != null) {
                crashHelper = "advancement";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("advancement").keySet()) {
                    if (eventsClient.advancement && (eventsClient.lastAdvancement.contains(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[5]) || SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[5].matches("YouWillNeverGuessThis"))) {
                        if (!events.contains("advancement-" + identifier)) {
                            events.add("advancement-" + identifier);
                            dynamicSongs.put("advancement-" + identifier, SoundHandler.TriggerIdentifierMap.get("advancement").get(identifier));
                            dynamicPriorities.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[0]));
                            dynamicFadeIn.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[1]));
                            dynamicFadeOut.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[35]));
                            dynamicDelay.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[4]));
                            triggerPersistence.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[33])) timeSwitch.add("advancement-" + identifier);
                        }
                    } else if (triggerPersistence.get("advancement-" + identifier) != null && triggerPersistence.get("advancement-" + identifier) > 0) {
                        if (!events.contains("advancement-" + identifier)) {
                            events.add("advancement-" + identifier);
                            dynamicSongs.put("advancement-" + identifier, SoundHandler.TriggerIdentifierMap.get("advancement").get(identifier));
                            dynamicPriorities.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[0]));
                            dynamicFadeIn.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[1]));
                            dynamicFadeOut.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[35]));
                            dynamicDelay.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[33])) timeSwitch.add("advancement-" + identifier);
                        }
                    }
                }
                eventsClient.advancement = false;
            }
            if (!configRegistry.clientSideOnly && SoundHandler.TriggerIdentifierMap.get("raid") != null) {
                crashHelper = "raid";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("raid").keySet()) {
                    PacketHandler.sendToServer(InfoForRaid.id, InfoForRaid.encode("raid-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[2]), player.getBlockPos(), player.getUuid()));
                    fromServer.isRaid.putIfAbsent("raid-" + identifier, false);
                    if (fromServer.isRaid.get("raid-" + identifier)) {
                        if (!events.contains("raid-" + identifier)) {
                            events.add("raid-" + identifier);
                            dynamicSongs.put("raid-" + identifier, SoundHandler.TriggerIdentifierMap.get("raid").get(identifier));
                            dynamicPriorities.put("raid-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[0]));
                            dynamicFadeIn.put("raid-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[1]));
                            dynamicFadeOut.put("raid-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[35]));
                            dynamicDelay.put("raid-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[4]));
                            triggerPersistence.put("raid-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[33])) timeSwitch.add("raid-" + identifier);
                        }
                        fromServer.curStruct = SoundHandler.TriggerInfoMap.get("raid-" + identifier)[9];
                    } else if (triggerPersistence.get("raid-" + identifier) != null && triggerPersistence.get("raid-" + identifier) > 0) {
                        if (!events.contains("raid-" + identifier)) {
                            events.add("raid-" + identifier);
                            dynamicSongs.put("raid-" + identifier, SoundHandler.TriggerIdentifierMap.get("raid").get(identifier));
                            dynamicPriorities.put("raid-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[0]));
                            dynamicFadeIn.put("raid-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[1]));
                            dynamicFadeOut.put("raid-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[35]));
                            dynamicDelay.put("raid-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("raid-" + identifier)[33])) timeSwitch.add("raid-" + identifier);
                        }
                    }
                }
            }
            if (SoundHandler.TriggerIdentifierMap.get("statistic") != null) {
                crashHelper = "statistic";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("statistic").keySet()) {
                    if (checkStat(SoundHandler.TriggerInfoMap.get("statistic-" + identifier)[9], Integer.parseInt(SoundHandler.TriggerInfoMap.get("statistic-" + identifier)[9]))) {
                        if (!events.contains("statistic-" + identifier)) {
                            events.add("statistic-" + identifier);
                            dynamicSongs.put("statistic-" + identifier, SoundHandler.TriggerIdentifierMap.get("statistic").get(identifier));
                            dynamicPriorities.put("statistic-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("statistic-" + identifier)[0]));
                            dynamicFadeIn.put("statistic-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("statistic-" + identifier)[1]));
                            dynamicFadeOut.put("statistic-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("statistic-" + identifier)[35]));
                            dynamicDelay.put("statistic-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("statistic-" + identifier)[4]));
                            triggerPersistence.put("statistic-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("statistic-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("statistic-" + identifier)[33])) timeSwitch.add("statistic-" + identifier);
                        }
                    } else if (triggerPersistence.get("statistic-" + identifier) != null && triggerPersistence.get("advancement-" + identifier) > 0) {
                        if (!events.contains("statistic-" + identifier)) {
                            events.add("statistic-" + identifier);
                            dynamicSongs.put("statistic-" + identifier, SoundHandler.TriggerIdentifierMap.get("statistic").get(identifier));
                            dynamicPriorities.put("statistic-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("statistic-" + identifier)[0]));
                            dynamicFadeIn.put("statistic-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("statistic-" + identifier)[1]));
                            dynamicFadeOut.put("statistic-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("statistic-" + identifier)[35]));
                            dynamicDelay.put("statistic-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("statistic-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("statistic-" + identifier)[33])) timeSwitch.add("statistic-" + identifier);
                        }
                    }
                }
                eventsClient.advancement = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("There was a problem with your "+crashHelper+" trigger! See the log for the full stack trace");
        }
        playableList = events;
        String triggersPacket = allTriggersAsSingleString();
        if(triggersPacket!=null) PacketHandler.sendToServer(AllTriggers.id, AllTriggers.encode(triggersPacket));
        return events;
    }

    public static BlockPos roundedPos(PlayerEntity p) {
        return new BlockPos((Math.round(p.getBlockPos().getX() * 2) / 2.0), (Math.round(p.getBlockPos().getY() * 2) / 2.0), (Math.round(p.getBlockPos().getZ() * 2) / 2.0));
    }

    public static double averageLight(BlockPos p, boolean b) {
        return b ? world.getLightLevel(p) : world.getLightLevel(LightType.BLOCK, p);
    }

    public static String[] stringBreaker(String s, String regex) {
        return s.split(regex);
    }

    public static boolean checkResourceList(String type, String resourceList, boolean match) {
        for(String resource : stringBreaker(resourceList,";")) {
            if(match && type.matches(resource)) return true;
            else if(!match && type.contains(resource)) return true;
        }
        return false;
    }

    public static String allTriggersAsSingleString() {
        StringBuilder ret = new StringBuilder();
        for(String trigger : playableList) {
            ret.append(trigger).append(",");
        }
        if(ret.length()!=0) return ret.substring(0, ret.length()-1);
        return null;
    }

    public static boolean checkStat(String statName, int level) {
        assert mc.player != null;
        ObjectArrayList<Stat<Identifier>> stats = new ObjectArrayList<>(Stats.CUSTOM.iterator());
        for(Stat<Identifier> stat : stats) {
            if(checkResourceList(stat.toString(),statName,false) && mc.player.getStatHandler().getStat(stat)>level) return true;
        }
        return false;
    }

    public static String allMenuSongsAsSingleString() {
        StringBuilder ret = new StringBuilder();
        if (SoundHandler.TriggerIdentifierMap.get("menu") != null) {
            for (String song : SoundHandler.TriggerIdentifierMap.get("menu").get("_")) {
                ret.append(configToml.songholder.get(song)).append(",");
            }
            if (ret.length() != 0) {
                return player.getUuidAsString()+","+ret.substring(0, ret.length() - 1);
            }
        }
        return null;
    }

    public static void emptyMapsAndLists() {
        triggerPersistence = new HashMap<>();
        victory = new HashMap<>();
        dynamicSongs = new HashMap<>();
        dynamicPriorities = new HashMap<>();
        dynamicFadeIn = new HashMap<>();
        dynamicFadeOut = new HashMap<>();
        dynamicDelay = new HashMap<>();
        playableList = new ArrayList<>();
        titleCardEvents = new ArrayList<>();
        effectList = new ArrayList<>();
    }
}