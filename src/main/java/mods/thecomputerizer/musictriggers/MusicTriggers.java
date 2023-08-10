package mods.thecomputerizer.musictriggers;

import com.rits.cloning.Cloner;
import mods.thecomputerizer.musictriggers.client.channels.Channel;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.network.*;
import mods.thecomputerizer.musictriggers.registry.ItemRegistry;
import mods.thecomputerizer.musictriggers.registry.RegistryHandler;
import mods.thecomputerizer.musictriggers.registry.items.CustomRecord;
import mods.thecomputerizer.musictriggers.registry.items.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.server.channels.ServerChannelListener;
import mods.thecomputerizer.musictriggers.server.data.IPersistentTriggerData;
import mods.thecomputerizer.theimpossiblelibrary.TheImpossibleLibrary;
import mods.thecomputerizer.theimpossiblelibrary.network.NetworkHandler;
import mods.thecomputerizer.theimpossiblelibrary.util.CustomTick;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.util.Tuple;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.commons.io.FileExistsException;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Mod(Constants.MODID)
@Mod.EventBusSubscriber(modid = Constants.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MusicTriggers {
    private static LogUtil.ModLogger MOD_LOG;
    private static final List<Tuple<String, Integer>> ORDERED_LOG_MESSAGES = Collections.synchronizedList(new ArrayList<>());
    private static Random RANDOM;

    public MusicTriggers() throws IOException {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerCapabilities);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
        MOD_LOG = LogUtil.create(Constants.MODID);
        RANDOM = new Random();
        if(!Constants.CONFIG_DIR.exists())
            if(!Constants.CONFIG_DIR.mkdirs())
                throw new FileExistsException("Unable to create file directory at "+Constants.CONFIG_DIR.getPath()+
                        "! Music Triggers is unable to load any further.");
        ConfigRegistry.initialize(new File(Constants.CONFIG_DIR,"registration.toml"),FMLEnvironment.dist.isClient());
        if(!ConfigRegistry.CLIENT_SIDE_ONLY) {
            if(ConfigRegistry.REGISTER_DISCS)
                RegistryHandler.init(FMLJavaModLoadingContext.get().getModEventBus());
            NetworkHandler.queuePacketRegisterToServer(PacketDynamicChannelInfo.class,PacketDynamicChannelInfo::new);
            NetworkHandler.queuePacketRegisterToServer(PacketInitChannels.class,PacketInitChannels::new);
            NetworkHandler.queuePacketRegisterToServer(PacketInitChannelsLogin.class, PacketInitChannelsLogin::new);
            NetworkHandler.queuePacketRegisterToServer(PacketRequestServerConfig.class,buf -> new PacketRequestServerConfig(buf));
            NetworkHandler.queuePacketRegisterToClient(PacketFinishedServerInit.class,buf -> new PacketFinishedServerInit(buf));
            NetworkHandler.queuePacketRegisterToClient(PacketJukeBoxCustom.class,PacketJukeBoxCustom::new);
            NetworkHandler.queuePacketRegisterToClient(PacketMusicTriggersLogin.class,PacketMusicTriggersLogin::new);
            NetworkHandler.queuePacketRegisterToClient(PacketSendCommand.class,PacketSendCommand::new);
            NetworkHandler.queuePacketRegisterToClient(PacketSendServerConfig.class,PacketSendServerConfig::new);
            NetworkHandler.queuePacketRegisterToClient(PacketSyncServerInfo.class,PacketSyncServerInfo::new);
            if(Constants.isDev()) TheImpossibleLibrary.enableDevLog();
        }
    }

    private void clientSetup(final FMLClientSetupEvent ev) {
        ClientRegistry.registerKeyBinding(Channel.GUI);
        CustomTick.addCustomTickEvent(20);
        //music disc texture overrides
        ev.enqueueWork(() -> ItemProperties.register(ItemRegistry.MUSIC_TRIGGERS_RECORD.get(), Constants.res("trigger"),
                (stack, level, living, id) -> {
                    if (stack.getOrCreateTag().contains("triggerID"))
                        return MusicTriggersRecord.mapTriggerToFloat(stack.getOrCreateTag().getString("triggerID"));
                    return 0f;
                }));
        ev.enqueueWork(() -> ItemProperties.register(ItemRegistry.CUSTOM_RECORD.get(),
                Constants.res("custom_record"),
                (stack, level, living, id) -> {
                    if (stack.getOrCreateTag().contains("triggerID"))
                        return CustomRecord.mapTriggerToFloat(stack.getOrCreateTag().getString("channelFrom"),
                                stack.getOrCreateTag().getString("triggerID"));
                    return 0f;
                }));
    }

    public void commonSetup(FMLCommonSetupEvent ev) {
    }

    public void loadComplete(FMLLoadCompleteEvent ev) {
        if(FMLEnvironment.dist.isClient()) {
            try {
                ChannelManager.initClient(configFile("channels", "toml"), true);
                ChannelManager.readResourceLocations();
                CustomTick.addCustomTickEvent(20);
                ChannelManager.reloading = false;
            } catch (IOException ex) {
                throw new RuntimeException("Caught a fatal error in Music Triggers configuration registration! Please " +
                        "report this and make sure to include the full crash report.",ex);
            }
        }
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IPersistentTriggerData.class);
    }

    @SubscribeEvent
    public void onReloadData(AddReloadListenerEvent ev) {
        ev.addListener(new ServerChannelListener());
    }

    public static List<PackResources> getActiveResourcePacks() {
        /*
        Minecraft mc = Minecraft.getInstance();
        ResourcePackList repo = mc.getResourcePackRepository();
        repo.
        List<IResourcePack> packs = Lists.newArrayList(mc.defaultResourcePacks);
        for(ResourcePackList.Entry entry : repo.getRepositoryEntries())
            packs.add(entry.getResourcePack());
        IResourcePack serverPack = repo.getServerResourcePack();
        if(Objects.nonNull(serverPack)) packs.add(serverPack);
        return packs;
         */
        return new ArrayList<>();
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

    public static File configFile(String path, String extension) {
        return FileUtil.generateNestedFile(new File(Constants.CONFIG_DIR,path+"."+extension),false);
    }
}
