package mods.thecomputerizer.musictriggers.api.data;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

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
     * Qualifyies a message to be logged
     */
    public String qualified() {
        return "Channel["+getChannelName()+"]: ";
    }
}