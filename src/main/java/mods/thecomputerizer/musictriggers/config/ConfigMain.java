package mods.thecomputerizer.musictriggers.config;

import com.moandjiezana.toml.Toml;
import mods.thecomputerizer.musictriggers.MusicTriggers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

@SuppressWarnings({"ResultOfMethodCallIgnored", "ExplicitArgumentCanBeLambda", "SuspiciousToArrayCall"})
public class ConfigMain {
    private final File file;
    private final String channel;
    private String CrashHelper;
    public int universalFadeIn = 0;
    public int universalFadeOut = 0;
    public String universalDelay = "0";
    public final Map<String, String> songholder = new HashMap<>();
    public final Map<String, Map<String, String[]>> triggerholder = new HashMap<>();
    public final Map<String, Map<String, String>> triggerMapper = new HashMap<>();
    public final Map<String, String[]> otherinfo = new HashMap<>();
    public final Map<String, Map<String, String[]>> otherlinkinginfo = new HashMap<>();
    public final Map<String, Map<String, String[]>> triggerlinking = new HashMap<>();
    public final Map<String, Map<Integer, String[]>> loopPoints = new HashMap<>();
    public final Map<String, Map<String, Map<Integer, String[]>>> linkingLoopPoints = new HashMap<>();

    public static final String[] triggers = new String[]
            {"menu","generic","difficulty","time","light","height","raining","storming","snowing","lowhp","dead",
                    "creative","spectator","riding","pet","underwater","elytra","fishing","drowning","home",
                    "dimension","biome", "structure","mob","victory","gui","effect","zones","pvp","advancement",
                    "statistic","command"};
    public static final String[] modtriggers = new String[]
            {"gamestage","season"};
    public static final String[] allmodtriggers = new String[]
            {"raid", "gamestage","bloodmoon","harvestmoon","fallingstars","bluemoon","rainintensity","tornado","hurricane","sandstorm","acidrain","blizzard","cloudy","lightrain","season"};

    public ConfigMain(File file, String channel) {
        if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch(Exception e) {
                e.printStackTrace();
            }
            writeInformationalHeader(file);
        }
        this.file = file;
        this.channel = channel;
    }

    public String getChannel() {
        return this.channel;
    }

    public void parse() {
        try {
            Toml toml = new Toml().read(this.file);
            int songCounter = 0;
            if (toml.contains("universal")) {
                Toml universal = toml.getTable("universal");
                if (universal.contains("fade_in"))
                    this.universalFadeIn = MusicTriggers.randomInt(universal.getString("fade_in"));
                if (universal.contains("fade_out"))
                    this.universalFadeOut = MusicTriggers.randomInt(universal.getString("fade_out"));
                if (universal.contains("delay"))
                    this.universalDelay = universal.getString("delay");
            }
            for (String s : songCollector(this.file)) {
                if (toml.containsTableArray(s)) {
                    try {
                        for (Toml song : toml.getTables(s)) {
                            this.triggerMapper.put("song" + songCounter, new HashMap<>());
                            int triggerMapCounter = 0;
                            if (song.containsTableArray("trigger")) {
                                for (Toml trigger : song.getTables("trigger")) {
                                    if (trigger.contains("name")) {
                                        String triggerID = "trigger-" + triggerMapCounter;
                                        this.triggerMapper.get("song" + songCounter).put(triggerID, trigger.getString("name"));
                                        if (Arrays.asList(triggers).contains(this.triggerMapper.get("song" + songCounter).get(triggerID)) || Arrays.asList(allmodtriggers).contains(this.triggerMapper.get("song" + songCounter).get(triggerID))) {
                                            if (!(Arrays.asList(triggers).contains(this.triggerMapper.get("song" + songCounter).get(triggerID)) || Arrays.asList(allmodtriggers).contains(this.triggerMapper.get("song" + songCounter).get(triggerID))))
                                                MusicTriggers.logger.warn("Trigger " + this.triggerMapper.get("song" + songCounter).get(triggerID) + " exists but is not supported in this version. Song " + s + " will be loaded but unplayable");
                                            CrashHelper = this.triggerMapper.get("song" + songCounter).get(triggerID);
                                            songholder.put("song" + songCounter, s);
                                            this.triggerholder.putIfAbsent("song" + songCounter, new HashMap<>());
                                            this.triggerholder.get("song" + songCounter).putIfAbsent(triggerID, new String[]{"0", "0", "0", "0", "0", "YouWillNeverGuessThis", "and", "0,0,0,0,0,0", "60",
                                                    "minecraft", "_", "16", "false", "100", "100", "100",
                                                    "false", "0", "minecraft", "true", "true", "0", "0", "nope",
                                                    "nope", "-111", "false", "_", "true", "-1", "-111", "true",
                                                    "false", "false", "false", "0", "minecraft", "true"});
                                            if (trigger.contains("priority"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[0] = trigger.getString("priority");
                                            if (trigger.contains("fade_in"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[1] = trigger.getString("fade_in");
                                            if (trigger.contains("level"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[2] = trigger.getString("level");
                                            if (trigger.contains("time"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[3] = trigger.getString("time");
                                            if (trigger.contains("delay"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[4] = trigger.getString("delay");
                                            if (trigger.contains("advancement"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[5] = trigger.getString("advancement");
                                            if (trigger.contains("operator"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[6] = trigger.getString("operator");
                                            if (trigger.containsTable("zone")) {
                                                Toml zone = trigger.getTable("zone");
                                                if (!zone.contains("x_min") || !zone.contains("y_min") || !zone.contains("z_min") || !zone.contains("x_max") || !zone.contains("y_max") || !zone.contains("z_max")) {
                                                    MusicTriggers.logger.warn("Incorrect format for the zone parameter! Skipping...");
                                                } else {
                                                    String coords = zone.getString("x_min");
                                                    coords = coords + "," + zone.getString("y_min");
                                                    coords = coords + "," + zone.getString("z_min");
                                                    coords = coords + "," + zone.getString("x_max");
                                                    coords = coords + "," + zone.getString("y_max");
                                                    coords = coords + "," + zone.getString("z_max");
                                                    this.triggerholder.get("song" + songCounter).get(triggerID)[7] = coords;
                                                }
                                            }
                                            if (trigger.contains("start"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[8] = trigger.getString("start");
                                            if (trigger.contains("resource_name"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[9] = trigger.getString("resource_name");
                                            if (trigger.contains("identifier"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[10] = trigger.getString("identifier");
                                            else if (trigger.contains("id"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[10] = trigger.getString("id");
                                            if (trigger.contains("detection_range"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[11] = trigger.getString("detection_range");
                                            if (trigger.contains("mob_targeting"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[12] = trigger.getString("mob_targeting");
                                            if (trigger.contains("horde_targeting_percentage"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[13] = trigger.getString("horde_targeting_percentage");
                                            if (trigger.contains("health"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[14] = trigger.getString("health");
                                            if (trigger.contains("horde_health_percentage"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[15] = trigger.getString("horde_health_percentage");
                                            if (trigger.contains("victory"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[16] = trigger.getString("victory");
                                            if (trigger.contains("victory_id"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[17] = trigger.getString("victory_id");
                                            if (trigger.contains("infernal"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[18] = trigger.getString("infernal");
                                            if (trigger.contains("whitelist"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[19] = trigger.getString("whitelist");
                                            if (trigger.contains("sky_light"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[20] = trigger.getString("sky_light");
                                            if (trigger.contains("phase"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[21] = trigger.getString("phase");
                                            if (trigger.contains("victory_timeout"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[22] = trigger.getString("victory_timeout");
                                            if (trigger.contains("biome_category"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[23] = trigger.getString("biome_category");
                                            if (trigger.contains("rain_type"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[24] = trigger.getString("rain_type");
                                            if (trigger.contains("biome_temperature"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[25] = trigger.getString("biome_temperature");
                                            if (trigger.contains("biome_cold"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[26] = trigger.getString("biome_cold");
                                            if (trigger.contains("mob_nbt"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[27] = trigger.getString("mob_nbt");
                                            if (trigger.contains("is_underground"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[28] = trigger.getString("is_underground");
                                            if (trigger.contains("end"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[29] = trigger.getString("end");
                                            if (trigger.contains("biome_rainfall"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[30] = trigger.getString("biome_rainfall");
                                            if (trigger.contains("biome_rainfall_higher"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[31] = trigger.getString("biome_rainfall");
                                            if (trigger.contains("is_instantiated"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[32] = trigger.getString("is_instantiated");
                                            if (trigger.contains("time_switch"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[33] = trigger.getString("time_switch");
                                            if (trigger.contains("remove_inactive_playable"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[34] = trigger.getString("remove_inactive_playable");
                                            if (trigger.contains("fade_out"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[35] = trigger.getString("fade_out");
                                            if (trigger.contains("mob_champion"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[36] = trigger.getString("mob_champion");
                                            if (trigger.contains("toggled"))
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[37] = trigger.getString("toggled");
                                        } else
                                            MusicTriggers.logger.warn("Could not find trigger with name " + triggerID + "in channel "+this.channel);
                                    } else
                                        MusicTriggers.logger.warn("Skipping trigger block because there was no name in channel "+this.channel+"!");
                                    triggerMapCounter++;
                                }
                            } else if (song.containsTable("trigger")) {
                                Toml trigger = song.getTable("trigger");
                                if (trigger.contains("name")) {
                                    String triggerID = "trigger-" + triggerMapCounter;
                                    this.triggerMapper.get("song" + songCounter).put(triggerID, trigger.getString("name"));
                                    if (Arrays.asList(triggers).contains(this.triggerMapper.get("song" + songCounter).get(triggerID)) || Arrays.asList(allmodtriggers).contains(this.triggerMapper.get("song" + songCounter).get(triggerID))) {
                                        if (!(Arrays.asList(triggers).contains(this.triggerMapper.get("song" + songCounter).get(triggerID)) || Arrays.asList(allmodtriggers).contains(this.triggerMapper.get("song" + songCounter).get(triggerID))))
                                            MusicTriggers.logger.warn("Trigger " + this.triggerMapper.get("song" + songCounter).get(triggerID) + " exists but is not supported in this version. Song " + s + " will be loaded but unplayable");
                                        CrashHelper = this.triggerMapper.get("song" + songCounter).get(triggerID);
                                        songholder.put("song" + songCounter, s);
                                        this.triggerholder.putIfAbsent("song" + songCounter, new HashMap<>());
                                        this.triggerholder.get("song" + songCounter).putIfAbsent(triggerID, new String[]{"0", "0", "0", "0", "0", "YouWillNeverGuessThis", "and", "0,0,0,0,0,0", "60",
                                                "minecraft", "_", "16", "false", "100", "100", "100",
                                                "false", "0", "minecraft", "true", "true", "0", "0", "nope",
                                                "nope", "-111", "false", "_", "true", "-1", "-111", "true",
                                                "false", "false", "false", "0", "minecraft", "true"});
                                        if (trigger.contains("priority"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[0] = trigger.getString("priority");
                                        if (trigger.contains("fade_in"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[1] = trigger.getString("fade_in");
                                        if (trigger.contains("level"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[2] = trigger.getString("level");
                                        if (trigger.contains("time"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[3] = trigger.getString("time");
                                        if (trigger.contains("delay"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[4] = trigger.getString("delay");
                                        if (trigger.contains("advancement"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[5] = trigger.getString("advancement");
                                        if (trigger.contains("operator"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[6] = trigger.getString("operator");
                                        if (trigger.containsTable("zone")) {
                                            Toml zone = trigger.getTable("zone");
                                            if (!zone.contains("x_min") || !zone.contains("y_min") || !zone.contains("z_min") || !zone.contains("x_max") || !zone.contains("y_max") || !zone.contains("z_max")) {
                                                MusicTriggers.logger.warn("Incorrect format for the zone parameter! Skipping...");
                                            } else {
                                                String coords = zone.getString("x_min");
                                                coords = coords + "," + zone.getString("y_min");
                                                coords = coords + "," + zone.getString("z_min");
                                                coords = coords + "," + zone.getString("x_max");
                                                coords = coords + "," + zone.getString("y_max");
                                                coords = coords + "," + zone.getString("z_max");
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[7] = coords;
                                            }
                                        }
                                        if (trigger.contains("start"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[8] = trigger.getString("start");
                                        if (trigger.contains("resource_name"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[9] = trigger.getString("resource_name");
                                        if (trigger.contains("identifier"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[10] = trigger.getString("identifier");
                                        else if (trigger.contains("id"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[10] = trigger.getString("id");
                                        if (trigger.contains("detection_range"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[11] = trigger.getString("detection_range");
                                        if (trigger.contains("mob_targeting"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[12] = trigger.getString("mob_targeting");
                                        if (trigger.contains("horde_targeting_percentage"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[13] = trigger.getString("horde_targeting_percentage");
                                        if (trigger.contains("health"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[14] = trigger.getString("health");
                                        if (trigger.contains("horde_health_percentage"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[15] = trigger.getString("horde_health_percentage");
                                        if (trigger.contains("victory"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[16] = trigger.getString("victory");
                                        if (trigger.contains("victory_id"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[17] = trigger.getString("victory_id");
                                        if (trigger.contains("infernal"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[18] = trigger.getString("infernal");
                                        if (trigger.contains("whitelist"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[19] = trigger.getString("whitelist");
                                        if (trigger.contains("sky_light"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[20] = trigger.getString("sky_light");
                                        if (trigger.contains("phase"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[21] = trigger.getString("phase");
                                        if (trigger.contains("victory_timeout"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[22] = trigger.getString("victory_timeout");
                                        if (trigger.contains("biome_category"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[23] = trigger.getString("biome_category");
                                        if (trigger.contains("rain_type"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[24] = trigger.getString("rain_type");
                                        if (trigger.contains("biome_temperature"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[25] = trigger.getString("biome_temperature");
                                        if (trigger.contains("biome_cold"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[26] = trigger.getString("biome_cold");
                                        if (trigger.contains("mob_nbt"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[27] = trigger.getString("mob_nbt");
                                        if (trigger.contains("is_underground"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[28] = trigger.getString("is_underground");
                                        if (trigger.contains("end"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[29] = trigger.getString("end");
                                        if (trigger.contains("biome_rainfall"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[30] = trigger.getString("biome_rainfall");
                                        if (trigger.contains("biome_rainfall_higher"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[31] = trigger.getString("biome_rainfall");
                                        if (trigger.contains("is_instantiated"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[32] = trigger.getString("is_instantiated");
                                        if (trigger.contains("time_switch"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[33] = trigger.getString("time_switch");
                                        if (trigger.contains("remove_inactive_playable"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[34] = trigger.getString("remove_inactive_playable");
                                        if (trigger.contains("fade_out"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[35] = trigger.getString("fade_out");
                                        if (trigger.contains("mob_champion"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[36] = trigger.getString("mob_champion");
                                        if (trigger.contains("toggled"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[37] = trigger.getString("toggled");
                                    } else MusicTriggers.logger.warn("Could not find trigger with name " + triggerID + "in channel "+this.channel);
                                } else MusicTriggers.logger.warn("Skipping trigger block because there was no name in channel "+this.channel+"!");
                            } else
                                MusicTriggers.logger.warn("Skipping instance of song " + s + " because no triggers were attached to it!");
                            this.otherinfo.putIfAbsent("song" + songCounter, new String[]{"1", "0", "false", "100", "1", "0", "0"});
                            if (song.contains("pitch"))
                                this.otherinfo.get("song" + songCounter)[0] = song.getString("pitch");
                            if (song.contains("play_once"))
                                this.otherinfo.get("song" + songCounter)[1] = song.getString("play_once");
                            if (song.contains("must_finish"))
                                this.otherinfo.get("song" + songCounter)[2] = song.getString("must_finish");
                            if (song.contains("chance"))
                                this.otherinfo.get("song" + songCounter)[3] = song.getString("chance");
                            if (song.contains("volume"))
                                this.otherinfo.get("song" + songCounter)[4] = song.getString("volume");
                            if (song.containsTable("link")) {
                                Toml link = song.getTable("link");
                                if (link.contains("default")) {
                                    if (link.contains("fade_in"))
                                        this.otherinfo.get("song" + songCounter)[5] = link.getString("fade_in");
                                    if (link.contains("fade_out"))
                                        this.otherinfo.get("song" + songCounter)[6] = link.getString("fade_out");
                                    if (link.containsTableArray("trigger")) {
                                        for (Toml trigger : link.getTables("trigger")) {
                                            if (!trigger.contains("song") || !trigger.contains("name"))
                                                MusicTriggers.logger.warn("Trigger needs a name and a song for linking to work in channel "+this.channel);
                                            else {
                                                this.triggerlinking.putIfAbsent("song" + songCounter, new HashMap<>());
                                                this.triggerlinking.get("song" + songCounter).putIfAbsent("song" + songCounter, link.getList("default").toArray(new String[0]));
                                                this.triggerlinking.get("song" + songCounter).put(trigger.getString("song"), trigger.getList("name").toArray(new String[0]));
                                                this.otherlinkinginfo.put("song" + songCounter, new HashMap<>());
                                                this.otherlinkinginfo.get("song" + songCounter).put(trigger.getString("song"), new String[]{"1", "1", "0", "0"});
                                                if (trigger.contains("pitch"))
                                                    this.otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[0] = trigger.getString("pitch");
                                                if (trigger.contains("volume"))
                                                    this.otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[1] = trigger.getString("volume");
                                                if (trigger.contains("fade_in"))
                                                    this.otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[2] = trigger.getString("fade_in");
                                                if (trigger.contains("fade_out"))
                                                    this.otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[3] = trigger.getString("fade_out");
                                                int loopIndex = 0;
                                                this.linkingLoopPoints.putIfAbsent("song" + songCounter, new HashMap<>());
                                                this.linkingLoopPoints.get("song" + songCounter).putIfAbsent(trigger.getString("song"), new HashMap<>());
                                                if (trigger.containsTable("loop")) {
                                                    Toml loop = trigger.getTable("loop");
                                                    this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                                    if (loop.contains("amount"))
                                                        this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[0] = loop.getString("amount");
                                                    if (loop.contains("min"))
                                                        this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[1] = loop.getString("min");
                                                    if (loop.contains("max"))
                                                        this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[2] = loop.getString("max");
                                                } else if (trigger.containsTableArray("loop")) {
                                                    for (Toml loop : trigger.getTables("loop")) {
                                                        this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                                        if (loop.contains("amount"))
                                                            this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[0] = loop.getString("amount");
                                                        if (loop.contains("min"))
                                                            this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[1] = loop.getString("min");
                                                        if (loop.contains("max"))
                                                            this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[2] = loop.getString("max");
                                                        loopIndex++;
                                                    }
                                                }
                                            }
                                        }

                                    } else if (link.containsTable("trigger")) {
                                        Toml trigger = link.getTable("trigger");
                                        if (!trigger.contains("song") || !trigger.contains("name"))
                                            MusicTriggers.logger.warn("Trigger needs a name and a song for linking to work in channel "+this.channel);
                                        else {
                                            this.triggerlinking.put("song" + songCounter, new HashMap<>());
                                            this.triggerlinking.get("song" + songCounter).putIfAbsent("song" + songCounter, link.getList("default").toArray(new String[0]));
                                            this.triggerlinking.get("song" + songCounter).put(trigger.getString("song"), trigger.getList("name").toArray(new String[0]));
                                            this.otherlinkinginfo.put("song" + songCounter, new HashMap<>());
                                            this.otherlinkinginfo.get("song" + songCounter).put(trigger.getString("song"), new String[]{"1", "1", "0", "0"});
                                            if (trigger.contains("pitch"))
                                                this.otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[0] = trigger.getString("pitch");
                                            if (trigger.contains("volume"))
                                                this.otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[1] = trigger.getString("volume");
                                            if (trigger.contains("fade_in"))
                                                this.otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[2] = trigger.getString("fade_in");
                                            if (trigger.contains("fade_out"))
                                                this.otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[3] = trigger.getString("fade_out");
                                            int loopIndex = 0;
                                            this.linkingLoopPoints.putIfAbsent("song" + songCounter, new HashMap<>());
                                            this.linkingLoopPoints.get("song" + songCounter).putIfAbsent(trigger.getString("song"), new HashMap<>());
                                            if (trigger.containsTable("loop")) {
                                                Toml loop = trigger.getTable("loop");
                                                this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                                if (loop.contains("amount"))
                                                    this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[0] = loop.getString("amount");
                                                if (loop.contains("min"))
                                                    this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[1] = loop.getString("min");
                                                if (loop.contains("max"))
                                                    this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[2] = loop.getString("max");
                                            } else if (trigger.containsTableArray("loop")) {
                                                for (Toml loop : trigger.getTables("loop")) {
                                                    this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                                    if (loop.contains("amount"))
                                                        this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[0] = loop.getString("amount");
                                                    if (loop.contains("min"))
                                                        this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[1] = loop.getString("min");
                                                    if (loop.contains("max"))
                                                        this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[2] = loop.getString("max");
                                                    loopIndex++;
                                                }
                                            }
                                        }
                                    } else
                                        MusicTriggers.logger.warn("Song " + s + " was set up for music linking but is not linked to anything in channel "+this.channel+"!");
                                } else
                                    MusicTriggers.logger.warn("Skipping music linking for song " + s + " as there was no default trigger set in channel "+this.channel+"!");
                            }
                            int loopIndex = 0;
                            this.loopPoints.putIfAbsent("song" + songCounter, new HashMap<>());
                            if (song.containsTable("loop")) {
                                Toml loop = song.getTable("loop");
                                this.loopPoints.get("song" + songCounter).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                if (loop.contains("amount"))
                                    this.loopPoints.get("song" + songCounter).get(loopIndex)[0] = loop.getString("amount");
                                if (loop.contains("min"))
                                    this.loopPoints.get("song" + songCounter).get(loopIndex)[1] = loop.getString("min");
                                if (loop.contains("max"))
                                    this.loopPoints.get("song" + songCounter).get(loopIndex)[2] = loop.getString("max");
                            } else if (song.containsTableArray("loop")) {
                                for (Toml loop : song.getTables("loop")) {
                                    this.loopPoints.get("song" + songCounter).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                    if (loop.contains("amount"))
                                        this.loopPoints.get("song" + songCounter).get(loopIndex)[0] = loop.getString("amount");
                                    if (loop.contains("min"))
                                        this.loopPoints.get("song" + songCounter).get(loopIndex)[1] = loop.getString("min");
                                    if (loop.contains("max"))
                                        this.loopPoints.get("song" + songCounter).get(loopIndex)[2] = loop.getString("max");
                                    loopIndex++;
                                }
                            }
                            songCounter++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Failed to initialize song block in channel "+this.channel+" from song " + s + " at " + CrashHelper + " (Internally: File " + e.getStackTrace()[0].getFileName() + " at line " + e.getStackTrace()[0].getLineNumber() + ")");
                    }
                } else if (toml.containsTable(s)) {
                    try {
                        Toml song = toml.getTable(s);
                        this.triggerMapper.put("song" + songCounter, new HashMap<>());
                        int triggerMapCounter = 0;
                        if (song.containsTableArray("trigger")) {
                            for (Toml trigger : song.getTables("trigger")) {
                                if (trigger.contains("name")) {
                                    String triggerID = "trigger-" + triggerMapCounter;
                                    this.triggerMapper.get("song" + songCounter).put(triggerID, trigger.getString("name"));
                                    if (Arrays.asList(triggers).contains(this.triggerMapper.get("song" + songCounter).get(triggerID)) || Arrays.asList(allmodtriggers).contains(this.triggerMapper.get("song" + songCounter).get(triggerID))) {
                                        if (!(Arrays.asList(triggers).contains(this.triggerMapper.get("song" + songCounter).get(triggerID)) || Arrays.asList(allmodtriggers).contains(this.triggerMapper.get("song" + songCounter).get(triggerID))))
                                            MusicTriggers.logger.warn("Trigger " + this.triggerMapper.get("song" + songCounter).get(triggerID) + " exists but is not supported in this version. Song " + s + " will be loaded but unplayable");
                                        CrashHelper = this.triggerMapper.get("song" + songCounter).get(triggerID);
                                        songholder.put("song" + songCounter, s);
                                        this.triggerholder.putIfAbsent("song" + songCounter, new HashMap<>());
                                        this.triggerholder.get("song" + songCounter).putIfAbsent(triggerID, new String[]{"0", "0", "0", "0", "0", "YouWillNeverGuessThis", "and", "0,0,0,0,0,0", "60",
                                                "minecraft", "_", "16", "false", "100", "100", "100",
                                                "false", "0", "minecraft", "true", "true", "0", "0", "nope",
                                                "nope", "-111", "false", "_", "true", "-1", "-111", "true",
                                                "false", "false", "false", "0", "minecraft", "true"});
                                        if (trigger.contains("priority"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[0] = trigger.getString("priority");
                                        if (trigger.contains("fade_in"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[1] = trigger.getString("fade_in");
                                        if (trigger.contains("level"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[2] = trigger.getString("level");
                                        if (trigger.contains("time"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[3] = trigger.getString("time");
                                        if (trigger.contains("delay"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[4] = trigger.getString("delay");
                                        if (trigger.contains("advancement"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[5] = trigger.getString("advancement");
                                        if (trigger.contains("operator"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[6] = trigger.getString("operator");
                                        if (trigger.containsTable("zone")) {
                                            Toml zone = trigger.getTable("zone");
                                            if (!zone.contains("x_min") || !zone.contains("y_min") || !zone.contains("z_min") || !zone.contains("x_max") || !zone.contains("y_max") || !zone.contains("z_max")) {
                                                MusicTriggers.logger.warn("Incorrect format for the zone parameter! Skipping...");
                                            } else {
                                                String coords = zone.getString("x_min");
                                                coords = coords + "," + zone.getString("y_min");
                                                coords = coords + "," + zone.getString("z_min");
                                                coords = coords + "," + zone.getString("x_max");
                                                coords = coords + "," + zone.getString("y_max");
                                                coords = coords + "," + zone.getString("z_max");
                                                this.triggerholder.get("song" + songCounter).get(triggerID)[7] = coords;
                                            }
                                        }
                                        if (trigger.contains("start"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[8] = trigger.getString("start");
                                        if (trigger.contains("resource_name"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[9] = trigger.getString("resource_name");
                                        if (trigger.contains("identifier"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[10] = trigger.getString("identifier");
                                        else if (trigger.contains("id"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[10] = trigger.getString("id");
                                        if (trigger.contains("detection_range"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[11] = trigger.getString("detection_range");
                                        if (trigger.contains("mob_targeting"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[12] = trigger.getString("mob_targeting");
                                        if (trigger.contains("horde_targeting_percentage"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[13] = trigger.getString("horde_targeting_percentage");
                                        if (trigger.contains("health"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[14] = trigger.getString("health");
                                        if (trigger.contains("horde_health_percentage"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[15] = trigger.getString("horde_health_percentage");
                                        if (trigger.contains("victory"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[16] = trigger.getString("victory");
                                        if (trigger.contains("victory_id"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[17] = trigger.getString("victory_id");
                                        if (trigger.contains("infernal"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[18] = trigger.getString("infernal");
                                        if (trigger.contains("whitelist"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[19] = trigger.getString("whitelist");
                                        if (trigger.contains("sky_light"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[20] = trigger.getString("sky_light");
                                        if (trigger.contains("phase"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[21] = trigger.getString("phase");
                                        if (trigger.contains("victory_timeout"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[22] = trigger.getString("victory_timeout");
                                        if (trigger.contains("biome_category"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[23] = trigger.getString("biome_category");
                                        if (trigger.contains("rain_type"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[24] = trigger.getString("rain_type");
                                        if (trigger.contains("biome_temperature"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[25] = trigger.getString("biome_temperature");
                                        if (trigger.contains("biome_cold"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[26] = trigger.getString("biome_cold");
                                        if (trigger.contains("mob_nbt"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[27] = trigger.getString("mob_nbt");
                                        if (trigger.contains("is_underground"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[28] = trigger.getString("is_underground");
                                        if (trigger.contains("end"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[29] = trigger.getString("end");
                                        if (trigger.contains("biome_rainfall"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[30] = trigger.getString("biome_rainfall");
                                        if (trigger.contains("biome_rainfall_higher"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[31] = trigger.getString("biome_rainfall");
                                        if (trigger.contains("is_instantiated"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[32] = trigger.getString("is_instantiated");
                                        if (trigger.contains("time_switch"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[33] = trigger.getString("time_switch");
                                        if (trigger.contains("remove_inactive_playable"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[34] = trigger.getString("remove_inactive_playable");
                                        if (trigger.contains("fade_out"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[35] = trigger.getString("fade_out");
                                        if (trigger.contains("mob_champion"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[36] = trigger.getString("mob_champion");
                                        if (trigger.contains("toggled"))
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[37] = trigger.getString("toggled");
                                    } else MusicTriggers.logger.warn("Could not find trigger with name " + triggerID + "in channel "+this.channel);
                                } else MusicTriggers.logger.warn("Skipping trigger block because there was no name in channel "+this.channel+"!");
                                triggerMapCounter++;
                            }
                        } else if (song.containsTable("trigger")) {
                            Toml trigger = song.getTable("trigger");
                            if (trigger.contains("name")) {
                                String triggerID = "trigger-" + triggerMapCounter;
                                this.triggerMapper.get("song" + songCounter).put(triggerID, trigger.getString("name"));
                                if (Arrays.asList(triggers).contains(this.triggerMapper.get("song" + songCounter).get(triggerID)) || Arrays.asList(allmodtriggers).contains(this.triggerMapper.get("song" + songCounter).get(triggerID))) {
                                    if (!(Arrays.asList(triggers).contains(this.triggerMapper.get("song" + songCounter).get(triggerID)) || Arrays.asList(allmodtriggers).contains(this.triggerMapper.get("song" + songCounter).get(triggerID))))
                                        MusicTriggers.logger.warn("Trigger " + this.triggerMapper.get("song" + songCounter).get(triggerID) + " exists but is not supported in this version. Song " + s + " will be loaded but unplayable");
                                    CrashHelper = this.triggerMapper.get("song" + songCounter).get(triggerID);
                                    songholder.put("song" + songCounter, s);
                                    this.triggerholder.putIfAbsent("song" + songCounter, new HashMap<>());
                                    this.triggerholder.get("song" + songCounter).putIfAbsent(triggerID, new String[]{"0", "0", "0", "0", "0", "YouWillNeverGuessThis", "and", "0,0,0,0,0,0", "60",
                                            "minecraft", "_", "16", "false", "100", "100", "100",
                                            "false", "0", "minecraft", "true", "true", "0", "0", "nope",
                                            "nope", "-111", "false", "_", "true", "-1", "-111", "true",
                                            "false", "false", "false", "0", "minecraft", "true"});
                                    if (trigger.contains("priority"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[0] = trigger.getString("priority");
                                    if (trigger.contains("fade_in"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[1] = trigger.getString("fade_in");
                                    if (trigger.contains("level"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[2] = trigger.getString("level");
                                    if (trigger.contains("time"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[3] = trigger.getString("time");
                                    if (trigger.contains("delay"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[4] = trigger.getString("delay");
                                    if (trigger.contains("advancement"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[5] = trigger.getString("advancement");
                                    if (trigger.contains("operator"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[6] = trigger.getString("operator");
                                    if (trigger.containsTable("zone")) {
                                        Toml zone = trigger.getTable("zone");
                                        if (!zone.contains("x_min") || !zone.contains("y_min") || !zone.contains("z_min") || !zone.contains("x_max") || !zone.contains("y_max") || !zone.contains("z_max")) {
                                            MusicTriggers.logger.warn("Incorrect format for the zone parameter! Skipping...");
                                        } else {
                                            String coords = zone.getString("x_min");
                                            coords = coords + "," + zone.getString("y_min");
                                            coords = coords + "," + zone.getString("z_min");
                                            coords = coords + "," + zone.getString("x_max");
                                            coords = coords + "," + zone.getString("y_max");
                                            coords = coords + "," + zone.getString("z_max");
                                            this.triggerholder.get("song" + songCounter).get(triggerID)[7] = coords;
                                        }
                                    }
                                    if (trigger.contains("start"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[8] = trigger.getString("start");
                                    if (trigger.contains("resource_name"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[9] = trigger.getString("resource_name");
                                    if (trigger.contains("identifier"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[10] = trigger.getString("identifier");
                                    else if (trigger.contains("id"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[10] = trigger.getString("id");
                                    if (trigger.contains("detection_range"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[11] = trigger.getString("detection_range");
                                    if (trigger.contains("mob_targeting"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[12] = trigger.getString("mob_targeting");
                                    if (trigger.contains("horde_targeting_percentage"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[13] = trigger.getString("horde_targeting_percentage");
                                    if (trigger.contains("health"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[14] = trigger.getString("health");
                                    if (trigger.contains("horde_health_percentage"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[15] = trigger.getString("horde_health_percentage");
                                    if (trigger.contains("victory"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[16] = trigger.getString("victory");
                                    if (trigger.contains("victory_id"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[17] = trigger.getString("victory_id");
                                    if (trigger.contains("infernal"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[18] = trigger.getString("infernal");
                                    if (trigger.contains("whitelist"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[19] = trigger.getString("whitelist");
                                    if (trigger.contains("sky_light"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[20] = trigger.getString("sky_light");
                                    if (trigger.contains("phase"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[21] = trigger.getString("phase");
                                    if (trigger.contains("victory_timeout"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[22] = trigger.getString("victory_timeout");
                                    if (trigger.contains("biome_category"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[23] = trigger.getString("biome_category");
                                    if (trigger.contains("rain_type"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[24] = trigger.getString("rain_type");
                                    if (trigger.contains("biome_temperature"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[25] = trigger.getString("biome_temperature");
                                    if (trigger.contains("biome_cold"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[26] = trigger.getString("biome_cold");
                                    if (trigger.contains("mob_nbt"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[27] = trigger.getString("mob_nbt");
                                    if (trigger.contains("is_underground"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[28] = trigger.getString("is_underground");
                                    if (trigger.contains("end"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[29] = trigger.getString("end");
                                    if (trigger.contains("biome_rainfall"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[30] = trigger.getString("biome_rainfall");
                                    if (trigger.contains("biome_rainfall_higher"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[31] = trigger.getString("biome_rainfall");
                                    if (trigger.contains("is_instantiated"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[32] = trigger.getString("is_instantiated");
                                    if (trigger.contains("time_switch"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[33] = trigger.getString("time_switch");
                                    if (trigger.contains("remove_inactive_playable"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[34] = trigger.getString("remove_inactive_playable");
                                    if (trigger.contains("fade_out"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[35] = trigger.getString("fade_out");
                                    if (trigger.contains("mob_champion"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[36] = trigger.getString("mob_champion");
                                    if (trigger.contains("toggled"))
                                        this.triggerholder.get("song" + songCounter).get(triggerID)[37] = trigger.getString("toggled");
                                } else MusicTriggers.logger.warn("Could not find trigger with name " + triggerID + "in channel "+this.channel);
                            } else MusicTriggers.logger.warn("Skipping trigger block because there was no name in channel "+this.channel+"!");
                        } else
                            MusicTriggers.logger.warn("Skipping instance of song " + s + " because no triggers were attached to it!");
                        this.otherinfo.putIfAbsent("song" + songCounter, new String[]{"1", "0", "false", "100", "1", "0", "0"});
                        if (song.contains("pitch"))
                            this.otherinfo.get("song" + songCounter)[0] = song.getString("pitch");
                        if (song.contains("play_once"))
                            this.otherinfo.get("song" + songCounter)[1] = song.getString("play_once");
                        if (song.contains("must_finish"))
                            this.otherinfo.get("song" + songCounter)[2] = song.getString("must_finish");
                        if (song.contains("chance"))
                            this.otherinfo.get("song" + songCounter)[3] = song.getString("chance");
                        if (song.contains("volume"))
                            this.otherinfo.get("song" + songCounter)[4] = song.getString("volume");
                        if (song.containsTable("link")) {
                            Toml link = song.getTable("link");
                            if (link.contains("default")) {
                                if (link.contains("fade_in"))
                                    this.otherinfo.get("song" + songCounter)[5] = link.getString("fade_in");
                                if (link.contains("fade_out"))
                                    this.otherinfo.get("song" + songCounter)[6] = link.getString("fade_out");
                                if (link.containsTableArray("trigger")) {
                                    for (Toml trigger : link.getTables("trigger")) {
                                        if (!trigger.contains("song") || !trigger.contains("name"))
                                            MusicTriggers.logger.warn("Trigger needs a name and a song for linking to work in channel "+this.channel);
                                        else {
                                            this.triggerlinking.put("song" + songCounter, new HashMap<>());
                                            this.triggerlinking.get("song" + songCounter).putIfAbsent("song" + songCounter, link.getList("default").toArray(new String[0]));
                                            this.triggerlinking.get("song" + songCounter).put(trigger.getString("song"), trigger.getList("name").toArray(new String[0]));
                                            this.otherlinkinginfo.put("song" + songCounter, new HashMap<>());
                                            this.otherlinkinginfo.get("song" + songCounter).put(trigger.getString("song"), new String[]{"1", "1", "0", "0"});
                                            if (trigger.contains("pitch"))
                                                this.otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[0] = trigger.getString("pitch");
                                            if (trigger.contains("volume"))
                                                this.otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[1] = trigger.getString("volume");
                                            if (trigger.contains("fade_in"))
                                                this.otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[2] = trigger.getString("fade_in");
                                            if (trigger.contains("fade_out"))
                                                this.otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[3] = trigger.getString("fade_out");
                                            int loopIndex = 0;
                                            this.linkingLoopPoints.putIfAbsent("song" + songCounter, new HashMap<>());
                                            this.linkingLoopPoints.get("song" + songCounter).putIfAbsent(trigger.getString("song"), new HashMap<>());
                                            if (trigger.containsTable("loop")) {
                                                Toml loop = trigger.getTable("loop");
                                                this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                                if (loop.contains("amount"))
                                                    this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[0] = loop.getString("amount");
                                                if (loop.contains("min"))
                                                    this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[1] = loop.getString("min");
                                                if (loop.contains("max"))
                                                    this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[2] = loop.getString("max");
                                            } else if (trigger.containsTableArray("loop")) {
                                                for (Toml loop : trigger.getTables("loop")) {
                                                    this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                                    if (loop.contains("amount"))
                                                        this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[0] = loop.getString("amount");
                                                    if (loop.contains("min"))
                                                        this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[1] = loop.getString("min");
                                                    if (loop.contains("max"))
                                                        this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[2] = loop.getString("max");
                                                    loopIndex++;
                                                }
                                            }
                                        }
                                    }
                                } else if (link.containsTable("trigger")) {
                                    Toml trigger = link.getTable("trigger");
                                    if (!trigger.contains("song") || !trigger.contains("name"))
                                        MusicTriggers.logger.warn("Trigger needs a name and a song for linking to work in channel "+this.channel);
                                    else {
                                        this.triggerlinking.put("song" + songCounter, new HashMap<>());
                                        this.triggerlinking.get("song" + songCounter).putIfAbsent("song" + songCounter, link.getList("default").toArray(new String[0]));
                                        this.triggerlinking.get("song" + songCounter).put(trigger.getString("song"), trigger.getList("name").toArray(new String[0]));
                                        this.otherlinkinginfo.put("song" + songCounter, new HashMap<>());
                                        this.otherlinkinginfo.get("song" + songCounter).put(trigger.getString("song"), new String[]{"1", "1", "0", "0"});
                                        if (trigger.contains("pitch"))
                                            this.otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[0] = trigger.getString("pitch");
                                        if (trigger.contains("volume"))
                                            this.otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[1] = trigger.getString("volume");
                                        if (trigger.contains("fade_in"))
                                            this.otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[2] = trigger.getString("fade_in");
                                        if (trigger.contains("fade_out"))
                                            this.otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[3] = trigger.getString("fade_out");
                                        int loopIndex = 0;
                                        this.linkingLoopPoints.putIfAbsent("song" + songCounter, new HashMap<>());
                                        this.linkingLoopPoints.get("song" + songCounter).putIfAbsent(trigger.getString("song"), new HashMap<>());
                                        if (trigger.containsTable("loop")) {
                                            Toml loop = trigger.getTable("loop");
                                            this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                            if (loop.contains("amount"))
                                                this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[0] = loop.getString("amount");
                                            if (loop.contains("min"))
                                                this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[1] = loop.getString("min");
                                            if (loop.contains("max"))
                                                this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[2] = loop.getString("max");
                                        } else if (trigger.containsTableArray("loop")) {
                                            for (Toml loop : trigger.getTables("loop")) {
                                                this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                                if (loop.contains("amount"))
                                                    this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[0] = loop.getString("amount");
                                                if (loop.contains("min"))
                                                    this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[1] = loop.getString("min");
                                                if (loop.contains("max"))
                                                    this.linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[2] = loop.getString("max");
                                                loopIndex++;
                                            }
                                        }
                                    }
                                } else
                                    MusicTriggers.logger.warn("Song " + s + " was set up for music linking but is not linked to anything in channel "+this.channel+"!");
                            } else
                                MusicTriggers.logger.warn("Skipping music linking for song " + s + " as there was no default trigger set in channel "+this.channel+"!");
                        }
                        int loopIndex = 0;
                        this.loopPoints.putIfAbsent("song" + songCounter, new HashMap<>());
                        if (song.containsTable("loop")) {
                            Toml loop = song.getTable("loop");
                            this.loopPoints.get("song" + songCounter).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                            if (loop.contains("amount"))
                                this.loopPoints.get("song" + songCounter).get(loopIndex)[0] = loop.getString("amount");
                            if (loop.contains("min"))
                                this.loopPoints.get("song" + songCounter).get(loopIndex)[1] = loop.getString("min");
                            if (loop.contains("max"))
                                this.loopPoints.get("song" + songCounter).get(loopIndex)[2] = loop.getString("max");
                        } else if (song.containsTableArray("loop")) {
                            for (Toml loop : song.getTables("loop")) {
                                this.loopPoints.get("song" + songCounter).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                if (loop.contains("amount"))
                                    this.loopPoints.get("song" + songCounter).get(loopIndex)[0] = loop.getString("amount");
                                if (loop.contains("min"))
                                    this.loopPoints.get("song" + songCounter).get(loopIndex)[1] = loop.getString("min");
                                if (loop.contains("max"))
                                    this.loopPoints.get("song" + songCounter).get(loopIndex)[2] = loop.getString("max");
                                loopIndex++;
                            }
                        }
                        songCounter++;
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Failed to initialize song block in channel "+this.channel+" from song " + s + " at " + CrashHelper + " (Internally: File " + e.getStackTrace()[0].getFileName() + " at line " + e.getStackTrace()[0].getLineNumber() + ")");
                    }
                }
            }
        } catch (RuntimeException ex) {
            throw new RuntimeException("Your main toml file has a fatal syntax error - "+ex.getMessage());
        }
    }

    public static List<String> songCollector(File toml) {
        List<String> ret = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(toml));
            String line = br.readLine();
            while (line != null) {
                if(!line.contains("universal")) {
                    if (!line.contains("\t") && !line.contains(" ") && line.contains("[") && line.contains("]") && !line.contains("\"") && !line.contains(".") && !line.contains("#")) {
                        String betterLine = line.replaceAll("\\[", "").replaceAll("]", "");
                        if (!ret.contains(betterLine)) ret.add(betterLine);
                    }
                }
                line = br.readLine();
            }
            br.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void writeInformationalHeader(File toml) {
        try {
            String header = """
                    # Please refer to the wiki page located at https://github.com/TheComputerizer/Music-Triggers/wiki/The-Basics
                    # or the discord server located at https://discord.gg/FZHXFYp8fc
                    # for any specific questions you might have regarding the main config file""";
            FileWriter writer = new FileWriter(toml);
            writer.write(header);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearMaps() {
        this.songholder.clear();
        this.triggerholder.clear();
        this.otherinfo.clear();
        this.otherlinkinginfo.clear();
        this.triggerlinking.clear();
        this.loopPoints.clear();
        this.linkingLoopPoints.clear();
    }
}
