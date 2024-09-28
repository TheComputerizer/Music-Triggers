package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerElytra extends SimpleTrigger {

    public TriggerElytra(ChannelAPI channel) {
        super(channel,"elytra");
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveElytra();
    }
}
