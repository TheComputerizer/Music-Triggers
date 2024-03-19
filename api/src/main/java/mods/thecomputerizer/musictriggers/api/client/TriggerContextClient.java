package mods.thecomputerizer.musictriggers.api.client;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;

public abstract class TriggerContextClient<PLAYER,WORLD> extends TriggerContextAPI<PLAYER,WORLD> {

    protected TriggerContextClient(ChannelAPI channel) {
        super(channel);
    }

    @Override
    public boolean isClient() {
        return true;
    }
}
