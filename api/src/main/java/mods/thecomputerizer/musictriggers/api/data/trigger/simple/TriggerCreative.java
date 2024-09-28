package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerCreative extends SimpleTrigger {

    public TriggerCreative(ChannelAPI channel) {
        super(channel,"creative");
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveCreative();
    }
}
