package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;

import java.util.Map;

public abstract class SimpleTrigger extends TriggerAPI {

    protected SimpleTrigger(IChannel channel, String name) {
        super(channel,name);
    }

    @Override
    protected void initExtraParameters(Map<String, Parameter<?>> map) {}

    @Override
    public String getNameWithID() {
        return getName();
    }
}
