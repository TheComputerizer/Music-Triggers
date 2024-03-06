package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;

import java.util.Arrays;
import java.util.List;

public class TriggerHarvestmoon extends SimpleTrigger {

    public TriggerHarvestmoon(IChannel channel) {
        super(channel,"harvestmoon");
    }

    @Override
    public List<String> getRequiredMods() {
        return Arrays.asList("enhancedcelestials","nyx");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
