package mods.thecomputerizer.musictriggers.config;

import com.moandjiezana.toml.Toml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class ConfigChannels {

    public static void create(File f) {
        try {
            String sb = "# Example of attaching config files to audio an channel.\n"+
                    "# Copy and replace the part in brackets with the desired audio channel.\n" +
                    "# This mod does not create custom audio channels. Here are the acceptable channel names.\n" +
                    "# master, music, record, weather, block, hostile, neutral, player, ambient, voice\n" +
                    "# Note that you can nest config files in folders by specifying the folder before it like folder/filename.\n\n" +
                    "[music]\n\tmain = \"musictriggers\"\n\ttransitions = \"transitions\"\n\tcommands = \"commands\"\n\tredirect = \"redirect\"\n\tpaused_by_jukebox = \"true\"\n\toverrides_normal_music = \"true\"";
            FileWriter writer = new FileWriter(f);
            writer.write(sb);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<ChannelInfo> parse(File f) {
        if (!f.exists()) {
            try {
                f.createNewFile();
                create(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Toml toml = new Toml().read(f);
        List<ChannelInfo> channels = new ArrayList<>();
        for(String channel : channelCollector(f)) if(toml.contains(channel)) {
            Toml ch = toml.getTable(channel);
            channels.add(new ChannelInfo(channel,ch.getString("main","musictriggers"),ch.getString("transitions", "transitions"),ch.getString("commands", "commands"),ch.getString("redirect", "redirect"),ch.getString("paused_by_jukebox", "true"),ch.getString("overrides_normal_music", "true")));
        }
        return channels;
    }

    public static List<String> channelCollector(File toml) {
        List<String> ret = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(toml));
            String line = br.readLine();
            while (line != null) {
                if (!line.contains("\t") && !line.contains(" ") && line.contains("[") && line.contains("]") && !line.contains("\"") && !line.contains(".") && !line.contains("#")) {
                    String betterLine = line.replaceAll("\\[", "").replaceAll("]", "");
                    if (!ret.contains(betterLine)) ret.add(betterLine);
                }
                line = br.readLine();
            }
            br.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static class ChannelInfo {
        private final String channelName;
        private final String main;
        private final String transitions;
        private final String commands;
        private final String redirect;
        private final boolean pausedByJukeBox;
        private final boolean overridesNormalMusic;

        public ChannelInfo(String channelName, String main, String transitions, String commands, String redirect, String pausedByJukeBox, String overridesNormalMusic) {
            this.channelName = channelName;
            this.main = main;
            this.transitions = transitions;
            this.commands = commands;
            this.redirect = redirect;
            this.pausedByJukeBox = Boolean.parseBoolean(pausedByJukeBox);
            this.overridesNormalMusic = Boolean.parseBoolean(overridesNormalMusic);
        }

        public String getChannelName() {
            return channelName;
        }

        public String getMain() {
            return this.main;
        }

        public String getTransitions() {
            return this.transitions;
        }

        public String getCommands() {
            return this.commands;
        }

        public String getRedirect() {
            return this.redirect;
        }

        public boolean getPausedByJukeBox() {
            return this.pausedByJukeBox;
        }

        public boolean getOverridesNormalMusic() {
            return this.overridesNormalMusic;
        }
    }
}
