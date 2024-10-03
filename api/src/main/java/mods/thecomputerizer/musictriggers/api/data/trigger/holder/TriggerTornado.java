package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

import java.util.Collections;
import java.util.List;

public class TriggerTornado extends HolderTrigger {

    public TriggerTornado(ChannelAPI channel) {
        super(channel,"tornado");
    }
    
    @Override public boolean imply(String id) {
        setExistingParameterValue("level",1);
        return super.imply(id);
    }

    @Override public List<String> getRequiredMods() {
        return Collections.singletonList("weather2");
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveTornado(getParameterAsInt("detection_range"),getParameterAsInt("level"));
    }

    @Override public boolean verifyRequiredParameters() {
        return hasValidIdentifier();
    }
}
