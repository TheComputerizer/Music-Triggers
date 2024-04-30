package mods.thecomputerizer.musictriggers.api.common;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.MTClientEntryPoint;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.network.MTNetwork;
import mods.thecomputerizer.musictriggers.api.server.MTServerEvents;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.common.CommonEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.core.loader.MultiVersionMod;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;

@MultiVersionMod(modid = MTRef.MODID, modName = MTRef.NAME, modVersion = MTRef.VERSION)
public class MTCommonEntryPoint extends CommonEntryPoint {

    @Override
    public @Nullable ClientEntryPoint delegatedClientEntry() {
        return new MTClientEntryPoint();
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
        File configDir = new File(MTRef.CONFIG_PATH);
        if(!configDir.exists() && !configDir.mkdirs())
            throw new RuntimeException("Unable to create file directory at "+MTRef.CONFIG_PATH+"! Music Triggers "+
                    "is unable to load any further.");
        MTNetwork.init();
        if(Objects.nonNull(this.delegatedClient)) this.delegatedClient.onConstructed();
    }

    @Override
    public void onPreRegistration() {
        MTServerEvents.init();
        if(Objects.nonNull(this.delegatedClient)) this.delegatedClient.onPreRegistration();
    }

    @Override
    protected void onDedicatedServerSetup() {
        ChannelHelper.initServer();
    }
}
