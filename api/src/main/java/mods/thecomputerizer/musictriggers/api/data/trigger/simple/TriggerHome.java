package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterFloat;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;

import java.util.Map;

public class TriggerHome extends SimpleTrigger {

    public TriggerHome(ChannelAPI channel) {
        super(channel,"home");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        addParameter(map,"detection_range",new ParameterInt(16));
        addParameter(map,"detection_y_ratio",new ParameterFloat(0.5f));
    }

    @Override
    public boolean isPlayableContext(TriggerContextAPI<?,?> ctx) {
        return ctx.isActiveHome(getParameterAsInt("detection_range"),getParameterAsFloat("detection_y_ratio"));
    }

    @Override
    public boolean isServer() {
        return true;
    }
}
