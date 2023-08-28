package mods.thecomputerizer.musictriggers;

import com.google.common.collect.Lists;
import com.rits.cloning.Cloner;
import mods.thecomputerizer.musictriggers.client.channels.Channel;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.network.*;
import mods.thecomputerizer.musictriggers.registry.RegistryHandler;
import mods.thecomputerizer.musictriggers.server.channels.ServerChannelManager;
import mods.thecomputerizer.musictriggers.server.data.IPersistentTriggerData;
import mods.thecomputerizer.musictriggers.server.data.PersistentTriggerData;
import mods.thecomputerizer.musictriggers.server.data.PersistentTriggerDataStorage;
import mods.thecomputerizer.theimpossiblelibrary.network.NetworkHandler;
import mods.thecomputerizer.theimpossiblelibrary.util.CustomTick;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.commons.io.FileExistsException;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Mod(modid = Constants.MODID, name = Constants.NAME, version = Constants.VERSION, dependencies = Constants.DEPENDENCIES,
        guiFactory = "mods.thecomputerizer.musictriggers.client.gui.Factory")
public class MusicTriggers {
    private static LogUtil.ModLogger MOD_LOG;
    private static final List<Tuple<String, Integer>> ORDERED_LOG_MESSAGES = Collections.synchronizedList(new ArrayList<>());
    private static Random RANDOM;

    public MusicTriggers() throws IOException {
        MOD_LOG = LogUtil.create(Constants.MODID);
        RANDOM = new Random();
        if (!Constants.CONFIG_DIR.exists())
            if(!Constants.CONFIG_DIR.mkdirs())
                throw new FileExistsException("Unable to create file directory at "+Constants.CONFIG_DIR.getPath()+
                        "! Music Triggers is unable to load any further.");
        ConfigRegistry.initialize(new File(Constants.CONFIG_DIR,"registration.toml"),FMLCommonHandler.instance().getSide().isClient());
        if(!ConfigRegistry.CLIENT_SIDE_ONLY) {
            NetworkHandler.queueServerPacketRegistries(PacketDynamicChannelInfo.class,PacketInitChannels.class,
                    PacketRequestServerConfig.class);
            NetworkHandler.queueClientPacketRegistries(PacketFinishedServerInit.class,PacketJukeBoxCustom.class,
                    PacketMusicTriggersLogin.class, PacketSendCommand.class,PacketSendServerConfig.class,
                    PacketSyncServerInfo.class);
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if(!ConfigRegistry.CLIENT_SIDE_ONLY)
            CapabilityManager.INSTANCE.register(IPersistentTriggerData.class,
                    new PersistentTriggerDataStorage(), PersistentTriggerData::new);
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        if(e.getSide().isClient()) {
            try {
                ChannelManager.initClient(configFile("channels", "toml",true), true);
                ChannelManager.readResourceLocations();
                ClientRegistry.registerKeyBinding(Channel.GUI);
                CustomTick.addCustomTickEvent(20);
                ChannelManager.reloading = false;
            } catch (IOException ex) {
                throw new RuntimeException("Caught a fatal error in Music Triggers configuration registration! Please " +
                        "report this and make sure to include the full crash report.",ex);
            }
        }
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {

    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent e) {
        RegistryHandler.registerCommands(e);
        try {
            if(FMLCommonHandler.instance().getSide().isServer())
                ServerChannelManager.initialize(configFile("channels", "toml",false));
        } catch (IOException ex) {
            logExternally(Level.FATAL,"Could not initialize server channels!");
            Constants.MAIN_LOG.fatal("Count not initialize server channels!",ex);
        }
    }

    public static List<IResourcePack> getActiveResourcePacks() {
        Minecraft mc = Minecraft.getMinecraft();
        ResourcePackRepository repo = mc.getResourcePackRepository();
        List<IResourcePack> packs = Lists.newArrayList(mc.defaultResourcePacks);
        for(ResourcePackRepository.Entry entry : repo.getRepositoryEntries())
            packs.add(entry.getResourcePack());
        IResourcePack serverPack = repo.getServerResourcePack();
        if(Objects.nonNull(serverPack)) packs.add(serverPack);
        return packs;
    }

    public static ResourceLocation getIcon(String type, String name) {
        return Objects.nonNull(type) ? Constants.res("textures/"+type+"/"+name+".png") : Constants.res("textures/"+name);
    }

    public static String[] stringBreaker(String s, String regex) {
        return s.split(regex);
    }

    public static StringBuilder stringBuilder(String s, Object ... parameters) {
        return new StringBuilder(LogUtil.injectParameters(s, parameters));
    }

    public static int randomInt(int max) {
        return RANDOM.nextInt(max);
    }

    /**
     * Uses a fallback in case someone decides to add something that is not a number to a number parameter
     */
    public static int randomInt(String parameter, String toConvert, int fallback) {
        String[] broken = stringBreaker(toConvert,":");
        if(broken.length==1) return parse(parameter, broken[0], fallback);
        int min = parse(parameter, broken[0], fallback);
        int max = parse(parameter, broken[1], fallback);
        if(min==max) return min;
        else if(min>max) {
            int temp = max;
            max = min;
            min = temp;
        }
        if(max-min<=0) return min;
        return min+ RANDOM.nextInt(max-min);
    }

    private static int parse(String parameter, String element, int fallback) {
        try {
            return Integer.parseInt(element);
        } catch (NumberFormatException ignored) {
            logExternally(Level.WARN,"Invalid element {} for parameter {}! Using fallback {}",element,parameter,
                    fallback);
            return fallback;
        }
    }

    public static float randomFloat(String parameter, String toConvert, float fallback) {
        String[] broken = stringBreaker(toConvert,":");
        if(broken.length==1) return parse(parameter, broken[0], fallback);
        float min = parse(parameter, broken[0], fallback);
        float max = parse(parameter, broken[1], fallback);
        if(min==max) return min;
        else if(min>max) {
            float temp = max;
            max = min;
            min = temp;
        }
        float h = max-min;
        if(h<=0) return min;
        return min+(RANDOM.nextFloat()*h);
    }

    private static float parse(String parameter, String element, float fallback) {
        try {
            return Float.parseFloat(element);
        } catch (NumberFormatException ignored) {
            logExternally(Level.WARN,"Invalid element {} for parameter {}! Using fallback {}",element,parameter,
                    fallback);
            return fallback;
        }
    }

    public static void logExternally(Level level, String message, Object ... parameters) {
        MOD_LOG.log(level, message, parameters);
        ORDERED_LOG_MESSAGES.add(new Tuple<>("["+String.format("%-5s",level.toString())+"] "+
                LogUtil.injectParameters(message, parameters),colorizeLogLevel(level)));
    }

    private static int colorizeLogLevel(Level level) {
        if(level==Level.DEBUG) return GuiUtil.makeRGBAInt(200,200,200,255);
        else if(level==Level.INFO) return GuiUtil.WHITE;
        else if(level==Level.WARN) return GuiUtil.makeRGBAInt(255,215,0,255);
        else if(level==Level.ERROR) return GuiUtil.makeRGBAInt(255,0,0,255);
        return GuiUtil.makeRGBAInt(100,0,0,255);
    }

    public static List<Tuple<String,Integer>> getLogEntries() {
        return Collections.unmodifiableList(ORDERED_LOG_MESSAGES);
    }

    public static void clearLog() {
        ORDERED_LOG_MESSAGES.clear();
    }

    public static <T> T clone(final T o) {
        return Cloner.standard().deepClone(o);
    }

    public static File configFile(String path, String extension, boolean createIfAbsent) {
        File file = new File(Constants.CONFIG_DIR,path+"."+extension);
        return createIfAbsent ? FileUtil.generateNestedFile(file,false) : file;
    }
}