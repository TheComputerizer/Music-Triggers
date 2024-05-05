package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerHome extends SimpleTrigger {

    public TriggerHome(ChannelAPI channel) {
        super(channel,"home");
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveHome(getParameterAsInt("detection_range"),getParameterAsFloat("detection_y_ratio"));
    }

    @Override
    public boolean isServer() {
        return true;
    }
}
