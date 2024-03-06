package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

public class TriggerStorming extends SimpleTrigger {

    public TriggerStorming(ChannelAPI channel) {
        super(channel,"storming");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
