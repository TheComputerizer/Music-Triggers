package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;

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

    @Override
    public void deactivate() {}

    public String getChannelName() {
        return getChannel().getName();
    }

    public boolean isEnabled() {
        return this.channel.isEnabled();
    }

    public abstract boolean isResource();

    @Override
    public void logAll(String msg, Object ... args) {
        MTLogger.logAll("Channel",getChannelName(),msg,args);
    }

    @Override
    public void logDebug(String msg, Object ... args) {
        MTLogger.logDebug("Channel",getChannelName(),msg,args);
    }

    @Override
    public void logError(String msg, Object ... args) {
        MTLogger.logError("Channel",getChannelName(),msg,args);
    }

    @Override
    public void logFatal(String msg, Object ... args) {
        MTLogger.logFatal("Channel",getChannelName(),msg,args);
    }

    @Override
    public void logInfo(String msg, Object ... args) {
        MTLogger.logInfo("Channel",getChannelName(),msg,args);
    }

    @Override
    public void logTrace(String msg, Object ... args) {
        MTLogger.logTrace("Channel",getChannelName(),msg,args);
    }

    @Override
    public void logWarn(String msg, Object ... args) {
        MTLogger.logWarn("Channel",getChannelName(),msg,args);
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

    @Override
    public void tickActive() {}

    @Override
    public void tickPlayable() {}

    @Override
    public void unplayable() {}
}