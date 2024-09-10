package mods.thecomputerizer.musictriggers.api.client;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.network.MTNetwork;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.input.KeyHelper;

import javax.annotation.Nullable;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.musictriggers.api.MTRef.NAME;
import static mods.thecomputerizer.musictriggers.api.client.gui.MTGUIScreen.GUI_KEY;

public class MTClientEntryPoint extends ClientEntryPoint {
    
    @Override public void onClientSetup() {
        KeyHelper.register(GUI_KEY);
        ChannelHelper.onResourcesLoaded();
    }

    @Override public @Nullable ClientEntryPoint delegatedClientEntry() {
        return this;
    }

    @Override protected String getModID() {
        return MODID;
    }

    @Override protected String getModName() {
        return NAME;
    }

    @Override public void onConstructed() {
        ChannelHelper.initClient();
        MTNetwork.initClient();
    }

    @Override public void onPreRegistration() {
        MTClientEvents.init();
    }
    
    @Override public void onLoadComplete() {
        MTRef.logInfo("Minecraft instance (LoadComplete) is",ClientHelper.getMinecraft());
    }
}