package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;

import java.util.Map;

public class TriggerDrowning extends SimpleTrigger {

    public TriggerDrowning(IChannel channel) {
        super(channel,"drowning");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        addParameter(map,"level",new ParameterInt(100));
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
