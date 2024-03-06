package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;

public class TriggerCreative extends SimpleTrigger {

    public TriggerCreative(IChannel channel) {
        super(channel,"creative");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
