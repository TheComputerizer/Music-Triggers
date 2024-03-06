package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TriggerBluemoon extends SimpleTrigger {

    public TriggerBluemoon(IChannel channel) {
        super(channel,"bluemoon");
    }

    @Override
    public List<String> getRequiredMods() {
        return Collections.singletonList("enhancedcelestials");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
