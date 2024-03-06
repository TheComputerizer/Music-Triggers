package mods.thecomputerizer.musictriggers.api.data.trigger.basic;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;

public class TriggerGeneric extends BasicTrigger {

    public TriggerGeneric(IChannel channel) {
        super(channel,"generic");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}