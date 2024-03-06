package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TriggerSandStorm extends HolderTrigger {

    public TriggerSandStorm(ChannelAPI channel) {
        super(channel,"sandstorm");
    }

    @Override
    public List<String> getRequiredMods() {
        return Collections.singletonList("weather2");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"detection_range",new ParameterInt(16));
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
