package mods.thecomputerizer.musictriggers.api.data.trigger.basic;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;

public class TriggerLoading extends BasicTrigger {

    public TriggerLoading(IChannel channel) {
        super(channel,"loading");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
