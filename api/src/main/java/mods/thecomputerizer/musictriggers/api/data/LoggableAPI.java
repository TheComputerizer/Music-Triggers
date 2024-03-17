package mods.thecomputerizer.musictriggers.api.data;

public interface LoggableAPI {

    void logAll(String msg, Object ...args);
    void logDebug(String msg, Object ...args);
    void logError(String msg, Object ...args);
    void logFatal(String msg, Object ...args);
    void logInfo(String msg, Object ...args);
    void logTrace(String msg, Object ...args);
    void logWarn(String msg, Object ...args);
}