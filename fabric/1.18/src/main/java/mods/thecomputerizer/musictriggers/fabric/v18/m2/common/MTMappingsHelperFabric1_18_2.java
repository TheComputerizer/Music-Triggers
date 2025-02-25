package mods.thecomputerizer.musictriggers.fabric.v18.m2.common;

import mods.thecomputerizer.musictriggers.fabric.v18.m2.client.MTDevResourceFinderFabric1_18_2;
import mods.thecomputerizer.musictriggers.shared.v18.m2.common.MTMappingsHelper1_18_2;
import mods.thecomputerizer.theimpossiblelibrary.api.core.annotation.IndirectCallers;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.RepositorySource;

import java.io.File;

@IndirectCallers
public class MTMappingsHelperFabric1_18_2 implements MTMappingsHelper1_18_2 {
    
    @Override public void addDevResources(Minecraft mc, File resourceDir) {
        RepositorySource source = new MTDevResourceFinderFabric1_18_2(resourceDir); //TODO Figure out how to apply this
    }
}