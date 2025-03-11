package mods.thecomputerizer.musictriggers.shared.v20.m1.client;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.DelegatingClientEntryPoint;

import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.musictriggers.api.MTRef.NAME;

public class MTClientEntryPoint1_20_1 extends DelegatingClientEntryPoint {
    
    private static MTClientEntryPoint1_20_1 INSTANCE;
    
    public static MTClientEntryPoint1_20_1 getInstance() {
        return Objects.nonNull(INSTANCE) ? INSTANCE : new MTClientEntryPoint1_20_1();
    }
    
    private MTClientEntryPoint1_20_1() {
        INSTANCE = this;
    }
    
    @Override protected String getModID() {
        return MODID;
    }
    
    @Override protected String getModName() {
        return NAME;
    }
    
    @Override public void onClientSetup() {
    }
    
    @Override public void onLoadComplete() {
        ChannelHelper.getClientHelper().queryCategoryVolume();
        super.onLoadComplete();
    }
}