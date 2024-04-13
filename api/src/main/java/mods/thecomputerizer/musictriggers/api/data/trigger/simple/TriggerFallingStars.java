package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

import java.util.Collections;
import java.util.List;

public class TriggerFallingStars extends SimpleTrigger {

    public TriggerFallingStars(ChannelAPI channel) {
        super(channel,"fallingstars");
    }

    @Override
    public List<String> getRequiredMods() {
        return Collections.singletonList("nyx");
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveFallingStars();
    }
}
