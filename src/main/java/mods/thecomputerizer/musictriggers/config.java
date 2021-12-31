package mods.thecomputerizer.musictriggers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class config {
    public static List<String> fb = new ArrayList<>();

    public static String[] Categories = {"Main Menu", "Generic", "Difficulty", "Zones", "Day", "Night", "Sunrise", "Sunset", "Light Level", "Underground - underground with no sky visible", "Deep Under - deep below the surface with no sky visible", "Raining", "Storming", "Snowing", "Low HP", "Dead", "inVoid", "Spectator", "Creative", "Riding", "Pet", "High", "Underwater", "PVP", "Dimension", "Biome", "Structure", "Mob (This works for both bosses and hordes!)", "Effect (Trigger based on potion effects)", "Victory - This can only be called after the pvp or mob trigger", "Gui", "Gamestages (Only fires if the mod Game Stages is active)", "Blood Moon (Only fires if the mod Enhanced Celestials is active)", "Harvest Moon (Only fires if the mod Enhanced Celestials is active)", "Blue Moon (Only fires if the mod Enhanced Celestials is active)", "Rain Intensity (Only fires if the mod dynamic surroundings is active)", "Acid Rain (Only fires if the mod better weather is active)", "Blizzard (Only fires if the mod better weather is active)", "Cloudy (Only fires if the mod better weather is active)", "Light Rain (Only fires if the mod better weather is active)", "Seasons (Only fires if the mod serene season is active)"};
    public static Integer[] withPriority = {-1111, -1111, 100, 10000, 1000, 900, 1111, 1111, 500, 1500, 2000, 1300, 1350, 1333, 3000, 10000, 7777, 5000, 5000, 2222, 1200, 1200, 1999, 20000, 1150, 1160, 3333, 3500, 500, 20000, 13333, 500, 1200, 1400, 1400, 1349, 9999, 9999, 9999, 9999, 500};
    public static Integer[] withFade = {-1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static Integer[] withLevel = {9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 7, 55, 20, 9999, 9999, 9999, 30, 9999, 0, 9999, 9999, 9999, 9999, 150, 9999, 16, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999, 9999};

    public static HashMap<Integer, TriggerData> readTriggers = new HashMap<>();
    public static int triggerCounter = 0;

    public static int universalDelay;

    public static List<String> menuSongs;

    public static int genericFade;
    public static List<String> genericSongs;

    public static int difficultyPriority;
    public static List<String> difficultySongs;

    public static int zonesPriority;
    public static int zonesFade;
    public static List<String> zonesSongs;

    public static int dayPriority;
    public static int dayFade;
    public static List<String> daySongs;

    public static int nightPriority;
    public static List<String> nightSongs;

    public static int sunrisePriority;
    public static int sunriseFade;
    public static List<String> sunriseSongs;

    public static int sunsetPriority;
    public static int sunsetFade;
    public static List<String> sunsetSongs;

    public static int lightPriority;
    public static int lightFade;
    public static int lightLevel;
    public static List<String> lightSongs;

    public static int undergroundPriority;
    public static int undergroundFade;
    public static int undergroundLevel;
    public static List<String> undergroundSongs;

    public static int deepUnderPriority;
    public static int deepUnderFade;
    public static int deepUnderLevel;
    public static List<String> deepUnderSongs;

    public static int rainingPriority;
    public static int rainingFade;
    public static List<String> rainingSongs;

    public static int stormingPriority;
    public static int stormingFade;
    public static List<String> stormingSongs;

    public static int snowingPriority;
    public static int snowingFade;
    public static List<String> snowingSongs;

    public static int lowHPPriority;
    public static int lowHPFade;
    public static int lowHPLevel;
    public static List<String> lowHPSongs;

    public static int deadPriority;
    public static int deadFade;
    public static List<String> deadSongs;

    public static int inVoidPriority;
    public static int inVoidFade;
    public static int inVoidLevel;
    public static List<String> inVoidSongs;

    public static int spectatorPriority;
    public static int spectatorFade;
    public static List<String> spectatorSongs;

    public static int creativePriority;
    public static int creativeFade;
    public static List<String> creativeSongs;

    public static int ridingPriority;
    public static int ridingFade;
    public static List<String> ridingSongs;

    public static int petPriority;
    public static int petFade;
    public static List<String> petSongs;

    public static int highPriority;
    public static int highFade;
    public static int highLevel;
    public static List<String> highSongs;

    public static int underwaterPriority;
    public static int underwaterFade;
    public static List<String> underwaterSongs;

    public static int pvpPriority;
    public static int pvpFade;
    public static int pvpRange;
    public static int pvpTime;
    public static boolean pvpVictory;
    public static int pvpVictoryID;
    public static List<String> pvpSongs;

    public static int dimensionPriority;
    public static List<String> dimensionSongs;

    public static int biomePriority;
    public static List<String> biomeSongs;

    public static int structurePriority;
    public static List<String> structureSongs;

    public static int mobPriority;
    public static List<String> mobSongs;

    public static int effectPriority;
    public static List<String> effectSongs;

    public static int victoryPriority;
    public static List<String> victorySongs;

    public static int guiPriority;
    public static List<String> guiSongs;

    public static int gamestagePriority;
    public static List<String> gamestageSongs;

    public static int bloodmoonPriority;
    public static int bloodmoonFade;
    public static List<String> bloodmoonSongs;

    public static int harvestmoonPriority;
    public static int harvestmoonFade;
    public static List<String> harvestmoonSongs;

    public static int bluemoonPriority;
    public static int bluemoonFade;
    public static List<String> bluemoonSongs;

    public static int rainintensityPriority;
    public static int rainintensityFade;
    public static List<String> rainintensitySongs;

    public static int acidrainPriority;
    public static int acidrainFade;
    public static List<String> acidrainSongs;

    public static int blizzardPriority;
    public static int blizzardFade;
    public static List<String> blizzardSongs;

    public static int cloudyPriority;
    public static int cloudyFade;
    public static List<String> cloudySongs;

    public static int lightrainPriority;
    public static int lightrainFade;
    public static List<String> lightrainSongs;

    public static int seasonsPriority;
    public static List<String> seasonsSongs;

    public static void build(File f) {
        fb.add("All event Triggers");
        fb.add("");
        fb.add("\tUniversal Delay=0");
        fb.add("");
        for (int i = 0; i < Categories.length; i++) {
            fb.add("\t" + Categories[i]);
            if (withPriority[i] != -1111) {
                fb.add("\t\tPriority [min: -99, max: 2147483647 default: " + withPriority[i] + "]");
                fb.add("\t\tPriority Value=" + withPriority[i]);
                fb.add("");
            }
            if (withFade[i] != -1) {
                fb.add("\t\tFade Time [in ticks, default: " + withFade[i] + "]");
                fb.add("\t\tFade Value=" + withFade[i]);
                fb.add("");
            }
            if (withLevel[i] != 9999) {
                if (!Categories[i].matches("Low HP")) {
                    fb.add("\t\tConfigurable Level [Y level to activate, default: " + withLevel[i] + "]");
                    fb.add("\t\tLevel Value=" + withLevel[i]);
                    fb.add("");
                } else if (Categories[i].matches("PVP")) {
                    fb.add("\t\tConfigurable Range [default: " + withLevel[i] + "]");
                    fb.add("\t\tLevel Value=" + withLevel[i]);
                    fb.add("");
                } else {
                    fb.add("\t\tPercentage of maximum health [Out of 100, default: " + withLevel[i] + "]");
                    fb.add("\t\tLevel Value=" + withLevel[i]);
                    fb.add("");
                }
            }
            if (Categories[i].matches("PVP")) {
                fb.add("\t\tBattle Time [in ticks, default: 200]");
                fb.add("\t\tPersistence=200");
                fb.add("");
            }
            if (Categories[i].matches("PVP")) {
                fb.add("\t\tVictory - whether to activate the victory trigger [default: false]");
                fb.add("\t\tVictory=false");
                fb.add("");
            }
            if (Categories[i].matches("PVP")) {
                fb.add("\t\tVictory ID - ID of the victory to activate [default: 0]");
                fb.add("\t\tVictoryID=0");
                fb.add("");
            }
            if (Categories[i].matches("Zones")) {
                fb.add("\t\tSongs per zone");
                fb.add("\t\tFormat[min x,min y,min z,max x, max y,max z,songname,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]]");
                fb.add("\t\tExample: 0,0,0,100,100,100,home3,33981,200");
                if (!fb.contains("\t\tSongs=<\n\t\t>")) {
                    fb.add("\t\tSongs=<\n\t\t>");
                }
            }
            if (Categories[i].matches("Night")) {
                fb.add("\t\tSongs- Format: [song name,moon phase,(Optional)fade time [in ticks, default: 0]]");
                fb.add("\t\tMoon Phases: 1 - Full Moon, 2 - Waning Gibbous, 3 - Third Quarter, 4 - Waning Crescent");
                fb.add("\t\t5 - New Moon, 6 - Waxing Crescent, 7 - First Quarter, 8 - Waxing Gibbous");
                fb.add("\t\tYou can put 0 to ignore moon phase, or put multiple numbers for a song to be active during multiple phases");
                fb.add("\t\tExample 1: [nighttime,1] - This will only play during a full moon");
                fb.add("\t\tExample 2: [nighttime,2,3,4,6,7,8] - This will play every night except for full moons and new moons");
                fb.add("\t\tExample 3: [nighttime,0] - This will play whenever it is nighttime, just like the old version of this trigger");
                fb.add("\t\tNote - If the fade is not the last number it will not work properly");
                fb.add("\t\tSongs=<\n\t\t>");
                fb.add("");
            } else if (Categories[i].matches("Dimension")) {
                fb.add("\t\tSongs per dimension [Format: dimensionID,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]");
                fb.add("\t\tNote: You only have to set the priority per dimension ID for 1 song");
                fb.add("\t\tExample: minecraft:the_nether,(songname),11111");
                fb.add("\t\tSongs=<\n\t\t>");
                fb.add("");
            } else if (Categories[i].matches("Biome")) {
                fb.add("\t\tSongs Per Biome [Format: \"BiomeResourceName,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0],(Optional)Biome Time[default: 20]\"]");
                fb.add("\t\tNote: You only have to set the priority per biome name for 1 song");
                fb.add("\t\tExample: minecraft:swampland,(songname),11111");
                fb.add("\t\tAdditional Note: You can also specify multiple biomes at once through regexing! You can use this feature for both mod ids and biome names");
                fb.add("\t\tExample 2: minecraft,(songname),11111 (all minecraft biomes will have (songname))");
                fb.add("\t\tExample 3: forest,(songname),11111 (all biomes with \"forest\" in the name will have (songname))");
                fb.add("\t\tFinal Note: The biome time will allow the trigger to persist after leaving the specified biome for that amount of time");
                fb.add("\t\tFull Scale Example: swamp,(songname),11111,50,30");
                fb.add("\t\tSongs=<\n\t\t>");
                fb.add("");
            } else if (Categories[i].matches("Structure")) {
                fb.add("\t\tSongs per structure [Format: \"structurename,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]");
                fb.add("\t\tNote: You only have to set the priority per structure name for 1 song");
                fb.add("\t\tExample: Fortress,(songname),11111");
                fb.add("\t\tSongs=<\n\t\t>");
                fb.add("");
            } else if (Categories[i].contains("This works for both bosses and hordes!")) {
                fb.add("\t\tSongs Per Mob [Format: \"MobName,number of mobs,SongName,(Optional)detection range[min: 1, max: 1000, default: 16],(Optional)Priority:[min: -99, max: 2147483647],(Optional)Fade Time:[in ticks, default: 0],(Optional)Targetting[default: false],(Optional)Horde Targetting percentage[default: 100], (Optional)Health[default: 100],(Optional)Horde Health Percentage[default: 100],(Optional)Battle Time[in ticks, default: 0],(Optional)Trigger Victory[default: false],(Optional)Victory ID[min:0, max:2147483647, default: 0](Optional)Infernal[only works with the mod infernal mobs active]\"]");
                fb.add("\t\tNote: You only have to set the priority per mob name for 1 song");
                fb.add("\t\tAdditional Note: Putting high numbers (over 100) for the mob range may cause lag! The higher it is, the more noticable that lag will be. Only use higher numbers for a boss that could be far away, like the Ender Dragon");
                fb.add("\t\tAdditional Note: Targetting requires the mob(s) to be looking at you while horde targetting percentage is the total percentage of the number of mobs you set that have to be looking at you");
                fb.add("\t\tAdditional Note: Health requires the mob(s) to be below the set percentage threshold of health while horde health percentage is the total percentage of the number of mobs you set that have to be below the set percentage threshold of health");
                fb.add("\t\tAdditional Note: Battle time is how long the trigger will persist after the conditions are no longer met. Due to possible conflicts it may to better to leave this at 0");
                fb.add("\t\tAdditional Note: The victory trigger is special in that it can only activated after escaping the set trigger. The ID exists so there can multiple different victory scenarios");
                fb.add("\t\tFinal Note: The infernal trigger goes of of the mod name, which can be obtained via the debug info set by the debug config. Number of mobs will not affect this");
                fb.add("\t\tExample: Zombie,8,(songname),16,11111");
                fb.add("\t\tFull-Scale example: Skeleton,4,123486,50,true,50,80,25,0,Withering");
                fb.add("\t\tSpecial case - If you put \"MOB\" as the mob ID, it will default to any hostile mob");
                fb.add("\t\tSongs=<\n\t\t>");
                fb.add("");
            } else if (Categories[i].contains("Trigger based on potion effects")) {
                fb.add("\t\tSongs Per Effect [Format: \"EffectName,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]");
                fb.add("\t\tNote: You only have to set the priority per effect name for 1 song");
                fb.add("\t\tExample: effect.regeneration,(songname),11111");
                fb.add("\t\tSongs=<\n\t\t>");
                fb.add("");
            } else if (Categories[i].contains("Victory - This can only be called after the pvp or mob trigger")) {
                fb.add("\t\tSongs - [Format: \"SongName,Victory ID,(Optional)Victory Time[default: 200],(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]");
                fb.add("\t\tNote - The victory time is how long the victory trigger will last for");
                fb.add("\t\tAdditional Note: Dying will cut the trigger short");
                fb.add("\t\tExample: enderdragonwin,11,300,9999999,20");
                fb.add("\t\tSongs=<\n\t\t>");
                fb.add("");
            } else if (Categories[i].contains("Gui")) {
                fb.add("\t\tSongs - [Format: \"Gui Name,SongName,(Optional)Priority:[min: -99, max: 2147483647],(Optional)Fade Time:[in ticks, default: 0]\"]");
                fb.add("\t\tExample: net.minecraft.client.gui.screen.ChatScreen,inventory,67000");
                fb.add("\t\tNote: This can also be a regex");
                fb.add("\t\tExample 2: ChatScreen,inventory,67000");
                fb.add("\t\tSongs=<\n\t\t>");
                fb.add("");
            } else if (Categories[i].contains("Only fires if the mod Game Stages is active")) {
                fb.add("\t\tSongs Per Gamestage [Format: \"StageName,whitelist,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]");
                fb.add("\t\tNote: You only have to set the priority per gamestage name for 1 song");
                fb.add("\t\tExample: StageOne,true,(songname),11111 - This will play when the player has the stage. If it were false it would play whenever the player does not have it.");
                fb.add("\t\tSongs=<\n\t\t>");
                fb.add("");
            } else if (Categories[i].contains("Only fires if the mod dynamic surroundings is active")) {
                fb.add("\t\tSongs [Format: \"songname,Intensity Level (min: 0, max: 100)\"]");
                fb.add("\t\tNote - This trigger will play when the rain has a higher intensity than you put in");
                fb.add("\t\tExample: intenserain,70");
                fb.add("\t\tSongs=<\n\t\t>");
                fb.add("");
            } else if (Categories[i].contains("Only fires if the mod serene season is active")) {
                fb.add("\t\tSongs per seasons");
                fb.add("\t\tFormat[songname],season [int],(Optional)Priority:[min: -99, max: 2147483647],(Optional)Fade Time:[in ticks, default: 0]]");
                fb.add("\t\tNote - Spring=0 Summer=1 Fall=2 Winter=3");
                fb.add("\t\tExample spring,0,511,20");
                fb.add("\t\tSongs=<\n\t\t>");
                fb.add("");
            } else {
                fb.add("\t\tSongs=<\n\t\t>");
                fb.add("");
            }
        }
        try {
            Files.write(Paths.get(f.getPath()), fb, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void read(File f) {
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            int p = 1000;
            int fade = 0;
            int l = 0;
            List<String> songs = new ArrayList<>();
            boolean songLines = false;
            while ((line = br.readLine()) != null) {
                if (line.contains("Universal Delay=")) {
                    universalDelay = Integer.parseInt(stringBreakerRegex(line.replaceAll(" ", ""), "=")[1]);
                }
                for(int i=0;i<Categories.length;i++) {
                    if(line.contains(Categories[i]) && !songLines) {
                        triggerCounter=i;
                    }
                }
                if (line.contains("Priority Value=")) {
                    p = Integer.parseInt(stringBreakerRegex(line, "=")[1]);
                }
                if (line.contains("Fade Value=")) {
                    fade = Integer.parseInt(stringBreakerRegex(line, "=")[1]);
                }
                if (line.contains("Level Value=")) {
                    l = Integer.parseInt(stringBreakerRegex(line, "=")[1]);
                }
                if (line.contains("Persistence=")) {
                    pvpTime = Integer.parseInt(stringBreakerRegex(line, "=")[1]);
                }
                if (line.contains("Victory=")) {
                    pvpVictory = Boolean.parseBoolean(stringBreakerRegex(line, "=")[1]);
                }
                if (line.contains("VictoryID=")) {
                    pvpVictoryID = Integer.parseInt(stringBreakerRegex(line, "=")[1]);
                }
                if (line.contains("Songs=<")) {
                    songLines = true;
                }
                if (line.contains(">")) {
                    readTriggers.put(triggerCounter, new TriggerData(p, fade, l, songs));
                    songs = new ArrayList<>();
                    songLines = false;
                }
                if (songLines && !line.contains("Songs=<")) {
                    line = line.trim();
                    if (!songs.contains(line) && line.length() != 0) {
                        MusicTriggers.logger.info("The song " + line + " is being added to the current trigger category " + Categories[triggerCounter] + "!");
                        songs.add(line);
                    }
                }
            }
            readTriggers.put(triggerCounter, new TriggerData(p, fade, l, songs));
        } catch (Exception e) {
            e.printStackTrace();
        }

        menuSongs = accountForNullSongTriggerData(0);

        genericFade = accountForNullNumericalTriggerData(1,2);
        genericSongs = accountForNullSongTriggerData(1);
        
        difficultyPriority = accountForNullNumericalTriggerData(2,1);
        difficultySongs = accountForNullSongTriggerData(2);

        zonesPriority = accountForNullNumericalTriggerData(3,1);
        zonesFade = accountForNullNumericalTriggerData(3,2);
        zonesSongs = accountForNullSongTriggerData(3);

        dayPriority = accountForNullNumericalTriggerData(4,1);
        dayFade = accountForNullNumericalTriggerData(4,2);
        daySongs = accountForNullSongTriggerData(4);

        nightPriority = accountForNullNumericalTriggerData(5,1);
        nightSongs = accountForNullSongTriggerData(5);

        sunrisePriority = accountForNullNumericalTriggerData(6,1);
        sunriseFade = accountForNullNumericalTriggerData(6,2);
        sunriseSongs = accountForNullSongTriggerData(6);

        sunsetPriority = accountForNullNumericalTriggerData(7,1);
        sunsetFade = accountForNullNumericalTriggerData(7,2);
        sunsetSongs = accountForNullSongTriggerData(7);

        lightPriority = accountForNullNumericalTriggerData(8,1);
        lightFade = accountForNullNumericalTriggerData(8,2);
        lightLevel = accountForNullNumericalTriggerData(8,3);
        lightSongs = accountForNullSongTriggerData(8);

        undergroundPriority = accountForNullNumericalTriggerData(9,1);
        undergroundFade = accountForNullNumericalTriggerData(9,2);
        undergroundLevel = accountForNullNumericalTriggerData(9,3);
        undergroundSongs = accountForNullSongTriggerData(9);

        deepUnderPriority = accountForNullNumericalTriggerData(10,1);
        deepUnderFade = accountForNullNumericalTriggerData(10,2);
        deepUnderLevel = accountForNullNumericalTriggerData(10,3);
        deepUnderSongs = accountForNullSongTriggerData(10);

        rainingPriority = accountForNullNumericalTriggerData(11,1);
        rainingFade = accountForNullNumericalTriggerData(11,2);
        rainingSongs = accountForNullSongTriggerData(11);

        stormingPriority = accountForNullNumericalTriggerData(12,1);
        stormingFade = accountForNullNumericalTriggerData(12,2);
        stormingSongs = accountForNullSongTriggerData(12);

        snowingPriority = accountForNullNumericalTriggerData(13,1);
        snowingFade = accountForNullNumericalTriggerData(13,2);
        snowingSongs = accountForNullSongTriggerData(13);

        lowHPPriority = accountForNullNumericalTriggerData(14,1);
        lowHPFade = accountForNullNumericalTriggerData(14,2);
        lowHPLevel = accountForNullNumericalTriggerData(14,3);
        lowHPSongs = accountForNullSongTriggerData(14);

        deadPriority = accountForNullNumericalTriggerData(15,1);
        deadFade = accountForNullNumericalTriggerData(15,2);
        deadSongs = accountForNullSongTriggerData(15);

        inVoidPriority = accountForNullNumericalTriggerData(16,1);
        inVoidFade = accountForNullNumericalTriggerData(16,2);
        inVoidLevel = accountForNullNumericalTriggerData(16,3);
        inVoidSongs = accountForNullSongTriggerData(16);

        spectatorPriority = accountForNullNumericalTriggerData(17,1);
        spectatorFade = accountForNullNumericalTriggerData(17,2);
        spectatorSongs = accountForNullSongTriggerData(17);

        creativePriority = accountForNullNumericalTriggerData(18,1);
        creativeFade = accountForNullNumericalTriggerData(18,2);
        creativeSongs = accountForNullSongTriggerData(18);

        ridingPriority = accountForNullNumericalTriggerData(19,1);
        ridingFade = accountForNullNumericalTriggerData(19,2);
        ridingSongs = accountForNullSongTriggerData(19);

        petPriority = accountForNullNumericalTriggerData(20,1);
        petFade = accountForNullNumericalTriggerData(20,2);
        petSongs = accountForNullSongTriggerData(20);

        highPriority = accountForNullNumericalTriggerData(21,1);
        highFade = accountForNullNumericalTriggerData(21,2);
        highLevel = accountForNullNumericalTriggerData(21,3);
        highSongs = accountForNullSongTriggerData(21);

        underwaterPriority = accountForNullNumericalTriggerData(22,1);
        underwaterFade = accountForNullNumericalTriggerData(22,2);
        underwaterSongs = accountForNullSongTriggerData(22);

        pvpPriority = accountForNullNumericalTriggerData(23,1);
        pvpFade = accountForNullNumericalTriggerData(23,2);
        pvpRange = accountForNullNumericalTriggerData(23,3);
        pvpSongs = accountForNullSongTriggerData(23);

        dimensionPriority = accountForNullNumericalTriggerData(24,1);
        dimensionSongs = accountForNullSongTriggerData(24);

        biomePriority = accountForNullNumericalTriggerData(25,1);
        biomeSongs = accountForNullSongTriggerData(25);

        structurePriority = accountForNullNumericalTriggerData(26,1);
        structureSongs = accountForNullSongTriggerData(26);

        mobPriority = accountForNullNumericalTriggerData(27,1);
        mobSongs = accountForNullSongTriggerData(27);

        effectPriority = accountForNullNumericalTriggerData(28,1);
        effectSongs = accountForNullSongTriggerData(28);

        victoryPriority = accountForNullNumericalTriggerData(29,1);
        victorySongs = accountForNullSongTriggerData(29);

        guiPriority = accountForNullNumericalTriggerData(30,1);
        guiSongs = accountForNullSongTriggerData(30);

        gamestagePriority = accountForNullNumericalTriggerData(31,1);
        gamestageSongs = accountForNullSongTriggerData(31);

        bloodmoonPriority = accountForNullNumericalTriggerData(32,1);
        bloodmoonFade = accountForNullNumericalTriggerData(32,2);
        bloodmoonSongs = accountForNullSongTriggerData(32);

        harvestmoonPriority = accountForNullNumericalTriggerData(33,1);
        harvestmoonFade = accountForNullNumericalTriggerData(33,2);
        harvestmoonSongs = accountForNullSongTriggerData(33);

        bluemoonPriority = accountForNullNumericalTriggerData(34,1);
        bluemoonFade = accountForNullNumericalTriggerData(34,2);
        bluemoonSongs = accountForNullSongTriggerData(34);

        rainintensityPriority = accountForNullNumericalTriggerData(35,1);
        rainintensityFade = accountForNullNumericalTriggerData(35,2);
        rainintensitySongs = accountForNullSongTriggerData(35);

        acidrainPriority = accountForNullNumericalTriggerData(36,1);
        acidrainFade = accountForNullNumericalTriggerData(36,2);
        acidrainSongs = accountForNullSongTriggerData(36);

        blizzardPriority = accountForNullNumericalTriggerData(37,1);
        blizzardFade = accountForNullNumericalTriggerData(37,2);
        blizzardSongs = accountForNullSongTriggerData(37);

        cloudyPriority = accountForNullNumericalTriggerData(38,1);
        cloudyFade = accountForNullNumericalTriggerData(38,2);
        cloudySongs = accountForNullSongTriggerData(38);

        lightrainPriority = accountForNullNumericalTriggerData(39,1);
        lightrainFade = accountForNullNumericalTriggerData(39,2);
        lightrainSongs = accountForNullSongTriggerData(39);

        seasonsPriority = accountForNullNumericalTriggerData(40,1);
        seasonsSongs = accountForNullSongTriggerData(40);
    }

    public static void update(File f) {
        read(f);
        fb.add("All event Triggers");
        fb.add("");
        fb.add("\tUniversal Delay=" + universalDelay);
        fb.add("");
        for (int i = 0; i < Categories.length; i++) {
            fb.add("\t" + Categories[i]);
            if (withPriority[i] != -1111) {
                fb.add("\t\tPriority [min: -99, max: 2147483647 default: " + withPriority[i] + "]");
                fb.add("\t\tPriority Value=" + accountForNullNumericalTriggerData(i,1));
                fb.add("");
            }
            if (withFade[i] != -1) {
                fb.add("\t\tFade Time [in ticks, default: " + withFade[i] + "]");
                fb.add("\t\tFade Value=" + accountForNullNumericalTriggerData(i,2));
                fb.add("");
            }
            if (withLevel[i] != 9999) {
                if (!Categories[i].matches("Low HP")) {
                    fb.add("\t\tConfigurable Level [Y level to activate, default: " + withLevel[i] + "]");
                    fb.add("\t\tLevel Value=" + accountForNullNumericalTriggerData(i,3));
                    fb.add("");
                } else if (Categories[i].matches("PVP")) {
                    fb.add("\t\tConfigurable Range [default: " + withLevel[i] + "]");
                    fb.add("\t\tLevel Value=" + accountForNullNumericalTriggerData(i,3));
                    fb.add("");
                } else {
                    fb.add("\t\tPercentage of maximum health [Out of 100, default: " + withLevel[i] + "]");
                    fb.add("\t\tLevel Value=" + accountForNullNumericalTriggerData(i,3));
                    fb.add("");
                }
            }
            if (Categories[i].matches("PVP")) {
                fb.add("\t\tBattle Time [in ticks, default: 200]");
                fb.add("\t\tPersistence=" + pvpTime);
                fb.add("");
            }
            if (Categories[i].matches("PVP")) {
                fb.add("\t\tVictory - whether to activate the victory trigger [default: false]");
                fb.add("\t\tVictory=" + pvpVictory);
                fb.add("");
            }
            if (Categories[i].matches("PVP")) {
                fb.add("\t\tVictory ID - ID of the victory to activate [default: 0]");
                fb.add("\t\tVictoryID=" + pvpVictoryID);
                fb.add("");
            }
            if (Categories[i].matches("Zones")) {
                fb.add("\t\tSongs per zone");
                fb.add("\t\tFormat[min x,min y,min z,max x, max y,max z,songname,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]]");
                fb.add("\t\tExample: 0,0,0,100,100,100,home3,33981,200");
                if (!fb.contains("\t\tSongs=<")) {
                    fb.add("\t\tSongs=<");
                    List<String> songList = accountForNullSongTriggerData(i);
                    for (String iter : songList) {
                        fb.add("\t\t" + iter);
                    }
                    fb.add("\t\t>");
                }
                fb.add("");
            }
            if (Categories[i].matches("Night")) {
                fb.add("\t\tSongs- Format: [song name,moon phase,(Optional)fade time [in ticks, default: 0]]");
                fb.add("\t\tMoon Phases: 1 - Full Moon, 2 - Waning Gibbous, 3 - Third Quarter, 4 - Waning Crescent");
                fb.add("\t\t5 - New Moon, 6 - Waxing Crescent, 7 - First Quarter, 8 - Waxing Gibbous");
                fb.add("\t\tYou can put 0 to ignore moon phase, or put multiple numbers for a song to be active during multiple phases");
                fb.add("\t\tExample 1: [nighttime,1] - This will only play during a full moon");
                fb.add("\t\tExample 2: [nighttime,2,3,4,6,7,8] - This will play every night except for full moons and new moons");
                fb.add("\t\tExample 3: [nighttime,0] - This will play whenever it is nighttime, just like the old version of this trigger");
                fb.add("\t\tNote - If the fade is not the last number it will not work properly");
                fb.add("\t\tSongs=<");
                List<String> songList = accountForNullSongTriggerData(i);
                for (String iter : songList) {
                    fb.add("\t\t" + iter);
                }
                fb.add("\t\t>");
                fb.add("");
            } else if (Categories[i].matches("Dimension")) {
                fb.add("\t\tSongs per dimension [Format: dimensionID,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]");
                fb.add("\t\tNote: You only have to set the priority per dimension ID for 1 song");
                fb.add("\t\tExample: minecraft:the_nether,(songname),11111");
                fb.add("\t\tSongs=<");
                List<String> songList = accountForNullSongTriggerData(i);
                for (String iter : songList) {
                    fb.add("\t\t" + iter);
                }
                fb.add("\t\t>");
                fb.add("");
            } else if (Categories[i].matches("Biome")) {
                fb.add("\t\tSongs Per Biome [Format: \"BiomeResourceName,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0],(Optional)Biome Time[default: 20]\"]");
                fb.add("\t\tNote: You only have to set the priority per biome name for 1 song");
                fb.add("\t\tExample: minecraft:swampland,(songname),11111");
                fb.add("\t\tAdditional Note: You can also specify multiple biomes at once through regexing! You can use this feature for both mod ids and biome names");
                fb.add("\t\tExample 2: minecraft,(songname),11111 (all minecraft biomes will have (songname))");
                fb.add("\t\tExample 3: forest,(songname),11111 (all biomes with \"forest\" in the name will have (songname))");
                fb.add("\t\tFinal Note: The biome time will allow the trigger to persist after leaving the specified biome for that amount of time");
                fb.add("\t\tFull Scale Example: swamp,(songname),11111,50,30");
                fb.add("\t\tSongs=<");
                List<String> songList = accountForNullSongTriggerData(i);
                for (String iter : songList) {
                    fb.add("\t\t" + iter);
                }
                fb.add("\t\t>");
                fb.add("");
            } else if (Categories[i].matches("Structure")) {
                fb.add("\t\tSongs per structure [Format: \"structurename,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]");
                fb.add("\t\tNote: You only have to set the priority per structure name for 1 song");
                fb.add("\t\tExample: Fortress,(songname),11111");
                fb.add("\t\tSongs=<");
                List<String> songList = accountForNullSongTriggerData(i);
                for (String iter : songList) {
                    fb.add("\t\t" + iter);
                }
                fb.add("\t\t>");
                fb.add("");
            } else if (Categories[i].contains("This works for both bosses and hordes!")) {
                fb.add("\t\tSongs Per Mob [Format: \"MobName,number of mobs,SongName,(Optional)detection range[min: 1, max: 1000, default: 16],(Optional)Priority:[min: -99, max: 2147483647],(Optional)Fade Time:[in ticks, default: 0],(Optional)Targetting[default: false],(Optional)Horde Targetting percentage[default: 100], (Optional)Health[default: 100],(Optional)Horde Health Percentage[default: 100],(Optional)Battle Time[in ticks, default: 0],(Optional)Trigger Victory[default: false],(Optional)Victory ID[min:0, max:2147483647, default: 0](Optional)Infernal[only works with the mod infernal mobs active]\"]");
                fb.add("\t\tNote: You only have to set the priority per mob name for 1 song");
                fb.add("\t\tAdditional Note: Putting high numbers (over 100) for the mob range may cause lag! The higher it is, the more noticable that lag will be. Only use higher numbers for a boss that could be far away, like the Ender Dragon");
                fb.add("\t\tAdditional Note: Targetting requires the mob(s) to be looking at you while horde targetting percentage is the total percentage of the number of mobs you set that have to be looking at you");
                fb.add("\t\tAdditional Note: Health requires the mob(s) to be below the set percentage threshold of health while horde health percentage is the total percentage of the number of mobs you set that have to be below the set percentage threshold of health");
                fb.add("\t\tAdditional Note: Battle time is how long the trigger will persist after the conditions are no longer met. Due to possible conflicts it may to better to leave this at 0");
                fb.add("\t\tAdditional Note: The victory trigger is special in that it can only activated after escaping the set trigger. The ID exists so there can multiple different victory scenarios");
                fb.add("\t\tFinal Note: The infernal trigger goes of of the mod name, which can be obtained via the debug info set by the debug config. Number of mobs will not affect this");
                fb.add("\t\tExample: Zombie,8,(songname),16,11111");
                fb.add("\t\tFull-Scale example: Skeleton,4,123486,50,true,50,80,25,0,Withering");
                fb.add("\t\tSpecial case - If you put \"MOB\" as the mob ID, it will default to any hostile mob");
                fb.add("\t\tSongs=<");
                List<String> songList = accountForNullSongTriggerData(i);
                for (String iter : songList) {
                    fb.add("\t\t" + iter);
                }
                fb.add("\t\t>");
                fb.add("");
            } else if (Categories[i].contains("Trigger based on potion effects")) {
                fb.add("\t\tSongs Per Effect [Format: \"EffectName,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]");
                fb.add("\t\tNote: You only have to set the priority per effect name for 1 song");
                fb.add("\t\tExample: effect.regeneration,(songname),11111");
                fb.add("\t\tSongs=<");
                List<String> songList = accountForNullSongTriggerData(i);
                for (String iter : songList) {
                    fb.add("\t\t" + iter);
                }
                fb.add("\t\t>");
                fb.add("");
            } else if (Categories[i].contains("Victory - This can only be called after the pvp or mob trigger")) {
                fb.add("\t\tSongs - [Format: \"SongName,Victory ID,(Optional)Victory Time[default: 200],(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]");
                fb.add("\t\tNote - The victory time is how long the victory trigger will last for");
                fb.add("\t\tAdditional Note: Dying will cut the trigger short");
                fb.add("\t\tExample: enderdragonwin,11,300,9999999,20");
                fb.add("\t\tSongs=<");
                List<String> songList = accountForNullSongTriggerData(i);
                for (String iter : songList) {
                    fb.add("\t\t" + iter);
                }
                fb.add("\t\t>");
                fb.add("");
            } else if (Categories[i].contains("Gui")) {
                fb.add("Songs - [Format: \"Gui Name,SongName,(Optional)Priority:[min: -99, max: 2147483647],(Optional)Fade Time:[in ticks, default: 0]\"]");
                fb.add("Example: net.minecraft.client.gui.inventory.GuiInventory,inventory,67000");
                fb.add("Note: This can also be a regex");
                fb.add("Example 2: GuiInventory,inventory,67000");
                fb.add("\t\tSongs=<");
                List<String> songList = accountForNullSongTriggerData(i);
                for (String iter : songList) {
                    fb.add("\t\t" + iter);
                }
                fb.add("\t\t>");
                fb.add("");
            } else if (Categories[i].contains("Only fires if the mod Game Stages is active")) {
                fb.add("\t\tSongs Per Gamestage [Format: \"StageName,whitelist,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]");
                fb.add("\t\tNote: You only have to set the priority per gamestage name for 1 song");
                fb.add("\t\tExample: StageOne,true,(songname),11111 - This will play when the player has the stage. If it were false it would play whenever the player does not have it.");
                fb.add("\t\tSongs=<");
                List<String> songList = accountForNullSongTriggerData(i);
                for (String iter : songList) {
                    fb.add("\t\t" + iter);
                }
                fb.add("\t\t>");
                fb.add("");
            } else if (Categories[i].contains("Only fires if the mod dynamic surroundings is active")) {
                fb.add("\t\tSongs [Format: \"songname,Intensity Level (min: 0, max: 100)\"]");
                fb.add("\t\tNote - This trigger will play when the rain has a higher intensity than you put in");
                fb.add("\t\tExample: intenserain,70");
                fb.add("\t\tSongs=<");
                List<String> songList = accountForNullSongTriggerData(i);
                for (String iter : songList) {
                    fb.add("\t\t" + iter);
                }
                fb.add("\t\t>");
                fb.add("");
            } else if (Categories[i].contains("Only fires if the mod serene season is active")) {
                fb.add("\t\tSongs per seasons");
                fb.add("\t\tFormat[songname],season [int],(Optional)Priority:[min: -99, max: 2147483647],(Optional)Fade Time:[in ticks, default: 0]]");
                fb.add("\t\tNote - Spring=0 Summer=1 Fall=2 Winter=3");
                fb.add("\t\tExample spring,0,511,20");
                fb.add("\t\tSongs=<");
                List<String> songList = accountForNullSongTriggerData(i);
                for (String iter : songList) {
                    fb.add("\t\t" + iter);
                }
                fb.add("\t\t>");
                fb.add("");
            } else {
                fb.add("\t\tSongs=<");
                List<String> songList = accountForNullSongTriggerData(i);
                for (String iter : songList) {
                    fb.add("\t\t" + iter);
                }
                fb.add("\t\t>");
                fb.add("");
            }
        }
        try {
            Files.delete(Paths.get(f.getPath()));
            Files.write(Paths.get(f.getPath()), fb, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String[] stringBreakerRegex(String s, String regex) {
        return s.split(regex);
    }

    private static int accountForNullNumericalTriggerData(int index, int selector) {
        if(readTriggers.get(index)!=null) {
            if(selector==1) {return readTriggers.get(index).getPriority();}
            else if(selector==2) {return readTriggers.get(index).getFade();}
            else {return readTriggers.get(index).getLevel();}
        }
        else {
            if(selector==1) {return withPriority[index];}
            else if(selector==2) {return 0;}
            else {return withLevel[index];}
        }
    }

    private static List<String> accountForNullSongTriggerData(int index) {
        if(readTriggers.get(index)!=null) {return readTriggers.get(index).getSongs();}
        return new ArrayList<>();
    }

    private static class TriggerData {
        public int priority;
        public int fade;
        public int level;
        public List<String> songs;

        TriggerData(int p, int f, int l, List<String> s) {
            this.priority = p;
            this.fade = f;
            this.level = l;
            this.songs = s;
        }

        private int getPriority() {
            return this.priority;
        }

        private int getFade() {
            return this.fade;
        }

        private int getLevel() {
            return this.level;
        }

        private List<String> getSongs() {
            return this.songs;
        }
    }
}
