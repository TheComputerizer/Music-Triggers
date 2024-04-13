package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerUnderwater extends SimpleTrigger {

    public TriggerUnderwater(ChannelAPI channel) {
        super(channel,"underwater");
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveUnderwater();
    }
}
