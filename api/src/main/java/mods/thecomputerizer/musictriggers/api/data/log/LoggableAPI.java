package mods.thecomputerizer.musictriggers.api.data.log;

public interface LoggableAPI {

    void logDebug(String msg, Object ...args);
    void logError(String msg, Object ...args);
    void logFatal(String msg, Object ...args);
    void logInfo(String msg, Object ...args);
    void logTrace(String msg, Object ...args);
    void logWarn(String msg, Object ...args);
}