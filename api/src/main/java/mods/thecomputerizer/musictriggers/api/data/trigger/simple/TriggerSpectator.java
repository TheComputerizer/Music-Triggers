package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerSpectator extends SimpleTrigger {

    public TriggerSpectator(ChannelAPI channel) {
        super(channel,"spectator");
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveSpectator();
    }
}
