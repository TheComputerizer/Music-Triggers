package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;

import java.util.Map;

public class TriggerZones extends HolderTrigger {

    public TriggerZones(IChannel channel) {
        super(channel,"zones");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"zone_min_x",new ParameterInt(Integer.MIN_VALUE));
        addParameter(map,"zone_min_y",new ParameterInt(Integer.MIN_VALUE));
        addParameter(map,"zone_min_z",new ParameterInt(Integer.MIN_VALUE));
        addParameter(map,"zone_max_x",new ParameterInt(Integer.MAX_VALUE));
        addParameter(map,"zone_max_y",new ParameterInt(Integer.MAX_VALUE));
        addParameter(map,"zone_max_z",new ParameterInt(Integer.MAX_VALUE));
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
