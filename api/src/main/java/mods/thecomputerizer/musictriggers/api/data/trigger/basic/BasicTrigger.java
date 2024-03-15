package mods.thecomputerizer.musictriggers.api.data.trigger.basic;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;

import javax.annotation.Nullable;
import java.util.Map;

public abstract class BasicTrigger extends TriggerAPI {

    protected BasicTrigger(ChannelAPI channel, String name) {
        super(channel, name);
    }

    @Override
    protected @Nullable Parameter<?> initParameter(String parameter, Parameter<?> defaultParameter) {
        return parameter.equals("priority") ? null : defaultParameter;
    }

    @Override
    protected void initExtraParameters(Map<String, Parameter<?>> map) {}

    @Override
    public boolean matches(TriggerAPI trigger) {
        return getName().matches(trigger.getName());
    }

    @Override
    public boolean verifyRequiredParameters() {
        return true;
    }
}
