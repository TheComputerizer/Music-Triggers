package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;

import java.util.Map;

public abstract class SimpleTrigger extends TriggerAPI {

    protected SimpleTrigger(ChannelAPI channel, String name) {
        super(channel,name);
    }

    @Override
    protected void initExtraParameters(Map<String, Parameter<?>> map) {}

    @Override
    public String getNameWithID() {
        return getName();
    }
}
