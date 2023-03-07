package mods.thecomputerizer.musictriggers.config;

import com.moandjiezana.toml.Toml;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.gui.instance.Debug;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.TomlUtil;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigDebug {

    public static File FILE;

    public static boolean SHOW_DEBUG = false;
    public static boolean CURRENT_SONG_ONLY = false;
    public static String LOG_LEVEL = "INFO";
    public static boolean PLAY_NORMAL_MUSIC = false;
    public static boolean REVERSE_PRIORITY = false;
    public static boolean COMBINE_EQUAL_PRIORITY = false;
    public static String[] BLOCKED_MOD_MUSIC = {};
    public static String[] BLOCKED_MOD_RECORDS = {};

    public static Debug copyToGui() {
        Holder holder = Holder.makeEmpty();
        try {
            holder = TomlUtil.readFully(FILE);
        } catch (IOException ex) {
            MusicTriggers.logExternally(Level.ERROR, "Caught exception when reading debug config for the GUI");
            Constants.MAIN_LOG.error("Caught exception when reading debug config for the GUI",ex);
        }
        return new Debug(holder);
    }

    public static void initialize(File f) {
        FILE = FileUtil.generateNestedFile(f,false);
        read();
        write();
    }

    public static void write() {
        List<String> lines = new ArrayList<>();
        lines.add("# Show the debug info");
        lines.add(LogUtil.injectParameters("SHOW_DEBUG = {}",SHOW_DEBUG));
        lines.add("");
        lines.add("# If SHOW_DEBUG is set to true, but you only want to see the song name");
        lines.add(LogUtil.injectParameters("CURRENT_SONG_ONLY = {}",CURRENT_SONG_ONLY));
        lines.add("");
        lines.add("# The lowest level of logging (DEBUG/INFO/WARN/ERROR/FATAL) to include in the GUI log visualizer");
        lines.add(LogUtil.injectParameters("LOG_LEVEL = \"{}\"",LOG_LEVEL));
        lines.add("");
        lines.add("# Allows vanilla and blocked music to play when there is music from Music Triggers already playing");
        lines.add(LogUtil.injectParameters("PLAY_NORMAL_MUSIC = {}",PLAY_NORMAL_MUSIC));
        lines.add("");
        lines.add("# Reverses the priority checker in the case of multiple triggers being able to play at once so the lowest priority wins instead of the highest");
        lines.add(LogUtil.injectParameters("REVERSE_PRIORITY = {}",REVERSE_PRIORITY));
        lines.add("");
        lines.add("# Allows for the combination of song pools in the case of multiple triggers with the same priority value being able to play at once");
        lines.add(LogUtil.injectParameters("COMBINE_EQUAL_PRIORITY = {}",COMBINE_EQUAL_PRIORITY));
        lines.add("");
        lines.add("# A list of mod ids to block audio in the Music category from so there is no overlap in music playing during events from other mods");
        lines.add(LogUtil.injectParameters("BLOCKED_MOD_MUSIC = {}", TextUtil.compileCollection((Object[])BLOCKED_MOD_MUSIC)));
        lines.add("");
        lines.add("# A list of mod ids to block audio in the Records category from so there is no overlap in records playing during events from other mods");
        lines.add(LogUtil.injectParameters("BLOCKED_MOD_RECORDS = {}",TextUtil.compileCollection((Object[])BLOCKED_MOD_RECORDS)));
        FileUtil.writeLinesToFile(FILE,lines,false);
    }

    public static void read() {
        Toml toml = new Toml().read(FILE);
        SHOW_DEBUG = TomlUtil.readIfExists(toml,"SHOW_DEBUG",SHOW_DEBUG);
        CURRENT_SONG_ONLY = TomlUtil.readIfExists(toml,"CURRENT_SONG_ONLY",CURRENT_SONG_ONLY);
        LOG_LEVEL = TomlUtil.readIfExists(toml,"LOG_LEVEL",LOG_LEVEL);
        PLAY_NORMAL_MUSIC = TomlUtil.readIfExists(toml,"PLAY_NORMAL_MUSIC",PLAY_NORMAL_MUSIC);
        REVERSE_PRIORITY = TomlUtil.readIfExists(toml,"REVERSE_PRIORITY",REVERSE_PRIORITY);
        COMBINE_EQUAL_PRIORITY = TomlUtil.readIfExists(toml,"COMBINE_EQUAL_PRIORITY",COMBINE_EQUAL_PRIORITY);
        BLOCKED_MOD_MUSIC = TomlUtil.readGenericArray(toml,"BLOCKED_MOD_MUSIC",BLOCKED_MOD_MUSIC);
        BLOCKED_MOD_RECORDS = TomlUtil.readGenericArray(toml,"BLOCKED_MOD_RECORDS",BLOCKED_MOD_RECORDS);
    }

    public static void update(Holder data) {
        SHOW_DEBUG = data.getValOrDefault("SHOW_DEBUG",false);
        CURRENT_SONG_ONLY = data.getValOrDefault("CURRENT_SONG_ONLY",false);
        LOG_LEVEL = data.getValOrDefault("LOG_LEVEL","INFO");
        PLAY_NORMAL_MUSIC = data.getValOrDefault("PLAY_NORMAL_MUSIC",false);
        REVERSE_PRIORITY = data.getValOrDefault("REVERSE_PRIORITY",false);
        COMBINE_EQUAL_PRIORITY = data.getValOrDefault("COMBINE_EQUAL_PRIORITY",false);
        BLOCKED_MOD_MUSIC = data.getValOrDefault("BLOCKED_MOD_MUSIC",new ArrayList<String>()).toArray(new String[]{});
        BLOCKED_MOD_RECORDS = data.getValOrDefault("BLOCKED_MOD_RECORDS",new ArrayList<String>()).toArray(new String[]{});
        write();
    }
}
