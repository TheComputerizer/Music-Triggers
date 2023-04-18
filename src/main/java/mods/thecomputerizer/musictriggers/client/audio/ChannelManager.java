package mods.thecomputerizer.musictriggers.client.audio;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.ClientEvents;
import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.instance.ChannelHolder;
import mods.thecomputerizer.musictriggers.client.gui.instance.ChannelInstance;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.network.NetworkHandler;
import mods.thecomputerizer.musictriggers.network.packets.PacketDynamicChannelInfo;
import mods.thecomputerizer.musictriggers.network.packets.PacketInitChannels;
import mods.thecomputerizer.musictriggers.server.data.ServerChannels;
import mods.thecomputerizer.theimpossiblelibrary.client.render.PNG;
import mods.thecomputerizer.theimpossiblelibrary.client.render.Renderable;
import mods.thecomputerizer.theimpossiblelibrary.client.render.Renderer;
import mods.thecomputerizer.theimpossiblelibrary.client.render.Text;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.util.CustomTick;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.TomlUtil;
import net.minecraft.block.BlockJukebox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;
import paulscode.sound.SoundSystem;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Side.CLIENT)
public class ChannelManager {
    public static char blinker = ' ';
    private static JukeboxChannel jukeboxChannel;
    private static File channelsConfig;
    private static final HashMap<String,Channel> channelMap = new HashMap<>();
    public static final HashMap<String, File[]> openAudioFiles = new HashMap<>();
    public static final Map<Table, Renderable> tickingRenderables = new ConcurrentHashMap<>();
    private static final HashSet<String> PAUSED_VANILLA_SOUNDS = new HashSet<>();

    private static int tickCounter = 0;
    public static boolean reloading = true;

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

    public static boolean handleSoundEventOverride(ISound sound) {
        if(!ConfigDebug.PLAY_NORMAL_MUSIC) return true;
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
            ServerChannels data = new ServerChannels();
            for (Channel channel : channelMap.values())
                channel.initializeServerData(data);
            NetworkHandler.sendToServer(new PacketInitChannels.Message(data));
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
                    if ((ConfigDebug.PAUSE_WHEN_TABBED && !ClientEvents.IS_DISPLAY_FOCUSED) || Minecraft.getMinecraft().isGamePaused())
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
        EntityPlayer player = Minecraft.getMinecraft().player;
        if(player!=null)
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
        if(Minecraft.getMinecraft().player!=null && !ConfigRegistry.CLIENT_SIDE_ONLY) {
            List<Channel> updatedChannels = new ArrayList<>();
            for(Channel channel : channelMap.values())
                if(channel.needsUpdatePacket()) updatedChannels.add(channel);
            if(!updatedChannels.isEmpty())
                NetworkHandler.sendToServer(new PacketDynamicChannelInfo.Message(updatedChannels));
        }
    }

    public static ChannelHolder createGuiData() {
        Map<String, ChannelInstance> channels = channelMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().createGuiData()));
        return new ChannelHolder(MusicTriggers.configFile("channels","toml"),channels);
    }
}
