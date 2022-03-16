package mods.thecomputerizer.musictriggers.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mappings {
    public static Map<Integer, String> parameters = new HashMap<>();
    public static Map<String, Integer> reverseparameters = new HashMap<>();
    public static Map<Integer, String> songparameters = new HashMap<>();
    public static Map<String, Integer> reversesongparameters = new HashMap<>();
    public static Map<Integer, String> linkingparameters = new HashMap<>();
    public static Map<String, Integer> reverselinkingparameters = new HashMap<>();
    public static String[] def = new String[]{"0", "0", "0", "0", "0", "YouWillNeverGuessThis", "and", "0,0,0,0,0,0", "60",
            "minecraft", "_", "16", "false", "100", "100", "100",
            "false", "0", "minecraft", "true", "true", "0", "0", "nope",
            "nope", "-111", "false", "nope","_", "true"};
    public static Map<Integer, String> defparameters = new HashMap<>();

    public static void init() {
        parameters.put(0, "priority");
        parameters.put(1, "fade");
        parameters.put(2, "level");
        parameters.put(3, "time");
        parameters.put(4, "delay");
        parameters.put(5, "advancement");
        parameters.put(6, "operator");
        parameters.put(7, "zone");
        parameters.put(8, "start");
        parameters.put(9, "resource_name");
        parameters.put(10, "identifier");
        parameters.put(11, "detection_range");
        parameters.put(12, "mob_targetting");
        parameters.put(13, "horde_targetting_percentage");
        parameters.put(14, "health");
        parameters.put(15, "horde_health_percentage");
        parameters.put(16, "victory");
        parameters.put(17, "victory_id");
        parameters.put(18, "infernal");
        parameters.put(19, "whitelist");
        parameters.put(20, "sky_light");
        parameters.put(21, "phase");
        parameters.put(22, "victory_timeout");
        parameters.put(23, "biome_category");
        parameters.put(24, "rain_type");
        parameters.put(25, "biome_temperature");
        parameters.put(26, "biome_cold");
        parameters.put(27, "mob_nbt");
        parameters.put(28, "is_underground");
        songparameters.put(0, "pitch");
        songparameters.put(1, "play_once");
        songparameters.put(2, "must_finish");
        songparameters.put(3, "chance");
        songparameters.put(4, "volume");
        linkingparameters.put(0, "pitch");
        linkingparameters.put(1, "volume");
        reverseparameters.put("priority", 0);
        reverseparameters.put("fade", 1);
        reverseparameters.put("level", 2);
        reverseparameters.put("time", 3);
        reverseparameters.put("delay", 4);
        reverseparameters.put("advancement", 5);
        reverseparameters.put("operator", 6);
        reverseparameters.put("zone", 7);
        reverseparameters.put("start", 8);
        reverseparameters.put("resource_name", 9);
        reverseparameters.put("identifier", 10);
        reverseparameters.put("detection_range", 11);
        reverseparameters.put("mob_targetting", 12);
        reverseparameters.put("horde_targetting_percentage", 13);
        reverseparameters.put("health", 14);
        reverseparameters.put("horde_health_percentage", 15);
        reverseparameters.put("victory", 16);
        reverseparameters.put("victory_id", 17);
        reverseparameters.put("infernal", 18);
        reverseparameters.put("whitelist", 19);
        reverseparameters.put("sky_light", 20);
        reverseparameters.put("phase", 21);
        reverseparameters.put("victory_timeout", 22);
        reverseparameters.put("biome_category", 23);
        reverseparameters.put("rain_type", 24);
        reverseparameters.put("biome_temperature", 25);
        reverseparameters.put("biome_cold", 26);
        reverseparameters.put("mob_nbt", 27);
        reverseparameters.put("is_underground", 28);
        reversesongparameters.put("pitch", 0);
        reversesongparameters.put("play_once", 1);
        reversesongparameters.put("must_finish", 2);
        reversesongparameters.put("chance", 3);
        reversesongparameters.put("volume", 4);
        reverselinkingparameters.put("pitch", 0);
        reverselinkingparameters.put("volume", 1);
        for(int i=0;i<def.length;i++) {
            defparameters.put(i,def[i]);
        }
    }

    public static List<String> convertList(List<Integer> ints) {
        List<String> ret = new ArrayList<>();
        for(int i : ints) {
            ret.add(parameters.get(i));
        }
        return ret;
    }

    public static List<Integer> buildGuiParameters(String trigger) {
        List<Integer> ret = new ArrayList<>();
        if(trigger!=null) {
            switch (trigger) {
                case "generic":
                    ret.add(1);
                    ret.add(4);
                    return ret;
                case "difficulty":
                case "rainintensity":
                    ret.add(0);
                    ret.add(1);
                    ret.add(2);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(10);
                    return ret;
                case "time":
                    ret.add(0);
                    ret.add(1);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(8);
                    ret.add(10);
                    ret.add(21);
                    return ret;
                case "light":
                    ret.add(0);
                    ret.add(1);
                    ret.add(2);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(10);
                    ret.add(20);
                    return ret;
                case "height":
                    ret.add(0);
                    ret.add(1);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(10);
                    ret.add(28);
                    return ret;
                case "raining":
                case "storming":
                case "snowing":
                case "dead":
                case "creative":
                case "spectator":
                case "pet":
                case "underwater":
                case "drowning":
                case "bloodmoon":
                case "harvestmoon":
                case "fallingstars":
                    ret.add(0);
                    ret.add(1);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    return ret;
                case "lowhp":
                    ret.add(0);
                    ret.add(1);
                    ret.add(2);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    return ret;
                case "riding":
                case "dimension":
                case "structure":
                case "gui":
                case "effect":
                    ret.add(0);
                    ret.add(1);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(9);
                    ret.add(10);
                    return ret;
                case "elytra":
                case "fishing":
                    ret.add(0);
                    ret.add(1);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(8);
                    return ret;
                case "home":
                case "hurricane":
                case "sandstorm":
                    ret.add(0);
                    ret.add(1);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(11);
                    return ret;
                case "biome":
                    ret.add(0);
                    ret.add(1);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(9);
                    ret.add(10);
                    ret.add(23);
                    ret.add(24);
                    ret.add(25);
                    ret.add(26);
                    return ret;
                case "mob":
                    ret.add(0);
                    ret.add(1);
                    ret.add(2);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(9);
                    ret.add(10);
                    ret.add(11);
                    ret.add(12);
                    ret.add(13);
                    ret.add(14);
                    ret.add(15);
                    ret.add(16);
                    ret.add(17);
                    ret.add(18);
                    ret.add(22);
                    ret.add(27);
                    return ret;
                case "victory":
                    ret.add(0);
                    ret.add(1);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(10);
                    ret.add(17);
                    return ret;
                case "zones":
                    ret.add(0);
                    ret.add(1);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(7);
                    ret.add(10);
                    return ret;
                case "pvp":
                    ret.add(0);
                    ret.add(1);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(10);
                    ret.add(17);
                    ret.add(22);
                    return ret;
                case "advancement":
                    ret.add(0);
                    ret.add(1);
                    ret.add(3);
                    ret.add(4);
                    ret.add(5);
                    ret.add(6);
                    ret.add(10);
                    return ret;
                case "gamestage":
                    ret.add(0);
                    ret.add(1);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(9);
                    ret.add(10);
                    ret.add(19);
                    return ret;
                case "tornado":
                    ret.add(0);
                    ret.add(1);
                    ret.add(2);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(10);
                    ret.add(11);
                    return ret;
                case "season":
                    ret.add(0);
                    ret.add(1);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(8);
                    ret.add(10);
                    return ret;
            }
        }
        return ret;
    }
}