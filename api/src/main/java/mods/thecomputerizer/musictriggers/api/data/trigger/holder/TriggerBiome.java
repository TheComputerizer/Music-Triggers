package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterList;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterFloat;

import java.util.Collections;
import java.util.Map;

public class TriggerBiome extends HolderTrigger {

    public TriggerBiome(ChannelAPI channel) {
        super(channel,"biome");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"biome_rainfall",new ParameterFloat(Float.MIN_VALUE));
        addParameter(map,"biome_tag",new ParameterList<>(String.class,Collections.singletonList("ANY")));
        addParameter(map,"biome_temperature",new ParameterFloat(Float.MIN_VALUE));
        addParameter(map,"check_higher_rainfall",new ParameterBoolean(true));
        addParameter(map,"check_lower_temp",new ParameterBoolean(false));
        addParameter(map,"rain_type",new ParameterString("ANY"));
        addParameter(map,"resource_name",new ParameterList<>(String.class,Collections.singletonList("ANY")));
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isServer() {
        return true;
    }
}
