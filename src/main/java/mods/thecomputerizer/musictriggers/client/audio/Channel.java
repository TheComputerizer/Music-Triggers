package mods.thecomputerizer.musictriggers.client.audio;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.client.MusicPicker;
import mods.thecomputerizer.musictriggers.client.data.Audio;
import mods.thecomputerizer.musictriggers.client.data.Toggle;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.instance.*;
import mods.thecomputerizer.musictriggers.config.ConfigJukebox;
import mods.thecomputerizer.musictriggers.config.ConfigRedirect;
import mods.thecomputerizer.musictriggers.server.ServerData;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Variable;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.TomlUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultClientPackResources;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.sounds.SoundSource;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class Channel {
    private final Table info;
    private final SoundSource category;
    private final Data data;
    private final ConfigRedirect redirect;
    private final ConfigJukebox jukebox;
    private final MusicPicker picker;
    private final boolean canBePausedByJukeBox;
    private final boolean overrides;
    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final ChannelListener listener;
    private final HashMap<String, AudioTrack> loadedTracks;
    private final ClientSync sync;
    private final List<String> commandsForPacket;
    private final List<String> erroredSongDownloads;
    private final String localFolderPath;

    private boolean triggerStarted;
    private boolean fadingIn = false;
    private boolean fadingOut = false;
    private boolean reverseFade = false;
    private int tempFadeIn = 0;
    private int tempFadeOut = 0;
    private int savedFadeOut = 0;
    private Audio curTrack;
    private final List<Audio> oncePerTrigger;
    private final List<Audio> onceUntilEmpty;
    private int delayCounter = 0;
    private boolean delayCatch = false;
    private final List<Trigger> playingTriggers;
    private boolean pausedByJukebox = false;
    private boolean changedStatus = false;

    public Channel(Table info) throws IOException {
        for(String filePath : collectFilePaths(info))
            if(!ChannelManager.verifyOtherFilePath(filePath))
                throw new IOException("Config path in channel "+info.getName()+" cannot be "+filePath+" as that " +
                        "matches the path of a config file in an already registered channel!");
        this.info = info;
        String category = info.getValOrDefault("sound_category","music");
        this.category = EnumUtils.isValidEnum(SoundSource.class, category) ?
                SoundSource.valueOf(category) : SoundSource.MUSIC;
        Constants.MAIN_LOG.error("SOUND CATEGORY IS {}",this.category);
        this.canBePausedByJukeBox = info.getValOrDefault("paused_by_jukebox",true);
        this.overrides = info.getValOrDefault("overrides_normal_music",true);
        this.sync = new ClientSync(info.getName());
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(this.playerManager);
        AudioSourceManagers.registerLocalSource(this.playerManager);
        this.player = this.playerManager.createPlayer();
        this.player.setVolume(100);
        this.listener = new ChannelListener(this.player, StandardAudioDataFormats.DISCORD_PCM_S16_BE, this);
        this.loadedTracks = new HashMap<>();
        this.playerManager.setFrameBufferDuration(1000);
        this.playerManager.setPlayerCleanupThreshold(Long.MAX_VALUE);
        this.playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        this.playerManager.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
        this.playerManager.getConfiguration().setOutputFormat(StandardAudioDataFormats.DISCORD_PCM_S16_BE);
        this.oncePerTrigger = new ArrayList<>();
        this.onceUntilEmpty = new ArrayList<>();
        this.commandsForPacket = new ArrayList<>();
        this.erroredSongDownloads = new ArrayList<>();
        this.playingTriggers = new ArrayList<>();
        MusicTriggers.logExternally(Level.INFO, "Registered sound engine channel "+ info.getName());
        this.data = new Data(TomlUtil.readFully(MusicTriggers.configFile(
                info.getValOrDefault("main", info.getName() + "/main"),"toml")),
                TomlUtil.readFully(MusicTriggers.configFile(
                        info.getValOrDefault("transitions", info.getName() + "/transitions"),"toml")),
                TomlUtil.readFully(MusicTriggers.configFile(
                        info.getValOrDefault("commands", info.getName() + "/commands"),"toml")),
                TomlUtil.readFully(MusicTriggers.configFile(
                        info.getValOrDefault("toggles", info.getName() + "/toggles"),"toml")));
        this.redirect = new ConfigRedirect(MusicTriggers.configFile(
                info.getValOrDefault("redirect", info.getName() + "/redirect"),"txt"));
        this.jukebox = new ConfigJukebox(MusicTriggers.configFile(
                info.getValOrDefault("jukebox", info.getName() + "/jukebox"),"txt"));
        this.picker = new MusicPicker(this);
        this.localFolderPath = info.getValOrDefault("songs_folder", "config/MusicTriggers/songs");
        File file = new File(this.localFolderPath);
        if(!file.exists()) file.mkdirs();
    }

    private List<String> collectFilePaths(Table info) {
        return Arrays.asList(info.getValOrDefault("main", info.getName() + "/main"),
                info.getValOrDefault("transitions", info.getName() + "/transitions"),
                info.getValOrDefault("commands", info.getName() + "/commands"),
                info.getValOrDefault("toggles", info.getName() + "/toggles"),
                info.getValOrDefault("redirect", info.getName() + "/redirect"),
                info.getValOrDefault("jukebox", info.getName() + "/jukebox"));
    }

    public boolean verifyOtherFilePath(String path) {
        return !collectFilePaths(this.info).contains(path);
    }

    public SoundSource getCategory() {
        return this.category;
    }

    public String getLocalFolder() {
        return this.localFolderPath;
    }

    public Map<String, String> getRecordMap() {
        return this.jukebox.recordMap;
    }

    public String getChannelName() {
        return this.info.getName();
    }

    public void runToggle(int condition, List<Trigger> triggers) {
        for(Toggle toggle : this.data.toggleList)
            toggle.runToggle(condition, triggers);
    }

    public ClientSync getSyncStatus() {
        return this.sync;
    }

    public void markUpdated() {
        this.changedStatus = true;
    }

    public boolean needsUpdatePacket() {
        return this.changedStatus;
    }

    public boolean overridesNormalMusic() {
        return this.overrides;
    }

    public boolean getVictory(int id) {
        return this.picker.getVictory(id);
    }

    private void checkLoops() {
        if(Objects.nonNull(this.curTrack) && Objects.nonNull(getCurPlaying())) {
            for(Audio.Loop loop : this.curTrack.getLoops()) {
                long posCapture = this.getMillis();
                long setTo = loop.checkForLoop(posCapture,this.getTotalMillis());
                if(posCapture!=setTo) setMillis(setTo);
            }
        }
    }

    public void tickFast() {
        if(checkAudio() && !this.data.registeredAudio.isEmpty()) {
            for (Trigger trigger : this.picker.startMap.keySet()) {
                if (this.picker.startMap.get(trigger).getValue() > 0)
                    this.picker.startMap.get(trigger).decrement();
            }
            for (Trigger trigger : this.picker.triggerPersistence.keySet()) {
                if (this.picker.triggerPersistence.get(trigger).getValue() > 0)
                    this.picker.triggerPersistence.get(trigger).decrement();
            }
            checkLoops();
            if(this.isPlaying()) {
                float calculatedVolume = this.curTrack.getVolume()*getChannelVolume();
                if (this.fadingIn && !this.fadingOut) {
                    this.reverseFade = false;
                    if (this.tempFadeIn == 0) this.fadingIn = false;
                    else {
                        float ratio = 1f-(((float)this.tempFadeIn)/((float)this.picker.fadeIn));
                        calculatedVolume = calculatedVolume*ratio;
                        this.tempFadeIn -= 1;
                    }
                }
                else if (this.fadingOut && !this.reverseFade) {
                    this.tempFadeIn = 0;
                    this.fadingIn = false;
                    if (this.tempFadeOut == 0) clearSongs();
                    else {
                        if (getCurPlaying() == null) this.tempFadeOut = 0;
                        else {
                            float ratio = ((float)this.tempFadeOut)/((float)this.savedFadeOut);
                            calculatedVolume = calculatedVolume*ratio;
                            this.tempFadeOut -= 1;
                            if (!this.picker.getInfo().songListChanged())
                                if(!this.picker.getInfo().getCurrentSongList().isEmpty())
                                    this.reverseFade = true;
                        }
                    }
                } else if (this.fadingOut) {
                    if (this.tempFadeOut >= this.savedFadeOut) {
                        this.fadingOut = false;
                        this.reverseFade = false;
                        this.tempFadeOut = 0;
                    } else {
                        float ratio = ((float)this.tempFadeOut)/((float)this.savedFadeOut);
                        calculatedVolume = calculatedVolume*ratio;
                        this.tempFadeOut += 1;
                    }
                }
                setVolume(calculatedVolume);
            } else clearSongs();
            if (this.delayCounter > 0) this.delayCounter -= 1;
        } else {
            this.delayCounter = 0;
            this.tempFadeIn = 0;
            this.tempFadeOut = 0;
            this.fadingIn = false;
            this.fadingOut = false;
            this.reverseFade = false;
        }
    }

    public void tickSlow() {
        if (checkAudio() && !this.data.registeredAudio.isEmpty()) {
            this.picker.querySongList(this.data.universalTriggerParameters);
            if (!isPlaying()) {
                if (!this.picker.getInfo().getActiveTriggers().isEmpty()) {
                    if (this.playingTriggers.isEmpty()) {
                        this.delayCounter = MusicTriggers.randomInt("trigger_delay", this.picker.triggerDelay, 0);
                        this.delayCatch = true;
                        this.playingTriggers.addAll(this.picker.getInfo().getActiveTriggers());
                        onTriggerStart();
                    }
                    if (this.playingTriggers.equals(this.picker.getInfo().getActiveTriggers())) {
                        if (!this.delayCatch) {
                            this.delayCounter = MusicTriggers.randomInt("song_delay", this.picker.songDelay, 0);
                            this.delayCatch = true;
                        }
                        if (this.delayCounter <= 0) {
                            Audio audio = getWeightedAudio();
                            if (Objects.nonNull(audio)) {
                                MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Attempting to play track \"{}\"",
                                        getChannelName(), audio.getName());
                                float pitch = audio.getPitch();
                                setPitch(pitch);
                                if(this.triggerStarted) this.tempFadeIn = this.picker.fadeIn;
                                this.triggerStarted = false;
                                playTrack(audio, 0);
                                delayCatch = false;
                                if (audio.getPlayOnce() == 1) this.onceUntilEmpty.add(audio);
                                if (audio.getPlayOnce() == 2) this.oncePerTrigger.add(audio);
                            } else
                                MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Audio was null!", getChannelName());
                        }
                    } else {
                        this.delayCounter = MusicTriggers.randomInt("trigger_delay", this.picker.triggerDelay, 0);
                        this.delayCatch = true;
                        this.playingTriggers.clear();
                        this.playingTriggers.addAll(this.picker.getInfo().getActiveTriggers());
                        onTriggerStart();
                    }
                } else {
                    this.curTrack = null;
                    this.playingTriggers.clear();
                }
            } else if (!this.fadingOut && Objects.nonNull(this.curTrack)) {
                if (!this.curTrack.mustFinish() && (!this.playingTriggers.equals(this.picker.getInfo().getActiveTriggers())
                        || this.picker.getInfo().getActiveTriggers().isEmpty())) {
                    if (this.picker.getInfo().getCurrentSongList().contains(this.curTrack)) {
                        this.playingTriggers.clear();
                        this.playingTriggers.addAll(this.picker.getInfo().getActiveTriggers());
                    } else stopTrack(true);
                }
            } else if (Objects.isNull(this.curTrack)) stopTrack(true);
            for (Trigger playable : this.picker.getInfo().getPlayableTriggers()) {
                if (!this.picker.getInfo().getActiveTriggers().contains(playable))
                    if (playable.getParameterBool("toggle_inactive_playable"))
                        playable.setToggle(false);
            }
        } else clearSongs();
    }

    private void onTriggerStart() {
        this.commandsForPacket.clear();
        for(String command : this.data.commandMap.keySet()) {
            if(new HashSet<>(this.picker.getInfo().getActiveTriggers()).containsAll(this.data.commandMap.get(command)))
                this.commandsForPacket.add(command);
        }
        renderCards();
        this.savedFadeOut = this.picker.fadeOut;
        this.tempFadeOut = this.picker.fadeOut;
        this.triggerStarted = true;
        setVolume(0.01f/this.getChannelVolume());
        this.fadingIn = true;
    }

    //Returns null if a no song is found
    private Audio getWeightedAudio() {
        List<Audio> curSongs = this.picker.getInfo().getCurrentSongList().stream()
                .filter(audio -> !this.oncePerTrigger.contains(audio) && !this.onceUntilEmpty.contains(audio)).toList();
        if(curSongs.size()==0) {
            this.onceUntilEmpty.clear();
            return null;
        }
        if(curSongs.size()==1) return curSongs.get(0);
        int sum = 0;
        for(Audio audio : curSongs) {
            if(audio!=this.curTrack)
                sum+=audio.getChance();
        }
        int rand = MusicTriggers.randomInt(sum);
        for(Audio audio : curSongs) {
            rand-=(audio==this.curTrack ? 0 : audio.getChance());
            if(rand<0) return audio;
        }
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    public void renderCards() {
        MusicTriggers.logExternally(Level.DEBUG, "Channel[{}] - Finding cards to render",getChannelName());
        for (Table table : this.data.titleCards.keySet())
            if(this.data.canPlayTitle(table,table.getValOrDefault("vague",false))) {
                synchronized(ChannelManager.tickingRenderables) {
                    ChannelManager.addRenderable(true,table);
                }
            }
        for (Table table : this.data.imageCards.keySet())
            if(this.data.canPlayImage(table,table.getValOrDefault("vague",false))) {
                synchronized(ChannelManager.tickingRenderables) {
                    ChannelManager.addRenderable(false,table);
                }
            }
    }

    public String formatPlayback() {
        String ret = "No song playing";
        if(isPlaying())
            ret = formatMinutes((int) (getMillis() / 1000f));// + "/" + formatMinutes((int) (getTotalMillis() / 1000f));
        return ret;
    }

    private String formatMinutes(int seconds) {
        int minutes = 0;
        while (seconds>=60) {
            minutes++;
            seconds-=60;
        }
        return minutes+":"+(seconds<10 ? "0"+seconds : seconds);
    }

    public String formatSongTime() {
        String ret = "No song playing";
        if(isPlaying()) ret = formattedTimeFromTicks(((float)getMillis())/50f);
        return ret;
    }

    public String formattedFadeInTime() {
        if(this.fadingIn) return formattedTimeFromTicks(this.tempFadeIn);
        return null;
    }

    public String formattedFadeOutTime() {
        if(this.fadingOut) return formattedTimeFromTicks(this.tempFadeOut);
        return null;
    }

    public String formattedTimeFromTicks(float ticks) {
        if (ticks == -1) ticks = 0;
        float seconds = ticks / 20f;
        if (seconds % 60 < 10) return (int) (seconds / 60) + ":0" + (int) (seconds % 60) + formatTicksToMillis(ticks);
        else return (int) (seconds / 60) + ":" + (int) (seconds % 60) + formatTicksToMillis(ticks);
    }

    private String formatTicksToMillis(float ticks) {
        float milliseconds = ticks*50f;
        if(milliseconds%1000<10) return ":00"+(int)(milliseconds%1000);
        else if(milliseconds%1000<100) return ":0"+(int)(milliseconds%1000);
        else return ":"+(int)(milliseconds%1000);
    }

    public List<Trigger> getPlayableTriggers() {
        return this.picker.getInfo().getPlayableTriggers();
    }

    public AudioPlayer getPlayer() {
        return this.player;
    }

    public AudioTrack getCurPlaying() {
        return this.player.getPlayingTrack();
    }

    public String curPlayingName() {
        if(Objects.isNull(this.curTrack)) return null;
        return this.curTrack.getName();
    }

    public boolean isPlaying() {
        return getCurPlaying()!=null;
    }

    private boolean checkAudio() {
        return Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER) > 0
                    && Minecraft.getInstance().options.getSoundSourceVolume(this.category) > 0;
    }

    public long getTotalMillis() {
        return getCurPlaying().getInfo().length;
    }

    public long getMillis() {
        return getCurPlaying().getPosition();
    }

    public void setMillis(long milliseconds) {
        MusicTriggers.logExternally(Level.WARN, "Found loop! Setting track time to {}",milliseconds);
        getCurPlaying().setPosition(milliseconds);
    }

    public void setVolume(float volume) {
        this.getPlayer().setVolume((int)(volume*100));
    }

    private float getChannelVolume() {
        float master = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
        if(getCategory()==SoundSource.MASTER) return master;
        else return master*Minecraft.getInstance().options.getSoundSourceVolume(getCategory());
    }

    public void resetTrack() {
        if(isPlaying()) {
            MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Attempting to reset currently playing track",getChannelName());
            AudioTrack cloned = this.getPlayer().getPlayingTrack().makeClone();
            if (!this.getPlayer().startTrack(cloned, false))
                MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Could not reset track!",getChannelName());
        }
    }

    public void playTrack(Audio audio, long milliseconds) {
        String id = audio.getName();
        AudioTrack track = this.loadedTracks.get(audio.getName());
        if(track!=null) {
            try {
                if (!this.getPlayer().startTrack(track, false))
                    MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Could not start track!",getChannelName());
                else this.curTrack = audio;
                MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Track with id {} is seekable: {}",getChannelName(),id,track.isSeekable());
            } catch (IllegalStateException e) {
                AudioTrack cloned = track.makeClone();
                if (!this.getPlayer().startTrack(cloned, false))
                    MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Could not start track!",getChannelName());
                else this.curTrack = audio;
                MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Track with id {} is seekable: {}",getChannelName(),id,track.isSeekable());
            }
        } else {
            MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Track with id {} was null! Attempting to " +
                    "refresh track...",getChannelName(),id);
            this.loadedTracks.remove(id);
            if(this.redirect.urlMap.containsKey(id)) loadFromURL(id,this.redirect.urlMap.get(id),audio);
            else if(this.redirect.resourceLocationMap.containsKey(id))
                loadFromResourceLocation(id,this.redirect.resourceLocationMap.get(id),audio);
            else {
                boolean foundFile = false;
                for(File file : ChannelManager.openAudioFiles.get(this.localFolderPath)) {
                    if (FilenameUtils.getBaseName(file.getName()).matches(id)) {
                        loadAudioFile(id,file,audio);
                        foundFile = true;
                        break;
                    }
                }
                if(!foundFile) {
                    MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Track with id {} does not seem to exist! All " +
                            "instances using this song will be removed until reloading.", getChannelName(), id);
                    removeErroredAudio(audio);
                }
            }
        }
    }

    public AudioTrack getCopyOfTrackFromID(String id) {
        return this.loadedTracks.get(id).makeClone();
    }

    public boolean isPaused() {
        return this.getPlayer().isPaused();
    }

    public void jukeBoxPause() {
        if(this.canBePausedByJukeBox) {
            if (!this.getPlayer().isPaused()) this.getPlayer().setPaused(true);
            this.pausedByJukebox = true;
        }
    }

    public void jukeBoxUnpause() {
        if(this.pausedByJukebox) {
            if (this.getPlayer().isPaused()) this.getPlayer().setPaused(false);
            this.pausedByJukebox = false;
        }
    }

    public void setPausedGeneric(boolean paused) {
        if(!this.pausedByJukebox) {
            if(this.getPlayer().isPaused()) {
                if (!paused) this.getPlayer().setPaused(false);
            } else if(paused) this.getPlayer().setPaused(true);
        }
    }

    public void setPitch(float pitch) {
        setFilters(pitch);
    }

    private void setFilters(float pitch) {
        //getPlayer().setFilterFactory((track, format, output) -> {});
    }

    public void stopTrack(boolean shouldFade) {
        if(!shouldFade) {
            this.picker.startMap.entrySet().removeIf(entry -> !this.picker.getInfo().getPlayableTriggers().contains(entry.getKey()));
            this.getPlayer().stopTrack();
        }
        else changeTrack();
    }

    public void parseRedirect(ConfigRedirect redirect) {
        redirect.parse();
        this.erroredSongDownloads.clear();
        for(String id : redirect.urlMap.keySet()) loadFromURL(id,redirect.urlMap.get(id),null);
        for (String folderPath : ChannelManager.openAudioFiles.keySet()) {
            if (this.localFolderPath.matches(folderPath)) {
                for(File file : ChannelManager.openAudioFiles.get(folderPath)) {
                    String name = FilenameUtils.getBaseName(file.getName());
                    if (!loadedTracks.containsKey(name))
                        loadAudioFile(name, file, null);
                }
            }
        }
        if(!this.erroredSongDownloads.isEmpty())
            MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Could not read audio from these sources",getChannelName());
        for(String error : this.erroredSongDownloads) MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - {}",
                getChannelName(),error);
    }

    public void readResourceLocations() {
        for(String id : redirect.resourceLocationMap.keySet())
            loadFromResourceLocation(id,redirect.resourceLocationMap.get(id),null);
    }

    public void addTrackToMap(String id, AudioTrack track) {
        this.loadedTracks.put(id, track);
    }

    private void loadFromURL(String id, String url, @Nullable Audio audioReference) {
        this.playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if(!Channel.this.loadedTracks.containsKey(id)) {
                    Channel.this.addTrackToMap(id,track);
                    MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Track loaded from url {}",getChannelName(),url);
                } else MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Audio file with id {}} already exists!",
                        getChannelName(),id);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Loaded a playlist from {}",getChannelName(),url);
                for(int i=1;i<playlist.getTracks().size()+1;i++) {
                    if(!Channel.this.loadedTracks.containsKey(id+"_"+i)) {
                        Channel.this.addTrackToMap(id,playlist.getTracks().get(i));
                        MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Track {} loaded from playlist url {}",
                                getChannelName(),i,url);
                    } else MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Audio file with id {}_{} " +
                            "already exists!",getChannelName(),id,i);
                }
            }

            @Override
            public void noMatches() {
                MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - No audio able to be extracted from url {}",
                        getChannelName(),url);
                Channel.this.erroredSongDownloads.add(id+" -> "+url);
                if(Objects.nonNull(audioReference)) {
                    MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Audio track with id {} has errored twice in " +
                                    "row and will be removed from the registry!",getChannelName());
                    removeErroredAudio(audioReference);
                }
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Load failed! {}",getChannelName(),url);
                exception.printStackTrace();
                if(Objects.nonNull(audioReference)) {
                    MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Audio track with id {} has errored twice in " +
                            "row and will be removed from the registry!",getChannelName());
                    removeErroredAudio(audioReference);
                }
            }
        });
    }

    private void loadFromResourceLocation(String id, ResourceLocation source, @Nullable Audio audioReference) {
        try {
            FileSystem zipSystem = null;
            if (!this.loadedTracks.containsKey(id)) {
                String namespace = source.getNamespace();
                String sourcePath = null;
                String[] sourceFolders = source.getPath().split("/");
                String name = sourceFolders[sourceFolders.length-1];
                for(Pack packInfo : Minecraft.getInstance().getResourcePackRepository().getSelectedPacks()) {
                    PackResources pack = packInfo.open();
                    if(pack.getNamespaces(PackType.CLIENT_RESOURCES).contains(namespace) &&
                            pack.hasResource(PackType.CLIENT_RESOURCES,source)) {
                        URL url = pack.getClass().getResource("/"+PackType.CLIENT_RESOURCES.getDirectory()+"/"+
                                source.getNamespace()+"/"+source.getPath());
                        if (url != null && (url.getProtocol().equals("jar") || FolderPackResources.validatePath(new File(url.getFile()),
                                "/"+PackType.CLIENT_RESOURCES.getDirectory()+"/"+source.getNamespace()+"/"+source.getPath())))
                            sourcePath = url.getPath();
                        else {
                            if (pack instanceof AbstractPackResources resourcePack) {
                                String resource = String.format("%s/%s/%s", PackType.CLIENT_RESOURCES.getDirectory(),
                                        source.getNamespace(), source.getPath());
                                if (!(pack instanceof FilePackResources)) {
                                    File resourceFile = new File(resourcePack.file, resource);
                                    if (resourceFile.exists() && resourceFile.isFile()) sourcePath = resourceFile.getAbsolutePath();
                                } else {
                                    if (zipSystem != null) zipSystem.close();
                                    URI zip = resourcePack.file.toURI();
                                    zipSystem = FileSystems.newFileSystem(zip, new HashMap<>());
                                    Path resourcePath = zipSystem.getPath(resource);
                                    try {
                                        URL test = new URL(resourcePath.toUri().toString());
                                        sourcePath = resourcePath.toAbsolutePath().toString();
                                        MusicTriggers.logExternally(Level.INFO, "breaking from zip");
                                        break;
                                    } catch (MalformedURLException ignored) {
                                    }
                                }
                            } else if (pack instanceof DefaultClientPackResources vanillaPack) {
                                File file = vanillaPack.assetIndex.getFile(source);
                                if (file != null && file.exists()) {
                                    sourcePath = file.getAbsolutePath();
                                    MusicTriggers.logExternally(Level.INFO, "found file uri!");
                                }
                            }
                        }
                    }
                }
                if(Objects.nonNull(sourcePath)) {
                    AudioLoadResultHandler handler = new AudioLoadResultHandler() {
                        @Override
                        public void trackLoaded(AudioTrack track) {
                            Channel.this.addTrackToMap(id, track);
                            MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Track loaded from resource " +
                                    "location {}",getChannelName(),source);
                        }

                        @Override
                        public void playlistLoaded(AudioPlaylist playlist) {
                            MusicTriggers.logExternally(Level.INFO, "Channel[{}] - no playlists here",getChannelName());
                        }

                        @Override
                        public void noMatches() {
                            MusicTriggers.logExternally(Level.INFO, "Channel[{}] - no matches from resource " +
                                    "location {}",getChannelName(),source);
                            if(Objects.nonNull(audioReference)) {
                                MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Audio track with id {} has errored twice in " +
                                        "row and will be removed from the registry!",getChannelName());
                                removeErroredAudio(audioReference);
                            }
                        }

                        @Override
                        public void loadFailed(FriendlyException exception) {
                            MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Track loaded failed resource " +
                                    "location {}",getChannelName(),source);
                            if(Objects.nonNull(audioReference)) {
                                MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Audio track with id {} has errored twice in " +
                                        "row and will be removed from the registry!",getChannelName());
                                removeErroredAudio(audioReference);
                            }
                        }
                    };
                    if(Objects.nonNull(name)) this.playerManager.loadItem(new AudioReference(sourcePath, name),handler);
                    else this.playerManager.loadItem(sourcePath,handler);
                } else MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Failed to get URI for resource " +
                        "location {}",getChannelName(),source);
            } else MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Audio file with id {} already exists!",
                    getChannelName(),id);
        } catch (Exception e) {
            MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Could not decode track from resource " +
                    "location {}! See the main log for the full error",getChannelName(),source);
            Constants.MAIN_LOG.error("Channel[{}] - Could not decode track from resource location",getChannelName(),e);
            e.printStackTrace();
        }
    }

    private void loadAudioFile(String id, File file, @Nullable Audio audioReference) {
        try {
            this.playerManager.loadItem(new AudioReference(file.getPath(), file.getName()), new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    if (!Channel.this.loadedTracks.containsKey(id)) {
                        Channel.this.addTrackToMap(id, track);
                        MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Track loaded from file {}",
                                getChannelName(),file.getName());
                    } else MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Audio file with id {} already " +
                            "exists!",getChannelName(),id);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Loaded track from file {}",
                            getChannelName(),file.getName());
                    for (int i = 1; i < playlist.getTracks().size() + 1; i++) {
                        if (!Channel.this.loadedTracks.containsKey(id + "_" + i)) {
                            Channel.this.addTrackToMap(id, playlist.getTracks().get(i));
                            MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Track {}_{} loaded from playlist " +
                                    "file {}",getChannelName(),id,i,file.getName());
                        } else MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Audio file with id {}_{} " +
                                "already exists!",getChannelName(),id,i);
                    }
                }

                @Override
                public void noMatches() {
                    MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - No audio able to be extracted from " +
                            "file {}",getChannelName(),file.getName());
                    Channel.this.erroredSongDownloads.add(id + " -> " + file.getName());
                    if(Objects.nonNull(audioReference)) {
                        MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Audio track with id {} has errored twice in " +
                                "row and will be removed from the registry!",getChannelName());
                        removeErroredAudio(audioReference);
                    }
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Load failed! {}",getChannelName(),file.getName());
                    exception.printStackTrace();
                    if(Objects.nonNull(audioReference)) {
                        MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Audio track with id {} has errored twice in " +
                                "row and will be removed from the registry!",getChannelName());
                        removeErroredAudio(audioReference);
                    }
                }
            });
        } catch (Exception e) {
            MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Could not load track from file {}! See the " +
                    "main log for the full error",getChannelName(),id);
            Constants.MAIN_LOG.error("Channel[{}] - Could not load track from file",getChannelName(),e);
            e.printStackTrace();
        }
    }

    public void initializeServerData(ServerData serverData) {
        serverData.addChannelInfo(getChannelName(),new ArrayList<>(this.data.serverTriggers.values()),this.data.menuSongs);
    }

    public void encodeDynamic(FriendlyByteBuf buf) {
        NetworkUtil.writeString(buf,getChannelName());
        NetworkUtil.writeGenericList(buf,this.commandsForPacket,NetworkUtil::writeString);
        boolean playingSong = Objects.nonNull(this.curTrack) && Objects.nonNull(this.picker.getInfo().highestPriorityActive());
        buf.writeBoolean(playingSong);
        if(playingSong) {
            NetworkUtil.writeString(buf,this.curTrack.getName());
            NetworkUtil.writeString(buf, Objects.requireNonNull(this.picker.getInfo().highestPriorityActive()).getName());
        }
        this.commandsForPacket.clear();
        this.changedStatus = false;
    }

    public void sync(ClientSync fromServer) {
        this.sync.merge(fromServer);
    }

    private void changeTrack() {
        if (!this.fadingOut) {
            this.fadingOut = true;
        } else if (reverseFade) reverseFade = false;
    }

    private void clearSongs() {
        if(isPlaying()) stopTrack(false);
        oncePerTrigger.clear();
        onceUntilEmpty.clear();
        this.fadingOut = false;
        tempFadeIn = this.picker.fadeIn;
        curTrack = null;
    }

    public void parseConfigs(boolean startup) {
        parseRedirect(this.redirect);
        if(!startup) readResourceLocations();
        this.jukebox.parse();
    }

    public Trigger getSimpleTrigger(String name) {
        return this.data.registeredTriggerMap.containsKey(name) ? this.data.registeredTriggerMap.get(name).get("not_accepted") :
                null;
    }

    public List<Trigger> getRegisteredTriggers() {
        return this.data.registeredTriggers;
    }

    public List<Audio> getSongPool(Trigger trigger) {
        return this.data.songPools.get(trigger);
    }

    private void removeErroredAudio(Audio audio) {
        String name = audio.getName();
        this.redirect.urlMap.remove(name);
        this.redirect.resourceLocationMap.remove(name);
        this.data.menuSongs.remove(name);
        this.data.registeredAudio.remove(audio);
        for(List<Audio> pool : this.data.songPools.values()) pool.remove(audio);
        this.data.songPools.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        this.data.registeredTriggers.removeIf(trigger -> !this.data.songPools.containsKey(trigger));
        for(String triggerName : this.data.registeredTriggerMap.keySet())
             this.data.registeredTriggerMap.get(triggerName).entrySet().removeIf(entry ->
                     !this.data.registeredTriggers.contains(entry.getValue()));
        this.data.registeredTriggerMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        this.data.serverTriggers.entrySet().removeIf(entry -> !this.data.registeredTriggers.contains(entry.getKey()));

    }

    public ChannelInstance createGuiData() {
        return new ChannelInstance(MusicTriggers.clone(this.info),new Main(getChannelName(),this.data.main),
                new Transitions(getChannelName(),this.data.transitions),new Commands(getChannelName(),this.data.commands),
                new Toggles(getChannelName(),this.data.toggles),
                new Redirect(getChannelName(),this.redirect.urlMap,this.redirect.resourceLocationMap),
                new Jukebox(getChannelName(),this.getRecordMap()));
    }

    public void clear() {
        this.listener.stop();
        this.player.destroy();
        this.loadedTracks.clear();
        this.changedStatus = false;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    class Data {
        private final Holder main;
        private final Holder transitions;
        private final Holder commands;
        private final Holder toggles;
        private final Map<String, HashMap<String, Trigger>> registeredTriggerMap;
        private final List<Trigger> registeredTriggers;
        private final Map<Trigger, Table> serverTriggers;
        private final List<Audio> registeredAudio;
        private final List<String> menuSongs;
        private final HashMap<Trigger, List<Audio>> songPools;
        private Optional<Table> universalTriggerParameters;
        private Optional<Table> universalAudioParameters;
        private final Map<Table, List<Trigger>> titleCards;
        private final Map<Table, List<Trigger>> imageCards;
        private final Map<String, List<Trigger>> commandMap;
        private final List<Toggle> toggleList;

        Data(Holder main, Holder transitions, Holder commands, Holder toggles) {
            this.main = main;
            this.transitions = transitions;
            this.commands = commands;
            this.toggles = toggles;
            this.registeredTriggers = new ArrayList<>();
            this.serverTriggers = new HashMap<>();
            this.registeredTriggerMap = parseTriggers(main.getTableByName("triggers"));
            this.menuSongs = new ArrayList<>();
            this.songPools = new HashMap<>();
            this.registeredAudio = parseAudio(main.getTableByName("songs"));
            for(Trigger trigger : this.registeredTriggers)
                if(!this.songPools.containsKey(trigger)) this.songPools.put(trigger,new ArrayList<>());
            this.titleCards = transitions.getTablesByName("title").stream().filter(table -> table.hasVar("triggers") &&
                            checkTriggerListParameter(table,"title card")).collect(Collectors.toMap(table -> table,
                    this::triggerList));
            this.imageCards = transitions.getTablesByName("image").stream().filter(table -> table.hasVar("triggers"))
                    .filter(table -> {
                        if(!table.hasVar("name")) {
                            MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Image card " +
                                    "is missing a file name and will be skipped!",info.getName());
                            return false;
                        }
                        return checkTriggerListParameter(table,"image card "+
                                table.getValOrDefault("name", "default"));
                    }).collect(Collectors.toMap(table -> table,this::triggerList));
            this.commandMap = commands.getTablesByName("command").stream()
                    .filter(table -> table.hasVar("literal") && table.hasVar("triggers") &&
                            checkTriggerListParameter(table,"command with literal "+
                                    table.getValOrDefault("literal", "literally")))
                    .collect(Collectors.toMap(table -> table.getValOrDefault("literal", "literally"),
                            table -> table.getValOrDefault("triggers", new ArrayList<String>()).stream().distinct()
                                    .map(triggerName -> {
                                        for(Trigger trigger : this.registeredTriggers)
                                            if(trigger.getNameWithID().matches(triggerName))
                                                return trigger;
                                        return null;
                                    }).filter(Objects::nonNull).collect(Collectors.toList())));
            this.toggleList = toggles.getTablesByName("toggle").stream().map(table -> new Toggle(table,this.registeredTriggers))
                    .filter(Toggle::isValid).collect(Collectors.toList());
        }

        private HashMap<String, HashMap<String, Trigger>> parseTriggers(Table triggers) {
            HashMap<String, HashMap<String, Trigger>> ret = new HashMap<>();
            if(Objects.nonNull(triggers)) {
                for (Table trigger : triggers.getChildren().values()) {
                    if(!trigger.getName().matches("universal")) {
                        ret.putIfAbsent(trigger.getName(), new HashMap<>());
                        String id = getIDOrFiller(trigger.getName(), trigger);
                        if (id.matches("missing_id")) MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Trigger " +
                                "{} is missing a required identifier or id parameter and will be skipped!", info.getName(), trigger.getName());
                        else {
                            if (ret.get(trigger.getName()).containsKey(id)) {
                                if (!id.matches("not_accepted"))
                                    MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Identifier {} for trigger {} " +
                                            "has already been defined and cannot be redefined", info.getName(), id, trigger.getName());
                                else MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Trigger {} " +
                                        "has already been defined and cannot be redefined", info.getName(), trigger.getName());
                            } else {
                                Optional<Trigger> createdTrigger = createTrigger(trigger);
                                if (createdTrigger.isPresent()) {
                                    String name = trigger.getName();
                                    ret.get(name).put(id, createdTrigger.get());
                                    registeredTriggers.add(createdTrigger.get());
                                    if(name.matches("snow") || name.matches("home") || name.matches("biome") ||
                                            name.matches("mob") || name.matches("structure") || name.matches("raid"))
                                        serverTriggers.put(createdTrigger.get(),trigger);
                                    logRegister(trigger.getName(), id);
                                }
                            }
                        }
                    }
                }
            }
            this.universalTriggerParameters = Objects.isNull(triggers) ? Optional.empty() : triggers.hasTable("universal") ?
                    Optional.of(triggers.getTableByName("universal")) : Optional.empty();
            return ret;
        }

        private Optional<Trigger> createTrigger(Table triggerTable) {
            Trigger trigger = new Trigger(triggerTable.getName(), info.getName());
            for (Variable parameter : triggerTable.getVars()) {
                if (!Trigger.isParameterAccepted(trigger.getName(),parameter.getName()))
                    MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Parameter {} is not accepted for " +
                            "trigger {} so it will be skipped!", info.getName(), parameter, triggerTable.getName());
                else {
                    if(parameter.getName().matches("resource_name") || parameter.getName().matches("infernal") ||
                            parameter.getName().matches("champion") || parameter.getName().matches("biome_category")) {
                        Optional<List<?>> potentialList = parameter.getAsList();
                        if(potentialList.isPresent()) {
                            List<String> list = potentialList.get().stream().map(Object::toString).collect(Collectors.toList());
                            trigger.setParameter(parameter.getName(), TextUtil.listToString(list,";"));
                        }
                    }
                    else trigger.setParameter(parameter.getName(),parameter.get().toString());
                }
            }
            return trigger.hasAllRequiredParameters() ? Optional.of(trigger) : Optional.empty();
        }

        private void logRegister(String triggerName, String id) {
            if(id.matches("not_accepted"))
                MusicTriggers.logExternally(Level.INFO,"Channel[{}] - Registered trigger {}",
                        info.getName(),triggerName);
            else MusicTriggers.logExternally(Level.INFO,"Channel[{}] - Registered instance of trigger {} with " +
                    "identifier {}", info.getName(),triggerName,id);
        }

        private String getIDOrFiller(String name, Table trigger) {
            if(!Trigger.isParameterAccepted(name,"identifier"))
                return "not_accepted";
            return trigger.getValOrDefault("identifier",trigger.getValOrDefault("id","missing_id"));
        }

        private List<Audio> parseAudio(Table songs) {
            List<Audio> ret = new ArrayList<>();
            if(Objects.nonNull(songs)) {
                for (Table audio : songs.getChildren().values()) {
                    if(!audio.getName().matches("universal")) {
                        Audio potential = new Audio(audio, this.registeredTriggers);
                        if (potential.getTriggers().isEmpty())
                            MusicTriggers.logExternally(Level.WARN, "Channel[{}] - No valid triggers were " +
                                    "registered for audio {} so it has been skipped!",info.getName(),audio.getName());
                        else {
                            ret.add(potential);
                            for (Trigger trigger : potential.getTriggers()) {
                                this.songPools.putIfAbsent(trigger, new ArrayList<>());
                                this.songPools.get(trigger).add(potential);
                                if(trigger.getName().matches("menu") && !this.menuSongs.contains(potential.getName()))
                                    this.menuSongs.add(potential.getName());
                            }
                        }
                    }
                }
            }
            this.universalAudioParameters = Objects.isNull(songs) ? Optional.empty() : songs.hasTable("universal") ?
                    Optional.of(songs.getTableByName("universal")) : Optional.empty();
            return ret;
        }

        private boolean checkTriggerListParameter(Table table, String type) {
            String cap = type.substring(0, 1).toUpperCase() + type.substring(1);
            List<String> triggers = table.getValOrDefault("triggers", new ArrayList<>());
            if(triggers.isEmpty()) {
                MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - {}  " +
                        "needs to be assigned to 1 or more triggers to be parsed correctly!",info.getName(),cap);
                return false;
            }
            for(String trigger : triggers) {
                if(!this.registeredTriggers.stream().map(Trigger::getNameWithID).toList().contains(trigger)) {
                    MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Trigger {} for {} " +
                            "did not exist! Command will be skipped.",info.getName(),trigger,type);
                    return false;
                }
            }
            return true;
        }

        private List<Trigger> triggerList(Table table) {
            return table.getValOrDefault("triggers", new ArrayList<String>()).stream().map(name -> {
                for(Trigger trigger : this.registeredTriggers)
                    if(trigger.getNameWithID().matches(name))
                        return trigger;
                return null;
            }).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        }

        private boolean canPlayTitle(Table table, boolean vague) {
            return vague ? new HashSet<>(picker.getInfo().getPlayableTriggers()).containsAll(titleCards.get(table)) :
                    new HashSet<>(picker.getInfo().getActiveTriggers()).containsAll(titleCards.get(table));
        }

        private boolean canPlayImage(Table table, boolean vague) {
            return vague ? new HashSet<>(picker.getInfo().getPlayableTriggers()).containsAll(imageCards.get(table)) :
                    new HashSet<>(picker.getInfo().getActiveTriggers()).containsAll(imageCards.get(table));
        }
    }
}
