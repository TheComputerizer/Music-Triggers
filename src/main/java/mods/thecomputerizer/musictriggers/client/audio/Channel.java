package mods.thecomputerizer.musictriggers.client.audio;

import com.mojang.blaze3d.platform.InputConstants;
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
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.common.ServerChannelData;
import mods.thecomputerizer.musictriggers.config.*;
import mods.thecomputerizer.theimpossiblelibrary.client.render.PNG;
import mods.thecomputerizer.theimpossiblelibrary.client.render.Renderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultClientPackResources;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.resource.PathPackResources;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.Level;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Channel {
    public static final KeyMapping GUI = new KeyMapping("key.musictriggers.gui", KeyConflictContext.UNIVERSAL, 
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.musictriggers");
    private static final AudioDataFormat FORMAT = new Pcm16AudioDataFormat(2, 48000, 960, true);
    private final String name;
    private final SoundSource category;
    private final ConfigMain main;
    private final ConfigTransitions transitions;
    private final ConfigCommands commands;
    private final ConfigToggles toggles;
    private final ConfigRedirect redirect;
    private final ConfigJukebox jukebox;
    private final MusicPicker picker;
    private final boolean canBePausedByJukeBox;
    private final boolean overrides;
    private final AudioPlayerManager playerManager;
    private AudioPlayer player;
    private ChannelListener listener;
    private final HashMap<String, AudioTrack> loadedTracks;
    private ClientSync sync;
    private MusicPicker.Packeted toSend;
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
    public final Map<Integer, Boolean> canPlayTitle;
    public final Map<Integer, Boolean> canPlayImage;
    private int delayCounter = 0;
    private boolean delayCatch = false;
    private final List<Trigger> playingTriggers;
    private boolean pausedByJukebox = false;

    public Channel(String name, String category, boolean pausedByJukeBox, boolean overrides, String main,
                   String transitions, String commands, String toggles, String redirect, String jukebox, String localFolder) {
        this.name = name;
        this.category = EnumUtils.isValidEnum(SoundSource.class, category) ?
                SoundSource.valueOf(category) : SoundSource.MUSIC;
        this.canBePausedByJukeBox = pausedByJukeBox;
        this.overrides = overrides;
        this.sync = new ClientSync(name);
        this.toSend = new MusicPicker.Packeted();
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(this.playerManager);
        AudioSourceManagers.registerLocalSource(this.playerManager);
        this.player = refreshPlayer();
        this.loadedTracks = new HashMap<>();
        this.playerManager.setFrameBufferDuration(1000);
        this.playerManager.setPlayerCleanupThreshold(Long.MAX_VALUE);
        this.playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        this.playerManager.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
        this.playerManager.getConfiguration().setOutputFormat(FORMAT);
        this.oncePerTrigger = new ArrayList<>();
        this.onceUntilEmpty = new ArrayList<>();
        this.canPlayTitle = new HashMap<>();
        this.canPlayImage = new HashMap<>();
        this.commandsForPacket = new ArrayList<>();
        this.erroredSongDownloads = new ArrayList<>();
        this.playingTriggers = new ArrayList<>();
        this.main = new ConfigMain(new File(Constants.CONFIG_DIR,main+".toml"));
        this.transitions = new ConfigTransitions(new File(Constants.CONFIG_DIR,transitions+".toml"));
        this.commands = new ConfigCommands(new File(Constants.CONFIG_DIR,commands+".toml"));
        this.toggles = new ConfigToggles(new File(Constants.CONFIG_DIR,toggles+".toml"));
        this.redirect = new ConfigRedirect(new File(Constants.CONFIG_DIR,redirect+".txt"));
        this.jukebox = new ConfigJukebox(new File(Constants.CONFIG_DIR,jukebox+".txt"));
        this.picker = new MusicPicker(this);
        this.localFolderPath = localFolder;
        MusicTriggers.logExternally(Level.INFO, "Registered sound engine channel "+ name);
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

    private AudioPlayer refreshPlayer() {
        if(this.player!=null) {
            if(isPlaying()) this.player.stopTrack();
            this.player.destroy();
            this.listener.stopThread();
        }
        AudioPlayer newPlayer = playerManager.createPlayer();
        newPlayer.setVolume(100);
        this.listener = new ChannelListener(newPlayer, FORMAT, this.name);
        return newPlayer;
    }

    public String getChannelName() {
        return this.name;
    }

    public ConfigMain getMainConfig() {
        return this.main;
    }

    public ConfigTransitions getTransitionsConfig() {
        return this.transitions;
    }

    public ConfigCommands getCommandsConfig() {
        return this.commands;
    }

    public ConfigToggles getTogglesConfig() {
        return this.toggles;
    }

    public ConfigRedirect getRedirect() {
        return this.redirect;
    }

    public void runToggle(int condition, List<Trigger> triggers) {
        this.toggles.runToggle(condition, triggers);
    }

    public void forceToggle(Trigger trigger, boolean on, boolean flip) {
        if(flip) this.toggles.forceToggle(trigger, invertToggle(trigger));
        else this.toggles.forceToggle(trigger, on);
    }

    public boolean defaultToggle(Trigger trigger) {
        return trigger.defaultToggle();
    }

    public boolean invertToggle(Trigger trigger) {
        return !this.toggles.getToggle(trigger, defaultToggle(trigger));
    }

    public ClientSync getSyncStatus() {
        return this.sync;
    }

    public boolean overridesNormalMusic() {
        return this.overrides;
    }

    public void initializeTriggerPersistence(Trigger trigger) {
        this.picker.initializePersistence(trigger);
    }

    public boolean getVictory(int id) {
        return this.picker.getVictory(id);
    }

    private void checkLoops() {
        if(Objects.nonNull(this.curTrack)) {
            for(Audio.Loop loop : this.curTrack.getLoops()) {
                long posCapture = this.getMillis();
                long setTo = loop.checkForLoop(posCapture,this.getTotalMillis());
                if(posCapture!=setTo) this.setMillis(setTo);
            }
        }
    }

    public void tickFast() {
        if(checkAudio() && Trigger.isRegistered(this.name)) {
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
        if(checkAudio() && Trigger.isRegistered(this.name)) {
            this.toSend = this.picker.querySongList(this.main.universalParameters);
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
                        forceToggle(playable, false, false);
            }
        } else clearSongs();
    }

    private void onTriggerStart() {
        for (int i : canPlayTitle.keySet()) {
            if (!this.canPlayTitle.get(i) && !new HashSet<>(this.picker.getInfo().getPlayableTriggers()).containsAll(this.transitions.titlecards.get(i).getTriggers()))
                this.canPlayTitle.put(i, true);
        }
        for (int i : this.canPlayImage.keySet()) {
            if (!this.canPlayImage.get(i) &&
                    !this.transitions.imagecards.get(i).getTriggers().isEmpty() &&
                    !new HashSet<>(this.picker.getInfo().getPlayableTriggers()).containsAll(this.transitions.imagecards.get(i).getTriggers()))
                this.canPlayImage.put(i, true);
        }
        this.commandsForPacket.clear();
        for (String command : this.commands.commandMap.keySet()) {
            if (this.commands.commandMap.get(command).equals(this.picker.getInfo().getActiveTriggers()))
                this.commandsForPacket.add(command);
        }
        renderCards();
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
        Minecraft mc = Minecraft.getInstance();
        MusicTriggers.logExternally(Level.DEBUG, "Finding cards to render");
        int markForDeletion = -1;
        for (int i : this.transitions.titlecards.keySet()) {
            boolean pass = false;
            if(new HashSet<>(this.picker.getInfo().getActiveTriggers()).containsAll(this.transitions.titlecards.get(i).getTriggers()) &&
                    new HashSet<>(this.transitions.titlecards.get(i).getTriggers()).containsAll(this.picker.getInfo().getActiveTriggers())) pass=true;
            else if(this.transitions.titlecards.get(i).getVague() &&
                    new HashSet<>(this.picker.getInfo().getPlayableTriggers()).containsAll(this.transitions.titlecards.get(i).getTriggers()) &&
                    canPlayTitle.get(i)) {
                pass=true;
                canPlayTitle.put(i, false);
            }
            if (pass && mc.player != null) {
                MusicTriggers.logExternally(Level.INFO,"displaying title card {}",i);
                if(!this.transitions.titlecards.get(i).getTitles().isEmpty())
                    mc.gui.setTitle(MutableComponent.create(new LiteralContents(this.transitions.titlecards.get(i).getTitles()
                            .get(ThreadLocalRandom.current().nextInt(0, this.transitions.titlecards.get(i)
                                    .getTitles().size())))).withStyle(ChatFormatting.getByName(
                                            this.transitions.titlecards.get(i).getTitlecolor())));
                if(!this.transitions.titlecards.get(i).getSubTitles().isEmpty())
                    mc.gui.setSubtitle(MutableComponent.create(new LiteralContents(this.transitions.titlecards.get(i)
                            .getSubTitles().get(ThreadLocalRandom.current().nextInt(0,
                                    this.transitions.titlecards.get(i).getSubTitles().size())))).withStyle(
                            ChatFormatting.getByName(this.transitions.titlecards.get(i).getSubtitlecolor())));
                if(this.transitions.titlecards.get(i).getPlayonce()) markForDeletion = i;
                break;
            }
        }
        if(markForDeletion!=-1) {
            this.transitions.titlecards.remove(markForDeletion);
            markForDeletion = -1;
        }
        for (int i : this.transitions.imagecards.keySet()) {
            boolean pass = false;
            if(!this.transitions.imagecards.get(i).getTriggers().isEmpty()) {
                if (new HashSet<>(this.picker.getInfo().getActiveTriggers()).containsAll(this.transitions.imagecards.get(i).getTriggers()) &&
                        new HashSet<>(this.transitions.imagecards.get(i).getTriggers()).containsAll(this.picker.getInfo().getActiveTriggers()))
                    pass = true;
                else if (this.transitions.imagecards.get(i).getVague() &&
                        new HashSet<>(this.picker.getInfo().getPlayableTriggers()).containsAll(this.transitions.imagecards.get(i).getTriggers()) &&
                        canPlayImage.get(i)) {
                    pass = true;
                    canPlayImage.put(i, false);
                }
            }
            if (pass && mc.player != null) {
                if(this.transitions.imagecards.get(i).getName()!=null) {
                    MusicTriggers.logExternally(Level.INFO, "displaying image card " + this.transitions.imagecards.get(i).getName());
                    ConfigTransitions.Image imageCard = this.transitions.imagecards.get(i);
                    if(!imageCard.isInitialized()) imageCard.initialize();
                    if(imageCard.getFormat()!=null) {
                        if(imageCard.getFormat() instanceof PNG) {
                            Renderer.renderPNGToBackground((PNG)imageCard.getFormat(),imageCard.getLocationX(),
                                    imageCard.getLocationY(),imageCard.getHorizontal(),imageCard.getVertical(),imageCard.getScaleX(),imageCard.getScaleY(),
                                    imageCard.getTime()*50L);
                        }
                        /*
                        if(imageCard.getFormat() instanceof GIF) {
                            Renderer.renderGifToBackground((GIF)imageCard.getFormat(),imageCard.getLocationX(),
                                    imageCard.getLocationY(),imageCard.getHorizontal(),imageCard.getVertical(),imageCard.getScaleX(),imageCard.getScaleY(),
                                    imageCard.getTime()*50L);
                        }
                        else if(imageCard.getFormat() instanceof MP4) Renderer.renderMP4ToBackground((MP4)imageCard.getFormat(),imageCard.getLocationX(),
                                imageCard.getLocationY(),imageCard.getHorizontal(),imageCard.getVertical(),imageCard.getScaleX(),imageCard.getScaleY(),
                                imageCard.getTime()*50L);
                         */
                    }
                    if (this.transitions.imagecards.get(i).getPlayonce()) markForDeletion = i;
                    break;
                }
            }
        }
        if(markForDeletion!=-1) this.transitions.imagecards.get(markForDeletion).setName(null);
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
        return Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER) > 0
                && Minecraft.getInstance().options.getSoundSourceVolume(this.category) > 0;
    }

    public long getTotalMillis() {
        return getCurPlaying().getDuration();
    }

    public long getMillis() {
        return getCurPlaying().getPosition();
    }

    public void setMillis(long milliseconds) {
        getCurPlaying().setPosition(milliseconds);
    }

    public void setVolume(float volume) {
        volume = Math.min(volume, 1f); // temporary cap the volume at 100% until the fade issue is fixed
        this.getPlayer().setVolume((int)(volume*getChannelVolume()*100));
    }

    private float getChannelVolume() {
        float master = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
        if(getCategory()==SoundSource.MASTER) return master;
        else return master*Minecraft.getInstance().options.getSoundSourceVolume(getCategory());
    }

    public void playTrack(Audio audio, long milliseconds) {
        String id = audio.getName();
        AudioTrack track = this.loadedTracks.get(audio.getName());
        MusicTriggers.logExternally(Level.INFO, "Playing track from id "+id+" at a millisecond time of "+milliseconds);
        if(track!=null) {
            track.setPosition(milliseconds);
            try {
                if (!this.getPlayer().startTrack(track, false))
                    MusicTriggers.logExternally(Level.ERROR, "Could not start track!");
                else this.curTrack = audio;
            } catch (IllegalStateException e) {
                if (!this.getPlayer().startTrack(track.makeClone(), false))
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
                this.main.parsedFile.entrySet().removeIf(entry -> entry.getValue()==audio);
                Trigger.removeAudio(this.name,audio);
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
                        MusicTriggers.logExternally(Level.INFO, "The resource pack that has {} is {} under class {}",
                                source,pack.getName(),pack.getClass().getName());
                        URL url = pack.getClass().getResource("/"+PackType.CLIENT_RESOURCES.getDirectory()+"/"+
                                source.getNamespace()+"/"+source.getPath());
                        if (url != null && (url.getProtocol().equals("jar") || FolderPackResources.validatePath(new File(url.getFile()),
                                        "/"+PackType.CLIENT_RESOURCES.getDirectory()+"/"+source.getNamespace()+"/"+source.getPath())))
                            sourcePath = url.getPath();
                        else {
                            if (pack instanceof AbstractPackResources resourcePack) {
                                String resource = String.format("%s/%s/%s", PackType.CLIENT_RESOURCES.getDirectory(),
                                        source.getNamespace(), source.getPath());
                                if (pack instanceof PathPackResources modResource &&
                                        !(namespace.matches("minecraft") || namespace.matches("realms"))) {
                                    Path test = modResource.getSource().resolve(resource);
                                    if(Files.exists(test)) sourcePath = test.toString();
                                } else if (!(pack instanceof FilePackResources)) {
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
                if(sourcePath!=null) {
                    this.playerManager.loadItem(new AudioReference(sourcePath, name), new AudioLoadResultHandler() {
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
            if(zipSystem!=null) zipSystem.close();
        } catch (Exception e) {
            MusicTriggers.logExternally(Level.ERROR, "Could not decode track from resource location "+source,e);
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
            MusicTriggers.logExternally(Level.ERROR, "Could not load track from file {}"+id,e);
        }
    }

    public void encode(ByteBuf buf) {
        String channelName = this.getChannelName();
        String name = Objects.isNull(this.curTrack) ? null : this.curTrack.getName();
        assert Minecraft.getInstance().player != null;
        String uuid = Minecraft.getInstance().player.getStringUUID();
        if(name==null) name = "placeholder";
        buf.writeInt(channelName.length());
        buf.writeInt(uuid.length());
        buf.writeInt(name.length());
        buf.writeCharSequence(channelName, StandardCharsets.UTF_8);
        buf.writeCharSequence(uuid, StandardCharsets.UTF_8);
        buf.writeCharSequence(name, StandardCharsets.UTF_8);
        buf.writeInt(this.commandsForPacket.size());
        for(String command : this.commandsForPacket) {
            buf.writeInt(command.length());
            buf.writeCharSequence(command, StandardCharsets.UTF_8);
        }
        buf.writeInt(this.toSend.getMenuSongs().size());
        for(Audio audio : this.toSend.getMenuSongs()) {
            buf.writeInt(audio.getName().length());
            buf.writeCharSequence(audio.getName(), StandardCharsets.UTF_8);
        }
        buf.writeInt(this.picker.getInfo().getActiveTriggers().size());
        for(Trigger trigger : this.picker.getInfo().getActiveTriggers()) {
            buf.writeInt(trigger.getName().length());
            buf.writeCharSequence(trigger.getName(), StandardCharsets.UTF_8);
        }
        buf.writeInt(this.picker.getInfo().getPlayableTriggers().size());
        for(Trigger trigger : this.picker.getInfo().getPlayableTriggers()) {
            buf.writeInt(trigger.getNameWithID().length());
            buf.writeCharSequence(trigger.getNameWithID(), StandardCharsets.UTF_8);
        }
        buf.writeInt(this.toSend.getSnowTriggers().size());
        buf.writeInt(this.toSend.getHomeTriggers().size());
        for(ServerChannelData.Home home : this.toSend.getHomeTriggers()) buf.writeInt(home.getRange());
        buf.writeInt(this.toSend.getBiomeTriggers().size());
        for(ServerChannelData.Biome biome : this.toSend.getBiomeTriggers()) {
            buf.writeInt(biome.getTrigger().length());
            buf.writeInt(biome.getBiome().length());
            buf.writeInt(biome.getCategory().length());
            buf.writeInt(biome.getRainType().length());
            buf.writeCharSequence(biome.getTrigger(), StandardCharsets.UTF_8);
            buf.writeCharSequence(biome.getBiome(), StandardCharsets.UTF_8);
            buf.writeCharSequence(biome.getCategory(), StandardCharsets.UTF_8);
            buf.writeCharSequence(biome.getRainType(), StandardCharsets.UTF_8);
            buf.writeFloat(biome.getTemperature());
            buf.writeBoolean(biome.isCold());
            buf.writeFloat(biome.getRainfall());
            buf.writeBoolean(biome.isTogglerainfall());
        }
        buf.writeInt(this.toSend.getStructureTriggers().size());
        for(ServerChannelData.Structure structure : this.toSend.getStructureTriggers()) {
            buf.writeInt(structure.getTrigger().length());
            buf.writeInt(structure.getStructure().length());
            buf.writeCharSequence(structure.getTrigger(), StandardCharsets.UTF_8);
            buf.writeCharSequence(structure.getStructure(), StandardCharsets.UTF_8);
        }
        buf.writeInt(this.toSend.getMobTriggers().size());
        for(ServerChannelData.Mob mob : this.toSend.getMobTriggers()) {
            buf.writeInt(mob.getTrigger().length());
            buf.writeInt(mob.getName().length());
            buf.writeInt(mob.getInfernal().length());
            buf.writeInt(mob.getNbtKey().length());
            buf.writeInt(mob.getChampion().length());
            buf.writeCharSequence(mob.getTrigger(), StandardCharsets.UTF_8);
            buf.writeCharSequence(mob.getName(), StandardCharsets.UTF_8);
            buf.writeInt(mob.getRange());
            buf.writeBoolean(mob.getTargetting());
            buf.writeInt(mob.getTargettingPercentage());
            buf.writeInt(mob.getHealth());
            buf.writeInt(mob.getHealthPercentage());
            buf.writeInt(mob.getVictoryID());
            buf.writeCharSequence(mob.getInfernal(), StandardCharsets.UTF_8);
            buf.writeInt(mob.getMobLevel());
            buf.writeInt(mob.getVictoryTimeout());
            buf.writeCharSequence(mob.getNbtKey(), StandardCharsets.UTF_8);
            buf.writeCharSequence(mob.getChampion(), StandardCharsets.UTF_8);
        }
        buf.writeInt(this.toSend.getRaidTriggers().size());
        for(ServerChannelData.Raid raid : this.toSend.getRaidTriggers()) {
            buf.writeInt(raid.getTrigger().length());
            buf.writeCharSequence(raid.getTrigger(), StandardCharsets.UTF_8);
            buf.writeInt(raid.getWave());
        }
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
        playingTriggers.clear();
        fadingOut = false;
        curTrack = null;
        tempFadeIn = this.picker.fadeIn;
    }

    public void parseConfigs(boolean startup) {
        parseRedirect(this.redirect);
        if(!startup) readResourceLocations();
        this.main.parse(this.name);
        this.transitions.parse(this.name);
        this.commands.parse(this.name);
        this.toggles.parse(this.name);
        this.jukebox.parse();
    }

    private void clearAllListsAndMaps() {
        this.main.clearMaps();
        this.transitions.clearMaps();
        this.commands.commandMap.clear();
        this.toggles.clearMaps();
        this.redirect.urlMap.clear();
        this.redirect.resourceLocationMap.clear();
        this.picker.clearListsAndMaps();
        this.loadedTracks.clear();
        this.oncePerTrigger.clear();
        this.onceUntilEmpty.clear();
        this.canPlayTitle.clear();
        this.canPlayImage.clear();
        this.commandsForPacket.clear();
        this.erroredSongDownloads.clear();
        this.playingTriggers.clear();
    }

    public void reload() {
        clearAllListsAndMaps();
        this.fadingIn = false;
        this.fadingOut = false;
        this.reverseFade = false;
        this.tempFadeIn = 0;
        this.tempFadeOut = 0;
        this.savedFadeOut = 0;
        this.saveVolIn = 1;
        this.saveVolOut = 1;
        this.player = refreshPlayer();
        parseConfigs(false);
    }
}
