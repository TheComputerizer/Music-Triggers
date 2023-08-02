package mods.thecomputerizer.musictriggers.server.channels;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

import java.io.InputStream;

public class ServerChannelListener extends ReloadListener<Void> {

    @Override
    public String getName() {
        return Constants.MODID+"$"+this.getClass().getSimpleName();
    }

    @Override
    protected Void prepare(IResourceManager manager, IProfiler profiler) {
        return null;
    }

    @Override
    protected void apply(Void obj, IResourceManager manager, IProfiler profiler) {
        MusicTriggers.logExternally(Level.INFO,"Checking for datapack channels");
        try {
            for(ResourceLocation resource : manager.listResources("config", res -> res.endsWith("channels.toml"))) {
                Constants.debugErrorServer("LOOKING AT POTENTIAL CHANNELS DATAPACK {}",resource);
                MusicTriggers.logExternally(Level.INFO,"Found datapack channels file at {}",resource);
                try (InputStream resourceStream = manager.getResource(resource).getInputStream()) {
                    ServerChannelManager.initialize(manager, resourceStream);
                }
                break;
            }
        } catch (Exception ex) {
            MusicTriggers.logExternally(Level.ERROR,"Failed to in read datapack channels! See the main log " +
                    "for more info.");
            Constants.MAIN_LOG.error("Failed to in read datapack channels!",ex);
        }
    }
}
