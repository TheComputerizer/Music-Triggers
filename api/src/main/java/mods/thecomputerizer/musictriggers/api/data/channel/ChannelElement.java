package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;

import javax.annotation.Nullable;
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

    protected ChannelElement(ChannelAPI channel, String name, @Nullable TableRef ref) {
        super(name,ref);
        this.channel = channel;
    }

    @Override public void activate() {}

    public boolean checkResource() {
        return !isResource() || (this.channel.isClientChannel() && ChannelHelper.getLoader().areResourcesLoaded());
    }

    @Override public void deactivate() {}

    public String getChannelName() {
        return Objects.nonNull(this.channel) ? this.channel.getName() : "unknown";
    }
    
    public String getChannelLogPrefix() {
        return (Objects.nonNull(this.channel) ? this.channel.getLogType() : "UNKNOWN")+" | Channel";
    }
    
    protected String getLogPrefix() {
        return getChannelLogPrefix()+"["+getChannelName()+"]: "+getSubTypeName();
    }
    
    protected abstract String getSubTypeName();

    public abstract boolean isResource();

    @Override public void play(boolean unpaused) {}

    @Override public void playable() {}

    @Override public void playing(boolean unpaused) {}

    @Override public void queue() {}

    @Override public void stop() {}

    @Override public void stopped() {}

    @Override public void tickActive(boolean unpaused) {}

    @Override public void tickPlayable(boolean unpaused) {}
    
    @Override public String toString() {
        return getSubTypeName()+"["+getName()+"]";
    }

    @Override public void unplayable() {}
}