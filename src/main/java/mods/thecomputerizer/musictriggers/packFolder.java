package mods.thecomputerizer.musictriggers;

import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class PackFolder implements RepositorySource {
    private static final FileFilter RESOURCEPACK_FILTER = (file) -> {
        boolean isZip = file.isFile() && file.getName().endsWith(".zip");
        boolean hasMcmeta = file.isDirectory() && (new File(file, "pack.mcmeta")).isFile();
        return isZip || hasMcmeta;
    };
    private final File folder;
    private final PackSource packSource;

    public PackFolder(File file, PackSource source) {
        this.folder = file;
        this.packSource = source;
    }

    public void loadPacks(@NotNull Consumer<Pack> consumer, Pack.@NotNull PackConstructor constructor) {
        if (!this.folder.isDirectory()) this.folder.mkdirs();
        File[] packs = this.folder.listFiles(RESOURCEPACK_FILTER);
        if (packs != null) {
            for(File file : packs) {
                Pack pack = Pack.create("Music Triggers Songs", true, this.createSupplier(file), constructor, Pack.Position.TOP, this.packSource);
                if (pack != null) consumer.accept(pack);
            }

        }
    }

    private Supplier<PackResources> createSupplier(File file) {
        return file.isDirectory() ? () -> new FolderPackResources(file) : () -> new FilePackResources(file);
    }
}
