package mods.thecomputerizer.musictriggers.api.data.trigger;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioPool;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;

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
    public @Nullable AudioPool getAudioPool() {
        Set<AudioPool> pools = new HashSet<>();
        for(TriggerAPI trigger : this.triggers) {
            AudioPool pool = trigger.getAudioPool();
            if(Objects.nonNull(pool)) pools.add(pool);
        }
        return AudioPool.unsafeMerge(pools);
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {

    }

    @Override
    public boolean verifyRequiredParameters() {
        return false;
    }

    @Override
    public boolean isActive(TriggerContextAPI<?,?> context) {
        for(TriggerAPI trigger : this.triggers)
            if(trigger.isActive(context)) return true;
        return false;
    }

    @Override
    public boolean matches(TriggerAPI other) {
        for(TriggerAPI trigger : this.triggers)
            if(trigger.matches(other)) return true;
        return false;
    }
}
