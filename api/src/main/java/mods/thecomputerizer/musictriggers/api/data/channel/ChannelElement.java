package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;

import java.util.Objects;

/**
 * Used for any piece of channel data for more consistently available shared info
 */
@Getter
public abstract class ChannelElement extends ParameterWrapper implements ChannelEventHandler {

    protected final ChannelAPI channel;

    protected ChannelElement(ChannelAPI channel, String name) {
        super(name);
        this.channel = channel;
    }

    @Override
    public void activate() {}

    public boolean checkResource() {
        return !isResource() || (this.channel.isClientChannel() && ChannelHelper.resourcesLoaded());
    }

    @Override
    public void deactivate() {}

    public String getChannelName() {
        return Objects.nonNull(this.channel) ? this.channel.getName() : "Unknown";
    }
    
    public String getChannelTypeName() {
        return Objects.nonNull(this.channel) ? this.channel.getTypeName() : "Channel";
    }
    
    protected abstract String getSubTypeName();
    
    protected final String getTypeName() {
        return getChannelTypeName()+"["+getChannelName()+"]: "+getSubTypeName();
    }

    public abstract boolean isResource();

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
    public String toString() {
        return getSubTypeName()+"["+getName()+"]";
    }

    @Override
    public void unplayable() {}
}