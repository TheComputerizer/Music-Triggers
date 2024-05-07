package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;

public abstract class SimpleTrigger extends TriggerAPI {

    protected SimpleTrigger(ChannelAPI channel, String name) {
        super(channel,name);
    }
}
