package mods.thecomputerizer.musictriggers.shared.v19.m2.common;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.shared.v19.m2.client.MTClientEntryPoint1_19_2;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.common.DelegatingCommonEntryPoint;

import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.musictriggers.api.MTRef.NAME;

@SuppressWarnings("unused")
public class MTCommonEntryPoint1_19_2 extends DelegatingCommonEntryPoint {
    
    private static MTCommonEntryPoint1_19_2 INSTANCE;
    
    public static MTCommonEntryPoint1_19_2 getInstance() {
        return Objects.nonNull(INSTANCE) ? INSTANCE : new MTCommonEntryPoint1_19_2();
    }
    
    private MTCommonEntryPoint1_19_2() {
        INSTANCE = this;
    }
    
    @Override protected String getModID() {
        return MODID;
    }
    
    @Override protected String getModName() {
        return NAME;
    }
    
    @Override public void onConstructed() {
        MTRef.logInfo("Running version specific onConstructed for 1.19.2");
        super.onConstructed();
    }
    
    @Override public void onLoadComplete() {
        MTRef.logInfo("Running version onLoadComplete for 1.19.2");
        super.onLoadComplete();
    }
    
    @Override public ClientEntryPoint setDelegatedClientHandle() {
        return MTClientEntryPoint1_19_2.getInstance();
    }
}
