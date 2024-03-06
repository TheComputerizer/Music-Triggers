package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

public class TriggerCreative extends SimpleTrigger {

    public TriggerCreative(ChannelAPI channel) {
        super(channel,"creative");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
