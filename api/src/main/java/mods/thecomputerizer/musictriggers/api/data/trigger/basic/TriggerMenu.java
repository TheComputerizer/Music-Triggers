package mods.thecomputerizer.musictriggers.api.data.trigger.basic;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;

public class TriggerMenu extends BasicTrigger {

    public TriggerMenu(IChannel channel) {
        super(channel,"menu");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
