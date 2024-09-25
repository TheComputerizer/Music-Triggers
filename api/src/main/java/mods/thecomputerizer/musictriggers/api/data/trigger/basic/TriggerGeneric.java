package mods.thecomputerizer.musictriggers.api.data.trigger.basic;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerGeneric extends BasicTrigger {

    public TriggerGeneric(ChannelAPI channel) {
        super(channel,"generic");
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveGeneric();
    }
}