package mods.thecomputerizer.musictriggers.fabric.v16.m5.client;

import mods.thecomputerizer.theimpossiblelibrary.api.core.annotation.IndirectCallers;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.Pack.PackConstructor;
import net.minecraft.server.packs.repository.RepositorySource;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.util.function.Consumer;

import static net.minecraft.server.packs.repository.PackSource.DEFAULT;
import static net.minecraft.server.packs.repository.Pack.Position.TOP;

@IndirectCallers @ParametersAreNonnullByDefault
public class MTDevResourceFinderFabric1_16_5 implements RepositorySource {
    
    final File devResources;
    
    public MTDevResourceFinderFabric1_16_5(File file) {
        this.devResources = file;
    }
    
    @Override public void loadPacks(Consumer<Pack> infoConsumer, PackConstructor infoFactory) {
        infoConsumer.accept(Pack.create("MTResources",true,
                () -> new FolderPackResources(this.devResources),infoFactory,TOP,DEFAULT));
    }
}