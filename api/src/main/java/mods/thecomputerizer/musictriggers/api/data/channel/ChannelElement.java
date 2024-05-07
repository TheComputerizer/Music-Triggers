package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;

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
        return getChannel().getName();
    }
    
    protected abstract String getSubTypeName();
    
    protected final String getTypeName() {
        return "Channel["+this.channel.getName()+"]: "+getSubTypeName();
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
    public void unplayable() {}
}