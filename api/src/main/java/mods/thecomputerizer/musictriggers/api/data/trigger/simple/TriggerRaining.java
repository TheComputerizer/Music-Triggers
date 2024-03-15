package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;

public class TriggerRaining extends SimpleTrigger {

    public TriggerRaining(ChannelAPI channel) {
        super(channel,"raining");
    }

    @Override
    public boolean isActive(TriggerContextAPI<?,?> ctx) {
        return ctx.isActiveRaining();
    }
}
