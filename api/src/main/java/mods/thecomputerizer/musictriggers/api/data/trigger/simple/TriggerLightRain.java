package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

import java.util.Collections;
import java.util.List;

public class TriggerLightRain extends SimpleTrigger {

    public TriggerLightRain(ChannelAPI channel) {
        super(channel,"lightrain");
    }

    @Override
    public List<String> getRequiredMods() {
        return Collections.singletonList("betterweather");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
