package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerAdvancement extends HolderTrigger {

    public TriggerAdvancement(ChannelAPI channel) {
        super(channel,"advancement");
    }
    
    @Override
    public boolean imply(String id) {
        logError("Trigger must be explicitly defined");
        return false;
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveAdvancement(getResourceCtx());
    }

    @Override
    public boolean verifyRequiredParameters() {
        String[] parameters = new String[]{"identifier","persistence"};
        if(hasAllNonDefaultParameter(parameters)) {
            parameters = new String[]{"display_name","resource_name"};
            if(hasAnyNonDefaultParameter(parameters)) return true;
            logMissingPotentialParameter(parameters);
        } else logMissingParameters(parameters);
        return false;
    }
}
