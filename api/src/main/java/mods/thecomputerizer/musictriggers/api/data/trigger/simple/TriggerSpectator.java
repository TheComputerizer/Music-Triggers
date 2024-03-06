package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

public class TriggerSpectator extends SimpleTrigger {

    public TriggerSpectator(ChannelAPI channel) {
        super(channel,"spectator");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
