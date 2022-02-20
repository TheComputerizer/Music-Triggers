package mods.thecomputerizer.musictriggers.config;

import com.moandjiezana.toml.Toml;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.util.json;

import java.io.File;
import java.util.*;

public class configToml {
    public static Map<String, String> songholder = new HashMap<>();
    public static Map<String, String> reversesongholder = new HashMap<>();
    public static Map<String, Map<String, String[]>> triggerholder = new HashMap<>();
    public static Map<String, String[]> otherinfo = new HashMap<>();
    public static Map<String, Map<String, String[]>> triggerlinking = new HashMap<>();

    public static final String[] triggers = new String[]
            {"menu","generic","difficulty","day","night","sunrise","sunset","light","underground","raining","storming","snowing",
                    "lowhp","dead","creative","spectator","riding","pet","high","underwater","elytra","fishing","dimension","biome",
                    "structure","mob","victory","gui","effect","zones","pvp"};
    public static final String[] modtriggers = new String[]
            {"gamestage","bloodmoon","harvestmoon","fallingstars","rainintensity","tornado","hurricane","sandstorm"};

    //priority,fade,level,time,one time,must finish,loop info,operator,zone,start,
    //resourcename,identifier,range,mobtargetting,hordetargetpercentage,health,hordehealthpercentage,
    //victory,victoryID,infernalmob,gamestagewhitelist
    public static final String[] populateTriggers = new String[]{"0","0","0","0","false","false","loop","and","0,0,0,0,0,0","60",
            "minecraft","","16","false","100","100","100",
            "false","0","minecraft","true"};

    public static void parse() {
        //pitch,defaulttrigger
        String[] populateSongInfo = new String[]{"1","day"};
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
            for (String s : json.allSongs) {
                if (toml.containsTableArray(s)) {
                    MusicTriggers.logger.info("found song "+s);
                    for (Toml song : toml.getTables(s)) {
                        if (song.containsTableArray("trigger")) {
                            MusicTriggers.logger.info("found trigger for "+s);
                            for (Toml trigger : song.getTables("trigger")) {
                                if (trigger.contains("name")) {
                                    String triggerID = trigger.getString("name");
                                    if (Arrays.asList(triggers).contains(triggerID) || Arrays.asList(modtriggers).contains(triggerID)) {
                                        songholder.put("song" + songCounter, s);
                                        reversesongholder.put(s, "song" + songCounter);
                                        triggerholder.putIfAbsent("song" + songCounter, new HashMap<>());
                                        triggerholder.get("song" + songCounter).put(triggerID, populateTriggers);
                                        if (trigger.contains("priority")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[0] = trigger.getString("priority");
                                        }
                                        if (trigger.contains("fade")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[1] = trigger.getString("fade");
                                        }
                                        if (triggerID.matches("mob") && !trigger.contains("level")) {
                                            MusicTriggers.logger.warn("The mob trigger needs a level parameter to specify the number of mobs!");
                                        }
                                        if (trigger.contains("level")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[2] = trigger.getString("level");
                                        }
                                        if (trigger.contains("time")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[3] = trigger.getString("time");
                                        }
                                        if (trigger.contains("play_once")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[4] = trigger.getString("play_once");
                                        }
                                        if (trigger.contains("must_finish")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[5] = trigger.getString("must_finish");
                                        }
                                        if (trigger.contains("operator")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[7] = trigger.getString("operator");
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
                                        if ((triggerID.matches("dimension") || triggerID.matches("biome") || triggerID.matches("structure") || triggerID.matches("mob") || triggerID.matches("effect") || triggerID.matches("gui")) && !trigger.contains("resource_name")) {
                                            MusicTriggers.logger.warn("The dimension, biome, structure, mob, effect, and gui triggers require a resource_name parameter!");
                                        }
                                        if (trigger.contains("resource_name")) {
                                            triggerholder.get("song" + songCounter).get(triggerID)[9] = trigger.getString("resource_name");
                                        }
                                        if ((triggerID.matches("light") || triggerID.matches("mob")) && !trigger.contains("identifier")) {
                                            MusicTriggers.logger.warn("The light and mob triggers require a string identifier parameter!");
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
                                    } else {
                                        MusicTriggers.logger.warn("Could not find trigger with name " + triggerID);
                                    }
                                } else {
                                    MusicTriggers.logger.warn("Skipping trigger block because there was no name!");
                                }
                            }
                        } else if (song.containsTable("trigger")) {
                            Toml trigger = song.getTable("trigger");
                            if (trigger.contains("name")) {
                                String triggerID = trigger.getString("name");
                                if (Arrays.asList(triggers).contains(triggerID) || Arrays.asList(modtriggers).contains(triggerID)) {
                                    songholder.put("song" + songCounter, s);
                                    reversesongholder.put(s, "song" + songCounter);
                                    triggerholder.putIfAbsent("song" + songCounter, new HashMap<>());
                                    triggerholder.get("song" + songCounter).put(triggerID, populateTriggers);
                                    if (trigger.contains("priority")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[0] = trigger.getString("priority");
                                    }
                                    if (trigger.contains("fade")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[1] = trigger.getString("fade");
                                    }
                                    if (triggerID.matches("mob") && !trigger.contains("level")) {
                                        MusicTriggers.logger.warn("The mob trigger needs a level parameter to specify the number of mobs!");
                                    }
                                    if (trigger.contains("level")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[2] = trigger.getString("level");
                                    }
                                    if (trigger.contains("time")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[3] = trigger.getString("time");
                                    }
                                    if (trigger.contains("play_once")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[4] = trigger.getString("play_once");
                                    }
                                    if (trigger.contains("must_finish")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[5] = trigger.getString("must_finish");
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
                                    if ((triggerID.matches("dimension") || triggerID.matches("biome") || triggerID.matches("structure") || triggerID.matches("mob") || triggerID.matches("effect") || triggerID.matches("gui")) && !trigger.contains("resource_name")) {
                                        MusicTriggers.logger.warn("The dimension, biome, structure, mob, effect, and gui triggers require a resource_name parameter!");
                                    }
                                    if (trigger.contains("resource_name")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[9] = trigger.getString("resource_name");
                                    }
                                    if ((triggerID.matches("light") || triggerID.matches("mob")) && !trigger.contains("identifier")) {
                                        MusicTriggers.logger.warn("The light and mob triggers require a string identifier parameter!");
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
                                } else {
                                    MusicTriggers.logger.warn("Could not find trigger with name " + triggerID);
                                }
                            } else {
                                MusicTriggers.logger.warn("Skipping trigger block because there was no name!");
                            }
                        } else {
                            MusicTriggers.logger.warn("Skipping instance of song " + s + " because no triggers were attached to it!");
                        }
                        otherinfo.put("song" + songCounter, populateSongInfo);
                        if (song.contains("pitch")) {
                            otherinfo.get("song" + songCounter)[0] = song.getString("pitch");
                        }
                        if (song.containsTable("link")) {
                            Toml link = song.getTable("link");
                            if (link.contains("default")) {
                                otherinfo.get("song" + songCounter)[1] = link.getString("default");
                                if (link.containsTableArray("trigger")) {
                                    for (Toml trigger : link.getTables("trigger")) {
                                        if (!trigger.contains("song") || !trigger.contains("name")) {
                                            MusicTriggers.logger.warn("Trigger needs a name and a song for linking to work");
                                        } else if (!Arrays.asList(triggers).contains(trigger.getString("name")) || !Arrays.asList(modtriggers).contains(trigger.getString("name"))) {
                                            MusicTriggers.logger.warn("Trigger name must match the list");
                                        } else {
                                            triggerlinking.put("song" + songCounter, new HashMap<>());
                                            triggerlinking.get("song" + songCounter).put(trigger.getString("song"), trigger.getList("name").toArray(new String[0]));
                                        }
                                    }

                                } else if (link.containsTable("trigger")) {
                                    Toml trigger = link.getTable("trigger");
                                    if (!trigger.contains("song") || !trigger.contains("name")) {
                                        MusicTriggers.logger.warn("Trigger needs a name and a song for linking to work");
                                    } else if (!Arrays.asList(triggers).contains(trigger.getString("name")) || !Arrays.asList(modtriggers).contains(trigger.getString("name"))) {
                                        MusicTriggers.logger.warn("Trigger name must match the list");
                                    } else {
                                        triggerlinking.put("song" + songCounter, new HashMap<>());
                                        triggerlinking.get("song" + songCounter).put(trigger.getString("song"), trigger.getList("name").toArray(new String[0]));
                                    }
                                } else {
                                    MusicTriggers.logger.warn("Song " + s + " was set up for music linking, but is not linked to anything!");
                                }
                            } else {
                                MusicTriggers.logger.warn("Skipping music linking for song " + s + " as there was no default trigger set!");
                            }
                        }
                        songCounter++;
                    }
                } else if (toml.containsTable(s)) {
                    MusicTriggers.logger.info("found song "+s);
                    Toml song = toml.getTable(s);
                    if (song.containsTableArray("trigger")) {
                        MusicTriggers.logger.info("found trigger for "+s);
                        for (Toml trigger : song.getTables("trigger")) {
                            if (trigger.contains("name")) {
                                String triggerID = trigger.getString("name");
                                MusicTriggers.logger.info("found trigger of "+triggerID+" for "+s);
                                if (Arrays.asList(triggers).contains(triggerID) || Arrays.asList(modtriggers).contains(triggerID)) {
                                    songholder.put("song" + songCounter, s);
                                    reversesongholder.put(s, "song" + songCounter);
                                    triggerholder.putIfAbsent("song" + songCounter, new HashMap<>());
                                    triggerholder.get("song" + songCounter).put(triggerID, populateTriggers);
                                    if (trigger.contains("priority")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[0] = trigger.getString("priority");
                                    }
                                    if (trigger.contains("fade")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[1] = trigger.getString("fade");
                                    }
                                    if (triggerID.matches("mob") && !trigger.contains("level")) {
                                        MusicTriggers.logger.warn("The mob trigger needs a level parameter to specify the number of mobs!");
                                    }
                                    if (trigger.contains("level")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[2] = trigger.getString("level");
                                    }
                                    if (trigger.contains("time")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[3] = trigger.getString("time");
                                    }
                                    if (trigger.contains("play_once")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[4] = trigger.getString("play_once");
                                    }
                                    if (trigger.contains("must_finish")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[5] = trigger.getString("must_finish");
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
                                    if ((triggerID.matches("dimension") || triggerID.matches("biome") || triggerID.matches("structure") || triggerID.matches("mob") || triggerID.matches("effect") || triggerID.matches("gui")) && !trigger.contains("resource_name")) {
                                        MusicTriggers.logger.warn("The dimension, biome, structure, mob, effect, and gui triggers require a resource_name parameter!");
                                    }
                                    if (trigger.contains("resource_name")) {
                                        triggerholder.get("song" + songCounter).get(triggerID)[9] = trigger.getString("resource_name");
                                    }
                                    if ((triggerID.matches("light") || triggerID.matches("mob")) && !trigger.contains("identifier")) {
                                        MusicTriggers.logger.warn("The light and mob triggers require a string identifier parameter!");
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
                                } else {
                                    MusicTriggers.logger.warn("Could not find trigger with name " + triggerID);
                                }
                            } else {
                                MusicTriggers.logger.warn("Skipping trigger block because there was no name!");
                            }
                        }
                    } else if (song.containsTable("trigger")) {
                        MusicTriggers.logger.info("found trigger for "+s);
                        Toml trigger = song.getTable("trigger");
                        if (trigger.contains("name")) {
                            String triggerID = trigger.getString("name");
                            if (Arrays.asList(triggers).contains(triggerID) || Arrays.asList(modtriggers).contains(triggerID)) {
                                songholder.put("song" + songCounter, s);
                                reversesongholder.put(s, "song" + songCounter);
                                triggerholder.putIfAbsent("song" + songCounter, new HashMap<>());
                                triggerholder.get("song" + songCounter).put(triggerID, populateTriggers);
                                if (trigger.contains("priority")) {
                                    triggerholder.get("song" + songCounter).get(triggerID)[0] = trigger.getString("priority");
                                }
                                if (trigger.contains("fade")) {
                                    triggerholder.get("song" + songCounter).get(triggerID)[1] = trigger.getString("fade");
                                }
                                if (triggerID.matches("mob") && !trigger.contains("level")) {
                                    MusicTriggers.logger.warn("The mob trigger needs a level parameter to specify the number of mobs!");
                                }
                                if (trigger.contains("level")) {
                                    triggerholder.get("song" + songCounter).get(triggerID)[2] = trigger.getString("level");
                                }
                                if (trigger.contains("time")) {
                                    triggerholder.get("song" + songCounter).get(triggerID)[3] = trigger.getString("time");
                                }
                                if (trigger.contains("play_once")) {
                                    triggerholder.get("song" + songCounter).get(triggerID)[4] = trigger.getString("play_once");
                                }
                                if (trigger.contains("must_finish")) {
                                    triggerholder.get("song" + songCounter).get(triggerID)[5] = trigger.getString("must_finish");
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
                                if ((triggerID.matches("dimension") || triggerID.matches("biome") || triggerID.matches("structure") || triggerID.matches("mob") || triggerID.matches("effect") || triggerID.matches("gui")) && !trigger.contains("resource_name")) {
                                    MusicTriggers.logger.warn("The dimension, biome, structure, mob, effect, and gui triggers require a resource_name parameter!");
                                }
                                if (trigger.contains("resource_name")) {
                                    triggerholder.get("song" + songCounter).get(triggerID)[9] = trigger.getString("resource_name");
                                }
                                if ((triggerID.matches("light") || triggerID.matches("mob")) && !trigger.contains("identifier")) {
                                    MusicTriggers.logger.warn("The light and mob triggers require a string identifier parameter!");
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
                            } else {
                                MusicTriggers.logger.warn("Could not find trigger with name " + triggerID);
                            }
                        } else {
                            MusicTriggers.logger.warn("Skipping trigger block because there was no name!");
                        }
                    } else {
                        MusicTriggers.logger.warn("Skipping instance of song " + s + " because no triggers were attached to it!");
                    }
                    otherinfo.put("song" + songCounter, populateSongInfo);
                    if (song.contains("pitch")) {
                        otherinfo.get("song" + songCounter)[0] = song.getString("pitch");
                    }
                    if (song.containsTable("link")) {
                        Toml link = song.getTable("link");
                        if (link.contains("default")) {
                            otherinfo.get("song" + songCounter)[1] = link.getString("default");
                            if (link.containsTableArray("trigger")) {
                                for (Toml trigger : link.getTables("trigger")) {
                                    if (!trigger.contains("song") || !trigger.contains("name")) {
                                        MusicTriggers.logger.warn("Trigger needs a name and a song for linking to work");
                                    } else if (!Arrays.asList(triggers).contains(trigger.getString("name")) || !Arrays.asList(modtriggers).contains(trigger.getString("name"))) {
                                        MusicTriggers.logger.warn("Trigger name must match the list");
                                    } else {
                                        triggerlinking.put("song" + songCounter, new HashMap<>());
                                        triggerlinking.get("song" + songCounter).put(trigger.getString("song"), trigger.getList("name").toArray(new String[0]));
                                    }
                                }

                            } else if (link.containsTable("trigger")) {
                                Toml trigger = link.getTable("trigger");
                                if (!trigger.contains("song") || !trigger.contains("name")) {
                                    MusicTriggers.logger.warn("Trigger needs a name and a song for linking to work");
                                } else if (!Arrays.asList(triggers).contains(trigger.getString("name")) || !Arrays.asList(modtriggers).contains(trigger.getString("name"))) {
                                    MusicTriggers.logger.warn("Trigger name must match the list");
                                } else {
                                    triggerlinking.put("song" + songCounter, new HashMap<>());
                                    triggerlinking.get("song" + songCounter).put(trigger.getString("song"), trigger.getList("name").toArray(new String[0]));
                                }
                            } else {
                                MusicTriggers.logger.warn("Song " + s + " was set up for music linking, but is not linked to anything!");
                            }
                        } else {
                            MusicTriggers.logger.warn("Skipping music linking for song " + s + " as there was no default trigger set!");
                        }
                    }
                    songCounter++;
                }
            }
        }
    }
}
