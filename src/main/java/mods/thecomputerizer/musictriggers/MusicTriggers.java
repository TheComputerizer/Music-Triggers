package mods.thecomputerizer.musictriggers;

import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.common.eventsCommon;
import mods.thecomputerizer.musictriggers.config.configDebug;
import mods.thecomputerizer.musictriggers.config.configRegistry;
import mods.thecomputerizer.musictriggers.config.configTitleCards;
import mods.thecomputerizer.musictriggers.config.configToml;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.musictriggers.util.json;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ResultOfMethodCallIgnored")
@Mod(MusicTriggers.MODID)
public class MusicTriggers {
    public static final String MODID = "musictriggers";

    public static File songsDir;
    public static File texturesDir;

    private static RepositorySource source;

    public static final Logger logger = LogManager.getLogger();

    @SuppressWarnings("InstantiationOfUtilityClass")
    public MusicTriggers() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonsetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addPack);
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, configDebug.SPEC, "MusicTriggers/debug.toml");
        MinecraftForge.EVENT_BUS.register(this);
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
        if (FMLEnvironment.dist == Dist.CLIENT) {
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
            if (!jjson.exists() && json.collector()!=null) {
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
            makeSoundsJson();
            makeDiscLang();
            configToml.parse();
            configTitleCards.parse();
            if(json.collector()!=null) {
                File pack = new File("config/MusicTriggers/");
                if (pack.isDirectory()/* && new File(pack, "pack.mcmeta").isFile()*/) {
                    MusicTriggers.logger.info("found pack :)");
                    source = new packFolder(pack, PackSource.DEFAULT);
                }
            }
        }
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
        RegistryHandler.init(eventBus);
        MinecraftForge.EVENT_BUS.register(MusicPlayer.class);
        MinecraftForge.EVENT_BUS.register(eventsClient.class);
        MinecraftForge.EVENT_BUS.register(eventsCommon.class);
    }

    private void clientSetup(final FMLClientSetupEvent ev) {
        ClientRegistry.registerKeyBinding(MusicPlayer.RELOAD);
    }

    public void commonsetup(FMLCommonSetupEvent ev) {
        if(configRegistry.clientSideOnly) {
            //ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DISPLAYTEST,() -> Pair.of(()-> FMLNetworkConstants.IGNORESERVERONLY,(a, b)->true));
        }
        else {
            PacketHandler.register();
        }
    }

    public void addPack(AddPackFindersEvent ev) {
        MusicTriggers.logger.info("is this even being called");
        if(source!=null) {
            MusicTriggers.logger.info("source added?");
            ev.addRepositorySource(source);
        }
    }

    public static void makeSoundsJson() {
        File sj = new File("config/MusicTriggers/songs/assets/musictriggers/sounds.json");
        if (sj.exists()) {
            sj.delete();
        }
        List<String> writeThis = json.create();
        if (writeThis != null) {
            try {
                sj.createNewFile();
                FileWriter writer = new FileWriter(sj);
                for (String str : writeThis) {
                    writer.write(str + System.lineSeparator());
                }
                writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void makeDiscLang() {
        if(configRegistry.registerDiscs) {
            File sj = new File("config/MusicTriggers/songs/assets/musictriggers/lang/en_us.json");
            if (sj.exists()) {
                sj.delete();
            }
            List<String> writeThis = json.create();
            assert writeThis != null;
            writeThis.clear();
            writeThis = json.lang();
            if (writeThis != null) {
                try {
                    sj.createNewFile();
                    FileWriter writer = new FileWriter(sj);
                    for (String str : writeThis) {
                        writer.write(str + System.lineSeparator());
                    }
                    writer.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
