package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;

public class TriggerPVP extends HolderTrigger {

    public TriggerPVP(IChannel channel) {
        super(channel,"pvp");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
