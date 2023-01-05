package mods.thecomputerizer.musictriggers.client.audio;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.config.ConfigChannels;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.packets.PacketQueryServerInfo;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.*;

public class ChannelManager {
    private static JukeboxChannel jukeboxChannel;
    private static final HashMap<String, ConfigChannels.ChannelInfo> channelBlueprints = new HashMap<>();
    private static final HashMap<String,Channel> channelMap = new HashMap<>();
    public static final HashMap<String, File[]> openAudioFiles = new HashMap<>();

    private static int tickCounter = 0;
    public static boolean reloading = true;

    public static void createChannel(ConfigChannels.ChannelInfo blueprint) {
        if(verifyChannelParameters(blueprint.getChannelName(), blueprint.getMain(), blueprint.getTransitions(),
                blueprint.getCommands(), blueprint.getToggles(), blueprint.getRedirect(), blueprint.getJukebox())) {
            channelMap.put(blueprint.getChannelName(), new Channel(blueprint.getChannelName(), blueprint.getSoundCategory(),
                    blueprint.getPausedByJukeBox(), blueprint.getOverridesNormalMusic(), blueprint.getMain(),
                    blueprint.getTransitions(), blueprint.getCommands(), blueprint.getToggles(), blueprint.getRedirect(),
                    blueprint.getJukebox(), blueprint.getSongsFolder()));
            channelBlueprints.put(blueprint.getChannelName(),blueprint);
        } else MusicTriggers.logExternally(Level.ERROR, "Channel {} failed to register! See the above errors for" +
                "more information.",blueprint.getChannelName());
    }

    private static boolean verifyChannelParameters(String channelName, String mainFileName, String transitionsFileName,
                                                   String commandsFileName, String togglesFileName, String redirectFileName,
                                                   String jukeboxFileName) {
        if(channelName.matches("preview"))
            MusicTriggers.logExternally(Level.ERROR, "Channel name cannot be set to \"jukebox\" or \"preview\"" +
                    "as those are used for internal functions!");
        else if(Objects.nonNull(channelMap.get(channelName)))
            MusicTriggers.logExternally(Level.ERROR, "Channel with name "+channelName+ " already exists" +
                    "! Different channels must have unique names!");
        boolean verifiedFile = verifyFilePath(mainFileName,"main");
        if(!verifiedFile) return false;
        verifiedFile = verifyFilePath(transitionsFileName,"transitions");
        if(!verifiedFile) return false;
        verifiedFile = verifyFilePath(commandsFileName,"commands");
        if(!verifiedFile) return false;
        verifiedFile = verifyFilePath(togglesFileName,"toggles");
        if(!verifiedFile) return false;
        verifiedFile = verifyFilePath(redirectFileName,"redirect");
        if(!verifiedFile) return false;
        return verifyFilePath(jukeboxFileName,"jukebox");
    }

    private static boolean verifyFilePath(String filePath, String configType) {
        if(filePath.endsWith("\\.toml") || filePath.endsWith("\\.txt")) {
            MusicTriggers.logExternally(Level.ERROR, "Please do not include the " +
                    "file extension in your file path. That is handled internally.");
            return false;
        }
        if(filePath.matches("debug") || filePath.matches("registration") || filePath.matches("channels")
                || filePath.matches("preview")) {
            MusicTriggers.logExternally(Level.ERROR, "Config type {} cannot be {} as that is the name of a " +
                    "preset config file!",configType,filePath);
            return false;
        }
        for(ConfigChannels.ChannelInfo registeredBlueprint : channelBlueprints.values()) {
            if(!registeredBlueprint.verifyOtherFilePathIsValid(filePath, configType))
                return false;
        }
        return true;
    }

    public static void createJukeboxChannel() {
        jukeboxChannel = new JukeboxChannel("jukebox");
    }

    public static void playCustomJukeboxSong(boolean start, String channel, String id, BlockPos pos) {
        if(!start) jukeboxChannel.stopTrack();
        else jukeboxChannel.playTrack(channelMap.get(channel).getCopyOfTrackFromID(id),pos);
    }

    public static void parseConfigFiles() {
        collectSongs();
        for(Channel channel : channelMap.values()) channel.parseConfigs(true);
        ConfigDebug.initialize(new File(Constants.CONFIG_DIR,"debug.toml"));
        ConfigRegistry.initialize(new File(Constants.CONFIG_DIR,"registration.toml"));
    }

    public static void readResourceLocations() {
        for(Channel channel : channelMap.values()) channel.readResourceLocations();
    }

    public static void collectSongs() {
        openAudioFiles.clear();
        for(Channel channel : channelMap.values()) {
            File folder = new File(channel.getLocalFolder());
            FileUtil.generateNestedFile(folder, false);
            File[] listOfFiles = folder.listFiles((dir, name) -> dir.canRead());
            if (listOfFiles != null)
                openAudioFiles.putIfAbsent(channel.getLocalFolder(),listOfFiles);
        }
    }

    public static Channel getChannel(String channel) {
        return channelMap.get(channel);
    }

    public static boolean channelExists(String channel) {
        return channelMap.containsKey(channel);
    }

    public static Collection<Channel> getAllChannels() {
        return channelMap.values();
    }

    public static List<String> getChannelNames() {
        return new ArrayList<>(channelMap.keySet());
    }

    public static boolean overridingMusicIsPlaying() {
        for(Channel channel : getAllChannels()) if(channel.overridesNormalMusic() && channel.isPlaying()) return true;
        return false;
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
        Trigger.clearInitialized();
        collectSongs();
        for(Channel channel : channelMap.values()) channel.reload();
        refreshDebug();
    }

    public static void tickChannels() {
        jukeboxChannel.checkStopPlaying(reloading);
        if(!reloading) {
                tickCounter++;
                if (checkForJukeBox()) jukeboxPause();
                else jukeboxUnpause();
                if (!Minecraft.getInstance().isWindowActive() || Minecraft.getInstance().isPaused()) pauseAllChannels();
                else unpauseAllChannels();
                for (Channel channel : channelMap.values())
                    if (!channel.isPaused()) channel.tickFast();
                if (tickCounter % 4 == 0) {
                    for (Channel channel : channelMap.values()) channel.tickSlow();
                    sendUpdatePacket();
                }
                if(tickCounter>=100) tickCounter=5;
        }
    }

    private static boolean checkForJukeBox() {
        Player player = Minecraft.getInstance().player;
        if(player!=null) {
            for (int x = player.chunkPosition().x - 3; x <= player.chunkPosition().x + 3; x++) {
                for (int z = player.chunkPosition().z - 3; z <= player.chunkPosition().z + 3; z++) {
                    Set<BlockPos> currentChunkTEPos = player.level.getChunk(x, z).getBlockEntitiesPos();
                    for (BlockPos b : currentChunkTEPos)
                        return player.level.getChunk(x, z).getBlockEntity(b) instanceof JukeboxBlockEntity te &&
                                te.getBlockState().getValue(JukeboxBlock.HAS_RECORD);
                }
            }
        }
        return false;
    }

    public static void refreshDebug() {
        ConfigDebug.read();
    }

    private static void sendUpdatePacket() {
        if(Minecraft.getInstance().player!=null)
            PacketHandler.sendToServer(new PacketQueryServerInfo(new ArrayList<>(channelMap.values())));
    }
}
