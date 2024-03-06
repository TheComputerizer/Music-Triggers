package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

public class TriggerPVP extends HolderTrigger {

    public TriggerPVP(ChannelAPI channel) {
        super(channel,"pvp");
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isServer() {
        return true;
    }
}
