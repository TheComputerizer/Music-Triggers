package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;

public class TriggerElytra extends SimpleTrigger {

    public TriggerElytra(ChannelAPI channel) {
        super(channel,"elytra");
    }

    @Override
    public boolean isActive(TriggerContextAPI ctx) {
        return ctx.isActiveElytra();
    }
}
