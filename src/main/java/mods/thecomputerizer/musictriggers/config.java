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

    public static String[] Categories = {"Main Menu","Generic","Zones","Day","Night","Sunrise","Sunset","Light Level","Underground - underground with no sky visible","Deep Under - deep below the surface with no sky visible","Raining","Storming","Snowing","Low HP","Dead","Void","Spectator","Creative","Riding","Pet","High","Dimension","Biome","Structure","Mob (This works for both bosses and hordes!)","Gamestages (Only fires if the mod Game Stages is active)","Blood Moon (Only fires if the mod Enhanced Celestials is active)","Harvest Moon (Only fires if the mod Enhanced Celestials is active)","Blue Moon (Only fires if the mod Enhanced Celestials is active)"};
    public static Integer[] withPriority = {-1111,-1111,10000,1000,900,1111,1111,500,1500,2000,1300,1350,1333,3000,10000,7777,5000,5000,2222,1200,1200,1150,1160,3333,3500,500,1200,1400,1400};
    public static Integer[] withFade = {-1,0,0,0,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-1,-1,-1,-1,-1,0,0,0};
    public static Integer[] withLevel = {9999,9999,9999,9999,9999,9999,9999,7,55,20,9999,9999,9999,30,9999,0,9999,9999,9999,9999,9999,9999,9999,9999,9999,9999,9999,9999,9999};

    public static HashMap<Integer, TriggerData> readTriggers = new HashMap<>();
    public static int triggerCounter = 0;

    public static int universalDelay;

    public static List<String> menuSongs;

    public static int genericFade;
    public static List<String> genericSongs;

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

    public static int dimensionPriority;
    public static List<String> dimensionSongs;

    public static int biomePriority;
    public static List<String> biomeSongs;

    public static int structurePriority;
    public static List<String> structureSongs;

    public static int mobPriority;
    public static List<String> mobSongs;

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


    public static void build(File f) {
        fb.add("All event Triggers");
        fb.add("");
        fb.add("\tUniversal Delay=0");
        fb.add("");
        for(int i=0;i< Categories.length;i++) {
            fb.add("\t"+Categories[i]);
            if(withPriority[i]!=-1111) {
                fb.add("\t\tPriority [min: -99, max: 2147483647 default: "+withPriority[i]+"]");
                fb.add("\t\tPriority Value="+withPriority[i]);
                fb.add("");
            }
            if(withFade[i]!=-1) {
                fb.add("\t\tFade Time [in ticks, default: "+withFade[i]+"]");
                fb.add("\t\tFade Value="+withFade[i]);
                fb.add("");
            }
            if(withLevel[i]!=9999) {
                if(!Categories[i].matches("Low HP")) {
                    fb.add("\t\tConfigurable Level [Y level to activate, default: " + withLevel[i] + "]");
                    fb.add("\t\tLevel Value=" + withLevel[i]);
                    fb.add("");
                }
                else {
                    fb.add("\t\tPercentage of maximum health [Out of 100, default: " + withLevel[i] + "]");
                    fb.add("\t\tLevel Value=" + withLevel[i]);
                    fb.add("");
                }
            }
            if(Categories[i].matches("Night")) {
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
            }
            else if(Categories[i].matches("Dimension")) {
                fb.add("\t\tSongs per dimension [Format: dimensionID,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]");
                fb.add("\t\tNote: You only have to set the priority per dimension ID for 1 song");
                fb.add("\t\tExample: -1,(songname),11111");
                fb.add("\t\tSongs=<\n\t\t>");
                fb.add("");
            }
            else if(Categories[i].matches("Biome")) {
                fb.add("\t\tSongs per biome [Format: \"biomeresourcename,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]");
                fb.add("\t\tNote: You only have to set the priority per biome name for 1 song");
                fb.add("\t\tExample: minecraft:swampland,(songname),11111");
                fb.add("\t\tSongs=<\n>");
                fb.add("");
            }
            else if(Categories[i].matches("Structure")) {
                fb.add("\t\tSongs per structure [Format: \"structurename,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]");
                fb.add("\t\tNote: You only have to set the priority per structure name for 1 song");
                fb.add("\t\tExample: Fortress,(songname),11111");
                fb.add("\t\tSongs=<\n\t\t>");
                fb.add("");
            }
            else if(Categories[i].matches("Mob (This works for both bosses and hordes!)")) {
                fb.add("\t\tSongs Per mob [Format: \"MobName,number of mobs,SongName,(Optional)detection range[min: 1, max: 1000, default: 16],(Optional)Priority:[min: -99, max: 2147483647],(Optional)Fade Time:[in ticks, default: 0]\"]");
                fb.add("\t\tNote: You only have to set the priority per mob name for 1 song");
                fb.add("\t\tAdditional Note: Putting high numbers for the mob range will cause lag! The higher it is, the more noticable that will be. Only use higher numbers for a boss that could be far away, like the Ender Dragon");
                fb.add("\t\tExample: Zombie,8,(songname),16,11111");
                fb.add("\t\tSpecial case - If you put \"MOB\" as the mob ID, it will default to any hostile mob");
                fb.add("\t\tSongs=<\n\t\t>");
                fb.add("");
            }
            else if(Categories[i].matches("Gamestages (Only fires if the mod Game Stages is active)")) {
                fb.add("\t\tSongs Per Gamestage [Format: \"StageName,whitelist,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]");
                fb.add("\t\tNote: You only have to set the priority per gamestage name for 1 song");
                fb.add("\t\tExample: StageOne,true,(songname),11111 - This will play when the player has the stage. If it were false it would play whenever the player does not have it.");
                fb.add("\t\tSongs=<\n\t\t>");
                fb.add("");
            }
            else {
                fb.add("\t\tSongs=<\n\t\t>");
                fb.add("");
            }
        }
        try {
            Files.write(Paths.get(f.getPath()),fb, StandardCharsets.UTF_8);
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
                for(int i=1;i<Categories.length;i++) {
                    if(line.contains("Universal Delay=")) {
                        universalDelay = Integer.parseInt(stringBreakerRegex(line.replaceAll(" ",""),"=")[1]);
                    }
                    if(line.contains(Categories[i])) {
                        readTriggers.put(triggerCounter,new TriggerData(p,fade,l,songs));
                        triggerCounter++;
                        songs = new ArrayList<>();
                        break;
                    }
                    if(line.contains("Priority Value=")) {
                        p = Integer.parseInt(stringBreakerRegex(line,"=")[1]);
                    }
                    if(line.contains("Fade Value=")) {
                        fade = Integer.parseInt(stringBreakerRegex(line,"=")[1]);
                    }
                    if(line.contains("Level Value=")) {
                        l = Integer.parseInt(stringBreakerRegex(line,"=")[1]);
                    }
                    if(line.contains("Songs=<")) {
                        songLines=true;
                    }
                    if(line.contains(">")) {
                        songLines=false;
                    }
                    if(songLines && !line.contains("Songs=<")) {
                        line = line.replaceAll(" ","");
                        line = line.replaceAll("\t","");
                        if(!songs.contains(line) && line.length()!=0) {
                            MusicTriggers.logger.info("The song "+line+" is being added to the current trigger!");
                            songs.add(line);
                        }
                    }
                }
            }
            readTriggers.put(triggerCounter,new TriggerData(p,fade,l,songs));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        menuSongs = readTriggers.get(0).getSongs();

        genericFade = readTriggers.get(1).getFade();
        genericSongs = readTriggers.get(1).getSongs();

        zonesPriority = readTriggers.get(2).getPriority();
        zonesFade = readTriggers.get(2).getFade();
        zonesSongs = readTriggers.get(2).getSongs();

        dayPriority = readTriggers.get(3).getPriority();
        dayFade = readTriggers.get(3).getFade();
        daySongs = readTriggers.get(3).getSongs();

        nightPriority = readTriggers.get(4).getPriority();
        nightSongs = readTriggers.get(4).getSongs();

        sunrisePriority = readTriggers.get(5).getPriority();
        sunriseFade = readTriggers.get(5).getFade();
        sunriseSongs = readTriggers.get(5).getSongs();

        sunsetPriority = readTriggers.get(6).getPriority();
        sunsetFade = readTriggers.get(6).getFade();
        sunsetSongs = readTriggers.get(6).getSongs();

        lightPriority = readTriggers.get(7).getPriority();
        lightFade = readTriggers.get(7).getFade();
        lightLevel = readTriggers.get(7).getLevel();
        lightSongs = readTriggers.get(7).getSongs();

        undergroundPriority = readTriggers.get(8).getPriority();
        undergroundFade = readTriggers.get(8).getFade();
        undergroundLevel = readTriggers.get(8).getLevel();
        undergroundSongs = readTriggers.get(8).getSongs();

        deepUnderPriority = readTriggers.get(9).getPriority();
        deepUnderFade = readTriggers.get(9).getFade();
        deepUnderLevel = readTriggers.get(9).getLevel();
        deepUnderSongs = readTriggers.get(9).getSongs();

        rainingPriority = readTriggers.get(10).getPriority();
        rainingFade = readTriggers.get(10).getFade();
        rainingSongs = readTriggers.get(10).getSongs();

        stormingPriority = readTriggers.get(11).getPriority();
        stormingFade = readTriggers.get(11).getFade();
        stormingSongs = readTriggers.get(11).getSongs();

        snowingPriority = readTriggers.get(12).getPriority();
        snowingFade = readTriggers.get(12).getFade();
        snowingSongs = readTriggers.get(12).getSongs();

        lowHPPriority = readTriggers.get(13).getPriority();
        lowHPFade = readTriggers.get(13).getFade();
        lowHPLevel = readTriggers.get(13).getLevel();
        lowHPSongs = readTriggers.get(13).getSongs();

        deadPriority = readTriggers.get(14).getPriority();
        deadFade = readTriggers.get(14).getFade();
        deadSongs = readTriggers.get(14).getSongs();

        inVoidPriority = readTriggers.get(15).getPriority();
        inVoidFade = readTriggers.get(15).getFade();
        inVoidLevel = readTriggers.get(15).getLevel();
        inVoidSongs = readTriggers.get(15).getSongs();

        spectatorPriority = readTriggers.get(16).getPriority();
        spectatorFade = readTriggers.get(16).getFade();
        spectatorSongs = readTriggers.get(16).getSongs();

        creativePriority = readTriggers.get(17).getPriority();
        creativeFade = readTriggers.get(17).getFade();
        creativeSongs = readTriggers.get(17).getSongs();

        ridingPriority = readTriggers.get(18).getPriority();
        ridingFade = readTriggers.get(18).getFade();
        ridingSongs = readTriggers.get(18).getSongs();

        petPriority = readTriggers.get(19).getPriority();
        petFade = readTriggers.get(19).getFade();
        petSongs = readTriggers.get(19).getSongs();

        highPriority = readTriggers.get(20).getPriority();
        highFade = readTriggers.get(20).getFade();
        highSongs = readTriggers.get(20).getSongs();

        dimensionPriority = readTriggers.get(21).getPriority();
        dimensionSongs = readTriggers.get(21).getSongs();
        
        biomePriority = readTriggers.get(22).getPriority();
        biomeSongs = readTriggers.get(22).getSongs();
        
        structurePriority = readTriggers.get(23).getPriority();
        structureSongs = readTriggers.get(23).getSongs();

        mobPriority = readTriggers.get(24).getPriority();
        mobSongs = readTriggers.get(24).getSongs();

        gamestagePriority = readTriggers.get(25).getPriority();
        gamestageSongs = readTriggers.get(25).getSongs();

        bloodmoonPriority = readTriggers.get(26).getPriority();
        bloodmoonFade = readTriggers.get(26).getFade();
        bloodmoonSongs = readTriggers.get(26).getSongs();

        harvestmoonPriority = readTriggers.get(27).getPriority();
        harvestmoonFade = readTriggers.get(27).getFade();
        harvestmoonSongs = readTriggers.get(27).getSongs();

        bluemoonPriority = readTriggers.get(28).getPriority();
        bluemoonFade = readTriggers.get(28).getFade();
        bluemoonSongs = readTriggers.get(28).getSongs();
        
    }

    public static String[] stringBreakerRegex(String s,String regex) {
        return s.split(regex);
    }

    private static class TriggerData {
        public int priority;
        public int fade;
        public int level;
        public List<String> songs;
        TriggerData(int p, int f, int l, List<String> s) {
            this.priority=p;
            this.fade=f;
            this.level=l;
            this.songs=s;
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
