package mods.thecomputerizer.musictriggers.api.data.trigger.basic;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

public class TriggerMenu extends BasicTrigger {

    public TriggerMenu(ChannelAPI channel) {
        super(channel,"menu");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
