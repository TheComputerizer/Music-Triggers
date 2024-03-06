package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

public class TriggerRaining extends SimpleTrigger {

    public TriggerRaining(ChannelAPI channel) {
        super(channel,"raining");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
