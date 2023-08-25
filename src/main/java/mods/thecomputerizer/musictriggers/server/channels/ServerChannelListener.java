package mods.thecomputerizer.musictriggers.server.channels;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.Level;

import java.io.InputStream;

public class ServerChannelListener implements SimpleSynchronousResourceReloadListener {

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
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

    @Override
    public String getName() {
        return Constants.MODID+"$"+this.getClass().getSimpleName();
    }

    @Override
    public ResourceLocation getFabricId() {
        return Constants.res("server_channel_listenter");
    }
}
