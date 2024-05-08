package mods.thecomputerizer.musictriggers.forge.v16.m5.common;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.forge.v16.m5.client.MTClientEntryPoint1_16_5;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.common.CommonEntryPoint;

import javax.annotation.Nullable;
import java.util.Objects;

@SuppressWarnings("unused")
public class MTCommonEntryPoint1_16_5 extends CommonEntryPoint {
    
    @Nullable @Override public ClientEntryPoint delegatedClientEntry() {
        return new MTClientEntryPoint1_16_5();
    }
    
    @Override protected String getModID() {
        return MTRef.MODID;
    }
    
    @Override protected String getModName() {
        return MTRef.NAME;
    }
    
    @Override public void onConstructed() {
        this.onLoadComplete();
        MTRef.logInfo("Running version specific onConstructed for 1.12.2");
        if(Objects.nonNull(this.delegatedClient)) this.delegatedClient.onConstructed();
    }
    
    @Override public void onLoadComplete() {
        MTRef.logInfo("Running version onLoadComplete for 1.12.2");
        if(Objects.nonNull(this.delegatedClient)) this.delegatedClient.onLoadComplete();
    }
}
