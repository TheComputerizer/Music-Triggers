package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerLight extends HolderTrigger {

    public TriggerLight(ChannelAPI channel) {
        super(channel,"light");
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveLight(getParameterAsInt("level"),getParameterAsString("light_type").toLowerCase());
    }

    @Override
    public boolean verifyRequiredParameters() {
        String[] parameters = new String[]{"identifier","level"};
        if(hasAllNonDefaultParameter(parameters)) return true;
        logMissingParameters(parameters);
        return false;
    }
}
