package mods.thecomputerizer.musictriggers.api.client;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;

import javax.annotation.Nullable;

public class MTClientEntryPoint extends ClientEntryPoint {

    public static MTDebugInfo debugInfo;
    
    @Override
    public void onClientSetup() {
        ChannelHelper.onResourcesLoaded();
    }

    @Override
    public @Nullable ClientEntryPoint delegatedClientEntry() {
        return this;
    }

    @Override
    protected String getModID() {
        return MTRef.MODID;
    }

    @Override
    protected String getModName() {
        return MTRef.NAME;
    }

    @Override
    public void onConstructed() {
        debugInfo = new MTDebugInfo(null);
        ChannelHelper.initClient(debugInfo);
    }

    @Override
    public void onPreRegistration() {
        MTClientEvents.init(debugInfo);
    }
}
