package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterFloat;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;

import java.util.Map;

public class TriggerTime extends HolderTrigger {

    public TriggerTime(IChannel channel) {
        super(channel,"time");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"end_hour",new ParameterFloat(0f));
        addParameter(map,"highest_day_number",new ParameterInt(Integer.MAX_VALUE));
        addParameter(map,"lowest_day_number",new ParameterInt(0));
        addParameter(map,"moon_phase",new ParameterInt(0));
        addParameter(map,"start_hour",new ParameterFloat(0f));
        addParameter(map,"time_bundle",new ParameterString("ANY"));
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
