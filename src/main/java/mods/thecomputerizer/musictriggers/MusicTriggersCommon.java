package mods.thecomputerizer.musictriggers;

import mods.thecomputerizer.musictriggers.common.eventsCommon;
import mods.thecomputerizer.musictriggers.config.configRegistry;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MusicTriggersCommon implements ModInitializer {
    public static final String MODID = "musictriggers";

    public static final Logger logger = LogManager.getLogger();

    @SuppressWarnings("InstantiationOfUtilityClass")
    @Override
    public void onInitialize() {
        File configDir = new File("config", "MusicTriggers");
        if (!configDir.exists()) {
            configDir.mkdir();
        }
        File redir = new File("config/MusicTriggers/redirect.txt");
        if(!redir.exists()) {
            try {
                Files.createFile(Paths.get(redir.getPath()));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        new readRedirect(redir);
        try {
            File Registrationconfig = new File(configDir,"registration.txt");
            if(!Registrationconfig.exists()) {
                configRegistry.build(Registrationconfig);
                configRegistry.read(Registrationconfig);
            }
            configRegistry.update(Registrationconfig);
        } catch(Exception e) {
            e.printStackTrace();
        }
        RegistryHandler.init();
        setUpCommonEvents();
        if(!configRegistry.clientSideOnly) {
            PacketHandler.register();
        }
    }

    private static void setUpCommonEvents() {
        ServerTickEvents.END_SERVER_TICK.register( server -> eventsCommon.onTick());
    }
}
