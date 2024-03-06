package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;

import java.util.Map;

public class TriggerLowHP extends SimpleTrigger {

    public TriggerLowHP(ChannelAPI channel) {
        super(channel,"lowhp");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        addParameter(map,"level",new ParameterInt(30));
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
