package mods.thecomputerizer.musictriggers;

import mods.thecomputerizer.musictriggers.client.gui.Mappings;
import mods.thecomputerizer.musictriggers.common.eventsCommon;
import mods.thecomputerizer.musictriggers.config.*;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.musictriggers.util.json;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MusicTriggersCommon implements ModInitializer {
    public static final String MODID = "musictriggers";

    public static final Logger logger = LogManager.getLogger();

    public static File configDir;
    public static File songsDir;
    public static File texturesDir;

    @Override
    public void onInitialize() {
        Mappings.init();
        configDir = new File(FabricLoaderImpl.INSTANCE.getConfigDir().toString(), "MusicTriggers");
        if (!configDir.exists()) {
            configDir.mkdir();
        }
        if(FabricLoaderImpl.INSTANCE.getEnvironmentType()== EnvType.CLIENT) {
            setUpClientPt1();
        }
        configToml.parse();
        if(FabricLoaderImpl.INSTANCE.getEnvironmentType()== EnvType.CLIENT) {
            setUpClientPt2();
        }
        File debugConfig = new File("config/MusicTriggers/debug.toml");
        if(!debugConfig.exists()) {
            try {
                debugConfig.createNewFile();
                configDebug.create(debugConfig);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        configDebug.parse(debugConfig);
        configCommands.parse();
        File registrationConfig = new File("config/MusicTriggers/registration.toml");
        if(!registrationConfig.exists()) {
            try {
                registrationConfig.createNewFile();
                configRegistry.create(registrationConfig);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        configRegistry.parse(registrationConfig);
        RegistryHandler.init();
        setUpCommonEvents();
        if(!configRegistry.clientSideOnly) {
            PacketHandler.register();
        }
    }

    private static void setUpCommonEvents() {
        ServerTickEvents.END_SERVER_TICK.register( server -> eventsCommon.onTick());
    }

    private static void setUpClientPt1() {
        songsDir = new File(configDir.getPath(), "songs");
        if (!songsDir.exists()) {
            songsDir.mkdir();
        }
        File assetsDir = new File(songsDir.getPath(), "assets");
        if (!assetsDir.exists()) {
            assetsDir.mkdir();
        }
        File musictriggersDir = new File(assetsDir.getPath(), "musictriggers");
        if (!musictriggersDir.exists()) {
            musictriggersDir.mkdir();
        }
        File soundsDir = new File(musictriggersDir.getPath(), "sounds");
        if (!soundsDir.exists()) {
            soundsDir.mkdir();
        }
        File musicDir = new File(soundsDir.getPath(), "music");
        if (!musicDir.exists()) {
            musicDir.mkdir();
        }
        texturesDir = new File(musictriggersDir.getPath(), "textures");
        if (!texturesDir.exists()) {
            texturesDir.mkdir();
        }
        File mcmeta = new File(songsDir.getPath() + "/pack.mcmeta");
        if (!mcmeta.exists()) {
            try {
                mcmeta.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            List<String> lines = Arrays.asList("{", "\t\"pack\": {", "\t\t\"pack_format\": 8,", "\t\t\"description\": \"Can you believe this was generated automatically?\"", "\t}", "}");
            Path file = Paths.get(mcmeta.getPath());
            try {
                Files.write(file, lines, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        File jjson = new File(musictriggersDir.getPath() + "/sounds.json");
        if (!jjson.exists() && json.collector() != null) {
            try {
                jjson.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        File langDir = new File(musictriggersDir.getPath(), "lang");
        if (!langDir.exists()) {
            langDir.mkdir();
        }
        File lang = new File(langDir.getPath() + "/en_us.json");
        if (!lang.exists()) {
            try {
                lang.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        MusicTriggersClient.makeSoundsJson();
    }

    private static void setUpClientPt2() {
        MusicTriggersClient.makeDiscLang();
        configTitleCards.parse();
    }
}
