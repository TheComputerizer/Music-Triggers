package mods.thecomputerizer.musictriggers.client;

import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.weather.event.AcidRain;
import corgitaco.betterweather.weather.event.Blizzard;
import corgitaco.betterweather.weather.event.Cloudy;
import corgitaco.betterweather.weather.event.Rain;
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
import mods.thecomputerizer.musictriggers.util.packets.InfoForBiome;
import mods.thecomputerizer.musictriggers.util.packets.InfoForMob;
import mods.thecomputerizer.musictriggers.util.packets.InfoForStructure;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.WinGameScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.ModList;
import org.orecruncher.lib.WorldUtils;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

import java.util.*;

@SuppressWarnings("rawtypes")
public class MusicPicker {
    public static Minecraft mc;
    public static PlayerEntity player;
    public static World world;

    public static HashMap<String, Integer> triggerPersistence = new HashMap<>();
    public static HashMap<Integer, Boolean> victory = new HashMap<>();
    public static boolean fishBool = false;
    public static int fishingStart = 0;

    public static HashMap<String, List<String>> dynamicSongs = new HashMap<>();
    public static HashMap<String, Integer> dynamicPriorities = new HashMap<>();
    public static HashMap<String, Integer> dynamicFade = new HashMap<>();
    public static HashMap<String, Integer> dynamicDelay = new HashMap<>();

    public static List<String> playableList = new ArrayList<>();
    public static List<String> titleCardEvents = new ArrayList<>();

    public static List<String> effectList = new ArrayList<>();

    public static int curFade = 0;
    public static int curDelay = 0;
    public static boolean shouldChange = false;

    public static float musicVolSave;
    public static float masterVolSave;

    public static List<String> playThese() {
        if (!MusicPlayer.fading) {
            titleCardEvents = new ArrayList<>();
        }
        mc = Minecraft.getInstance();
        player = mc.player;
        if (player != null) {
            world = player.getCommandSenderWorld();
        }
        if(SoundHandler.TriggerSongMap.isEmpty()) {
            return null;
        }
        if(player == null) {
            if (SoundHandler.TriggerSongMap.get("menu") != null) {
                return new ArrayList<>(SoundHandler.TriggerSongMap.get("menu").keySet());
            }
            else return null;
        }
        List<String> res = comboChecker(priorityHandler(playableEvents()));
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
            return new ArrayList<>(SoundHandler.TriggerSongMap.get("generic").keySet());
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static List<String> comboChecker(String st) {
        if (st == null) {
            return null;
        }
        List<String> playableSongs = new ArrayList<>();
        for (String s : dynamicSongs.get(st)) {
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.songCombos.entrySet()) {
                String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                if (s.startsWith("@") && s.replaceAll("@","").matches(checkThis)) {
                    if (playableList.containsAll(SoundHandler.songCombos.get(s.replaceAll("@",""))) && SoundHandler.songCombos.get(s.replaceAll("@","")).size() != 1) {
                        playableSongs.add(s.substring(1));
                        if (!titleCardEvents.contains(st)) {
                            titleCardEvents.addAll(SoundHandler.songCombos.get(s.replaceAll("@","")));
                        }
                    }
                }
            }
        }
        if (playableSongs.isEmpty()) {
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
        List<String> events = new ArrayList<>();
        double time = (double) world.dayTime() / 24000.0;
        if (time > 1) {
            time = time - (long) time;
        }
        if (time < 0.54166666666) {
            events.add("day");
            dynamicSongs.put("day", new ArrayList<>(SoundHandler.TriggerSongMap.get("day").keySet()));
            dynamicPriorities.put("day", Integer.parseInt(SoundHandler.TriggerInfoMap.get("day")[0]));
            dynamicFade.put("day", Integer.parseInt(SoundHandler.TriggerInfoMap.get("day")[1]));
            dynamicDelay.put("day", Integer.parseInt(SoundHandler.TriggerInfoMap.get("day")[4]));
            triggerPersistence.put("day", Integer.parseInt(SoundHandler.TriggerInfoMap.get("day")[3]));
        }
        else if (triggerPersistence.get("day")!=null && triggerPersistence.get("day") > 0) {
            events.add("day");
            dynamicSongs.put("day", new ArrayList<>(SoundHandler.TriggerSongMap.get("day").keySet()));
            dynamicPriorities.put("day", Integer.parseInt(SoundHandler.TriggerInfoMap.get("day")[0]));
            dynamicFade.put("day", Integer.parseInt(SoundHandler.TriggerInfoMap.get("day")[1]));
            dynamicDelay.put("day", Integer.parseInt(SoundHandler.TriggerInfoMap.get("day")[4]));
        }
        else {
            if(SoundHandler.TriggerSongMap.get("night")!=null) {
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("night").entrySet()) {
                    String nightSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(nightSong.replaceAll("@","")).get("night")[10];
                    if (Integer.parseInt(SoundHandler.TriggerInfoMap.get("night-" + identifier)[21]) == 0) {
                        if (!events.contains("night-" + identifier)) {
                            events.add("night-" + identifier);
                        }
                        dynamicSongs.put("night-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("night"), identifier));
                        dynamicPriorities.put("night-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("night-" + identifier)[0]));
                        dynamicFade.put("night-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("night-" + identifier)[1]));
                        dynamicDelay.put("night-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("night-" + identifier)[4]));
                        triggerPersistence.put("night-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("night-" + identifier)[3]));
                    } else if (Integer.parseInt(SoundHandler.TriggerInfoMap.get("night-" + identifier)[21]) == world.getMoonPhase() + 1) {
                        if (!events.contains("night-" + identifier)) {
                            events.add("night-" + identifier);
                        }
                        dynamicSongs.put("night-" + identifier, new ArrayList<>(SoundHandler.TriggerSongMap.get("night-" + identifier).keySet()));
                        dynamicPriorities.put("night-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("night-" + identifier)[0]));
                        dynamicFade.put("night-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("night-" + identifier)[1]));
                        dynamicDelay.put("night-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("night-" + identifier)[4]));
                        triggerPersistence.put("night-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("night-" + identifier)[3]));
                    } else if (triggerPersistence.get("night-" + identifier) != null && triggerPersistence.get("night-" + identifier) > 0) {
                        if (!events.contains("night-" + identifier)) {
                            events.add("night-" + identifier);
                        }
                        dynamicSongs.put("night-" + identifier, new ArrayList<>(SoundHandler.TriggerSongMap.get("night-" + identifier).keySet()));
                        dynamicPriorities.put("night-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("night-" + identifier)[0]));
                        dynamicFade.put("night-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("night-" + identifier)[1]));
                        dynamicDelay.put("night-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("night-" + identifier)[4]));
                    }
                }
            }
        }
        if (time < 0.54166666666 && time >= 0.5 && SoundHandler.TriggerSongMap.get("sunset")!=null) {
            events.add("sunset");
            dynamicSongs.put("sunset", new ArrayList<>(SoundHandler.TriggerSongMap.get("sunset").keySet()));
            dynamicPriorities.put("sunset", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sunset")[0]));
            dynamicFade.put("sunset", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sunset")[1]));
            dynamicDelay.put("sunset", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sunset")[4]));
            triggerPersistence.put("sunset", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sunset")[3]));
        }
        else if (triggerPersistence.get("sunset")!=null && triggerPersistence.get("sunset") > 0) {
            events.add("sunset");
            dynamicSongs.put("sunset", new ArrayList<>(SoundHandler.TriggerSongMap.get("sunset").keySet()));
            dynamicPriorities.put("sunset", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sunset")[0]));
            dynamicFade.put("sunset", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sunset")[1]));
            dynamicDelay.put("sunset", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sunset")[4]));
        }
        else if (time >= 0.95833333333 && time < 1 && SoundHandler.TriggerSongMap.get("sunrise")!=null) {
            events.add("sunrise");
            dynamicSongs.put("sunrise", new ArrayList<>(SoundHandler.TriggerSongMap.get("sunrise").keySet()));
            dynamicPriorities.put("sunrise", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sunrise")[0]));
            dynamicFade.put("sunrise", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sunrise")[1]));
            dynamicDelay.put("sunrise", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sunrise")[4]));
            triggerPersistence.put("sunrise", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sunrise")[3]));
        }
        else if (triggerPersistence.get("sunrise")!=null && triggerPersistence.get("sunrise") > 0) {
            events.add("sunrise");
            dynamicSongs.put("sunrise", new ArrayList<>(SoundHandler.TriggerSongMap.get("sunrise").keySet()));
            dynamicPriorities.put("sunrise", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sunrise")[0]));
            dynamicFade.put("sunrise", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sunrise")[1]));
            dynamicDelay.put("sunrise", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sunrise")[4]));
        }
        if(SoundHandler.TriggerSongMap.get("light")!=null) {
            for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("light").entrySet()) {
                String lightSong = ((Map.Entry) stringListEntry).getKey().toString();
                String identifier = configToml.triggerholder.get(lightSong.replaceAll("@","")).get("light")[10];
                if (averageLight(roundedPos(player), Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("light-" + identifier)[20])) <= Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[2])) {
                    if (!events.contains("light-" + identifier)) {
                        events.add("light-" + identifier);
                    }
                    dynamicSongs.put("light-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("light"), identifier));
                    dynamicPriorities.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[0]));
                    dynamicFade.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[1]));
                    dynamicDelay.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[4]));
                    triggerPersistence.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[3]));
                } else if (triggerPersistence.get("light-" + identifier) != null && triggerPersistence.get("light-" + identifier) > 0) {
                    if (!events.contains("light-" + identifier)) {
                        events.add("light-" + identifier);
                    }
                    dynamicSongs.put("light-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("light"), identifier));
                    dynamicPriorities.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[0]));
                    dynamicFade.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[1]));
                    dynamicDelay.put("light-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("light-" + identifier)[4]));
                }
            }
        }
        if(SoundHandler.TriggerSongMap.get("underground")!=null) {
            for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("underground").entrySet()) {
                String undergroundSong = ((Map.Entry) stringListEntry).getKey().toString();
                String identifier = configToml.triggerholder.get(undergroundSong.replaceAll("@","")).get("underground")[10];
                if (player.getY() < Integer.parseInt(SoundHandler.TriggerInfoMap.get("underground-" + identifier)[2]) && !world.canSeeSky(roundedPos(player))) {
                    if (!events.contains("underground-" + identifier)) {
                        events.add("underground-" + identifier);
                    }
                    dynamicSongs.put("underground-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("underground"), identifier));
                    dynamicPriorities.put("underground-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("underground-" + identifier)[0]));
                    dynamicFade.put("underground-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("underground-" + identifier)[1]));
                    dynamicDelay.put("underground-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("underground-" + identifier)[4]));
                    triggerPersistence.put("underground-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("underground-" + identifier)[3]));
                } else if (triggerPersistence.get("underground-" + identifier) != null && triggerPersistence.get("underground-" + identifier) > 0) {
                    if (!events.contains("underground-" + identifier)) {
                        events.add("underground-" + identifier);
                    }
                    dynamicSongs.put("underground-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("underground"), identifier));
                    dynamicPriorities.put("underground-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("underground-" + identifier)[0]));
                    dynamicFade.put("underground-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("underground-" + identifier)[1]));
                    dynamicDelay.put("underground-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("underground-" + identifier)[4]));
                }
            }
        }
        if (SoundHandler.TriggerInfoMap.get("high")!=null && player.getY() >= Integer.parseInt(SoundHandler.TriggerInfoMap.get("high")[2])) {
            events.add("high");
            dynamicSongs.put("high", new ArrayList<>(SoundHandler.TriggerSongMap.get("high").keySet()));
            dynamicPriorities.put("high", Integer.parseInt(SoundHandler.TriggerInfoMap.get("high")[0]));
            dynamicFade.put("high", Integer.parseInt(SoundHandler.TriggerInfoMap.get("high")[1]));
            dynamicDelay.put("high", Integer.parseInt(SoundHandler.TriggerInfoMap.get("high")[4]));
            triggerPersistence.put("high", Integer.parseInt(SoundHandler.TriggerInfoMap.get("high")[3]));
        }
        else if (triggerPersistence.get("high")!=null && triggerPersistence.get("high") > 0) {
            events.add("high");
            dynamicSongs.put("high", new ArrayList<>(SoundHandler.TriggerSongMap.get("high").keySet()));
            dynamicPriorities.put("high", Integer.parseInt(SoundHandler.TriggerInfoMap.get("high")[0]));
            dynamicFade.put("high", Integer.parseInt(SoundHandler.TriggerInfoMap.get("high")[1]));
            dynamicDelay.put("high", Integer.parseInt(SoundHandler.TriggerInfoMap.get("high")[4]));
        }
        if(SoundHandler.TriggerInfoMap.get("elytra")!=null && player.getFallFlyingTicks()>Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[8])) {
            events.add("elytra");
            dynamicSongs.put("elytra", new ArrayList<>(SoundHandler.TriggerSongMap.get("elytra").keySet()));
            dynamicPriorities.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[0]));
            dynamicFade.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[1]));
            dynamicDelay.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[4]));
            triggerPersistence.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[3]));
        }
        else if (triggerPersistence.get("elytra")!=null && triggerPersistence.get("elytra") > 0) {
            events.add("elytra");
            dynamicSongs.put("elytra", new ArrayList<>(SoundHandler.TriggerSongMap.get("elytra").keySet()));
            dynamicPriorities.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[0]));
            dynamicFade.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[1]));
            dynamicDelay.put("elytra", Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[4]));
        }
        if(player.fishing!=null && player.fishing.isInWaterOrBubble()) {
            fishBool = true;
        }
        else {
            fishingStart=0;
        }
        if(SoundHandler.TriggerInfoMap.get("fishing")!=null && fishingStart>Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[8])) {
            events.add("fishing");
            dynamicSongs.put("fishing", new ArrayList<>(SoundHandler.TriggerSongMap.get("fishing").keySet()));
            dynamicPriorities.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[0]));
            dynamicFade.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[1]));
            dynamicDelay.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[4]));
            triggerPersistence.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[3]));
        }
        else if (triggerPersistence.get("fishing")!=null && triggerPersistence.get("fishing") > 0) {
            events.add("fishing");
            dynamicSongs.put("fishing", new ArrayList<>(SoundHandler.TriggerSongMap.get("fishing").keySet()));
            dynamicPriorities.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[0]));
            dynamicFade.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[1]));
            dynamicDelay.put("fishing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[4]));
        }
        if (world.isRaining() && SoundHandler.TriggerSongMap.get("raining")!=null) {
            events.add("raining");
            dynamicSongs.put("raining", new ArrayList<>(SoundHandler.TriggerSongMap.get("raining").keySet()));
            dynamicPriorities.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[0]));
            dynamicFade.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[1]));
            dynamicDelay.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[4]));
            triggerPersistence.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[3]));
            if (world.getBiome(roundedPos(player)).shouldSnow(world, roundedPos(player))) {
                events.add("snowing");
                dynamicSongs.put("snowing", new ArrayList<>(SoundHandler.TriggerSongMap.get("snowing").keySet()));
                dynamicPriorities.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[0]));
                dynamicFade.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[1]));
                dynamicDelay.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[4]));
                triggerPersistence.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[3]));
            }
            else if (triggerPersistence.get("snowing")!=null && triggerPersistence.get("snowing") > 0) {
                events.add("snowing");
                dynamicSongs.put("snowing", new ArrayList<>(SoundHandler.TriggerSongMap.get("snowing").keySet()));
                dynamicPriorities.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[0]));
                dynamicFade.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[1]));
                dynamicDelay.put("snowing", Integer.parseInt(SoundHandler.TriggerInfoMap.get("snowing")[4]));
            }
        }
        else if (triggerPersistence.get("raining")!=null && triggerPersistence.get("raining") > 0) {
            events.add("raining");
            dynamicSongs.put("raining", new ArrayList<>(SoundHandler.TriggerSongMap.get("raining").keySet()));
            dynamicPriorities.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[0]));
            dynamicFade.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[1]));
            dynamicDelay.put("raining", Integer.parseInt(SoundHandler.TriggerInfoMap.get("raining")[4]));
        }
        if (world.isThundering() && SoundHandler.TriggerSongMap.get("storming")!=null) {
            events.add("storming");
            dynamicSongs.put("storming", new ArrayList<>(SoundHandler.TriggerSongMap.get("storming").keySet()));
            dynamicPriorities.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[0]));
            dynamicFade.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[1]));
            dynamicDelay.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[4]));
            triggerPersistence.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[3]));
        }
        else if (triggerPersistence.get("storming")!=null && triggerPersistence.get("snowing") > 0) {
            events.add("storming");
            dynamicSongs.put("storming", new ArrayList<>(SoundHandler.TriggerSongMap.get("storming").keySet()));
            dynamicPriorities.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[0]));
            dynamicFade.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[1]));
            dynamicDelay.put("storming", Integer.parseInt(SoundHandler.TriggerInfoMap.get("storming")[4]));
        }
        if (SoundHandler.TriggerInfoMap.get("lowhp")!=null && player.getHealth() < player.getMaxHealth() * (Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[2])/100F)) {
            events.add("lowhp");
            dynamicSongs.put("lowhp", new ArrayList<>(SoundHandler.TriggerSongMap.get("lowhp").keySet()));
            dynamicPriorities.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[0]));
            dynamicFade.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[1]));
            dynamicDelay.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[4]));
            triggerPersistence.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[3]));
        }
        else if (triggerPersistence.get("lowhp")!=null && triggerPersistence.get("lowhp") > 0) {
            events.add("lowhp");
            dynamicSongs.put("lowhp", new ArrayList<>(SoundHandler.TriggerSongMap.get("lowhp").keySet()));
            dynamicPriorities.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[0]));
            dynamicFade.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[1]));
            dynamicDelay.put("lowhp", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[4]));
        }
        if (player.isDeadOrDying() && SoundHandler.TriggerSongMap.get("dead")!=null) {
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
        }
        else if (triggerPersistence.get("dead")!=null && triggerPersistence.get("dead") > 0) {
            events.add("dead");
            dynamicSongs.put("dead", new ArrayList<>(SoundHandler.TriggerSongMap.get("dead").keySet()));
            dynamicPriorities.put("dead", Integer.parseInt(SoundHandler.TriggerInfoMap.get("dead")[0]));
            dynamicFade.put("dead", Integer.parseInt(SoundHandler.TriggerInfoMap.get("dead")[1]));
            dynamicDelay.put("dead", Integer.parseInt(SoundHandler.TriggerInfoMap.get("dead")[4]));
        }
        if (player.isSpectator() && SoundHandler.TriggerSongMap.get("spectator")!=null) {
            events.add("spectator");
            dynamicSongs.put("spectator", new ArrayList<>(SoundHandler.TriggerSongMap.get("spectator").keySet()));
            dynamicPriorities.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[0]));
            dynamicFade.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[1]));
            dynamicDelay.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[4]));
            triggerPersistence.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[3]));
        }
        else if (triggerPersistence.get("spectator")!=null && triggerPersistence.get("spectator") > 0) {
            events.add("spectator");
            dynamicSongs.put("spectator", new ArrayList<>(SoundHandler.TriggerSongMap.get("spectator").keySet()));
            dynamicPriorities.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[0]));
            dynamicFade.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[1]));
            dynamicDelay.put("spectator", Integer.parseInt(SoundHandler.TriggerInfoMap.get("spectator")[4]));
        }
        if (player.isCreative() && SoundHandler.TriggerSongMap.get("creative")!=null) {
            events.add("creative");
            dynamicSongs.put("creative", new ArrayList<>(SoundHandler.TriggerSongMap.get("creative").keySet()));
            dynamicPriorities.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[0]));
            dynamicFade.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[1]));
            dynamicDelay.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[4]));
            triggerPersistence.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[3]));
        }
        else if (triggerPersistence.get("creative")!=null && triggerPersistence.get("creative") > 0) {
            events.add("creative");
            dynamicSongs.put("creative", new ArrayList<>(SoundHandler.TriggerSongMap.get("creative").keySet()));
            dynamicPriorities.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[0]));
            dynamicFade.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[1]));
            dynamicDelay.put("creative", Integer.parseInt(SoundHandler.TriggerInfoMap.get("creative")[4]));
        }
        if (player.isPassenger() && SoundHandler.TriggerSongMap.get("riding")!=null) {
            events.add("riding");
            dynamicSongs.put("riding", new ArrayList<>(SoundHandler.TriggerSongMap.get("riding").keySet()));
            dynamicPriorities.put("riding", Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding")[0]));
            dynamicFade.put("riding", Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding")[1]));
            dynamicDelay.put("riding", Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding")[4]));
            triggerPersistence.put("riding", Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding")[3]));
        }
        else if (triggerPersistence.get("riding")!=null && triggerPersistence.get("riding") > 0) {
            events.add("riding");
            dynamicSongs.put("riding", new ArrayList<>(SoundHandler.TriggerSongMap.get("riding").keySet()));
            dynamicPriorities.put("riding", Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding")[0]));
            dynamicFade.put("riding", Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding")[1]));
            dynamicDelay.put("riding", Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding")[4]));
        }
        if (SoundHandler.TriggerSongMap.get("underwater")!=null && ((world.getBlockState(roundedPos(player)).getMaterial() == Material.WATER || world.getBlockState(roundedPos(player)).getMaterial() == Material.WATER_PLANT || world.getBlockState(roundedPos(player)).getMaterial() == Material.REPLACEABLE_WATER_PLANT) && (world.getBlockState(roundedPos(player).above()).getMaterial() == Material.WATER || world.getBlockState(roundedPos(player).above()).getMaterial() == Material.WATER_PLANT || world.getBlockState(roundedPos(player).above()).getMaterial() == Material.REPLACEABLE_WATER_PLANT))) {
            events.add("underwater");
            dynamicSongs.put("underwater", new ArrayList<>(SoundHandler.TriggerSongMap.get("underwater").keySet()));
            dynamicPriorities.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[0]));
            dynamicFade.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[1]));
            dynamicDelay.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[4]));
            triggerPersistence.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[3]));
        }
        else if (triggerPersistence.get("underwater")!=null && triggerPersistence.get("underwater") > 0) {
            events.add("underwater");
            dynamicSongs.put("underwater", new ArrayList<>(SoundHandler.TriggerSongMap.get("underwater").keySet()));
            dynamicPriorities.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[0]));
            dynamicFade.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[1]));
            dynamicDelay.put("underwater", Integer.parseInt(SoundHandler.TriggerInfoMap.get("underwater")[4]));
        }
        for (LivingEntity ent : world.getEntitiesOfClass(LivingEntity.class, new AxisAlignedBB(player.getX() - 16, player.getY() - 8, player.getZ() - 16, player.getX() + 16, player.getY() + 8, player.getZ() + 16))) {
            if ((ent instanceof TameableEntity && ent.serializeNBT()!=null && ent.serializeNBT().getString("Owner").matches(player.getStringUUID())) && SoundHandler.TriggerSongMap.get("pet")!=null) {
                events.add("pet");
                dynamicSongs.put("pet", new ArrayList<>(SoundHandler.TriggerSongMap.get("pet").keySet()));
                dynamicPriorities.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[0]));
                dynamicFade.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[1]));
                dynamicDelay.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[4]));
                triggerPersistence.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[3]));
                break;
            }
        }
        if (triggerPersistence.get("pet")!=null && triggerPersistence.get("pet") > 0) {
            events.add("pet");
            dynamicSongs.put("pet", new ArrayList<>(SoundHandler.TriggerSongMap.get("pet").keySet()));
            dynamicPriorities.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[0]));
            dynamicFade.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[1]));
            dynamicDelay.put("pet", Integer.parseInt(SoundHandler.TriggerInfoMap.get("pet")[4]));
        }
        if(SoundHandler.TriggerSongMap.get("dimension")!=null) {
            for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("dimension").entrySet()) {
                String dimensionSong = ((Map.Entry) stringListEntry).getKey().toString();
                String identifier = configToml.triggerholder.get(dimensionSong.replaceAll("@","")).get("dimension")[10];
                if (player.level.dimension().location().toString().contains(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[9])) {
                    if (!events.contains("dimension-" + identifier)) {
                        events.add("dimension-" + identifier);
                    }
                    dynamicSongs.put("dimension-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("dimension"), identifier));
                    dynamicPriorities.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[0]));
                    dynamicFade.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[1]));
                    dynamicDelay.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[4]));
                    triggerPersistence.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[3]));
                } else if (triggerPersistence.get("dimension-" + identifier) != null && triggerPersistence.get("dimension-" + identifier) > 0) {
                    if (!events.contains("dimension-" + identifier)) {
                        events.add("dimension-" + identifier);
                    }
                    dynamicSongs.put("dimension-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("dimension"), identifier));
                    dynamicPriorities.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[0]));
                    dynamicFade.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[1]));
                    dynamicDelay.put("dimension-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[4]));
                }
            }
        }
        if(SoundHandler.TriggerSongMap.get("biome")!=null) {
            if (world.getBiome(roundedPos(player)).getRegistryName() != null) {
                fromServer.curBiome = world.getBiome(roundedPos(player)).getRegistryName().toString();
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("biome").entrySet()) {
                    String biomeSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(biomeSong.replaceAll("@", "")).get("biome")[10];
                    boolean pass = checkBiome(world.getBiome(roundedPos(player)), SoundHandler.TriggerInfoMap.get("biome-" + identifier)[9],
                            SoundHandler.TriggerInfoMap.get("biome-" + identifier)[23], SoundHandler.TriggerInfoMap.get("biome-" + identifier)[24],
                            Float.parseFloat(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[25]), Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[26]));
                    if (pass) {
                        if (!events.contains("biome-" + identifier)) {
                            events.add("biome-" + identifier);
                        }
                        dynamicSongs.put("biome-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("biome"), identifier));
                        dynamicPriorities.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[0]));
                        dynamicFade.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[1]));
                        dynamicDelay.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[4]));
                        triggerPersistence.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[3]));
                    } else if (triggerPersistence.get("biome-" + identifier) != null && triggerPersistence.get("biome-" + identifier) > 0) {
                        if (!events.contains("biome-" + identifier)) {
                            events.add("biome-" + identifier);
                        }
                        dynamicSongs.put("biome-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("biome"), identifier));
                        dynamicPriorities.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[0]));
                        dynamicFade.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[1]));
                        dynamicDelay.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[4]));
                    }
                }
            } else if (!configRegistry.clientSideOnly) {
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("biome").entrySet()) {
                    String biomeSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(biomeSong.replaceAll("@", "")).get("biome")[10];
                    PacketHandler.sendToServer(new InfoForBiome(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[9], roundedPos(player), player.getUUID(),
                            SoundHandler.TriggerInfoMap.get("biome-" + identifier)[23], SoundHandler.TriggerInfoMap.get("biome-" + identifier)[24],
                            SoundHandler.TriggerInfoMap.get("biome-" + identifier)[25], SoundHandler.TriggerInfoMap.get("biome-" + identifier)[26]));
                    if (fromServer.inBiome.get(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[9])) {
                        if (!events.contains("biome-" + identifier)) {
                            events.add("biome-" + identifier);
                        }
                        dynamicSongs.put("biome-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("biome"), identifier));
                        dynamicPriorities.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[0]));
                        dynamicFade.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[1]));
                        dynamicDelay.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[4]));
                        triggerPersistence.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[3]));
                    } else if (triggerPersistence.get("biome-" + identifier) != null && triggerPersistence.get("biome-" + identifier) > 0) {
                        if (!events.contains("biome-" + identifier)) {
                            events.add("biome-" + identifier);
                        }
                        dynamicSongs.put("biome-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("biome"), identifier));
                        dynamicPriorities.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[0]));
                        dynamicFade.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[1]));
                        dynamicDelay.put("biome-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[4]));
                    }
                }
            }
        }
        if (!configRegistry.clientSideOnly && SoundHandler.TriggerSongMap.get("structure")!=null) {
            for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("structure").entrySet()) {
                String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                String identifier = configToml.triggerholder.get(structureSong.replaceAll("@","")).get("structure")[10];
                PacketHandler.sendToServer(new InfoForStructure(SoundHandler.TriggerInfoMap.get("structure-"+identifier)[9], player.blockPosition(), player.getUUID()));
                if (fromServer.inStructure.containsKey(SoundHandler.TriggerInfoMap.get("structure-"+identifier)[9])) {
                    if (!events.contains("structure-" + identifier)) {
                        events.add("structure-" + identifier);
                    }
                    dynamicSongs.put("structure-"+identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("structure"), identifier));
                    dynamicPriorities.put("structure-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-"+identifier)[0]));
                    dynamicFade.put("structure-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-"+identifier)[1]));
                    dynamicDelay.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[4]));
                    triggerPersistence.put("structure-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-"+identifier)[3]));
                    fromServer.curStruct = SoundHandler.TriggerInfoMap.get("structure-"+identifier)[9];
                }
                else if (triggerPersistence.get("structure-"+identifier)!=null && triggerPersistence.get("structure-"+identifier) > 0) {
                    if (!events.contains("structure-" + identifier)) {
                        events.add("structure-" + identifier);
                    }
                    dynamicSongs.put("structure-"+identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("structure"), identifier));
                    dynamicPriorities.put("structure-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-"+identifier)[0]));
                    dynamicFade.put("structure-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-"+identifier)[1]));
                    dynamicDelay.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[4]));
                }
            }
        }
        if(SoundHandler.TriggerSongMap.get("mob")!=null) {
            for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("mob").entrySet()) {
                String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                String identifier = configToml.triggerholder.get(structureSong.replaceAll("@","")).get("mob")[10];
                triggerPersistence.putIfAbsent("mob-" + identifier, 0);
                PacketHandler.sendToServer(new InfoForMob(player.getUUID(),
                        SoundHandler.TriggerInfoMap.get("mob-" + identifier)[9], SoundHandler.TriggerInfoMap.get("mob-" + identifier)[11],
                        SoundHandler.TriggerInfoMap.get("mob-" + identifier)[12], SoundHandler.TriggerInfoMap.get("mob-" + identifier)[13],
                        SoundHandler.TriggerInfoMap.get("mob-" + identifier)[14], SoundHandler.TriggerInfoMap.get("mob-" + identifier)[15],
                        SoundHandler.TriggerInfoMap.get("mob-" + identifier)[16], SoundHandler.TriggerInfoMap.get("mob-" + identifier)[17],
                        SoundHandler.TriggerInfoMap.get("mob-" + identifier)[18], SoundHandler.TriggerInfoMap.get("mob-" + identifier)[2],
                        triggerPersistence.get("mob-" + identifier), Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[22])));
                if (fromServer.mob.get(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[9])) {
                    if (!events.contains("mob-" + identifier)) {
                        events.add("mob-" + identifier);
                    }
                    dynamicSongs.put("mob-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("mob"), identifier));
                    dynamicPriorities.put("mob-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[0]));
                    dynamicFade.put("mob-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[1]));
                    dynamicDelay.put("mob-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[4]));
                }
            }
        }
        if(SoundHandler.TriggerSongMap.get("zones")!=null) {
            for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("zones").entrySet()) {
                String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                String identifier = configToml.triggerholder.get(structureSong.replaceAll("@","")).get("zones")[10];
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
                    }
                    dynamicSongs.put("zones-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("zones"), identifier));
                    dynamicPriorities.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[0]));
                    dynamicFade.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[1]));
                    dynamicDelay.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[4]));
                    triggerPersistence.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[3]));
                } else if (triggerPersistence.get("zones-" + identifier) != null && triggerPersistence.get("zones-" + identifier) > 0) {
                    if (!events.contains("zones-" + identifier)) {
                        events.add("zones-" + identifier);
                    }
                    dynamicSongs.put("zones-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("zones"), identifier));
                    dynamicPriorities.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[0]));
                    dynamicFade.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[1]));
                    dynamicDelay.put("zones-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[4]));
                }
            }
        }
        if(SoundHandler.TriggerSongMap.get("effect")!=null) {
            for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("effect").entrySet()) {
                String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                String identifier = configToml.triggerholder.get(structureSong.replaceAll("@","")).get("effect")[10];
                effectList = new ArrayList<>();
                for (Effect p : player.getActiveEffectsMap().keySet()) {
                    effectList.add(p.getRegistryName().toString());
                    if (p.getRegistryName().toString().contains(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[9])) {
                        if (!events.contains("effect-" + identifier)) {
                            events.add("effect-" + identifier);
                        }
                        dynamicSongs.put("effect-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("effect"), identifier));
                        dynamicPriorities.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[0]));
                        dynamicFade.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[1]));
                        dynamicDelay.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[4]));
                        triggerPersistence.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[3]));
                    } else if (triggerPersistence.get("effect-" + identifier) != null && triggerPersistence.get("effect-" + identifier) > 0) {
                        if (!events.contains("effect-" + identifier)) {
                            events.add("effect-" + identifier);
                        }
                        dynamicSongs.put("effect-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("effect"), identifier));
                        dynamicPriorities.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[0]));
                        dynamicFade.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[1]));
                        dynamicDelay.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[4]));
                    }
                }
            }
        }
        if(SoundHandler.TriggerSongMap.get("victory")!=null) {
            for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("victory").entrySet()) {
                String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                String identifier = configToml.triggerholder.get(structureSong.replaceAll("@","")).get("victory")[10];
                if (victory.get(Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[17]))) {
                    if (!events.contains("victory-" + identifier)) {
                        events.add("victory-" + identifier);
                    }
                    dynamicSongs.put("victory-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("victory"), identifier));
                    dynamicPriorities.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[0]));
                    dynamicFade.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[1]));
                    dynamicDelay.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[4]));
                    triggerPersistence.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[3]));
                    victory.put(Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[17]), false);
                } else if (triggerPersistence.get("victory-" + identifier) != null && triggerPersistence.get("victory-" + identifier) > 0) {
                    if (!events.contains("victory-" + identifier)) {
                        events.add("victory-" + identifier);
                    }
                    dynamicSongs.put("victory-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("victory"), identifier));
                    dynamicPriorities.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[0]));
                    dynamicFade.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[1]));
                    dynamicDelay.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[4]));
                }
            }
        }
        if(mc.screen !=null && SoundHandler.TriggerSongMap.get("gui")!=null) {
            for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("gui").entrySet()) {
                String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                String identifier = configToml.triggerholder.get(structureSong.replaceAll("@","")).get("gui")[10];
                if(mc.screen.toString().contains(SoundHandler.TriggerInfoMap.get("gui-"+identifier)[9])) {
                    if (!events.contains("gui-" + identifier)) {
                        events.add("gui-" + identifier);
                    }
                    dynamicSongs.put("gui-"+identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("gui"), identifier));
                    dynamicPriorities.put("gui-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-"+identifier)[0]));
                    dynamicFade.put("gui-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-"+identifier)[1]));
                    dynamicDelay.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[4]));
                    triggerPersistence.put("gui-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-"+identifier)[3]));
                }
                else if (SoundHandler.TriggerInfoMap.get("effect-"+identifier)[9].matches("CREDITS") && mc.screen instanceof WinGameScreen) {
                    if (!events.contains("gui-" + identifier)) {
                        events.add("gui-" + identifier);
                    }
                    dynamicSongs.put("gui-"+identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("gui"), identifier));
                    dynamicPriorities.put("gui-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-"+identifier)[0]));
                    dynamicFade.put("gui-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-"+identifier)[1]));
                    dynamicDelay.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[4]));
                    triggerPersistence.put("gui-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-"+identifier)[3]));
                }
                else if (triggerPersistence.get("gui-"+identifier)!=null && triggerPersistence.get("gui-"+identifier) > 0) {
                    if (!events.contains("gui-" + identifier)) {
                        events.add("gui-" + identifier);
                    }
                    dynamicSongs.put("gui-"+identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("gui"), identifier));
                    dynamicPriorities.put("gui-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-"+identifier)[0]));
                    dynamicFade.put("gui-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-"+identifier)[1]));
                    dynamicDelay.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[4]));
                }
            }
        }
        else {
            musicVolSave = mc.options.getSoundSourceVolume(SoundCategory.MUSIC);
            masterVolSave = mc.options.getSoundSourceVolume(SoundCategory.MASTER);
        }
        if(SoundHandler.TriggerSongMap.get("difficulty")!=null) {
            for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("difficulty").entrySet()) {
                String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                String identifier = configToml.triggerholder.get(structureSong.replaceAll("@","")).get("difficulty")[10];
                int diffID = Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[2]);
                if (diffID == 4 && world.getLevelData().isHardcore()) {
                    if (!events.contains("difficulty-" + identifier)) {
                        events.add("difficulty-" + identifier);
                    }
                    dynamicSongs.put("difficulty-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("difficulty"), identifier));
                    dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                    dynamicFade.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                    dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                    triggerPersistence.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[3]));
                } else if (diffID == 3 && mc.level.getDifficulty() == Difficulty.HARD) {
                    if (!events.contains("difficulty-" + identifier)) {
                        events.add("difficulty-" + identifier);
                    }
                    dynamicSongs.put("difficulty-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("difficulty"), identifier));
                    dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                    dynamicFade.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                    dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                    triggerPersistence.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[3]));
                } else if (diffID == 2 && mc.level.getDifficulty() == Difficulty.NORMAL) {
                    if (!events.contains("difficulty-" + identifier)) {
                        events.add("difficulty-" + identifier);
                    }
                    dynamicSongs.put("difficulty-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("difficulty"), identifier));
                    dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                    dynamicFade.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                    dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                    triggerPersistence.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[3]));
                } else if (diffID == 1 && mc.level.getDifficulty() == Difficulty.EASY) {
                    if (!events.contains("difficulty-" + identifier)) {
                        events.add("difficulty-" + identifier);
                    }
                    dynamicSongs.put("difficulty-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("difficulty"), identifier));
                    dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                    dynamicFade.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                    dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                    triggerPersistence.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[3]));
                } else if (diffID == 0 && mc.level.getDifficulty() == Difficulty.PEACEFUL) {
                    if (!events.contains("difficulty-" + identifier)) {
                        events.add("difficulty-" + identifier);
                    }
                    dynamicSongs.put("difficulty-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("difficulty"), identifier));
                    dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                    dynamicFade.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                    dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                    triggerPersistence.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[3]));
                } else if (triggerPersistence.get("difficulty-" + identifier) != null && triggerPersistence.get("difficulty-" + identifier) > 0) {
                    if (!events.contains("difficulty-" + identifier)) {
                        events.add("difficulty-" + identifier);
                    }
                    dynamicSongs.put("difficulty-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("difficulty"), identifier));
                    dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                    dynamicFade.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                    dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                }
            }
        }
        if(SoundHandler.TriggerSongMap.get("advancement")!=null) {
            for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("advancement").entrySet()) {
                String advancementSong = ((Map.Entry) stringListEntry).getKey().toString();
                String identifier = configToml.triggerholder.get(advancementSong.replaceAll("@", "")).get("advancement")[10];
                if(eventsClient.advancement && (eventsClient.lastAdvancement.contains(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[5]) || SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[5].matches("YouWillNeverGuessThis"))) {
                    dynamicSongs.put("advancement-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("advancement"), identifier));
                    dynamicPriorities.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[0]));
                    dynamicFade.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[1]));
                    dynamicDelay.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[4]));
                    triggerPersistence.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[3]));
                } else if (triggerPersistence.get("advancement-" + identifier) != null && triggerPersistence.get("advancement-" + identifier) > 0) {
                    if(!events.contains("advancement-" + identifier)) {
                        events.add("advancement-" + identifier);
                    }
                    dynamicSongs.put("advancement-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("advancement"), identifier));
                    dynamicPriorities.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[0]));
                    dynamicFade.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[1]));
                    dynamicDelay.put("advancement-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[4]));
                }
            }
            eventsClient.advancement = false;
        }
        List<String> stages = gamestages();
        if (!stages.isEmpty()) {
            events.addAll(stages);
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

        String dynamicrain = dynamicrain();
        if (dynamicrain != null && !events.contains(dynamicrain)) {
            events.add(dynamicrain);
        }

        boolean acidrain = acidrain();
        if (acidrain) {
            events.add("acidrain");
        }
        boolean blizzard = blizzard();
        if (blizzard) {
            events.add("blizzard");
        }
        boolean cloudy = cloudy();
        if (cloudy) {
            events.add("cloudy");
        }
        boolean lightrain = lightrain();
        if (lightrain) {
            events.add("lightrain");
        }
        List<String> seasons = seasons();
        if(!seasons.isEmpty()) {
            events.addAll(seasons);
        }

        playableList = events;
        return events;
    }

    private static List<String> gamestages() {
        if (ModList.get().isLoaded("gamestages") && SoundHandler.TriggerSongMap.get("gamestage")!=null) {
            List<String> events = new ArrayList<>();
            for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("gamestage").entrySet()) {
                String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                String identifier = configToml.triggerholder.get(structureSong.replaceAll("@","")).get("gamestage")[10];
                if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[9])) {
                    if (GameStageHelper.hasStage(player, SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[19])) {
                        if (!events.contains("gamestage-" + identifier)) {
                            events.add("gamestage-" + identifier);
                        }
                        dynamicSongs.put("gamestage-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("gamestage"), identifier));
                        dynamicPriorities.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[0]));
                        dynamicFade.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[1]));
                        dynamicDelay.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[4]));
                        triggerPersistence.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[3]));
                    } else if (triggerPersistence.get("gamestage-" + identifier) != null && triggerPersistence.get("gamestage-" + identifier) > 0) {
                        if (!events.contains("gamestage-" + identifier)) {
                            events.add("gamestage-" + identifier);
                        }
                        dynamicSongs.put("gamestage-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("gamestage"), identifier));
                        dynamicPriorities.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[0]));
                        dynamicFade.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[1]));
                        dynamicDelay.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[4]));
                    }
                } else {
                    if (!GameStageHelper.hasStage(player, SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[19])) {
                        if (!events.contains("gamestage-" + identifier)) {
                            events.add("gamestage-" + identifier);
                        }
                        dynamicSongs.put("gamestage-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("gamestage"), identifier));
                        dynamicPriorities.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[0]));
                        dynamicFade.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[1]));
                        dynamicDelay.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[4]));
                        triggerPersistence.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[3]));
                    } else if (triggerPersistence.get("gamestage-" + identifier) != null && triggerPersistence.get("gamestage-" + identifier) > 0) {
                        if (!events.contains("gamestage-" + identifier)) {
                            events.add("gamestage-" + identifier);
                        }
                        dynamicSongs.put("gamestage-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("gamestage"), identifier));
                        dynamicPriorities.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[0]));
                        dynamicFade.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[1]));
                        dynamicDelay.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[4]));
                    }
                }
            }
            return events;
        } else {
            return new ArrayList<>();
        }
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
                return true;
            }
            else if (triggerPersistence.get("bloodmoon")!=null && triggerPersistence.get("bloodmoon") > 0) {
                dynamicSongs.put("bloodmoon", new ArrayList<>(SoundHandler.TriggerSongMap.get("bloodmoon").keySet()));
                dynamicPriorities.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[0]));
                dynamicFade.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[1]));
                dynamicDelay.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[4]));
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
                return true;
            }
            else if (triggerPersistence.get("harvestmoon")!=null && triggerPersistence.get("harvestmoon") > 0) {
                dynamicSongs.put("harvestmoon", new ArrayList<>(SoundHandler.TriggerSongMap.get("harvestmoon").keySet()));
                dynamicPriorities.put("harvestmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("harvestmoon")[0]));
                dynamicFade.put("harvestmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("harvestmoon")[1]));
                dynamicDelay.put("harvestmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("harvestmoon")[4]));
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
                return true;
            }
            else if (triggerPersistence.get("bluemoon")!=null && triggerPersistence.get("bluemoon") > 0) {
                dynamicSongs.put("bluemoon", new ArrayList<>(SoundHandler.TriggerSongMap.get("bluemoon").keySet()));
                dynamicPriorities.put("bluemoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bluemoon")[0]));
                dynamicFade.put("bluemoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bluemoon")[1]));
                dynamicDelay.put("bluemoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bluemoon")[4]));
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
                        }
                        dynamicSongs.put("moon-"+identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("moon"), identifier));
                        dynamicPriorities.put("moon-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("moon-"+identifier)[0]));
                        dynamicFade.put("moon-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("moon-"+identifier)[1]));
                        dynamicDelay.put("moon-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("moon-"+identifier)[4]));
                        triggerPersistence.put("moon-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("moon-"+identifier)[3]));
                    } else if (triggerPersistence.get("moon-"+identifier) != null && triggerPersistence.get("moon-"+identifier) > 0) {
                        if (!events.contains("moon-" + identifier)) {
                            events.add("moon-" + identifier);
                        }
                        dynamicSongs.put("moon-"+identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("moon"), identifier));
                        dynamicPriorities.put("moon-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("moon-"+identifier)[0]));
                        dynamicFade.put("moon-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("moon-"+identifier)[1]));
                        dynamicDelay.put("moon-"+identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("moon-"+identifier)[4]));
                    }
                }
            }
        }
        return events;
    }

    private static String dynamicrain() {
        if (ModList.get().isLoaded("dsurround") && SoundHandler.TriggerSongMap.get("rainintensity")!=null) {
            for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("rainintensity").entrySet()) {
                String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                String identifier = configToml.triggerholder.get(structureSong.replaceAll("@","")).get("rainintensity")[10];
                if (WorldUtils.getRainStrength(world, 1F) > Float.parseFloat(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[2]) / 100F) {
                    dynamicSongs.put("rainintensity-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("rainintensity"), identifier));
                    dynamicPriorities.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[0]));
                    dynamicFade.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[1]));
                    dynamicDelay.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[4]));
                    triggerPersistence.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity" + identifier)[3]));
                    return "rainintensity-" + identifier;
                } else if (triggerPersistence.get("rainintensity-" + identifier) != null && triggerPersistence.get("rainintensity-" + identifier) > 0) {
                    dynamicSongs.put("rainintensity-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("rainintensity"), identifier));
                    dynamicPriorities.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[0]));
                    dynamicFade.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[1]));
                    dynamicDelay.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[4]));
                    return "rainintensity-" + identifier;
                }
            }
        }
        return null;
    }

    private static boolean acidrain() {
        if (ModList.get().isLoaded("betterweather") && SoundHandler.TriggerSongMap.get("acidrain")!=null) {
            BetterWeatherWorldData weatherdata = (BetterWeatherWorldData) world;
            if (weatherdata.getWeatherEventContext() != null && weatherdata.getWeatherEventContext().getCurrentEvent() instanceof AcidRain) {
                dynamicSongs.put("acidrain", new ArrayList<>(SoundHandler.TriggerSongMap.get("acidrain").keySet()));
                dynamicPriorities.put("acidrain", Integer.parseInt(SoundHandler.TriggerInfoMap.get("acidrain")[0]));
                dynamicFade.put("acidrain", Integer.parseInt(SoundHandler.TriggerInfoMap.get("acidrain")[1]));
                dynamicDelay.put("acidrain", Integer.parseInt(SoundHandler.TriggerInfoMap.get("acidrain")[4]));
                triggerPersistence.put("acidrain", Integer.parseInt(SoundHandler.TriggerInfoMap.get("acidrain")[3]));
                return true;
            }
            else if (triggerPersistence.get("acidrain")!=null && triggerPersistence.get("acidrain") > 0) {
                dynamicSongs.put("acidrain", new ArrayList<>(SoundHandler.TriggerSongMap.get("acidrain").keySet()));
                dynamicPriorities.put("acidrain", Integer.parseInt(SoundHandler.TriggerInfoMap.get("acidrain")[0]));
                dynamicFade.put("acidrain", Integer.parseInt(SoundHandler.TriggerInfoMap.get("acidrain")[1]));
                dynamicDelay.put("acidrain", Integer.parseInt(SoundHandler.TriggerInfoMap.get("acidrain")[4]));
                return true;
            }
        }
        return false;
    }

    private static boolean blizzard() {
        if (ModList.get().isLoaded("betterweather") && SoundHandler.TriggerSongMap.get("blizzard")!=null) {
            BetterWeatherWorldData weatherdata = (BetterWeatherWorldData) world;
            if (weatherdata.getWeatherEventContext() != null && weatherdata.getWeatherEventContext().getCurrentEvent() instanceof Blizzard) {
                dynamicSongs.put("blizzard", new ArrayList<>(SoundHandler.TriggerSongMap.get("blizzard").keySet()));
                dynamicPriorities.put("blizzard", Integer.parseInt(SoundHandler.TriggerInfoMap.get("blizzard")[0]));
                dynamicFade.put("blizzard", Integer.parseInt(SoundHandler.TriggerInfoMap.get("blizzard")[1]));
                dynamicDelay.put("blizzard", Integer.parseInt(SoundHandler.TriggerInfoMap.get("blizzard")[4]));
                triggerPersistence.put("blizzard", Integer.parseInt(SoundHandler.TriggerInfoMap.get("blizzard")[3]));
                return true;
            }
            else if (triggerPersistence.get("blizzard")!=null && triggerPersistence.get("blizzard") > 0) {
                dynamicSongs.put("blizzard", new ArrayList<>(SoundHandler.TriggerSongMap.get("blizzard").keySet()));
                dynamicPriorities.put("blizzard", Integer.parseInt(SoundHandler.TriggerInfoMap.get("blizzard")[0]));
                dynamicFade.put("blizzard", Integer.parseInt(SoundHandler.TriggerInfoMap.get("blizzard")[1]));
                dynamicDelay.put("blizzard", Integer.parseInt(SoundHandler.TriggerInfoMap.get("blizzard")[4]));
                return true;
            }
        }
        return false;
    }

    private static boolean cloudy() {
        if (ModList.get().isLoaded("betterweather") && SoundHandler.TriggerSongMap.get("cloudy")!=null) {
            BetterWeatherWorldData weatherdata = (BetterWeatherWorldData) world;
            if (weatherdata.getWeatherEventContext() != null && weatherdata.getWeatherEventContext().getCurrentEvent() instanceof Cloudy) {
                dynamicSongs.put("cloudy", new ArrayList<>(SoundHandler.TriggerSongMap.get("cloudy").keySet()));
                dynamicPriorities.put("cloudy", Integer.parseInt(SoundHandler.TriggerInfoMap.get("cloudy")[0]));
                dynamicFade.put("cloudy", Integer.parseInt(SoundHandler.TriggerInfoMap.get("cloudy")[1]));
                dynamicDelay.put("cloudy", Integer.parseInt(SoundHandler.TriggerInfoMap.get("cloudy")[4]));
                triggerPersistence.put("cloudy", Integer.parseInt(SoundHandler.TriggerInfoMap.get("cloudy")[3]));
                return true;
            }
            else if (triggerPersistence.get("cloudy")!=null && triggerPersistence.get("cloudy") > 0) {
                dynamicSongs.put("cloudy", new ArrayList<>(SoundHandler.TriggerSongMap.get("cloudy").keySet()));
                dynamicPriorities.put("cloudy", Integer.parseInt(SoundHandler.TriggerInfoMap.get("cloudy")[0]));
                dynamicFade.put("cloudy", Integer.parseInt(SoundHandler.TriggerInfoMap.get("cloudy")[1]));
                dynamicDelay.put("cloudy", Integer.parseInt(SoundHandler.TriggerInfoMap.get("cloudy")[4]));
                return true;
            }
        }
        return false;
    }

    private static boolean lightrain() {
        if (ModList.get().isLoaded("betterweather") && SoundHandler.TriggerSongMap.get("lightrain")!=null) {
            BetterWeatherWorldData weatherdata = (BetterWeatherWorldData) world;
            if (weatherdata.getWeatherEventContext() != null && weatherdata.getWeatherEventContext().getCurrentEvent() instanceof Rain && !(weatherdata.getWeatherEventContext().getCurrentEvent() instanceof AcidRain)) {
                dynamicSongs.put("lightrain", new ArrayList<>(SoundHandler.TriggerSongMap.get("lightrain").keySet()));
                dynamicPriorities.put("lightrain", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lightrain")[0]));
                dynamicFade.put("lightrain", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lightrain")[1]));
                dynamicDelay.put("lightrain", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lightrain")[4]));
                triggerPersistence.put("lightrain", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lightrain")[3]));
                return true;
            }
            else if (triggerPersistence.get("lightrain")!=null && triggerPersistence.get("lightrain") > 0) {
                dynamicSongs.put("lightrain", new ArrayList<>(SoundHandler.TriggerSongMap.get("lightrain").keySet()));
                dynamicPriorities.put("lightrain", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lightrain")[0]));
                dynamicFade.put("lightrain", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lightrain")[1]));
                dynamicDelay.put("lightrain", Integer.parseInt(SoundHandler.TriggerInfoMap.get("lightrain")[4]));
                return true;
            }
        }
        return false;
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
                        dynamicSongs.put("season-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("season"), identifier));
                        dynamicPriorities.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[0]));
                        dynamicFade.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[1]));
                        dynamicDelay.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[4]));
                        triggerPersistence.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season" + identifier)[3]));
                        if (!tempList.contains("season:" + seasonID)) {
                            tempList.add("season:" + seasonID);
                        }
                    } else if (seasonID == 1 && curSeason.getSeason() == Season.SUMMER) {
                        dynamicSongs.put("season-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("season"), identifier));
                        dynamicPriorities.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[0]));
                        dynamicFade.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[1]));
                        dynamicDelay.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[4]));
                        triggerPersistence.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season" + identifier)[3]));
                        if (!tempList.contains("season:" + seasonID)) {
                            tempList.add("season:" + seasonID);
                        }
                    } else if (seasonID == 2 && curSeason.getSeason() == Season.AUTUMN) {
                        dynamicSongs.put("season-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("season"), identifier));
                        dynamicPriorities.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[0]));
                        dynamicFade.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[1]));
                        dynamicDelay.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[4]));
                        triggerPersistence.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season" + identifier)[3]));
                        if (!tempList.contains("season:" + seasonID)) {
                            tempList.add("season:" + seasonID);
                        }
                    } else if (seasonID == 3 && curSeason.getSeason() == Season.WINTER) {
                        dynamicSongs.put("season-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("season"), identifier));
                        dynamicPriorities.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[0]));
                        dynamicFade.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[1]));
                        dynamicDelay.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[4]));
                        triggerPersistence.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season" + identifier)[3]));
                        if (!tempList.contains("season:" + seasonID)) {
                            tempList.add("season:" + seasonID);
                        }
                    } else if (triggerPersistence.get("season-" + identifier) != null && triggerPersistence.get("season-" + identifier) > 0) {
                        dynamicSongs.put("season-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("season"), identifier));
                        dynamicPriorities.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[0]));
                        dynamicFade.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[1]));
                        dynamicDelay.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[4]));
                    }
                }
            }
            return tempList;
        }
        return tempList;
    }

    public static BlockPos roundedPos(PlayerEntity p) {
        return new BlockPos((Math.round(p.blockPosition().getX() * 2) / 2.0), (Math.round(p.blockPosition().getY() * 2) / 2.0), (Math.round(p.blockPosition().getZ() * 2) / 2.0));
    }

    public static double averageLight(BlockPos p, boolean b) {
        return b ? world.getRawBrightness(p, 0) : world.getBrightness(LightType.BLOCK, p);
    }

    public static boolean checkBiome(Biome b, String name, String category, String rainType, float temperature, boolean cold) {
        if(Objects.requireNonNull(b.getRegistryName()).toString().contains(name) || name.matches("minecraft")) {
            if(b.getBiomeCategory().getName().contains(category) || category.matches("nope")) {
                if(b.getPrecipitation().getName().contains(rainType) || rainType.matches("nope")) {
                    float bt = b.getBaseTemperature();
                    if(temperature==-111) return true;
                    else if(bt>=temperature && !cold) return true;
                    else return bt <= temperature && cold;
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
}