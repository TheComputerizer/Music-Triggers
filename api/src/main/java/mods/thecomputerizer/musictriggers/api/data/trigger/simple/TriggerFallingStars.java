package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;

import java.util.Collections;
import java.util.List;

public class TriggerFallingStars extends SimpleTrigger {

    public TriggerFallingStars(IChannel channel) {
        super(channel,"fallingstars");
    }

    @Override
    public List<String> getRequiredMods() {
        return Collections.singletonList("nyx");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
