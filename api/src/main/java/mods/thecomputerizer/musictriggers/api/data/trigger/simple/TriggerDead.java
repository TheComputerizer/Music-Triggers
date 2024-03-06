package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;

public class TriggerDead extends SimpleTrigger {

    public TriggerDead(IChannel channel) {
        super(channel,"dead");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
