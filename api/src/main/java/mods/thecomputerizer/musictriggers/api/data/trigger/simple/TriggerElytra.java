package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

public class TriggerElytra extends SimpleTrigger {

    public TriggerElytra(ChannelAPI channel) {
        super(channel,"elytra");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
