package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;

public class TriggerStorming extends SimpleTrigger {

    public TriggerStorming(IChannel channel) {
        super(channel,"storming");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
