package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.config.ConfigTransitions;

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
            "nope", "-111", "false","_", "true", "-1", "-111", "true",
            "false", "false", "false", "0"};
    public static Map<Integer, String> defparameters = new HashMap<>();

    public static void init() {
        parameters.put(0, "priority");
        parameters.put(1, "fade_in");
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
        parameters.put(12, "mob_targeting");
        parameters.put(13, "horde_targeting_percentage");
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
        parameters.put(29, "end");
        parameters.put(30, "biome_rainfall");
        parameters.put(31, "biome_rainfall_higher");
        parameters.put(32, "is_instantiated");
        parameters.put(33, "time_switch");
        parameters.put(34, "remove_inactive_playable");
        parameters.put(35, "fade_out");
        parameters.put(36, "mob_champion");
        parameters.put(37, "toggled");
        songparameters.put(0, "pitch");
        songparameters.put(1, "play_once");
        songparameters.put(2, "must_finish");
        songparameters.put(3, "chance");
        songparameters.put(4, "volume");
        songparameters.put(5, "fade_in");
        songparameters.put(6, "fade_out");
        linkingparameters.put(0, "pitch");
        linkingparameters.put(1, "volume");
        linkingparameters.put(2, "fade_in");
        linkingparameters.put(3, "fade_out");
        reverseparameters.put("priority", 0);
        reverseparameters.put("fade_in", 1);
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
        reverseparameters.put("mob_targeting", 12);
        reverseparameters.put("horde_targeting_percentage", 13);
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
        reverseparameters.put("end", 29);
        reverseparameters.put("biome_rainfall", 30);
        reverseparameters.put("biome_rainfall_higher", 31);
        reverseparameters.put("is_instantiated", 32);
        reverseparameters.put("time_switch", 33);
        reverseparameters.put("remove_inactive_playable", 34);
        reverseparameters.put("fade_out", 35);
        reverseparameters.put("mob_champion", 36);
        reverseparameters.put("toggled",37);
        reversesongparameters.put("pitch", 0);
        reversesongparameters.put("play_once", 1);
        reversesongparameters.put("must_finish", 2);
        reversesongparameters.put("chance", 3);
        reversesongparameters.put("volume", 4);
        reversesongparameters.put("fade_in", 5);
        reversesongparameters.put("fade_out", 6);
        reverselinkingparameters.put("pitch", 0);
        reverselinkingparameters.put("volume", 1);
        reverselinkingparameters.put("fade_in", 2);
        reverselinkingparameters.put("fade_out", 3);
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
        if (trigger != null) {
            switch (trigger) {
                case "generic":
                    ret.add(1);
                    ret.add(4);
                    ret.add(35);
                    return ret;
                case "difficulty":
                case "rainintensity":
                case "season":
                    ret.add(0);
                    ret.add(1);
                    ret.add(2);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(10);
                    ret.add(32);
                    ret.add(33);
                    ret.add(34);
                    ret.add(35);
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
                    ret.add(29);
                    ret.add(32);
                    ret.add(33);
                    ret.add(34);
                    ret.add(35);
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
                    ret.add(32);
                    ret.add(33);
                    ret.add(34);
                    ret.add(35);
                    return ret;
                case "height":
                    ret.add(0);
                    ret.add(1);
                    ret.add(2);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(10);
                    ret.add(28);
                    ret.add(32);
                    ret.add(33);
                    ret.add(34);
                    ret.add(35);
                    return ret;
                case "raining":
                case "storming":
                case "snowing":
                case "dead":
                case "creative":
                case "spectator":
                case "pet":
                case "bloodmoon":
                case "harvestmoon":
                case "fallingstars":
                    ret.add(0);
                    ret.add(1);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(32);
                    ret.add(33);
                    ret.add(34);
                    ret.add(35);
                    return ret;
                case "drowning":
                case "lowhp":
                    ret.add(0);
                    ret.add(1);
                    ret.add(2);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(32);
                    ret.add(33);
                    ret.add(34);
                    ret.add(35);
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
                    ret.add(32);
                    ret.add(33);
                    ret.add(34);
                    ret.add(35);
                    return ret;
                case "elytra":
                case "fishing":
                case "underwater":
                    ret.add(0);
                    ret.add(1);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(8);
                    ret.add(32);
                    ret.add(33);
                    ret.add(34);
                    ret.add(35);
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
                    ret.add(32);
                    ret.add(33);
                    ret.add(34);
                    ret.add(35);
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
                    ret.add(30);
                    ret.add(31);
                    ret.add(32);
                    ret.add(33);
                    ret.add(34);
                    ret.add(35);
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
                    ret.add(32);
                    ret.add(33);
                    ret.add(34);
                    ret.add(35);
                    return ret;
                case "victory":
                    ret.add(0);
                    ret.add(1);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(10);
                    ret.add(17);
                    ret.add(32);
                    ret.add(33);
                    ret.add(34);
                    ret.add(35);
                    return ret;
                case "zones":
                    ret.add(0);
                    ret.add(1);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(7);
                    ret.add(10);
                    ret.add(32);
                    ret.add(33);
                    ret.add(34);
                    ret.add(35);
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
                    ret.add(32);
                    ret.add(33);
                    ret.add(34);
                    ret.add(35);
                    return ret;
                case "advancement":
                    ret.add(0);
                    ret.add(1);
                    ret.add(3);
                    ret.add(4);
                    ret.add(5);
                    ret.add(6);
                    ret.add(10);
                    ret.add(32);
                    ret.add(33);
                    ret.add(34);
                    ret.add(35);
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
                    ret.add(32);
                    ret.add(33);
                    ret.add(34);
                    ret.add(35);
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
                    ret.add(32);
                    ret.add(33);
                    ret.add(34);
                    ret.add(35);
                    return ret;
                case "statistic":
                    ret.add(0);
                    ret.add(1);
                    ret.add(2);
                    ret.add(3);
                    ret.add(4);
                    ret.add(6);
                    ret.add(9);
                    ret.add(10);
                    ret.add(32);
                    ret.add(33);
                    ret.add(34);
                    ret.add(35);
                    return ret;
            }
        }
        return ret;
    }

    public static void buildTitleOutputForGuiFromIndex(ConfigTransitions.Title title, StringBuilder builder, List<Integer> parameters) {
        builder.append("\ttitle = [ ");
        for(String t : title.getTitles()) {
            builder.append("\"").append(t).append("\" ");
        }
        builder.append("]\n");
        builder.append("\tsubtitle = [ ");
        for(String s : title.getSubTitles()) {
            builder.append("\"").append(s).append("\" ");
        }
        builder.append("]\n");
        for(int i : parameters) {
            buildIndividualTitleOutputForGuiFromIndex(title,builder,i);
        }
    }

    private static void buildIndividualTitleOutputForGuiFromIndex(ConfigTransitions.Title title, StringBuilder builder, int index) {
        switch (index) {
            case 0:
                builder.append("\tplay_once = \"").append(title.getPlayonce()).append("\"\n");
                return;
            case 1:
                builder.append("\ttitle_color = \"").append(title.getTitlecolor()).append("\"\n");
                return;
            case 2:
                builder.append("\tsubtitle_color = \"").append(title.getSubtitlecolor()).append("\"\n");
                return;
            case 3:
                builder.append("\tvague = \"").append(title.getVague()).append("\"\n");
        }
    }

    public static void buildImageOutputForGuiFromIndex(ConfigTransitions.Image image, StringBuilder builder, List<Integer> parameters, boolean ismoving) {
        for(int i : parameters) {
            buildIndividualStaticImageOutputForGuiFromIndex(image,builder,i);
        }
        if(ismoving) {
            builder.append("\t[image.animation]\n");
            for(int i : parameters) {
                buildIndividualMovingImageOutputForGuiFromIndex(image,builder,i);
            }
        }
    }

    private static void buildIndividualStaticImageOutputForGuiFromIndex(ConfigTransitions.Image image, StringBuilder builder, int index) {
        if(index<=9) {
            switch (index) {
                case 0:
                    builder.append("\tname = \"").append(image.getName()).append("\"\n");
                    return;
                case 1:
                    builder.append("\ttime = \"").append(image.getTime()).append("\"\n");
                    return;
                case 2:
                    builder.append("\tvertical = \"").append(image.getVertical()).append("\"\n");
                    return;
                case 3:
                    builder.append("\thorizontal = \"").append(image.getHorizontal()).append("\"\n");
                    return;
                case 4:
                    builder.append("\tscale_x = \"").append(image.getScaleX()).append("\"\n");
                    return;
                case 5:
                    builder.append("\tscale_y = \"").append(image.getScaleY()).append("\"\n");
                    return;
                case 6:
                    builder.append("\tplay_once = \"").append(image.getPlayonce()).append("\"\n");
                    return;
                case 7:
                    builder.append("\tfade_in = \"").append(image.getFadeIn()).append("\"\n");
                    return;
                case 8:
                    builder.append("\tfade_out = \"").append(image.getFadeOut()).append("\"\n");
                    return;
                case 9:
                    builder.append("\tvague = \"").append(image.getVague()).append("\"\n");
            }
        }
    }

    private static void buildIndividualMovingImageOutputForGuiFromIndex(ConfigTransitions.Image image, StringBuilder builder, int index) {
        if(index>9) {
            switch (index) {
                case 10:
                    //builder.append("\t\tdelay = \"").append(image.getDelay()).append("\"\n");
                    return;
                case 11:
                    builder.append("\t\tsplit = \"").append(image.getSplit()).append("\"\n");
                    return;
                case 12:
                    builder.append("\t\tframes_skipped = \"").append(image.getSkip()).append("\"\n");
            }
        }
    }
}
