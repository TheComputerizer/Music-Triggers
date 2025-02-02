package mods.thecomputerizer.musictriggers.forge.v18.m2.client;

import mods.thecomputerizer.theimpossiblelibrary.api.core.annotation.IndirectCallers;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.Pack.PackConstructor;
import net.minecraft.server.packs.repository.RepositorySource;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.util.function.Consumer;

import static net.minecraft.server.packs.repository.Pack.Position.TOP;
import static net.minecraft.server.packs.repository.PackSource.DEFAULT;

@IndirectCallers @ParametersAreNonnullByDefault
public class MTDevResourceFinderForge1_18_2 implements RepositorySource {
    
    final File devResources;
    
    public MTDevResourceFinderForge1_18_2(File file) {
        this.devResources = file;
    }
    
    @Override public void loadPacks(Consumer<Pack> infoConsumer, PackConstructor infoFactory) {
        infoConsumer.accept(Pack.create("MTResources",true,
                () -> new FolderPackResources(this.devResources),infoFactory,TOP,DEFAULT));
    }
}
