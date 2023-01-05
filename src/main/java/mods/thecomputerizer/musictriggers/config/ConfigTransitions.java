package mods.thecomputerizer.musictriggers.config;

import com.moandjiezana.toml.Toml;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.instance.Transitions;
import mods.thecomputerizer.theimpossiblelibrary.client.render.PNG;
import mods.thecomputerizer.theimpossiblelibrary.client.render.Renderer;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ConfigTransitions {

    private final File file;
    private String CrashHelper;
    public final Map<Integer, Title> titlecards = new HashMap<>();
    public final Map<Integer, Image> imagecards = new HashMap<>();

    public Transitions copyToGui(String channelName) {
        return new Transitions(this.file,channelName,MusicTriggers.clone(this.titlecards),
                MusicTriggers.clone(this.imagecards));
    }

    public ConfigTransitions(File file) {
        boolean exists = file.exists();
        this.file = exists ? file : FileUtil.generateNestedFile(file,false);
        if(!exists) FileUtil.writeLinesToFile(this.file,headerLines(),false);
        this.CrashHelper = "There was a problem initializing a transitions object!";
    }

    public List<String> headerLines() {
        return Arrays.asList("# Please refer to the wiki page located at https://github.com/TheComputerizer/Music-Triggers/wiki/The-Basics",
                "# or the discord server located at https://discord.gg/FZHXFYp8fc",
                "# for any specific questions you might have regarding transitions");
    }

    public void parse(String channel) {
        CrashHelper = "There was a problem initializing transitions in channel "+channel;
        int titleCounter = 0;
        int imageCounter = 0;
        try {
            Toml toml = new Toml().read(this.file);
            if (toml.containsTableArray("title"))
                for (Toml titleToml : toml.getTables("title"))
                    titleCounter = parseTitleCard(titleToml,channel,titleCounter);
            else if (toml.containsTable("title"))
                parseTitleCard(toml.getTable("title"),channel,titleCounter);
            if (toml.containsTableArray("image"))
                for (Toml imageToml : toml.getTables("image"))
                    imageCounter = parseImageCard(imageToml,channel,imageCounter);
            else if (toml.containsTable("image"))
                parseImageCard(toml.getTable("image"),channel,imageCounter);
        } catch (Exception e) {
            throw new RuntimeException(CrashHelper);
        }
    }

    private int parseTitleCard(Toml titleToml, String channel, int titleCounter) {
        CrashHelper = "There was a problem initializing title cards in channel "+channel;
        if(titleToml.contains("triggers")) {
            this.titlecards.putIfAbsent(titleCounter, new Title());
            Title title = this.titlecards.get(titleCounter);
            parseTriggers(title,channel,titleToml.getList("trigger"));
            if (titleToml.contains("title"))
                title.setTitles(titleToml.getList("title"));
            if (titleToml.contains("subtitle"))
                title.setSubTitles(titleToml.getList("subtitle"));
            if (titleToml.contains("play_once"))
                title.setPlayonce(Boolean.parseBoolean(titleToml.getString("play_once")));
            if (titleToml.contains("title_color"))
                title.setTitlecolor(titleToml.getString("title_color"));
            if (titleToml.contains("subtitle_color"))
                title.setSubtitlecolor(titleToml.getString("subtitle_color"));
            if (titleToml.contains("vague")) {
                boolean parsedVague = Boolean.parseBoolean(titleToml.getString("vague"));
                title.setVague(parsedVague);
                if (parsedVague) ChannelManager.getChannel(channel).canPlayTitle.put(titleCounter, true);
            }
            titleCounter++;
        } else MusicTriggers.logExternally(Level.ERROR,"Title card in channel {} needs to be " +
                "assigned to 1 or more triggers to be parsed correctly!",channel);
        return titleCounter;
    }

    private void parseTriggers(Title title, String channel, List<String> triggerNames) {
        AtomicBoolean broken = new AtomicBoolean(false);
        List<Trigger> triggers = triggerNames.stream()
                .map(name -> {
                    Trigger trigger = Trigger.parseAndGetTrigger(channel, name);
                    if (Objects.isNull(trigger)) {
                        broken.set(true);
                        MusicTriggers.logExternally(Level.ERROR, "Trigger {} for title did not exist! " +
                                "Title card will be skipped.", name);
                    }
                    return trigger;
                }).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (!broken.get()) title.setTriggers(triggers);
    }

    private int parseImageCard(Toml imageToml, String channel, int imageCounter) {
        CrashHelper = "There was a problem initializing image cards in channel "+channel;
        if(imageToml.contains("triggers")) {
            if(imageToml.contains("name")) {
                this.imagecards.putIfAbsent(imageCounter, new Image());
                Image image = this.imagecards.get(imageCounter);
                image.setName(imageToml.getString("name"));
                parseTriggers(image,channel,imageToml.getList("triggers"));
                if (imageToml.contains("split"))
                    image.setSplit(MusicTriggers.randomInt("split",
                            imageToml.getString("split"), 0));
                if (imageToml.contains("vertical"))
                    image.setVertical(MusicTriggers.randomInt("vertical",
                            imageToml.getString("vertical"), 0));
                if (imageToml.contains("horizontal"))
                    image.setHorizontal(MusicTriggers.randomInt("horizontal",
                            imageToml.getString("horizontal"), 0));
                if (imageToml.contains("horizontal_preset"))
                    image.setLocationX(imageToml.getString("horizontal_preset"));
                if (imageToml.contains("vertical_preset"))
                    image.setLocationY(imageToml.getString("vertical_preset"));
                if (imageToml.contains("scale_x"))
                    image.setScaleX(MusicTriggers.randomInt("scale_x",
                            imageToml.getString("scale_x"), 100));
                if (imageToml.contains("scale_y"))
                    image.setScaleY(MusicTriggers.randomInt("scale_y",
                            imageToml.getString("scale_y"), 100));
                if (imageToml.contains("time"))
                    image.setTime(MusicTriggers.randomInt("time",
                            imageToml.getString("time"), 0));
                if (imageToml.contains("play_once"))
                    image.setPlayonce(Boolean.parseBoolean(imageToml.getString("play_once")));
                if (imageToml.contains("fade_in"))
                    image.setFadeIn(MusicTriggers.randomInt("fade_in",
                            imageToml.getString("fade_in"), 0));
                if (imageToml.contains("fade_out"))
                    image.setFadeOut(MusicTriggers.randomInt("fade_out",
                            imageToml.getString("fade_out"), 0));
                if (imageToml.contains("vague")) {
                    boolean parsedVague = Boolean.parseBoolean(imageToml.getString("vague"));
                    image.setVague(parsedVague);
                    if (parsedVague) ChannelManager.getChannel(channel).canPlayImage.put(imageCounter, true);
                }
                imageCounter++;
            } else MusicTriggers.logExternally(Level.ERROR,"Image card in channel {} cannot be  " +
                    "assigned without a file name!",channel);
        } else MusicTriggers.logExternally(Level.ERROR,"Image card in channel {} needs to be " +
                "assigned to 1 or more triggers to be parsed correctly!",channel);
        return imageCounter;
    }

    private void parseTriggers(Image image, String channel, List<String> triggerNames) {
        AtomicBoolean broken = new AtomicBoolean(false);
        List<Trigger> triggers = triggerNames.stream()
                .map(name -> {
                    Trigger trigger = Trigger.parseAndGetTrigger(channel, name);
                    if (Objects.isNull(trigger)) {
                        broken.set(true);
                        MusicTriggers.logExternally(Level.ERROR, "Trigger {} for image \"{}\" did not exist! " +
                                "Title card will be skipped.",name, image.getName());
                    }
                    return trigger;
                }).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (!broken.get()) image.setTriggers(triggers);
    }

    public static class Title {
        private List<String> titles;
        private List<String> subtitles;
        private Boolean playonce;
        private String titlecolor;
        private String subtitlecolor;
        private boolean vague;
        private final List<Trigger> triggers;

        public Title() {
            this.titles = new ArrayList<>();
            this.subtitles = new ArrayList<>();
            this.playonce = false;
            this.titlecolor = "red";
            this.subtitlecolor = "white";
            this.vague = false;
            this.triggers = new ArrayList<>();
        }

        public List<String> getAsTomlLines(boolean multi) {
            List<String> lines = new ArrayList<>();
            if(!triggers.isEmpty() && (!titles.isEmpty() || !subtitles.isEmpty())) {
                lines.add(multi ? "[[title]]" : "[title]");
                if(!titles.isEmpty())
                    lines.add(LogUtil.injectParameters("\ttitle = {}",TextUtil.compileCollection(this.titles)));
                if(!subtitles.isEmpty())
                    lines.add(LogUtil.injectParameters("\tsubtitle = {}",TextUtil.compileCollection(this.titles)));
                lines.add(LogUtil.injectParameters("\ttriggers = {}",
                        TextUtil.compileCollection(this.triggers.stream().map(Trigger::getNameWithID).distinct()
                                .collect(Collectors.toList()))));
                if(playonce)
                    lines.add("\tplay_once = true");
                if(!titlecolor.matches("red"))
                    lines.add(LogUtil.injectParameters("\ttitle_color = \"{}\"",this.titlecolor));
                if(!subtitlecolor.matches("white"))
                    lines.add(LogUtil.injectParameters("\tsubtitle_color = \"{}\"",this.subtitlecolor));
                if(vague)
                    lines.add("\tvague = true");
                lines.add("");
            }
            return lines;
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

        public void setTriggers(List<Trigger> t) {
            this.triggers.clear();
            this.triggers.addAll(t);
        }

        public List<Trigger> getTriggers() {
            return this.triggers;
        }
    }

    public static class Image {
        private String name;
        private PNG png;
        private int vertical;
        private int horizontal;
        private int scalex;
        private int scaley;
        private int time;
        private String locationX;
        private String locationY;
        private int split;
        private int fadeIn;
        private int fadeOut;
        private boolean playonce;
        private boolean vague;
        private final List<Trigger> triggers;
        private boolean initialized;

        public Image() {
            this.name = "";
            this.vertical = 0;
            this.horizontal = 0;
            this.scalex = 100;
            this.scaley = 100;
            this.time = 750;
            this.locationX = "center";
            this.locationY = "center";
            this.split = 0;
            this.fadeIn = 10;
            this.fadeOut = 10;
            this.playonce = false;
            this.vague = false;
            this.triggers = new ArrayList<>();
            this.initialized = false;
        }

        public List<String> getAsTomlLines(boolean multi) {
            List<String> lines = new ArrayList<>();
            if(!triggers.isEmpty() && !name.isEmpty()) {
                lines.add(multi ? "[[image]]" : "[image]");
                lines.add(LogUtil.injectParameters("\tname = \"{}\"",this.name));
                lines.add(LogUtil.injectParameters("\ttriggers = {}",
                        TextUtil.compileCollection(this.triggers.stream().map(Trigger::getNameWithID).distinct()
                                .collect(Collectors.toList()))));
                if(vertical!=0)
                    lines.add(LogUtil.injectParameters("\tvertical = {}",this.vertical));
                if(horizontal!=0)
                    lines.add(LogUtil.injectParameters("\thorizontal = {}",this.horizontal));
                if(scalex!=0)
                    lines.add(LogUtil.injectParameters("\tscale_x = {}",this.scalex));
                if(scaley!=0)
                    lines.add(LogUtil.injectParameters("\tscale_y = {}",this.scaley));
                if(time!=0)
                    lines.add(LogUtil.injectParameters("\ttime = {}",this.time));
                if(!locationX.matches("center"))
                    lines.add(LogUtil.injectParameters("\thorizontal_preset = \"{}\"",this.locationX));
                if(!locationY.matches("center"))
                    lines.add(LogUtil.injectParameters("\tvertical_preset = \"{}\"",this.locationY));
                if(split!=0)
                    lines.add(LogUtil.injectParameters("\tsplit = {}",this.split));
                if(fadeIn!=0)
                    lines.add(LogUtil.injectParameters("\tfade_in = {}",this.fadeIn));
                if(fadeOut!=0)
                    lines.add(LogUtil.injectParameters("\tfade_out = {}",this.fadeOut));
                if(playonce)
                    lines.add("\tplay_once = true");
                if(vague)
                    lines.add("\tvague = true");
                lines.add("");
            }
            return lines;
        }

        public void setName(String t) {
            this.name = t;
        }

        public String getName() {
            return this.name;
        }

        public Object getFormat() {
            if(this.png!=null) return this.png;
            return null;
        }

        public void initialize() {
            /*
            if(this.name.contains(".gif")) {
                this.gif = Renderer.initializeGif(new ResourceLocation(MusicTriggers.MODID,"textures/"+this.name));
                this.initialized = true;
            }
            else if(this.name.contains(".mp4")) {
                this.mp4 = Renderer.initializeMp4(new ResourceLocation(MusicTriggers.MODID,"textures/"+this.name));
                this.initialized = true;
            }
             */
            if(this.name.contains(".png")) {
                this.png = Renderer.initializePng(new ResourceLocation(Constants.MODID,"textures/"+this.name));
                this.initialized = true;
            }
            else MusicTriggers.logExternally(Level.WARN,"Could not initialize image card "+this.name+"! Is the format correct?");
        }

        public boolean isInitialized() {
            return this.initialized;
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

        public String getLocationX() {
            return this.locationX;
        }

        public void setLocationX(String locationX) {
            this.locationX = locationX;
        }

        public String getLocationY() {
            return locationY;
        }

        public void setLocationY(String locationY) {
            this.locationY = locationY;
        }

        public int getSplit() {
            return this.split;
        }

        public void setSplit(int s) {
            this.split = s;
        }

        public void setPlayonce(Boolean b) {
            this.playonce = b;
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

        public Boolean getPlayonce() {
            return this.playonce;
        }

        public void setVague(Boolean b) {
            this.vague = b;
        }

        public Boolean getVague() {
            return this.vague;
        }

        public void setTriggers(List<Trigger> t) {
            this.triggers.clear();
            this.triggers.addAll(t);
        }

        public List<Trigger> getTriggers() {
            return this.triggers;
        }
    }

    public void clearMaps() {
        this.titlecards.clear();
        this.imagecards.clear();
    }
}
