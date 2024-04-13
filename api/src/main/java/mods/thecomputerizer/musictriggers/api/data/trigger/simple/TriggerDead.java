package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerDead extends SimpleTrigger {

    public TriggerDead(ChannelAPI channel) {
        super(channel,"dead");
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveDead();
    }
}
