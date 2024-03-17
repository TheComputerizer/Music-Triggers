package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.LoggableAPI;
import org.apache.logging.log4j.Level;

/**
 * Used for any piece of channel data for more consistently available shared info
 */
@Getter
public abstract class ChannelElement implements ChannelEventHandler, LoggableAPI {

    protected final ChannelAPI channel;

    protected ChannelElement(ChannelAPI channel) {
        this.channel = channel;
    }

    @Override
    public void activate() {}

    public String getChannelName() {
        return getChannel().getName();
    }

    public boolean isEnabled() {
        return this.channel.isEnabled();
    }

    @Override
    public void logAll(String msg, Object ... args) {
        ChannelAPI.log("Channel",getChannelName(),Level.ALL,msg,args);
    }

    @Override
    public void logDebug(String msg, Object ... args) {
        ChannelAPI.log("Channel",getChannelName(),Level.DEBUG,msg,args);
    }

    @Override
    public void logError(String msg, Object ... args) {
        ChannelAPI.log("Channel",getChannelName(),Level.ERROR,msg,args);
    }

    @Override
    public void logFatal(String msg, Object ... args) {
        ChannelAPI.log("Channel",getChannelName(),Level.FATAL,msg,args);
    }

    @Override
    public void logInfo(String msg, Object ... args) {
        ChannelAPI.log("Channel",getChannelName(),Level.INFO,msg,args);
    }

    @Override
    public void logTrace(String msg, Object ... args) {
        ChannelAPI.log("Channel",getChannelName(),Level.TRACE,msg,args);
    }

    @Override
    public void logWarn(String msg, Object ... args) {
        ChannelAPI.log("Channel",getChannelName(),Level.WARN,msg,args);
    }

    @Override
    public void play() {}

    @Override
    public void playable() {}

    @Override
    public void playing() {}

    @Override
    public void queue() {}

    @Override
    public void stop() {}

    @Override
    public void stopped() {}
}