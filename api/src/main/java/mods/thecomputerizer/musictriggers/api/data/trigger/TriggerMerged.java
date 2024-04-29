package mods.thecomputerizer.musictriggers.api.data.trigger;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioPool;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Only used when COMBINE_EQUAL_PRIORITY is enabled
 */
@Getter
public class TriggerMerged extends TriggerAPI {

    private final Collection<TriggerAPI> triggers;

    public TriggerMerged(ChannelAPI channel, Collection<TriggerAPI> triggers) {
        super(channel,"merged");
        this.triggers = triggers;
    }

    @Override
    public void activate() {
        for(TriggerAPI trigger : this.triggers) trigger.activate();
    }

    @Override
    public void close() {
        super.close();
        this.triggers.clear();
    }

    @Override
    public void deactivate() {
        for(TriggerAPI trigger : this.triggers) trigger.deactivate();
    }

    @Override
    public void encode(ByteBuf buf) {
        super.encode(buf);
        NetworkHelper.writeCollection(buf,this.triggers,trigger -> trigger.encode(buf));
    }

    @Override
    public @Nullable AudioPool getAudioPool() {
        Set<AudioPool> pools = new HashSet<>();
        for(TriggerAPI trigger : this.triggers) {
            AudioPool pool = trigger.getAudioPool();
            if(Objects.nonNull(pool)) pools.add(pool);
        }
        return AudioPool.unsafeMerge(pools);
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {}

    @Override
    public boolean isPlayableContext(TriggerContext context) {
        for(TriggerAPI trigger : this.triggers)
            if(trigger.isPlayableContext(context)) return true;
        return false;
    }

    @Override
    public boolean isContained(Collection<TriggerAPI> triggers) {
        for(TriggerAPI trigger : this.triggers)
            if(trigger.isContained(triggers)) return true;
        return false;
    }

    @Override
    public boolean matches(TriggerAPI other) {
        for(TriggerAPI trigger : this.triggers)
            if(trigger.matches(other)) return true;
        return false;
    }

    @Override
    public void queue() {
        for(TriggerAPI trigger : this.triggers) trigger.queue();
    }

    @Override
    public void play() {
        for(TriggerAPI trigger : this.triggers) trigger.play();
    }

    @Override
    public void playable() {
        for(TriggerAPI trigger : this.triggers) trigger.playable();
    }

    @Override
    public void playing() {
        for(TriggerAPI trigger : this.triggers) trigger.playing();
    }

    @Override
    public void stop() {
        for(TriggerAPI trigger : this.triggers) trigger.stop();
    }

    @Override
    public void stopped() {
        for(TriggerAPI trigger : this.triggers) trigger.stopped();
    }

    @Override
    public void tickActive() {
        for(TriggerAPI trigger : this.triggers) trigger.tickActive();
    }

    @Override
    public void tickPlayable() {
        for(TriggerAPI trigger : this.triggers) trigger.tickPlayable();
    }

    @Override
    public String toString() {
        return "Merged"+this.triggers;
    }

    @Override
    public void unplayable() {
        for(TriggerAPI trigger : this.triggers) trigger.unplayable();
    }

    @Override
    public boolean verifyRequiredParameters() {
        return true;
    }
}
