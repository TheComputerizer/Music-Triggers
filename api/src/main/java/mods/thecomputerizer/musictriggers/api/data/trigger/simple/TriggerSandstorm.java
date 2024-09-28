package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

import java.util.Collections;
import java.util.List;

public class TriggerSandstorm extends SimpleTrigger {

    public TriggerSandstorm(ChannelAPI channel) {
        super(channel,"sandstorm");
    }

    @Override public List<String> getRequiredMods() {
        return Collections.singletonList("weather2");
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveSandstorm(getParameterAsInt("detection_range"));
    }
}
