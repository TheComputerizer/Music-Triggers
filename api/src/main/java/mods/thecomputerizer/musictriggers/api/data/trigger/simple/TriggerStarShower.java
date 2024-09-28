package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

import java.util.Collections;
import java.util.List;

public class TriggerStarShower extends SimpleTrigger {

    public TriggerStarShower(ChannelAPI channel) {
        super(channel,"starshower");
    }

    @Override public List<String> getRequiredMods() {
        return Collections.singletonList("nyx");
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveStarShower();
    }
}
