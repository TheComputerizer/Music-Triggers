package mods.thecomputerizer.musictriggers.forge.v18.m2.common;

import mods.thecomputerizer.musictriggers.forge.v18.m2.client.MTDevResourceFinderForge1_18_2;
import mods.thecomputerizer.musictriggers.shared.v18.m2.common.MTMappingsHelper1_18_2;
import mods.thecomputerizer.theimpossiblelibrary.api.core.annotation.IndirectCallers;
import net.minecraft.client.Minecraft;

import java.io.File;

@IndirectCallers
public class MTMappingsHelperForge1_18_2 implements MTMappingsHelper1_18_2 {
    
    @Override public void addDevResources(Minecraft mc, File resourceDir) {
        mc.getResourcePackRepository().addPackFinder(new MTDevResourceFinderForge1_18_2(resourceDir));
    }
}