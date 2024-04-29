package mods.thecomputerizer.musictriggers.api.client;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;

import javax.annotation.Nullable;

public class MTClientEntryPoint extends ClientEntryPoint {

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
        ChannelHelper.initClient();
    }

    @Override
    public void onPreRegistration() {
        MTClientEvents.init(null);
    }
}
