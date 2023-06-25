package mods.thecomputerizer.musictriggers.client.channels;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.ClientEvents;
import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.instance.ChannelHolder;
import mods.thecomputerizer.musictriggers.client.gui.instance.ChannelInstance;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.network.PacketDynamicChannelInfo;
import mods.thecomputerizer.musictriggers.network.PacketInitChannels;
import mods.thecomputerizer.musictriggers.network.PacketRequestServerConfig;
import mods.thecomputerizer.musictriggers.server.channels.ServerChannel;
import mods.thecomputerizer.musictriggers.server.channels.ServerTriggerStatus;
import mods.thecomputerizer.theimpossiblelibrary.client.render.PNG;
import mods.thecomputerizer.theimpossiblelibrary.client.render.Renderable;
import mods.thecomputerizer.theimpossiblelibrary.client.render.Renderer;
import mods.thecomputerizer.theimpossiblelibrary.client.render.Text;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.util.CustomTick;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.TomlUtil;
import net.minecraft.block.BlockJukebox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;
import paulscode.sound.SoundSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Side.CLIENT)
public class ChannelManager {
    private static final HashSet<String> VALID_FILE_EXTENSIONS = new HashSet<>(Arrays.asList(".acc",".flac",".m3u",
            ".m4a",".mkv",".mp3",".mp4",".pls",".ogg",".wav",".webm"));
    private static final HashMap<String,Channel> CHANNEL_MAP = new HashMap<>();
    public static final HashMap<String, HashSet<File>> OPEN_AUDIO_FILES = new HashMap<>();
    public static final Map<Table, Renderable> TICKING_RENDERABLES = new ConcurrentHashMap<>();
    private static final HashSet<String> PAUSED_VANILLA_SOUNDS = new HashSet<>();

    private static JukeboxChannel jukeboxChannel;
    private static File channelsFile;
    public static char blinkerChar = ' ';
    private static int tickCounter = 0;
    public static boolean reloading = true;
    private static boolean isResourceControlled = false;
    private static boolean isServerdControlled = false;
    private static DataStorage worldDataStorage;

    public static void preInit() {
        for(Channel channel : CHANNEL_MAP.values())
            channel.clear();
        CHANNEL_MAP.clear();
        for(Renderable card : TICKING_RENDERABLES.values()) {
            Renderer.removeRenderable(card);
            card.stop();
        }
        TICKING_RENDERABLES.clear();
        PAUSED_VANILLA_SOUNDS.clear();
        Trigger.loadData();
        jukeboxChannel = new JukeboxChannel();
    }

    public static void initServer() throws IOException {
        preInit();
        for(ServerChannel channel : worldDataStorage.serverChannels)
            CHANNEL_MAP.put(channel.getName(),channel.convertToClient());
        parseConfigFiles(false);
    }


    public static void initClient(File channelsFile, boolean startup) throws IOException {
        preInit();
        MusicTriggers.logExternally(Level.INFO,"Initializing client channels");
        ChannelManager.channelsFile = channelsFile;
        FileUtil.generateNestedFile(channelsFile,false);
        Holder channels = getChannelsHolder(channelsFile);
        for(String type : new String[]{"gui","reload","log","playback","debug","registration"})
            checkDisabledGuiButton(channels,type);
        if(channels.getTables().isEmpty()) channels.addTable(null,"example");
        for(Table channel : channels.getTables().values()) {
            if(verifyChannelName(channel.getName())) CHANNEL_MAP.put(channel.getName(),new Channel(channel,isResourceControlled));
            else MusicTriggers.logExternally(Level.ERROR, "Channel {} failed to register! See the above errors for" +
                    "more information.",channel.getName());
        }
        parseConfigFiles(startup);
    }

    private static void checkDisabledGuiButton(Holder holder, String type) {
        if(isResourceControlled) {
            String valName = !type.matches("gui") ? "disable_" + type + "_button" : "disable_" + type;
            if (holder.getValOrDefault(valName, false)) {
                if(Objects.isNull(worldDataStorage)) worldDataStorage = new DataStorage();
                worldDataStorage.disableGuiButton(type);
            }
        }
    }

    private static Holder getChannelsHolder(File channelsFile) throws IOException {
        ResourceLocation channelsResource = MusicTriggers.res("config/channels.toml");
        try {
            IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(channelsResource);
            isResourceControlled = true;
            return TomlUtil.readFully(resource.getInputStream());
        } catch (FileNotFoundException ex) {
            isResourceControlled = false;
            return TomlUtil.readFully(channelsFile);
        }
    }

    private static boolean verifyChannelName(String channelName) {
        if(channelName.matches("preview") || channelName.matches("jukebox")) {
            MusicTriggers.logExternally(Level.ERROR, "Channel name cannot be set to \"jukebox\" or \"preview\"" +
                    "as those are used for internal functions!");
            return false;
        }
        else if(Objects.nonNull(CHANNEL_MAP.get(channelName))) {
            MusicTriggers.logExternally(Level.ERROR, "Channel with name {} already exists! Different channels " +
                    "must have unique names!",channelName);
            return false;
        }
        return true;
    }

    public static boolean verifyOtherFilePath(String filePath) {
        for(Channel channel : CHANNEL_MAP.values())
            if(!channel.verifyOtherFilePath(filePath)) return false;
        return true;
    }

    public static void playCustomJukeboxSong(boolean start, String channel, String id, BlockPos pos) {
        if(!start) jukeboxChannel.stopTrack();
        else jukeboxChannel.playTrack(CHANNEL_MAP.get(channel).getCopyOfTrackFromID(id),pos);
    }

    public static void parseConfigFiles(boolean startup) {
        collectSongs();
        for(Channel channel : CHANNEL_MAP.values()) channel.parseConfigs(startup);
        for(Channel channel : CHANNEL_MAP.values()) channel.parseToggles();
        ConfigDebug.initialize(new File(Constants.CONFIG_DIR,"debug.toml"));
        if(!startup) initializeServerInfo();
    }

    public static void readResourceLocations() {
        for(Channel channel : CHANNEL_MAP.values()) channel.readResourceLocations();
    }

    public static void collectSongs() {
        OPEN_AUDIO_FILES.clear();
        for(Channel channel : CHANNEL_MAP.values()) {
            File folder = new File(channel.getLocalFolder());
            if(!folder.exists()) {
                if(!folder.mkdirs()) {
                    MusicTriggers.logExternally(Level.FATAL,"Unable to get or generate songs folder at " +
                            "location {} for channel {}",folder.getAbsolutePath(),channel.getChannelName());
                    continue;
                }
            }
            File[] listOfFiles = folder.listFiles((dir, name) -> {
                if(!dir.canRead() || Objects.isNull(name) || name.isEmpty()) return false;
                String lName = name.toLowerCase();
                for(String ext : VALID_FILE_EXTENSIONS)
                    if(lName.endsWith(ext)) return true;
                return false;
            });
            if (Objects.nonNull(listOfFiles) && listOfFiles.length>0)
                OPEN_AUDIO_FILES.putIfAbsent(channel.getLocalFolder(),new HashSet<>(Arrays.asList(listOfFiles)));
        }
    }

    public static boolean channelDoesNotExist(String channel) {
        return CHANNEL_MAP.containsKey(channel);
    }

    public static Channel getChannel(String channel) {
        return CHANNEL_MAP.get(channel);
    }

    public static Collection<Channel> getAllChannels() {
        return CHANNEL_MAP.values();
    }

    public static boolean checkMusicTickerCancel() {
        if(!ConfigDebug.PLAY_NORMAL_MUSIC || ConfigDebug.BLOCKED_MOD_CATEGORIES.contains("minecraft;music")) return true;
        for(Channel channel : getAllChannels())
            if(channel.canOverrideMusic()) return true;
        return false;
    }

    @SuppressWarnings("ConstantValue")
    public static boolean handleSoundEventOverride(ISound sound) {
        if(!ConfigDebug.PLAY_NORMAL_MUSIC || Objects.isNull(sound.getSound())) return true;
        if(!sound.getSound().isStreaming() && ConfigDebug.BLOCK_STREAMING_ONLY) return false;
        for(Channel channel : getAllChannels())
            if(channel.getOverrideStatus(sound)) return true;
        return false;
    }

    public static HashSet<SoundCategory> getInterrputedCategories() {
        HashSet<SoundCategory> ret = new HashSet<>();
        for(String interrputed : ConfigDebug.INTERRUPTED_AUDIO_CATEGORIES) {
            if(SoundCategory.getSoundCategoryNames().contains(interrputed)) {
                SoundCategory toAdd = SoundCategory.getByName(interrputed);
                ret.add(toAdd);
            }
        }
        return ret;
    }

    public static void handleAudioStart(boolean pause, HashSet<SoundCategory> categories) {
        if(categories.isEmpty()) return;
        SoundManager sounds = Minecraft.getMinecraft().getSoundHandler().sndManager;
        SoundSystem sys = sounds.sndSystem;
        Consumer<String> handler = soundString -> {
            if(pause) {
                sys.pause(soundString);
                PAUSED_VANILLA_SOUNDS.add(soundString);
            }
            else sys.stop(soundString);
        };
        HashSet<String> soundStrings = new HashSet<>();
        for(SoundCategory category : categories)
            soundStrings.addAll(sounds.categorySounds.get(category));
        for(String sound : soundStrings)
            if(Objects.nonNull(sound))
                handler.accept(sound);
    }

    public static void handleAudioStop(HashSet<SoundCategory> categories) {
        SoundManager sounds = Minecraft.getMinecraft().getSoundHandler().sndManager;
        SoundSystem sys = sounds.sndSystem;
        Iterator<String> pausedItr = PAUSED_VANILLA_SOUNDS.iterator();
        while(pausedItr.hasNext()) {
            String paused = pausedItr.next();
            for(SoundCategory category : categories) {
                if(sounds.categorySounds.get(category).contains(paused)) {
                    sys.play(paused);
                    pausedItr.remove();
                    break;
                }
            }
        }
    }

    public static void initializeServerInfo() {
        if(!ConfigRegistry.CLIENT_SIDE_ONLY) {
            ServerTriggerStatus data = new ServerTriggerStatus();
            for (Channel channel : CHANNEL_MAP.values())
                channel.initializeServerData(data);
            new PacketInitChannels(data).send();
        }
    }

    public static void syncInfoFromServer(ClientSync sync) {
        try {
            getChannel(sync.getChannel()).sync(sync);
        } catch (NullPointerException exception) {
            MusicTriggers.logExternally(Level.ERROR, "Channel "+sync.getChannel()+" did not exist and could " +
                    "not be synced!");
        }
    }

    public static void jukeboxPause() {
        for(Channel channel : CHANNEL_MAP.values()) channel.jukeBoxPause();
    }

    public static void jukeboxUnpause() {
        for(Channel channel : CHANNEL_MAP.values()) channel.jukeBoxUnpause();
    }

    public static void pauseAllChannels() {
        for(Channel channel : CHANNEL_MAP.values())
            channel.setPausedGeneric(true);
    }

    public static void unpauseAllChannels() {
        for(Channel channel : CHANNEL_MAP.values())
            channel.setPausedGeneric(false);
    }

    public static void checkResourceReload() {
        if(!reloading && !isServerdControlled) {
            ResourceLocation channelsRes = MusicTriggers.res("config/channels.toml");
            for(IResourcePack pack : MusicTriggers.getActiveResourcePacks()) {
                if(pack.resourceExists(channelsRes)) {
                    MusicTriggers.logExternally(Level.INFO,"Detected configuration in resource pack {}! " +
                            "Reloading channels...",pack.getPackName());
                    reloadAllChannels();
                    break;
                }
            }
        }
    }

    public static void reloadAllChannels() {
        try {
            if(isServerdControlled) new PacketRequestServerConfig().send();
            else {
                initClient(channelsFile,false);
                reloading = false;
            }
        } catch (IOException ex) {
            MusicTriggers.logExternally(Level.FATAL, "Failed to reload channels");
            Constants.MAIN_LOG.fatal("Failed to reload channels for Music Triggers!",ex);
        }
    }

    public static void addRenderable(boolean title, Table table) {
        if(!TICKING_RENDERABLES.containsKey(table)) {
            if(title) {
                MusicTriggers.logExternally(Level.DEBUG, "Initializing title card");
                Text titleCard = new Text(table.getVarMap());
                Renderer.addRenderable(titleCard);
                TICKING_RENDERABLES.put(table,titleCard);
            }
            else {
                PNG imageCard = Renderer.initializePng(MusicTriggers.getIcon(null,
                        table.getValOrDefault("name","missing")),table.getVarMap());
                if(Objects.nonNull(imageCard)) {
                    MusicTriggers.logExternally(Level.DEBUG, "Initializing image card");
                    Renderer.addRenderable(imageCard);
                    TICKING_RENDERABLES.put(table,imageCard);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void tickChannels(CustomTick event) {
        jukeboxChannel.checkStopPlaying(reloading);
        if(!reloading) {
            try {
                if (event.checkTickRate(20)) {
                    synchronized (TICKING_RENDERABLES) {
                        TICKING_RENDERABLES.entrySet().removeIf(entry -> !entry.getValue().canRender());
                    }
                    tickCounter++;
                    if (checkForJukeBox()) jukeboxPause();
                    else jukeboxUnpause();
                    if ((ConfigDebug.PAUSE_WHEN_TABBED && !ClientEvents.IS_DISPLAY_FOCUSED) || Minecraft.getMinecraft().isGamePaused())
                        pauseAllChannels();
                    else unpauseAllChannels();
                    for (Channel channel : CHANNEL_MAP.values())
                        if(!channel.isPaused() && !channel.isFrozen()) channel.tickFast();
                    if (tickCounter % 4 == 0) {
                        for(Channel channel : CHANNEL_MAP.values())
                            if(!channel.isFrozen()) channel.tickSlow();
                        runToggles();
                        sendUpdatePacket();
                    }
                    if (tickCounter % 10 == 0) {
                        if (blinkerChar == ' ') blinkerChar = '|';
                        else if (blinkerChar == '|') blinkerChar = ' ';
                    }
                    if (tickCounter >= 100) tickCounter = 0;
                }
            } catch (Exception e) {
                Constants.MAIN_LOG.fatal("Caught unknown exception while checking audio conditions!",e);
                MusicTriggers.logExternally(Level.FATAL,"Caught unknown exception while checking audio conditions! " +
                        "Freezing all channels until reloaded. See the main log for the full stacktrace of the error.");
                reloading = true;
            }
        }
    }

    public static void runToggles() {
        Map<Channel,Map<String,HashSet<Trigger>>> targetMaps = new HashMap<>();
        for(Channel channel : CHANNEL_MAP.values()) {
            for(Map.Entry<Channel,Map<String,HashSet<Trigger>>> targetMapEntry : channel.getToggleTargets().entrySet()) {
                for(Map.Entry<String,HashSet<Trigger>> targetEntry : targetMapEntry.getValue().entrySet()) {
                    Channel targetChannel = targetMapEntry.getKey();
                    String targetCon = targetEntry.getKey();
                    targetMaps.putIfAbsent(targetChannel,new HashMap<>());
                    targetMaps.get(targetChannel).putIfAbsent(targetCon,new HashSet<>());
                    targetMaps.get(targetChannel).get(targetCon).addAll(targetEntry.getValue());
                }
            }
        }
        runToggleTargetMaps(targetMaps);
    }

    public static void runToggleTargetMaps(Map<Channel,Map<String,HashSet<Trigger>>> targetMaps) {
        for(Map.Entry<Channel,Map<String,HashSet<Trigger>>> targetMap : targetMaps.entrySet())
            targetMap.getKey().runToggles(targetMap.getValue());
    }

    private static boolean checkForJukeBox() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if(Objects.nonNull(player))
            for (int x = player.chunkCoordX - 3; x <= player.chunkCoordX + 3; x++)
                for (int z = player.chunkCoordZ - 3; z <= player.chunkCoordZ + 3; z++) {
                    Map<BlockPos, TileEntity> currentChunkTE = player.getEntityWorld().getChunk(x, z).getTileEntityMap();
                    for (TileEntity te : currentChunkTE.values())
                        if (te instanceof BlockJukebox.TileEntityJukebox && te.getBlockMetadata() != 0)
                            return true;
                }
        return false;
    }

    public static void refreshDebug() {
        ConfigDebug.read();
    }

    private static void sendUpdatePacket() {
        if(Objects.nonNull(Minecraft.getMinecraft().player) && !ConfigRegistry.CLIENT_SIDE_ONLY) {
            List<Channel> updatedChannels = new ArrayList<>();
            for(Channel channel : CHANNEL_MAP.values())
                if(channel.needsUpdatePacket()) updatedChannels.add(channel);
            if(!updatedChannels.isEmpty())
                new PacketDynamicChannelInfo(updatedChannels).send();
        }
    }

    public static ChannelHolder createGuiData() {
        Map<String, ChannelInstance> channels = CHANNEL_MAP.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().createGuiData()));
        return new ChannelHolder(MusicTriggers.configFile("channels","toml"),channels);
    }

    public static boolean isClientConfig() {
        return !isResourceControlled && !isServerdControlled;
    }

    public static boolean isButtonEnabled(String type) {
        return isClientConfig() || worldDataStorage.isButtonEnabled(type);
    }

    public static void readStoredData() {
        for(Map.Entry<String,Channel> channelEntry : CHANNEL_MAP.entrySet())
            channelEntry.getValue().readStoredData(worldDataStorage.toggleMap.get(channelEntry.getKey()),
                    worldDataStorage.playedOnceMap.get(channelEntry.getKey()));
        reloading = false;
    }

    public static void onClientLogin(ByteBuf buf) {
        reloading = true;
        isServerdControlled = buf.readBoolean();
        DataStorage previousStorage = worldDataStorage;
        worldDataStorage = new DataStorage(NetworkUtil.readGenericMap(buf,NetworkUtil::readString,
                buf1 -> NetworkUtil.readGenericMap(buf1,NetworkUtil::readString,ByteBuf::readBoolean)),
                NetworkUtil.readGenericMap(buf,NetworkUtil::readString, buf1 -> NetworkUtil.readGenericMap(buf1,
                        NetworkUtil::readString, buf2 -> NetworkUtil.readGenericList(buf2,NetworkUtil::readString))));
        worldDataStorage.inheritStartupData(previousStorage);
        if(isServerdControlled) new PacketRequestServerConfig().send();
        else readStoredData();
    }

    public static void onClientLogout() {
        boolean needsReload = isServerdControlled;
        isServerdControlled = false;
        if(needsReload) {
            reloading = true;
            reloadAllChannels();
        } else
            for(Channel channel : CHANNEL_MAP.values())
                channel.onLogOut();

    }

    public static void addServerChannels(List<ServerChannel> serverChannels, List<String> disabledGuiButtons) {
        worldDataStorage.syncServeChannels(serverChannels);
        try {
            initServer();
            for(String type : disabledGuiButtons)
                worldDataStorage.disableGuiButton(type);
        } catch (IOException ex) {
            MusicTriggers.logExternally(Level.FATAL, "Failed to server channels");
            Constants.MAIN_LOG.fatal("Failed to reload server channels for Music Triggers!",ex);
        }
        reloading = false;
    }

    private static class DataStorage {
        private final Map<String,Map<String,Boolean>> toggleMap;
        private final Map<String,Map<String,List<String>>> playedOnceMap;
        private final List<ServerChannel> serverChannels;
        private boolean canOpenGui = true;
        private boolean canOpenPlayBack = true;
        private boolean canReload = true;
        private boolean canOpenLog = true;
        private boolean canOpenDebug = true;
        private boolean canOpenRegistry = true;

        /**
         * For startup
         */
        private DataStorage() {
            this.toggleMap = new HashMap<>();
            this.playedOnceMap = new HashMap<>();
            this.serverChannels = new ArrayList<>();
        }

        private DataStorage(Map<String,Map<String,Boolean>> toggleMap,Map<String,Map<String,List<String>>> playedOnceMap) {
            this.toggleMap = toggleMap;
            this.playedOnceMap = playedOnceMap;
            this.serverChannels = new ArrayList<>();
        }

        private void inheritStartupData(DataStorage previousStorage) {
            if(Objects.nonNull(previousStorage)) {
                this.canOpenGui = previousStorage.canOpenGui;
                this.canOpenPlayBack = previousStorage.canOpenPlayBack;
                this.canReload = previousStorage.canReload;
                this.canOpenLog = previousStorage.canOpenLog;
                this.canOpenDebug = previousStorage.canOpenDebug;
                this.canOpenRegistry = previousStorage.canOpenRegistry;
            }
        }

        private void syncServeChannels(List<ServerChannel> serverChannels) {
            this.serverChannels.clear();
            this.serverChannels.addAll(serverChannels);
        }

        private void disableGuiButton(String type) {
            switch (type) {
                case "gui" : {
                    this.canOpenGui = false;
                    this.canOpenPlayBack = false;
                    this.canReload = false;
                    this.canOpenLog = false;
                    this.canOpenDebug = false;
                    this.canOpenRegistry = false;
                    return;
                }
                case "playback" : {
                    this.canOpenPlayBack = false;
                    return;
                }
                case "reload" : {
                    this.canReload = false;
                    return;
                }
                case "log" : {
                    this.canOpenLog = false;
                    return;
                }
                case "debug" : {
                    this.canOpenDebug = false;
                    return;
                }
                case "registration" : {
                    this.canOpenRegistry = false;
                }
            }
        }

        public boolean isButtonEnabled(String type) {
            if(!this.canOpenGui) return false;
            switch (type) {
                case "playback" : return this.canOpenPlayBack;
                case "reload" : return this.canReload;
                case "log" : return this.canOpenLog;
                case "debug" : return this.canOpenDebug;
                case "registration" : return this.canOpenRegistry;
                default : return true;
            }
        }
    }
}
