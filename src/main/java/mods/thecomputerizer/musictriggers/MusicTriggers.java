package mods.thecomputerizer.musictriggers;

import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.common.eventsCommon;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.musictriggers.util.json;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ResultOfMethodCallIgnored")
@Mod(modid = MusicTriggers.MODID, name = MusicTriggers.NAME, version = MusicTriggers.VERSION)
public class MusicTriggers {
    public static final String MODID = "musictriggers";
    public static final String NAME = "Music Triggers";
    public static final String VERSION = "2.1";

    public static File songsDir;
    public static File texturesDir;
    public static File songs;
    public static File readFrom;
    public static File pack;
    public static List<ResourcePackRepository.Entry> oldpacks = new ArrayList<>();
    public static List<ResourcePackRepository.Entry> newpacks = new ArrayList<>();

    public static Logger logger;

    @SuppressWarnings("JavaReflectionMemberAccess")
    public MusicTriggers() {
        readFrom = new File("config/MusicTriggers/songs/");
        if (readFrom.exists() && Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
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
                    oldpacks = Minecraft.getMinecraft().getResourcePackRepository().getRepositoryEntriesAll();
                    newpacks.addAll(oldpacks);
                    Constructor<ResourcePackRepository.Entry> cn = ResourcePackRepository.Entry.class.getDeclaredConstructor(ResourcePackRepository.class, IResourcePack.class);
                    cn.setAccessible(true);
                    newpacks.add(cn.newInstance(Minecraft.getMinecraft().getResourcePackRepository(), new FolderResourcePack(pack)));
                    Minecraft.getMinecraft().getResourcePackRepository().setRepositories(newpacks);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
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
        RegistryHandler.init();
        MinecraftForge.EVENT_BUS.register(MusicPlayer.class);
        MinecraftForge.EVENT_BUS.register(eventsCommon.class);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
    }

}
