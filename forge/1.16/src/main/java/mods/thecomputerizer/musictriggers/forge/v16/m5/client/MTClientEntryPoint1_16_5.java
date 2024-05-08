package mods.thecomputerizer.musictriggers.forge.v16.m5.client;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;

import javax.annotation.Nullable;

public class MTClientEntryPoint1_16_5 extends ClientEntryPoint {
    
    @Nullable @Override public ClientEntryPoint delegatedClientEntry() {
        return this;
    }
    
    @Override protected String getModID() {
        return MTRef.MODID;
    }
    
    @Override protected String getModName() {
        return MTRef.NAME;
    }
    
    @Override public void onClientSetup() {
    
    }
}
