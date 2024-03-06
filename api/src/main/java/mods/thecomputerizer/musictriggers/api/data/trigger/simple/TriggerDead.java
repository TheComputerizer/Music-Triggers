package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

public class TriggerDead extends SimpleTrigger {

    public TriggerDead(ChannelAPI channel) {
        super(channel,"dead");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
