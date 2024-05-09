package mods.thecomputerizer.musictriggers.api.data.channel;

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
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import lombok.Getter;
import lombok.SneakyThrows;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.ChannelClient;
import mods.thecomputerizer.musictriggers.api.client.MTDebugInfo;
import mods.thecomputerizer.musictriggers.api.config.ConfigVersionManager;
import mods.thecomputerizer.musictriggers.api.data.global.Debug;
import mods.thecomputerizer.musictriggers.api.data.global.GlobalData;
import mods.thecomputerizer.musictriggers.api.data.global.Toggle;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;
import mods.thecomputerizer.musictriggers.api.network.MTNetwork;
import mods.thecomputerizer.musictriggers.api.network.MessageInitChannels;
import mods.thecomputerizer.musictriggers.api.network.MessageInitChannels.ChannelMessage;
import mods.thecomputerizer.musictriggers.api.server.ChannelServer;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.MinecraftAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.sound.SoundHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.PlayerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.TILRef;
import mods.thecomputerizer.theimpossiblelibrary.api.io.FileHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.server.MinecraftServerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.server.ServerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlParsingException;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlWritingException;
import mods.thecomputerizer.theimpossiblelibrary.api.util.CustomTick;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ChannelHelper {

    private static final Map<String,ChannelHelper> PLAYER_MAP = new HashMap<>();
    @Getter private static final GlobalData globalData = new GlobalData();
    @Getter private static final LoadTracker loader = new LoadTracker();
    
    public static void closePlayerChannel(String playerID) {
        ChannelHelper helper = PLAYER_MAP.get(playerID);
        if(Objects.nonNull(helper)) {
            helper.close();
            PLAYER_MAP.remove(playerID);
        }
    }

    public static @Nullable ChannelAPI findChannel(String playerID, boolean isClient, String channelName) {
        ChannelHelper helper = isClient ? getClientHelper(playerID) : getServerHelper(playerID);
        return Objects.nonNull(helper) ? helper.findChannel(globalData,channelName) : null;
    }
    
    public static void flipDebugParameter(boolean client, String name) {
        if(client) {
            ChannelHelper helper = getClientHelper();
            if(Objects.nonNull(helper)) helper.flipDebugParameter(name);
        } else for(ChannelHelper helper : PLAYER_MAP.values()) helper.flipDebugParameter(name);
    }

    public static int getTickRate() {
        Debug debug = getGlobalData().getDebug();
        return Objects.nonNull(debug) ? debug.getParameterAsInt("tick_rate") : 20;
    }
    
    public static ChannelHelper getHelper(String uuid, boolean client) {
        return client ? getClientHelper(uuid) : getServerHelper(uuid);
    }
    
    public static ChannelHelper getClientHelper() {
        return PLAYER_MAP.get("CLIENT");
    }
    
    private static ChannelHelper getClientHelper(String uuid) {
        ChannelHelper helper = PLAYER_MAP.get("CLIENT");
        return Objects.nonNull(helper) && uuid.equals(String.valueOf(helper.getPlayerID())) ? helper : null;
    }
    
    @SneakyThrows
    private static ChannelHelper getServerHelper(String uuid) {
        if(!PLAYER_MAP.containsKey(uuid)) PLAYER_MAP.put(uuid,new ChannelHelper(uuid,false));
        return PLAYER_MAP.get(uuid);
    }
    
    public static List<PlayerAPI<?,?>> getPlayers(boolean client) {
        if(client) {
            MinecraftAPI mc = TILRef.getClientSubAPI(ClientAPI::getMinecraft);
            if(Objects.nonNull(mc)) {
                PlayerAPI<?,?> player = mc.getPlayer();
                if(Objects.nonNull(player)) return Collections.singletonList(player);
            }
        } else {
            MinecraftServerAPI<?> server = ServerHelper.getAPI();
            if(Objects.nonNull(server)) return server.getPlayers();
        }
        return new ArrayList<>();
    }
    
    @SneakyThrows public static void initClient() {
        MTRef.logInfo("Initializing client channel data");
        loadConfig("CLIENT",true);
    }
    
    public static void loadConfig(String playerID, boolean client) throws TomlWritingException {
        try {
            ConfigVersionManager.queryRemap();
            globalData.parse(openToml(MTRef.GLOBAL_CONFIG,true,globalData));
        } catch(Exception ex) {
            throw new RuntimeException("Error parsing global data!",ex);
        }
        ChannelHelper helper = new ChannelHelper(playerID,client);
        helper.loadFromFile(globalData.getGlobal());
        PLAYER_MAP.put(playerID,helper);
        loader.setClient(client);
        loader.setLoading(false);
        if(loader.isConnected() || !client) {
            if(client) MTNetwork.sendToServer(helper.getInitMessage(),false);
            else MTNetwork.sendToClient(helper.getInitMessage(),playerID);
        }
    }
    
    public static void loadMessage(MessageInitChannels<?> init) {
        ChannelHelper helper = globalData.loadFromInit(init);
        PLAYER_MAP.put(init.getUuid(),helper);
        helper.setSyncable(true);
        loader.setLoading(false);
    }

    public static void onClientConnected() {
        loader.setConnected(true);
    }

    public static void onClientDisconnected() {
        loader.setConnected(false);
        ChannelHelper helper = getClientHelper();
        if(Objects.nonNull(helper)) helper.setSyncable(false);
    }

    public static void onReloadQueued(boolean client) {
        if(loader.isLoading()) return;
        loader.setLoading(true);
        loader.setClient(client);
        globalData.logInfo("Queued reload on the {} side",loader.isClient() ? "client" : "server");
        MTLogger.onReloadQueued();
        for(Entry<String,ChannelHelper> entry : PLAYER_MAP.entrySet()) {
            String playerID = entry.getKey();
            ChannelHelper helper = entry.getValue();
            if(StringUtils.isNotBlank(playerID) && Objects.nonNull(helper)) helper.close();
        }
        PLAYER_MAP.clear();
    }

    public static void onResourcesLoaded() {
        for(ChannelHelper helper : PLAYER_MAP.values())
            if(helper.client) {
                loader.setResourcesLoaded(true);
                helper.forEachChannel(ChannelAPI::onResourcesLoaded);
            }
    }

    public static void reload() {
        try {
            if(loader.isClient()) { {
                loadConfig("CLIENT",true);
                MTNetwork.sendToServer(PLAYER_MAP.get("CLIENT").getInitMessage(),false);
            }
            } else
                for(PlayerAPI<?,?> player : getPlayers(false))
                    loadConfig(player.getUUID().toString(),false);
        } catch(TomlWritingException ex) {
            MTRef.logFatal("Failed to reload config files!",ex);
        }
    }

    public static void tick(@Nullable CustomTick ticker) {
        if(!loader.isLoading() && Objects.nonNull(ticker) && ticker.isEquivalentTPS(getTickRate()))
            for(ChannelHelper helper : PLAYER_MAP.values()) helper.tickChannels();
    }

    /**
     * Assumes the file extension is not present
     */
    public static @Nullable Toml openToml(String path, boolean writeDefaults, LoggableAPI logger) {
        String tomlPath = path+".toml";
        try {
            File file = FileHelper.get(tomlPath,false);
            Toml toml = Toml.readFile(file);
            String name = file.getName().substring(0,file.getName().length()-5);
            if(Objects.nonNull(toml) && writeDefaults)
                ConfigVersionManager.writeDefaults(toml,name,path);
            return toml;
        } catch(IOException|TomlParsingException ex) {
            String msg = "Unable to read toml file at `{}`!";
            if(Objects.nonNull(logger)) logger.logError(msg,tomlPath,ex);
            else MTRef.logError(msg,tomlPath,ex);
            return null;
        }
    }

    /**
     * Assumes the file extension is not present
     */
    public static List<String> openTxt(String path, @Nullable LoggableAPI logger) {
        path+=".txt";
        File file = FileHelper.get(path,false);
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().collect(Collectors.toList());
        } catch(IOException ex) {
            String msg = "Unable to read txt file at `{}`!";
            if(Objects.nonNull(logger)) logger.logError(msg,ex);
            else MTRef.logError(msg,ex);
            return Collections.emptyList();
        }
    }

    public static void registerRemoteSources(ChannelAPI channel, AudioPlayerManager manager) {
        registerRemoteSource(channel,manager,"YouTube",YoutubeAudioSourceManager::new);
        registerRemoteSource(channel,manager,"SoundCloud",SoundCloudAudioSourceManager::createDefault);
        registerRemoteSource(channel,manager,"BandCamp",BandcampAudioSourceManager::new);
        registerRemoteSource(channel,manager,"Vimeo",VimeoAudioSourceManager::new);
        registerRemoteSource(channel,manager,"Twitch",TwitchStreamAudioSourceManager::new);
        registerRemoteSource(channel,manager,"Beam",BeamAudioSourceManager::new);
        registerRemoteSource(channel,manager,"Getyarn",GetyarnAudioSourceManager::new);
        registerRemoteSource(channel,manager,"HTTPAudio",() -> new HttpAudioSourceManager(
                MediaContainerRegistry.DEFAULT_REGISTRY));
    }

    private static void registerRemoteSource(
            ChannelAPI channel, AudioPlayerManager manager, String sourceName, Supplier<AudioSourceManager> supplier) {
        try {
            manager.registerSourceManager(supplier.get());
        } catch(Exception ex) {
            channel.logError("Failed to register remote source for `{}`!",sourceName,ex);
        }
    }

    @Getter private final Map<String,ChannelAPI> channels;
    @Getter private final Set<Toggle> toggles;
    @Getter private final boolean client;
    @Getter private final MTDebugInfo debugInfo;
    @Getter private boolean syncable;
    private final String playerID;

    public ChannelHelper(String playerID, boolean client) {
        this.channels = new HashMap<>();
        this.toggles = new HashSet<>();
        this.client = client;
        this.playerID = playerID;
        this.debugInfo = client ? new MTDebugInfo(this) : null; //Don't initialize the debug info on the server
        loader.setClient(client);
    }
    
    public boolean canVanillaMusicPlay() {
        for(ChannelAPI channel : this.channels.values())
            if(channel.shouldBlockMusicTicker()) return false;
        return true;
    }

    public void close() {
        for(ChannelAPI channel : this.channels.values()) channel.close();
        this.channels.clear();
        for(Toggle toggle : this.toggles) toggle.close();
        this.toggles.clear();
    }

    public @Nullable ChannelAPI findChannel(LoggableAPI logger, String channelName) {
        ChannelAPI channel = channels.get(channelName);
        if(Objects.isNull(channel)) logger.logError("Unable to find channel with name `{}`!",channelName);
        return channel;
    }
    
    public void flipDebugParameter(String name) {
        Debug debug = getDebug();
        if(Objects.nonNull(debug)) debug.flipBooleanParameter(name);
    }
    
    public void forEachChannel(Consumer<ChannelAPI> consumer) {
        this.channels.values().forEach(consumer);
    }

    public @Nullable Debug getDebug() {
        return globalData.getDebug();
    }

    public boolean getDebugBool(String name) {
        Debug debug = getDebug();
        return Objects.nonNull(debug) && debug.getParameterAsBoolean(name);
    }

    public Number getDebugNumber(String name) {
        Debug debug = getDebug();
        return Objects.nonNull(debug) ? debug.getParameterAsNumber(name) : 0;
    }

    public String getDebugString(String name) {
        Debug debug = getDebug();
        if(Objects.isNull(debug)) return "";
        String ret = debug.getParameterAsString(name);
        return Objects.nonNull(ret) ? ret : "";
    }
    
    public MessageInitChannels<?> getInitMessage() {
        Toml toggles = globalData.openToggles(MTRef.CONFIG_PATH);
        return new MessageInitChannels<>(globalData.getGlobal(),toggles,this);
    }
    
    public @Nullable PlayerAPI<?,?> getPlayer() {
        for(ChannelAPI channel : this.channels.values()) return channel.getPlayerEntity();
        return null;
    }

    public String getPlayerID() {
        if(!this.client) return this.playerID;
        else {
            MinecraftAPI mc = TILRef.getClientSubAPI(ClientAPI::getMinecraft);
            if(Objects.nonNull(mc)) {
                PlayerAPI<?,?> player = mc.getPlayer();
                return Objects.nonNull(player) ? player.getUUID().toString() : null;
            }
        }
        PlayerAPI<?,?> player = getPlayer();
        return Objects.nonNull(player) ? player.getUUID().toString() : null;
    }

    public void loadFromFile(@Nullable Toml globalHolder) throws TomlWritingException {
        if(Objects.isNull(globalHolder)) {
            globalData.logFatal("Cannot initialize channel or toggle data from missing global config!");
            return;
        }
        initChannels(globalHolder);
        initToggles();
        parseData();
    }
    
    public void loadFromInit(MessageInitChannels<?> init) {
        for(Entry<String,ChannelMessage> entry : init.getChannels().entrySet()) {
            String name = entry.getKey();
            loadChannel(name,globalData.getGlobal().getTable("channels").getTable(name),entry.getValue());
        }
        this.toggles.removeIf(toggle -> !toggle.parse());
        globalData.logInfo("Finished loading external channel data");
        if(this.client) {
            globalData.logInfo("Attempting to load stored audio references");
            this.debugInfo.initChannelElements();
        }
        forEachChannel(channel -> channel.loadTracks(this.client && loader.areResourcesLoaded()));
    }

    private void initChannel(String name, Toml info) {
        if(this.channels.containsKey(name)) globalData.logError("Channel with name `{}` already exists!");
        else {
            ChannelAPI channel = this.client ? new ChannelClient(this,info) : new ChannelServer(this,info);
            if(channel.isValid()) this.channels.put(name,channel);
            else globalData.logError("Channel with name `{}` is invalid!");
        }
    }

    private void initChannels(Toml toml) throws TomlWritingException {
        if(!toml.hasTable("channels")) writeExampleChannel(toml);
        Toml table = toml.getTable("channels");
        for(Toml info : table.getAllTables()) {
            if(Objects.nonNull(info)) initChannel(info.getName(),info);
            else globalData.logError("Channel `{}` does not have an info table! This should not be possible.");
        }
    }

    private void initToggles() {
        parseToggles(globalData.openToggles(MTRef.CONFIG_PATH));
    }
    
    private void loadChannel(String name, Toml info, ChannelMessage message) {
        ChannelAPI channel = this.client ? new ChannelClient(this,info) : new ChannelServer(this,info);
        channel.loadData(message);
        this.channels.put(name,channel);
    }

    public void parseData() {
        for(ChannelAPI channel : this.channels.values()) channel.parseData();
        this.toggles.removeIf(toggle -> !toggle.parse());
        globalData.logInfo("Finished parsing channel data");
        if(this.client) {
            globalData.logInfo("Attempting to load stored audio references");
            this.debugInfo.initChannelElements();
        }
        forEachChannel(channel -> {
            channel.loadTracks(this.client && loader.areResourcesLoaded());
            if(this.client) {
                channel.setMasterVolume(SoundHelper.getCategoryVolume("master"));
                String category = channel.getInfo().getCategory();
                if(category.equalsIgnoreCase("master")) channel.setCategoryVolume(1f);
                else channel.setCategoryVolume(SoundHelper.getCategoryVolume(category));
            }
        });
    }
    
    private void parseToggles(Toml toggles) {
        if(Objects.nonNull(toggles) && toggles.hasTable("toggle"))
            for(Toml table : toggles.getTableArray("toggle"))
                this.toggles.add(new Toggle(this,table));
    }
    
    public void setCategoryVolume(String category, float volume) {
        forEachChannel(channel -> {
            if(category.equals("master")) channel.setMasterVolume(volume);
            else if(category.equals(channel.getInfo().getCategory())) channel.setCategoryVolume(volume);
        });
    }
    
    public void setSyncable(boolean sync) {
        forEachChannel(channel -> {
            TriggerContext context = channel.getSelector().getContext();
            if(!this.syncable && sync) context.initSync();
            else if(this.syncable && !sync) context.clearSync();
        });
        this.syncable = sync;
    }

    public void tickChannels() {
        this.channels.values().forEach(ChannelAPI::tick);
    }

    private void writeExampleChannel(Toml toml) throws TomlWritingException {
        Toml table = toml.addTable("channels",false);
        ChannelInfo.writeExampleData(table.addTable("example",false));
    }
}