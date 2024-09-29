package mods.thecomputerizer.musictriggers.forge.v16.m5.common;

import mods.thecomputerizer.musictriggers.forge.v16.m5.client.MTDevResourceFinderForge1_16_5;
import mods.thecomputerizer.musictriggers.shared.v16.m5.common.MTMappingsHelper1_16_5;
import mods.thecomputerizer.theimpossiblelibrary.api.core.annotation.IndirectCallers;
import net.minecraft.client.Minecraft;

import java.io.File;

@IndirectCallers
public class MTMappingsHelperForge1_16_5 implements MTMappingsHelper1_16_5 {
    
    @Override public void addDevResources(Minecraft mc, File resourceDir) {
        mc.getResourcePackRepository().addPackFinder(new MTDevResourceFinderForge1_16_5(resourceDir));
    }
}