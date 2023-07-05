package mods.thecomputerizer.musictriggers.client.channels;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.client.MusicPicker;
import mods.thecomputerizer.musictriggers.client.data.Audio;
import mods.thecomputerizer.musictriggers.client.data.Toggle;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.instance.*;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.musictriggers.config.ConfigJukebox;
import mods.thecomputerizer.musictriggers.config.ConfigRedirect;
import mods.thecomputerizer.musictriggers.server.channels.ServerTriggerStatus;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Variable;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.TomlUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@SideOnly(value = Side.CLIENT)
public class Channel implements IChannel {
    public static final KeyBinding GUI = new KeyBinding("key.musictriggers.gui", Keyboard.KEY_R, "key.categories.musictriggers");
    private final Table info;
    private final SoundCategory category;
    private final Data data;
    private final ConfigRedirect redirect;
    private final ConfigJukebox jukebox;
    private final MusicPicker picker;
    private final boolean canBePausedByJukeBox;
    private final boolean overrides;
    private final boolean pausesOverrides;
    private final boolean explicitlyOverrides;
    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final ChannelListener listener;
    private final HashMap<String, AudioTrack> loadedTracks;
    private final HashMap<String, String> loadedTrackTypes;
    private final ClientSync sync;
    private final List<String> commandsForPacket;
    private final List<String> erroredSongDownloads;
    private final String localFolderPath;
    private final AtomicInteger AUDIO_QUEUE;
    private final boolean isResourcePack;

    private boolean triggerStarted;
    private boolean fadingIn = false;
    private boolean fadingOut = false;
    private boolean reverseFade = false;
    private int tempFadeIn = 0;
    private int tempFadeOut = 0;
    private int savedFadeOut = 0;
    private Audio prevTrack;
    private Audio curTrack;
    private final List<Audio> oncePerTrigger;
    private final List<Audio> onceUntilEmpty;
    private int delayCounter = 0;
    private boolean delayCatch = false;
    private final HashSet<Trigger> playingTriggers;
    private boolean pausedByJukebox = false;
    private boolean changedStatus = false;
    private boolean changedStorageStatus = false;
    private final Map<String,Boolean> toggleStorage;
    private final List<Audio> playedOnce;
    private int audioCounter = 0;
    private long lastKnownSongTime = 0;
    private final Map<Integer,HashSet<Trigger>> futureToggles;
    private boolean isToggled = true;
    private boolean frozen = false;

    private static String getFilePath(Table info, String type) {
        return info.getValOrDefault(type,info.getName()+"/"+type);
    }

    private static Holder makeTomlHolder(Table info, String type, boolean isResourcePack) throws IOException{
        String path = getFilePath(info,type);
        return isResourcePack ? TomlUtil.readFully(Minecraft.getMinecraft().getResourceManager().getResource(
                MusicTriggers.res("config/"+path+".toml")).getInputStream()) :
                TomlUtil.readFully(MusicTriggers.configFile(path,"toml"));
    }

    private static ConfigRedirect makeRedirect(Table info, boolean isResourcePack) {
        String path = getFilePath(info,"redirect");
        return isResourcePack ? new ConfigRedirect(MusicTriggers.res("config/"+path+".txt")) :
                new ConfigRedirect(MusicTriggers.configFile(path,"txt"));
    }

    private static ConfigJukebox makeJukebox(Table info, boolean isResourcePack) {
        String path = getFilePath(info,"jukebox");
        return isResourcePack ? new ConfigJukebox(MusicTriggers.res("config/"+path+".txt")) :
                new ConfigJukebox(MusicTriggers.configFile(path,"txt"));
    }

    /**
     * Client Config
     */
    public Channel(Table info, boolean isResourcePack) throws IOException {
        this(info,isResourcePack,makeTomlHolder(info,"main",isResourcePack),
                makeTomlHolder(info,"transitions",isResourcePack),
                makeTomlHolder(info,"commands",isResourcePack),makeTomlHolder(info,"toggles",isResourcePack),
                makeRedirect(info,isResourcePack),makeJukebox(info,isResourcePack));
    }

    /**
     * Server Config
     */
    public Channel(Table info, boolean isResourcePack, Holder main, Holder transitions, Holder commands, Holder toggles,
                   ConfigRedirect redirect, ConfigJukebox jukebox) throws IOException {
        for(String filePath : collectFilePaths(info))
            if(!ChannelManager.verifyOtherFilePath(filePath))
                throw new IOException("Config path in channel "+info.getName()+" cannot be "+filePath+" as that " +
                        "matches the path of a config file in an already registered channel!");
        this.isResourcePack = isResourcePack;
        this.info = info;
        String category = info.getValOrDefault("sound_category","music");
        this.category = SoundCategory.getSoundCategoryNames().contains(category) ?
                SoundCategory.getByName(category) : SoundCategory.MUSIC;
        this.canBePausedByJukeBox = info.getValOrDefault("paused_by_jukebox",true);
        this.overrides = info.getValOrDefault("overrides_normal_music",true);
        this.pausesOverrides = info.getValOrDefault("pause_overrides",false);
        this.explicitlyOverrides = info.getValOrDefault("explicit_overrides",false);
        this.sync = new ClientSync(info.getName());
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(this.playerManager);
        AudioSourceManagers.registerLocalSource(this.playerManager);
        this.player = this.playerManager.createPlayer();
        this.player.setVolume(100);
        this.listener = new ChannelListener(this);
        this.loadedTracks = new HashMap<>();
        this.loadedTrackTypes = new HashMap<>();
        this.toggleStorage = new HashMap<>();
        this.futureToggles = new HashMap<>();
        this.playerManager.setFrameBufferDuration(1000);
        this.playerManager.setPlayerCleanupThreshold(Long.MAX_VALUE);
        String resamplingQuality = ConfigDebug.RESAMPLING_QUALITY.toUpperCase();
        this.playerManager.getConfiguration().setResamplingQuality(EnumUtils.isValidEnum(
                AudioConfiguration.ResamplingQuality.class,resamplingQuality) ?
                AudioConfiguration.ResamplingQuality.valueOf(resamplingQuality) :
                AudioConfiguration.ResamplingQuality.HIGH);
        this.playerManager.getConfiguration().setOpusEncodingQuality(ConfigDebug.ENCODING_QUALITY);
        this.playerManager.getConfiguration().setOutputFormat(StandardAudioDataFormats.DISCORD_PCM_S16_BE);
        this.oncePerTrigger = new ArrayList<>();
        this.onceUntilEmpty = new ArrayList<>();
        this.commandsForPacket = new ArrayList<>();
        this.erroredSongDownloads = new ArrayList<>();
        this.playingTriggers = new HashSet<>();
        this.playedOnce = new ArrayList<>();
        MusicTriggers.logExternally(Level.INFO, "Registered sound engine channel "+ info.getName());
        this.data = new Data(main,transitions,commands,toggles);
        this.redirect = redirect;
        this.jukebox = jukebox;
        this.picker = new MusicPicker(this);
        this.localFolderPath = info.getValOrDefault("songs_folder", "config/MusicTriggers/songs");
        File file = new File(this.localFolderPath);
        if(!file.exists()) file.mkdirs();
        this.AUDIO_QUEUE = new AtomicInteger();
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

    @Override
    public String getChannelName() {
        return this.info.getName();
    }

    public ClientSync getSyncStatus() {
        return this.sync;
    }

    public boolean needsUpdatePacket() {
        return this.changedStatus || this.changedStorageStatus;
    }

    public boolean canOverrideMusic() {
        if(!isPlaying() || !this.overrides) return false;
        return getOverrideCategories().contains(SoundCategory.MUSIC);
    }

    public boolean getOverrideStatus(ISound sound) {
        if(!isPlaying() || !this.overrides) return false;
        for(SoundCategory category : getOverrideCategories())
            if(sound.getCategory()==category) return true;
        return false;
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    public void setChannelToggle(String state) {
        this.isToggled = state.matches("switch") ? !this.isToggled : Boolean.parseBoolean(state);
    }

    public void updateFutureToggles(int condition, HashSet<Trigger> toToggle) {
        this.futureToggles.put(condition,toToggle);
    }

    public Map<Channel,Map<String,HashSet<Trigger>>> getToggleTargets() {
        Map<Channel,Map<String,HashSet<Trigger>>> ret = new HashMap<>();
        for(Map.Entry<Integer,HashSet<Trigger>> toggleSets : this.futureToggles.entrySet()) {
            for (Toggle toggle : this.data.toggleList) {
                Map<Channel,Tuple<String,List<Trigger>>> targetMap = toggle.getTargets(toggleSets.getKey(),toggleSets.getValue());
                for (Map.Entry<Channel, Tuple<String, List<Trigger>>> targetEntry : targetMap.entrySet()) {
                    ret.putIfAbsent(targetEntry.getKey(), new HashMap<>());
                    Tuple<String, List<Trigger>> targetTuple = targetEntry.getValue();
                    ret.get(targetEntry.getKey()).putIfAbsent(targetTuple.getFirst(), new HashSet<>());
                    ret.get(targetEntry.getKey()).get(targetTuple.getFirst()).addAll(targetTuple.getSecond());
                }
            }
        }
        return ret;
    }

    public void runToggles(Map<String,HashSet<Trigger>> targetConditions) {
        HashSet<Trigger> toggledOn = new HashSet<>();
        for(Map.Entry<String,HashSet<Trigger>> conditionEntry : targetConditions.entrySet()) {
            if(conditionEntry.getKey().matches("switch")) {
                for(Trigger trigger : conditionEntry.getValue()) {
                    boolean state = !trigger.isToggled();
                    trigger.setToggle(state,true);
                    if(state) toggledOn.add(trigger);
                }
                continue;
            }
            boolean state = Boolean.parseBoolean(conditionEntry.getKey());
            for(Trigger trigger : conditionEntry.getValue()) {
                trigger.setToggle(state,true);
                if(state) toggledOn.add(trigger);
            }
        }
        if(!toggledOn.isEmpty()) updateFutureToggles(1,toggledOn);
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
            this.picker.stopMap.entrySet().removeIf(entry -> entry.getValue().decrementAndGet()<=0);
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
            if(this.isToggled) this.picker.querySongList(this.data.universalTriggerParameters);
            else this.picker.skipQuery();
            if (!isPlaying()) {
                if(!this.isToggled) {
                    this.frozen = true;
                    return;
                }
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
                                this.listener.setPitch(audio.getPitch());
                                if(this.triggerStarted) this.tempFadeIn = this.picker.fadeIn;
                                this.triggerStarted = false;
                                playTrack(audio);
                                this.delayCatch = false;
                            }
                        }
                    } else {
                        this.delayCounter = MusicTriggers.randomInt("trigger_delay", this.picker.triggerDelay, 0);
                        this.delayCatch = true;
                        for(Trigger trigger : this.playingTriggers) {
                            if (!this.picker.getInfo().getActiveTriggers().contains(trigger)) {
                                int stopDelay = trigger.getParameterInt("stop_delay");
                                stopDelay = stopDelay > 0 ? stopDelay : this.data.universalTriggerParameters.map(table ->
                                        MusicTriggers.randomInt("universal_stop_delay", table.getValOrDefault(
                                                "stop_delay", "0"), 0)).orElse(0);
                                if(stopDelay>0) this.picker.stopMap.put(trigger, new MutableInt(stopDelay));
                            }
                        }
                        this.playingTriggers.clear();
                        this.playingTriggers.addAll(this.picker.getInfo().getActiveTriggers());
                        onTriggerStart();
                    }
                } else {
                    if(Objects.nonNull(this.curTrack)) this.prevTrack = this.curTrack;
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
            for(Trigger playable : this.picker.getInfo().getPlayableTriggers()) {
                if(!this.picker.getInfo().getActiveTriggers().contains(playable))
                    if(playable.getParameterBool("toggle_inactive_playable"))
                        playable.setToggle(false,true);
            }
        } else clearSongs();
    }

    private void onTriggerStart() {
        this.oncePerTrigger.clear();
        this.onceUntilEmpty.clear();
        this.commandsForPacket.clear();
        for(String command : this.data.commandMap.keySet()) {
            if(new HashSet<>(this.picker.getInfo().getActiveTriggers()).containsAll(this.data.commandMap.get(command)))
                this.commandsForPacket.add(command);
        }
        renderCards();
        this.savedFadeOut = this.picker.fadeOut;
        this.tempFadeOut = this.picker.fadeOut;
        this.triggerStarted = true;
        setVolume(0.01f*this.getChannelVolume());
        this.fadingIn = true;
        this.audioCounter = 0;
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
        curSongs.removeIf(audio -> Objects.nonNull(this.prevTrack) && audio==this.prevTrack);
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

    public void renderCards() {
        MusicTriggers.logExternally(Level.DEBUG, "Channel[{}] - Finding cards to render",getChannelName());
        for (Table table : this.data.titleCards.keySet())
            if(this.data.canPlayTitle(table,table.getValOrDefault("vague",false))) {
                synchronized(ChannelManager.TICKING_RENDERABLES) {
                    ChannelManager.addRenderable(true,table);
                }
            }
        for (Table table : this.data.imageCards.keySet())
            if(this.data.canPlayImage(table,table.getValOrDefault("vague",false))) {
                synchronized(ChannelManager.TICKING_RENDERABLES) {
                    ChannelManager.addRenderable(false,table);
                }
            }
    }

    public String formatPlayback() {
        String ret = "No song playing";
        if(isPlaying()) {
            ret = formatMinutes((int) (getMillis() / 1000f))+" / ";
            ret+=isPlayingSeekable() ? formatMinutes((int) (getTotalMillis() / 1000f)) : "NONSEEK";
        }
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
            ret = formattedTimeFromTicks(((float)getMillis())/50f)+" / ";
            ret+=isPlayingSeekable() ? formatMinutes((int) (getTotalMillis() / 1000f)) : "NONSEEK";
        }
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

    @Override
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
        return Objects.nonNull(getCurPlaying());
    }

    public boolean isPlayingSeekable() {
        AudioTrack track = getCurPlaying();
        return Objects.nonNull(track) && track.isSeekable();
    }

    private boolean checkAudio() {
        return Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MASTER) > 0
                && Minecraft.getMinecraft().gameSettings.getSoundLevel(this.category) > 0 && this.AUDIO_QUEUE.get()<=0;
    }

    public long getTotalMillis() {
        return getCurPlaying().getInfo().length;
    }

    public long getMillis() {
        return getCurPlaying().getPosition();
    }

    public void setMillis(long milliseconds) {
        MusicTriggers.logExternally(Level.INFO, "Setting track time to {}",milliseconds);
        getCurPlaying().setPosition(milliseconds);
    }

    public void setVolume(float volume) {
        this.getPlayer().setVolume((int)(volume*100));
    }

    private float getChannelVolume() {
        float master = Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MASTER);
        if(getCategory()==SoundCategory.MASTER) return master;
        else return master*Minecraft.getMinecraft().gameSettings.getSoundLevel(getCategory());
    }

    public void resetTrack() {
        if(isPlaying()) {
            MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Attempting to reset currently playing track",
                    getChannelName());
            AudioTrack cloned = this.getPlayer().getPlayingTrack().makeClone();
            if(Objects.nonNull(this.curTrack)) cloned.setPosition(this.curTrack.getMilliStart());
            if (!this.getPlayer().startTrack(cloned, false))
                MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Could not reset track!",getChannelName());
        }
    }

    public void playTrack(Audio audio) {
        String id = audio.getName();
        AudioTrack track = this.loadedTracks.get(audio.getName());
        if(Objects.nonNull(track)) {
            try {
                if (!this.getPlayer().startTrack(track, false))
                    MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Could not start track!",
                            getChannelName());
                else {
                    if(Objects.nonNull(this.curTrack)) this.prevTrack = this.curTrack;
                    track.setPosition(audio.getResumeTime());
                    this.curTrack = audio;
                }
                MusicTriggers.logExternally(Level.DEBUG, "Channel[{}] - Track with id {} is seekable: {}",
                        getChannelName(),id,track.isSeekable());
            } catch (IllegalStateException e) {
                AudioTrack cloned = track.makeClone();
                if (!this.getPlayer().startTrack(cloned, false))
                    MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Could not start track!",
                            getChannelName());
                else {
                    if(Objects.nonNull(this.curTrack)) this.prevTrack = this.curTrack;
                    track.setPosition(audio.getResumeTime());
                    this.curTrack = audio;
                }
                MusicTriggers.logExternally(Level.DEBUG, "Channel[{}] - Track with id {} is seekable: {}",
                        getChannelName(),id,track.isSeekable());
            }
        } else {
            MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Track with id {} was null! Attempting to " +
                    "refresh track...",getChannelName(),id);
            String type = this.loadedTrackTypes.get(id);
            type = type.substring(0,type.indexOf("["));
            this.loadedTracks.remove(id);
            this.loadedTrackTypes.remove(id);
            if(type.matches("url")) loadFromURL(id,this.redirect.urlMap.get(id),audio);
            else if(type.matches("resource"))
                loadFromResourceLocation(id,this.redirect.resourceLocationMap.get(id),audio);
            else if(type.matches("file")) {
                boolean foundFile = false;
                for(File file : ChannelManager.OPEN_AUDIO_FILES.get(this.localFolderPath)) {
                    if (FilenameUtils.getBaseName(file.getName()).matches(id)) {
                        loadAudioFile(id,file,audio);
                        foundFile = true;
                        break;
                    }
                }
                if(!foundFile) {
                    MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Track with id {} does not seem to " +
                            "exist! All instances using this song will be removed until reloading.",getChannelName(),id);
                    unregisterAudio(audio);
                }
            } else {
                MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Track with id {} was registered as " +
                        "unknown type {}! All instances using this audio will be removed until reloading.",
                        getChannelName(),id,type);
                unregisterAudio(audio);
            }
        }
    }

    public HashSet<SoundCategory> getOverrideCategories() {
        return this.explicitlyOverrides ? new HashSet<>(Collections.singletonList(getCategory())) :
                ChannelManager.getInterrputedCategories();
    }

    @Override
    public void onTrackStart() {
        ChannelManager.handleAudioStart(this.pausesOverrides,getOverrideCategories());
        if(Objects.nonNull(this.curTrack)) {
            int playOnce = this.curTrack.getPlayOnce();
            if (playOnce == 1) this.onceUntilEmpty.add(this.curTrack);
            else if (playOnce == 2) this.oncePerTrigger.add(this.curTrack);
            else if (playOnce == 4) {
                this.playedOnce.add(this.curTrack);
                this.changedStorageStatus = true;
            }
            if(playOnce>=3) this.curTrack.onAudioPlayed();
        }
        this.changedStatus = true;
        this.audioCounter++;
    }

    @Override
    public void onTrackStop(AudioTrackEndReason endReason) {
        if(isPlaying() && (endReason == AudioTrackEndReason.STOPPED || endReason == AudioTrackEndReason.REPLACED))
            this.curTrack.onAudioStopped(getCurPlaying().getPosition());
        ChannelManager.handleAudioStop(getOverrideCategories());
        this.changedStatus = true;
        for(Trigger activeTrigger : this.picker.getInfo().getActiveTriggers())
            activeTrigger.onAudioFinish(this.audioCounter);
        for(Audio audio : this.playedOnce)
            unregisterAudio(audio);
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

    public void stopTrack(boolean shouldFade) {
        if(!shouldFade) {
            this.picker.startMap.entrySet().removeIf(entry -> !this.picker.getInfo().getPlayableTriggers().contains(entry.getKey()));
            this.getPlayer().stopTrack();
        }
        else changeTrack();
    }

    public void parseRedirect(ConfigRedirect redirect) {
        redirect.parse();
        for(String id : redirect.urlMap.keySet()) loadFromURL(id,redirect.urlMap.get(id),null);
        for (String folderPath : ChannelManager.OPEN_AUDIO_FILES.keySet()) {
            if (this.localFolderPath.matches(folderPath)) {
                for(File file : ChannelManager.OPEN_AUDIO_FILES.get(folderPath)) {
                    String name = FilenameUtils.getBaseName(file.getName());
                    if (!this.loadedTracks.containsKey(name))
                        loadAudioFile(name, file, null);
                }
            }
        }
        if(!this.erroredSongDownloads.isEmpty())
            MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Could not read audio from the sources " +
                    "listed below",getChannelName());
        for(String error : this.erroredSongDownloads) MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - {}",
                getChannelName(),error);
        this.erroredSongDownloads.clear();
    }

    public void readResourceLocations() {
        for(String id : redirect.resourceLocationMap.keySet())
            loadFromResourceLocation(id,redirect.resourceLocationMap.get(id),null);
        if(!this.erroredSongDownloads.isEmpty())
            MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Could not read audio from the sources " +
                    "listed below",getChannelName());
        for(String error : this.erroredSongDownloads) MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - {}",
                getChannelName(),error);
        this.erroredSongDownloads.clear();
    }

    private void tryAddTrack(AudioTrack track, String id, String type) {
        this.AUDIO_QUEUE.decrementAndGet();
        if(Objects.nonNull(track)) {
            if (!this.loadedTracks.containsKey(id)) {
                this.loadedTracks.put(id, track);
                this.loadedTrackTypes.put(id, type);
                String seekable = track.isSeekable() ? "Seekable" : "Nonseekable";
                MusicTriggers.logExternally(Level.INFO, "Channel[{}] - {} track loaded to id {} from {}",
                        getChannelName(),seekable,id,type);
            } else MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Audio track with id {} already exists " +
                    "as type {}!",getChannelName(), id, this.loadedTrackTypes.get(id));
        } else MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Audio track with id {} was null and could " +
                "not be loaded from {}!",getChannelName(), id, type);
    }

    private void tryAddPlaylist(AudioPlaylist playlist, String id, String type, String typeVal) {
        this.AUDIO_QUEUE.decrementAndGet();
        MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Attempting to load  a playlist with name to id" +
                        " {} from {}[{}]",getChannelName(),playlist.getName(),id,type,typeVal);
        int i = 1;
        for(AudioTrack track : playlist.getTracks()) {
            tryAddTrack(track,id+"_"+i,type+"[{[playlist_name:"+playlist.getName()+"] [type_id:"+typeVal+"]}]");
            i++;
        }
    }

    private void noMatches(String id, String type, @Nullable Audio audioReference) {
        this.AUDIO_QUEUE.decrementAndGet();
        MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - There was no valid audio able to be extracted " +
                        "to id {} from type {}!",getChannelName(),id,type);
        handleErroredAudio(id, type, audioReference);
    }

    private void loadFailed(String id, String type, FriendlyException ex, @Nullable Audio audioReference) {
        this.AUDIO_QUEUE.decrementAndGet();
        MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - There was an exception when attempting to " +
                "extract audio to id {} from type {}! See the main log for the full stacktrack of the error '{}'.",
                getChannelName(),id,type,ex.getLocalizedMessage());
        handleErroredAudio(id, type, audioReference);
    }

    private void handleErroredAudio(String id, String type, @Nullable Audio audioReference) {
        if(Objects.nonNull(audioReference)) {
            MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - There was an audio object attached to the " +
                    "errored id {}! It will be removed from the registry until the next reload.",getChannelName(),id);
            unregisterAudio(audioReference);
        } else this.erroredSongDownloads.add("from "+type+" into "+id);
    }

    private void loadFromURL(String id, String url, @Nullable Audio audioReference) {
        this.AUDIO_QUEUE.incrementAndGet();
        this.playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                Channel.this.tryAddTrack(track,id,"url["+url+"]");
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                Channel.this.tryAddPlaylist(playlist,id,"url",url);
            }

            @Override
            public void noMatches() {
                Channel.this.noMatches(id,"url["+url+"]",audioReference);
            }

            @Override
            public void loadFailed(FriendlyException ex) {
                Channel.this.loadFailed(id,"url["+url+"]",ex,audioReference);
            }
        });
    }

    private void loadFromResourceLocation(String id, ResourceLocation source, @Nullable Audio audioReference) {
        try {
            this.AUDIO_QUEUE.incrementAndGet();
            if (!this.loadedTracks.containsKey(id)) {
                String first = null;
                String second = null;
                if(Minecraft.getMinecraft().defaultResourcePack.resourceExists(source)) {
                    String sourcePath = "/assets/" + source.getNamespace() + "/" + source.getPath();
                    URL url = Minecraft.class.getResource(sourcePath);
                    if(Objects.nonNull(url)) {
                        URI uri = url.toURI();
                        Path path = null;
                        if ("file".equals(uri.getScheme())) {
                            URL resource = Minecraft.class.getResource(sourcePath);
                            if(Objects.nonNull(resource)) path = Paths.get(resource.toURI());
                        }
                        else {
                            FileSystem filesystem;
                            try {
                                filesystem = FileSystems.getFileSystem(uri);
                            } catch (FileSystemNotFoundException | ProviderNotFoundException ignored) {
                                filesystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                            }
                            path = filesystem.getPath(sourcePath);
                        }
                        if(Objects.nonNull(path)) first = path.toUri().toString();
                    }
                    else {
                        File file = Minecraft.getMinecraft().defaultResourcePack.resourceIndex.getFile(source);
                        if (Objects.nonNull(file) && file.isFile()) {
                            first = file.getAbsolutePath();
                            second = file.getName();
                        }
                    }
                }
                if(Objects.nonNull(first)) {
                    AudioLoadResultHandler handler = new AudioLoadResultHandler() {
                        @Override
                        public void trackLoaded(AudioTrack track) {
                            Channel.this.tryAddTrack(track,id,"resource["+source+"]");
                        }

                        @Override
                        public void playlistLoaded(AudioPlaylist playlist) {
                            Channel.this.tryAddPlaylist(playlist,id,"resource",source.toString());
                        }

                        @Override
                        public void noMatches() {
                            Channel.this.noMatches(id,"resource["+source+"]",audioReference);
                        }

                        @Override
                        public void loadFailed(FriendlyException ex) {
                            Channel.this.loadFailed(id,"resource["+source+"]",ex,audioReference);
                        }
                    };
                    if(Objects.nonNull(second)) this.playerManager.loadItem(new AudioReference(first, second),handler);
                    else this.playerManager.loadItem(first,handler);
                } else MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Failed to get URI for resource " +
                        "location {} when attempting to load from id {}",getChannelName(),source,id);
            } else MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Audio track with id {} already exists " +
                    "as type '{}'!",getChannelName(), id, this.loadedTrackTypes.get(id));
        } catch (Exception e) {
            Channel.this.AUDIO_QUEUE.decrementAndGet();
            MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Could not decode track from resource " +
                    "location {} when attempting to load from id {}! See the main log for the full error of '{}'",
                    getChannelName(),source,id,e.getLocalizedMessage());
            Constants.MAIN_LOG.error("Channel[{}] - Could not decode track from resource location {} to id {}",
                    getChannelName(),source,id,e);
            e.printStackTrace();
        }
    }

    private void loadAudioFile(String id, File file, @Nullable Audio audioReference) {
        try {
            this.AUDIO_QUEUE.incrementAndGet();
            this.playerManager.loadItem(new AudioReference(file.getPath(), file.getName()), new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    Channel.this.tryAddTrack(track,id,"file["+file.getName()+"]");
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    Channel.this.tryAddPlaylist(playlist,id,"file",file.getName());
                }

                @Override
                public void noMatches() {
                    Channel.this.noMatches(id,"file["+file.getName()+"]",audioReference);
                }

                @Override
                public void loadFailed(FriendlyException ex) {
                    Channel.this.loadFailed(id,"file["+file.getName()+"]",ex,audioReference);
                }
            });
        } catch (Exception e) {
            Channel.this.AUDIO_QUEUE.decrementAndGet();
            MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Could not load track from file {} to id! " +
                    "See the main log for the full error of '{}'",getChannelName(),file.getName(),id,
                    e.getLocalizedMessage());
            Constants.MAIN_LOG.error("Channel[{}] - Could not load track from file {} to id {}",
                    getChannelName(),file.getName(),id,e);
            e.printStackTrace();
        }
    }

    public void initializeServerData(ServerTriggerStatus serverTriggerStatus) {
        serverTriggerStatus.addChannelInfo(getChannelName(),new ArrayList<>(this.data.serverTriggers.values()),this.data.menuSongs);
    }

    public void storeToggleState(Trigger trigger, boolean isToggled) {
        this.toggleStorage.put(trigger.getNameWithID(),isToggled);
        this.changedStorageStatus = true;
    }

    public void encodeDynamic(ByteBuf buf) {
        NetworkUtil.writeString(buf,getChannelName());
        buf.writeBoolean(this.changedStatus);
        if(this.changedStatus) {
            NetworkUtil.writeGenericList(buf, this.commandsForPacket, NetworkUtil::writeString);
            boolean playingSong = Objects.nonNull(this.curTrack) && Objects.nonNull(this.picker.getInfo().highestPriorityActive());
            buf.writeBoolean(playingSong);
            if (playingSong) {
                NetworkUtil.writeString(buf, this.curTrack.getName());
                NetworkUtil.writeString(buf, Objects.requireNonNull(this.picker.getInfo().highestPriorityActive()).getName());
            }
            this.commandsForPacket.clear();
            this.changedStatus = false;
        }
        buf.writeBoolean(this.changedStorageStatus);
        if(this.changedStorageStatus) {
            NetworkUtil.writeGenericMap(buf,this.toggleStorage,NetworkUtil::writeString,ByteBuf::writeBoolean);
            NetworkUtil.writeGenericMap(buf,convertAudioForStorage(),NetworkUtil::writeString,
                    (buf1,list) -> NetworkUtil.writeGenericList(buf1,list,NetworkUtil::writeString));
            this.changedStorageStatus = false;
        }
    }

    private Map<String,List<String>> convertAudioForStorage() {
        Map<String,List<String>> ret = new HashMap<>();
        for(Audio audio : this.playedOnce) {
            Set<String> triggers = new HashSet<>();
            for(Trigger trigger : audio.getTriggers())
                triggers.add(trigger.getNameWithID());
            ret.put(audio.getName(),new ArrayList<>(triggers));
        }
        return ret;
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
        this.fadingOut = false;
        this.tempFadeIn = this.picker.fadeIn;
        if(Objects.nonNull(this.curTrack)) this.prevTrack = this.curTrack;
        this.curTrack = null;
    }

    public void parseConfigs(boolean startup) {
        parseRedirect(this.redirect);
        if(!startup) readResourceLocations();
        this.jukebox.parse();
    }

    /**
     * This needs to be called after all triggers are registered so ordering of registration does not have to be forced
     */
    public void parseMoreConfigs() {
        this.data.parseAudio();
        this.data.parseToggles();
        for(Trigger trigger : getRegisteredTriggers()) trigger.parseLinks();
    }

    public Trigger getSimpleTrigger(String name) {
        return this.data.registeredTriggerMap.containsKey(name) ?
                this.data.registeredTriggerMap.get(name).get("not_accepted") : null;
    }

    public List<Trigger> getRegisteredTriggers() {
        return this.data.registeredTriggers;
    }

    public List<Audio> getRegisteredAudio() {
        return this.data.registeredAudio;
    }

    public List<Audio> getSongPool(Trigger trigger) {
        List<Audio> playableSongs = this.data.songPools.get(trigger);
        if(Objects.nonNull(playableSongs) && !playableSongs.isEmpty()) playableSongs.removeIf(Audio::hasPlayed);
        return playableSongs;
    }

    private void unregisterAudio(Audio audio) {
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
        return new ChannelInstance(this.info,new Main(getChannelName(),this.data.main),
                new Transitions(getChannelName(),this.data.transitions),new Commands(getChannelName(),this.data.commands),
                new Toggles(getChannelName(),this.data.toggles),
                new Redirect(getChannelName(),this.redirect.urlMap,this.redirect.resourceLocationMap),
                new Jukebox(getChannelName(),this.getRecordMap()));
    }

    public void clear() {
        this.player.destroy();
        this.loadedTracks.clear();
        this.loadedTrackTypes.clear();
        this.changedStatus = false;
    }

    public void readStoredData(Map<String,Boolean> toggleMap, Map<String,List<String>> playedOnceMap) {
        if(Objects.nonNull(toggleMap))
            for(Trigger trigger : getRegisteredTriggers())
                if(trigger.getParameterInt("toggle_save_status")==2 && toggleMap.containsKey(trigger.getNameWithID()))
                    trigger.setToggle(toggleMap.get(trigger.getNameWithID()),false);
        if(Objects.nonNull(playedOnceMap)) {
            for(Audio audio : getRegisteredAudio()) {
                if(audio.getPlayOnce()==4 && playedOnceMap.containsKey(audio.getName())) {
                    HashSet<String> triggerNames = new HashSet<>();
                    for(Trigger trigger : audio.getTriggers())
                        triggerNames.add(trigger.getNameWithID());
                    if(triggerNames.containsAll(playedOnceMap.get(audio.getName())))
                        unregisterAudio(audio);
                }
            }
        }
    }

    public void onLogOut() {
        for(Trigger trigger : getRegisteredTriggers())
            trigger.onLogOut();
        for(Audio audio : getRegisteredAudio())
            audio.onLogOut();
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
            this.registeredAudio = new ArrayList<>();
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
            this.toggleList = new ArrayList<>();
        }

        private HashMap<String, HashMap<String, Trigger>> parseTriggers(Table triggers) {
            HashMap<String, HashMap<String, Trigger>> ret = new HashMap<>();
            if(Objects.nonNull(triggers)) {
                for (Table trigger : triggers.getChildren().values()) {
                    if(!trigger.getName().matches("universal")) {
                        if(!Trigger.isLoaded(trigger.getName()))
                            MusicTriggers.logExternally(Level.WARN,"Channel[{}] - Tried to assign unregistered " +
                                    "trigger with name \"{}\"! Is this a modded trigger?",info.getName(),trigger.getName());
                        else {
                            ret.putIfAbsent(trigger.getName(), new HashMap<>());
                            String id = getIDOrFiller(trigger.getName(), trigger);
                            if (id.matches("missing_id"))
                                MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Trigger " +
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
                                        this.registeredTriggers.add(createdTrigger.get());
                                        if (Trigger.isServerSide(name))
                                            this.serverTriggers.put(createdTrigger.get(), trigger);
                                        logRegister(trigger.getName(), id);
                                    }
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
            Trigger trigger = new Trigger(triggerTable.getName(), Channel.this, triggerTable.getTablesByName("link"));
            for (Variable parameter : triggerTable.getVars()) {
                if (!Trigger.isParameterAccepted(trigger.getName(),parameter.getName()))
                    MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Parameter {} is not accepted for " +
                            "trigger {} so it will be skipped!", info.getName(), parameter, triggerTable.getName());
                else trigger.setParameter(parameter.getName(),parameter.get());
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

        private void parseAudio() {
            this.registeredAudio.clear();
            Table songs = this.main.getTableByName("songs");
            Table universal = Objects.isNull(songs) ? null :
                    songs.hasTable("universal") ? songs.getTableByName("universal") : null;
            if(Objects.nonNull(songs)) {
                for (Table audio : songs.getChildren().values()) {
                    if(!audio.getName().matches("universal")) {
                        Audio potential = new Audio(Channel.this.getChannelName(),audio,this.registeredTriggers,universal);
                        if (potential.getTriggers().isEmpty())
                            MusicTriggers.logExternally(Level.WARN, "Channel[{}] - No valid triggers were " +
                                    "registered for audio {} so it has been skipped!",info.getName(),audio.getName());
                        else {
                            this.registeredAudio.add(potential);
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
            for(Trigger trigger : this.registeredTriggers)
                if(!this.songPools.containsKey(trigger)) this.songPools.put(trigger,new ArrayList<>());
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
                if(!this.registeredTriggers.stream().map(Trigger::getNameWithID)
                        .collect(Collectors.toList()).contains(trigger)) {
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
            return vague ? new HashSet<>(picker.getInfo().getPlayableTriggers()).containsAll(this.titleCards.get(table)) :
                    new HashSet<>(picker.getInfo().getActiveTriggers()).containsAll(this.titleCards.get(table));
        }

        private boolean canPlayImage(Table table, boolean vague) {
            return vague ? new HashSet<>(picker.getInfo().getPlayableTriggers()).containsAll(this.imageCards.get(table)) :
                    new HashSet<>(picker.getInfo().getActiveTriggers()).containsAll(this.imageCards.get(table));
        }

        private void parseToggles() {
            this.toggleList.clear();
            for(Table table : this.toggles.getTablesByName("toggle")) {
                Toggle potentialToggle = new Toggle(table,Channel.this.getChannelName());
                if(potentialToggle.isValid(Channel.this.getChannelName())) this.toggleList.add(potentialToggle);
            }
        }
    }
}
