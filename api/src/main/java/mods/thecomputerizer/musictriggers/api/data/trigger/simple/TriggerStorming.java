package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerStorming extends SimpleTrigger {

    public TriggerStorming(ChannelAPI channel) {
        super(channel,"storming");
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveStorming();
    }
}
