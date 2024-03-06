package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

public class TriggerAdventure extends SimpleTrigger {

    public TriggerAdventure(ChannelAPI channel) {
        super(channel,"adventure");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
