package mods.thecomputerizer.musictriggers.api.data.trigger.basic;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

public class TriggerGeneric extends BasicTrigger {

    public TriggerGeneric(ChannelAPI channel) {
        super(channel,"generic");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}