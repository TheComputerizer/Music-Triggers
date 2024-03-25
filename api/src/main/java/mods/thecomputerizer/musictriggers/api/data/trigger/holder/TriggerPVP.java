package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;

public class TriggerPVP extends HolderTrigger {

    public TriggerPVP(ChannelAPI channel) {
        super(channel,"pvp");
    }

    @Override
    public boolean isPlayableContext(TriggerContextAPI<?,?> ctx) {
        return ctx.isActivePVP();
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
