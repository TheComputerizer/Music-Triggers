package mods.thecomputerizer.musictriggers.api;

import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.Reference;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Random;

public class MTRef {

    public static final String CONFIG_PATH = "config/MusicTriggers";
    public static final String GLOBAL_CONFIG = "config/MusicTriggers/global";
    public static final Logger LOGGER = LogManager.getLogger("Music Triggers");
    public static final String MODID = "musictriggers";
    public static final String NAME = "Music Triggers";
    public static final Random RANDOM = new Random();
    public static final String VERSION = "7.0.0";

    public static void log(Level level, String msg, Object ... args) {
        LOGGER.log(level,msg,args);
    }

    public static void logAll(String msg, Object ... args) {
        log(Level.ALL,msg,args);
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

    public static void logOff(String msg, Object ... args) {
        log(Level.OFF,msg,args);
    }

    public static void logTrace(String msg, Object ... args) {
        log(Level.TRACE,msg,args);
    }

    public static void logWarn(String msg, Object ... args) {
        log(Level.WARN,msg,args);
    }

    private static int parse(LoggableAPI logger, String parameter, String element, int fallback) {
        try {
            return Integer.parseInt(element);
        } catch (NumberFormatException ignored) {
            logger.logWarn("Invalid element {} for parameter {}! Using fallback {}",element,parameter,
                    fallback);
            return fallback;
        }
    }

    public static int randomInt(int max) {
        return RANDOM.nextInt(max);
    }

    /**
     * Uses a fallback in case someone decides to add something that is not a number to a number parameter
     */
    public static int randomInt(LoggableAPI logger, String parameter, String toConvert, int fallback) {
        String[] broken = toConvert.split(":");
        if(broken.length==1) return parse(logger,parameter, broken[0], fallback);
        int min = parse(logger,parameter,broken[0],fallback);
        int max = parse(logger,parameter,broken[1],fallback);
        if(min==max) return min;
        else if(min>max) {
            int temp = max;
            max = min;
            min = temp;
        }
        if(max-min<=0) return min;
        return min+RANDOM.nextInt(max-min);
    }

    public static @Nullable ResourceLocationAPI<?> res(String path) {
        return ResourceHelper.getResource(MODID,path);
    }

    private static final class MTRefInstance extends Reference {

        private MTRefInstance(boolean client, @Nullable String dependencies, @Nullable Logger logger,
                              @Nullable String modid, @Nullable String name, @Nullable String version) {
            super(client,dependencies,logger,modid,name,version);
        }
    }
}
