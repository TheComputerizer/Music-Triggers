package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;

import java.util.Map;

public class TriggerHeight extends HolderTrigger {

    public TriggerHeight(IChannel channel) {
        super(channel,"height");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"check_above_level",new ParameterBoolean(false));
        addParameter(map,"check_for_sky",new ParameterBoolean(true));
        addParameter(map,"level",new ParameterInt(7));
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
