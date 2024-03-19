package mods.thecomputerizer.musictriggers.api.server;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerSelectorAPI;

public abstract class TriggerSelectorServer<PLAYER,WORLD> extends TriggerSelectorAPI<PLAYER,WORLD> {

    protected TriggerSelectorServer(ChannelAPI channel, TriggerContextServer<PLAYER,WORLD> context) {
        super(channel,context);
    }

    @Override
    public boolean isClient() {
        return false;
    }
}
