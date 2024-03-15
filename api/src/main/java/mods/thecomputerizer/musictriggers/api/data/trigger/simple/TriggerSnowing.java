package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;

public class TriggerSnowing extends SimpleTrigger {

    public TriggerSnowing(ChannelAPI channel) {
        super(channel,"snowing");
    }

    @Override
    public boolean isActive(TriggerContextAPI<?,?> ctx) {
        return ctx.isActiveSnowing();
    }

    @Override
    public boolean isServer() {
        return true;
    }
}
