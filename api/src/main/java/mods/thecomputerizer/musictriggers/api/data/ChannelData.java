package mods.thecomputerizer.musictriggers.api.data;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.channel.IChannel;

/**
 * Used for any piece of channel data for more consistently available shared info
 */
@Getter
public abstract class ChannelData {

    protected final IChannel channel;

    protected ChannelData(IChannel channel) {
        this.channel = channel;
    }

    public String getChannelName() {
        return getChannel().getName();
    }

    public boolean isEnabled() {
        return this.channel.isEnabled();
    }

    /**
     * Qualifyies a message to be logged
     */
    public String qualify(String msg) {
        return "Channel["+getChannelName()+"]: "+msg;
    }
}