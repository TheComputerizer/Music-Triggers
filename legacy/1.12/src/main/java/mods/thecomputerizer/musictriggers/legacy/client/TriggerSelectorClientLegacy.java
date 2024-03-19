package mods.thecomputerizer.musictriggers.legacy.client;

import mods.thecomputerizer.musictriggers.api.client.TriggerContextClient;
import mods.thecomputerizer.musictriggers.api.client.TriggerSelectorClient;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;

public class TriggerSelectorClientLegacy extends TriggerSelectorClient<EntityPlayerSP,WorldClient> {

    public TriggerSelectorClientLegacy(ChannelAPI channel, TriggerContextClient<EntityPlayerSP,WorldClient> context) {
        super(channel,context);
    }

    @Override
    public void select() {
        select(Minecraft.getMinecraft().player,Minecraft.getMinecraft().world);
    }
}
