package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;

public class TriggerSpectator extends SimpleTrigger {

    public TriggerSpectator(IChannel channel) {
        super(channel,"spectator");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
