package mods.thecomputerizer.musictriggers.api.data.log;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.theimpossiblelibrary.api.core.TILDev;
import mods.thecomputerizer.theimpossiblelibrary.api.io.LogHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.io.LogHelper.ModLogger;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MTLogger {
    public static final ModLogger LOGGER = LogHelper.create(MTRef.MODID);
    private static final List<LogMessage> LOGGED_MESSAGES = new ArrayList<>();
    private static int reloadIndex = 0;
    
    public static List<LogMessage> getGUISnapshot() {
        List<LogMessage> snapshot = new ArrayList<>();
        for(int i=reloadIndex;i<LOGGED_MESSAGES.size();i++) snapshot.add(LOGGED_MESSAGES.get(i));
        return Collections.unmodifiableList(snapshot);
    }

    /**
     * Logs a channel qualified message both the normal log and MT log
     */
    public static void log(String type, String typeName, Level level, String msg, Object ... args) {
        synchronized(LOGGER) {
            LOGGED_MESSAGES.add(new LogMessage(level,type+"["+typeName+"]: "+msg,args).log(LOGGER));
        }
    }

    public static void logDebug(String type, String typeName, String msg, Object ... args) {
        log(type,typeName,Level.DEBUG,msg,args);
    }

    public static void logError(String type, String typeName, String msg, Object ... args) {
        log(type,typeName,Level.ERROR,msg,args);
    }

    public static void logFatal(String type, String typeName, String msg, Object ... args) {
        log(type,typeName,Level.FATAL,msg,args);
    }

    public static void logInfo(String type, String typeName, String msg, Object ... args) {
        log(type,typeName,Level.INFO,msg,args);
    }

    public static void logTrace(String type, String typeName, String msg, Object ... args) {
        if(TILDev.DEV) log(type,typeName,Level.TRACE,msg,args);
    }

    public static void logWarn(String type, String typeName, String msg, Object ... args) {
        log(type,typeName,Level.WARN,msg,args);
    }

    public static void onReloadQueued() {
        reloadIndex = LOGGED_MESSAGES.size();
    }
}
