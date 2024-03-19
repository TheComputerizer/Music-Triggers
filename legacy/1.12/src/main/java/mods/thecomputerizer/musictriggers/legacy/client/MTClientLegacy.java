package mods.thecomputerizer.musictriggers.legacy.client;

import mods.thecomputerizer.musictriggers.api.client.TriggerContextClient;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerSelectorAPI;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;

public class MTClientLegacy {

    public static TriggerContextAPI<?,?> getTriggerContext(ChannelAPI channel) {
        return new TriggerContextClientLegacy(channel);
    }

    @SuppressWarnings("unchecked")
    public static <P,W> TriggerSelectorAPI<P,W> getTriggerSelector(ChannelAPI channel, TriggerContextAPI<P,W> context) {
        return (TriggerSelectorAPI<P,W>)new TriggerSelectorClientLegacy(channel,(TriggerContextClient<EntityPlayerSP,WorldClient>)context);
    }
}