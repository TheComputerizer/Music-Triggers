package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;

import java.util.Map;

public class TriggerLight extends HolderTrigger {

    public TriggerLight(ChannelAPI channel) {
        super(channel,"light");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"level",new ParameterInt(7));
        addParameter(map,"light_type",new ParameterString("ANY"));
    }

    @Override
    public boolean isActive(TriggerContextAPI ctx) {
        return ctx.isActiveLight(getParameterAsInt("level"),getParameterAsString("light_type"));
    }

    @Override
    protected boolean verifyRequiredParameters() {
        String[] parameters = new String[]{"identifier","level"};
        if(hasAllNonDefaultParameter(parameters)) return true;
        logMissingParameters(parameters);
        return false;
    }
}
