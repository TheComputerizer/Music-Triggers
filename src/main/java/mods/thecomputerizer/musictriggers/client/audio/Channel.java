package mods.thecomputerizer.musictriggers.client.audio;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.Pcm16AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.client.MusicPicker;
import mods.thecomputerizer.musictriggers.client.data.Audio;
import mods.thecomputerizer.musictriggers.client.data.Toggle;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.instance.*;
import mods.thecomputerizer.musictriggers.common.ServerData;
import mods.thecomputerizer.musictriggers.config.ConfigJukebox;
import mods.thecomputerizer.musictriggers.config.ConfigRedirect;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Variable;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.TomlUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.FileResourcePack;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@SideOnly(value = Side.CLIENT)
public class Channel {
    public static final KeyBinding GUI = new KeyBinding("key.musictriggers.gui", Keyboard.KEY_R, "key.categories.musictriggers");
    private static final AudioDataFormat FORMAT = new Pcm16AudioDataFormat(2, 48000, 960, true);
    private final Table info;
    private final SoundCategory category;
    private final Data data;
    private final ConfigRedirect redirect;
    private final ConfigJukebox jukebox;
    private final MusicPicker picker;
    private final boolean canBePausedByJukeBox;
    private final boolean overrides;
    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final HashMap<String, AudioTrack> loadedTracks;
    private ClientSync sync;
    private final List<String> commandsForPacket;
    private final List<String> erroredSongDownloads;
    private final String localFolderPath;

    private boolean fadingIn = false;
    private boolean fadingOut = false;
    private boolean reverseFade = false;
    private int tempFadeIn = 0;
    private int tempFadeOut = 0;
    private int savedFadeOut = 0;
    private float saveVolIn = 1;
    private float saveVolOut = 1;
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
        this.category = SoundCategory.getSoundCategoryNames().contains(category) ?
                SoundCategory.getByName(category) : SoundCategory.MUSIC;
        this.canBePausedByJukeBox = info.getValOrDefault("paused_by_jukebox",true);
        this.overrides = info.getValOrDefault("overrides_normal_music",true);
        this.sync = new ClientSync(info.getName());
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(this.playerManager);
        AudioSourceManagers.registerLocalSource(this.playerManager);
        this.player = this.playerManager.createPlayer();
        this.player.setVolume(100);
        new ChannelListener(this.player, FORMAT, this);
        this.loadedTracks = new HashMap<>();
        this.playerManager.setFrameBufferDuration(1000);
        this.playerManager.setPlayerCleanupThreshold(Long.MAX_VALUE);
        this.playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        this.playerManager.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
        this.playerManager.getConfiguration().setOutputFormat(FORMAT);
        this.oncePerTrigger = new ArrayList<>();
        this.onceUntilEmpty = new ArrayList<>();
        this.commandsForPacket = new ArrayList<>();
        this.erroredSongDownloads = new ArrayList<>();
        this.playingTriggers = new ArrayList<>();
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
        MusicTriggers.logExternally(Level.INFO, "Registered sound engine channel "+ info.getName());
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

    public SoundCategory getCategory() {
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
            for (Trigger trigger : this.picker.boolMap.keySet()) {
                this.picker.startMap.putIfAbsent(trigger, new MutableInt(0));
                if (this.picker.boolMap.get(trigger)) this.picker.startMap.get(trigger).increment();
                else this.picker.startMap.put(trigger, new MutableInt(0));
            }
            for (Trigger trigger : this.picker.triggerPersistence.keySet()) {
                this.picker.triggerPersistence.putIfAbsent(trigger, new MutableInt(0));
                if (this.picker.triggerPersistence.get(trigger).getValue() > 0)
                    this.picker.triggerPersistence.get(trigger).decrement();
            }
            checkLoops();
            float calculatedVolume = saveVolIn;
            if (fadingIn && !fadingOut) {
                reverseFade = false;
                if (tempFadeIn == 0) fadingIn = false;
                else {
                    calculatedVolume = saveVolIn * (float) (((double) (this.picker.fadeIn - tempFadeIn)) / ((double) this.picker.fadeIn));
                    tempFadeIn -= 1;
                }
            }
            if (fadingOut && !reverseFade) {
                tempFadeIn = 0;
                fadingIn = false;
                if (tempFadeOut == 0) clearSongs();
                else {
                    if (getCurPlaying() == null) tempFadeOut = 0;
                    else {
                        calculatedVolume = saveVolOut * (float) (((double) tempFadeOut) / ((double) savedFadeOut));
                        tempFadeOut -= 1;
                        if (!this.picker.getInfo().songListChanged()) reverseFade = true;
                    }
                }
            } else if (fadingOut) {
                if (tempFadeOut >= savedFadeOut) {
                    fadingOut = false;
                    reverseFade = false;
                    calculatedVolume = saveVolOut / getChannelVolume();
                    tempFadeOut = 0;
                } else {
                    calculatedVolume = saveVolOut * (float) (((double) tempFadeOut) / ((double) savedFadeOut));
                    tempFadeOut += 1;
                }
            }
            setVolume(calculatedVolume);
            if (delayCounter > 0) delayCounter -= 1;
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
        if(checkAudio() && !this.data.registeredAudio.isEmpty()) {
            this.picker.querySongList(this.data.universalTriggerParameters);
            if (!isPlaying()) {
                if(!this.picker.getInfo().getActiveTriggers().isEmpty()) {
                    if(this.playingTriggers.isEmpty()) {
                        this.delayCounter = MusicTriggers.randomInt("trigger_delay", this.picker.triggerDelay, 0);
                        this.delayCatch = true;
                        this.playingTriggers.addAll(this.picker.getInfo().getActiveTriggers());
                        onTriggerStart();
                    }
                    if(this.playingTriggers.equals(this.picker.getInfo().getActiveTriggers())) {
                        if(!this.delayCatch) {
                            this.delayCounter = MusicTriggers.randomInt("song_delay", this.picker.songDelay, 0);
                            this.delayCatch = true;
                        }
                        if (this.delayCounter <= 0) {
                            Audio audio = getWeightedAudio();
                            if (Objects.nonNull(audio)) {
                                MusicTriggers.logExternally(Level.INFO, "Attempting to play track \"{}\"", audio.getName());
                                this.saveVolIn = audio.getVolume();
                                float pitch = audio.getPitch();
                                setPitch(pitch);
                                this.tempFadeIn = this.picker.fadeIn;
                                setVolume(this.saveVolIn);
                                playTrack(audio, 0);
                                delayCatch = false;
                                if(audio.getPlayOnce()==1) this.onceUntilEmpty.add(audio);
                                if(audio.getPlayOnce()==2) this.oncePerTrigger.add(audio);
                            } else MusicTriggers.logExternally(Level.INFO, "Audio was null!");
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
            } else if(!this.fadingOut && !this.fadingIn && Objects.nonNull(this.curTrack)) {
                if (!this.curTrack.mustFinish()
                        && (!this.playingTriggers.equals(this.picker.getInfo().getActiveTriggers())
                        || this.picker.getInfo().getActiveTriggers().isEmpty())) {
                    if (this.picker.getInfo().getCurrentSongList().contains(this.curTrack)) {
                        this.playingTriggers.clear();
                        this.playingTriggers.addAll(this.picker.getInfo().getActiveTriggers());
                    } else stopTrack(true);
                }
            } else if(Objects.isNull(this.curTrack)) stopTrack(true);
            for (Trigger playable : this.picker.getInfo().getPlayableTriggers()) {
                if (!this.picker.getInfo().getActiveTriggers().contains(playable))
                    if (playable.getParameterBool("toggle_inactive_playable"))
                        playable.setToggle(false);
            }
        } else clearSongs();
    }

    private void onTriggerStart() {
        this.commandsForPacket.clear();
        for (String command : this.data.commandMap.keySet()) {
            if (this.data.commandMap.get(command).equals(this.picker.getInfo().getActiveTriggers()))
                this.commandsForPacket.add(command);
        }
        renderCards();
    }

    //Returns null if a no song is found
    private Audio getWeightedAudio() {
        List<Audio> curSongs = this.picker.getInfo().getCurrentSongList().stream()
                .filter(audio -> !this.oncePerTrigger.contains(audio) && !this.onceUntilEmpty.contains(audio))
                .collect(Collectors.toList());
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
        MusicTriggers.logExternally(Level.DEBUG, "Finding cards to render");
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
            ret = formatMinutes((int)(getMillis()/1000f))+"/"+formatMinutes((int)(getTotalMillis()/1000f));
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
        if(isPlaying()) {
            ret = formattedTimeFromMilliseconds(getMillis());
        }
        return ret;
    }

    public String formattedFadeInTime() {
        if(fadingIn) return formattedTimeFromMilliseconds(tempFadeIn);
        return null;
    }

    public String formattedFadeOutTime() {
        if(fadingOut) return formattedTimeFromMilliseconds(tempFadeOut);
        return null;
    }

    public String formattedTimeFromMilliseconds(float milliseconds) {
        if (milliseconds == -1) milliseconds = 0;
        float seconds = milliseconds / 1000f;
        if (seconds % 60 < 10) return (int) (seconds / 60) + ":0" + (int) (seconds % 60) + formatMilliseconds(milliseconds);
        else return (int) (seconds / 60) + ":" + (int) (seconds % 60) + formatMilliseconds(milliseconds);
    }

    private String formatMilliseconds(float milliseconds) {
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
        return Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MASTER) > 0
                && Minecraft.getMinecraft().gameSettings.getSoundLevel(this.category) > 0;
    }

    public long getTotalMillis() {
        return getCurPlaying().getDuration();
    }

    public long getMillis() {
        return getCurPlaying().getPosition();
    }

    public void setMillis(long milliseconds) {
        MusicTriggers.logExternally(Level.WARN, "Found loop! Setting track time to {}",milliseconds);
        getCurPlaying().setPosition(milliseconds);
    }

    public void setVolume(float volume) {
        volume = Math.min(volume, 1f); // temporary cap the volume at 100% until the fade issue is fixed
        this.getPlayer().setVolume((int)(volume*getChannelVolume()*100));
    }

    private float getChannelVolume() {
        float master = Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MASTER);
        if(getCategory()==SoundCategory.MASTER) return master;
        else return master*Minecraft.getMinecraft().gameSettings.getSoundLevel(getCategory());
    }

    public void playTrack(Audio audio, long milliseconds) {
        String id = audio.getName();
        AudioTrack track = this.loadedTracks.get(audio.getName());
        MusicTriggers.logExternally(Level.INFO, "Playing track from id "+id+" at a millisecond time of "+milliseconds);
        if(track!=null) {
            try {
                if (!this.getPlayer().startTrack(track, false))
                    MusicTriggers.logExternally(Level.ERROR, "Could not start track!");
                else this.curTrack = audio;
            } catch (IllegalStateException e) {
                AudioTrack cloned = track.makeClone();
                if (!this.getPlayer().startTrack(cloned, false))
                    MusicTriggers.logExternally(Level.ERROR, "Could not start track!");
                else this.curTrack = audio;
            }
        } else {
            MusicTriggers.logExternally(Level.ERROR, "Track with id "+id+" was null! Attempting to refresh track...");
            this.loadedTracks.remove(id);
            if(this.redirect.urlMap.containsKey(id)) loadFromURL(id,this.redirect.urlMap.get(id));
            else if(this.redirect.resourceLocationMap.containsKey(id)) loadFromResourceLocation(id,this.redirect.resourceLocationMap.get(id));
            else {
                MusicTriggers.logExternally(Level.ERROR, "Track with id "+id+" does not seem to exist! All " +
                        "instances using this song will be removed until reloading.");
                this.data.registeredAudio.remove(audio);
                for(List<Audio> pool : this.data.songPools.values()) pool.remove(audio);
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
        if(!shouldFade) this.getPlayer().stopTrack();
        else changeTrack();
    }

    public void parseRedirect(ConfigRedirect redirect) {
        redirect.parse();
        this.erroredSongDownloads.clear();
        for(String id : redirect.urlMap.keySet()) loadFromURL(id,redirect.urlMap.get(id));
        for (String folderPath : ChannelManager.openAudioFiles.keySet()) {
            if (this.localFolderPath.matches(folderPath)) {
                for(File file : ChannelManager.openAudioFiles.get(folderPath)) {
                    String name = FilenameUtils.getBaseName(file.getName());
                    if (!loadedTracks.containsKey(name))
                        loadAudioFile(name, file);
                }
            }
        }
        if(!this.erroredSongDownloads.isEmpty()) MusicTriggers.logExternally(Level.ERROR, "Could not read audio from these sources");
        for(String error : this.erroredSongDownloads) MusicTriggers.logExternally(Level.ERROR, error);
    }

    public void readResourceLocations() {
        for(String id : redirect.resourceLocationMap.keySet()) loadFromResourceLocation(id,redirect.resourceLocationMap.get(id));
    }

    public void addTrackToMap(String id, AudioTrack track) {
        this.loadedTracks.put(id, track);
    }

    private void loadFromURL(String id, String url) {
        this.playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if(!Channel.this.loadedTracks.containsKey(id)) {
                    Channel.this.addTrackToMap(id,track);
                    MusicTriggers.logExternally(Level.INFO, "Track loaded from url "+url);
                } else MusicTriggers.logExternally(Level.WARN, "Audio file with id "+id+" already exists!");
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                MusicTriggers.logExternally(Level.INFO, "Loaded a playlist from "+url);
                for(int i=1;i<playlist.getTracks().size()+1;i++) {
                    if(!Channel.this.loadedTracks.containsKey(id+"_"+i)) {
                        Channel.this.addTrackToMap(id,playlist.getTracks().get(i));
                        MusicTriggers.logExternally(Level.INFO, "Track "+i+" loaded from playlist url "+url);
                    } else MusicTriggers.logExternally(Level.WARN, "Audio file with id "+id+"_"+i+" already exists!");
                }
            }

            @Override
            public void noMatches() {
                MusicTriggers.logExternally(Level.ERROR, "No audio able to be extracted from url "+url);
                Channel.this.erroredSongDownloads.add(id+" -> "+url);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                MusicTriggers.logExternally(Level.INFO, "Load failed! "+url);
                exception.printStackTrace();
            }
        });
    }

    private void loadFromResourceLocation(String id, ResourceLocation source) {
        try {
            if (!this.loadedTracks.containsKey(id)) {
                URL url = null;
                String first = null;
                String second = null;
                if(Minecraft.getMinecraft().defaultResourcePack.resourceExists(source)) {
                    url = DefaultResourcePack.class.getResource("/assets/" + source.getNamespace() + "/" + source.getPath());
                    if(url!=null) {
                        first = url.toURI().toString();
                        second = url.getFile();
                    } else {
                        File file = Minecraft.getMinecraft().defaultResourcePack.resourceIndex.getFile(source);
                        if(file!=null && file.isFile()) {
                            first = file.getAbsolutePath();
                            second = file.getName();
                        }
                    }
                }
                if(url==null && first==null) {
                    for (IResourcePack resourcePack : Minecraft.getMinecraft().defaultResourcePacks) {
                        if (!(resourcePack instanceof DefaultResourcePack)) {
                            String resourceName = String.format("%s/%s/%s", "assets", source.getNamespace(), source.getPath());
                            if(resourcePack instanceof FolderResourcePack) {
                                File temp = new File(((FolderResourcePack)resourcePack).resourcePackFile,resourceName);
                                if(temp.isFile()) {
                                    first = temp.getAbsolutePath();
                                    second = temp.getName();
                                }
                            } else if (resourcePack instanceof FileResourcePack) {
                                //TODO
                                //ZipFile file = ((FileResourcePack)resourcePack).getResourcePackZipFile();
                                //ZipEntry entry = file.getEntry(resourceName);
                            }
                        }
                    }
                }
                if(first!=null && second!=null) {
                    this.playerManager.loadItem(new AudioReference(first, second), new AudioLoadResultHandler() {
                        @Override
                        public void trackLoaded(AudioTrack track) {
                            Channel.this.addTrackToMap(id, track);
                            MusicTriggers.logExternally(Level.INFO, "Track loaded from resource location " + source);
                        }

                        @Override
                        public void playlistLoaded(AudioPlaylist playlist) {
                            MusicTriggers.logExternally(Level.INFO, "no playlists here");
                        }

                        @Override
                        public void noMatches() {
                            MusicTriggers.logExternally(Level.INFO, "no matches from resource location " + source);
                        }

                        @Override
                        public void loadFailed(FriendlyException exception) {
                            MusicTriggers.logExternally(Level.INFO, "Track loaded failed resource location " + source);
                        }
                    });
                } else MusicTriggers.logExternally(Level.WARN, "Failed to get URI for resource location "+source);
            } else MusicTriggers.logExternally(Level.WARN, "Audio file with id " + id + " already exists!");
        } catch (Exception e) {
            MusicTriggers.logExternally(Level.ERROR, "Could not decode track from resource location {}! See the main log for the full error",source);
            Constants.MAIN_LOG.error("Could not decode track from resource location",e);
            e.printStackTrace();
        }
    }

    private void loadAudioFile(String id, File file) {
        try {
            this.playerManager.loadItem(new AudioReference(file.getPath(), file.getName()), new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    if (!Channel.this.loadedTracks.containsKey(id)) {
                        Channel.this.addTrackToMap(id, track);
                        MusicTriggers.logExternally(Level.INFO, "Track loaded from file " + file.getName());
                    } else MusicTriggers.logExternally(Level.WARN, "Audio file with id " + id + " already exists!");
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    MusicTriggers.logExternally(Level.INFO, "Loaded track from file " + file.getName());
                    for (int i = 1; i < playlist.getTracks().size() + 1; i++) {
                        if (!Channel.this.loadedTracks.containsKey(id + "_" + i)) {
                            Channel.this.addTrackToMap(id, playlist.getTracks().get(i));
                            MusicTriggers.logExternally(Level.INFO, "Track " + i + " loaded from playlist file " + file.getName());
                        } else MusicTriggers.logExternally(Level.WARN, "Audio file with id " + id + "_" + i + " already exists!");
                    }
                }

                @Override
                public void noMatches() {
                    MusicTriggers.logExternally(Level.ERROR, "No audio able to be extracted from file " + file.getName());
                    Channel.this.erroredSongDownloads.add(id + " -> " + file.getName());
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    MusicTriggers.logExternally(Level.INFO, "Load failed! " + file.getName());
                    exception.printStackTrace();
                }
            });
        } catch (Exception e) {
            MusicTriggers.logExternally(Level.ERROR, "Could not load track from file {}! See the main log for the full error",id);
            Constants.MAIN_LOG.error("Could not load track from file",e);
            e.printStackTrace();
        }
    }

    public void initializeServerData(ServerData serverData) {
        serverData.addChannelInfo(getChannelName(),this.data.serverTriggers,this.data.menuSongs);
    }

    public void encodeDynamic(ByteBuf buf) {
        NetworkUtil.writeString(buf,getChannelName());
        NetworkUtil.writeGenericList(buf,this.commandsForPacket,NetworkUtil::writeString);
        boolean playingSong = Objects.nonNull(this.curTrack);
        buf.writeBoolean(playingSong);
        if(playingSong) {
            NetworkUtil.writeString(buf,this.curTrack.getName());
            NetworkUtil.writeString(buf,this.picker.getInfo().highestPriorityActive().getName());
        }
        this.changedStatus = false;
    }

    public void sync(ClientSync fromServer) {
        this.sync = fromServer;
    }

    private void changeTrack() {
        if (!fadingOut) {
            fadingOut = true;
            tempFadeOut = this.picker.fadeOut;
        } else if (reverseFade) reverseFade = false;
    }

    private void clearSongs() {
        stopTrack(false);
        oncePerTrigger.clear();
        onceUntilEmpty.clear();
        this.fadingOut = false;
        curTrack = null;
        tempFadeIn = this.picker.fadeIn;
    }

    public void parseConfigs(boolean startup) {
        parseRedirect(this.redirect);
        if(!startup) readResourceLocations();
        this.jukebox.parse();
    }

    public Trigger getSimpleTrigger(String name) {
        return this.data.registeredTriggerMap.get(name).get("not_accepted");
    }

    public List<Trigger> getRegisteredTriggers() {
        return this.data.registeredTriggers;
    }

    public List<Audio> getSongPool(Trigger trigger) {
        return this.data.songPools.get(trigger);
    }

    public ChannelInstance createGuiData() {
        return new ChannelInstance(this.info,new Main(getChannelName(),this.data.main),
                new Transitions(getChannelName(),this.data.transitions),new Commands(getChannelName(),this.data.commands),
                new Toggles(getChannelName(),this.data.toggles),
                new Redirect(getChannelName(),this.redirect.urlMap,this.redirect.resourceLocationMap),
                new Jukebox(getChannelName(),this.getRecordMap()));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    class Data {
        private final Holder main;
        private final Holder transitions;
        private final Holder commands;
        private final Holder toggles;
        private final HashMap<String, HashMap<String, Trigger>> registeredTriggerMap;
        private final List<Trigger> registeredTriggers;
        private final List<Table> serverTriggers;
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
            this.serverTriggers = new ArrayList<>();
            this.registeredTriggerMap = parseTriggers(main.getTableByName("triggers"));
            this.menuSongs = new ArrayList<>();
            this.songPools = new HashMap<>();
            this.registeredAudio = parseAudio(main.getTableByName("songs"));
            this.titleCards = transitions.getTablesByName("title").stream().filter(table -> table.hasVar("triggers") &&
                            checkTriggerListParameter(table,"title card")).collect(Collectors.toMap(table -> table,
                    this::triggerList));
            this.imageCards = transitions.getTablesByName("image").stream().filter(table -> table.hasVar("triggers"))
                    .filter(table -> {
                        if(!table.hasVar("name")) {
                            MusicTriggers.logExternally(Level.ERROR, "Image card in " +
                                    "channel {} is missing a file name and will be skipped!",info.getName());
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
                                    if(name.matches("home") || name.matches("mob") || name.matches("structure"))
                                        serverTriggers.add(trigger);
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
                            parameter.getName().matches("champion")) {
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
                            MusicTriggers.logExternally(Level.WARN, "No valid triggers were registered for audio {} so " +
                                    "it has been skipped!", audio.getName());
                        else {
                            ret.add(potential);
                            for (Trigger trigger : potential.getTriggers()) {
                                this.songPools.putIfAbsent(trigger, new ArrayList<>());
                                this.songPools.get(trigger).add(potential);
                            }
                            if(potential.getTriggers().stream().anyMatch(trigger -> trigger.getName().matches("menu")))
                                this.menuSongs.add(potential.getName());
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
                MusicTriggers.logExternally(Level.ERROR, "{} in channel {} " +
                        "needs to be assigned to 1 or more triggers to be parsed correctly!",cap,info.getName());
                return false;
            }
            for(String trigger : triggers) {
                if(!this.registeredTriggers.stream().map(Trigger::getNameWithID)
                        .collect(Collectors.toList()).contains(trigger)) {
                    MusicTriggers.logExternally(Level.ERROR, "Trigger {} for {} " +
                            "in channel {} did not exist! Command will be skipped.", trigger,type,info.getName());
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
