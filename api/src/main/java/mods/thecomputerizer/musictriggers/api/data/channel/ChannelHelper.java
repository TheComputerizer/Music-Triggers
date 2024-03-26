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
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.ChannelClient;
import mods.thecomputerizer.musictriggers.api.data.global.Debug;
import mods.thecomputerizer.musictriggers.api.data.global.GlobalData;
import mods.thecomputerizer.musictriggers.api.data.global.Registration;
import mods.thecomputerizer.musictriggers.api.data.global.Toggle;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.io.FileHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlHelper;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ChannelHelper {

    private static final Map<String,ChannelHelper> PLAYER_MAP = new HashMap<>();
    @Getter private static final GlobalData globalData = new GlobalData();
    private static boolean resourcesLoaded;

    public static void addPlayer(String playerID, boolean isClient) { //TODO Get id for actual server player reference
        PLAYER_MAP.put(playerID,globalData.initHelper(playerID,isClient));
    }

    public static void init() {
        globalData.parse(openToml(MTRef.GLOBAL_CONFIG,globalData));
    }

    public static void initClient() {
        addPlayer("CLIENT",true);
    }

    public static void onResourcesLoaded() {
        resourcesLoaded = true;
        for(ChannelHelper helper : PLAYER_MAP.values())
            if(helper.client)
                for(ChannelAPI channel : helper.channels.values())
                    channel.onResourcesLoaded();
    }

    /**
     * Assumes the file extension is not present
     */
    public static @Nullable Holder openToml(String path, LoggableAPI logger) {
        path+=".toml";
        try {
            return TomlHelper.readFully(path);
        } catch(IOException ex) {
            String msg = "Unable to read toml file at `{}`!";
            if(Objects.nonNull(logger)) logger.logError(msg,ex);
            else MTRef.logError(msg,ex);
            return null;
        }
    }

    /**
     * Assumes the file extension is not present
     */
    public static List<String> openTxt(String path, @Nullable ChannelAPI channel) {
        path+=".txt";
        File file = FileHelper.get(path,false);
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().collect(Collectors.toList());
        } catch(IOException ex) {
            String msg = "Unable to read toml file at `{}`!";
            if(Objects.nonNull(channel)) channel.logError(msg,ex);
            else MTRef.logError(msg,ex);
            return Collections.emptyList();
        }
    }

    public static void registerRemoteSources(ChannelAPI channel, AudioPlayerManager manager) {
        registerRemoteSource(channel,manager,"YouTube", () -> new YoutubeAudioSourceManager(
                true,channel.getHelper().youtubeEmail,channel.getHelper().youtubePassword));
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
    @Getter private final String playerID;
    @Getter private final boolean client;
    private final String youtubeEmail;
    private final String youtubePassword;

    public ChannelHelper(String playerID, boolean client, String email, String password) {
        this.channels = new HashMap<>();
        this.toggles = new HashSet<>();
        this.playerID = playerID;
        this.client = client;
        this.youtubeEmail = email;
        this.youtubePassword = password;
    }

    public @Nullable ChannelAPI findChannel(LoggableAPI logger, String channelName) {
        ChannelAPI channel = channels.get(channelName);
        if(Objects.isNull(channel)) logger.logError("Unable to find channel with name `{}`!",channelName);
        return channel;
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

    public boolean getRegistrationBool(String name) {
        Registration registration = getRegistration();
        return Objects.nonNull(registration) && registration.getParameterAsBoolean(name);
    }

    public @Nullable Registration getRegistration() {
        return globalData.getRegistration();
    }

    public void init(@Nullable Holder globalHolder) { //TODO Sided stuff & server channels
        if(Objects.isNull(globalHolder)) {
            globalData.logFatal("Cannot initialize channel or toggle data from missing global config!");
            return;
        }
        initChannels(globalHolder.getTableByName("channels"));
        initToggles();
        parseData(resourcesLoaded);
    }

    private void initChannels(@Nullable Table table) {
        if(Objects.isNull(table)) return;
        for(String name : table.getTableNames()) {
            ChannelAPI channel = new ChannelClient(this,table.getTableByName(name));
            if(channel.isValid()) {
                if(!this.channels.containsKey(name)) this.channels.put(name,channel);
                else globalData.logError("Channel with name `{}` already exists!");
            } else globalData.logError("Channel with name `{}` is invalid!");
        }
    }

    private void initToggles() {
        Holder toggles = globalData.openToggles(MTRef.CONFIG_PATH+"/");
        for(Table table : toggles.getTablesByName("toggle")) this.toggles.add(new Toggle(this,table));
    }

    public void parseData(boolean loadResources) {
        for(ChannelAPI channel : channels.values()) {
            channel.parseData();
            channel.loadTracks(loadResources);
        }
        this.toggles.removeIf(toggle -> !toggle.parse());
    }
}