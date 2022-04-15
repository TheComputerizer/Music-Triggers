package mods.thecomputerizer.musictriggers.config;

import com.moandjiezana.toml.Toml;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.MusicPicker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class configToml {
    public static String CrashHelper;

    public static Map<String, String> songholder = new HashMap<>();
    public static Map<String, Map<String, String[]>> triggerholder = new HashMap<>();
    public static Map<String, Map<String, String>> triggerMapper = new HashMap<>();
    public static Map<String, String[]> otherinfo = new HashMap<>();
    public static Map<String, Map<String, String[]>> otherlinkinginfo = new HashMap<>();
    public static Map<String, Map<String, String[]>> triggerlinking = new HashMap<>();
    public static Map<String, Map<Integer, String[]>> loopPoints = new HashMap<>();
    public static Map<String, Map<String, Map<Integer, String[]>>> linkingLoopPoints = new HashMap<>();

    public static final String[] triggers = new String[]
            {"menu","generic","difficulty","time","light","height","raining","storming","snowing","lowhp","dead",
                    "creative","spectator","riding","pet","underwater","elytra","fishing","drowning","home",
                    "dimension","biome", "structure","mob","victory","gui","effect","zones","pvp","advancement",
                    "raid", "statistic"};
    public static final String[] modtriggers = new String[]
            {""};

    //priority,fade,level,time,delay,advancement,operator,zone,start,
    //resourcename,identifier,range,mobtargetting,hordetargetpercentage,health,hordehealthpercentage,
    //victory,victoryID,infernalmob,gamestagewhitelist,skylight,phase,victory_timeout,biome_category,rain_type,
    //biome_temperature,biome_cold,mob_nbt,underground,end,rainfall,higher_rainfall,instance,time_switch,inactiveplayable

    //pitch,one time,must finish,chance

    public static void parse() {
        File file = new File("config/MusicTriggers/musictriggers.toml");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        else {
            Toml toml = new Toml().read(file);
            int songCounter = 0;
            if(toml.contains("universal")) {
                Toml universal = toml.getTable("universal");
                if (universal.contains("fade_in")) MusicPicker.universalFadeIn = Integer.parseInt(universal.getString("fade_in"));
                if (universal.contains("fade_out")) MusicPicker.universalFadeOut = Integer.parseInt(universal.getString("fade_out"));
                if (universal.contains("fade_delay")) MusicPicker.universalDelay = Integer.parseInt(universal.getString("fade_delay"));
            }
            for (String s : songCollector(file)) {
                if (toml.containsTableArray(s)) {
                    try {
                        for (Toml song : toml.getTables(s)) {
                            triggerMapper.put("song" + songCounter, new HashMap<>());
                            int triggerMapCounter = 0;
                            if (song.containsTableArray("trigger")) {
                                for (Toml trigger : song.getTables("trigger")) {
                                    if (trigger.contains("name")) {
                                        String triggerID = "trigger-"+triggerMapCounter;
                                        triggerMapper.get("song" + songCounter).put(triggerID,trigger.getString("name"));
                                        if (Arrays.asList(triggers).contains(triggerMapper.get("song" + songCounter).get(triggerID)) || Arrays.asList(modtriggers).contains(triggerMapper.get("song" + songCounter).get(triggerID))) {
                                            CrashHelper = triggerMapper.get("song" + songCounter).get(triggerID);
                                            songholder.put("song" + songCounter, s);
                                            triggerholder.putIfAbsent("song" + songCounter, new HashMap<>());
                                            triggerholder.get("song" + songCounter).putIfAbsent(triggerID, new String[]{"0", "0", "0", "0", "0", "YouWillNeverGuessThis", "and", "0,0,0,0,0,0", "60",
                                                    "minecraft", "_", "16", "false", "100", "100", "100",
                                                    "false", "0", "minecraft", "true", "true", "0", "0", "nope",
                                                    "nope", "-111", "false","_", "true", "-1", "-111", "true",
                                                    "false", "false", "false", "0"});
                                            if (trigger.contains("priority")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[0] = trigger.getString("priority");
                                            }
                                            if (trigger.contains("fade_in")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[1] = trigger.getString("fade_in");
                                            }
                                            if (trigger.contains("level")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[2] = trigger.getString("level");
                                            }
                                            if (trigger.contains("time")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[3] = trigger.getString("time");
                                            }
                                            if (trigger.contains("delay")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[4] = trigger.getString("delay");
                                            }
                                            if (trigger.contains("advancement")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[5] = trigger.getString("advancement");
                                            }
                                            if (trigger.contains("operator")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[6] = trigger.getString("operator");
                                            }
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
                                                    triggerholder.get("song" + songCounter).get(triggerID)[7] = coords;
                                                }
                                            }
                                            if (trigger.contains("start")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[8] = trigger.getString("start");
                                            }
                                            if (trigger.contains("resource_name")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[9] = trigger.getString("resource_name");
                                            }
                                            if (trigger.contains("identifier")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[10] = trigger.getString("identifier");
                                            }
                                            if (trigger.contains("detection_range")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[11] = trigger.getString("detection_range");
                                            }
                                            if (trigger.contains("mob_targetting")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[12] = trigger.getString("mob_targetting");
                                            }
                                            if (trigger.contains("horde_targetting_percentage")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[13] = trigger.getString("horde_targetting_percentage");
                                            }
                                            if (trigger.contains("health")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[14] = trigger.getString("health");
                                            }
                                            if (trigger.contains("horde_health_percentage")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[15] = trigger.getString("horde_health_percentage");
                                            }
                                            if (trigger.contains("victory")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[16] = trigger.getString("victory");
                                            }
                                            if (trigger.contains("victory_id")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[17] = trigger.getString("victory_id");
                                            }
                                            if (trigger.contains("infernal")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[18] = trigger.getString("infernal");
                                            }
                                            if (trigger.contains("whitelist")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[19] = trigger.getString("whitelist");
                                            }
                                            if (trigger.contains("sky_light")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[20] = trigger.getString("sky_light");
                                            }
                                            if (trigger.contains("phase")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[21] = trigger.getString("phase");
                                            }
                                            if (trigger.contains("victory_timeout")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[22] = trigger.getString("victory_timeout");
                                            }
                                            if (trigger.contains("biome_category")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[23] = trigger.getString("biome_category");
                                            }
                                            if (trigger.contains("rain_type")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[24] = trigger.getString("rain_type");
                                            }
                                            if (trigger.contains("biome_temperature")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[25] = trigger.getString("biome_temperature");
                                            }
                                            if (trigger.contains("biome_cold")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[26] = trigger.getString("biome_cold");
                                            }
                                            if (trigger.contains("mob_nbt")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[27] = trigger.getString("mob_nbt");
                                            }
                                            if (trigger.contains("is_underground")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[28] = trigger.getString("is_underground");
                                            }
                                            if (trigger.contains("end")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[29] = trigger.getString("end");
                                            }
                                            if (trigger.contains("biome_rainfall")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[30] = trigger.getString("biome_rainfall");
                                            }
                                            if (trigger.contains("biome_rainfall_higher")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[31] = trigger.getString("biome_rainfall");
                                            }
                                            if (trigger.contains("is_instantiated")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[32] = trigger.getString("is_instantiated");
                                            }
                                            if (trigger.contains("time_switch")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[33] = trigger.getString("time_switch");
                                            }
                                            if (trigger.contains("remove_inactive_playable")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[34] = trigger.getString("remove_inactive_playable");
                                            }
                                            if (trigger.contains("fade_out")) {
                                                triggerholder.get("song" + songCounter).get(triggerID)[35] = trigger.getString("fade_out");
                                            }
                                        } else {
                                            MusicTriggers.logger.warn("Could not find trigger with name " + triggerID);
                                        }
                                    } else {
                                        MusicTriggers.logger.warn("Skipping trigger block because there was no name!");
                                    }
                                    triggerMapCounter++;
                                }
                            } else if (song.containsTable("trigger")) {
                                Toml trigger = song.getTable("trigger");
                                if (trigger.contains("name")) {
                                    String triggerID = "trigger-"+triggerMapCounter;
                                    triggerMapper.get("song" + songCounter).put(triggerID,trigger.getString("name"));
                                    if (Arrays.asList(triggers).contains(triggerMapper.get("song" + songCounter).get(triggerID)) || Arrays.asList(modtriggers).contains(triggerMapper.get("song" + songCounter).get(triggerID))) {
                                        CrashHelper = triggerMapper.get("song" + songCounter).get(triggerID);
                                        songholder.put("song" + songCounter, s);
                                        triggerholder.putIfAbsent("song" + songCounter, new HashMap<>());
                                        triggerholder.get("song" + songCounter).putIfAbsent(triggerID, new String[]{"0", "0", "0", "0", "0", "YouWillNeverGuessThis", "and", "0,0,0,0,0,0", "60",
                                                "minecraft", "_", "16", "false", "100", "100", "100",
                                                "false", "0", "minecraft", "true", "true", "0", "0", "nope",
                                                "nope", "-111", "false","_", "true", "-1", "-111", "true",
                                                "false", "false", "false", "0"});
                                        if (trigger.contains("priority")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[0] = trigger.getString("priority");
                                        }
                                        if (trigger.contains("fade_in")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[1] = trigger.getString("fade_in");
                                        }
                                        if (trigger.contains("level")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[2] = trigger.getString("level");
                                        }
                                        if (trigger.contains("time")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[3] = trigger.getString("time");
                                        }
                                        if (trigger.contains("delay")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[4] = trigger.getString("delay");
                                        }
                                        if (trigger.contains("advancement")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[5] = trigger.getString("advancement");
                                        }
                                        if (trigger.contains("operator")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[6] = trigger.getString("operator");
                                        }
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
                                                triggerholder.get("song" + songCounter).get(triggerID)[7] = coords;
                                            }
                                        }
                                        if (trigger.contains("start")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[8] = trigger.getString("start");
                                        }
                                        if (trigger.contains("resource_name")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[9] = trigger.getString("resource_name");
                                        }
                                        if (trigger.contains("identifier")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[10] = trigger.getString("identifier");
                                        }
                                        if (trigger.contains("detection_range")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[11] = trigger.getString("detection_range");
                                        }
                                        if (trigger.contains("mob_targetting")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[12] = trigger.getString("mob_targetting");
                                        }
                                        if (trigger.contains("horde_targetting_percentage")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[13] = trigger.getString("horde_targetting_percentage");
                                        }
                                        if (trigger.contains("health")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[14] = trigger.getString("health");
                                        }
                                        if (trigger.contains("horde_health_percentage")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[15] = trigger.getString("horde_health_percentage");
                                        }
                                        if (trigger.contains("victory")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[16] = trigger.getString("victory");
                                        }
                                        if (trigger.contains("victory_id")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[17] = trigger.getString("victory_id");
                                        }
                                        if (trigger.contains("infernal")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[18] = trigger.getString("infernal");
                                        }
                                        if (trigger.contains("whitelist")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[19] = trigger.getString("whitelist");
                                        }
                                        if (trigger.contains("sky_light")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[20] = trigger.getString("sky_light");
                                        }
                                        if (trigger.contains("phase")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[21] = trigger.getString("phase");
                                        }
                                        if (trigger.contains("victory_timeout")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[22] = trigger.getString("victory_timeout");
                                        }
                                        if (trigger.contains("biome_category")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[23] = trigger.getString("biome_category");
                                        }
                                        if (trigger.contains("rain_type")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[24] = trigger.getString("rain_type");
                                        }
                                        if (trigger.contains("biome_temperature")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[25] = trigger.getString("biome_temperature");
                                        }
                                        if (trigger.contains("biome_cold")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[26] = trigger.getString("biome_cold");
                                        }
                                        if (trigger.contains("mob_nbt")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[27] = trigger.getString("mob_nbt");
                                        }
                                        if (trigger.contains("is_underground")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[28] = trigger.getString("is_underground");
                                        }
                                        if (trigger.contains("end")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[29] = trigger.getString("end");
                                        }
                                        if (trigger.contains("biome_rainfall")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[30] = trigger.getString("biome_rainfall");
                                        }
                                        if (trigger.contains("biome_rainfall_higher")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[31] = trigger.getString("biome_rainfall");
                                        }
                                        if (trigger.contains("is_instantiated")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[32] = trigger.getString("is_instantiated");
                                        }
                                        if (trigger.contains("time_switch")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[33] = trigger.getString("time_switch");
                                        }
                                        if (trigger.contains("remove_inactive_playable")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[34] = trigger.getString("remove_inactive_playable");
                                        }
                                        if (trigger.contains("fade_out")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[35] = trigger.getString("fade_out");
                                        }
                                    } else {
                                        MusicTriggers.logger.warn("Could not find trigger with name " + triggerID);
                                    }
                                } else {
                                    MusicTriggers.logger.warn("Skipping trigger block because there was no name!");
                                }
                            } else {
                                MusicTriggers.logger.warn("Skipping instance of song " + s + " because no triggers were attached to it!");
                            }
                            otherinfo.putIfAbsent("song" + songCounter, new String[]{"1", "0", "false", "100", "1", "0", "0"});
                            if (song.contains("pitch")) {
                                otherinfo.get("song" + songCounter)[0] = song.getString("pitch");
                            }
                            if (song.contains("play_once")) {
                                otherinfo.get("song" + songCounter)[1] = song.getString("play_once");
                            }
                            if (song.contains("must_finish")) {
                                otherinfo.get("song" + songCounter)[2] = song.getString("must_finish");
                            }
                            if (song.contains("chance")) {
                                otherinfo.get("song" + songCounter)[3] = song.getString("chance");
                            }
                            if (song.contains("volume")) {
                                otherinfo.get("song" + songCounter)[4] = song.getString("volume");
                            }
                            if (song.containsTable("link")) {
                                Toml link = song.getTable("link");
                                if (link.contains("default")) {
                                    if (link.contains("fade_in")) {
                                        otherinfo.get("song" + songCounter)[5] = song.getString("fade_in");
                                    }
                                    if (link.contains("fade_out")) {
                                        otherinfo.get("song" + songCounter)[6] = song.getString("fade_out");
                                    }
                                    if (link.containsTableArray("trigger")) {
                                        for (Toml trigger : link.getTables("trigger")) {
                                            if (!trigger.contains("song") || !trigger.contains("name")) {
                                                MusicTriggers.logger.warn("Trigger needs a name and a song for linking to work");
                                            } else {
                                                triggerlinking.putIfAbsent("song" + songCounter, new HashMap<>());
                                                triggerlinking.get("song" + songCounter).putIfAbsent("song" + songCounter, link.getList("default").toArray(new String[0]));
                                                triggerlinking.get("song" + songCounter).put(trigger.getString("song"), trigger.getList("name").toArray(new String[0]));
                                                otherlinkinginfo.put("song" + songCounter, new HashMap<>());
                                                otherlinkinginfo.get("song" + songCounter).put(trigger.getString("song"), new String[]{"1", "1", "0", "0"});
                                                if (trigger.contains("pitch")) {
                                                    otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[0] = trigger.getString("pitch");
                                                }
                                                if (trigger.contains("volume")) {
                                                    otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[1] = trigger.getString("volume");
                                                }
                                                if (trigger.contains("fade_in")) {
                                                    otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[2] = trigger.getString("fade_in");
                                                }
                                                if (trigger.contains("fade_out")) {
                                                    otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[3] = trigger.getString("fade_out");
                                                }
                                                int loopIndex = 0;
                                                linkingLoopPoints.putIfAbsent("song" + songCounter, new HashMap<>());
                                                linkingLoopPoints.get("song" + songCounter).putIfAbsent(trigger.getString("song"), new HashMap<>());
                                                if (trigger.containsTable("loop")) {
                                                    Toml loop = trigger.getTable("loop");
                                                    linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                                    if (loop.contains("amount")) {
                                                        linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[0] = loop.getString("amount");
                                                    }
                                                    if (loop.contains("min")) {
                                                        linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[1] = loop.getString("min");
                                                    }
                                                    if (loop.contains("max")) {
                                                        linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[2] = loop.getString("max");
                                                    }
                                                } else if (trigger.containsTableArray("loop")) {
                                                    for (Toml loop : trigger.getTables("loop")) {
                                                        linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                                        if (loop.contains("amount")) {
                                                            linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[0] = loop.getString("amount");
                                                        }
                                                        if (loop.contains("min")) {
                                                            linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[1] = loop.getString("min");
                                                        }
                                                        if (loop.contains("max")) {
                                                            linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[2] = loop.getString("max");
                                                        }
                                                        loopIndex++;
                                                    }
                                                }
                                            }
                                        }

                                    } else if (link.containsTable("trigger")) {
                                        Toml trigger = link.getTable("trigger");
                                        if (!trigger.contains("song") || !trigger.contains("name")) {
                                            MusicTriggers.logger.warn("Trigger needs a name and a song for linking to work");
                                        } else {
                                            triggerlinking.put("song" + songCounter, new HashMap<>());
                                            triggerlinking.get("song" + songCounter).putIfAbsent("song" + songCounter, link.getList("default").toArray(new String[0]));
                                            triggerlinking.get("song" + songCounter).put(trigger.getString("song"), trigger.getList("name").toArray(new String[0]));
                                            otherlinkinginfo.put("song" + songCounter, new HashMap<>());
                                            otherlinkinginfo.get("song" + songCounter).put(trigger.getString("song"), new String[]{"1", "1", "0", "0"});
                                            if (trigger.contains("pitch")) {
                                                otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[0] = trigger.getString("pitch");
                                            }
                                            if (trigger.contains("volume")) {
                                                otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[1] = trigger.getString("volume");
                                            }
                                            if (trigger.contains("fade_in")) {
                                                otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[2] = trigger.getString("fade_in");
                                            }
                                            if (trigger.contains("fade_out")) {
                                                otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[3] = trigger.getString("fade_out");
                                            }
                                            int loopIndex = 0;
                                            linkingLoopPoints.putIfAbsent("song" + songCounter, new HashMap<>());
                                            linkingLoopPoints.get("song" + songCounter).putIfAbsent(trigger.getString("song"), new HashMap<>());
                                            if (trigger.containsTable("loop")) {
                                                Toml loop = trigger.getTable("loop");
                                                linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                                if (loop.contains("amount")) {
                                                    linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[0] = loop.getString("amount");
                                                }
                                                if (loop.contains("min")) {
                                                    linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[1] = loop.getString("min");
                                                }
                                                if (loop.contains("max")) {
                                                    linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[2] = loop.getString("max");
                                                }
                                            } else if (trigger.containsTableArray("loop")) {
                                                for (Toml loop : trigger.getTables("loop")) {
                                                    linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                                    if (loop.contains("amount")) {
                                                        linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[0] = loop.getString("amount");
                                                    }
                                                    if (loop.contains("min")) {
                                                        linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[1] = loop.getString("min");
                                                    }
                                                    if (loop.contains("max")) {
                                                        linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[2] = loop.getString("max");
                                                    }
                                                    loopIndex++;
                                                }
                                            }
                                        }
                                    } else {
                                        MusicTriggers.logger.warn("Song " + s + " was set up for music linking, but is not linked to anything!");
                                    }
                                } else {
                                    MusicTriggers.logger.warn("Skipping music linking for song " + s + " as there was no default trigger set!");
                                }
                            }
                            int loopIndex = 0;
                            loopPoints.putIfAbsent("song" + songCounter, new HashMap<>());
                            if (song.containsTable("loop")) {
                                Toml loop = song.getTable("loop");
                                loopPoints.get("song" + songCounter).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                if (loop.contains("amount")) {
                                    loopPoints.get("song" + songCounter).get(loopIndex)[0] = loop.getString("amount");
                                }
                                if (loop.contains("min")) {
                                    loopPoints.get("song" + songCounter).get(loopIndex)[1] = loop.getString("min");
                                }
                                if (loop.contains("max")) {
                                    loopPoints.get("song" + songCounter).get(loopIndex)[2] = loop.getString("max");
                                }
                            } else if(song.containsTableArray("loop")) {
                                for (Toml loop : song.getTables("loop")) {
                                    loopPoints.get("song" + songCounter).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                    if (loop.contains("amount")) {
                                        loopPoints.get("song" + songCounter).get(loopIndex)[0] = loop.getString("amount");
                                    }
                                    if (loop.contains("min")) {
                                        loopPoints.get("song" + songCounter).get(loopIndex)[1] = loop.getString("min");
                                    }
                                    if (loop.contains("max")) {
                                        loopPoints.get("song" + songCounter).get(loopIndex)[2] = loop.getString("max");
                                    }
                                    loopIndex++;
                                }
                            }
                            songCounter++;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to initialize song block from song "+s+" at "+CrashHelper+" (Internally: File "+e.getStackTrace()[0].getFileName()+" at line "+e.getStackTrace()[0].getLineNumber()+")");
                    }
                } else if (toml.containsTable(s)) {
                    try {
                        Toml song = toml.getTable(s);
                        triggerMapper.put("song" + songCounter, new HashMap<>());
                        int triggerMapCounter = 0;
                        if (song.containsTableArray("trigger")) {
                            for (Toml trigger : song.getTables("trigger")) {
                                if (trigger.contains("name")) {
                                    String triggerID = "trigger-"+triggerMapCounter;
                                    triggerMapper.get("song" + songCounter).put(triggerID,trigger.getString("name"));
                                    if (Arrays.asList(triggers).contains(triggerMapper.get("song" + songCounter).get(triggerID)) || Arrays.asList(modtriggers).contains(triggerMapper.get("song" + songCounter).get(triggerID))) {
                                        CrashHelper = triggerMapper.get("song" + songCounter).get(triggerID);
                                        songholder.put("song" + songCounter, s);
                                        triggerholder.putIfAbsent("song" + songCounter, new HashMap<>());
                                        triggerholder.get("song" + songCounter).putIfAbsent(triggerID, new String[]{"0", "0", "0", "0", "0", "YouWillNeverGuessThis", "and", "0,0,0,0,0,0", "60",
                                                "minecraft", "_", "16", "false", "100", "100", "100",
                                                "false", "0", "minecraft", "true", "true", "0", "0", "nope",
                                                "nope", "-111", "false","_", "true", "-1", "-111", "true",
                                                "false", "false", "false", "0"});
                                        if (trigger.contains("priority")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[0] = trigger.getString("priority");
                                        }
                                        if (trigger.contains("fade_in")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[1] = trigger.getString("fade_in");
                                        }
                                        if (trigger.contains("level")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[2] = trigger.getString("level");
                                        }
                                        if (trigger.contains("time")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[3] = trigger.getString("time");
                                        }
                                        if (trigger.contains("delay")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[4] = trigger.getString("delay");
                                        }
                                        if (trigger.contains("advancement")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[5] = trigger.getString("advancement");
                                        }
                                        if (trigger.contains("operator")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[6] = trigger.getString("operator");
                                        }
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
                                                triggerholder.get("song" + songCounter).get(triggerID)[7] = coords;
                                            }
                                        }
                                        if (trigger.contains("start")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[8] = trigger.getString("start");
                                        }
                                        if (trigger.contains("resource_name")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[9] = trigger.getString("resource_name");
                                        }
                                        if (trigger.contains("identifier")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[10] = trigger.getString("identifier");
                                        }
                                        if (trigger.contains("detection_range")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[11] = trigger.getString("detection_range");
                                        }
                                        if (trigger.contains("mob_targetting")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[12] = trigger.getString("mob_targetting");
                                        }
                                        if (trigger.contains("horde_targetting_percentage")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[13] = trigger.getString("horde_targetting_percentage");
                                        }
                                        if (trigger.contains("health")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[14] = trigger.getString("health");
                                        }
                                        if (trigger.contains("horde_health_percentage")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[15] = trigger.getString("horde_health_percentage");
                                        }
                                        if (trigger.contains("victory")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[16] = trigger.getString("victory");
                                        }
                                        if (trigger.contains("victory_id")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[17] = trigger.getString("victory_id");
                                        }
                                        if (trigger.contains("infernal")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[18] = trigger.getString("infernal");
                                        }
                                        if (trigger.contains("whitelist")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[19] = trigger.getString("whitelist");
                                        }
                                        if (trigger.contains("sky_light")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[20] = trigger.getString("sky_light");
                                        }
                                        if (trigger.contains("phase")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[21] = trigger.getString("phase");
                                        }
                                        if (trigger.contains("victory_timeout")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[22] = trigger.getString("victory_timeout");
                                        }
                                        if (trigger.contains("biome_category")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[23] = trigger.getString("biome_category");
                                        }
                                        if (trigger.contains("rain_type")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[24] = trigger.getString("rain_type");
                                        }
                                        if (trigger.contains("biome_temperature")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[25] = trigger.getString("biome_temperature");
                                        }
                                        if (trigger.contains("biome_cold")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[26] = trigger.getString("biome_cold");
                                        }
                                        if (trigger.contains("mob_nbt")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[27] = trigger.getString("mob_nbt");
                                        }
                                        if (trigger.contains("is_underground")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[28] = trigger.getString("is_underground");
                                        }
                                        if (trigger.contains("end")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[29] = trigger.getString("end");
                                        }
                                        if (trigger.contains("biome_rainfall")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[30] = trigger.getString("biome_rainfall");
                                        }
                                        if (trigger.contains("biome_rainfall_higher")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[31] = trigger.getString("biome_rainfall");
                                        }
                                        if (trigger.contains("is_instantiated")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[32] = trigger.getString("is_instantiated");
                                        }
                                        if (trigger.contains("time_switch")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[33] = trigger.getString("time_switch");
                                        }
                                        if (trigger.contains("remove_inactive_playable")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[34] = trigger.getString("remove_inactive_playable");
                                        }
                                        if (trigger.contains("fade_out")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[35] = trigger.getString("fade_out");
                                        }
                                    } else {
                                        MusicTriggers.logger.warn("Could not find trigger with name " + triggerID);
                                    }
                                } else {
                                    MusicTriggers.logger.warn("Skipping trigger block because there was no name!");
                                }
                                triggerMapCounter++;
                            }
                        } else if (song.containsTable("trigger")) {
                            Toml trigger = song.getTable("trigger");
                            if (trigger.contains("name")) {
                                String triggerID = "trigger-"+triggerMapCounter;
                                triggerMapper.get("song" + songCounter).put(triggerID,trigger.getString("name"));
                                if (Arrays.asList(triggers).contains(triggerMapper.get("song" + songCounter).get(triggerID)) || Arrays.asList(modtriggers).contains(triggerMapper.get("song" + songCounter).get(triggerID))) {
                                    CrashHelper = triggerMapper.get("song" + songCounter).get(triggerID);
                                    songholder.put("song" + songCounter, s);
                                    triggerholder.putIfAbsent("song" + songCounter, new HashMap<>());
                                    triggerholder.get("song" + songCounter).putIfAbsent(triggerID, new String[]{"0", "0", "0", "0", "0", "YouWillNeverGuessThis", "and", "0,0,0,0,0,0", "60",
                                            "minecraft", "_", "16", "false", "100", "100", "100",
                                            "false", "0", "minecraft", "true", "true", "0", "0", "nope",
                                            "nope", "-111", "false","_", "true", "-1", "-111", "true",
                                            "false", "false", "false", "0"});
                                    if (trigger.contains("priority")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[0] = trigger.getString("priority");
                                    }
                                    if (trigger.contains("fade_in")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[1] = trigger.getString("fade_in");
                                    }
                                    if (trigger.contains("level")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[2] = trigger.getString("level");
                                    }
                                    if (trigger.contains("time")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[3] = trigger.getString("time");
                                    }
                                    if (trigger.contains("delay")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[4] = trigger.getString("delay");
                                    }
                                    if (trigger.contains("advancement")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[5] = trigger.getString("advancement");
                                    }
                                    if (trigger.contains("operator")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[6] = trigger.getString("operator");
                                    }
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
                                            triggerholder.get("song" + songCounter).get(triggerID)[7] = coords;
                                        }
                                    }
                                    if (trigger.contains("start")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[8] = trigger.getString("start");
                                    }
                                    if (trigger.contains("resource_name")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[9] = trigger.getString("resource_name");
                                    }
                                    if (trigger.contains("identifier")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[10] = trigger.getString("identifier");
                                    }
                                    if (trigger.contains("detection_range")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[11] = trigger.getString("detection_range");
                                    }
                                    if (trigger.contains("mob_targetting")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[12] = trigger.getString("mob_targetting");
                                    }
                                    if (trigger.contains("horde_targetting_percentage")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[13] = trigger.getString("horde_targetting_percentage");
                                    }
                                    if (trigger.contains("health")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[14] = trigger.getString("health");
                                    }
                                    if (trigger.contains("horde_health_percentage")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[15] = trigger.getString("horde_health_percentage");
                                    }
                                    if (trigger.contains("victory")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[16] = trigger.getString("victory");
                                    }
                                    if (trigger.contains("victory_id")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[17] = trigger.getString("victory_id");
                                    }
                                    if (trigger.contains("infernal")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[18] = trigger.getString("infernal");
                                    }
                                    if (trigger.contains("whitelist")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[19] = trigger.getString("whitelist");
                                    }
                                    if (trigger.contains("sky_light")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[20] = trigger.getString("sky_light");
                                    }
                                    if (trigger.contains("phase")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[21] = trigger.getString("phase");
                                    }
                                    if (trigger.contains("victory_timeout")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[22] = trigger.getString("victory_timeout");
                                    }
                                    if (trigger.contains("biome_category")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[23] = trigger.getString("biome_category");
                                    }
                                    if (trigger.contains("rain_type")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[24] = trigger.getString("rain_type");
                                    }
                                    if (trigger.contains("biome_temperature")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[25] = trigger.getString("biome_temperature");
                                    }
                                    if (trigger.contains("biome_cold")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[26] = trigger.getString("biome_cold");
                                    }
                                    if (trigger.contains("mob_nbt")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[27] = trigger.getString("mob_nbt");
                                    }
                                    if (trigger.contains("is_underground")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[28] = trigger.getString("is_underground");
                                    }
                                    if (trigger.contains("end")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[29] = trigger.getString("end");
                                    }
                                    if (trigger.contains("biome_rainfall")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[30] = trigger.getString("biome_rainfall");
                                    }
                                    if (trigger.contains("biome_rainfall_higher")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[31] = trigger.getString("biome_rainfall");
                                    }
                                    if (trigger.contains("is_instantiated")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[32] = trigger.getString("is_instantiated");
                                    }
                                    if (trigger.contains("time_switch")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[33] = trigger.getString("time_switch");
                                    }
                                    if (trigger.contains("remove_inactive_playable")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[34] = trigger.getString("remove_inactive_playable");
                                    }
                                    if (trigger.contains("fade_out")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[35] = trigger.getString("fade_out");
                                    }
                                } else {
                                    MusicTriggers.logger.warn("Could not find trigger with name " + triggerID);
                                }
                            } else {
                                MusicTriggers.logger.warn("Skipping trigger block because there was no name!");
                            }
                        } else {
                            MusicTriggers.logger.warn("Skipping instance of song " + s + " because no triggers were attached to it!");
                        }
                        otherinfo.putIfAbsent("song" + songCounter, new String[]{"1", "0", "false", "100", "1", "0", "0"});
                        if (song.contains("pitch")) {
                            otherinfo.get("song" + songCounter)[0] = song.getString("pitch");
                        }
                        if (song.contains("play_once")) {
                            otherinfo.get("song" + songCounter)[1] = song.getString("play_once");
                        }
                        if (song.contains("must_finish")) {
                            otherinfo.get("song" + songCounter)[2] = song.getString("must_finish");
                        }
                        if (song.contains("chance")) {
                            otherinfo.get("song" + songCounter)[3] = song.getString("chance");
                        }
                        if (song.contains("volume")) {
                            otherinfo.get("song" + songCounter)[4] = song.getString("volume");
                        }
                        if (song.containsTable("link")) {
                            Toml link = song.getTable("link");
                            if (link.contains("default")) {
                                if (link.contains("fade_in")) {
                                    otherinfo.get("song" + songCounter)[5] = song.getString("fade_in");
                                }
                                if (link.contains("fade_out")) {
                                    otherinfo.get("song" + songCounter)[6] = song.getString("fade_out");
                                }
                                if (link.containsTableArray("trigger")) {
                                    for (Toml trigger : link.getTables("trigger")) {
                                        if (!trigger.contains("song") || !trigger.contains("name")) {
                                            MusicTriggers.logger.warn("Trigger needs a name and a song for linking to work");
                                        } else {
                                            triggerlinking.put("song" + songCounter, new HashMap<>());
                                            triggerlinking.get("song" + songCounter).putIfAbsent("song" + songCounter, link.getList("default").toArray(new String[0]));
                                            triggerlinking.get("song" + songCounter).put(trigger.getString("song"), trigger.getList("name").toArray(new String[0]));
                                            otherlinkinginfo.put("song" + songCounter, new HashMap<>());
                                            otherlinkinginfo.get("song" + songCounter).put(trigger.getString("song"), new String[]{"1", "1", "0", "0"});
                                            if (trigger.contains("pitch")) {
                                                otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[0] = trigger.getString("pitch");
                                            }
                                            if (trigger.contains("volume")) {
                                                otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[1] = trigger.getString("volume");
                                            }
                                            if (trigger.contains("fade_in")) {
                                                otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[2] = trigger.getString("fade_in");
                                            }
                                            if (trigger.contains("fade_out")) {
                                                otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[3] = trigger.getString("fade_out");
                                            }
                                            int loopIndex = 0;
                                            linkingLoopPoints.putIfAbsent("song" + songCounter, new HashMap<>());
                                            linkingLoopPoints.get("song" + songCounter).putIfAbsent(trigger.getString("song"), new HashMap<>());
                                            if (trigger.containsTable("loop")) {
                                                Toml loop = trigger.getTable("loop");
                                                linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                                if (loop.contains("amount")) {
                                                    linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[0] = loop.getString("amount");
                                                }
                                                if (loop.contains("min")) {
                                                    linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[1] = loop.getString("min");
                                                }
                                                if (loop.contains("max")) {
                                                    linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[2] = loop.getString("max");
                                                }
                                            } else if (trigger.containsTableArray("loop")) {
                                                for (Toml loop : trigger.getTables("loop")) {
                                                    linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                                    if (loop.contains("amount")) {
                                                        linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[0] = loop.getString("amount");
                                                    }
                                                    if (loop.contains("min")) {
                                                        linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[1] = loop.getString("min");
                                                    }
                                                    if (loop.contains("max")) {
                                                        linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[2] = loop.getString("max");
                                                    }
                                                    loopIndex++;
                                                }
                                            }
                                        }
                                    }

                                } else if (link.containsTable("trigger")) {
                                    Toml trigger = link.getTable("trigger");
                                    if (!trigger.contains("song") || !trigger.contains("name")) {
                                        MusicTriggers.logger.warn("Trigger needs a name and a song for linking to work");
                                    } else {
                                        triggerlinking.put("song" + songCounter, new HashMap<>());
                                        triggerlinking.get("song" + songCounter).putIfAbsent("song" + songCounter, link.getList("default").toArray(new String[0]));
                                        triggerlinking.get("song" + songCounter).put(trigger.getString("song"), trigger.getList("name").toArray(new String[0]));
                                        otherlinkinginfo.put("song" + songCounter, new HashMap<>());
                                        otherlinkinginfo.get("song" + songCounter).put(trigger.getString("song"), new String[]{"1", "1", "0", "0"});
                                        if (trigger.contains("pitch")) {
                                            otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[0] = trigger.getString("pitch");
                                        }
                                        if (trigger.contains("volume")) {
                                            otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[1] = trigger.getString("volume");
                                        }
                                        if (trigger.contains("fade_in")) {
                                            otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[2] = trigger.getString("fade_in");
                                        }
                                        if (trigger.contains("fade_out")) {
                                            otherlinkinginfo.get("song" + songCounter).get(trigger.getString("song"))[3] = trigger.getString("fade_out");
                                        }
                                        int loopIndex = 0;
                                        linkingLoopPoints.putIfAbsent("song" + songCounter, new HashMap<>());
                                        linkingLoopPoints.get("song" + songCounter).putIfAbsent(trigger.getString("song"), new HashMap<>());
                                        if (trigger.containsTable("loop")) {
                                            Toml loop = trigger.getTable("loop");
                                            linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                            if (loop.contains("amount")) {
                                                linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[0] = loop.getString("amount");
                                            }
                                            if (loop.contains("min")) {
                                                linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[1] = loop.getString("min");
                                            }
                                            if (loop.contains("max")) {
                                                linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[2] = loop.getString("max");
                                            }
                                        } else if (trigger.containsTableArray("loop")) {
                                            for (Toml loop : trigger.getTables("loop")) {
                                                linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                                if (loop.contains("amount")) {
                                                    linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[0] = loop.getString("amount");
                                                }
                                                if (loop.contains("min")) {
                                                    linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[1] = loop.getString("min");
                                                }
                                                if (loop.contains("max")) {
                                                    linkingLoopPoints.get("song" + songCounter).get(trigger.getString("song")).get(loopIndex)[2] = loop.getString("max");
                                                }
                                                loopIndex++;
                                            }
                                        }
                                    }
                                } else {
                                    MusicTriggers.logger.warn("Song " + s + " was set up for music linking, but is not linked to anything!");
                                }
                            } else {
                                MusicTriggers.logger.warn("Skipping music linking for song " + s + " as there was no default trigger set!");
                            }
                        }
                        int loopIndex = 0;
                        loopPoints.putIfAbsent("song" + songCounter, new HashMap<>());
                        if (song.containsTable("loop")) {
                            Toml loop = song.getTable("loop");
                            loopPoints.get("song" + songCounter).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                            if (loop.contains("amount")) {
                                loopPoints.get("song" + songCounter).get(loopIndex)[0] = loop.getString("amount");
                            }
                            if (loop.contains("min")) {
                                loopPoints.get("song" + songCounter).get(loopIndex)[1] = loop.getString("min");
                            }
                            if (loop.contains("max")) {
                                loopPoints.get("song" + songCounter).get(loopIndex)[2] = loop.getString("max");
                            }
                        } else if(song.containsTableArray("loop")) {
                            for (Toml loop : song.getTables("loop")) {
                                loopPoints.get("song" + songCounter).putIfAbsent(loopIndex, new String[]{"0", "0", "0"});
                                if (loop.contains("amount")) {
                                    loopPoints.get("song" + songCounter).get(loopIndex)[0] = loop.getString("amount");
                                }
                                if (loop.contains("min")) {
                                    loopPoints.get("song" + songCounter).get(loopIndex)[1] = loop.getString("min");
                                }
                                if (loop.contains("max")) {
                                    loopPoints.get("song" + songCounter).get(loopIndex)[2] = loop.getString("max");
                                }
                                loopIndex++;
                            }
                        }
                        songCounter++;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to initialize song block from song "+s+" at "+CrashHelper+" (Internally: File "+e.getStackTrace()[0].getFileName()+" at line "+e.getStackTrace()[0].getLineNumber()+")");
                    }
                }
            }
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

    public static void emptyMaps() {
        songholder = new HashMap<>();
        triggerholder = new HashMap<>();
        triggerMapper = new HashMap<>();
        otherinfo = new HashMap<>();
        otherlinkinginfo = new HashMap<>();
        triggerlinking = new HashMap<>();
    }
}
