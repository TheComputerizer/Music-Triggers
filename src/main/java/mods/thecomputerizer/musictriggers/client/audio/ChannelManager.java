package mods.thecomputerizer.musictriggers.client.audio;

import com.sedmelluq.discord.lavaplayer.remote.RemoteNodeProcessor;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config.*;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.musictriggers.util.packets.PacketQueryServerInfo;
import mods.thecomputerizer.theimpossiblelibrary.util.CustomTick;
import net.minecraft.block.BlockJukebox;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.FilenameUtils;
import org.lwjgl.opengl.Display;

import java.io.File;
import java.util.*;

public class ChannelManager {
    private static HttpInterfaceManager httpInterfaceManager;
    private static JukeboxChannel jukeboxChannel;
    private static final HashMap<String,Channel> channelMap = new HashMap<>();
    private static final HashMap<String, SoundHandler> handlerMap = new HashMap<>();
    private static final HashMap<String, Redirect> redirectMap = new HashMap<>();
    private static final HashMap<String, ConfigMain> mainConfigMap = new HashMap<>();
    private static final HashMap<String, ConfigTransitions> transitionsConfigMap = new HashMap<>();
    private static final HashMap<String, ConfigCommands> commandsConfigMap = new HashMap<>();
    private static final HashMap<String, ConfigToggles> toggleConfigMap = new HashMap<>();

    private static final List<String> songsInFolder = new ArrayList<>();
    public static final HashMap<String, File> openAudioFiles = new HashMap<>();
    private static final List<Channel> wasAlreadyPaused = new ArrayList<>();

    private static int tickCounter = 0;
    public static boolean reloading = false;

    public static void createChannel(String channel, String mainFileName, String transitionsFileName, String commandsFileName, String togglesFileName, String redirectFileName, boolean clientSide, boolean pausedByJukeBox, boolean overridesNormalMusic) {
        if(channel.matches("jukebox")) MusicTriggers.logger.error("Cannot name a channel jukebox!");
        else if(getChannel(channel)==null) {
            if(clientSide) {
                handlerMap.put(channel, new SoundHandler());
                channelMap.put(channel, new Channel(channel,pausedByJukeBox,overridesNormalMusic));
                redirectMap.put(channel, new Redirect(new File(MusicTriggers.configDir,redirectFileName+".txt")));
            }
            mainConfigMap.put(channel,new ConfigMain(new File(MusicTriggers.configDir,mainFileName+".toml"),channel));
            if(clientSide) {
                transitionsConfigMap.put(channel, new ConfigTransitions(new File(MusicTriggers.configDir, transitionsFileName + ".toml"),channelMap.get(channel)));
                commandsConfigMap.put(channel, new ConfigCommands(new File(MusicTriggers.configDir, commandsFileName + ".toml")));
                toggleConfigMap.put(channel, new ConfigToggles(new File(MusicTriggers.configDir, togglesFileName + ".toml"),channelMap.get(channel), handlerMap.get(channel)));
                channelMap.get(channel).passThroughConfigObjects(mainConfigMap.get(channel),transitionsConfigMap.get(channel),commandsConfigMap.get(channel),toggleConfigMap.get(channel),redirectMap.get(channel),handlerMap.get(channel));
            }
        }
        else MusicTriggers.logger.error("Channel already exists for category "+channel+"! Cannot assign 2 config files to the same music category.");
    }

    public static void createJukeboxChannel() {
        jukeboxChannel = new JukeboxChannel("jukebox");
    }

    public static void playCustomJukeboxSong(boolean start, String channel, String id, BlockPos pos) {
        if(!start) jukeboxChannel.stopTrack();
        else jukeboxChannel.playTrack(channelMap.get(channel).getCopyOfTrackFromID(id),pos);
    }

    public static void parseConfigFiles() {
        httpInterfaceManager = RemoteNodeProcessor.createHttpInterfaceManager();
        collectSongs();
        for(ConfigMain toml : mainConfigMap.values()) {
            toml.parse();
            handlerMap.get(toml.getChannel()).registerSounds(toml,toml.getChannel());
        }
        for(Channel channel : channelMap.values()) channel.parseRedirect(redirectMap.get(channel.getChannelName()));
        for(ConfigTransitions transitions: transitionsConfigMap.values()) transitions.parse();
        for(ConfigCommands commands : commandsConfigMap.values()) commands.parse();
        for(ConfigToggles toggles : toggleConfigMap.values()) toggles.parse();
        ConfigDebug.parse(new File(MusicTriggers.configDir,"debug.toml"));
        ConfigRegistry.parse(new File(MusicTriggers.configDir,"registration.toml"));
    }

    public static void readResourceLocations() {
        for(Channel channel : channelMap.values()) channel.readResourceLocations();
    }

    public static void collectSongs() {
        File folder = new File(MusicTriggers.configDir,"songs");
        if(!folder.exists()) {
            try {
                folder.createNewFile();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        File[] listOfFiles = folder.listFiles((dir, name) -> dir.canRead());
        if(listOfFiles!=null) {
            String curfile;
            songsInFolder.clear();
            openAudioFiles.clear();
            for (File f : listOfFiles) {
                curfile = FilenameUtils.getBaseName(f.getName());
                if (!songsInFolder.contains(curfile)) {
                    openAudioFiles.put(curfile, f);
                    songsInFolder.add(curfile);
                }
            }
        }
    }

    public static HttpInterface getInterface() {
        return httpInterfaceManager.getInterface();
    }

    public static Channel getChannel(String channel) {
        return channelMap.get(channel);
    }

    public static Collection<Channel> getAllChannels() {
        return channelMap.values();
    }

    public static boolean canAnyChannelOverrideMusic() {
        for(Channel channel : getAllChannels()) if(channel.overridesNormalMusic() && channel.isPlaying()) return true;
        return false;
    }

    public static Map<String, String> getSongHolder(Channel channel) {
        return mainConfigMap.get(channel.getChannelName()).songholder;
    }

    public static Map<String, Map<String, String[]>> getTriggerHolder(Channel channel) {
        return mainConfigMap.get(channel.getChannelName()).triggerholder;
    }

    public static Map<String, Map<String, String>> getTriggerMapper(Channel channel) {
        return mainConfigMap.get(channel.getChannelName()).triggerMapper;
    }

    public static Map<String, String[]> getOtherInfo(Channel channel) {
        return mainConfigMap.get(channel.getChannelName()).otherinfo;
    }

    public static Map<String, Map<String, String[]>> getOtherLinkingInfo(Channel channel) {
        return mainConfigMap.get(channel.getChannelName()).otherlinkinginfo;
    }

    public static Map<String, Map<String, String[]>> getTriggerLinking(Channel channel) {
        return mainConfigMap.get(channel.getChannelName()).triggerlinking;
    }

    public static Map<String, Map<Integer, String[]>> getLoopPoints(Channel channel) {
        return mainConfigMap.get(channel.getChannelName()).loopPoints;
    }

    public static Map<String, Map<String, Map<Integer, String[]>>> getLinkingLoopsPoints(Channel channel) {
        return mainConfigMap.get(channel.getChannelName()).linkingLoopPoints;
    }

    public static Map<Integer, ConfigTransitions.Title> getTitleCards(Channel channel) {
        return transitionsConfigMap.get(channel.getChannelName()).titlecards;
    }

    public static Map<Integer, ConfigTransitions.Image> getImageCards(Channel channel) {
        return transitionsConfigMap.get(channel.getChannelName()).imagecards;
    }

    public static Map<Integer, Boolean> getIsMoving(Channel channel) {
        return transitionsConfigMap.get(channel.getChannelName()).ismoving;
    }

    public static void syncInfoFromServer(ClientSync sync) {
        try {
            getChannel(sync.getChannel()).sync(sync);
        } catch (NullPointerException exception) {
            MusicTriggers.logger.error("Channel "+sync.getChannel()+" did not exist and could not be synced!");
        }
    }

    public static void pauseAllChannels(boolean fromJukebox) {
        for(Channel channel : channelMap.values()) {
            if(channel.isPaused()) wasAlreadyPaused.add(channel);
            channel.setPaused(true,fromJukebox);
        }
    }

    public static void unPauseAllChannels() {
        for(Channel channel : channelMap.values()) if(!wasAlreadyPaused.contains(channel) && channel.isPaused()) channel.setPaused(false,false);
        wasAlreadyPaused.clear();
    }

    public static void reloadAllChannels() {
        collectSongs();
        for(Channel channel : channelMap.values()) channel.reload();
        refreshDebug();
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void tickChannels(CustomTick event) {
        jukeboxChannel.checkStopPlaying(reloading);
        if(!reloading) {
            if(event.checkTickRate(20)) {
                tickCounter++;
                if (!Display.isActive()) pauseAllChannels(false);
                else if (!Minecraft.getMinecraft().isGamePaused()) {
                    if (checkForJukeBox()) {
                        pauseAllChannels(true);
                        for (Channel channel : channelMap.values()) if (!channel.isPaused()) channel.tickFast();
                        if (tickCounter % 4 == 0) {
                            for (Channel channel : channelMap.values()) if (!channel.isPaused()) channel.tickSlow();
                            sendUpdatePacket();
                        }
                    } else {
                        unPauseAllChannels();
                        for (Channel channel : channelMap.values()) if (!channel.isPaused()) channel.tickFast();
                        if (tickCounter % 4 == 0) {
                            for (Channel channel : channelMap.values()) channel.tickSlow();
                            sendUpdatePacket();
                        }
                    }
                }
                if(tickCounter>=100) tickCounter=5;
            }
        }
    }

    private static boolean checkForJukeBox() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if(player!=null) {
            for (int x = player.chunkCoordX - 3; x <= player.chunkCoordX + 3; x++) {
                for (int z = player.chunkCoordZ - 3; z <= player.chunkCoordZ + 3; z++) {
                    Map<BlockPos, TileEntity> currentChunkTE = player.getEntityWorld().getChunkFromChunkCoords(x, z).getTileEntityMap();
                    for (TileEntity te : currentChunkTE.values()) {
                        if (te instanceof BlockJukebox.TileEntityJukebox && te.getBlockMetadata() != 0) return true;
                    }
                }
            }
        }
        return false;
    }

    public static void refreshDebug() {
        ConfigDebug.parse(new File(MusicTriggers.configDir,"debug.toml"));
    }

    private static void sendUpdatePacket() {
        if(Minecraft.getMinecraft().player!=null) RegistryHandler.network.sendToServer(new PacketQueryServerInfo.PacketQueryServerInfoMessage(new ArrayList<>(channelMap.values())));
    }
}
