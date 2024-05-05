package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerLowHP extends SimpleTrigger {

    public TriggerLowHP(ChannelAPI channel) {
        super(channel,"lowhp");
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveLowHP(getParameterAsFloat("health_percentage"));
    }
}
