package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

public class TriggerSnowing extends SimpleTrigger {

    public TriggerSnowing(ChannelAPI channel) {
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
