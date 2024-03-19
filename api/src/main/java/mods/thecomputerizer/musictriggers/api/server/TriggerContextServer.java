package mods.thecomputerizer.musictriggers.api.server;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;

public abstract class TriggerContextServer<PLAYER,WORLD> extends TriggerContextAPI<PLAYER,WORLD> {

    protected TriggerContextServer(ChannelAPI channel) {
        super(channel);
    }

    @Override
    public boolean isClient() {
        return false;
    }
}