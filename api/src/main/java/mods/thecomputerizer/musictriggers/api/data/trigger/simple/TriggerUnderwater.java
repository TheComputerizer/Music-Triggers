package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

public class TriggerUnderwater extends SimpleTrigger {

    public TriggerUnderwater(ChannelAPI channel) {
        super(channel,"underwater");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
