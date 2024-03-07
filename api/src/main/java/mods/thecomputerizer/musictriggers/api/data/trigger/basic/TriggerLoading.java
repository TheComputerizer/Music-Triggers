package mods.thecomputerizer.musictriggers.api.data.trigger.basic;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;

public class TriggerLoading extends BasicTrigger {

    public TriggerLoading(ChannelAPI channel) {
        super(channel,"loading");
    }

    @Override
    public boolean isActive(TriggerContextAPI ctx) {
        return ctx.isActiveLoading();
    }
}
