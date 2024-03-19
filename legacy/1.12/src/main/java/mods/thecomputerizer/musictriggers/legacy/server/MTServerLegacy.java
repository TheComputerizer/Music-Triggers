package mods.thecomputerizer.musictriggers.legacy.server;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerSelectorAPI;
import mods.thecomputerizer.musictriggers.api.server.TriggerContextServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

public class MTServerLegacy {

    public static TriggerContextAPI<?,?> getTriggerContext(ChannelAPI channel) {
        return new TriggerContextServerLegacy(channel);
    }

    @SuppressWarnings("unchecked")
    public static <P,W> TriggerSelectorAPI<P,W> getTriggerSelector(ChannelAPI channel, TriggerContextAPI<P,W> context) {
        return (TriggerSelectorAPI<P,W>)new TriggerSelectorServerLegacy(channel,(TriggerContextServer<EntityPlayerMP,WorldServer>)context);
    }
}
