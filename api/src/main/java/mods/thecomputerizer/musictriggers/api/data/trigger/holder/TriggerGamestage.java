package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

import java.util.Collections;
import java.util.List;

public class TriggerGamestage extends HolderTrigger {

    public TriggerGamestage(ChannelAPI channel) {
        super(channel,"gamestage");
    }

    @Override
    public List<String> getRequiredMods() {
        return Collections.singletonList("gamestages");
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveGamestage(getResourceCtx(),getParameterAsBoolean("is_whitelist"));
    }

    @Override
    public boolean verifyRequiredParameters() {
        if(hasValidIdentifier()) {
            String[] parameters = new String[]{"display_name","resource_name"};
            if(hasAnyNonDefaultParameter(parameters)) return true;
            logMissingPotentialParameter(parameters);
        }
        return false;
    }
}
