package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.HolderTrigger;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TriggerHurricane extends SimpleTrigger {

    public TriggerHurricane(ChannelAPI channel) {
        super(channel,"hurricane");
    }

    @Override
    public List<String> getRequiredMods() {
        return Collections.singletonList("weather2");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        addParameter(map,"detection_range",new ParameterInt(16));
    }

    @Override
    public boolean isActive(TriggerContextAPI ctx) {
        return ctx.isActiveHurricane(getParameterAsInt("detection_range"));
    }
}
