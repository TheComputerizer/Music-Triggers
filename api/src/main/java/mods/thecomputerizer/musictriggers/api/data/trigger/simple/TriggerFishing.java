package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

public class TriggerFishing extends SimpleTrigger {

    public TriggerFishing(ChannelAPI channel) {
        super(channel,"fishing");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
