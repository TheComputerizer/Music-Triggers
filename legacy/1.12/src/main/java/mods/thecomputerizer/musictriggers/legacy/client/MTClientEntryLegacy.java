package mods.thecomputerizer.musictriggers.legacy.client;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class MTClientEntryLegacy extends ClientEntryPoint {

    public MTClientEntryLegacy() {
        ChannelHelper.initClient();
    }

    public void preInit(FMLPreInitializationEvent event) {}

    public void init(FMLInitializationEvent event) {
        ChannelHelper.onResourcesLoaded();
    }

    public void postInit(FMLPreInitializationEvent event) {}
}
