package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerRaining extends SimpleTrigger {

    public TriggerRaining(ChannelAPI channel) {
        super(channel,"raining");
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveRaining();
    }
}
