package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

import java.util.Collections;
import java.util.List;

public class TriggerBluemoon extends SimpleTrigger {

    public TriggerBluemoon(ChannelAPI channel) {
        super(channel,"bluemoon");
    }

    @Override public List<String> getRequiredMods() {
        return Collections.singletonList("enhancedcelestials");
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveBlueMoon();
    }
}
