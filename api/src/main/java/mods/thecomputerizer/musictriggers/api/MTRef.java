package mods.thecomputerizer.musictriggers.api;

import mods.thecomputerizer.theimpossiblelibrary.api.Reference;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public class MTRef {

    public static final Logger LOGGER = LogManager.getLogger("Music Triggers");
    public static final String MODID = "musictriggers";
    public static final String NAME = "Music Triggers";
    public static final String VERSION = "7.0.0";
    private static Reference INSTANCE;

    /**
     * Initializes the base reference API
     */
    public static Reference instance(Supplier<Boolean> client, String dependencies) {
        if(Objects.isNull(INSTANCE)) INSTANCE = new MTRefInstance(client.get(),dependencies,LOGGER,MODID,NAME,VERSION);
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

    public static @Nullable ResourceLocationAPI<?> res(String path) {
        if(Objects.nonNull(INSTANCE)) return INSTANCE.getResource(path);
        logError("Cannot get a ResourceLocation until the reference API has been initialized!");
        return null;
    }

    private static final class MTRefInstance extends Reference {

        private MTRefInstance(boolean client, @Nullable String dependencies, @Nullable Logger logger,
                              @Nullable String modid, @Nullable String name, @Nullable String version) {
            super(client,dependencies,logger,modid,name,version);
        }
    }
}
