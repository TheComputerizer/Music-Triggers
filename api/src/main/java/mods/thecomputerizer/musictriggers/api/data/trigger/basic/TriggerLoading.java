package mods.thecomputerizer.musictriggers.api.data.trigger.basic;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerLoading extends BasicTrigger {

    public TriggerLoading(ChannelAPI channel) {
        super(channel,"loading");
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveLoading();
    }
}