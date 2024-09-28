package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

import java.util.Collections;
import java.util.List;

public class TriggerRainIntensity extends HolderTrigger {

    public TriggerRainIntensity(ChannelAPI channel) {
        super(channel,"rainintensity");
    }

    @Override public List<String> getRequiredMods() {
        return Collections.singletonList("dsurround");
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveRainIntensity(getParameterAsFloat("level"));
    }

    @Override public boolean verifyRequiredParameters() {
        return hasValidIdentifier();
    }
}
