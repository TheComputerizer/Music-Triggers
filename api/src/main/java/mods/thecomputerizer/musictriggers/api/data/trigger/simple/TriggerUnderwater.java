package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;

public class TriggerUnderwater extends SimpleTrigger {

    public TriggerUnderwater(IChannel channel) {
        super(channel,"underwater");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
