package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;

import java.util.Map;

public class TriggerDrowning extends SimpleTrigger {

    public TriggerDrowning(ChannelAPI channel) {
        super(channel,"drowning");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        addParameter(map,"level",new ParameterInt(100));
    }

    @Override
    public boolean isActive(TriggerContextAPI ctx) {
        return ctx.isActiveDrowning(getParameterAsInt("level"));
    }
}
