package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;

import java.util.Map;

public class TriggerRaid extends HolderTrigger {

    public TriggerRaid(ChannelAPI channel) {
        super(channel,"raid");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"wave",new ParameterInt(0));
    }

    @Override
    public boolean isActive(TriggerContextAPI<?,?> ctx) {
        return ctx.isActiveRaid(getParameterAsInt("wave"));
    }

    @Override
    public boolean isServer() {
        return true;
    }

    @Override
    public boolean verifyRequiredParameters() {
        return hasValidIdentifier();
    }
}
