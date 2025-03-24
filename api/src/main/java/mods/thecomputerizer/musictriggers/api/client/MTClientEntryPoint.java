package mods.thecomputerizer.musictriggers.api.client;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.network.MTNetwork;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.DelegatingClientEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.client.input.KeyHelper;

import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.musictriggers.api.MTRef.NAME;
import static mods.thecomputerizer.musictriggers.api.client.gui.MTGUIScreen.GUI_KEY;

public class MTClientEntryPoint extends DelegatingClientEntryPoint {
    
    private static MTClientEntryPoint INSTANCE;
    
    public static MTClientEntryPoint getInstance() {
        return Objects.nonNull(INSTANCE) ? INSTANCE : new MTClientEntryPoint();
    }
    
    private MTClientEntryPoint() {
        INSTANCE = this;
    }

    @Override protected String getModID() {
        return MODID;
    }

    @Override protected String getModName() {
        return NAME;
    }
    
    @Override public void onClientSetup() {
        ChannelHelper.onResourcesLoaded();
    }

    @Override public void onConstructed() {
        KeyHelper.register(GUI_KEY);
        ChannelHelper.initClient();
        MTNetwork.initClient();
        super.onConstructed();
    }

    @Override public void onPreRegistration() {
        MTClientEvents.init();
        super.onPreRegistration();
    }
    
    @Override public void onLoadComplete() {
        MTRef.logInfo("Minecraft instance (LoadComplete) is",ClientHelper.getMinecraft());
        super.onLoadComplete();
    }
}