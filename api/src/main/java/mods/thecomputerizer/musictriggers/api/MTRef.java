package mods.thecomputerizer.musictriggers.api;

import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

@SuppressWarnings("unused") public class MTRef {

    public static final String CONFIG_PATH = "config/MusicTriggers";
    public static final String GLOBAL_CONFIG = "config/MusicTriggers/global";
    public static final Logger LOGGER = LogManager.getLogger("Music Triggers");
    public static final String MODID = "musictriggers";
    public static final String NAME = "Music Triggers";
    public static final String VERSION = "7.0.0+beta-4";

    public static void log(Level level, String msg, Object ... args) {
        LOGGER.log(level,msg,args);
    }

    public static void logDebug(String msg, Object ... args) {
        log(Level.DEBUG,msg,args);
    }

    public static void logError(String msg, Object ... args) {
        log(Level.ERROR,msg,args);
    }

    public static void logFatal(String msg, Object ... args) {
        log(Level.FATAL,msg,args);
    }

    public static void logInfo(String msg, Object ... args) {
        log(Level.INFO,msg,args);
    }

    public static void logTrace(String msg, Object ... args) {
        log(Level.TRACE,msg,args);
    }

    public static void logWarn(String msg, Object ... args) {
        log(Level.WARN,msg,args);
    }

    public static @Nullable ResourceLocationAPI<?> res(String path) {
        return ResourceHelper.getResource(MODID,path);
    }
}
