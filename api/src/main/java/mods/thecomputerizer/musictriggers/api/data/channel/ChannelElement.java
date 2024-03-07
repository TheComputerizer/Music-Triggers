package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import org.apache.logging.log4j.Level;

/**
 * Used for any piece of channel data for more consistently available shared info
 */
@Getter
public abstract class ChannelElement {

    protected final ChannelAPI channel;

    protected ChannelElement(ChannelAPI channel) {
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
    public void log(Level level, String msg, Object ... args) {
        msg = "Channel["+getChannelName()+"]: "+msg;
        MTRef.log(level,msg,args);
        ChannelAPI.LOGGER.log(level,msg,args);
    }

    public void logAll(String msg, Object ... args) {
        log(Level.ALL,msg,args);
    }

    public void logDebug(String msg, Object ... args) {
        log(Level.DEBUG,msg,args);
    }

    public void logError(String msg, Object ... args) {
        log(Level.ERROR,msg,args);
    }

    public void logFatal(String msg, Object ... args) {
        log(Level.FATAL,msg,args);
    }

    public void logInfo(String msg, Object ... args) {
        log(Level.INFO,msg,args);
    }

    public void logTrace(String msg, Object ... args) {
        log(Level.TRACE,msg,args);
    }

    public void logWarn(String msg, Object ... args) {
        log(Level.WARN,msg,args);
    }
}