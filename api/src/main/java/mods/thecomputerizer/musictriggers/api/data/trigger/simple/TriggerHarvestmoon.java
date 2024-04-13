package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

import java.util.Arrays;
import java.util.List;

public class TriggerHarvestmoon extends SimpleTrigger {

    public TriggerHarvestmoon(ChannelAPI channel) {
        super(channel,"harvestmoon");
    }

    @Override
    public List<String> getRequiredMods() {
        return Arrays.asList("enhancedcelestials","nyx");
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveHarvestMoon();
    }
}
