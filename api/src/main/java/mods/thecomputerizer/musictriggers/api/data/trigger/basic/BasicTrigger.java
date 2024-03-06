package mods.thecomputerizer.musictriggers.api.data.trigger.basic;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;

import javax.annotation.Nullable;
import java.util.Map;

public abstract class BasicTrigger extends TriggerAPI {

    protected BasicTrigger(IChannel channel, String name) {
        super(channel, name);
    }

    @Override
    public String getNameWithID() {
        return getName();
    }

    @Override
    protected @Nullable Parameter<?> initParameter(String parameter, Parameter<?> defaultParameter) {
        return parameter.equals("priority") ? null : defaultParameter;
    }

    @Override
    protected void initExtraParameters(Map<String, Parameter<?>> map) {}
}
