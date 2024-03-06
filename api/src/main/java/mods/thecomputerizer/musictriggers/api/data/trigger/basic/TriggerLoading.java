package mods.thecomputerizer.musictriggers.api.data.trigger.basic;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

public class TriggerLoading extends BasicTrigger {

    public TriggerLoading(ChannelAPI channel) {
        super(channel,"loading");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
