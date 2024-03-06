package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;

public class TriggerSnowing extends SimpleTrigger {

    public TriggerSnowing(IChannel channel) {
        super(channel,"snowing");
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isServer() {
        return true;
    }
}
