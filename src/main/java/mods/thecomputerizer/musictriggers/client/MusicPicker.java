package mods.thecomputerizer.musictriggers.client;

import CoroUtil.util.Vec3;
import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.lunarevents.BloodMoon;
import de.ellpeck.nyx.lunarevents.HarvestMoon;
import de.ellpeck.nyx.lunarevents.StarShower;
import lumien.bloodmoon.Bloodmoon;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config.configRegistry;
import mods.thecomputerizer.musictriggers.config.configToml;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.musictriggers.util.packets.packet;
import mods.thecomputerizer.musictriggers.util.packets.packetAllTriggers;
import mods.thecomputerizer.musictriggers.util.packets.packetSendMobInfo;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Optional;
import org.orecruncher.dsurround.client.weather.Weather;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;
import weather2.api.WeatherDataHelper;
import weather2.weathersystem.storm.StormObject;

import java.util.*;

public class MusicPicker {
    public static Minecraft mc;
    public static EntityPlayer player;
    public static World world;

    public static HashMap<String, Integer> triggerPersistence = new HashMap<>();
    public static HashMap<Integer, Boolean> victory = new HashMap<>();
    public static boolean fishBool = false;
    public static int fishingStart = 0;
    public static boolean setPVP = false;
    public static int pvpVictoryID = 0;

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

    public static String[] playThese() {
        if (!MusicPlayer.fading) titleCardEvents = new ArrayList<>();
        mc = Minecraft.getMinecraft();
        player = mc.player;
        if (player != null) {
            world = player.getEntityWorld();
        }
        if(SoundHandler.TriggerSongMap.isEmpty()) {
            return null;
        }
        if(player == null) {
            if (SoundHandler.TriggerSongMap.get("menu") != null) {
                musicVolSave = mc.gameSettings.getSoundLevel(SoundCategory.MUSIC);
                masterVolSave = mc.gameSettings.getSoundLevel(SoundCategory.MASTER);
                return SoundHandler.TriggerSongMap.get("menu").keySet().toArray(new String[0]);
            }
            else
                return null;
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
            return res.toArray(new String[0]);
        }
        dynamicSongs = new HashMap<>();
        dynamicPriorities = new HashMap<>();
        dynamicFade = new HashMap<>();
        if(SoundHandler.TriggerInfoMap.get("generic") != null) {
            curFade = Integer.parseInt(SoundHandler.TriggerInfoMap.get("generic")[1]);
            return SoundHandler.TriggerSongMap.get("generic").keySet().toArray(new String[0]);
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
                    MusicTriggers.logger.info("Skipping");
                }
            }
            if(!skip) {
                for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.instantiationCombos.entrySet()) {
                    String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                    if (s.startsWith("$") && s.replaceAll("\\$","").matches(checkThis)) {
                        if (playableList.containsAll(SoundHandler.instantiationCombos.get(s.replaceAll("\\$", ""))) && SoundHandler.instantiationCombos.get(s.replaceAll("\\$", "")).size() != 1) {
                            playableSongs.add(s.substring(1));
                            if (!titleCardEvents.contains(st)) {
                                titleCardEvents.addAll(SoundHandler.instantiationCombos.get(s.replaceAll("\\$", "")));
                            }
                        }
                    }
                }
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
                if (!s.startsWith("@") && !s.startsWith("$")) {
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

    @SuppressWarnings("rawtypes")
    public static List<String> playableEvents() {
        String crashHelper = "";
        List<String> events = new ArrayList<>();
        try {
            double time = (double) world.getWorldTime() / 24000.0;
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
                        pass = player.posY < Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[2]) && !world.canSeeSky(roundedPos(player));
                    else
                        pass = player.posY > Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[2]);
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
            if (SoundHandler.TriggerInfoMap.get("elytra") != null && player.getTicksElytraFlying() > Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[8])) {
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
            if (player.fishEntity != null && player.fishEntity.isOverWater()) {
                fishBool = true;
            } else {
                fishingStart = 0;
            }
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
            if (triggerPersistence.get("snowing") != null && world.isRaining() && world.getBiomeProvider().getTemperatureAtHeight(world.getBiome(roundedPos(player)).getTemperature(roundedPos(player)), world.getPrecipitationHeight(roundedPos(player)).getY()) < 0.15f) {
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
            if (player.isDead && SoundHandler.TriggerSongMap.get("dead") != null) {
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
            if (SoundHandler.TriggerSongMap.get("riding") != null && player.isRiding()) {
                crashHelper = "riding";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("riding").entrySet()) {
                    String ridingSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(ridingSong.replaceAll("@", "").replaceAll("#", "")).get("riding")[10];
                    String ridingName = SoundHandler.TriggerInfoMap.get("riding-" + identifier)[9];
                    if (Objects.requireNonNull(player.getRidingEntity()).getName().matches(ridingName) || Objects.requireNonNull(EntityList.getKey(player.getRidingEntity())).toString().matches(ridingName) || ridingName.matches("minecraft")) {
                        if (!events.contains("riding-" + identifier)) {
                            events.add("riding-" + identifier);
                            dynamicSongs.put("riding-" + identifier, new ArrayList<>(SoundHandler.TriggerSongMap.get("riding-" + identifier).keySet()));
                            dynamicPriorities.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[0]));
                            dynamicFade.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[1]));
                            dynamicDelay.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[4]));
                            triggerPersistence.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[33])) timeSwitch.add("riding-" + identifier);
                        }
                    } else if (triggerPersistence.get("riding-" + identifier) != null && triggerPersistence.get("riding-" + identifier) > 0) {
                        if (!events.contains("riding-" + identifier)) {
                            events.add("riding-" + identifier);
                            dynamicSongs.put("riding-" + identifier, new ArrayList<>(SoundHandler.TriggerSongMap.get("riding-" + identifier).keySet()));
                            dynamicPriorities.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[0]));
                            dynamicFade.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[1]));
                            dynamicDelay.put("riding-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[33])) timeSwitch.add("riding-" + identifier);
                        }
                    }
                }
            }
            if (world.getBlockState(roundedPos(player)).getMaterial() == Material.WATER && world.getBlockState(roundedPos(player).up()).getMaterial() == Material.WATER && SoundHandler.TriggerSongMap.get("underwater") != null) {
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
            for (EntityLiving ent : world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(player.posX - 16, player.posY - 8, player.posZ - 16, player.posX + 16, player.posY + 8, player.posZ + 16))) {
                if (ent instanceof EntityTameable && ent.serializeNBT().getString("Owner").matches(player.getName()) && SoundHandler.TriggerSongMap.get("pet") != null) {
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
            if (triggerPersistence.get("drowning") != null && player.getAir() < Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[2])) {
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
            if (eventsClient.PVPTracker != null && eventsClient.PVPTracker.isDead) {
                victory.put(pvpVictoryID, true);
                eventsClient.PVPTracker = null;
            }
            if (triggerPersistence.get("home") != null && player.getBedLocation(player.dimension).getDistance(roundedPos(player).getX(), roundedPos(player).getY(), roundedPos(player).getZ()) < Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[11])) {
                crashHelper = "home";
                events.add("home");
                dynamicSongs.put("home", new ArrayList<>(SoundHandler.TriggerSongMap.get("home").keySet()));
                dynamicPriorities.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[0]));
                dynamicFade.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[1]));
                dynamicDelay.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[4]));
                triggerPersistence.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[3]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("home")[33])) timeSwitch.add("home");
            } else if (triggerPersistence.get("home") != null && triggerPersistence.get("home") > 0) {
                crashHelper = "home";
                events.add("home");
                dynamicSongs.put("home", new ArrayList<>(SoundHandler.TriggerSongMap.get("home").keySet()));
                dynamicPriorities.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[0]));
                dynamicFade.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[1]));
                dynamicDelay.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[4]));
                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("home")[33])) timeSwitch.add("home");
            }
            if (SoundHandler.TriggerSongMap.get("dimension") != null) {
                crashHelper = "dimension";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("dimension").entrySet()) {
                    String dimensionSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(dimensionSong.replaceAll("@", "").replaceAll("#", "")).get("dimension")[10];
                    if (Integer.parseInt(SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[9]) == player.dimension) {
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
            }
            if (mc.isSingleplayer() && SoundHandler.TriggerSongMap.get("structure") != null) {
                crashHelper = "structure";
                WorldServer nworld = Objects.requireNonNull(mc.getIntegratedServer()).getWorld(player.dimension);
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("structure").entrySet()) {
                    String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(structureSong.replaceAll("@", "").replaceAll("#", "")).get("structure")[10];
                    if (nworld.getChunkProvider().isInsideStructure(world, SoundHandler.TriggerInfoMap.get("structure-" + identifier)[9], player.getPosition())) {
                        if (!events.contains("structure-" + identifier)) {
                            events.add("structure-" + identifier);
                            dynamicSongs.put("structure-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("structure"), identifier));
                            dynamicPriorities.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[0]));
                            dynamicFade.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[1]));
                            dynamicDelay.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[4]));
                            triggerPersistence.put("structure-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[33])) timeSwitch.add("structure-" + identifier);
                        }
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
            } else if (!configRegistry.clientSideOnly && SoundHandler.TriggerSongMap.get("structure") != null) {
                crashHelper = "structure";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("structure").entrySet()) {
                    String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(structureSong.replaceAll("@", "").replaceAll("#", "")).get("structure")[10];
                    RegistryHandler.network.sendToServer(new packet.packetMessage("structure-" + identifier, SoundHandler.TriggerInfoMap.get("structure-" + identifier)[9], player.getPosition(), player.dimension, player.getUniqueID()));
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
                    } else {
                        fromServer.curStruct = null;
                    }
                }
            }
            if (SoundHandler.TriggerSongMap.get("mob") != null && !configRegistry.clientSideOnly) {
                crashHelper = "mob";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("mob").entrySet()) {
                    String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(structureSong.replaceAll("@", "").replaceAll("#", "")).get("mob")[10];
                    triggerPersistence.putIfAbsent("mob-" + identifier, 0);
                    RegistryHandler.network.sendToServer(new packetSendMobInfo.packetSendMobInfoMessage("mob-" + identifier, player.getUniqueID(),
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
                for (int i : fromServer.mobVictory.keySet()) {
                    victory.put(i, fromServer.mobVictory.get(i));
                }
            }
            if (SoundHandler.TriggerSongMap.get("zones") != null) {
                crashHelper = "zones";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("zones").entrySet()) {
                    String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(structureSong.replaceAll("@", "").replaceAll("#", "")).get("zones")[10];
                    String[] broken = stringBreaker(SoundHandler.TriggerInfoMap.get("zones-" + identifier)[7], ",");
                    BlockPos bp = player.getPosition();
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
                    for (PotionEffect p : player.getActivePotionEffects()) {
                        effectList.add(p.getEffectName());
                        if (p.getEffectName().contains(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[9])) {
                            if (!events.contains("effect-" + identifier)) {
                                events.add("effect-" + identifier);
                                dynamicSongs.put("effect-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("effect"), identifier));
                                dynamicPriorities.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[0]));
                                dynamicFade.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[1]));
                                dynamicDelay.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[4]));
                                triggerPersistence.put("effect-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("effect-" + identifier)[3]));
                                if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("effect--" + identifier)[33])) timeSwitch.add("effect--" + identifier);
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
                    int id = Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[17]);
                    if (victory.get(id)!=null && victory.get(id)) {
                        if (!events.contains("victory-" + identifier)) {
                            events.add("victory-" + identifier);
                            dynamicSongs.put("victory-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("victory"), identifier));
                            dynamicPriorities.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[0]));
                            dynamicFade.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[1]));
                            dynamicDelay.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[4]));
                            triggerPersistence.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[3]));
                            victory.put(Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[17]), false);
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[33])) timeSwitch.add("victory-" + identifier);
                        }
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
            if (!mc.inGameHasFocus && SoundHandler.TriggerSongMap.get("gui") != null) {
                crashHelper = "gui";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("gui").entrySet()) {
                    String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(structureSong.replaceAll("@", "").replaceAll("#", "")).get("gui")[10];
                    if (eventsClient.GUIName.contains(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[9])) {
                        if (!events.contains("gui-" + identifier)) {
                            events.add("gui-" + identifier);
                            dynamicSongs.put("gui-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("gui"), identifier));
                            dynamicPriorities.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[0]));
                            dynamicFade.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[1]));
                            dynamicDelay.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[4]));
                            triggerPersistence.put("gui-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("gui-" + identifier)[33])) timeSwitch.add("gui-" + identifier);
                        }
                    } else if (SoundHandler.TriggerInfoMap.get("gui-" + identifier)[9].matches("CREDITS") && mc.currentScreen instanceof GuiWinGame) {
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
                musicVolSave = mc.gameSettings.getSoundLevel(SoundCategory.MUSIC);
                masterVolSave = mc.gameSettings.getSoundLevel(SoundCategory.MASTER);
            }
            if (SoundHandler.TriggerSongMap.get("difficulty") != null) {
                crashHelper = "difficulty";
                for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("difficulty").entrySet()) {
                    String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                    String identifier = configToml.triggerholder.get(structureSong.replaceAll("@", "").replaceAll("#", "")).get("difficulty")[10];
                    int diffID = Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[2]);
                    if (diffID == 4 && mc.world.getWorldInfo().isHardcoreModeEnabled()) {
                        if (!events.contains("difficulty-" + identifier)) {
                            events.add("difficulty-" + identifier);
                            dynamicSongs.put("difficulty-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("difficulty"), identifier));
                            dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                            dynamicFade.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                            dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                            triggerPersistence.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[33])) timeSwitch.add("difficulty-" + identifier);
                        }
                    } else if (diffID == 3 && mc.world.getDifficulty() == EnumDifficulty.HARD) {
                        if (!events.contains("difficulty-" + identifier)) {
                            events.add("difficulty-" + identifier);
                            dynamicSongs.put("difficulty-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("difficulty"), identifier));
                            dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                            dynamicFade.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                            dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                            triggerPersistence.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[33])) timeSwitch.add("difficulty-" + identifier);
                        }
                    } else if (diffID == 2 && mc.world.getDifficulty() == EnumDifficulty.NORMAL) {
                        if (!events.contains("difficulty-" + identifier)) {
                            events.add("difficulty-" + identifier);
                            dynamicSongs.put("difficulty-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("difficulty"), identifier));
                            dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                            dynamicFade.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                            dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                            triggerPersistence.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[33])) timeSwitch.add("difficulty-" + identifier);
                        }
                    } else if (diffID == 1 && mc.world.getDifficulty() == EnumDifficulty.EASY) {
                        if (!events.contains("difficulty-" + identifier)) {
                            events.add("difficulty-" + identifier);
                            dynamicSongs.put("difficulty-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("difficulty"), identifier));
                            dynamicPriorities.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[0]));
                            dynamicFade.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[1]));
                            dynamicDelay.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[4]));
                            triggerPersistence.put("difficulty-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[33])) timeSwitch.add("difficulty-" + identifier);
                        }
                    } else if (diffID == 0 && mc.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
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
            try {
                List<String> stages = gamestages();
                if (!stages.isEmpty()) {
                    events.addAll(stages);
                }
            } catch (NoSuchMethodError ignored) {
            }
            try {
                boolean bloodmoon = bloodmoon();
                if (bloodmoon) {
                    events.add("bloodmoon");
                }
            } catch (NoSuchMethodError ignored) {
            }
            try {
                boolean nyxbloodmoon = nyxbloodmoon();
                if (nyxbloodmoon) {
                    events.add("bloodmoon");
                }
            } catch (NoSuchMethodError ignored) {
            }
            try {
                boolean nyxharvestmoon = nyxharvestmoon();
                if (nyxharvestmoon) {
                    events.add("harvestmoon");
                }
            } catch (NoSuchMethodError ignored) {
            }
            try {
                boolean nyxfallingstars = nyxfallingstars();
                if (nyxfallingstars) {
                    events.add("fallingstars");
                }
            } catch (NoSuchMethodError ignored) {
            }
            try {
                if (dynamicrain(events) != null) {
                    String rainIntensity = dynamicrain(events);
                    if(!events.contains(rainIntensity)) events.add(rainIntensity);
                }
            } catch (NoSuchMethodError ignored) {
            }
            try {
                List<String> tornado = weatherTornado();
                if (!tornado.isEmpty()) {
                    events.addAll(tornado);
                }
                if (weatherHurricane()) {
                    events.add("hurricane");
                }
                if (weatherSandstorm()) {
                    events.add("sandstorm");
                }
            } catch (NoSuchMethodError ignored) {
            }
            try {
                List<String> seasons = seasons();
                if (!seasons.isEmpty()) {
                    events.addAll(seasons);
                }
            } catch (NoSuchMethodError ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("There was a problem with your "+crashHelper+" trigger! See the log for the full stack trace");
        }
        playableList = events;
        RegistryHandler.network.sendToServer(new packetAllTriggers.packetAllTriggersMessage(allTriggersAsSingleString()));
        return events;
    }

    @SuppressWarnings("rawtypes")
    @Optional.Method(modid = "gamestages")
    private static List<String> gamestages() {
        List<String> events = new ArrayList<>();
        if(SoundHandler.TriggerSongMap.get("gamestage")!=null) {
            for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("gamestage").entrySet()) {
                String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                String identifier = configToml.triggerholder.get(structureSong.replaceAll("@","")).get("gamestage")[10];
                if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[9])) {
                    if (GameStageHelper.clientHasStage(player, SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[19])) {
                        if(!events.contains("gamestage-" + identifier)) {
                            events.add("gamestage-" + identifier);
                            dynamicSongs.put("gamestage-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("gamestage"), identifier));
                            dynamicPriorities.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[0]));
                            dynamicFade.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[1]));
                            dynamicDelay.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[4]));
                            triggerPersistence.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[33])) timeSwitch.add("gamestage-" + identifier);
                        }
                    } else if (triggerPersistence.get("gamestage-" + identifier) != null && triggerPersistence.get("gamestage-" + identifier) > 0) {
                        if(!events.contains("gamestage-" + identifier)) {
                            events.add("gamestage-" + identifier);
                            dynamicSongs.put("gamestage-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("gamestage"), identifier));
                            dynamicPriorities.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[0]));
                            dynamicFade.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[1]));
                            dynamicDelay.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[33])) timeSwitch.add("gamestage-" + identifier);
                        }
                    }
                } else {
                    if (!GameStageHelper.clientHasStage(player, SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[19])) {
                        if(!events.contains("gamestage-" + identifier)) {
                            events.add("gamestage-" + identifier);
                            dynamicSongs.put("gamestage-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("gamestage"), identifier));
                            dynamicPriorities.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[0]));
                            dynamicFade.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[1]));
                            dynamicDelay.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[4]));
                            triggerPersistence.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[33])) timeSwitch.add("gamestage-" + identifier);
                        }
                    } else if (triggerPersistence.get("gamestage-" + identifier) != null && triggerPersistence.get("gamestage-" + identifier) > 0) {
                        if(!events.contains("gamestage-" + identifier)) {
                            events.add("gamestage-" + identifier);
                            dynamicSongs.put("gamestage-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("gamestage"), identifier));
                            dynamicPriorities.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[0]));
                            dynamicFade.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[1]));
                            dynamicDelay.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[33])) timeSwitch.add("gamestage-" + identifier);
                        }
                    }
                }
            }
        }
        return events;
    }

    @Optional.Method(modid = "bloodmoon")
    private static boolean bloodmoon() {
        if (Bloodmoon.proxy.isBloodmoon() && SoundHandler.TriggerSongMap.get("bloodmoon")!=null) {
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
        return false;
    }

    @Optional.Method(modid = "nyx")
    private static boolean nyxbloodmoon() {
        if (NyxWorld.get(player.getEntityWorld()).currentEvent instanceof BloodMoon && SoundHandler.TriggerSongMap.get("bloodmoon")!=null) {
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
        return false;
    }

    @Optional.Method(modid = "nyx")
    private static boolean nyxharvestmoon() {
        if (NyxWorld.get(player.getEntityWorld()).currentEvent instanceof HarvestMoon && SoundHandler.TriggerSongMap.get("harvestmoon")!=null) {
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
        return false;
    }

    @Optional.Method(modid = "nyx")
    private static boolean nyxfallingstars() {
        if (NyxWorld.get(player.getEntityWorld()).currentEvent instanceof StarShower && SoundHandler.TriggerSongMap.get("fallingstars")!=null) {
            dynamicSongs.put("fallingstars", new ArrayList<>(SoundHandler.TriggerSongMap.get("fallingstars").keySet()));
            dynamicPriorities.put("fallingstars", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fallingstars")[0]));
            dynamicFade.put("fallingstars", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fallingstars")[1]));
            dynamicDelay.put("fallingstars", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fallingstars")[4]));
            triggerPersistence.put("fallingstars", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fallingstars")[3]));
            return true;
        }
        else if (triggerPersistence.get("fallingstars")!=null && triggerPersistence.get("fallingstars") > 0) {
            dynamicSongs.put("fallingstars", new ArrayList<>(SoundHandler.TriggerSongMap.get("fallingstars").keySet()));
            dynamicPriorities.put("fallingstars", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fallingstars")[0]));
            dynamicFade.put("fallingstars", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fallingstars")[1]));
            dynamicDelay.put("fallingstars", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fallingstars")[4]));
            return true;
        }
        return false;
    }

    @Optional.Method(modid = "dsurround")
    private static String dynamicrain(List<String> events) {
        if(SoundHandler.TriggerSongMap.get("rainintensity")!=null) {
            for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("rainintensity").entrySet()) {
                String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                String identifier = configToml.triggerholder.get(structureSong.replaceAll("@","")).get("rainintensity")[10];
                if (Weather.getIntensityLevel() > Float.parseFloat(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[2]) / 100F) {
                    if(!events.contains("rainintensity-" + identifier)) {
                        dynamicSongs.put("rainintensity-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("rainintensity"), identifier));
                        dynamicPriorities.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[0]));
                        dynamicFade.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[1]));
                        dynamicDelay.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[4]));
                        triggerPersistence.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity" + identifier)[3]));
                        if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[33])) timeSwitch.add("rainintensity-" + identifier);
                        return "rainintensity-" + identifier;
                    }
                } else if (triggerPersistence.get("rainintensity-" + identifier) != null && triggerPersistence.get("rainintensity-" + identifier) > 0) {
                    if(!events.contains("rainintensity-" + identifier)) {
                        dynamicSongs.put("rainintensity-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("rainintensity"), identifier));
                        dynamicPriorities.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[0]));
                        dynamicFade.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[1]));
                        dynamicDelay.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[4]));
                        if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[33])) timeSwitch.add("rainintensity-" + identifier);
                        return "rainintensity-" + identifier;
                    }
                }
            }
        }
        return null;
    }

    @Optional.Method(modid = "weather2")
    private static List<String> weatherTornado() {
        List<String> tempList = new ArrayList<>();
        if(SoundHandler.TriggerSongMap.get("tornado")!=null) {
            for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("tornado").entrySet()) {
                String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                String identifier = configToml.triggerholder.get(structureSong.replaceAll("@","")).get("tornado")[10];
                if (WeatherDataHelper.getWeatherManagerForClient() != null && WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()), Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[11])) != null) {
                    StormObject storm = WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()), Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[11]));
                    if (storm.levelCurIntensityStage >= Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[2])) {
                        if (!tempList.contains("tornado-" + identifier)) {
                            tempList.add("tornado-" + identifier);
                            dynamicSongs.put("tornado-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("tornado"), identifier));
                            dynamicPriorities.put("tornado-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[0]));
                            dynamicFade.put("tornado-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[1]));
                            dynamicDelay.put("tornado-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[4]));
                            triggerPersistence.put("tornado-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[33])) timeSwitch.add("tornado-" + identifier);
                        }
                    } else if (triggerPersistence.get("tornado-" + identifier) != null && triggerPersistence.get("tornado-" + identifier) > 0) {
                        if (!tempList.contains("tornado-" + identifier)) {
                            tempList.add("tornado-" + identifier);
                            dynamicSongs.put("rainintensity-" + identifier, buildSongsFromIdentifier(SoundHandler.TriggerSongMap.get("tornado"), identifier));
                            dynamicPriorities.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[0]));
                            dynamicFade.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[1]));
                            dynamicDelay.put("tornado-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[33])) timeSwitch.add("tornado-" + identifier);
                        }
                    }
                }
            }
        }
        return tempList;
    }

    @Optional.Method(modid = "weather2")
    private static boolean weatherHurricane() {
        if (WeatherDataHelper.getWeatherManagerForClient() != null && WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()), Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[11])) != null) {
            StormObject storm = WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()), Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[11]));
            if (storm.isHurricane() && SoundHandler.TriggerSongMap.get("hurricane")!=null) {
                dynamicSongs.put("hurricane", new ArrayList<>(SoundHandler.TriggerSongMap.get("hurricane").keySet()));
                dynamicPriorities.put("hurricane", Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[0]));
                dynamicFade.put("hurricane", Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[1]));
                dynamicDelay.put("hurricane", Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[4]));
                triggerPersistence.put("hurricane", Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[3]));
                return true;
            } else if (triggerPersistence.get("hurricane")!=null && triggerPersistence.get("hurricane") > 0) {
                dynamicSongs.put("hurricane", new ArrayList<>(SoundHandler.TriggerSongMap.get("hurricane").keySet()));
                dynamicPriorities.put("hurricane", Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[0]));
                dynamicFade.put("hurricane", Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[1]));
                dynamicDelay.put("hurricane", Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[4]));
                return true;
            }
        }
        return false;
    }

    @Optional.Method(modid = "weather2")
    private static boolean weatherSandstorm() {
        if (WeatherDataHelper.getWeatherManagerForClient() != null && WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()), Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[11])) != null) {
            StormObject storm = WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()), Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[11]));
            if (storm.isHurricane() && SoundHandler.TriggerSongMap.get("sandstorm")!=null) {
                dynamicSongs.put("sandstorm", new ArrayList<>(SoundHandler.TriggerSongMap.get("sandstorm").keySet()));
                dynamicPriorities.put("sandstorm", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[0]));
                dynamicFade.put("sandstorm", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[1]));
                dynamicDelay.put("sandstorm", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[4]));
                triggerPersistence.put("sandstorm", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[3]));
                return true;
            } else if (triggerPersistence.get("sandstorm")!=null && triggerPersistence.get("sandstorm") > 0) {
                dynamicSongs.put("sandstorm", new ArrayList<>(SoundHandler.TriggerSongMap.get("sandstorm").keySet()));
                dynamicPriorities.put("sandstorm", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[0]));
                dynamicFade.put("sandstorm", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[1]));
                dynamicDelay.put("sandstorm", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[4]));
                return true;
            }
        }
        return false;
    }

    @Optional.Method(modid = "sereneseasons")
    private static List<String> seasons() {
        List<String> tempList = new ArrayList<>();
        if(SoundHandler.TriggerSongMap.get("season")!=null) {
            for (Map.Entry<String, String> stringListEntry : SoundHandler.TriggerSongMap.get("season").entrySet()) {
                String structureSong = ((Map.Entry) stringListEntry).getKey().toString();
                String identifier = configToml.triggerholder.get(structureSong.replaceAll("@","")).get("season")[10];
                int seasonID = Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[8]);
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

    public static BlockPos roundedPos(EntityPlayer p) {
        return new BlockPos((Math.round(p.posX * 2) / 2.0), (Math.round(p.posY * 2) / 2.0), (Math.round(p.posZ * 2) / 2.0));
    }


    public static double averageLight(BlockPos p, boolean b) {
        return b ? world.getLight(p, true) : world.getLightFor(EnumSkyBlock.BLOCK, p);
    }

    public static boolean checkBiome(Biome b, String name, String category, String rainType, float temperature, boolean cold, float rainfall, boolean togglerainfall) {
        if(Objects.requireNonNull(b.getRegistryName()).toString().contains(name) || name.matches("minecraft")) {
            if(b.getTempCategory().toString().contains(category) || category.matches("nope")) {
                boolean pass = false;
                if(rainfall==-111f) pass = true;
                else if(b.getRainfall()>rainfall && togglerainfall) pass = true;
                else if(b.getRainfall()<rainfall && !togglerainfall) pass = true;
                if(pass) {
                    if (rainType.matches("nope")) {
                        float bt = b.getDefaultTemperature();
                        if (temperature == -111) return true;
                        else if (bt >= temperature && !cold) return true;
                        else return bt <= temperature && cold;
                    } else if (b.canRain() && rainType.matches("rain")) {
                        float bt = b.getDefaultTemperature();
                        if (temperature == -111) return true;
                        else if (bt >= temperature && !cold) return true;
                        else return bt <= temperature && cold;
                    } else if (b.isSnowyBiome() && rainType.matches("snow")) {
                        float bt = b.getDefaultTemperature();
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
}