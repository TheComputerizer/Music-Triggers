package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;

public class TriggerAdventure extends SimpleTrigger {

    public TriggerAdventure(IChannel channel) {
        super(channel,"adventure");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
