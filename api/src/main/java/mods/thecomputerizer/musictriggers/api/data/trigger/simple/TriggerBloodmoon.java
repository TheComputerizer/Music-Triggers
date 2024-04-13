package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

import java.util.Arrays;
import java.util.List;

public class TriggerBloodmoon extends SimpleTrigger {

    public TriggerBloodmoon(ChannelAPI channel) {
        super(channel,"bloodmoon");
    }

    @Override
    public List<String> getRequiredMods() {
        return Arrays.asList("bloodmoon","enhancedcelestials","nyx");
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveBloodMoon();
    }
}
