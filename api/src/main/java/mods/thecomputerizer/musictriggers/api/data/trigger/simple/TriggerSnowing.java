package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerSnowing extends SimpleTrigger {

    public TriggerSnowing(ChannelAPI channel) {
        super(channel,"snowing");
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveSnowing();
    }

    @Override
    public boolean isServer() {
        return true;
    }
}
