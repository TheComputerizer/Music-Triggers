package mods.thecomputerizer.musictriggers.api.data;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.io.LogHelper;
import org.apache.logging.log4j.Level;

/**
 * Used for any piece of channel data for more consistently available shared info
 */
@Getter
public abstract class ChannelData {

    protected final ChannelAPI channel;

    protected ChannelData(ChannelAPI channel) {
        this.channel = channel;
    }

    public String getChannelName() {
        return getChannel().getName();
    }

    public boolean isEnabled() {
        return this.channel.isEnabled();
    }

    /**
     * Logs a channel qualified message both the normal log and MT log
     */
    protected void log(Level level, String msg, Object ... args) {
        msg = "Channel["+getChannelName()+"]: "+msg;
        MTRef.log(level,msg,args);
        ChannelAPI.LOGGER.log(level,msg,args);
    }

    protected void logAll(String msg, Object ... args) {
        log(Level.ALL,msg,args);
    }

    protected void logDebug(String msg, Object ... args) {
        log(Level.DEBUG,msg,args);
    }

    protected void logError(String msg, Object ... args) {
        log(Level.ERROR,msg,args);
    }

    protected void logFatal(String msg, Object ... args) {
        log(Level.FATAL,msg,args);
    }

    protected void logInfo(String msg, Object ... args) {
        log(Level.INFO,msg,args);
    }

    protected void logTrace(String msg, Object ... args) {
        log(Level.TRACE,msg,args);
    }

    protected void logWarn(String msg, Object ... args) {
        log(Level.WARN,msg,args);
    }
}