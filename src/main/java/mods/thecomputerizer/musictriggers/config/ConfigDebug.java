package mods.thecomputerizer.musictriggers.config;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import mods.thecomputerizer.shadowed.moandjiezana.toml.Toml;
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
import java.util.*;

public class ConfigDebug {

    public static File FILE;

    public static boolean SHOW_DEBUG = false;
    public static boolean CURRENT_SONG_ONLY = false;
    public static boolean ALLOW_TIMESTAMPS = false;
    public static String LOG_LEVEL = "INFO";
    public static boolean PLAY_NORMAL_MUSIC = false;
    public static boolean REVERSE_PRIORITY = false;
    public static boolean COMBINE_EQUAL_PRIORITY = false;
    public static boolean PAUSE_WHEN_TABBED = true;
    public static HashSet<String> BLOCKED_MOD_CATEGORIES = new HashSet<>(Collections.singleton("minecraft;music"));
    public static HashSet<String> FORMATTED_BLOCKED_MODS = new HashSet<>(Collections.singleton("minecraft[music]"));
    public static boolean BLOCK_STREAMING_ONLY = true;
    public static HashSet<String> INTERRUPTED_AUDIO_CATEGORIES = new HashSet<>(Collections.singleton("music"));

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
        lines.add("# If SHOW_DEBUG and CURRENT_SONG_ONLY are both enabled, but you want to see the timestamps as well");
        lines.add(LogUtil.injectParameters("ALLOW_TIMESTAMPS = {}",ALLOW_TIMESTAMPS));
        lines.add("");
        lines.add("# The lowest level of logging (DEBUG/INFO/WARN/ERROR/FATAL) to include in the GUI log visualizer");
        lines.add(LogUtil.injectParameters("LOG_LEVEL = \"{}\"",LOG_LEVEL));
        lines.add("");
        lines.add("# Allows vanilla and blocked music to play when there is music from Music Triggers already playing");
        lines.add(LogUtil.injectParameters("PLAY_NORMAL_MUSIC = {}",PLAY_NORMAL_MUSIC));
        lines.add("");
        lines.add("# Reverses the priority checker in the case of multiple triggers being able to play at once so the " +
                "lowest priority wins instead of the highest");
        lines.add(LogUtil.injectParameters("REVERSE_PRIORITY = {}",REVERSE_PRIORITY));
        lines.add("");
        lines.add("# Allows for the combination of song pools in the case of multiple triggers with the same priority " +
                "value being able to play at once");
        lines.add(LogUtil.injectParameters("COMBINE_EQUAL_PRIORITY = {}",COMBINE_EQUAL_PRIORITY));
        lines.add("");
        lines.add("# If PAUSE_WHEN_TABBED is set to false, the music will no longer get paused when the game is not in focus");
        lines.add(LogUtil.injectParameters("PAUSE_WHEN_TABBED = {}",PAUSE_WHEN_TABBED));
        lines.add("");
        lines.add("# A list of mod ids to block audio in the specified audio category from so there is no overlap in " +
                "music playing during events from other mods");
        lines.add(LogUtil.injectParameters("BLOCKED_MOD_CATEGORIES = {}", TextUtil.compileCollection(BLOCKED_MOD_CATEGORIES)));
        lines.add("");
        lines.add("# If BLOCK_STREAMING_ONLY is disabled, BLOCKED_MOD_CATEGORIES will be able to stop non streaming " +
                "audio sources like sound effects from playing");
        lines.add(LogUtil.injectParameters("BLOCK_STREAMING_ONLY = {}",BLOCK_STREAMING_ONLY));
        lines.add("");
        lines.add("# When Music Triggers starts playing audio, the audio from these categories will be stopped or paused " +
                "dependeing on the channel settings. Only affects blocked audio");
        lines.add(LogUtil.injectParameters("INTERRUPTED_AUDIO_CATEGORIES = {}", TextUtil.compileCollection(INTERRUPTED_AUDIO_CATEGORIES)));
        FileUtil.writeLinesToFile(FILE,lines,false);
        FORMATTED_BLOCKED_MODS = formatBlockedMods();
    }

    public static void read() {
        Toml toml = new Toml().read(FILE);
        SHOW_DEBUG = TomlUtil.readIfExists(toml,"SHOW_DEBUG",SHOW_DEBUG);
        CURRENT_SONG_ONLY = TomlUtil.readIfExists(toml,"CURRENT_SONG_ONLY",CURRENT_SONG_ONLY);
        ALLOW_TIMESTAMPS = TomlUtil.readIfExists(toml,"ALLOW_TIMESTAMPS",ALLOW_TIMESTAMPS);
        LOG_LEVEL = TomlUtil.readIfExists(toml,"LOG_LEVEL",LOG_LEVEL);
        PLAY_NORMAL_MUSIC = TomlUtil.readIfExists(toml,"PLAY_NORMAL_MUSIC",PLAY_NORMAL_MUSIC);
        REVERSE_PRIORITY = TomlUtil.readIfExists(toml,"REVERSE_PRIORITY",REVERSE_PRIORITY);
        COMBINE_EQUAL_PRIORITY = TomlUtil.readIfExists(toml,"COMBINE_EQUAL_PRIORITY",COMBINE_EQUAL_PRIORITY);
        PAUSE_WHEN_TABBED = TomlUtil.readIfExists(toml,"PAUSE_WHEN_TABBED",PAUSE_WHEN_TABBED);
        BLOCKED_MOD_CATEGORIES = new HashSet<>(Arrays.asList(TomlUtil.readGenericArray(toml,"BLOCKED_MOD_CATEGORIES",
                BLOCKED_MOD_CATEGORIES.toArray(new Object[0]))));
        BLOCK_STREAMING_ONLY = TomlUtil.readIfExists(toml,"BLOCK_STREAMING_ONLY",BLOCK_STREAMING_ONLY);
        INTERRUPTED_AUDIO_CATEGORIES = new HashSet<>(Arrays.asList(TomlUtil.readGenericArray(toml,
                "INTERRUPTED_AUDIO_CATEGORIES",INTERRUPTED_AUDIO_CATEGORIES.toArray(new Object[0]))));
    }

    public static void update(Holder data) {
        SHOW_DEBUG = data.getValOrDefault("SHOW_DEBUG",false);
        CURRENT_SONG_ONLY = data.getValOrDefault("CURRENT_SONG_ONLY",false);
        ALLOW_TIMESTAMPS = data.getValOrDefault("ALLOW_TIMESTAMPS",false);
        LOG_LEVEL = data.getValOrDefault("LOG_LEVEL","INFO");
        PLAY_NORMAL_MUSIC = data.getValOrDefault("PLAY_NORMAL_MUSIC",false);
        REVERSE_PRIORITY = data.getValOrDefault("REVERSE_PRIORITY",false);
        COMBINE_EQUAL_PRIORITY = data.getValOrDefault("COMBINE_EQUAL_PRIORITY",false);
        PAUSE_WHEN_TABBED = data.getValOrDefault("PAUSE_WHEN_TABBED",true);
        BLOCKED_MOD_CATEGORIES = new HashSet<>(data.getValOrDefault("BLOCKED_MOD_CATEGORIES",
                Collections.singletonList("minecraft;music")));
        BLOCK_STREAMING_ONLY = data.getValOrDefault("BLOCK_STREAMING_ONLY",true);
        INTERRUPTED_AUDIO_CATEGORIES = new HashSet<>(data.getValOrDefault("INTERRUPTED_AUDIO_CATEGORIES",
                Collections.singletonList("music")));
        write();
    }

    private static HashSet<String> formatBlockedMods() {
        Multimap<String, String> compiled = HashMultimap.create();
        for(String blocked : BLOCKED_MOD_CATEGORIES) {
            String modid = blocked.contains(";") ? blocked.substring(0,blocked.indexOf(';')) : blocked;
            String category = blocked.contains(";") && blocked.indexOf(';')+1<blocked.length() ?
                    blocked.substring(blocked.indexOf(';')+1) : "music";
            if(!compiled.containsEntry(modid,category)) compiled.put(modid,category);
        }
        HashSet<String> ret = new HashSet<>();
        for(String mod : compiled.keySet())
            ret.add(mod+"["+TextUtil.listToString(compiled.get(mod),",")+"]");
        return ret;
    }
}
