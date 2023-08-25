package mods.thecomputerizer.musictriggers;

import com.rits.cloning.Cloner;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.network.*;
import mods.thecomputerizer.musictriggers.registry.BlockRegistry;
import mods.thecomputerizer.musictriggers.registry.ItemRegistry;
import mods.thecomputerizer.musictriggers.registry.TileRegistry;
import mods.thecomputerizer.musictriggers.server.ServerEvents;
import mods.thecomputerizer.musictriggers.server.TriggerCommand;
import mods.thecomputerizer.musictriggers.server.channels.ServerChannelListener;
import mods.thecomputerizer.theimpossiblelibrary.TheImpossibleLibrary;
import mods.thecomputerizer.theimpossiblelibrary.events.EntityAddedEvent;
import mods.thecomputerizer.theimpossiblelibrary.events.ServerPlayerLoginEvent;
import mods.thecomputerizer.theimpossiblelibrary.network.NetworkHandler;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Tuple;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.*;

public class MusicTriggers implements ModInitializer {
    private static final LogUtil.ModLogger MOD_LOG = LogUtil.create(Constants.MODID);
    private static final List<Tuple<String, Integer>> ORDERED_LOG_MESSAGES = Collections.synchronizedList(new ArrayList<>());
    private static final Random RANDOM = new Random();
    public static final ResourceLocation PACKET_DYNAMIC_CHANNEL_INFO = Constants.res("packet_dynamic_channel_info");
    public static final ResourceLocation PACKET_FINISHED_SERVER_INIT= Constants.res("packet_finished_server_init");
    public static final ResourceLocation PACKET_INIT_CHANNELS = Constants.res("packet_init_channels");
    public static final ResourceLocation PACKET_JUKEBOX_CUSTOM = Constants.res("packet_jukebox_customn");
    public static final ResourceLocation PACKET_MUSIC_TRIGGERS_LOGIN = Constants.res("packet_music_triggers_login");
    public static final ResourceLocation PACKET_REQUEST_SERVER_CONFIG = Constants.res("packet_request_server_config");
    public static final ResourceLocation PACKET_SEND_COMMAND = Constants.res("packet_send_command");
    public static final ResourceLocation PACKET_SEND_SERVER_CONFIG = Constants.res("packet_send_server_config");
    public static final ResourceLocation PACKET_SYNC_SERVER_INFO = Constants.res("packet_sync_server_info");

    @Override
    public void onInitialize() {
        if(!Constants.CONFIG_DIR.exists())
            if(!Constants.CONFIG_DIR.mkdirs())
                throw new RuntimeException("Unable to create file directory at "+Constants.CONFIG_DIR.getPath()+
                        "! Music Triggers is unable to load any further.");
        ConfigRegistry.initialize(new File(Constants.CONFIG_DIR,"registration.toml"),false);
        if(!ConfigRegistry.CLIENT_SIDE_ONLY) {
            if (ConfigRegistry.REGISTER_DISCS) {
                Constants.MAIN_LOG.info("Loading Music Triggers discs and blocks");
                BlockRegistry.register();
                ItemRegistry.register();
                TileRegistry.register();
            }
            NetworkHandler.queuePacketRegisterToServer(PacketDynamicChannelInfo.class,PacketDynamicChannelInfo::new,
                    PACKET_DYNAMIC_CHANNEL_INFO);
            NetworkHandler.queuePacketRegisterToServer(PacketInitChannels.class,PacketInitChannels::new,
                    PACKET_INIT_CHANNELS);
            NetworkHandler.queuePacketRegisterToServer(PacketRequestServerConfig.class,PacketRequestServerConfig::new,
                    PACKET_REQUEST_SERVER_CONFIG);
            NetworkHandler.queuePacketRegisterToClient(PacketFinishedServerInit.class,PacketFinishedServerInit::new,
                    PACKET_FINISHED_SERVER_INIT);
            NetworkHandler.queuePacketRegisterToClient(PacketJukeBoxCustom.class,PacketJukeBoxCustom::new,
                    PACKET_JUKEBOX_CUSTOM);
            NetworkHandler.queuePacketRegisterToClient(PacketMusicTriggersLogin.class,PacketMusicTriggersLogin::new,
                    PACKET_MUSIC_TRIGGERS_LOGIN);
            NetworkHandler.queuePacketRegisterToClient(PacketSendCommand.class,PacketSendCommand::new,
                    PACKET_SEND_COMMAND);
            NetworkHandler.queuePacketRegisterToClient(PacketSendServerConfig.class,PacketSendServerConfig::new,
                    PACKET_SEND_SERVER_CONFIG);
            NetworkHandler.queuePacketRegisterToClient(PacketSyncServerInfo.class,PacketSyncServerInfo::new,
                    PACKET_SYNC_SERVER_INFO);
            setUpCommonEvents();
            ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new ServerChannelListener());
        }
        if(Constants.isDev()) TheImpossibleLibrary.enableDevLog();
    }

    private void setUpCommonEvents() {
        CommandRegistrationCallback.EVENT.register((dispatcher,access,environment) -> TriggerCommand.register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(server -> ServerEvents.onServerTick());
        ServerLifecycleEvents.SERVER_STARTING.register(ServerEvents::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerEvents::onServerStopping);
        EntityAddedEvent.EVENT.register(ServerEvents::onEntityAdded);
        AttackEntityCallback.EVENT.register(((player,world,hand,entity,hitResult) -> ServerEvents.onPlayerAttackEntity(player,entity)));
        ServerPlayerLoginEvent.EVENT.register(ServerEvents::onPlayerLogin);
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
        if(broken.length==1) return parseInt(parameter, broken[0], fallback);
        int min = parseInt(parameter, broken[0], fallback);
        int max = parseInt(parameter, broken[1], fallback);
        if(min==max) return min;
        else if(min>max) {
            int temp = max;
            max = min;
            min = temp;
        }
        if(max-min<=0) return min;
        return min+ RANDOM.nextInt(max-min);
    }

    private static int parseInt(String parameter, String element, int fallback) {
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
        if(broken.length==1) return parseFloat(parameter, broken[0], fallback);
        float min = parseFloat(parameter, broken[0], fallback);
        float max = parseFloat(parameter, broken[1], fallback);
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

    private static float parseFloat(String parameter, String element, float fallback) {
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
