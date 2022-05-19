package mods.thecomputerizer.musictriggers.client;

import CoroUtil.util.Vec3;
import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.lunarevents.BloodMoon;
import de.ellpeck.nyx.lunarevents.HarvestMoon;
import de.ellpeck.nyx.lunarevents.StarShower;
import lumien.bloodmoon.Bloodmoon;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.config.ConfigToml;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.musictriggers.util.packets.PacketMenuSongs;
import mods.thecomputerizer.musictriggers.util.packets.PacketSendTriggers;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Loader;
import org.orecruncher.dsurround.client.weather.Weather;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;
import weather2.api.WeatherDataHelper;
import weather2.weathersystem.storm.StormObject;

import java.util.*;

import static mods.thecomputerizer.musictriggers.MusicTriggers.stringBreaker;

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
    public static boolean waterBool = false;
    public static int waterStart = 0;

    public static HashMap<String, List<String>> dynamicSongs = new HashMap<>();
    public static HashMap<String, Integer> dynamicPriorities = new HashMap<>();
    public static HashMap<String, Integer> dynamicFadeIn = new HashMap<>();
    public static HashMap<String, Integer> dynamicFadeOut = new HashMap<>();
    public static HashMap<String, Integer> dynamicDelay = new HashMap<>();

    public static List<String> playableList = new ArrayList<>();
    public static List<String> savePlayable = new ArrayList<>();
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
    public static String crashHelper;

    public static List<String> playThese() {
        if (!MusicPlayer.fadingOut) titleCardEvents = new ArrayList<>();
        mc = Minecraft.getMinecraft();
        player = mc.player;
        if (player != null) world = player.getEntityWorld();
        if(SoundHandler.TriggerIdentifierMap.isEmpty()) return null;

        if(player == null) {
            if (SoundHandler.TriggerIdentifierMap.get("menu") != null && mc.currentScreen!=null)
                return SoundHandler.TriggerIdentifierMap.get("menu").get("_");
        } else {
            String builtMenuSongs = allMenuSongsAsSingleString();
            if (builtMenuSongs != null)
                RegistryHandler.network.sendToServer(new PacketMenuSongs.packetMenuSongsMessage(builtMenuSongs));
            List<String> res = comboChecker(priorityHandler(playableEvents()));
            playableList = savePlayable;
            for (String event : timeSwitch) {
                if (!titleCardEvents.contains(event) && triggerPersistence.get(event) > 0)
                    triggerPersistence.put(event, 0);
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
            if (SoundHandler.TriggerIdentifierMap.get("generic") != null) {
                playableList.add("generic");
                titleCardEvents.add("generic");
                curDelay = Integer.parseInt(SoundHandler.TriggerInfoMap.get("generic")[4]);
                if (curDelay == 0) curDelay = universalDelay;
                curFadeIn = Integer.parseInt(SoundHandler.TriggerInfoMap.get("generic")[1]);
                if (curFadeIn == 0) curFadeIn = universalFadeIn;
                curFadeOut = Integer.parseInt(SoundHandler.TriggerInfoMap.get("generic")[35]);
                if (curFadeOut == 0) curFadeOut = universalFadeOut;
                return SoundHandler.TriggerIdentifierMap.get("generic").get("_");
            }
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
                        if (new HashSet<>(playableList).containsAll(SoundHandler.songCombos.get(s.replaceAll("@", ""))) && SoundHandler.TriggerInfoMap.keySet().containsAll(SoundHandler.instantiatedCombos.get(SoundHandler.songCombos.get(s.replaceAll("@", ""))))) {
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
        int highest = Integer.MIN_VALUE;
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
        MusicTriggers.logger.debug("debugging curfade "+curFadeOut);
        if (dynamicDelay != null && !dynamicDelay.isEmpty()) {
            if (dynamicDelay.get(trueHighest) != null) curDelay = dynamicDelay.get(trueHighest);
            else curDelay = 0;
            if(curDelay==0) curDelay = universalDelay;
        }
        return trueHighest;
    }

    @SuppressWarnings("rawtypes")
    public static List<String> playableEvents() {
        crashHelper = "";
        StringBuilder packetBuilder = new StringBuilder();
        List<String> events = new ArrayList<>();
        try {
            double time = (double) world.getWorldTime() / 24000.0;
            if (time > 1) {
                time = time - (long) time;
            }
            if (SoundHandler.TriggerIdentifierMap.get("time") != null) {
                crashHelper = "time";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("time").keySet()) {
                    crashHelper = "time-" + identifier;
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
                    crashHelper = "light-" + identifier;
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
                    crashHelper = "height-" + identifier;
                    boolean pass;
                    if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("height-" + identifier)[28])) {
                        pass = player.posY < Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[2]) && !world.canSeeSky(roundedPos(player));
                    }
                    else
                        pass = player.posY > Integer.parseInt(SoundHandler.TriggerInfoMap.get("height-" + identifier)[2]);
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
            if (SoundHandler.TriggerIdentifierMap.get("elytra") != null && player.getTicksElytraFlying() > Integer.parseInt(SoundHandler.TriggerInfoMap.get("elytra")[8])) {
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
            if (player.fishEntity != null && player.fishEntity.isOverWater()) {
                fishBool = true;
            } else {
                fishingStart = 0;
            }
            if (SoundHandler.TriggerIdentifierMap.get("fishing") != null && fishingStart > Integer.parseInt(SoundHandler.TriggerInfoMap.get("fishing")[8])) {
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
            if (SoundHandler.TriggerIdentifierMap.get("snowing") != null && world.isRaining() && world.getBiomeProvider().getTemperatureAtHeight(world.getBiome(roundedPos(player)).getTemperature(roundedPos(player)), world.getPrecipitationHeight(roundedPos(player)).getY()) < 0.15f) {
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
            if (SoundHandler.TriggerIdentifierMap.get("lowhp") != null && player.getHealth() < player.getMaxHealth() * (Integer.parseInt(SoundHandler.TriggerInfoMap.get("lowhp")[2]) / 100F)) {
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
            if (SoundHandler.TriggerIdentifierMap.get("dead") != null && (player.getHealth()<=0f || player.isDead)) {
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
            if (SoundHandler.TriggerIdentifierMap.get("riding") != null) {
                crashHelper = "riding";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("riding").keySet()) {
                    crashHelper = "riding-" + identifier;
                    if (checkRiding(SoundHandler.TriggerInfoMap.get("riding-" + identifier)[9])) {
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
            if (world.getBlockState(roundedPos(player)).getMaterial() == Material.WATER && world.getBlockState(roundedPos(player).up()).getMaterial() == Material.WATER) {
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
            for (EntityLiving ent : world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(player.posX - 16, player.posY - 8, player.posZ - 16, player.posX + 16, player.posY + 8, player.posZ + 16))) {
                if (ent instanceof EntityTameable && ent.serializeNBT().getString("Owner").matches(player.getName()) && SoundHandler.TriggerIdentifierMap.get("pet") != null) {
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
            if (SoundHandler.TriggerIdentifierMap.get("drowning") != null && player.getAir() < Integer.parseInt(SoundHandler.TriggerInfoMap.get("drowning")[2])) {
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
            if (SoundHandler.TriggerIdentifierMap.get("pvp") != null && setPVP) {
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
            if (triggerPersistence.get("pvp") != null && EventsClient.PVPTracker != null && triggerPersistence.get("victory_timeout") <= 0) {
                EventsClient.PVPTracker = null;
            }
            if (EventsClient.PVPTracker != null && EventsClient.PVPTracker.isDead) {
                victory.put(pvpVictoryID, true);
                EventsClient.PVPTracker = null;
            }
            if(SoundHandler.TriggerIdentifierMap.get("home") != null) {
                crashHelper = "home";
                packetBuilder.append(Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[11]));
                if (FromServer.home){
                    events.add("home");
                    dynamicSongs.put("home", SoundHandler.TriggerIdentifierMap.get("home").get("_"));
                    dynamicPriorities.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[0]));
                    dynamicFadeIn.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[1]));
                    dynamicFadeOut.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[35]));
                    dynamicDelay.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[4]));
                    triggerPersistence.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[3]));
                    if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("home")[33])) timeSwitch.add("home");
                } else if (triggerPersistence.get("home") != null && triggerPersistence.get("home") > 0) {
                    crashHelper = "home";
                    events.add("home");
                    dynamicSongs.put("home", SoundHandler.TriggerIdentifierMap.get("home").get("_"));
                    dynamicPriorities.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[0]));
                    dynamicFadeIn.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[1]));
                    dynamicFadeOut.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[35]));
                    dynamicDelay.put("home", Integer.parseInt(SoundHandler.TriggerInfoMap.get("home")[4]));
                    if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("home")[33])) timeSwitch.add("home");
                }
            }
            packetBuilder.append("&#");
            if (SoundHandler.TriggerIdentifierMap.get("dimension") != null) {
                crashHelper = "dimension";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("dimension").keySet()) {
                    crashHelper = "dimension-" + identifier;
                    if (checkDimensionList(player.dimension,SoundHandler.TriggerInfoMap.get("dimension-" + identifier)[9])) {
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
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("biome").keySet()) {
                    crashHelper = "biome-" + identifier;
                    boolean pass = checkBiome(world.getBiome(roundedPos(player)), SoundHandler.TriggerInfoMap.get("biome-" + identifier)[9],
                            SoundHandler.TriggerInfoMap.get("biome-" + identifier)[23], SoundHandler.TriggerInfoMap.get("biome-" + identifier)[24],
                            Float.parseFloat(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[25]), Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[26]),
                            Float.parseFloat(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[30]), Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("biome-" + identifier)[31]));
                    if (pass) {
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
            if (!ConfigRegistry.clientSideOnly && SoundHandler.TriggerIdentifierMap.get("structure") != null) {
                crashHelper = "structure";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("structure").keySet()) {
                    crashHelper = "structure-" + identifier;
                    packetBuilder.append("structure-").append(identifier).append("@").append(SoundHandler.TriggerInfoMap.get("structure-" + identifier)[9]).append("@").append(player.getPosition().toLong()).append("@").append(player.dimension).append("$");
                    FromServer.inStructure.putIfAbsent("structure-" + identifier, false);
                    if (FromServer.inStructure.get("structure-" + identifier)) {
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
                        FromServer.curStruct = SoundHandler.TriggerInfoMap.get("structure-" + identifier)[9];
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
                    } else {
                        FromServer.curStruct = null;
                    }
                }
                packetBuilder = new StringBuilder(packetBuilder.substring(0, packetBuilder.length() - 1));
            }
            packetBuilder.append("&#");
            if (SoundHandler.TriggerIdentifierMap.get("mob") != null && !ConfigRegistry.clientSideOnly) {
                crashHelper = "mob";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("mob").keySet()) {
                    crashHelper = "mob-" + identifier;
                    packetBuilder.append("mob-").append(identifier).append("@").append(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[9]).append("@")
                            .append(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[11]).append("@").append(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[12])
                            .append("@").append(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[13]).append("@").append(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[14])
                            .append("@").append(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[15]).append("@").append(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[16])
                            .append("@").append(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[17]).append("@").append(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[18])
                            .append("@").append(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[2]).append("@").append(Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[22]))
                            .append("@").append(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[27]).append("@").append(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[36]).append("$");
                    FromServer.mob.putIfAbsent("mob-" + identifier, false);
                    if (FromServer.mob.get("mob-" + identifier)) {
                        if (!events.contains("mob-" + identifier)) {
                            events.add("mob-" + identifier);
                            dynamicSongs.put("mob-" + identifier, SoundHandler.TriggerIdentifierMap.get("mob").get(identifier));
                            dynamicPriorities.put("mob-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[0]));
                            dynamicFadeIn.put("mob-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[1]));
                            dynamicFadeOut.put("mob-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[35]));
                            dynamicDelay.put("mob-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[4]));
                            triggerPersistence.put("mob-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("mob-" + identifier)[33])) timeSwitch.add("mob-" + identifier);
                        }
                    }  else if (triggerPersistence.get("mob-" + identifier) != null && triggerPersistence.get("mob-" + identifier) > 0) {
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
                packetBuilder = new StringBuilder(packetBuilder.substring(0, packetBuilder.length() - 1));
                for (int i : FromServer.mobVictory.keySet()) {
                    victory.put(i, FromServer.mobVictory.get(i));
                }
            }
            packetBuilder.append("&");
            if (SoundHandler.TriggerIdentifierMap.get("zones") != null) {
                crashHelper = "zones";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("zones").keySet()) {
                    crashHelper = "zones-" + identifier;
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
                    crashHelper = "effect-" + identifier;
                    effectList = new ArrayList<>();
                    for (PotionEffect p : player.getActivePotionEffects()) {
                        effectList.add(p.getEffectName());
                        if (checkResourceList(p.getEffectName(),SoundHandler.TriggerInfoMap.get("effect-" + identifier)[9],false)) {
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
                    crashHelper = "victory-" + identifier;
                    int id = Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[17]);
                    if (victory.get(id)!=null && victory.get(id)) {
                        if (!events.contains("victory-" + identifier)) {
                            events.add("victory-" + identifier);
                            dynamicSongs.put("victory-" + identifier, SoundHandler.TriggerIdentifierMap.get("victory").get(identifier));
                            dynamicPriorities.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[0]));
                            dynamicFadeIn.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[1]));
                            dynamicFadeOut.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[35]));
                            dynamicDelay.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[4]));
                            triggerPersistence.put("victory-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[3]));
                            victory.put(Integer.parseInt(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[17]), false);
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("victory-" + identifier)[33])) timeSwitch.add("victory-" + identifier);
                        }
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
            if (SoundHandler.TriggerIdentifierMap.get("gui") != null) {
                crashHelper = "gui";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("gui").keySet()) {
                    crashHelper = "gui-" + identifier;
                    if (mc.currentScreen!=null && checkResourceList(mc.currentScreen.getClass().getName(),SoundHandler.TriggerInfoMap.get("gui-" + identifier)[9],false)) {
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
                    } else if (SoundHandler.TriggerInfoMap.get("gui-" + identifier)[9].matches("CREDITS") && mc.currentScreen instanceof GuiWinGame) {
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
            }
            if (SoundHandler.TriggerIdentifierMap.get("difficulty") != null) {
                crashHelper = "difficulty";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("difficulty").keySet()) {
                    crashHelper = "difficulty-" + identifier;
                    int diffID = Integer.parseInt(SoundHandler.TriggerInfoMap.get("difficulty-" + identifier)[2]);
                    if (diffID == 4 && mc.world.getWorldInfo().isHardcoreModeEnabled()) {
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
                    } else if (diffID == 3 && mc.world.getDifficulty() == EnumDifficulty.HARD) {
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
                    } else if (diffID == 2 && mc.world.getDifficulty() == EnumDifficulty.NORMAL) {
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
                    } else if (diffID == 1 && mc.world.getDifficulty() == EnumDifficulty.EASY) {
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
                    } else if (diffID == 0 && mc.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
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
                    crashHelper = "advancement-" + identifier;
                    if (EventsClient.advancement && (EventsClient.lastAdvancement.contains(SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[5]) || SoundHandler.TriggerInfoMap.get("advancement-" + identifier)[5].matches("YouWillNeverGuessThis"))) {
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
                EventsClient.advancement = false;
            }
            if (SoundHandler.TriggerIdentifierMap.get("statistic") != null) {
                crashHelper = "statistic";
                for (String identifier : SoundHandler.TriggerIdentifierMap.get("statistic").keySet()) {
                    crashHelper = "statistic-" + identifier;
                    if (checkStat(SoundHandler.TriggerInfoMap.get("statistic-" + identifier)[9], Integer.parseInt(SoundHandler.TriggerInfoMap.get("statistic-" + identifier)[2]))) {
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
            }
            List<String> stages = gamestages();
            if (!stages.isEmpty()) events.addAll(stages);
            if (bloodmoon()) events.add("bloodmoon");
            if (nyxbloodmoon()) events.add("bloodmoon");
            if (nyxharvestmoon()) events.add("harvestmoon");
            if (nyxfallingstars()) events.add("fallingstars");
            String rainIntensity = dynamicrain(events);
            if (rainIntensity != null && !events.contains(rainIntensity)) events.add(rainIntensity);
            List<String> tornado = weatherTornado();
            if (!tornado.isEmpty()) events.addAll(tornado);
            if (weatherHurricane()) events.add("hurricane");
            if (weatherSandstorm()) events.add("sandstorm");
            List<String> seasons = seasons();
            if (!seasons.isEmpty()) events.addAll(seasons);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("There was a problem with your "+crashHelper+" trigger! The error was "+e.getMessage()+" and was caught on line "+e.getStackTrace()[0].getLineNumber()+" in the class "+e.getStackTrace()[0]);
        }
        playableList = events;
        savePlayable = events;
        if(!ConfigRegistry.clientSideOnly) {
            packetBuilder.insert(0, allTriggersAsSingleString() + "&#");
            RegistryHandler.network.sendToServer(new PacketSendTriggers.PacketSendTriggersMessage(packetBuilder.toString(), player.getUniqueID()));
        }
        return events;
    }

    @SuppressWarnings("rawtypes")
    private static List<String> gamestages() {
        List<String> events = new ArrayList<>();
        if(Loader.isModLoaded("gamestages") && SoundHandler.TriggerIdentifierMap.get("gamestage")!=null) {
            crashHelper = "gamestages";
            for (String identifier : SoundHandler.TriggerIdentifierMap.get("gamestage").keySet()) {
                if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[9])) {
                    if (GameStageHelper.clientHasStage(player, SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[19])) {
                        if(!events.contains("gamestage-" + identifier)) {
                            events.add("gamestage-" + identifier);
                            dynamicSongs.put("gamestage-" + identifier, SoundHandler.TriggerIdentifierMap.get("gamestage").get(identifier));
                            dynamicPriorities.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[0]));
                            dynamicFadeIn.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[1]));
                            dynamicFadeOut.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[35]));
                            dynamicDelay.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[4]));
                            triggerPersistence.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[33])) timeSwitch.add("gamestage-" + identifier);
                        }
                    } else if (triggerPersistence.get("gamestage-" + identifier) != null && triggerPersistence.get("gamestage-" + identifier) > 0) {
                        if(!events.contains("gamestage-" + identifier)) {
                            events.add("gamestage-" + identifier);
                            dynamicSongs.put("gamestage-" + identifier, SoundHandler.TriggerIdentifierMap.get("gamestage").get(identifier));
                            dynamicPriorities.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[0]));
                            dynamicFadeIn.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[1]));
                            dynamicFadeOut.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[35]));
                            dynamicDelay.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[33])) timeSwitch.add("gamestage-" + identifier);
                        }
                    }
                } else {
                    if (!GameStageHelper.clientHasStage(player, SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[19])) {
                        if(!events.contains("gamestage-" + identifier)) {
                            events.add("gamestage-" + identifier);
                            dynamicSongs.put("gamestage-" + identifier, SoundHandler.TriggerIdentifierMap.get("gamestage").get(identifier));
                            dynamicPriorities.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[0]));
                            dynamicFadeIn.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[1]));
                            dynamicFadeOut.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[35]));
                            dynamicDelay.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[4]));
                            triggerPersistence.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[33])) timeSwitch.add("gamestage-" + identifier);
                        }
                    } else if (triggerPersistence.get("gamestage-" + identifier) != null && triggerPersistence.get("gamestage-" + identifier) > 0) {
                        if(!events.contains("gamestage-" + identifier)) {
                            events.add("gamestage-" + identifier);
                            dynamicSongs.put("gamestage-" + identifier, SoundHandler.TriggerIdentifierMap.get("gamestage").get(identifier));
                            dynamicPriorities.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[0]));
                            dynamicFadeIn.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[1]));
                            dynamicFadeOut.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[35]));
                            dynamicDelay.put("gamestage-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("gamestage-" + identifier)[33])) timeSwitch.add("gamestage-" + identifier);
                        }
                    }
                }
            }
        }
        return events;
    }

    private static boolean bloodmoon() {
        if(Loader.isModLoaded("bloodmoon") && SoundHandler.TriggerIdentifierMap.get("bloodmoon") != null) {
            crashHelper = "bloodmoon";
            if (Bloodmoon.proxy.isBloodmoon() && SoundHandler.TriggerIdentifierMap.get("bloodmoon") != null) {
                dynamicSongs.put("bloodmoon", SoundHandler.TriggerIdentifierMap.get("bloodmoon").get("_"));
                dynamicPriorities.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[0]));
                dynamicFadeIn.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[1]));
                dynamicFadeOut.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[35]));
                dynamicDelay.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[4]));
                triggerPersistence.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[3]));
                if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("bloodmoon")[33])) timeSwitch.add("bloodmoon");
                return true;
            } else if (triggerPersistence.get("bloodmoon") != null && triggerPersistence.get("bloodmoon") > 0) {
                dynamicSongs.put("bloodmoon", SoundHandler.TriggerIdentifierMap.get("bloodmoon").get("_"));
                dynamicPriorities.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[0]));
                dynamicFadeIn.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[1]));
                dynamicFadeOut.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[35]));
                dynamicDelay.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[4]));
                if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("bloodmoon")[33])) timeSwitch.add("bloodmoon");
                return true;
            }
        }
        return false;
    }

    private static boolean nyxbloodmoon() {
        if(Loader.isModLoaded("nyx") && SoundHandler.TriggerIdentifierMap.get("bloodmoon") != null) {
            crashHelper = "bloodmoon";
            if (NyxWorld.get(player.getEntityWorld()).currentEvent instanceof BloodMoon) {
                dynamicSongs.put("bloodmoon", SoundHandler.TriggerIdentifierMap.get("bloodmoon").get("_"));
                dynamicPriorities.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[0]));
                dynamicFadeIn.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[1]));
                dynamicFadeOut.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[35]));
                dynamicDelay.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[4]));
                triggerPersistence.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[3]));
                if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("bloodmoon")[33])) timeSwitch.add("bloodmoon");
                return true;
            } else if (triggerPersistence.get("bloodmoon") != null && triggerPersistence.get("bloodmoon") > 0) {
                dynamicSongs.put("bloodmoon", SoundHandler.TriggerIdentifierMap.get("bloodmoon").get("_"));
                dynamicPriorities.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[0]));
                dynamicFadeIn.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[1]));
                dynamicFadeOut.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[35]));
                dynamicDelay.put("bloodmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("bloodmoon")[4]));
                if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("bloodmoon")[33])) timeSwitch.add("bloodmoon");
                return true;
            }
        }
        return false;
    }

    private static boolean nyxharvestmoon() {
        if(Loader.isModLoaded("nyx") && SoundHandler.TriggerIdentifierMap.get("harvestmoon") != null) {
            crashHelper = "harvestmoon";
            if (NyxWorld.get(player.getEntityWorld()).currentEvent instanceof HarvestMoon) {
                dynamicSongs.put("harvestmoon", SoundHandler.TriggerIdentifierMap.get("harvestmoon").get("_"));
                dynamicPriorities.put("harvestmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("harvestmoon")[0]));
                dynamicFadeIn.put("harvestmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("harvestmoon")[1]));
                dynamicFadeOut.put("harvestmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("harvestmoon")[35]));
                dynamicDelay.put("harvestmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("harvestmoon")[4]));
                triggerPersistence.put("harvestmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("harvestmoon")[3]));
                if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("harvestmoon")[33]))
                    timeSwitch.add("harvestmoon");
                return true;
            } else if (triggerPersistence.get("harvestmoon") != null && triggerPersistence.get("harvestmoon") > 0) {
                dynamicSongs.put("harvestmoon", SoundHandler.TriggerIdentifierMap.get("harvestmoon").get("_"));
                dynamicPriorities.put("harvestmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("harvestmoon")[0]));
                dynamicFadeIn.put("harvestmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("harvestmoon")[1]));
                dynamicFadeOut.put("harvestmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("harvestmoon")[35]));
                dynamicDelay.put("harvestmoon", Integer.parseInt(SoundHandler.TriggerInfoMap.get("harvestmoon")[4]));
                if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("harvestmoon")[33]))
                    timeSwitch.add("harvestmoon");
                return true;
            }
        }
        return false;
    }

    private static boolean nyxfallingstars() {
        if(Loader.isModLoaded("nyx") && SoundHandler.TriggerIdentifierMap.get("fallingstars") != null) {
            crashHelper = "fallingstars";
            if (NyxWorld.get(player.getEntityWorld()).currentEvent instanceof StarShower) {
                dynamicSongs.put("fallingstars", SoundHandler.TriggerIdentifierMap.get("fallingstars").get("_"));
                dynamicPriorities.put("fallingstars", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fallingstars")[0]));
                dynamicFadeIn.put("fallingstars", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fallingstars")[1]));
                dynamicFadeOut.put("fallingstars", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fallingstars")[35]));
                dynamicDelay.put("fallingstars", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fallingstars")[4]));
                triggerPersistence.put("fallingstars", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fallingstars")[3]));
                if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("fallingstars")[33]))
                    timeSwitch.add("fallingstars");
                return true;
            } else if (triggerPersistence.get("fallingstars") != null && triggerPersistence.get("fallingstars") > 0) {
                dynamicSongs.put("fallingstars", SoundHandler.TriggerIdentifierMap.get("fallingstars").get("_"));
                dynamicPriorities.put("fallingstars", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fallingstars")[0]));
                dynamicFadeIn.put("fallingstars", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fallingstars")[1]));
                dynamicFadeOut.put("fallingstars", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fallingstars")[35]));
                dynamicDelay.put("fallingstars", Integer.parseInt(SoundHandler.TriggerInfoMap.get("fallingstars")[4]));
                if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("fallingstars")[33]))
                    timeSwitch.add("fallingstars");
                return true;
            }
        }
        return false;
    }

    private static String dynamicrain(List<String> events) {
        if(Loader.isModLoaded("dsurround") && SoundHandler.TriggerIdentifierMap.get("rainintensity")!=null) {
            crashHelper = "rainintensity";
            for (String identifier : SoundHandler.TriggerIdentifierMap.get("rainintensity").keySet()) {
                crashHelper = "rainintensity-" + identifier;
                if (Weather.getIntensityLevel() > Float.parseFloat(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[2]) / 100F) {
                    if(!events.contains("rainintensity-" + identifier)) {
                        dynamicSongs.put("rainintensity-" + identifier, SoundHandler.TriggerIdentifierMap.get("rainintensity").get(identifier));
                        dynamicPriorities.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[0]));
                        dynamicFadeIn.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[1]));
                        dynamicFadeOut.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[35]));
                        dynamicDelay.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[4]));
                        triggerPersistence.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity" + identifier)[3]));
                        if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[33])) timeSwitch.add("rainintensity-" + identifier);
                        return "rainintensity-" + identifier;
                    }
                } else if (triggerPersistence.get("rainintensity-" + identifier) != null && triggerPersistence.get("rainintensity-" + identifier) > 0) {
                    if(!events.contains("rainintensity-" + identifier)) {
                        dynamicSongs.put("rainintensity-" + identifier, SoundHandler.TriggerIdentifierMap.get("rainintensity").get(identifier));
                        dynamicPriorities.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[0]));
                        dynamicFadeIn.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[1]));
                        dynamicFadeOut.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[35]));
                        dynamicDelay.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[4]));
                        if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("rainintensity-" + identifier)[33])) timeSwitch.add("rainintensity-" + identifier);
                        return "rainintensity-" + identifier;
                    }
                }
            }
        }
        return null;
    }

    private static List<String> weatherTornado() {
        List<String> tempList = new ArrayList<>();
        if(Loader.isModLoaded("weather2") && SoundHandler.TriggerIdentifierMap.get("tornado")!=null) {
            crashHelper = "tornado";
            for (String identifier : SoundHandler.TriggerIdentifierMap.get("tornado").keySet()) {
                crashHelper = "tornado-" + identifier;
                if (WeatherDataHelper.getWeatherManagerForClient() != null && WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()), Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[11])) != null) {
                    StormObject storm = WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()), Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[11]));
                    if (storm.levelCurIntensityStage >= Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[2])) {
                        if (!tempList.contains("tornado-" + identifier)) {
                            tempList.add("tornado-" + identifier);
                            dynamicSongs.put("tornado-" + identifier, SoundHandler.TriggerIdentifierMap.get("tornado").get(identifier));
                            dynamicPriorities.put("tornado-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[0]));
                            dynamicFadeIn.put("tornado-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[1]));
                            dynamicFadeOut.put("tornado-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[35]));
                            dynamicDelay.put("tornado-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[4]));
                            triggerPersistence.put("tornado-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[3]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[33])) timeSwitch.add("tornado-" + identifier);
                        }
                    } else if (triggerPersistence.get("tornado-" + identifier) != null && triggerPersistence.get("tornado-" + identifier) > 0) {
                        if (!tempList.contains("tornado-" + identifier)) {
                            tempList.add("tornado-" + identifier);
                            dynamicSongs.put("rainintensity-" + identifier, SoundHandler.TriggerIdentifierMap.get("tornado").get(identifier));
                            dynamicPriorities.put("rainintensity-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[0]));
                            dynamicFadeIn.put("tornado-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[1]));
                            dynamicFadeOut.put("tornado-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[35]));
                            dynamicDelay.put("tornado-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[4]));
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("tornado-" + identifier)[33])) timeSwitch.add("tornado-" + identifier);
                        }
                    }
                }
            }
        }
        return tempList;
    }

    private static boolean weatherHurricane() {
        if(Loader.isModLoaded("weather2") && SoundHandler.TriggerIdentifierMap.get("hurricane")!=null) {
            crashHelper = "hurricane";
            if (WeatherDataHelper.getWeatherManagerForClient() != null && WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()), Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[11])) != null) {
                StormObject storm = WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()), Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[11]));
                if (storm.isHurricane()) {
                    dynamicSongs.put("hurricane", SoundHandler.TriggerIdentifierMap.get("hurricane").get("_"));
                    dynamicPriorities.put("hurricane", Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[0]));
                    dynamicFadeIn.put("hurricane", Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[1]));
                    dynamicFadeOut.put("hurricane", Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[35]));
                    dynamicDelay.put("hurricane", Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[4]));
                    triggerPersistence.put("hurricane", Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[3]));
                    if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("hurricane")[33]))
                        timeSwitch.add("hurricane");
                    return true;
                } else if (triggerPersistence.get("hurricane") != null && triggerPersistence.get("hurricane") > 0) {
                    dynamicSongs.put("hurricane", SoundHandler.TriggerIdentifierMap.get("hurricane").get("_"));
                    dynamicPriorities.put("hurricane", Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[0]));
                    dynamicFadeIn.put("hurricane", Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[1]));
                    dynamicFadeOut.put("hurricane", Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[35]));
                    dynamicDelay.put("hurricane", Integer.parseInt(SoundHandler.TriggerInfoMap.get("hurricane")[4]));
                    if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("hurricane")[33]))
                        timeSwitch.add("hurricane");
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean weatherSandstorm() {
        if(Loader.isModLoaded("weather2") && SoundHandler.TriggerIdentifierMap.get("sandstorm")!=null) {
            crashHelper = "sandstorm";
            if (WeatherDataHelper.getWeatherManagerForClient() != null && WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()), Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[11])) != null) {
                StormObject storm = WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()), Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[11]));
                if (storm.isHurricane()) {
                    dynamicSongs.put("sandstorm", SoundHandler.TriggerIdentifierMap.get("sandstorm").get("_"));
                    dynamicPriorities.put("sandstorm", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[0]));
                    dynamicFadeIn.put("sandstorm", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[1]));
                    dynamicFadeOut.put("sandstorm", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[35]));
                    dynamicDelay.put("sandstorm", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[4]));
                    triggerPersistence.put("sandstorm", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[3]));
                    if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("sandstorm")[33]))
                        timeSwitch.add("sandstorm");
                    return true;
                } else if (triggerPersistence.get("sandstorm") != null && triggerPersistence.get("sandstorm") > 0) {
                    dynamicSongs.put("sandstorm", SoundHandler.TriggerIdentifierMap.get("sandstorm").get("_"));
                    dynamicPriorities.put("sandstorm", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[0]));
                    dynamicFadeIn.put("sandstorm", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[1]));
                    dynamicFadeOut.put("sandstorm", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[35]));
                    dynamicDelay.put("sandstorm", Integer.parseInt(SoundHandler.TriggerInfoMap.get("sandstorm")[4]));
                    if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("sandstorm")[33]))
                        timeSwitch.add("sandstorm");
                    return true;
                }
            }
        }
        return false;
    }

    private static List<String> seasons() {
        List<String> tempList = new ArrayList<>();
        if(Loader.isModLoaded("sereneseasons") && SoundHandler.TriggerIdentifierMap.get("season")!=null) {
            crashHelper = "season";
            for (String identifier : SoundHandler.TriggerIdentifierMap.get("season").keySet()) {
                crashHelper = "season-" + identifier;
                int seasonID = Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[2]);
                ISeasonState curSeason = SeasonHelper.getSeasonState(world);
                if (seasonID == 0 && curSeason.getSeason() == Season.SPRING) {
                    if (!tempList.contains("season-" + identifier)) {
                        tempList.add("season-" + identifier);
                        dynamicSongs.put("season-" + identifier, SoundHandler.TriggerIdentifierMap.get("season").get(identifier));
                        dynamicPriorities.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[0]));
                        dynamicFadeIn.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[1]));
                        dynamicFadeOut.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[35]));
                        dynamicDelay.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[4]));
                        triggerPersistence.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[3]));
                        if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("season-" + identifier)[33])) timeSwitch.add("season-" + identifier);
                    }
                } else if (seasonID == 1 && curSeason.getSeason() == Season.SUMMER) {
                    if (!tempList.contains("season-" + identifier)) {
                        tempList.add("season-" + identifier);
                        dynamicSongs.put("season-" + identifier, SoundHandler.TriggerIdentifierMap.get("season").get(identifier));
                        dynamicPriorities.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[0]));
                        dynamicFadeIn.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[1]));
                        dynamicFadeOut.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[35]));
                        dynamicDelay.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[4]));
                        triggerPersistence.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[3]));
                        if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("season-" + identifier)[33])) timeSwitch.add("season-" + identifier);
                    }
                } else if (seasonID == 2 && curSeason.getSeason() == Season.AUTUMN) {
                    if (!tempList.contains("season-" + identifier)) {
                        tempList.add("season-" + identifier);
                        dynamicSongs.put("season-" + identifier, SoundHandler.TriggerIdentifierMap.get("season").get(identifier));
                        dynamicPriorities.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[0]));
                        dynamicFadeIn.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[1]));
                        dynamicFadeOut.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[35]));
                        dynamicDelay.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[4]));
                        triggerPersistence.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[3]));
                        if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("season-" + identifier)[33])) timeSwitch.add("season-" + identifier);
                    }
                } else if (seasonID == 3 && curSeason.getSeason() == Season.WINTER) {
                    if (!tempList.contains("season-" + identifier)) {
                        tempList.add("season-" + identifier);
                        dynamicSongs.put("season-" + identifier, SoundHandler.TriggerIdentifierMap.get("season").get(identifier));
                        dynamicPriorities.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[0]));
                        dynamicFadeIn.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[1]));
                        dynamicFadeOut.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[35]));
                        dynamicDelay.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[4]));
                        triggerPersistence.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[3]));
                        if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get("season-" + identifier)[33])) timeSwitch.add("season-" + identifier);
                    }
                } else if (triggerPersistence.get("season-" + identifier) != null && triggerPersistence.get("season-" + identifier) > 0) {
                    if (!tempList.contains("season-" + identifier)) {
                        tempList.add("season-" + identifier);
                        dynamicSongs.put("season-" + identifier, SoundHandler.TriggerIdentifierMap.get("season").get(identifier));
                        dynamicPriorities.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[0]));
                        dynamicFadeIn.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[1]));
                        dynamicFadeOut.put("season-" + identifier, Integer.parseInt(SoundHandler.TriggerInfoMap.get("season-" + identifier)[35]));
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
        if(checkResourceList(Objects.requireNonNull(b.getRegistryName()).toString(),name, false) || name.matches("minecraft")) {
            if(checkResourceList(b.getTempCategory().toString(),category,false) || category.matches("nope")) {
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

    public static boolean checkResourceList(String type, String resourceList, boolean match) {
        for(String resource : stringBreaker(resourceList,";")) {
            if(match && type.matches(resource)) return true;
            else if(!match && type.contains(resource)) return true;
        }
        return false;
    }

    public static boolean checkDimensionList(int playerDim, String resourceList) {
        for(String resource : stringBreaker(resourceList,";")) if((playerDim+"").matches(resource)) return true;
        return checkResourceList(DimensionType.getById(playerDim).getName(),resourceList,false);
    }

    public static boolean checkRiding(String resource) {
        if(!player.isRiding() || player.getRidingEntity()==null) return false;
        else if(resource.matches("minecraft")) return true;
        else if(checkResourceList(Objects.requireNonNull(player.getRidingEntity()).getName(),resource,true)) return true;
        else if(EntityList.getKey(player.getRidingEntity())==null) return false;
        return checkResourceList(Objects.requireNonNull(EntityList.getKey(player.getRidingEntity())).toString(),resource,false);
    }

    public static boolean checkStat(String stat, int level) {
        if(mc.getConnection()!=null) {
            Objects.requireNonNull(mc.getConnection()).sendPacket(new CPacketClientStatus(CPacketClientStatus.State.REQUEST_STATS));
            for (StatBase s : StatList.ALL_STATS) {
                return checkResourceList(s.statId, stat, false) && mc.player.getStatFileWriter().readStat(s) > level;
            }
        }
        return false;
    }

    public static String allTriggersAsSingleString() {
        StringBuilder ret = new StringBuilder();
        for(String trigger : playableList) {
            ret.append(trigger).append(",");
        }
        if(ret.length()!=0) return ret.substring(0, ret.length()-1);
        return "";
    }

    public static String allMenuSongsAsSingleString() {
        StringBuilder ret = new StringBuilder();
        if (SoundHandler.TriggerIdentifierMap.get("menu") != null) {
            for (String song : SoundHandler.TriggerIdentifierMap.get("menu").get("_")) {
                ret.append(ConfigToml.songholder.get(song)).append(",");
            }
            if (ret.length() != 0) return player.getUniqueID()+","+ret.substring(0, ret.length() - 1);
        }
        return null;
    }
}