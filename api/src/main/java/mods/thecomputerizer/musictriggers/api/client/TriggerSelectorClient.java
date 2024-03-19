package mods.thecomputerizer.musictriggers.api.client;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerSelectorAPI;

public abstract class TriggerSelectorClient<PLAYER,WORLD> extends TriggerSelectorAPI<PLAYER,WORLD> {

    protected TriggerSelectorClient(ChannelAPI channel, TriggerContextClient<PLAYER,WORLD> context) {
        super(channel,context);
    }

    @Override
    public boolean isClient() {
        return true;
    }
}
