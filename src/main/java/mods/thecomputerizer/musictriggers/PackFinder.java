package mods.thecomputerizer.musictriggers;

import net.minecraft.resources.*;
import net.minecraft.resources.ResourcePackInfo.Priority;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class PackFinder implements IPackFinder {
    private final File pack;

    PackFinder(File p) {
        pack = p;
    }

    @Override
    public void loadPacks(Consumer<ResourcePackInfo> info, ResourcePackInfo.IFactory factory) {
        String packName = pack.getName();
        Supplier<IResourcePack> rPack = pack.isDirectory() ? () -> new FolderPack(pack) : () -> new FilePack(pack);
        ResourcePackInfo packInfo = ResourcePackInfo.create(packName, true, rPack, factory, Priority.TOP, IPackNameDecorator.DEFAULT);
        info.accept(packInfo);
    }
}
