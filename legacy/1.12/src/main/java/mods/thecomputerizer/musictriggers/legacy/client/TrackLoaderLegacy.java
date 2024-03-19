package mods.thecomputerizer.musictriggers.legacy.client;

import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import mods.thecomputerizer.musictriggers.api.client.audio.TrackLoaderAPI;
import mods.thecomputerizer.musictriggers.api.data.LoggableAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;
import mods.thecomputerizer.theimpossiblelibrary.legacy.resource.ResourceLocationLegacy;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.Objects;

public class TrackLoaderLegacy extends TrackLoaderAPI {

    @Override
    protected @Nullable AudioReference getResourceReference(ResourceLocationAPI<?> res, LoggableAPI logger) {
        ResourceLocation source = ((ResourceLocationLegacy)res).get();
        try {
            String id = null;
            String title = null;
            if(Minecraft.getMinecraft().defaultResourcePack.resourceExists(source)) {
                String sourcePath = "/assets/"+source.getNamespace()+"/"+source.getPath();
                URL url = Minecraft.class.getResource(sourcePath);
                if(Objects.nonNull(url)) {
                    URI uri = url.toURI();
                    Path path = null;
                    if("file".equals(uri.getScheme())) {
                        URL resource = Minecraft.class.getResource(sourcePath);
                        if(Objects.nonNull(resource)) path = Paths.get(resource.toURI());
                    } else {
                        FileSystem filesystem;
                        try {
                            filesystem = FileSystems.getFileSystem(uri);
                        } catch(FileSystemNotFoundException | ProviderNotFoundException ignored) {
                            filesystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                        }
                        path = filesystem.getPath(sourcePath);
                    }
                    if(Objects.nonNull(path)) id = path.toUri().toString();
                } else {
                    File file = Minecraft.getMinecraft().defaultResourcePack.resourceIndex.getFile(source);
                    if(Objects.nonNull(file) && file.isFile()) {
                        id = file.getAbsolutePath();
                        title = file.getName();
                    }
                }
            }
            return Objects.nonNull(id) ? new AudioReference(id,title) : null;
        } catch(Exception ex) {
            logger.logError("Unable to get audio reference for resource `{}`!",source,ex);
            return null;
        }
    }
}