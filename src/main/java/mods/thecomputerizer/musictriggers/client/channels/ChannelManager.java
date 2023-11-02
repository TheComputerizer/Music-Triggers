package mods.thecomputerizer.musictriggers.client.channels;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.core.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.ClientEvents;
import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.client.data.Audio;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.instance.ChannelHolder;
import mods.thecomputerizer.musictriggers.client.gui.instance.ChannelInstance;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
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
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import mods.thecomputerizer.theimpossiblelibrary.util.CustomTick;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.TomlUtil;
import net.minecraft.block.BlockJukebox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Side.CLIENT)
public class ChannelManager {
    private static final HashSet<String> VALID_FILE_EXTENSIONS = new HashSet<>(Arrays.asList(".acc",".flac",".m3u",
            ".m4a",".mkv",".mp3",".mp4",".pls",".ogg",".wav",".webm"));
    private static final Map<String,IChannel> CHANNEL_MAP = new HashMap<>();
    private static final List<Channel> ORDERED_CHANNELS = new ArrayList<>();
    public static final Map<String,Set<File>> OPEN_AUDIO_FILES = new HashMap<>();
    public static final Map<Table,Renderable> TICKING_RENDERABLES = new ConcurrentHashMap<>();
    private static final Set<String> PAUSED_VANILLA_SOUNDS = new HashSet<>();
    private static final Map<Channel,Trigger.Link> ACTIVE_LINKS_FROM = new HashMap<>();
    private static final Map<Channel,Trigger.Link> ACTIVE_LINKS_TO = new HashMap<>();
    private static final List<MessageImpl> QUEUED_LOGIN_PACKETS = new ArrayList<>();
    private static String youtubeEmail = null;
    private static String youtubePassword = null;
    private static File channelsFile;
    public static char blinkerChar = ' ';
    private static int tickCounter = 0;
    public static boolean reloading = true;
    public static boolean isPlayerAssigned = false;
    private static boolean isServerInfoInitialized = false;
    private static boolean caughtNullJukebox = false;
    private static boolean isResourceControlled = false;
    private static boolean isServerdControlled = false;
    private static DataStorage worldDataStorage;

    public static void preInit() {
        ORDERED_CHANNELS.clear();
        for(Channel channel : getAllChannels())
            channel.clear();
        CHANNEL_MAP.clear();
        for(Renderable card : TICKING_RENDERABLES.values()) {
            Renderer.removeRenderable(card);
            card.stop();
        }
        TICKING_RENDERABLES.clear();
        PAUSED_VANILLA_SOUNDS.clear();
        Trigger.loadData();
        CHANNEL_MAP.put("jukebox",new JukeboxChannel());
        caughtNullJukebox = false;
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
        ResourceLocation channelsResource = Constants.res("config/channels.toml");
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
        for(Channel channel : getAllChannels())
            if(!channel.verifyOtherFilePath(filePath)) return false;
        return true;
    }

    public static JukeboxChannel getJukeBoxChannel() {
        IChannel channel = getChannel("jukebox");
        if(channel instanceof JukeboxChannel) return (JukeboxChannel)channel;
        MusicTriggers.logExternally(Level.ERROR,"Jukebox channel does not exist! This is an issue!");
        caughtNullJukebox = true;
        return null;
    }

    public static void distributeSoundUpdate(SoundCategory category, float volume) {
        for(IChannel channel : CHANNEL_MAP.values()) {
            channel.onSetSound(category,volume);
        }
    }

    public static void playCustomJukeboxSong(boolean start, String otherChannelName, String id, BlockPos pos) {
        if(!caughtNullJukebox) {
            JukeboxChannel jukebox = getJukeBoxChannel();
            if (Objects.nonNull(jukebox)) {
                if (!start) jukebox.stopTrack();
                else {
                    Channel channel = getNonDefaultChannel(otherChannelName);
                    if (Objects.nonNull(channel)) jukebox.playTrack(channel.getCopyOfTrackFromID(id), pos);
                    else MusicTriggers.logExternally(Level.ERROR, "Cannot play jukebox track from unknown channel " +
                            "{}", otherChannelName);
                }
            }
        }
    }

    public static void parseConfigFiles(boolean startup) {
        ORDERED_CHANNELS.addAll(getAllChannels());
        ORDERED_CHANNELS.sort(Comparator.comparing(Channel::getChannelName));
        collectSongs();
        for(Channel channel : getAllChannels()) channel.parseConfigs(startup);
        for(Channel channel : getAllChannels()) channel.parseMoreConfigs();
        ConfigDebug.initialize(new File(Constants.CONFIG_DIR,"debug.toml"));
        if(!startup) initializeServerInfo();
    }

    public static void readResourceLocations() {
        for(Channel channel : getAllChannels()) channel.readResourceLocations();
    }

    public static void collectSongs() {
        OPEN_AUDIO_FILES.clear();
        for(Channel channel : getAllChannels()) {
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
        return !CHANNEL_MAP.containsKey(channel);
    }

    public static IChannel getChannel(String channelName) {
        return CHANNEL_MAP.get(channelName);
    }

    public static Channel getNonDefaultChannel(String channelName) {
        IChannel channel = getChannel(channelName);
        return channel instanceof Channel ? (Channel)channel : null;
    }

    public static Collection<Channel> getAllChannels() {
        Set<Channel> channels = new HashSet<>();
        for(IChannel channel : CHANNEL_MAP.values())
            if(channel instanceof Channel) channels.add((Channel)channel);
        return channels;
    }

    public static List<Channel> getOrderedChannels() {
        return ORDERED_CHANNELS;
    }

    public static void registerRemoteSources(AudioPlayerManager manager) {
        registerRemoteSource(manager,"YouTube",() -> new YoutubeAudioSourceManager(true, youtubeEmail, youtubePassword));
        registerRemoteSource(manager,"SoundCloud",SoundCloudAudioSourceManager::createDefault);
        registerRemoteSource(manager,"BandCamp",BandcampAudioSourceManager::new);
        registerRemoteSource(manager,"Vimeo",VimeoAudioSourceManager::new);
        registerRemoteSource(manager,"Twitch",TwitchStreamAudioSourceManager::new);
        registerRemoteSource(manager,"Beam",BeamAudioSourceManager::new);
        registerRemoteSource(manager,"Getyarn",GetyarnAudioSourceManager::new);
        registerRemoteSource(manager,"HTTPAudio",() -> new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));
    }

    private static void registerRemoteSource(AudioPlayerManager manager, String sourceName, Supplier<AudioSourceManager> supplier) {
        try {
            manager.registerSourceManager(supplier.get());
        } catch (Exception ex) {
            MusicTriggers.logExternalException("Failed to register remote source for {}!"," See the main log for the "+
                    "full stacktrace.",ex,sourceName);
        }
    }

    public static boolean checkMusicTickerCancel() {
        if(!ConfigDebug.PLAY_NORMAL_MUSIC || ConfigDebug.BLOCKED_MOD_CATEGORIES.contains("all;music") ||
                ConfigDebug.BLOCKED_MOD_CATEGORIES.contains("minecraft;music")) return true;
        for(Channel channel : getAllChannels())
            if(channel.canOverrideMusic()) return true;
        return false;
    }

    public static boolean handleSoundEventOverride(Sound sound, SoundCategory category) {
        if(!ConfigDebug.PLAY_NORMAL_MUSIC || Objects.isNull(sound)) return true;
        if(!sound.isStreaming() && ConfigDebug.BLOCK_STREAMING_ONLY) return false;
        for(Channel channel : getAllChannels())
            if(channel.getOverrideStatus(category)) return true;
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

    public static void handleAudioStart(boolean pause, Set<SoundCategory> categories) {
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
        Set<String> soundStrings = new HashSet<>();
        for(SoundCategory category : categories)
            soundStrings.addAll(sounds.categorySounds.get(category));
        for(String sound : soundStrings)
            if(Objects.nonNull(sound))
                handler.accept(sound);
    }

    public static void handleAudioStop(Set<SoundCategory> categories) {
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
            for(IChannel channel : CHANNEL_MAP.values()) {
                channel.initCache();
                if(channel instanceof Channel)
                    ((Channel)channel).initializeServerData(data);
            }
            if(Objects.isNull(Minecraft.getMinecraft().player)) QUEUED_LOGIN_PACKETS.add(new PacketInitChannels(data));
            else new PacketInitChannels(data).send();
        }
    }

    public static void finalizeServerChannelInit() {
        isServerInfoInitialized = true;
    }

    public static void syncInfoFromServer(ClientSync sync) {
        try {
            getNonDefaultChannel(sync.getChannel()).sync(sync);
        } catch (NullPointerException exception) {
            MusicTriggers.logExternally(Level.ERROR, "Channel "+sync.getChannel()+" did not exist and could " +
                    "not be synced!");
        }
    }

    public static void jukeboxPause() {
        for(Channel channel : getAllChannels()) channel.jukeBoxPause();
    }

    public static void jukeboxUnpause() {
        for(Channel channel : getAllChannels()) channel.jukeBoxUnpause();
    }

    public static void pauseAllChannels() {
        for(Channel channel : getAllChannels())
            channel.setPausedGeneric(true);
    }

    public static void unpauseAllChannels() {
        for(Channel channel : getAllChannels())
            channel.setPausedGeneric(false);
    }

    public static void checkResourceReload() {
        if(!reloading && !isServerdControlled) {
            ResourceLocation channelsRes = Constants.res("config/channels.toml");
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
        QUEUED_LOGIN_PACKETS.clear();
        try {
            if(isServerdControlled) {
                if(Objects.isNull(Minecraft.getMinecraft().player)) QUEUED_LOGIN_PACKETS.add(new PacketRequestServerConfig());
                else new PacketRequestServerConfig().send();
            }
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
        if(!isPlayerAssigned) {
            if(Objects.nonNull(Minecraft.getMinecraft().player)) {
                for(MessageImpl packet : QUEUED_LOGIN_PACKETS) packet.send();
                QUEUED_LOGIN_PACKETS.clear();
                isPlayerAssigned = true;
            }
        }
        if(!reloading) {
            if(!caughtNullJukebox) {
                JukeboxChannel jukebox = getJukeBoxChannel();
                if(Objects.nonNull(jukebox)) jukebox.checkStopPlaying(reloading);
            }
            try {
                if(event.checkTickRate(20)) {
                    synchronized(TICKING_RENDERABLES) {
                        TICKING_RENDERABLES.entrySet().removeIf(entry -> !entry.getValue().canRender());
                    }
                    tickCounter++;
                    Minecraft mc = Minecraft.getMinecraft();
                    Trigger.updateEffectCache(mc.player);
                    if(checkForJukeBox()) jukeboxPause();
                    else jukeboxUnpause();
                    if((ConfigDebug.PAUSE_WHEN_TABBED && !ClientEvents.IS_DISPLAY_FOCUSED) || mc.isGamePaused())
                        pauseAllChannels();
                    else unpauseAllChannels();
                    for(IChannel ichannel : CHANNEL_MAP.values()) {
                        if(ichannel instanceof Channel) {
                            Channel channel = (Channel)ichannel;
                            if(!channel.isPaused() && channel.isNotFrozen()) channel.tickFast();
                        }
                        else ichannel.tickFast();
                    }
                    if(tickCounter%4==0) {
                        for(Channel channel : getAllChannels())
                            if(channel.isNotFrozen()) channel.tickSlow();
                        runToggles();
                        if(isServerInfoInitialized) sendUpdatePacket();
                    }
                    if(tickCounter%10==0) {
                        if(blinkerChar==' ') blinkerChar = '|';
                        else if(blinkerChar=='|') blinkerChar = ' ';
                    }
                    if(tickCounter>=100) tickCounter = 0;
                }
            } catch (Exception e) {
                Constants.MAIN_LOG.fatal("Caught unknown exception while checking audio conditions!",e);
                MusicTriggers.logExternally(Level.FATAL,"Caught unknown exception while checking audio conditions! " +
                        "Freezing all channels until reloaded. See the main log for the full stacktrace of the error '{}'.",
                        e.getLocalizedMessage());
                reloading = true;
            }
        }
    }

    public static void activateLink(Trigger.Link link) {
        ACTIVE_LINKS_FROM.put(link.getParentChannel(),link);
        ACTIVE_LINKS_TO.put(link.getLinkedChannel(),link);
        link.activate();
    }

    public static boolean isLinkedFrom(Channel channel, boolean slowVersion) {
        Trigger.Link link = ACTIVE_LINKS_FROM.get(channel);
        if(Objects.isNull(link)) return false;
        if(link.areChannelsDifferent()) {
            if(!slowVersion) return true;
            Channel linkedChannel = link.getLinkedChannel();
            return ACTIVE_LINKS_TO.containsKey(linkedChannel) &&
                    (!linkedChannel.isFadingOut() || ACTIVE_LINKS_FROM.containsKey(linkedChannel));
        }
        return false;
    }

    public static void checkRemoveLinkedFrom(Channel channel) {
        Trigger.Link link = ACTIVE_LINKS_TO.get(channel);
        if(Objects.nonNull(link)) {
            if(channel.getActiveTriggers().isEmpty() || !link.isActive(channel.getActiveTriggers())) {
                ACTIVE_LINKS_TO.remove(channel);
                ACTIVE_LINKS_FROM.remove(link.getLinkedChannel());
            }
        }
    }

    public static long getLinkedTime(Channel channel) {
        Trigger.Link link = ACTIVE_LINKS_FROM.get(channel);
        if(Objects.isNull(link)) link = ACTIVE_LINKS_TO.get(channel);
        return Objects.nonNull(link) ? link.getTime(channel) : 0;
    }

    public static Audio getLinkedAudio(Channel channel) {
        Trigger.Link link = ACTIVE_LINKS_FROM.get(channel);
        if(Objects.isNull(link)) link = ACTIVE_LINKS_TO.get(channel);
        return Objects.nonNull(link) ? link.getResumedAudio(channel) : null;
    }

    public static void checkRemoveLinkedTo(Channel channel, boolean emptyActive) {
        if(emptyActive) ACTIVE_LINKS_TO.remove(channel);
        else ACTIVE_LINKS_TO.entrySet().removeIf(entry -> entry.getKey()==channel &&
                 !ACTIVE_LINKS_FROM.containsKey(channel) && !entry.getValue().shouldLink(channel.getActiveTriggers()));
    }

    public static void setLinkedToTime(Channel channel, long time) {
        if(ACTIVE_LINKS_TO.containsKey(channel))
            ACTIVE_LINKS_TO.get(channel).setTime(channel,time,channel.getCurTrack());
    }

    public static void runToggles() {
        Map<Channel,Map<String,Set<Trigger>>> targetMaps = new HashMap<>();
        for(Channel channel : getAllChannels()) {
            for(Map.Entry<Channel,Map<String,Set<Trigger>>> targetMapEntry : channel.getToggleTargets().entrySet()) {
                for(Map.Entry<String,Set<Trigger>> targetEntry : targetMapEntry.getValue().entrySet()) {
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

    public static void runToggleTargetMaps(Map<Channel,Map<String,Set<Trigger>>> targetMaps) {
        for(Map.Entry<Channel,Map<String,Set<Trigger>>> targetMap : targetMaps.entrySet())
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
            for(Channel channel : getAllChannels())
                if(channel.needsUpdatePacket()) updatedChannels.add(channel);
            if(!updatedChannels.isEmpty() || Instance.changedPreferredSort())
                new PacketDynamicChannelInfo(updatedChannels).send();
        }
    }

    public static ChannelHolder createGuiData() {
        Map<String, ChannelInstance> channels = CHANNEL_MAP.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof Channel)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> ((Channel)entry.getValue()).createGuiData()));
        return new ChannelHolder(MusicTriggers.configFile("channels","toml",true),channels);
    }

    public static boolean isClientConfig() {
        return !isResourceControlled && !isServerdControlled;
    }

    public static boolean isButtonEnabled(String type) {
        return isClientConfig() || worldDataStorage.isButtonEnabled(type);
    }

    public static void readStoredData() {
        for(Map.Entry<String,IChannel> channelEntry : CHANNEL_MAP.entrySet()) {
            if(channelEntry.getValue() instanceof Channel) {
                Channel channel = (Channel)channelEntry.getValue();
                channel.readStoredData(worldDataStorage.toggleMap.get(channelEntry.getKey()),
                        worldDataStorage.playedOnceMap.get(channelEntry.getKey()));
            }
        }
        initializeServerInfo();
        reloading = false;
    }

    public static void onClientLogin(ByteBuf buf) {
        reloading = true;
        isServerdControlled = buf.readBoolean();
        DataStorage previousStorage = worldDataStorage;
        worldDataStorage = new DataStorage(NetworkUtil.readGenericMap(buf,NetworkUtil::readString,
                buf1 -> NetworkUtil.readGenericMap(buf1,NetworkUtil::readString,ByteBuf::readBoolean)),
                NetworkUtil.readGenericMap(buf,NetworkUtil::readString, buf1 -> NetworkUtil.readGenericMap(buf1,
                        NetworkUtil::readString, buf2 -> new Tuple<>(NetworkUtil.readGenericList(buf2,
                                NetworkUtil::readString),buf2.readInt()))));
        Instance.setPreferredSort(MathHelper.clamp(buf.readInt(),1,3));
        worldDataStorage.inheritStartupData(previousStorage);
        if(isServerdControlled) {
            if(Objects.isNull(Minecraft.getMinecraft().player)) QUEUED_LOGIN_PACKETS.add(new PacketRequestServerConfig());
            else new PacketRequestServerConfig().send();
        }
        else readStoredData();
    }

    public static void onClientLogout() {
        isServerInfoInitialized = false;
        boolean needsReload = isServerdControlled;
        isServerdControlled = false;
        if(needsReload) {
            reloading = true;
            reloadAllChannels();
        } else
            for(Channel channel : getAllChannels())
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
        initializeServerInfo();
        reloading = false;
    }

    private static class DataStorage {
        private final Map<String,Map<String,Boolean>> toggleMap;
        private final Map<String,Map<String,Tuple<List<String>,Integer>>> playedOnceMap;
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

        private DataStorage(Map<String,Map<String,Boolean>> toggleMap,Map<String,Map<String,Tuple<List<String>,Integer>>> playedOnceMap) {
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
