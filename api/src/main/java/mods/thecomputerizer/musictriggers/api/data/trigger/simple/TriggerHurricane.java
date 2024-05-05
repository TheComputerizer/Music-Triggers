package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

import java.util.Collections;
import java.util.List;

public class TriggerHurricane extends SimpleTrigger {

    public TriggerHurricane(ChannelAPI channel) {
        super(channel,"hurricane");
    }

    @Override
    public List<String> getRequiredMods() {
        return Collections.singletonList("weather2");
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveHurricane(getParameterAsInt("detection_range"));
    }
}
