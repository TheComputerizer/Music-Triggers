package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

import java.util.Collections;
import java.util.List;

public class TriggerInventory extends HolderTrigger {

    public TriggerInventory(ChannelAPI channel) {
        super(channel,"inventory");
    }
    
    @Override public boolean imply(String id) {
        setExistingParameterValue("items",Collections.singletonList(id));
        return super.imply(id);
    }

    @SuppressWarnings("unchecked")
    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveInventory((List<String>)getParameterAsList("items"),
                (List<String>)getParameterAsList("slots"));
    }

    @Override public boolean verifyRequiredParameters() {
        String[] parameters = new String[]{"identifier","items"};
        if(hasAllNonDefaultParameter(parameters)) return true;
        logMissingParameters(parameters);
        return false;
    }
}
