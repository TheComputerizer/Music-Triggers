package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;

public class TriggerCommand extends HolderTrigger {

    public TriggerCommand(IChannel channel) {
        super(channel,"command");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
