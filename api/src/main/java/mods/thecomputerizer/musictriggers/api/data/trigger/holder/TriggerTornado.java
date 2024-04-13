package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TriggerTornado extends HolderTrigger {

    public TriggerTornado(ChannelAPI channel) {
        super(channel,"tornado");
    }

    @Override
    public List<String> getRequiredMods() {
        return Collections.singletonList("weather2");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"detection_range",new ParameterInt(16));
        addParameter(map,"level",new ParameterInt(0));
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveTornado(getParameterAsInt("detection_range"),getParameterAsInt("level"));
    }

    @Override
    public boolean verifyRequiredParameters() {
        String[] parameters = new String[]{"identifier","level"};
        if(hasAllNonDefaultParameter(parameters)) return true;
        logMissingParameters(parameters);
        return false;
    }
}
