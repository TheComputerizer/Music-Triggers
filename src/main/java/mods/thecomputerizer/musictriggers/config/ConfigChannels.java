package mods.thecomputerizer.musictriggers.config;

import com.moandjiezana.toml.Toml;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.client.gui.instance.ChannelHolder;
import mods.thecomputerizer.musictriggers.client.gui.instance.ChannelInstance;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigChannels {

    public static File FILE;
    public static List<ChannelInfo> CHANNELS = new ArrayList<>();

    private static final List<TomlHolders.Type> parsedFile = new ArrayList<>();

    public static ChannelHolder copyToGui() {
        Map<String, ChannelInstance> channelsMap = new HashMap<>();
        for(ChannelInfo info : CHANNELS) channelsMap.put(info.channelName,info.copyToGui());
        return new ChannelHolder(FILE, null, channelsMap);
    }

    public static void initialize(File f) {
        FILE = FileUtil.generateNestedFile(f,false);
        CHANNELS = read();
        write();
    }

    private static void write() {
        List<String> lines = new ArrayList<>();
        lines.add("# This mod does not create custom audio channels. Here are the acceptable sound categories:");
        lines.add("# master, music, record, weather, block, hostile, neutral, player, ambient, voice");
        lines.add("# Note that you can nest config files in folders by specifying the folder before it like folder/filename.");
        lines.add("");
        for(ChannelInfo info : CHANNELS) lines.addAll(writeChannel(info));
        FileUtil.writeLinesToFile(FILE,lines,false);
    }

    private static List<String> writeChannel(ChannelInfo info) {
        List<String> lines = new ArrayList<>();
        lines.add(LogUtil.injectParameters("[{}]",info.channelName));
        lines.add(LogUtil.injectParameters("\tsound_category = \"{}\"",info.soundCategory));
        lines.add(LogUtil.injectParameters("\tmain = \"{}\"",info.main));
        lines.add(LogUtil.injectParameters("\ttransitions = \"{}\"",info.transitions));
        lines.add(LogUtil.injectParameters("\tcommands = \"{}\"",info.commands));
        lines.add(LogUtil.injectParameters("\ttoggles = \"{}\"",info.toggles));
        lines.add(LogUtil.injectParameters("\tredirect = \"{}\"",info.redirect));
        lines.add(LogUtil.injectParameters("\tsongs_folder = \"{}\"",info.songsFolder));
        lines.add(LogUtil.injectParameters("\tpaused_by_jukebox = \"{}\"",info.pausedByJukeBox));
        lines.add(LogUtil.injectParameters("\toverrides_normal_music = \"{}\"",info.overridesNormalMusic));
        return lines;
    }

    private static List<ChannelInfo> read() {
        parsedFile.addAll(TomlHolders.readCompleteToml(FILE));
        Toml toml = new Toml().read(FILE);
        List<ChannelInfo> channels = new ArrayList<>();
        for (TomlHolders.Type type : parsedFile) {
            if (type instanceof TomlHolders.Table table) {
                String channelName = table.getName();
                Toml ch = toml.getTable(channelName);
                channels.add(new ChannelInfo(channelName, ch.getString("sound_category", "music"),
                        ch.getString("main", channelName+"/main"),
                        ch.getString("transitions", channelName+"/transitions"),
                        ch.getString("commands", channelName+"/commands"),
                        ch.getString("toggles", channelName+"/toggles"),
                        ch.getString("redirect", channelName+"/redirect"),
                        ch.getString("jukebox", channelName+"/jukebox"),
                        ch.getString("songs_folder", "config/MusicTriggers/songs"),
                        ch.getString("paused_by_jukebox", "true"),
                        ch.getString("overrides_normal_music", "true")));
            }
        }
        if(channels.isEmpty()) channels.add(new ChannelInfo("music","music",
                "music/main","music/transitions",
                "music/commands", "music/toggles", "music/redirect", "music/jukebox",
                "config/MusicTriggers/songs", "true", "true"));
        return channels;
    }

    public static class ChannelInfo {
        private final String channelName;
        private final String soundCategory;
        private final String main;
        private final String transitions;
        private final String commands;
        private final String toggles;
        private final String redirect;
        private final String jukebox;
        private final String songsFolder;
        private final boolean pausedByJukeBox;
        private final boolean overridesNormalMusic;

        public ChannelInstance copyToGui() {
            Channel actualChannel = ChannelManager.getChannel(this.channelName);
            return new ChannelInstance(this.channelName,this.soundCategory,
                    actualChannel.getMainConfig().copyToGui(this.channelName),
                    this.main, actualChannel.getTransitionsConfig().copyToGui(this.channelName), this.transitions,
                    actualChannel.getCommandsConfig().copyToGui(this.channelName), this.commands,
                    actualChannel.getTogglesConfig().copyToGui(this.channelName), this.toggles,
                    actualChannel.getRedirect().copyToGui(this.channelName), this.redirect, this.songsFolder,
                    this.pausedByJukeBox, this.overridesNormalMusic);
        }

        public ChannelInfo(String channelName, String soundCategory, String main, String transitions, String commands, String toggles,
                           String redirect, String jukebox, String songFolder, String pausedByJukeBox, String overridesNormalMusic) {
            this.channelName = channelName;
            this.soundCategory = soundCategory;
            this.main = main;
            this.transitions = transitions;
            this.commands = commands;
            this.toggles = toggles;
            this.redirect = redirect;
            this.jukebox = jukebox;
            this.songsFolder = songFolder;
            this.pausedByJukeBox = Boolean.parseBoolean(pausedByJukeBox);
            this.overridesNormalMusic = Boolean.parseBoolean(overridesNormalMusic);
        }
        public boolean verifyOtherFilePathIsValid(String path, String configType) {
            if(path.matches(this.main)) {
                MusicTriggers.logExternally(Level.ERROR, "Config type {} cannot be {} " +
                                "as that matches the {} config type of the channel {} which was already registered!",
                        configType,path,"main",this.channelName);
                return false;
            }
            if(path.matches(this.transitions)) {
                MusicTriggers.logExternally(Level.ERROR, "Config type {} cannot be {} " +
                                "as that matches the {} config type of the channel {} which was already registered!",
                        configType,path,"transitions",this.channelName);
                return false;
            }
            if(path.matches(this.commands)) {
                MusicTriggers.logExternally(Level.ERROR, "Config type {} cannot be {} " +
                                "as that matches the {} config type of the channel {} which was already registered!",
                        configType,path,"commands",this.channelName);
                return false;
            }
            if(path.matches(this.toggles)) {
                MusicTriggers.logExternally(Level.ERROR, "Config type {} cannot be {} " +
                                "as that matches the {} config type of the channel {} which was already registered!",
                        configType,path,"toggles",this.channelName);
                return false;
            }
            if(path.matches(this.redirect)) {
                MusicTriggers.logExternally(Level.ERROR, "Config type {} cannot be {} " +
                                "as that matches the {} config type of the channel {} which was already registered!",
                        configType,path,"redirect",this.channelName);
                return false;
            }
            if(path.matches(this.jukebox)) {
                MusicTriggers.logExternally(Level.ERROR, "Config type {} cannot be {} " +
                                "as that matches the {} config type of the channel {} which was already registered!",
                        configType,path,"jukebox",this.channelName);
                return false;
            }
            return true;
        }

        public String getChannelName() {
            return channelName;
        }

        public String getSoundCategory() {
            return this.soundCategory;
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

        public String getToggles() {
            return this.toggles;
        }

        public String getRedirect() {
            return this.redirect;
        }

        public String getJukebox() {
            return this.jukebox;
        }

        public String getSongsFolder() {
            return this.songsFolder;
        }

        public boolean getPausedByJukeBox() {
            return this.pausedByJukeBox;
        }

        public boolean getOverridesNormalMusic() {
            return this.overridesNormalMusic;
        }
    }
}
