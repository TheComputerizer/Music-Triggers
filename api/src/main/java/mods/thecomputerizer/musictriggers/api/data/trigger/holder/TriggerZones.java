package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;

import java.util.Map;

public class TriggerZones extends HolderTrigger {

    public TriggerZones(ChannelAPI channel) {
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
    public boolean isPlayableContext(TriggerContextAPI<?,?> ctx) {
        return ctx.isActiveZones(getParameterAsInt("zone_min_x"),getParameterAsInt("zone_min_y"),
                getParameterAsInt("zone_min_z"),getParameterAsInt("zone_max_x"),
                getParameterAsInt("zone_max_y"),getParameterAsInt("zone_max_z"));
    }

    @Override
    public boolean verifyRequiredParameters() {
        if(hasValidIdentifier()) {
            String[] parameters = new String[]{"zone_min_x","zone_max_x","zone_min_y","zone_max_y","zone_min_z","zone_max_z"};
            if(hasAnyNonDefaultParameter(parameters)) return true;
            logMissingPotentialParameter(parameters);
        }
        return false;
    }
}
