package mods.thecomputerizer.musictriggers;

import mods.thecomputerizer.musictriggers.common.eventsCommon;
import mods.thecomputerizer.musictriggers.config.configRegistry;
import mods.thecomputerizer.musictriggers.config.configTitleCards;
import mods.thecomputerizer.musictriggers.config.configToml;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.musictriggers.util.json;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.resource.*;
import net.minecraft.text.Text;
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
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MusicTriggersCommon implements ModInitializer {
    public static final String MODID = "musictriggers";

    public static final Logger logger = LogManager.getLogger();

    public static File configDir;
    public static File songsDir;
    public static File texturesDir;

    @SuppressWarnings("InstantiationOfUtilityClass")
    @Override
    public void onInitialize() {
        configDir = new File(FabricLoaderImpl.INSTANCE.getConfigDir().toString(), "MusicTriggers");
        if (!configDir.exists()) {
            configDir.mkdir();
        }
        if(FabricLoaderImpl.INSTANCE.getEnvironmentType()== EnvType.CLIENT) {
            setUpClientPt1();
        }
        configToml.parse();
        File redir = new File(FabricLoaderImpl.INSTANCE.getConfigDir().toString(),"MusicTriggers/redirect.txt");
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
        if(FabricLoaderImpl.INSTANCE.getEnvironmentType()== EnvType.CLIENT) {
            setUpClientPt2();
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
