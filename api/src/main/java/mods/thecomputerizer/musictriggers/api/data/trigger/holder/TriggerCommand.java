package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

public class TriggerCommand extends HolderTrigger {

    public TriggerCommand(ChannelAPI channel) {
        super(channel,"command");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
