package mods.thecomputerizer.musictriggers.api.data.trigger.basic;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerMenu extends BasicTrigger {

    public TriggerMenu(ChannelAPI channel) {
        super(channel,"menu");
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveMenu();
    }
}