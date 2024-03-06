package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterFloat;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;

import java.util.Map;

public class TriggerPet extends SimpleTrigger {

    public TriggerPet(IChannel channel) {
        super(channel,"pet");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        addParameter(map,"detection_range",new ParameterInt(16));
        addParameter(map,"detection_y_ratio",new ParameterFloat(0.5f));
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
