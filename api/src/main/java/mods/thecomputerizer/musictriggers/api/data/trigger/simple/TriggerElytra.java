package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;

public class TriggerElytra extends SimpleTrigger {

    public TriggerElytra(IChannel channel) {
        super(channel,"elytra");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
