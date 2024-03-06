package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;

public class TriggerRaining extends SimpleTrigger {

    public TriggerRaining(IChannel channel) {
        super(channel,"raining");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
