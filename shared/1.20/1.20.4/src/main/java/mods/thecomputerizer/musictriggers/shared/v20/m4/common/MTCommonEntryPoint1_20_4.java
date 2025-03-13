package mods.thecomputerizer.musictriggers.shared.v20.m4.common;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.shared.v20.m4.client.MTClientEntryPoint1_20_4;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.common.DelegatingCommonEntryPoint;

import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.musictriggers.api.MTRef.NAME;

@SuppressWarnings("unused")
public class MTCommonEntryPoint1_20_4 extends DelegatingCommonEntryPoint {
    
    private static MTCommonEntryPoint1_20_4 INSTANCE;
    
    public static MTCommonEntryPoint1_20_4 getInstance() {
        return Objects.nonNull(INSTANCE) ? INSTANCE : new MTCommonEntryPoint1_20_4();
    }
    
    private MTCommonEntryPoint1_20_4() {
        INSTANCE = this;
    }
    
    @Override protected String getModID() {
        return MODID;
    }
    
    @Override protected String getModName() {
        return NAME;
    }
    
    @Override public void onConstructed() {
        MTRef.logInfo("Running version specific onConstructed for 1.20.4");
        super.onConstructed();
    }
    
    @Override public void onLoadComplete() {
        MTRef.logInfo("Running version onLoadComplete for 1.20.4");
        super.onLoadComplete();
    }
    
    @Override public ClientEntryPoint setDelegatedClientHandle() {
        return MTClientEntryPoint1_20_4.getInstance();
    }
}
