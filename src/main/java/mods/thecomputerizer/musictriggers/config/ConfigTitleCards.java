package mods.thecomputerizer.musictriggers.config;

import com.moandjiezana.toml.Toml;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.util.image.GIFHandler;
import mods.thecomputerizer.musictriggers.util.image.MP4Handler;
import mods.thecomputerizer.musictriggers.util.image.PNGMcMetaHandler;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileDeleteStrategy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ConfigTitleCards {

    public static String CrashHelper;
    public static HashMap<Integer, Title> titlecards = new HashMap<>();
    public static HashMap<Integer, Image> imagecards = new HashMap<>();
    public static HashMap<Integer, Boolean> ismoving = new HashMap<>();
    public static HashMap<ResourceLocation, ImageDimensions> imageDimensions= new HashMap<>();

    public static void parse() {
        File file = new File("config/MusicTriggers/transitions.toml");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        else {
            CrashHelper = "There was a problem initializing transitions";
            int titleCounter = 0;
            int imageCounter = 0;
            try {
                Toml toml = new Toml().read(file);
                if(toml.containsTableArray("title")) {
                    CrashHelper = "There was a problem initializing title cards";
                    for(Toml title : toml.getTables("title")) {
                        titlecards.putIfAbsent(titleCounter,new Title());
                        if(title.contains("title")) {
                            titlecards.get(titleCounter).setTitles(title.getList("title"));
                        }
                        if(title.contains("subtitle")) {
                            titlecards.get(titleCounter).setSubTitles(title.getList("subtitle"));
                        }
                        if(title.contains("triggers")) {
                            titlecards.get(titleCounter).addTriggers(new ArrayList<>(title.getList("triggers")));
                        }
                        if(title.contains("play_once")) {
                            titlecards.get(titleCounter).setPlayonce(Boolean.parseBoolean(title.getString("play_once")));
                        }
                        if(title.contains("title_color")) {
                            titlecards.get(titleCounter).setTitlecolor(title.getString("title_color"));
                        }
                        if(title.contains("subtitle_color")) {
                            titlecards.get(titleCounter).setSubtitlecolor(title.getString("subtitle_color"));
                        }
                        if(title.contains("vague")) {
                            boolean parsedVague = Boolean.parseBoolean(title.getString("vague"));
                            titlecards.get(titleCounter).setVague(parsedVague);
                            if(parsedVague) MusicPlayer.canPlayTitle.put(titleCounter, true);
                        }
                        titleCounter++;
                    }
                }
                else if(toml.containsTable("title")) {
                    CrashHelper = "There was a problem initializing title cards";
                    Toml title = toml.getTable("title");
                    titlecards.putIfAbsent(titleCounter,new Title());
                    if(title.contains("title")) {
                        titlecards.get(titleCounter).setTitles(title.getList("title"));
                    }
                    if(title.contains("subtitle")) {
                        titlecards.get(titleCounter).setSubTitles(title.getList("subtitle"));
                    }
                    if(title.contains("triggers")) {
                        titlecards.get(titleCounter).addTriggers(new ArrayList<>(title.getList("triggers")));
                    }
                    if(title.contains("play_once")) {
                        titlecards.get(titleCounter).setPlayonce(Boolean.parseBoolean(title.getString("play_once")));
                    }
                    if(title.contains("title_color")) {
                        titlecards.get(titleCounter).setTitlecolor(title.getString("title_color"));
                    }
                    if(title.contains("subtitle_color")) {
                        titlecards.get(titleCounter).setSubtitlecolor(title.getString("subtitle_color"));
                    }
                    if(title.contains("vague")) {
                        boolean parsedVague = Boolean.parseBoolean(title.getString("vague"));
                        titlecards.get(titleCounter).setVague(parsedVague);
                        if(parsedVague) MusicPlayer.canPlayTitle.put(titleCounter, true);
                    }
                }
                if(toml.containsTableArray("image")) {
                    CrashHelper = "There was a problem initializing image cards";
                    for(Toml image : toml.getTables("image")) {
                        imagecards.putIfAbsent(imageCounter,new Image());
                        ismoving.putIfAbsent(imageCounter, false);
                        if(image.containsTable("animation")) {
                            Toml gif = image.getTable("animation");
                            ismoving.put(imageCounter,true);
                            if(gif.contains("delay")) {
                                imagecards.get(imageCounter).setDelay(Integer.parseInt(gif.getString("delay")));
                            }
                            if(gif.contains("split")) {
                                imagecards.get(imageCounter).setSplit(Integer.parseInt(gif.getString("split")));
                            }
                            if(gif.contains("frames_skipped")) {
                                imagecards.get(imageCounter).setSkip(Integer.parseInt(gif.getString("frames_skipped")));
                            }
                        }
                        if(image.contains("name")) {
                            imagecards.get(imageCounter).setName(image.getString("name"));
                        }
                        if(image.contains("vertical")) {
                            imagecards.get(imageCounter).setVertical(Integer.parseInt(image.getString("vertical")));
                        }
                        if(image.contains("horizontal")) {
                            imagecards.get(imageCounter).setHorizontal(Integer.parseInt(image.getString("horizontal")));
                        }
                        if(image.contains("scale_x")) {
                            imagecards.get(imageCounter).setScaleX(Integer.parseInt(image.getString("scale_x")));
                        }
                        if(image.contains("scale_y")) {
                            imagecards.get(imageCounter).setScaleY(Integer.parseInt(image.getString("scale_y")));
                        }
                        if(image.contains("time")) {
                            imagecards.get(imageCounter).setTime(Integer.parseInt(image.getString("time")));
                        }
                        if(image.contains("triggers")) {
                            imagecards.get(imageCounter).addTriggers(new ArrayList<>(image.getList("triggers")));
                        }
                        if(image.contains("play_once")) {
                            imagecards.get(imageCounter).setPlayonce(Boolean.parseBoolean(image.getString("play_once")));
                        }
                        if(image.contains("fade_in")) {
                            imagecards.get(imageCounter).setFadeIn(Integer.parseInt(image.getString("fade_in")));
                        }
                        if(image.contains("fade_out")) {
                            imagecards.get(imageCounter).setFadeOut(Integer.parseInt(image.getString("fade_out")));
                        }
                        if(image.contains("vague")) {
                            boolean parsedVague = Boolean.parseBoolean(image.getString("vague"));
                            imagecards.get(imageCounter).setVague(parsedVague);
                            if(parsedVague) MusicPlayer.canPlayImage.put(imageCounter, true);
                        }
                        imageCounter++;
                    }
                }
                else if(toml.containsTable("image")) {
                    CrashHelper = "There was a problem initializing image cards";
                    Toml image = toml.getTable("image");
                    imagecards.putIfAbsent(imageCounter,new Image());
                    ismoving.putIfAbsent(imageCounter, false);
                    if(image.containsTable("animation")) {
                        Toml gif = image.getTable("animation");
                        ismoving.put(imageCounter,true);
                        if(gif.contains("delay")) {
                            imagecards.get(imageCounter).setDelay(Integer.parseInt(gif.getString("delay")));
                        }
                        if(gif.contains("split")) {
                            imagecards.get(imageCounter).setSplit(Integer.parseInt(gif.getString("split")));
                        }
                        if(gif.contains("frames_skipped")) {
                            imagecards.get(imageCounter).setSkip(Integer.parseInt(gif.getString("frames_skipped")));
                        }
                    }
                    if(image.contains("name")) {
                        imagecards.get(imageCounter).setName(image.getString("name"));
                    }
                    if(image.contains("vertical")) {
                        imagecards.get(imageCounter).setVertical(Integer.parseInt(image.getString("vertical")));
                    }
                    if(image.contains("horizontal")) {
                        imagecards.get(imageCounter).setHorizontal(Integer.parseInt(image.getString("horizontal")));
                    }
                    if(image.contains("scale_x")) {
                        imagecards.get(imageCounter).setScaleX(Integer.parseInt(image.getString("scale_x")));
                    }
                    if(image.contains("scale_y")) {
                        imagecards.get(imageCounter).setScaleY(Integer.parseInt(image.getString("scale_y")));
                    }
                    if(image.contains("time")) {
                        imagecards.get(imageCounter).setTime(Integer.parseInt(image.getString("time")));
                    }
                    if(image.contains("triggers")) {
                        imagecards.get(imageCounter).addTriggers(new ArrayList<>(image.getList("triggers")));
                    }
                    if(image.contains("play_once")) {
                        imagecards.get(imageCounter).setPlayonce(Boolean.parseBoolean(image.getString("play_once")));
                    }
                    if(image.contains("fade_in")) {
                        imagecards.get(imageCounter).setFadeIn(Integer.parseInt(image.getString("fade_in")));
                    }
                    if(image.contains("fade_out")) {
                        imagecards.get(imageCounter).setFadeOut(Integer.parseInt(image.getString("fade_out")));
                    }
                    if(image.contains("vague")) {
                        boolean parsedVague = Boolean.parseBoolean(image.getString("vague"));
                        imagecards.get(imageCounter).setVague(parsedVague);
                        if(parsedVague) MusicPlayer.canPlayImage.put(imageCounter, true);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(CrashHelper);
            }
        }
        formatImages();
    }

    private static void formatImages() {
        for (int i : ConfigTitleCards.imagecards.keySet()) {
            ResourceLocation rl;
            if(ismoving.get(i)) {
                if (ConfigTitleCards.imagecards.get(i).getName() != null) {
                    String path = "." + "/config/MusicTriggers/songs/assets/musictriggers/textures/" + imagecards.get(i).getName();
                    File folder = new File(path);
                    File findMP4 = new File(path + ".mp4");
                    File findGIF = new File(path + ".gif");
                    File findPng = new File(path + ".png");
                    File findPngMeta = new File(path + ".png.mcmeta");
                    if (findMP4.exists()) {
                        try {
                            folder.mkdir();
                            MP4Handler.splitMP4(findMP4, folder, ConfigTitleCards.imagecards.get(i).getSkip());
                            if(!findMP4.delete()) {
                                FileDeleteStrategy.FORCE.delete(findMP4);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new RuntimeException("There was a problem breaking down the mp4 named " + imagecards.get(i).getName() + ".mp4 (Internally: File " + e.getStackTrace()[0].getFileName() + " at line " + e.getStackTrace()[0].getLineNumber()+")");
                        }
                    } else if (findGIF.exists()) {
                        try {
                            folder.mkdir();
                            GIFHandler.splitGif(findGIF, folder);
                            if(!findGIF.delete()) {
                                FileDeleteStrategy.FORCE.delete(findGIF);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new RuntimeException("There was a problem breaking down the gif named " + imagecards.get(i).getName() + ".gif (Internally: File " + e.getStackTrace()[0].getFileName() + " at line " + e.getStackTrace()[0].getLineNumber()+")");
                        }
                    } else if (findPngMeta.exists()) {
                        try {
                            folder.mkdir();
                            PNGMcMetaHandler.splitPNG(findPng, folder, imagecards.get(i).getSplit());
                            findPng.delete();
                            findPngMeta.delete();
                        } catch (Exception e) {
                            throw new RuntimeException("There was a problem breaking down the png named " + imagecards.get(i).getName() + ".png (Internally: File " + e.getStackTrace()[0].getFileName() + " at line " + e.getStackTrace()[0].getLineNumber()+")");
                        }
                    }
                    if (!folder.exists()) {
                        MusicTriggers.logger.error("Animated image of " + imagecards.get(i).getName() + " does not exist or was not parsed properly");
                    }
                    else {
                        if(imagecards.get(i).getName()!=null) {
                            File[] listOfPNG = folder.listFiles();
                            assert listOfPNG != null;
                            for (File f : listOfPNG) {
                                rl = new ResourceLocation(MusicTriggers.MODID, "textures/" + ConfigTitleCards.imagecards.get(i).getName() + "/" + f.getName());
                                try {
                                    BufferedImage image = ImageIO.read(new File("." + "/config/MusicTriggers/songs/assets/musictriggers/textures/" + imagecards.get(i).getName() + "/" + f.getName()));
                                    imageDimensions.put(rl, new ImageDimensions());
                                    imageDimensions.get(rl).setWidth(image.getWidth());
                                    imageDimensions.get(rl).setHeight(image.getHeight());
                                } catch (Exception ex) {
                                    throw new RuntimeException("Unable to read size of image with resource location " + rl);
                                }
                            }
                        }
                    }
                }
            }
            else {
                rl = new ResourceLocation(MusicTriggers.MODID,"textures/"+imagecards.get(i).getName()+".png");
                try {
                    BufferedImage image = ImageIO.read(new File("." + "/config/MusicTriggers/songs/assets/musictriggers/textures/"+imagecards.get(i).getName()+".png"));
                    imageDimensions.put(rl, new ImageDimensions());
                    imageDimensions.get(rl).setWidth(image.getWidth());
                    imageDimensions.get(rl).setHeight(image.getHeight());
                } catch(Exception ex) {
                    throw new RuntimeException("Unable to read size of image with resource location "+ rl);
                }
            }
        }
    }

    public static class Title {
        private List<String> titles;
        private List<String> subtitles;
        private Boolean playonce;
        private String titlecolor;
        private String subtitlecolor;
        private boolean vague;
        private final List<String> triggers;

        public Title() {
            this.titles = new ArrayList<>();
            this.subtitles = new ArrayList<>();
            this.playonce = false;
            this.titlecolor = "red";
            this.subtitlecolor = "white";
            this.vague = false;
            this.triggers = new ArrayList<>();
        }

        public void setTitles(List<String> t) {
            this.titles = t;
        }

        public List<String> getTitles() {
            return this.titles;
        }

        public void setSubTitles(List<String> t) {
            this.subtitles = t;
        }

        public List<String> getSubTitles() {
            return this.subtitles;
        }

        public void setPlayonce(Boolean b) {
            this.playonce = b;
        }

        public Boolean getPlayonce() {
            return this.playonce;
        }

        public String getTitlecolor() {
            return this.titlecolor;
        }

        public void setTitlecolor(String c) {
            this.titlecolor = c;
        }

        public String getSubtitlecolor() {
            return this.subtitlecolor;
        }

        public void setSubtitlecolor(String c) {
            this.subtitlecolor = c;
        }

        public void setVague(Boolean b) {
            this.vague = b;
        }

        public Boolean getVague() {
            return this.vague;
        }

        public void addTriggers(ArrayList<String> t) {
            this.triggers.addAll(t);
        }

        public List<String> getTriggers() {
            return this.triggers;
        }
    }

    public static class Image {
        private String name;
        private int vertical;
        private int horizontal;
        private int scalex;
        private int scaley;
        private int time;
        private int delay;
        private int split;
        private int skip;
        private int fadeIn;
        private int fadeOut;
        private boolean playonce;
        private boolean vague;
        private final List<String> triggers;

        public Image() {
            this.name = "";
            this.vertical = 0;
            this.horizontal = 0;
            this.scalex = 100;
            this.scaley = 100;
            this.time = 750;
            this.delay = 10;
            this.split = 0;
            this.skip = 4;
            this.fadeIn = 10;
            this.fadeOut = 10;
            this.playonce = false;
            this.vague = false;
            this.triggers = new ArrayList<>();
        }

        public void setName(String t) {
            this.name = t;
        }

        public String getName() {
            return this.name;
        }

        public void setVertical(int v) {
            this.vertical = v;
        }

        public int getVertical() {
            return this.vertical;
        }

        public void setHorizontal(int h) {
            this.horizontal = h;
        }

        public int getHorizontal() {
            return this.horizontal;
        }

        public void setScaleX(int s) {
            this.scalex = s;
        }

        public int getScaleX() {
            return this.scalex;
        }

        public void setScaleY(int s) {
            this.scaley = s;
        }

        public int getScaleY() {
            return this.scaley;
        }

        public int getTime() {
            return this.time;
        }

        public void setTime(int t) {
            this.time = t;
        }

        public int getDelay() {
            return this.delay;
        }

        public void setDelay(int d) {
            this.delay = d;
        }

        public int getSplit() {
            return this.split;
        }

        public void setSplit(int s) {
            this.split = s;
        }

        public int getSkip() {
            return this.skip;
        }

        public void setSkip(int s) {
            this.skip = s;
        }

        public int getFadeIn() {
            return this.fadeIn;
        }

        public void setFadeIn(int f) {
            this.fadeIn = f;
        }

        public int getFadeOut() {
            return this.fadeOut;
        }

        public void setFadeOut(int f) {
            this.fadeOut = f;
        }

        public void setPlayonce(Boolean b) {
            this.playonce = b;
        }

        public boolean getPlayonce() {
            return this.playonce;
        }

        public void setVague(Boolean b) {
            this.vague = b;
        }

        public Boolean getVague() {
            return this.vague;
        }

        public void addTriggers(ArrayList<String> t) {
            this.triggers.addAll(t);
        }

        public List<String> getTriggers() {
            return this.triggers;
        }
    }

    public static class ImageDimensions {
        private int width;
        private int height;

        public ImageDimensions() {
            this.width = 1;
            this.height = 1;
        }

        public int getWidth() {
            return this.width;
        }

        public void setWidth(int w) {
            this.width = w;
        }

        public int getHeight() {
            return this.height;
        }

        public void setHeight(int h) {
            this.height = h;
        }
    }

    public static void emptyMaps() {
        titlecards = new HashMap<>();
        imagecards = new HashMap<>();
        ismoving = new HashMap<>();
        imageDimensions = new HashMap<>();
    }
}