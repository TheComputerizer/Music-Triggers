package mods.thecomputerizer.musictriggers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class configTitleCards {
    public static List<String> fb = new ArrayList<>();


    public static List<String> TitleCards = new ArrayList<>();
    public static List<String> ImageCards = new ArrayList<>();
    public static int ImageH;
    public static int ImageV;
    public static float ImageSize;

    public static boolean titleLines = false;
    public static boolean imageLines = false;

    public static void build(File f) {
        fb.add("Title Card & Image Card Config");
        fb.add("""
                \tTitle Cards
                \tFormat: Title,subtitle,event1,event2,event3,etc...
                \tNote - Spaces within the title & subtitle are fine, but spaces in other places might break things here\tExample 1: The Underground,The only light is your own,underground
                \tExample 2: Twilit Rain,A more ominous rain than usual,raining,dimension7
                \tList of event names: menu, generic, day, night, sunrise, sunset, light,\s
                \tunderground, deepUnder, raining, storming, snowing, lowHP, dead, inVoid,\s
                \tspectator, creative, riding, pet, high,\s
                \tdimension(id) - Ex: dimension7, biomename - Ex: minecraft:swamp,\s
                \tstructure:(name) - Ex: structure:Fortress, mobName - Ex: Zombie,\s
                \tstageName(true/false) - Ex: stageOnetrue
                \ttitle cards=<
                \t>""");
        fb.add("");
        fb.add("""
                \tImage Cards
                \tFormat: Image Title,event1,event2,event3,etc...
                \tNote: The Image must be located in [config/MusicTriggers/songs/assets/musictriggers/textures] and be a png
                \tLook above to the title cards to see the list of events
                \tExample: nightimg,night
                \tExample 2: imgtitle,dimension-50,deepUnder,light
                \timage cards=<
                \t>""");
        fb.add("");
        fb.add("""
                \t\tImage Card Horizontal
                \t\tNegative numbers will move the image cards to the left while positive numbers will move them to the right
                \t\tA value of 0 means the image will be centered
                \t\tThis value must be an integer
                \t\thorizontal offset=1""");
        fb.add("");
        fb.add("""
                \t\tImage Card Horizontal
                \t\tNegative numbers will move the image cards to the left while positive numbers will move them to the right
                \t\tA value of 0 means the image will be centered
                \t\tThis value must be an integer
                \t\tvertical offset=1""");
        fb.add("");
        fb.add("""
                \t\tImage Card Size
                \t\tThis act as a percentage for how big the image cards are.
                \t\t1.00 = 100%
                \t\tThis value must be a float
                \t\timage scale=1.0""");
        try {
            Files.write(Paths.get(f.getPath()), fb, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void read(File f) {
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("title cards=<")) {
                    titleLines = true;
                }
                else if (!line.contains(">") && titleLines) {
                    line = line.replaceAll("\t", "");
                    if (!TitleCards.contains(line) && line.length() != 0) {
                        MusicTriggers.logger.warn("is this even reading");
                        TitleCards.add(line);
                    }
                }
                if (line.contains("image cards=<")) {
                    imageLines = true;
                }
                else if (!line.contains(">") && imageLines) {
                    line = line.replaceAll(" ", "");
                    line = line.replaceAll("\t", "");
                    if (!ImageCards.contains(line) && line.length() != 0) {
                        ImageCards.add(line);
                    }
                }
                if (line.contains(">")) {
                    titleLines = false;
                    imageLines = false;
                }
                if (line.contains("horizontal offset=")) {
                    ImageH=Integer.parseInt(stringBreakerRegex(line,"=")[1]);
                }
                if (line.contains("vertical offset=")) {
                    ImageV=Integer.parseInt(stringBreakerRegex(line,"=")[1]);
                }
                if (line.contains("image scale=")) {
                    ImageSize=Float.parseFloat(stringBreakerRegex(line,"=")[1]);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String[] stringBreakerRegex(String s, String regex) {
        return s.split(regex);
    }

    public static void reload(File f) {
        TitleCards = new ArrayList<>();
        ImageCards = new ArrayList<>();
        read(f);
    }
}
