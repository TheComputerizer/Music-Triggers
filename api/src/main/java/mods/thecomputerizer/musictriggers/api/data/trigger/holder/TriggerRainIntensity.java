package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterFloat;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TriggerRainIntensity extends HolderTrigger {

    public TriggerRainIntensity(ChannelAPI channel) {
        super(channel,"rainintensity");
    }

    @Override
    public List<String> getRequiredMods() {
        return Collections.singletonList("dsurround");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"level",new ParameterFloat(50f));
    }

    @Override
    public boolean isActive(TriggerContextAPI<?,?> ctx) {
        return ctx.isActiveRainIntensity(getParameterAsFloat("level"));
    }

    @Override
    public boolean verifyRequiredParameters() {
        return hasValidIdentifier();
    }
}
