package mods.thecomputerizer.musictriggers.api;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.Reference;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

public class MTRef {

    public static final String CONFIG_PATH = "config/MusicTriggers";
    public static final String GLOBAL_CONFIG = "config/MusicTriggers/global";
    public static final Logger LOGGER = LogManager.getLogger("Music Triggers");
    public static final String MODID = "musictriggers";
    public static final String NAME = "Music Triggers";
    public static final Random RANDOM = new Random();
    public static final String VERSION = "7.0.0";
    private static Reference INSTANCE;

    public static @Nullable MTAPI getAPI() {
        return Objects.nonNull(INSTANCE) ? ((MTRefInstance)INSTANCE).api : null;
    }

    /**
     * Initializes the base reference API
     */
    public static Reference instance(MTAPI api, Supplier<Boolean> client, String dependencies) throws IOException {
        if(Objects.isNull(INSTANCE)) {
            INSTANCE = new MTRefInstance(client.get(),dependencies,LOGGER,MODID,NAME,VERSION);
            ((MTRefInstance)INSTANCE).api = api;
            api.init();
        }
        return INSTANCE;
    }

    public static void log(Level level, String msg, Object ... args) {
        logNullable(level,msg,args);
    }

    public static void logAll(String msg, Object ... args) {
        logNullable(Level.ALL,msg,args);
    }

    public static void logDebug(String msg, Object ... args) {
        logNullable(Level.DEBUG,msg,args);
    }

    public static void logError(String msg, Object ... args) {
        logNullable(Level.ERROR,msg,args);
    }

    public static void logFatal(String msg, Object ... args) {
        logNullable(Level.FATAL,msg,args);
    }

    public static void logInfo(String msg, Object ... args) {
        logNullable(Level.INFO,msg,args);
    }

    private static void logNullable(Level level, String msg, Object ... args) {
        if(Objects.nonNull(INSTANCE)) INSTANCE.log(level,msg,args);
        else LOGGER.log(level,msg,args);
    }

    public static void logOff(String msg, Object ... args) {
        logNullable(Level.OFF,msg,args);
    }

    public static void logTrace(String msg, Object ... args) {
        logNullable(Level.TRACE,msg,args);
    }

    public static void logWarn(String msg, Object ... args) {
        logNullable(Level.WARN,msg,args);
    }

    private static int parse(ChannelAPI channel, String parameter, String element, int fallback) {
        try {
            return Integer.parseInt(element);
        } catch (NumberFormatException ignored) {
            channel.logWarn("Invalid element {} for parameter {}! Using fallback {}",element,parameter,
                    fallback);
            return fallback;
        }
    }

    public static int randomInt(ChannelAPI channel, int max) {
        return RANDOM.nextInt(max);
    }

    /**
     * Uses a fallback in case someone decides to add something that is not a number to a number parameter
     */
    public static int randomInt(ChannelAPI channel, String parameter, String toConvert, int fallback) {
        String[] broken = toConvert.split(":");
        if(broken.length==1) return parse(channel,parameter, broken[0], fallback);
        int min = parse(channel,parameter,broken[0],fallback);
        int max = parse(channel,parameter,broken[1],fallback);
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
        if(Objects.nonNull(INSTANCE)) return INSTANCE.getResource(path);
        logError("Cannot get a ResourceLocation until the reference API has been initialized!");
        return null;
    }

    private static final class MTRefInstance extends Reference {

        private MTAPI api;

        private MTRefInstance(boolean client, @Nullable String dependencies, @Nullable Logger logger,
                              @Nullable String modid, @Nullable String name, @Nullable String version) {
            super(client,dependencies,logger,modid,name,version);
        }
    }
}
