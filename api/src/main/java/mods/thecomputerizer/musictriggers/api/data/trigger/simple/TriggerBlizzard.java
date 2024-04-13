package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

import java.util.Collections;
import java.util.List;

public class TriggerBlizzard extends SimpleTrigger {

    public TriggerBlizzard(ChannelAPI channel) {
        super(channel,"blizzard");
    }

    @Override
    public List<String> getRequiredMods() {
        return Collections.singletonList("betterweather");
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveBlizzard();
    }
}
