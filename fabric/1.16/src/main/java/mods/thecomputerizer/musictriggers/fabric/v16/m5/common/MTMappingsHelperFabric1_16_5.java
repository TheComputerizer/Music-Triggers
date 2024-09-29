package mods.thecomputerizer.musictriggers.fabric.v16.m5.common;

import mods.thecomputerizer.musictriggers.fabric.v16.m5.client.MTDevResourceFinderFabric1_16_5;
import mods.thecomputerizer.musictriggers.shared.v16.m5.common.MTMappingsHelper1_16_5;
import mods.thecomputerizer.theimpossiblelibrary.api.core.annotation.IndirectCallers;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.RepositorySource;

import java.io.File;

@IndirectCallers
public class MTMappingsHelperFabric1_16_5 implements MTMappingsHelper1_16_5 {
    
    @Override public void addDevResources(Minecraft mc, File resourceDir) {
        RepositorySource source = new MTDevResourceFinderFabric1_16_5(resourceDir); //TODO Figure out how to apply this
    }
}