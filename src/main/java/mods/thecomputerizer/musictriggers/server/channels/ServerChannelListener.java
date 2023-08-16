package mods.thecomputerizer.musictriggers.server.channels;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Level;

import java.io.InputStream;

public class ServerChannelListener extends SimplePreparableReloadListener<Void> {

    @Override
    public String getName() {
        return Constants.MODID+"$"+this.getClass().getSimpleName();
    }

    @Override
    protected Void prepare(ResourceManager manager, ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(Void obj, ResourceManager manager, ProfilerFiller profiler) {
        MusicTriggers.logExternally(Level.INFO,"Checking for datapack channels");
        try {
            for(Resource resource : manager.listResources("config", res -> res.toString().endsWith("channels.toml")).values()) {
                MusicTriggers.logExternally(Level.INFO,"Found datapack channels file at {}",resource);
                try (InputStream resourceStream = resource.open()) {
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
