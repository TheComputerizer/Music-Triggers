package mods.thecomputerizer.musictriggers.config;

import com.moandjiezana.toml.Toml;
import mods.thecomputerizer.musictriggers.MusicTriggers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class configTitleCards {

    public static String CrashHelper;
    public static HashMap<Integer, Title> titlecards = new HashMap<>();
    public static HashMap<Integer, Image> imagecards = new HashMap<>();
    public static HashMap<Integer, Boolean> ismoving = new HashMap<>();

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
            //try {
                Toml toml = new Toml().read(file);
                if(toml.containsTableArray("title")) {
                    CrashHelper = "There was a problem initializing title cards";
                    for(Toml title : toml.getTables("title")) {
                        titlecards.putIfAbsent(titleCounter,new Title());
                        if(title.contains("title")) {
                            titlecards.get(titleCounter).setTitle(title.getString("title"));
                        }
                        if(title.contains("subtitle")) {
                            titlecards.get(titleCounter).setSubTitle(title.getString("subtitle"));
                        }
                        if(title.contains("triggers")) {
                            titlecards.get(titleCounter).addTriggers(new ArrayList<>(title.getList("triggers")));
                        }
                        titleCounter++;
                    }
                }
                else if(toml.containsTable("title")) {
                    MusicTriggers.logger.info("Found title card");
                    CrashHelper = "There was a problem initializing title cards";
                    Toml title = toml.getTable("title");
                    titlecards.putIfAbsent(titleCounter,new Title());
                    if(title.contains("title")) {
                        titlecards.get(titleCounter).setTitle(title.getString("title"));
                    }
                    if(title.contains("subtitle")) {
                        titlecards.get(titleCounter).setSubTitle(title.getString("subtitle"));
                    }
                    if(title.contains("triggers")) {
                        MusicTriggers.logger.info("Found triggers for title card");
                        titlecards.get(titleCounter).addTriggers(new ArrayList<>(title.getList("triggers")));
                    }
                }
                if(toml.containsTableArray("image")) {
                    CrashHelper = "There was a problem initializing image cards";
                    for(Toml image : toml.getTables("image")) {
                        imagecards.putIfAbsent(imageCounter,new Image());
                        if(image.contains("name")) {
                            imagecards.get(imageCounter).setName(image.getString("name"));
                        }
                        if(image.contains("vertical")) {
                            imagecards.get(imageCounter).setVertical(Integer.parseInt(image.getString("vertical")));
                        }
                        if(image.contains("horizontal")) {
                            imagecards.get(imageCounter).setHorizontal(Integer.parseInt(image.getString("horizontal")));
                        }
                        if(image.contains("scale")) {
                            imagecards.get(imageCounter).setScale(Integer.parseInt(image.getString("scale")));
                        }
                        if(image.contains("triggers")) {
                            imagecards.get(imageCounter).addTriggers(new ArrayList<>(image.getList("triggers")));
                        }
                        imageCounter++;
                    }
                }
                else if(toml.containsTable("image")) {
                    CrashHelper = "There was a problem initializing image cards";
                    Toml image = toml.getTable("image");
                    imagecards.putIfAbsent(imageCounter,new Image());
                    ismoving.putIfAbsent(imageCounter, false);
                    if(image.containsTable("gif")) {
                        Toml gif = image.getTable("gif");
                        if(gif.contains("delay")) {
                            imagecards.get(imageCounter).setDelay(Integer.parseInt(gif.getString("delay")));
                            ismoving.put(imageCounter,true);
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
                    if(image.contains("scale")) {
                        imagecards.get(imageCounter).setScale(Integer.parseInt(image.getString("scale")));
                    }
                    if(image.contains("time")) {
                        imagecards.get(imageCounter).setTime(Integer.parseInt(image.getString("time")));
                    }
                    if(image.contains("triggers")) {
                        imagecards.get(imageCounter).addTriggers(new ArrayList<>(image.getList("triggers")));
                    }
                }
            //} catch (Exception e) {
                //throw new RuntimeException(CrashHelper);
            //}
        }
    }

    public static class Title {
        private String title;
        private String subtitle;
        private final List<String> triggers;

        public Title() {
            this.title = "";
            this.subtitle = "";
            this.triggers = new ArrayList<>();
        }

        public void setTitle(String t) {
            this.title = t;
        }

        public String getTitle() {
            return this.title;
        }

        public void setSubTitle(String t) {
            this.subtitle = t;
        }

        public String getSubTitle() {
            return this.subtitle;
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
        private int scale;
        private int time;
        private int delay;
        private final List<String> triggers;

        public Image() {
            this.name = "";
            this.vertical = 0;
            this.horizontal = 0;
            this.scale = 100;
            this.time = 750;
            this.delay = 10;
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

        public void setScale(int s) {
            this.scale = s;
        }

        public int getScale() {
            return this.scale;
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

        public void addTriggers(ArrayList<String> t) {
            this.triggers.addAll(t);
        }

        public List<String> getTriggers() {
            return this.triggers;
        }
    }

    public static void emptyMaps() {
        titlecards = new HashMap<>();
        imagecards = new HashMap<>();
        ismoving = new HashMap<>();
    }
}
