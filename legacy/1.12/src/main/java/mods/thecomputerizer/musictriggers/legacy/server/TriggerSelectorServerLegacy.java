package mods.thecomputerizer.musictriggers.legacy.server;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.server.TriggerContextServer;
import mods.thecomputerizer.musictriggers.api.server.TriggerSelectorServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

public class TriggerSelectorServerLegacy extends TriggerSelectorServer<EntityPlayerMP,WorldServer> {

    public TriggerSelectorServerLegacy(ChannelAPI channel, TriggerContextServer<EntityPlayerMP,WorldServer> context) {
        super(channel,context);
    }

    @Override
    public void select() {
        select(null,null); //TODO get server player & server world for this
    }
}
