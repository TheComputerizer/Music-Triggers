package mods.thecomputerizer.musictriggers.legacy.v12.m2.common;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.legacy.v12.m2.client.MTClientEntryPoint1_12_2;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.common.DelegatingCommonEntryPoint;

import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.musictriggers.api.MTRef.NAME;

@SuppressWarnings("unused")
public class MTCommonEntryPoint1_12_2 extends DelegatingCommonEntryPoint {
    
    private static MTCommonEntryPoint1_12_2 INSTANCE;
    
    public static MTCommonEntryPoint1_12_2 getInstance() {
        return Objects.nonNull(INSTANCE) ? INSTANCE : new MTCommonEntryPoint1_12_2();
    }
    
    public MTCommonEntryPoint1_12_2() {
        INSTANCE = this;
    }
    
    @Override protected String getModID() {
        return MODID;
    }
    
    @Override protected String getModName() {
        return NAME;
    }
    
    @Override public void onConstructed() {
        MTRef.logInfo("Running version specific onConstructed for 1.12.2");
        super.onConstructed();
    }
    
    @Override public void onLoadComplete() {
        MTRef.logInfo("Running version onLoadComplete for 1.12.2");
        super.onLoadComplete();
    }
    
    @Override public ClientEntryPoint setDelegatedClientHandle() {
        return MTClientEntryPoint1_12_2.getInstance();
    }
}