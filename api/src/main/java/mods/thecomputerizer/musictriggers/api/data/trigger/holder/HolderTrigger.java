package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;

public abstract class HolderTrigger extends TriggerAPI {

    protected HolderTrigger(ChannelAPI channel, String name) {
        super(channel,name);
    }

    @Override public String getNameWithID() {
        return getName()+"-"+getIdentifier();
    }

    protected boolean hasValidIdentifier() {
        if(hasNonDefaultParameter("identifier")) return true;
        logMissingParameter("identifier");
        return false;
    }
}