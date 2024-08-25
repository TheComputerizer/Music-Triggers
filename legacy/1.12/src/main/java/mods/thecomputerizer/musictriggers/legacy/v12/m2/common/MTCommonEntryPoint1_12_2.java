package mods.thecomputerizer.musictriggers.legacy.v12.m2.common;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.legacy.v12.m2.client.MTClientEntryPoint1_12_2;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.common.CommonEntryPoint;

import javax.annotation.Nullable;
import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.musictriggers.api.MTRef.NAME;

@SuppressWarnings("unused")
public class MTCommonEntryPoint1_12_2 extends CommonEntryPoint {
    
    @Nullable @Override public ClientEntryPoint delegatedClientEntry() {
        return new MTClientEntryPoint1_12_2();
    }
    
    @Override protected String getModID() {
        return MODID;
    }
    
    @Override protected String getModName() {
        return NAME;
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