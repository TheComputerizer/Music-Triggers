package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;

public class TriggerFishing extends SimpleTrigger {

    public TriggerFishing(IChannel channel) {
        super(channel,"fishing");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
