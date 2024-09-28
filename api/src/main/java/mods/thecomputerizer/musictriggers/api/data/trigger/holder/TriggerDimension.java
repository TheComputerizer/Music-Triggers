package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

import java.util.Collections;

public class TriggerDimension extends HolderTrigger {

    public TriggerDimension(ChannelAPI channel) {
        super(channel,"dimension");
    }
    
    @Override public boolean imply(String id) {
        setExistingParameterValue("resource_name", Collections.singletonList(id));
        return super.imply(id);
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveDimension(getResourceCtx());
    }

    @Override public boolean verifyRequiredParameters() {
        if(hasValidIdentifier()) {
            String[] parameters = new String[]{"display_name","resource_name"};
            if(hasAnyNonDefaultParameter(parameters)) return true;
            logMissingPotentialParameter(parameters);
        }
        return false;
    }
}
