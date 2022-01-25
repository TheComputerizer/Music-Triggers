package mods.thecomputerizer.musictriggers;

import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.common.eventsCommon;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.musictriggers.util.json;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
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

@SuppressWarnings({"ResultOfMethodCallIgnored", "deprecation"})
@Mod(modid = MusicTriggers.MODID, name = MusicTriggers.NAME, version = MusicTriggers.VERSION)
public class MusicTriggers {
    public static final String MODID = "musictriggers";
    public static final String NAME = "Music Triggers";
    public static final String VERSION = "4.1";

    public static File songsDir;
    public static File texturesDir;
    public static File songs;
    public static File readFrom;
    public static File pack;

    public static Logger logger;

    public MusicTriggers() {
        logger = LogManager.getLogger(MODID);
        readFrom = new File("config/MusicTriggers/songs/");
        if (readFrom.exists() && Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
            File redir = new File("config/MusicTriggers/redirect.txt");
            if(!redir.exists()) {
                try {
                    Files.createFile(Paths.get(redir.getPath()));
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            new readRedirect(redir);
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
            sj = new File("config/MusicTriggers/songs/assets/musictriggers/lang/en_us.lang");
            if (sj.exists()) {
                sj.delete();
            }
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
            pack = new File("config/MusicTriggers/songs/");
            if (pack.exists()) {
                try {
                    List<IResourcePack> defaultResourcePacks = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "field_110449_ao", "ap");
                    defaultResourcePacks.add(new FolderResourcePack(pack));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        File configDir = new File(event.getSuggestedConfigurationFile().getParentFile().getPath(), "MusicTriggers");
        if (!configDir.exists()) {
            configDir.mkdir();
        }
        if(Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
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
                List<String> lines = Arrays.asList("{", "\t\"pack\": {", "\t\t\"pack_format\": 3,", "\t\t\"description\": \"Can you believe this was generated automatically?\"", "\t}", "}");
                Path file = Paths.get(mcmeta.getPath());
                try {
                    Files.write(file, lines, StandardCharsets.UTF_8);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            File json = new File(musictriggersDir.getPath() + "/sounds.json");
            if (!json.exists()) {
                try {
                    json.createNewFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            File langDir = new File(musictriggersDir.getPath(), "lang");
            if (!langDir.exists()) {
                langDir.mkdir();
            }
            File lang = new File(langDir.getPath() + "/en_us.lang");
            if (!lang.exists()) {
                try {
                    lang.createNewFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            songs = musictriggersDir;
        }
        if(!configRegistry.registry.clientSideOnly) {
            RegistryHandler.init();
        }
        MinecraftForge.EVENT_BUS.register(MusicPlayer.class);
        if(event.getSide()== Side.CLIENT) {
            MinecraftForge.EVENT_BUS.register(eventsClient.class);
        }
        MinecraftForge.EVENT_BUS.register(eventsCommon.class);
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        if(e.getSide()== Side.CLIENT) {
            ClientRegistry.registerKeyBinding(MusicPlayer.RELOAD);
        }
    }
}
