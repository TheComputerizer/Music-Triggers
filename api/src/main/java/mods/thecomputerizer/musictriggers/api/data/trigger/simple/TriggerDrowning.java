package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerDrowning extends SimpleTrigger {

    public TriggerDrowning(ChannelAPI channel) {
        super(channel,"drowning");
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveDrowning(getParameterAsInt("level"));
    }
}
