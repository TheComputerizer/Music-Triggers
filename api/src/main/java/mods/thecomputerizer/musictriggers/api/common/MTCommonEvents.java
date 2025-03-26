package mods.thecomputerizer.musictriggers.api.common;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.EventHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.events.CustomTickEventWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.util.CustomTick;

import static mods.thecomputerizer.theimpossiblelibrary.api.common.event.CommonEventWrapper.CommonType.CUSTOM_TICK;

public class MTCommonEvents {
    
    public static void init() {
        MTRef.logInfo("Initializing common event invokers");
        EventHelper.addListener(CUSTOM_TICK,MTCommonEvents::onCustomTick);
        CustomTick.addCustomTickTPS(ChannelHelper.getTickRate());
    }
    
    private static void onCustomTick(CustomTickEventWrapper<?> wrapper) {
        ChannelHelper.tick(wrapper.getTicker());
    }
}