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
import mods.thecomputerizer.musictriggers.api.client.MTDebugInfo.Element;
import mods.thecomputerizer.musictriggers.api.config.ConfigVersionManager;
import mods.thecomputerizer.musictriggers.api.data.global.Debug;
import mods.thecomputerizer.musictriggers.api.data.global.GlobalData;
import mods.thecomputerizer.musictriggers.api.data.global.Toggle;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.network.MTNetwork;
import mods.thecomputerizer.musictriggers.api.network.MessageInitChannels;
import mods.thecomputerizer.musictriggers.api.network.MessageInitChannels.ChannelMessage;
import mods.thecomputerizer.musictriggers.api.network.MessageRequestChannels;
import mods.thecomputerizer.musictriggers.api.server.ChannelServer;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.MinecraftAPI;
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
    @Getter private static boolean loading = true;
    private static boolean resourcesLoaded;
    private static boolean reloadingClient;
    private static boolean reloadingConfig; //TODO Add a dedicated reload tracker

    public static @Nullable ChannelAPI findChannel(String playerID, boolean isClient, String channelName) {
        ChannelHelper helper = isClient ? getClientHelper(playerID) : getServerHelper(playerID);
        return Objects.nonNull(helper) ? helper.findChannel(globalData,channelName) : null;
    }
    
    public static void forEach(Consumer<ChannelHelper> func) {
        PLAYER_MAP.values().forEach(func);
    }

    public static int getTickRate() {
        Debug debug = getGlobalData().getDebug();
        return Objects.nonNull(debug) ? debug.getParameterAsInt("tick_rate") : 20;
    }
    
    public static ChannelHelper getHelper(String uuid, boolean client) {
        return client ? getClientHelper(uuid) : getServerHelper(uuid);
    }
    
    private static ChannelHelper getClientHelper(String uuid) {
        ChannelHelper helper = PLAYER_MAP.get("CLIENT");
        return uuid.equals(helper.getPlayerID()) ? helper : null;
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
    
    @SneakyThrows public static void initClient(MTDebugInfo debug) {
        MTRef.logInfo("Initializing client channel data");
        loadConfig("CLIENT",true);
        PLAYER_MAP.get("CLIENT").setDebugInfo(debug);
    }
    
    public static boolean isReloading() {
        return reloadingClient;
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
    }
    
    public static void loadMessage(MessageInitChannels<?> init) {
        ChannelHelper helper = globalData.loadFromInit(init);
        PLAYER_MAP.put(init.getUuid(),helper);
        helper.setSyncable(true);
    }

    public static void onClientConnected() {

    }

    public static void onClientDisconnected() {

    }

    public static void onReloadQueued() {
        loading = true;
        for(Entry<String,ChannelHelper> entry : PLAYER_MAP.entrySet()) {
            String playerID = entry.getKey();
            ChannelHelper helper = entry.getValue();
            if(StringUtils.isNotBlank(playerID) && Objects.nonNull(helper)) {
                reloadingClient = helper.client;
                reloadingConfig = helper.fromConfig;
                helper.close();
            }
        }
        PLAYER_MAP.clear();
    }

    public static void onResourcesLoaded() {
        resourcesLoaded = true;
        for(ChannelHelper helper : PLAYER_MAP.values())
            if(helper.client)
                for(ChannelAPI channel : helper.channels.values())
                    channel.onResourcesLoaded();
    }

    public static void reload() {
        if(reloadingConfig) {
            try {
                if(reloadingClient) { {
                    loadConfig("CLIENT",true);
                    MTNetwork.sendToServer(PLAYER_MAP.get("CLIENT").getInitMessage(),false);
                }
                } else //TODO
                    for(PlayerAPI<?,?> player : getPlayers(false))
                        loadConfig(player.getUUID().toString(),false);
            } catch(TomlWritingException ex) {
                MTRef.logFatal("Failed to reload config files!",ex);
            }
        } else requestChannelInformation(!reloadingClient);
    }
    
    public static void requestChannelInformation(boolean toClient) {
        List<PlayerAPI<?,?>> players = getPlayers(!toClient);
        if(players.isEmpty()) MTRef.logError("Unable to get list of players for the {} side!",toClient ? "server" : "client");
        for(PlayerAPI<?,?> player : players) {
            MessageRequestChannels<?> msg = new MessageRequestChannels<>(player.getUUID().toString(),!toClient);
            if(toClient) MTNetwork.sendToClient(msg,false,player);
            else MTNetwork.sendToServer(msg,false);
        }
    }

    public static boolean resourcesLoaded() {
        return resourcesLoaded;
    }

    public static void tick(@Nullable CustomTick ticker) {
        if(!loading && Objects.nonNull(ticker) && ticker.isEquivalentTPS(getTickRate()))
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
    @Getter private boolean syncable;
    @Getter private MTDebugInfo debugInfo;
    private final String playerID;
    private boolean fromConfig;

    public ChannelHelper(String playerID, boolean client) {
        this.channels = new HashMap<>();
        this.toggles = new HashSet<>();
        this.client = client;
        this.playerID = playerID;
    }

    public void addDebugElements(MTDebugInfo info, Collection<Element> elements) {
        for(ChannelAPI channel : this.channels.values())
            if(channel instanceof ChannelClient)
                ((ChannelClient)channel).addDebugElements(info,elements);
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

    public @Nullable Debug getDebug() {
        return globalData.getDebug();
    }

    public boolean getDebugBool(String name) {
        Debug debug = getDebug();
        return Objects.nonNull(debug) && debug.getParameterAsBoolean(name);
    }

    @SuppressWarnings("unchecked")
    public List<String> getDebugList(String name) {
        Debug debug = getDebug();
        return Objects.nonNull(debug) ? (List<String>)debug.getParameterAsList(name) : Collections.emptyList();
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
    
    public @Nullable PlayerAPI<?,?> getPlayer() { //TODO Store player reference in this class?
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
        if(this.client) globalData.logInfo("Attempting to load stored audio references");
        for(ChannelAPI channel : this.channels.values()) channel.loadTracks(this.client && resourcesLoaded);
        this.fromConfig = false;
        loading = false;
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
        if(this.client) globalData.logInfo("Attempting to load stored audio references");
        for(ChannelAPI channel : this.channels.values()) channel.loadTracks(this.client && resourcesLoaded);
        this.fromConfig = true;
        loading = false;
    }
    
    private void parseToggles(Toml toggles) {
        if(Objects.nonNull(toggles) && toggles.hasTable("toggle"))
            for(Toml table : toggles.getTableArray("toggle"))
                this.toggles.add(new Toggle(this,table));
    }
    
    public void setDebugInfo(MTDebugInfo debugInfo) {
        this.debugInfo = debugInfo;
        debugInfo.setHelper(this);
    }
    
    public void setSyncable(boolean sync) {
        if(!this.syncable && sync)
            for(ChannelAPI channel : this.channels.values())
                channel.getSelector().getContext().initSync();
        this.syncable = sync;
    }

    public void syncChannels() {
        for(ChannelAPI channel : this.channels.values()) channel.sync();
    }

    public void tickChannels() {
        for(ChannelAPI channel : this.channels.values()) channel.tick();
    }

    private void writeExampleChannel(Toml toml) throws TomlWritingException {
        Toml table = toml.addTable("channels",false);
        ChannelInfo.writeExampleData(table.addTable("example",false));
    }
}