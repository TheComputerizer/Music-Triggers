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
import mods.thecomputerizer.musictriggers.api.client.MTClient;
import mods.thecomputerizer.musictriggers.api.client.channel.ChannelClient;
import mods.thecomputerizer.musictriggers.api.client.MTDebugInfo;
import mods.thecomputerizer.musictriggers.api.client.channel.ChannelJukebox;
import mods.thecomputerizer.musictriggers.api.client.channel.ChannelPreview;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.WrapperLink;
import mods.thecomputerizer.musictriggers.api.config.ConfigVersionManager;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioPool;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.global.Debug;
import mods.thecomputerizer.musictriggers.api.data.global.GlobalData;
import mods.thecomputerizer.musictriggers.api.data.global.Toggle;
import mods.thecomputerizer.musictriggers.api.data.jukebox.RecordElement;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import mods.thecomputerizer.musictriggers.api.data.nbt.NBTLoadable;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;
import mods.thecomputerizer.musictriggers.api.network.MTNetwork;
import mods.thecomputerizer.musictriggers.api.network.MessageFinishedInit;
import mods.thecomputerizer.musictriggers.api.network.MessageInitChannels;
import mods.thecomputerizer.musictriggers.api.network.MessageInitChannels.ChannelMessage;
import mods.thecomputerizer.musictriggers.api.network.MessageTriggerStates;
import mods.thecomputerizer.musictriggers.api.server.ChannelServer;
import mods.thecomputerizer.shadow.org.joml.Vector3d;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.MinecraftAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.sound.SoundHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.blockentity.BlockEntityAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.PlayerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.item.ItemStackAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.TILRef;
import mods.thecomputerizer.theimpossiblelibrary.api.io.FileHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.server.MinecraftServerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.server.ServerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.Box;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.ShapeHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.TagHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlParsingException;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlWritingException;
import mods.thecomputerizer.theimpossiblelibrary.api.util.CustomTick;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Misc;
import mods.thecomputerizer.theimpossiblelibrary.api.util.RandomHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.world.BlockPosAPI;
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

import static mods.thecomputerizer.musictriggers.api.MTRef.GLOBAL_CONFIG;

public class ChannelHelper implements NBTLoadable {

    private static final Map<String,ChannelHelper> PLAYER_MAP = new HashMap<>();
    @Getter private static final GlobalData globalData = new GlobalData();
    @Getter private static final LoadTracker loader = new LoadTracker();
    
    public static void closePlayerChannel(String playerID) {
        ChannelHelper helper = PLAYER_MAP.get(playerID);
        if(Objects.nonNull(helper)) {
            helper.close();
            PLAYER_MAP.remove(playerID);
        }
        globalData.close();
    }
    
    public static void flipDebugParameter(boolean client, String name) {
        if(client) {
            ChannelHelper helper = getClientHelper();
            if(Objects.nonNull(helper)) helper.flipDebugParameter(name);
        } else for(ChannelHelper helper : PLAYER_MAP.values())
            if(!helper.client) helper.flipDebugParameter(name);
    }
    
    public static Debug getDebug() {
        return globalData.getDebug();
    }
    
    public static boolean getDebugBool(String name) {
        Debug debug = getDebug();
        return Objects.nonNull(debug) && debug.getParameterAsBoolean(name);
    }
    
    public static Number getDebugNumber(String name) {
        Debug debug = getDebug();
        return Objects.nonNull(debug) ? debug.getParameterAsNumber(name) : 0;
    }
    
    public static String getDebugString(String name) {
        Debug debug = getDebug();
        if(Objects.isNull(debug)) return "";
        String ret = debug.getParameterAsString(name);
        return Objects.nonNull(ret) ? ret : "";
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
        if(!PLAYER_MAP.containsKey(uuid)) PLAYER_MAP.put(uuid, new ChannelHelper(uuid, false));
        return PLAYER_MAP.get(uuid);
    }
    
    public static List<? extends PlayerAPI<?,?>> getPlayers(boolean client) {
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
        logGlobalInfo("Initializing client channel data");
        loadConfig("CLIENT",true);
    }
    
    public static void loadConfig(String playerID, boolean client) throws TomlWritingException {
        try {
            ConfigVersionManager.queryRemap();
            globalData.parse(openToml(GLOBAL_CONFIG,true,globalData));
        } catch(Exception ex) {
            throw new RuntimeException("Error parsing global data!",ex);
        }
        ChannelHelper helper = new ChannelHelper(playerID,client);
        helper.loadFromFile(globalData.getGlobal());
        PLAYER_MAP.put(playerID, helper);
        loader.setClient(client);
        loader.setLoading(false);
        if(loader.isConnected() || !client) {
            if(client) MTNetwork.sendToServer(helper.getInitMessage(),false);
            else MTNetwork.sendToClient(helper.getInitMessage(),playerID);
        }
    }
    
    public static MessageFinishedInit<?> loadMessage(MessageInitChannels<?> init) {
        ChannelHelper helper = globalData.loadFromInit(init);
        PLAYER_MAP.put(init.getUuid(),helper);
        helper.setSyncable(true);
        loader.setLoading(false);
        return new MessageFinishedInit<>(helper);
    }
    
    public static void logGlobalDebug(String msg, Object ... args) {
        globalData.logDebug(msg,args);
    }
    
    public static void logGlobalError(String msg, Object ... args) {
        globalData.logError(msg,args);
    }
    
    public static void logGlobalFatal(String msg, Object ... args) {
        globalData.logFatal(msg,args);
    }
    
    public static void logGlobalInfo(String msg, Object ... args) {
        globalData.logInfo(msg,args);
    }
    
    public static void logGlobalTrace(String msg, Object ... args) {
        globalData.logTrace(msg,args);
    }
    
    public static void logGlobalWarn(String msg, Object ... args) {
        globalData.logWarn(msg,args);
    }

    public static void onClientConnected() {
        loader.setConnected(true);
        ChannelHelper helper = getClientHelper();
        if(Objects.nonNull(helper)) {
            for(ChannelAPI channel : helper.channels.values()) {
                //TODO persistent data
            }
        }
    }

    public static void onClientDisconnected() {
        loader.setConnected(false);
        ChannelHelper helper = getClientHelper();
        if(Objects.nonNull(helper)) {
            helper.channels.values().forEach(
                    channel -> channel.getData().getTriggerEventMap().keySet().forEach(TriggerAPI::onDisconnected));
            helper.setSyncable(false);
        }
    }

    public static void onReloadQueued(boolean client) {
        loader.setLoading(true);
        loader.setClient(client);
        globalData.logInfo("Queued reload on the {} side",loader.isClient() ? "client" : "server");
        synchronized(PLAYER_MAP) {
            for(ChannelHelper helper : PLAYER_MAP.values()) helper.close();
            PLAYER_MAP.clear();
        }
        MTLogger.onReloadQueued();
    }

    public static void onResourcesLoaded() {
        for(ChannelHelper helper : PLAYER_MAP.values()) {
            if(helper.client) {
                loader.setResourcesLoaded(true);
                helper.forEachChannel(ChannelAPI::onResourcesLoaded);
            }
        }
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
            else logGlobalError(msg,tomlPath,ex);
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
            else logGlobalError(msg,ex);
            return Collections.emptyList();
        }
    }
    
    public static void reload() {
        try {
            if(loader.isClient()) {
                loadConfig("CLIENT",true);
                MTNetwork.sendToServer(PLAYER_MAP.get("CLIENT").getInitMessage(),false);
            } else
                for(PlayerAPI<?,?> player : getPlayers(false))
                    loadConfig(player.getUUID().toString(),false);
        } catch(TomlWritingException ex) {
            logGlobalFatal("Failed to reload config files!",ex);
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
    
    public static void setDebugParameter(boolean client, String name, Object value) {
        if(client) {
            ChannelHelper helper = getClientHelper();
            if(Objects.nonNull(helper)) helper.setDebugParameter(name,value);
        } else for(ChannelHelper helper : PLAYER_MAP.values())
            if(!helper.client) helper.setDebugParameter(name,value);
    }
    
    public static void tick(@Nullable CustomTick ticker) {
        if(!loader.isLoading() && Objects.nonNull(ticker) && ticker.isEquivalentTPS(getTickRate()))
            for(ChannelHelper helper : PLAYER_MAP.values()) helper.tickChannels();
    }

    @Getter private final Map<String,ChannelAPI> channels;
    @Getter private final List<Toggle> toggles;
    @Getter private final boolean client;
    @Getter private final MTDebugInfo debugInfo;
    @Getter private boolean syncable;
    private final String playerID;
    private MessageTriggerStates<?> stateMsg;
    private int ticks;

    public ChannelHelper(String playerID, boolean client) {
        this.channels = Collections.synchronizedMap(new HashMap<>());
        this.toggles = new ArrayList<>();
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
    
    public boolean checkForJukebox() {
        PlayerAPI<?,?> player = getPlayer();
        if(Objects.nonNull(player)) {
            Vector3d pos = player.getPosExact();
            Box box = ShapeHelper.box(pos,126d);
            for(BlockEntityAPI<?,?> entity : player.getWorld().getBlockEntitiesInBox(box))
                if(entity.getRegistryName().getPath().contains("jukebox") &&
                   entity.getState().getPropertyBool("has_record")) return true;
        }
        return false;
    }

    public void close() {
        this.stateMsg = null;
        synchronized(this.channels) {
            for(ChannelAPI channel : this.channels.values()) channel.close();
        }
        this.channels.clear();
        for(Toggle toggle : this.toggles) toggle.close();
        this.toggles.clear();
    }

    public @Nullable ChannelAPI findChannel(LoggableAPI logger, String channelName) {
        ChannelAPI channel = channels.get(channelName);
        if(Objects.isNull(channel)) logger.logError("Unable to find channel with name `{}`!",channelName);
        return channel;
    }
    
    public @Nullable ChannelAPI findFirstUserChannel() {
        for(ChannelAPI channel : this.channels.values())
            if(channel.isClientChannel()==this.client && !Misc.equalsAny(channel.getName(),"jukebox","preview"))
                return channel;
        logGlobalWarn("Unable to find any user specified channels!");
        return null;
    }
    
    public void flipDebugParameter(String name) {
        Debug debug = getDebug();
        if(Objects.nonNull(debug)) debug.flipBooleanParameter(name);
    }
    
    public void forEachChannel(Consumer<ChannelAPI> consumer) {
        synchronized(this.channels) {
            this.channels.values().forEach(consumer);
        }
    }
    
    public MessageInitChannels<?> getInitMessage() {
        Toml toggles = globalData.openToggles();
        return new MessageInitChannels<>(globalData.getGlobal(),toggles,this);
    }
    
    public ChannelJukebox getJukeboxChannel() {
        if(!this.client) {
            globalData.logError("Attempted to get jukebox channel on the server side! Things may break");
            return null;
        }
        return (ChannelJukebox)this.channels.get("jukebox");
    }
    
    public @Nullable PlayerAPI<?,?> getPlayer() {
        if(this.client) {
            MinecraftAPI mc = TILRef.getClientSubAPI(ClientAPI::getMinecraft);
            return Objects.nonNull(mc) ? mc.getPlayer() : null;
        }
        for(ChannelAPI channel : this.channels.values()) {
            PlayerAPI<?,?> player = channel.getPlayerEntity();
            if(Objects.nonNull(player)) return channel.getPlayerEntity();
        }
        return null;
    }

    public String getPlayerID() {
        if(!this.client) return this.playerID;
        PlayerAPI<?,?> player = getPlayer();
        if(Objects.isNull(player)) logGlobalDebug("Tried to get the client player ID but the player was null");
        return Objects.nonNull(player) ? player.getUUID().toString() : null;
    }
    
    public ChannelPreview getPreviewChannel() {
        if(!this.client) {
            globalData.logError("Attempted to get preview channel on the server side! Things may break");
            return null;
        }
        return (ChannelPreview)this.channels.get("preview");
    }
    
    public WrapperLink getTogglesLink() {
        return new WrapperLink(this.toggles);
    }
    
    @Override public boolean hasDataToSave() {
        for(ChannelAPI channel : this.channels.values())
            if(channel.hasDataToSave()) return true;
        return false;
    }

    private void initChannel(String name, Toml info) {
        synchronized(this.channels) {
            if(this.channels.containsKey(name)) globalData.logError("Channel with name `{}` already exists!");
            else {
                ChannelAPI channel = this.client ? new ChannelClient(this, info) : new ChannelServer(this, info);
                if(channel.isValid()) this.channels.put(name, channel);
                else globalData.logError("Channel with name `{}` is invalid!");
            }
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
    
    public void loadFromFile(@Nullable Toml globalHolder) throws TomlWritingException {
        if(Objects.isNull(globalHolder)) {
            globalData.logFatal("Cannot initialize channel or toggle data from missing global config!");
            return;
        }
        initChannels(globalHolder);
        parseData();
    }
    
    public void loadFromInit(MessageInitChannels<?> init) {
        Set<Entry<String,ChannelMessage>> channelMessages = init.getChannels().entrySet();
        for(Entry<String,ChannelMessage> entry : channelMessages) {
            String name = entry.getKey();
            Toml info = globalData.getGlobal().getTable("channels").getTable(name);
            ChannelAPI channel = this.client ? new ChannelClient(this,info) : new ChannelServer(this,info);
            synchronized(this.channels) {
                this.channels.put(name, channel);
            }
        }
        for(Entry<String,ChannelMessage> entry : channelMessages) {
            synchronized(this.channels) {
                ChannelAPI channel = this.channels.get(entry.getKey());
                channel.getData().load(entry.getValue());
            }
        }
        synchronized(this.channels) {
            this.channels.values().forEach(channel -> channel.getData().setupLinkTargets());
        }
        globalData.parseToggles(this,init.getToggles());
        globalData.logInfo("Finished loading external channel data");
        if(this.client) {
            globalData.logInfo("Attempting to load stored audio references");
            this.debugInfo.initChannelElements();
            queryCategoryVolume();
        }
        forEachChannel(this::loadTracks);
    }
    
    public void loadTracks(ChannelAPI channel) {
        if(this.client) channel.loadTracks(loader.areResourcesLoaded());
        else channel.getData().getAudio().forEach(ref -> ref.setItem(null));
    }
    
    @Override public void onConnected(CompoundTagAPI<?> worldData) {
        for(ChannelAPI channel : this.channels.values())
            if(worldData.contains(channel.getName())) channel.onConnected(worldData.getCompoundTag(channel.getName()));
    }
    
    @Override public void onLoaded(CompoundTagAPI<?> globalData) {}

    public void parseData() {
        for(ChannelAPI channel : this.channels.values()) channel.parseData();
        for(ChannelAPI channel : this.channels.values())
            channel.getData().setupLinkTargets(); //Needs to be called after all the channels are set up
        globalData.logInfo("Finished parsing channel data");
        globalData.parseToggles(this);
        globalData.logInfo("Finished parsing toggles");
        if(this.client) {
            this.channels.put("jukebox",MTClient.getJukeboxChannel(this));
            this.channels.put("preview",MTClient.getPreviewChannel(this));
            globalData.logInfo("Attempting to load stored audio references");
            this.debugInfo.initChannelElements();
        }
        forEachChannel(this::loadTracks);
    }
    
    public void playToJukebox(BlockPosAPI<?> pos, String channelName, String audioRef) {
        ChannelAPI channel = this.channels.get(channelName);
        if(Objects.nonNull(channel)) {
            AudioRef playThis = null;
            for(AudioRef ref : channel.getData().getAudio())
                if(ref.getName().equals(audioRef))
                    playThis = ref;
            if(Objects.nonNull(playThis)) getJukeboxChannel().playReference(playThis,pos.getPosVec());
            else channel.logError("Unable to find audio with name {} to play for the jukebox channel!",audioRef);
        } else globalData.logError("Unable to find channel reference {}",channelName);
    }
    
    public void queryCategoryVolume() {
        float masterVolume = SoundHelper.getCategoryVolume("master");
        for(ChannelAPI channel : this.channels.values()) {
            channel.setMasterVolume(masterVolume);
            String category = channel.getInfo().getCategory();
            if(category.equalsIgnoreCase("master")) channel.setCategoryVolume(1f);
            else channel.setCategoryVolume(SoundHelper.getCategoryVolume(category));
        }
    }
    
    @Override public void saveGlobalTo(CompoundTagAPI<?> globalData) {
    
    }
    
    @Override public void saveWorldTo(CompoundTagAPI<?> worldData) {
        for(ChannelAPI channel : this.channels.values()) {
            if(channel.hasDataToSave()) {
                CompoundTagAPI<?> channelTag = TagHelper.makeCompoundTag();
                channel.saveWorldTo(channelTag);
                worldData.putTag(channel.getName(),channelTag);
            }
        }
    }
    
    public void setCategoryVolume(String category, float volume) {
        forEachChannel(channel -> {
            if(category.equals("master")) channel.setMasterVolume(volume);
            else if(category.equals(channel.getInfo().getCategory())) channel.setCategoryVolume(volume);
        });
    }
    
    public void setCurrentSong(String channel, String song) {
        if(!this.client) ((ChannelServer)this.channels.get(channel)).setCurrentSong(song);
    }
    
    public void setDebugParameter(String name, Object value) {
        Debug debug = getDebug();
        if(Objects.nonNull(debug)) debug.setParameterValue(name,value);
        else logGlobalError("Cannot set debug value {} = {} because Debug does not exist??",name,
                            value instanceof String ? "\""+value+"\"" : value);
    }
    
    public void setDiscTag(ItemStackAPI<?> stack, String channel, String trigger, String audio, boolean custom) {
        CompoundTagAPI<?> tag = TagHelper.makeCompoundTag();
        tag.putString("channel",channel);
        tag.putString("triggerID",trigger);
        if(custom) tag.putString("custom",audio);
        else tag.putString("audio",audio);
        stack.setTag(tag);
    }
    
    public void setSyncable(boolean sync) {
        forEachChannel(channel -> {
            TriggerContext context = channel.getSelector().getContext();
            if(!this.syncable && sync) context.initSync();
            else if(this.syncable && !sync) context.clearSync();
        });
        this.syncable = sync;
    }
    
    public void stopJukeboxAt(BlockPosAPI<?> pos) {
        getJukeboxChannel().checkStop(pos.getPosVec());
    }
    
    protected void sync() {
        if(this.syncable) {
            if(Objects.isNull(this.stateMsg))
                this.stateMsg = new MessageTriggerStates<>(this);
            for(ChannelAPI channel : this.channels.values()) channel.getSync().addSynced(this.stateMsg);
            if(this.stateMsg.readyToSend() && MTNetwork.send(this.stateMsg,this,false)) this.stateMsg = null;
        }
    }
    
    public void tickChannels() {
        if(loader.isLoading()) return; //Why is this necessary here when it's already in the static method? I have no idea
        boolean jukebox = this.client && checkForJukebox();
        boolean slow = (this.ticks++)%ChannelHelper.getDebugNumber("slow_tick_factor").intValue()==0;
        for(ChannelAPI channel : this.channels.values()) {
            boolean unpaused = channel.tick(jukebox,true);
            if(slow) channel.tickSlow(unpaused);
        }
        if(slow) {
            sync();
            this.ticks = 0;
        }
    }
    
    public Toml togglesAsToml() {
        Toml toml = Toml.getEmpty();
        for(Toggle toggle : this.toggles) toml.addTable("toggle",toggle.toToml());
        return toml;
    }
    
    private boolean writeBasicDisc(ItemStackAPI<?> stack) {
        Map<String,TriggerAPI> activeTriggers = new HashMap<>();
        for(ChannelAPI channel : this.channels.values()) {
            TriggerAPI activeTrigger = channel.getActiveTrigger();
            if(Objects.nonNull(activeTrigger)) activeTriggers.put(channel.getName(),activeTrigger);
        }
        if(activeTriggers.isEmpty()) return false;
        Entry<String,TriggerAPI> selected = RandomHelper.getBasicRandomEntry(activeTriggers.entrySet());
        String songName = this.channels.get(selected.getKey()).getPlayingSongName();
        if(StringUtils.isBlank(songName)) return false;
        setDiscTag(stack,selected.getKey(),selected.getValue().getName(),songName,false);
        return true;
    }
    
    public void writeDisc(ItemStackAPI<?> stack, boolean special) {
        String discType = special ? "special music disc" : "music disc";
        if(special ? writeSpecialDisc(stack) : writeBasicDisc(stack))
            ChannelHelper.logGlobalDebug("Successfully recorded {}",discType);
        else ChannelHelper.logGlobalWarn("Failed to record {}",discType);
    }

    private void writeExampleChannel(Toml toml) throws TomlWritingException {
        Toml table = toml.addTable("channels",false);
        ChannelInfo.writeExampleData(table.addTable("example",false));
    }
    
    private boolean writeSpecialDisc(ItemStackAPI<?> stack) {
        Map<String,List<ChannelEventHandler>> specialHandlers = new HashMap<>();
        for(ChannelAPI channel : this.channels.values()) {
            List<ChannelEventHandler> triggers = new ArrayList<>();
            channel.getData().collectSpecialHandlers(triggers);
            if(!triggers.isEmpty()) specialHandlers.put(channel.getName(),triggers);
        }
        if(specialHandlers.isEmpty()) return false;
        Entry<String,List<ChannelEventHandler>> selected = RandomHelper.getBasicRandomEntry(specialHandlers.entrySet());
        ChannelAPI channel = this.channels.get(selected.getKey());
        if(Objects.isNull(channel)) return false;
        ChannelEventHandler handler = RandomHelper.getBasicRandomEntry(selected.getValue());
        boolean custom = false;
        String songName;
        String triggerName;
        if(handler instanceof TriggerAPI) {
            TriggerAPI trigger = (TriggerAPI)handler;
            triggerName = trigger.getName();
            AudioPool pool = trigger.getAudioPool();
            if(Objects.isNull(pool)) return false;
            songName = RandomHelper.getBasicRandomEntry(pool.getFlattened()).getName();
        } else if(handler instanceof RecordElement) {
            triggerName = "generic";
            songName = ((RecordElement)handler).getKey();
            custom = true;
        } else return false;
        if(StringUtils.isBlank(songName) || StringUtils.isBlank(triggerName)) return false;
        setDiscTag(stack,channel.getName(),triggerName,songName,custom);
        return true;
    }
}