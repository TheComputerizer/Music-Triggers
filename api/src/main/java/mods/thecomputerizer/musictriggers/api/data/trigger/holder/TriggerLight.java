package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;

import java.util.Map;

public class TriggerLight extends HolderTrigger {

    public TriggerLight(IChannel channel) {
        super(channel,"light");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"level",new ParameterInt(7));
        addParameter(map,"light_type",new ParameterString("ANY"));
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
