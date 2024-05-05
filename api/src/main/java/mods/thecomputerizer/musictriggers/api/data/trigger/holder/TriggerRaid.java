package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerRaid extends HolderTrigger {

    public TriggerRaid(ChannelAPI channel) {
        super(channel,"raid");
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
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
