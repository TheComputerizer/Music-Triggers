package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerLowHP extends SimpleTrigger {

    public TriggerLowHP(ChannelAPI channel) {
        super(channel,"lowhp");
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        float min = getParameterAsFloat("min_health_percentage")/100f;
        float max = getParameterAsFloat("max_health_percentage")/100f;
        return ctx.isActiveLowHP(min,max);
    }
}