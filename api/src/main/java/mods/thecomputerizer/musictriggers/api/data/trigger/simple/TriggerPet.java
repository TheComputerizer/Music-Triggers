package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerPet extends SimpleTrigger {

    public TriggerPet(ChannelAPI channel) {
        super(channel,"pet");
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActivePet(getParameterAsInt("detection_range"),getParameterAsFloat("detection_y_ratio"));
    }
}
