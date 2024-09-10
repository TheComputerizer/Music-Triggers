package mods.thecomputerizer.musictriggers.forge.v16.m5.client;

import mods.thecomputerizer.theimpossiblelibrary.api.core.annotation.IndirectCallers;
import net.minecraft.resources.FolderPack;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackInfo.IFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.util.function.Consumer;

import static net.minecraft.resources.IPackNameDecorator.DEFAULT;
import static net.minecraft.resources.ResourcePackInfo.Priority.TOP;

@IndirectCallers @ParametersAreNonnullByDefault
public class MTDevResourceFinderForge1_16_5 implements IPackFinder {
    
    final File devResources;
    
    public MTDevResourceFinderForge1_16_5(File file) {
        this.devResources = file;
    }
    
    @Override public void loadPacks(Consumer<ResourcePackInfo> infoConsumer, IFactory infoFactory) {
        infoConsumer.accept(ResourcePackInfo.create("MTResources",true,
                () -> new FolderPack(this.devResources),infoFactory,TOP,DEFAULT));
    }
}
