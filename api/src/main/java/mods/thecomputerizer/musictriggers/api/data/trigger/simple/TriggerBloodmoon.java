package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TriggerBloodmoon extends SimpleTrigger {

    public TriggerBloodmoon(IChannel channel) {
        super(channel,"bloodmoon");
    }

    @Override
    public List<String> getRequiredMods() {
        return Arrays.asList("bloodmoon","enhancedcelestials","nyx");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
