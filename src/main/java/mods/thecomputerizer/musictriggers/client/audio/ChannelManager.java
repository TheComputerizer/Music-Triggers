package mods.thecomputerizer.musictriggers.client.audio;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.instance.ChannelHolder;
import mods.thecomputerizer.musictriggers.client.gui.instance.ChannelInstance;
import mods.thecomputerizer.musictriggers.server.data.ServerChannels;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.network.NetworkHandler;
import mods.thecomputerizer.musictriggers.network.packets.PacketDynamicChannelInfo;
import mods.thecomputerizer.musictriggers.network.packets.PacketInitChannels;
import mods.thecomputerizer.theimpossiblelibrary.client.render.PNG;
import mods.thecomputerizer.theimpossiblelibrary.client.render.Renderable;
import mods.thecomputerizer.theimpossiblelibrary.client.render.Renderer;
import mods.thecomputerizer.theimpossiblelibrary.client.render.Text;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.util.CustomTick;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.TomlUtil;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundEngine;
import net.minecraft.client.audio.SoundSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public class ChannelManager {
    public static char blinker = ' ';
    private static JukeboxChannel jukeboxChannel;
    private static File channelsConfig;
    private static final HashMap<String,Channel> channelMap = new HashMap<>();
    public static final HashMap<String, File[]> openAudioFiles = new HashMap<>();
    public static final Map<Table, Renderable> tickingRenderables = new ConcurrentHashMap<>();
    private static final HashSet<ISound> PAUSED_VANILLA_SOUNDS = new HashSet<>();

    private static int tickCounter = 0;
    public static boolean reloading = true;
    public static String CUR_STRUCT = "Structure has not been synced";

    public static void initialize(File channelsFile, boolean startup) throws IOException {
        for(Renderable card : tickingRenderables.values()) {
            Renderer.removeRenderable(card);
            card.stop();
        }
        tickingRenderables.clear();
        PAUSED_VANILLA_SOUNDS.clear();
        Trigger.loadDefaultData();
        jukeboxChannel = new JukeboxChannel("jukebox");
        channelsConfig = channelsFile;
        FileUtil.generateNestedFile(channelsFile,false);
        Holder channels = TomlUtil.readFully(channelsFile);
        if(channels.getTables().isEmpty()) channels.addTable(null,"example");
        for(Table channel : channels.getTables().values()) {
            if(verifyChannelName(channel.getName())) channelMap.put(channel.getName(),new Channel(channel));
            else MusicTriggers.logExternally(Level.ERROR, "Channel {} failed to register! See the above errors for" +
                    "more information.",channel.getName());
        }
        parseConfigFiles(startup);
    }

    private static boolean verifyChannelName(String channelName) {
        if(channelName.matches("preview") || channelName.matches("jukebox")) {
            MusicTriggers.logExternally(Level.ERROR, "Channel name cannot be set to \"jukebox\" or \"preview\"" +
                    "as those are used for internal functions!");
            return false;
        }
        else if(Objects.nonNull(channelMap.get(channelName))) {
            MusicTriggers.logExternally(Level.ERROR, "Channel with name " + channelName + " already exists" +
                    "! Different channels must have unique names!");
            return false;
        }
        return true;
    }

    public static boolean verifyOtherFilePath(String filePath) {
        for(Channel channel : channelMap.values())
            if(!channel.verifyOtherFilePath(filePath)) return false;
        return true;
    }

    public static void playCustomJukeboxSong(boolean start, String channel, String id, BlockPos pos) {
        if(!start) jukeboxChannel.stopTrack();
        else jukeboxChannel.playTrack(channelMap.get(channel).getCopyOfTrackFromID(id),pos);
    }

    public static void parseConfigFiles(boolean startup) {
        collectSongs();
        for(Channel channel : channelMap.values()) channel.parseConfigs(startup);
        ConfigDebug.initialize(new File(Constants.CONFIG_DIR,"debug.toml"));
        if(!startup) initializeServerInfo();
    }

    public static void readResourceLocations() {
        for(Channel channel : channelMap.values()) channel.readResourceLocations();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void collectSongs() {
        openAudioFiles.clear();
        for(Channel channel : channelMap.values()) {
            File folder = new File(channel.getLocalFolder());
            folder.mkdirs();
            File[] listOfFiles = folder.listFiles((dir, name) -> dir.canRead());
            if (listOfFiles != null)
                openAudioFiles.putIfAbsent(channel.getLocalFolder(),listOfFiles);
        }
    }

    public static Channel getChannel(String channel) {
        return channelMap.get(channel);
    }

    public static Collection<Channel> getAllChannels() {
        return channelMap.values();
    }

    @SuppressWarnings("ConstantValue")
    public static boolean handleSoundEventOverride(ISound sound) {
        if(!ConfigDebug.PLAY_NORMAL_MUSIC || Objects.isNull(sound.getSound())) return true;
        if(!sound.getSound().shouldStream() && ConfigDebug.BLOCK_STREAMING_ONLY) return false;
        for(Channel channel : getAllChannels())
            if(channel.getOverrideStatus(sound)) return true;
        return false;
    }

    public static HashSet<SoundCategory> getInterrputedCategories() {
        HashSet<SoundCategory> ret = new HashSet<>();
        for(String interrputed : ConfigDebug.INTERRUPTED_AUDIO_CATEGORIES) {
            if(SoundCategory.BY_NAME.containsKey(interrputed)) {
                SoundCategory toAdd = SoundCategory.BY_NAME.get(interrputed);
                ret.add(toAdd);
            }
        }
        return ret;
    }

    public static void handleAudioStart(boolean pause, HashSet<SoundCategory> categories) {
        if(categories.isEmpty()) return;
        SoundEngine engine = Minecraft.getInstance().getSoundManager().soundEngine;
        Consumer<ISound> handler = sound -> {
            net.minecraft.client.audio.ChannelManager.Entry entry = engine.instanceToChannel.get(sound);
            if(pause) {
                entry.execute(SoundSource::pause);
                PAUSED_VANILLA_SOUNDS.add(sound);
            }
            else entry.execute(SoundSource::stop);
        };
        HashSet<ISound> sounds = new HashSet<>();
        for(SoundCategory category : categories)
            sounds.addAll(engine.instanceBySource.get(category));
        for(ISound sound : sounds)
            if(Objects.nonNull(sound) && engine.instanceToChannel.containsKey(sound))
                handler.accept(sound);
    }

    public static void handleAudioStop(HashSet<SoundCategory> categories) {
        SoundEngine engine = Minecraft.getInstance().getSoundManager().soundEngine;
        Iterator<ISound> pausedItr = PAUSED_VANILLA_SOUNDS.iterator();
        while(pausedItr.hasNext()) {
            ISound paused = pausedItr.next();
            for(SoundCategory category : categories) {
                if(engine.instanceBySource.get(category).contains(paused)) {
                    engine.instanceToChannel.get(paused).execute(SoundSource::unpause);
                    pausedItr.remove();
                    break;
                }
            }
        }
    }

    public static void initializeServerInfo() {
        if(!ConfigRegistry.CLIENT_SIDE_ONLY) {
            ServerChannels data = new ServerChannels();
            for (Channel channel : channelMap.values())
                channel.initializeServerData(data);
            NetworkHandler.sendToServer(new PacketInitChannels(data));
        }
    }

    public static void syncInfoFromServer(ClientSync sync) {
        try {
            getChannel(sync.getChannel()).sync(sync);
        } catch (NullPointerException exception) {
            MusicTriggers.logExternally(Level.ERROR, "Channel {} did not exist and could " +
                    "not be synced!",sync.getChannel());
        }
    }

    public static void jukeboxPause() {
        for(Channel channel : channelMap.values()) channel.jukeBoxPause();
    }

    public static void jukeboxUnpause() {
        for(Channel channel : channelMap.values()) channel.jukeBoxUnpause();
    }

    public static void pauseAllChannels() {
        for(Channel channel : channelMap.values())
            channel.setPausedGeneric(true);
    }

    public static void unpauseAllChannels() {
        for(Channel channel : channelMap.values())
            channel.setPausedGeneric(false);
    }

    public static void reloadAllChannels() {
        for(Channel channel : channelMap.values())
            channel.clear();
        channelMap.clear();
        try {
            initialize(channelsConfig,false);
        } catch (IOException ex) {
            MusicTriggers.logExternally(Level.FATAL, "Failed to reload channels");
            Constants.MAIN_LOG.fatal("Failed to reload channels for Music Triggers!",ex);
        }
    }

    public static void addRenderable(boolean title, Table table) {
        if(!tickingRenderables.containsKey(table)) {
            if(title) {
                MusicTriggers.logExternally(Level.DEBUG, "Initializing title card");
                Text titleCard = new Text(table.getVarMap());
                Renderer.addRenderable(titleCard);
                tickingRenderables.put(table,titleCard);
            }
            else {
                PNG imageCard = Renderer.initializePng(MusicTriggers.getIcon(null,
                        table.getValOrDefault("name","missing")),table.getVarMap());
                if(Objects.nonNull(imageCard)) {
                    MusicTriggers.logExternally(Level.DEBUG, "Initializing image card");
                    Renderer.addRenderable(imageCard);
                    tickingRenderables.put(table,imageCard);
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
                    synchronized (tickingRenderables) {
                        tickingRenderables.entrySet().removeIf(entry -> !entry.getValue().canRender());
                    }
                    tickCounter++;
                    if (checkForJukeBox()) jukeboxPause();
                    else jukeboxUnpause();
                    if ((ConfigDebug.PAUSE_WHEN_TABBED && !Minecraft.getInstance().isWindowActive()) || Minecraft.getInstance().isPaused())
                        pauseAllChannels();
                    else unpauseAllChannels();
                    for (Channel channel : channelMap.values())
                        if (!channel.isPaused()) channel.tickFast();
                    if (tickCounter % 4 == 0) {
                        for (Channel channel : channelMap.values()) channel.tickSlow();
                        sendUpdatePacket();
                    }
                    if (tickCounter % 10 == 0) {
                        if (blinker == ' ') blinker = '|';
                        else if (blinker == '|') blinker = ' ';
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

    private static boolean checkForJukeBox() {
        PlayerEntity player = Minecraft.getInstance().player;
        if(player!=null) {
            for (int x = player.xChunk - 3; x <= player.xChunk + 3; x++) {
                for (int z = player.zChunk - 3; z <= player.zChunk + 3; z++) {
                    Set<BlockPos> currentChunkTEPos = player.level.getChunk(x, z).getBlockEntitiesPos();
                    for (BlockPos b : currentChunkTEPos) {
                        if (player.level.getChunk(x, z).getBlockEntity(b) instanceof JukeboxTileEntity) {
                            JukeboxTileEntity te = (JukeboxTileEntity) player.level.getChunk(x, z).getBlockEntity(b);
                            return te != null && te.getBlockState().getValue(JukeboxBlock.HAS_RECORD);
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void refreshDebug() {
        ConfigDebug.read();
    }

    private static void sendUpdatePacket() {
        if(Minecraft.getInstance().player!=null && !ConfigRegistry.CLIENT_SIDE_ONLY) {
            List<Channel> updatedChannels = new ArrayList<>();
            for(Channel channel : channelMap.values())
                if(channel.needsUpdatePacket()) updatedChannels.add(channel);
            if(!updatedChannels.isEmpty())
                NetworkHandler.sendToServer(new PacketDynamicChannelInfo(updatedChannels));
        }
    }

    public static ChannelHolder createGuiData() {
        Map<String, ChannelInstance> channels = channelMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().createGuiData()));
        return new ChannelHolder(MusicTriggers.configFile("channels","toml"),channels);
    }
}
