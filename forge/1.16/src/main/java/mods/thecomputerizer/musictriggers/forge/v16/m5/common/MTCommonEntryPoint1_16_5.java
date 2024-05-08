package mods.thecomputerizer.musictriggers.forge.v16.m5.common;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.forge.v16.m5.client.MTClientEntryPoint1_16_5;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.common.CommonEntryPoint;

import javax.annotation.Nullable;

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
}
