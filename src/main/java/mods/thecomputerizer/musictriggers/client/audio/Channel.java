package mods.thecomputerizer.musictriggers.client.audio;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerDetectionResult;
import com.sedmelluq.discord.lavaplayer.container.ogg.OggAudioTrack;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.Pcm16AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalSeekableInputStream;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.client.MusicPicker;
import mods.thecomputerizer.musictriggers.common.ServerChannelData;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config.ConfigCommands;
import mods.thecomputerizer.musictriggers.config.ConfigMain;
import mods.thecomputerizer.musictriggers.config.ConfigTransitions;
import mods.thecomputerizer.musictriggers.config.Redirect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@SideOnly(value = Side.CLIENT)
public class Channel {
    public static final KeyBinding GUI = new KeyBinding("key.musictriggers_gui", Keyboard.KEY_R, "key.categories.musictriggers");
    private static final AudioDataFormat FORMAT = new Pcm16AudioDataFormat(2, 48000, 960, true);
    private final String channel;
    private final SoundCategory category;
    private ConfigMain main;
    private ConfigTransitions transitions;
    private ConfigCommands commands;
    private Redirect redirect;
    private SoundHandler handler;
    private MusicPicker picker;
    private final boolean pausedByJukeBox;
    private final boolean overrides;
    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final HashMap<String, AudioTrack> loadedTracks;
    private ClientSync sync;
    private MusicPicker.Packeted toSend;
    private final List<String> commandsForPacket;
    private final List<String> erroredSongDownloads;

    private boolean fadingIn = false;
    private boolean fadingOut = false;
    private boolean reverseFade = false;
    private int tempFadeIn = 0;
    private int tempFadeOut = 0;
    private int savedFadeOut = 0;
    private float saveVolIn = 1;
    private float saveVolOut = 1;
    private final HashMap<String, AudioTrack> musicLinker;
    private final HashMap<String,String> songNameLinker;
    private final HashMap<String, String[]> triggerLinker;
    private final HashMap<String, Float> volumeLinker;
    private final HashMap<String, Float> pitchLinker;
    private final HashMap<String, Map<Integer, String[]>> loopLinker;
    private final HashMap<String, Map<Integer, Integer>> loopLinkerCounter;
    private String curTrack;
    private String curTrackHolder;
    private final List<String> oncePerTrigger;
    private final List<String> onceUntilEmpty;
    private boolean cards = true;
    private String trackToDelete;
    private int indexToDelete;
    private final List<String> playedEvents;
    public final Map<Integer, Boolean> canPlayTitle;
    public final Map<Integer, Boolean> canPlayImage;
    private String curLinkNum = "song-0";
    private boolean nullFromLink = false;
    private boolean trackSetChanged = true;

    public Channel(String channel, boolean pausedByJukeBox, boolean overrides) {
        this.channel = channel;
        this.category = SoundCategory.getByName(this.channel);
        this.pausedByJukeBox = pausedByJukeBox;
        this.overrides = overrides;
        this.sync = new ClientSync(channel);
        this.toSend = new MusicPicker.Packeted();
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(this.playerManager);
        AudioSourceManagers.registerLocalSource(this.playerManager);
        this.player = playerManager.createPlayer();
        this.player.setVolume(100);
        this.loadedTracks = new HashMap<>();
        new ChannelListener(this.player, FORMAT, this.channel);
        this.playerManager.setFrameBufferDuration(1000);
        this.playerManager.setPlayerCleanupThreshold(Long.MAX_VALUE);
        this.playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        this.playerManager.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
        this.playerManager.getConfiguration().setOutputFormat(FORMAT);
        this.musicLinker = new HashMap<>();
        this.songNameLinker = new HashMap<>();
        this.triggerLinker = new HashMap<>();
        this.volumeLinker = new HashMap<>();
        this.pitchLinker = new HashMap<>();
        this.loopLinker = new HashMap<>();
        this.loopLinkerCounter = new HashMap<>();
        this.oncePerTrigger = new ArrayList<>();
        this.onceUntilEmpty = new ArrayList<>();
        this.playedEvents = new ArrayList<>();
        this.canPlayTitle = new HashMap<>();
        this.canPlayImage = new HashMap<>();
        this.commandsForPacket = new ArrayList<>();
        this.erroredSongDownloads = new ArrayList<>();
        MusicTriggers.logger.info("Registered sound engine for channel "+channel);
    }

    public String getChannelName() {
        return this.channel;
    }

    public ConfigMain getMainConfig() {
        return this.main;
    }

    public void passThroughConfigObjects(ConfigMain main, ConfigTransitions transitions, ConfigCommands commands, Redirect redirect, SoundHandler handler) {
        this.main=main;
        this.transitions = transitions;
        this.commands = commands;
        this.redirect = redirect;
        this.handler = handler;
        this.picker = new MusicPicker(this,this.handler);
    }

    public ClientSync getSyncStatus() {
        return this.sync;
    }

    public boolean overridesNormalMusic() {
        return this.overrides;
    }

    public String currentSongName() {
        return this.curTrackHolder;
    }

    public void tickFast() {
        //if (curTrack != null && isPlaying() && Minecraft.getMinecraft().currentScreen instanceof GuiCurPlaying) ((GuiCurPlaying) Minecraft.getMinecraft().currentScreen).setSlider(GuiCurPlaying.getSongPosInSeconds(curMusic));
        for(String trigger : this.picker.boolMap.keySet()) {
            this.picker.startMap.putIfAbsent(trigger,0);
            if(this.picker.boolMap.get(trigger)) this.picker.startMap.put(trigger,this.picker.startMap.get(trigger)+1);
            else this.picker.startMap.put(trigger,0);
        }
        for(String trigger : this.picker.triggerPersistence.keySet()) {
            this.picker.triggerPersistence.putIfAbsent(trigger,0);
            if (this.picker.triggerPersistence.get(trigger) > 0) this.picker.triggerPersistence.put(trigger, this.picker.triggerPersistence.get(trigger)-1);
        }
        if(getCurPlaying()!=null) {
            for (String key : musicLinker.keySet()) {
                if (loopLinker.get(curLinkNum) != null) {
                    for (int i : loopLinker.get(key).keySet()) {
                        if (loopLinkerCounter.get(key).get(i) < MusicTriggers.randomInt(loopLinker.get(key).get(i)[0]) && MusicTriggers.randomInt(loopLinker.get(key).get(i)[2]) <= getMillis()) {
                            setMillis(MusicTriggers.randomInt(loopLinker.get(key).get(i)[1]));
                            loopLinkerCounter.get(key).put(i, loopLinkerCounter.get(key).get(i) + 1);
                        }
                    }
                }
            }
        }
        float calculatedVolume = saveVolIn;
        if (fadingIn && !fadingOut) {
            reverseFade = false;
            if (tempFadeIn == 0) {
                fadingIn = false;
            } else {
                calculatedVolume = saveVolIn * (float) (((double) (this.picker.curFadeIn - tempFadeIn)) / ((double) this.picker.curFadeIn));
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
                calculatedVolume = saveVolOut/getChannelVolume();
                tempFadeOut = 0;
            } else {
                calculatedVolume = saveVolOut * (float) (((double) tempFadeOut) / ((double) savedFadeOut));
                tempFadeOut += 1;
            }
        }
        setVolume(calculatedVolume);
    }

    public void tickSlow() {
        MusicTriggers.logger.info("slow ticking channel "+channel);
        Minecraft mc = Minecraft.getMinecraft();
        this.toSend = this.picker.querySongList();
        if (!this.picker.getInfo().getCurrentSongList().isEmpty()) {
            boolean startQuiet = false;
            for (int i : canPlayTitle.keySet()) {
                if (!this.canPlayTitle.get(i) && !new HashSet<>(this.picker.getInfo().getPlayableTriggers()).containsAll(this.transitions.titlecards.get(i).getTriggers()))
                    this.canPlayTitle.put(i, true);
            }
            for (int i : this.canPlayImage.keySet()) {
                if (!this.canPlayImage.get(i) && !new HashSet<>(this.picker.getInfo().getPlayableTriggers()).containsAll(this.transitions.imagecards.get(i).getTriggers()))
                    this.canPlayImage.put(i, true);
            }
            for (String playable : this.picker.getInfo().getPlayableTriggers()) {
                if (!this.picker.getInfo().getActiveTriggers().contains(playable)) {
                    if (Boolean.parseBoolean(this.handler.TriggerInfoMap.get(playable)[34])) {
                        this.handler.TriggerIdentifierMap.get(playable.split("-")[0]).remove(this.handler.TriggerInfoMap.get(playable)[10]);
                        this.handler.TriggerInfoMap.remove(playable);
                        if (this.handler.TriggerIdentifierMap.get(playable.split("-")[0]).isEmpty()) {
                            this.handler.TriggerIdentifierMap.remove(playable.split("-")[0]);
                            this.handler.TriggerInfoMap.remove(playable.split("-")[0]);
                        }
                    }
                }
            }
            if (this.trackSetChanged) {
                if (!this.nullFromLink) {
                    this.fadingIn = true;
                    startQuiet = true;
                    this.tempFadeIn = this.picker.curFadeIn;
                    this.savedFadeOut = this.picker.curFadeOut;
                }
                this.nullFromLink = false;
                this.commandsForPacket.clear();
                for (String command : this.commands.commandMap.keySet()) {
                    if (this.commands.commandMap.get(command).equals(this.picker.getInfo().getActiveTriggers()))
                        this.commandsForPacket.add(command);
                }
            }
            if (isPlaying() && (mc.gameSettings.getSoundLevel(this.category) == 0 || mc.gameSettings.getSoundLevel(SoundCategory.MASTER) == 0)) {
                stopTrack();
                if (this.fadingOut) {
                    this.fadingOut = false;
                    this.fadingIn = true;
                    startQuiet = true;
                }
                removeTrack(this.trackToDelete, this.indexToDelete, this.playedEvents);
            }
            if (this.picker.getInfo().songListChanged()) {
                this.picker.getInfo().logCurrentInfo();
                if (this.picker.getInfo().getCurrentSongList().size() != 0) changeTrack(mc);
                else this.trackSetChanged = true;
            } else if (!isPlaying() && mc.gameSettings.getSoundLevel(this.category) > 0 && mc.gameSettings.getSoundLevel(SoundCategory.MASTER) > 0) {
                this.triggerLinker.clear();
                this.musicLinker.clear();
                this.songNameLinker.clear();
                this.volumeLinker.clear();
                this.pitchLinker.clear();
                EventsClient.GuiCounter = 0;
                List<String> trimmedList = this.picker.getInfo().getCurrentSongList().stream().filter(track -> !this.oncePerTrigger.contains(track)).collect(Collectors.toList());
                trimmedList = trimmedList.stream().filter(track -> !this.onceUntilEmpty.contains(track)).collect(Collectors.toList());
                if (trimmedList.size() >= 1) {
                    int i = ThreadLocalRandom.current().nextInt(0, trimmedList.size());
                    if (trimmedList.size() > 1 && this.curTrack != null) {
                        int total = trimmedList.stream().mapToInt(s -> MusicTriggers.randomInt(this.main.otherinfo.get(s)[3])).sum();
                        int j;
                        for (j = 0; j < 1000; j++) {
                            int r = ThreadLocalRandom.current().nextInt(1, total + 1);
                            String temp = " ";
                            for (String s : trimmedList) {
                                if (r < MusicTriggers.randomInt(this.main.otherinfo.get(s)[3])) {
                                    temp = s;
                                    break;
                                }
                                r -= MusicTriggers.randomInt(this.main.otherinfo.get(s)[3]);
                            }
                            if (!temp.matches(this.curTrack) && !temp.matches(" ")) {
                                this.curTrack = temp;
                                break;
                            }
                        }
                        if (j >= 1000)
                            MusicTriggers.logger.warn("Attempt to get non duplicate song passed 1000 tries! Forcing current song " + this.main.songholder.get(curTrack) + " to play.");
                    } else curTrack = trimmedList.get(i);
                    if (this.curTrack != null) {
                        this.curTrack = curTrack.replaceAll("@", "").replaceAll("#", "");
                        MusicTriggers.logger.debug(curTrack + " was chosen");
                        this.curTrackHolder = this.main.songholder.get(curTrack);
                        MusicTriggers.logger.info("Attempting to play track: " + this.curTrackHolder);
                        if (this.main.triggerlinking.get(curTrack) != null) {
                            this.triggerLinker.put("song-" + 0, this.main.triggerlinking.get(this.curTrack).get(this.curTrack));
                            this.musicLinker.put("song-" + 0, this.loadedTracks.get(this.curTrackHolder));
                            this.songNameLinker.put("song-" + 0,this.curTrackHolder);
                            this.pitchLinker.put("song-" + 0, Float.parseFloat(this.main.otherinfo.get(this.curTrack)[0]));
                            this.volumeLinker.put("song-" + 0, Float.parseFloat(this.main.otherinfo.get(this.curTrack)[4]));
                            this.saveVolIn = Float.parseFloat(this.main.otherinfo.get(this.curTrack)[4]);
                            for (int l : this.main.loopPoints.get(this.curTrack).keySet()) {
                                this.loopLinker.putIfAbsent("song-" + 0, new HashMap<>());
                                this.loopLinker.get("song-" + 0).put(l, this.main.loopPoints.get(this.curTrack).get(l));
                                this.loopLinkerCounter.putIfAbsent("song-" + 0, new HashMap<>());
                                this.loopLinkerCounter.get("song-" + 0).put(l, 0);
                            }
                            int linkcounter = 1;
                            for (String song : this.main.triggerlinking.get(this.curTrack).keySet()) {
                                if (!song.matches(this.curTrack)) {
                                    this.triggerLinker.put("song-" + linkcounter, this.main.triggerlinking.get(this.curTrack).get(song));
                                    this.musicLinker.put("song-" + linkcounter, this.loadedTracks.get(song));
                                    this.songNameLinker.put("song-" + linkcounter,song);
                                    this.volumeLinker.put("song-" + linkcounter, Float.parseFloat(this.main.otherlinkinginfo.get(this.curTrack).get(song)[1]));
                                    this.pitchLinker.put("song-" + linkcounter, Float.parseFloat(this.main.otherlinkinginfo.get(this.curTrack).get(song)[0]));
                                    if (this.main.linkingLoopPoints.get(this.curTrack) != null && this.main.linkingLoopPoints.get(this.curTrack).get(song) != null) {
                                        for (int l : this.main.linkingLoopPoints.get(this.curTrack).get(song).keySet()) {
                                            this.loopLinker.putIfAbsent("song-" + linkcounter, new HashMap<>());
                                            this.loopLinker.get("song-" + linkcounter).put(l, this.main.linkingLoopPoints.get(this.curTrack).get(song).get(l));
                                            this.loopLinkerCounter.putIfAbsent("song-" + linkcounter, new HashMap<>());
                                            this.loopLinkerCounter.get("song-" + linkcounter).put(l, 0);
                                        }
                                    }
                                }
                                linkcounter++;
                            }
                        } else {
                            this.musicLinker.put("song-" + 0, this.loadedTracks.get(this.curTrackHolder));
                            this.songNameLinker.put("song-" + 0,this.curTrackHolder);
                            this.saveVolIn = Float.parseFloat(this.main.otherinfo.get(this.curTrack)[4]);
                            this.volumeLinker.put("song-" + 0, Float.parseFloat(this.main.otherinfo.get(this.curTrack)[4]));
                            this.pitchLinker.put("song-" + 0, Float.parseFloat(this.main.otherinfo.get(this.curTrack)[0]));
                            for (int l : this.main.loopPoints.get(this.curTrack).keySet()) {
                                this.loopLinker.putIfAbsent("song-" + 0, new HashMap<>());
                                this.loopLinker.get("song-" + 0).put(l, this.main.loopPoints.get(this.curTrack).get(l));
                                this.loopLinkerCounter.putIfAbsent("song-" + 0, new HashMap<>());
                                this.loopLinkerCounter.get("song-" + 0).put(l, 0);
                            }
                        }
                        if (cards) renderCards(mc);
                        setPitch(pitchLinker.get("song-0"));
                        if(!startQuiet) setVolume(volumeLinker.get("song-0"));
                        playTrack(songNameLinker.get("song-0"),musicLinker.get("song-0"),0);
                        if(this.trackSetChanged) this.trackSetChanged = false;
                        this.curLinkNum = "song-0";
                        if (MusicTriggers.randomInt(this.main.otherinfo.get(this.curTrack)[1]) == 1) this.onceUntilEmpty.add(curTrack);
                        else if (MusicTriggers.randomInt(this.main.otherinfo.get(this.curTrack)[1]) == 2) this.oncePerTrigger.add(curTrack);
                        else if (MusicTriggers.randomInt(this.main.otherinfo.get(this.curTrack)[1]) == 3) {
                            this.trackToDelete = this.curTrack;
                            this.indexToDelete = i;
                            this.playedEvents.clear();
                            this.playedEvents.addAll(this.picker.getInfo().getActiveTriggers());
                        }
                    } else this.trackSetChanged = true;
                } else this.onceUntilEmpty.clear();
            }
        } else {
            EventsClient.IMAGE_CARD = null;
            EventsClient.fadeCount = 1000;
            EventsClient.timer = 0;
            EventsClient.activated = false;
            EventsClient.ismoving = false;
            if(isPlaying()) stopTrack();
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void renderCards(Minecraft mc) {
        MusicTriggers.logger.debug("Finding cards to render");
        int markForDeletion = -1;
        for (int i : this.transitions.titlecards.keySet()) {
            boolean pass = false;
            if(new HashSet<>(this.picker.titleCardEvents).containsAll(this.transitions.titlecards.get(i).getTriggers()) && new HashSet<>(this.transitions.titlecards.get(i).getTriggers()).containsAll(this.picker.getInfo().getActiveTriggers())) pass=true;
            else if(this.transitions.titlecards.get(i).getVague() && new HashSet<>(this.picker.getInfo().getPlayableTriggers()).containsAll(this.transitions.titlecards.get(i).getTriggers()) && canPlayTitle.get(i)) {
                pass=true;
                canPlayTitle.put(i, false);
            }
            if (pass && mc.player != null) {
                MusicTriggers.logger.info("displaying title card "+i);
                if(!this.transitions.titlecards.get(i).getTitles().isEmpty()) mc.ingameGUI.displayTitle(TextFormatting.getValueByName(this.transitions.titlecards.get(i).getTitlecolor()).toString()+ this.transitions.titlecards.get(i).getTitles().get(ThreadLocalRandom.current().nextInt(0, this.transitions.titlecards.get(i).getTitles().size())), null, 5, 20, 20);
                if(!this.transitions.titlecards.get(i).getSubTitles().isEmpty()) mc.ingameGUI.displayTitle(null, TextFormatting.getValueByName(this.transitions.titlecards.get(i).getSubtitlecolor()).toString()+ this.transitions.titlecards.get(i).getSubTitles().get(ThreadLocalRandom.current().nextInt(0, this.transitions.titlecards.get(i).getSubTitles().size())), 5, 20, 20);
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
            if(new HashSet<>(this.picker.titleCardEvents).containsAll(this.transitions.imagecards.get(i).getTriggers()) && new HashSet<>(this.transitions.imagecards.get(i).getTriggers()).containsAll(this.picker.getInfo().getActiveTriggers())) pass=true;
            else if(this.transitions.imagecards.get(i).getVague() && new HashSet<>(this.picker.getInfo().getPlayableTriggers()).containsAll(this.transitions.imagecards.get(i).getTriggers()) && canPlayImage.get(i)) {
                pass=true;
                canPlayImage.put(i, false);
            }
            if (pass && mc.player != null) {
                if(this.transitions.imagecards.get(i).getName()!=null) {
                    MusicTriggers.logger.info("displaying image card " + this.transitions.imagecards.get(i).getName());
                    if (!this.transitions.ismoving.get(i))
                        EventsClient.IMAGE_CARD = new ResourceLocation(MusicTriggers.MODID, "textures/" + this.transitions.imagecards.get(i).getName() + ".png");
                    else {
                        EventsClient.pngs = new ArrayList<>();
                        EventsClient.ismoving = true;
                        EventsClient.movingcounter = 0;
                        File folder = new File(MusicTriggers.configDir,"songs/assets/musictriggers/textures/" + this.transitions.imagecards.get(i).getName());
                        File[] listOfPNG = folder.listFiles();
                        assert listOfPNG != null;
                        List<String> temp = new ArrayList<>();
                        for (File f : listOfPNG) temp.add(f.getName().replaceAll(".png", ""));
                        temp.sort(new Comparator<String>() {
                            public int compare(String o1, String o2) {
                                return extractInt(o1) - extractInt(o2);
                            }
                            int extractInt(String s) {
                                String num = s.replaceAll("\\D", "");
                                return num.isEmpty() ? 0 : MusicTriggers.randomInt(num);
                            }
                        });
                        for (int index = 0; index < temp.size(); index++) {
                            EventsClient.pngs.add(index, new ResourceLocation(MusicTriggers.MODID, "textures/" + this.transitions.imagecards.get(i).getName() + "/" + temp.get(index) + ".png"));
                        }
                        EventsClient.timer = Minecraft.getSystemTime();
                    }
                    EventsClient.curImageIndex = i;
                    EventsClient.activated = true;

                    if (this.transitions.imagecards.get(i).getPlayonce()) markForDeletion = i;
                    break;
                }
            }
        }
        if(markForDeletion!=-1) this.transitions.imagecards.get(markForDeletion).setName(null);
        cards = false;
    }

    public boolean theDecidingFactor(List<String> all, List<String> titlecard, String[] comparison) {
        List<String> updatedComparison = new ArrayList<>();
        boolean cont = false;
        for(String el : comparison) {
            if(titlecard.contains(el)) {
                updatedComparison = Arrays.stream(comparison)
                        .filter(element -> !element.matches(el))
                        .collect(Collectors.toList());
                if(updatedComparison.size()<=0) return true;
                cont = true;
                break;
            }
        }
        if(cont) return new HashSet<>(all).containsAll(updatedComparison);
        return false;
    }

    public String formatSongTime() {
        String ret = "No song playing";
        if(isPlaying()) ret = formattedTimeFromMilliseconds(getMillis());
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

    public List<String> getPlayableTriggers() {
        return this.picker.getInfo().getPlayableTriggers();
    }

    public AudioPlayer getPlayer() {
        return this.player;
    }

    public AudioTrack getCurPlaying() {
        return this.player.getPlayingTrack();
    }

    public boolean isPlaying() {
        return getCurPlaying()!=null;
    }

    public long getMillis() {
        return getCurPlaying().getPosition();
    }

    public void setMillis(long milliseconds) {
        getCurPlaying().setPosition(milliseconds);
    }

    public void setVolume(float volume) {
        this.getPlayer().setVolume((int)(volume*getChannelVolume()*100));
    }

    private float getChannelVolume() {
        float master = Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MASTER);
        if(SoundCategory.getByName(this.channel)==SoundCategory.MASTER) return master;
        else return master*Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.getByName(this.channel));
    }

    public void playTrack(String id, AudioTrack track, long milliseconds) {
        MusicTriggers.logger.info("Playing track from id "+id+" at a millisecond time of "+milliseconds);
        if(track!=null) {
            track.setPosition(milliseconds);
            try {
                if (!this.getPlayer().startTrack(track, false)) MusicTriggers.logger.error("Could not start track!");
            } catch (IllegalStateException e) {
                if (!this.getPlayer().startTrack(track.makeClone(), false)) MusicTriggers.logger.error("Could not start track!");
            }
        } else MusicTriggers.logger.error("Tried to play null track with id "+id+"!");
    }

    public boolean isPaused() {
        return this.getPlayer().isPaused();
    }

    public void setPaused(boolean paused, boolean fromJukeBox) {
        if(fromJukeBox && this.pausedByJukeBox) this.getPlayer().setPaused(paused);
        else if (!fromJukeBox) this.getPlayer().setPaused(paused);
    }

    public void setPitch(float pitch) {
        setFilters(pitch);
    }

    private void setFilters(float pitch) {
        //getPlayer().setFilterFactory((track, format, output) -> {});
    }

    public void stopTrack() {
        this.getPlayer().stopTrack();
    }

    public void parseRedirect(Redirect redirect) {
        redirect.parse();
        this.erroredSongDownloads.clear();
        for(String id : redirect.urlMap.keySet()) loadFromURL(id,redirect.urlMap.get(id));
        try {
            for (String file : ChannelManager.openAudioFiles.keySet()) {
                if (this.main.songholder.containsValue(file) && !loadedTracks.containsKey(file))
                    loadAudioFile(file, ChannelManager.openAudioFiles.get(file));
            }
        } catch (IOException e) {
            MusicTriggers.logger.error("Could not load one or more local audio files into the sound engine",e);
        }
        if(!this.erroredSongDownloads.isEmpty()) MusicTriggers.logger.error("Could not read audio from these sources");
        for(String error : this.erroredSongDownloads) MusicTriggers.logger.error(error);
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
                    MusicTriggers.logger.info("Track loaded from url "+url);
                } else MusicTriggers.logger.warn("Audio file with id "+id+" already exists!");
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                MusicTriggers.logger.info("Loaded a playlist from "+url);
                for(int i=1;i<playlist.getTracks().size()+1;i++) {
                    if(!Channel.this.loadedTracks.containsKey(id+"_"+i)) {
                        Channel.this.addTrackToMap(id,playlist.getTracks().get(i));
                        MusicTriggers.logger.info("Track "+i+" loaded from playlist url "+url);
                    } else MusicTriggers.logger.warn("Audio file with id "+id+"_"+i+" already exists!");
                }
            }

            @Override
            public void noMatches() {
                MusicTriggers.logger.error("No audio able to be extracted from url "+url);
                Channel.this.erroredSongDownloads.add(id+" -> "+url);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                MusicTriggers.logger.info("Load failed! "+url);
                exception.printStackTrace();
            }
        });
    }

    private void loadAudioFile(String id, File file) throws IOException {
        this.playerManager.loadItem(new AudioReference(file.getPath(),file.getName()), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if(!Channel.this.loadedTracks.containsKey(id)) {
                    Channel.this.addTrackToMap(id,track);
                    MusicTriggers.logger.info("Loaded track from file "+file.getName());
                } else MusicTriggers.logger.warn("Audio file with id "+id+" already exists!");
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                MusicTriggers.logger.info("Loaded track from file "+file.getName());
                for(int i=1;i<playlist.getTracks().size()+1;i++) {
                    if(!Channel.this.loadedTracks.containsKey(id+"_"+i)) {
                        Channel.this.addTrackToMap(id,playlist.getTracks().get(i));
                        MusicTriggers.logger.info("Track "+i+" loaded from playlist file "+file.getName());
                    } else MusicTriggers.logger.warn("Audio file with id "+id+"_"+i+" already exists!");
                }
            }

            @Override
            public void noMatches() {
                MusicTriggers.logger.error("No audio able to be extracted from file "+file.getName());
                Channel.this.erroredSongDownloads.add(id+" -> "+file.getName());
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                MusicTriggers.logger.info("Load failed! "+file.getName());
                exception.printStackTrace();
            }
        });
    }

    public void encode(ByteBuf buf) {
        String channelName = this.getChannelName();
        String name = this.curTrackHolder;
        String uuid = Minecraft.getMinecraft().player.getUniqueID().toString();
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
        for(String song : this.toSend.getMenuSongs()) {
            buf.writeInt(song.length());
            buf.writeCharSequence(song, StandardCharsets.UTF_8);
        }
        buf.writeInt(this.picker.getInfo().getPlayableTriggers().size());
        for(String trigger : this.picker.getInfo().getPlayableTriggers()) {
            buf.writeInt(trigger.length());
            buf.writeCharSequence(trigger, StandardCharsets.UTF_8);
        }
        buf.writeInt(this.toSend.getHomeTriggers().size());
        for(ServerChannelData.Home home : this.toSend.getHomeTriggers()) buf.writeInt(home.getRange());
        buf.writeInt(this.toSend.getStructureTriggers().size());
        for(ServerChannelData.Structure structure : this.toSend.getStructureTriggers()) {
            buf.writeInt(structure.getTrigger().length());
            buf.writeInt(structure.getStructure().length());
            buf.writeCharSequence(structure.getTrigger(), StandardCharsets.UTF_8);
            buf.writeCharSequence(structure.getStructure(), StandardCharsets.UTF_8);
            buf.writeLong(structure.getPos().toLong());
            buf.writeInt(structure.getDimension());
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
            buf.writeBoolean(mob.getVictory());
            buf.writeInt(mob.getVictoryID());
            buf.writeCharSequence(mob.getInfernal(), StandardCharsets.UTF_8);
            buf.writeInt(mob.getMobLevel());
            buf.writeInt(mob.getVictoryTimeout());
            buf.writeCharSequence(mob.getNbtKey(), StandardCharsets.UTF_8);
            buf.writeCharSequence(mob.getChampion(), StandardCharsets.UTF_8);
        }

    }

    public void sync(ClientSync fromServer) {
        this.sync = fromServer;
    }

    private void changeTrack(Minecraft mc) {
        EventsClient.GuiCounter = 1;
        String songNum = null;
        for (String song : this.musicLinker.keySet()) {
            if (this.triggerLinker.get(song) != null) {
                if (theDecidingFactor(this.picker.getInfo().getPlayableTriggers(), this.picker.getInfo().getActiveTriggers(), this.triggerLinker.get(song)) && mc.player != null) {
                    songNum = song;
                    break;
                }
            }
        }
        if (songNum == null) {
            if(this.curLinkNum==null) {
                MusicTriggers.logger.warn("Index of current music was null! Falling back to default fade out volume. You should report this");
                this.curLinkNum = "song-"+0;
            }
            triggerLinker.clear();
            loopLinker.clear();
            loopLinkerCounter.clear();
            if (!fadingOut) {
                fadingOut = true;
                tempFadeOut = this.picker.curFadeOut;
                if (isPlaying() && volumeLinker.get(curLinkNum)!=null) saveVolOut = volumeLinker.get(curLinkNum);
                else tempFadeOut = 0;
            } else if (reverseFade) reverseFade = false;
        } else {
            nullFromLink = true;
            EventsClient.IMAGE_CARD = null;
            EventsClient.fadeCount = 1000;
            EventsClient.timer = 0;
            EventsClient.activated = false;
            EventsClient.ismoving = false;
            cards = true;
            for (String song : this.musicLinker.keySet()) {
                if(loopLinkerCounter.get(song)!=null) for (int l : loopLinkerCounter.get(song).keySet()) loopLinkerCounter.get(song).put(l, 0);
                if (song.matches(songNum)) {
                    curLinkNum = song;
                    curTrackHolder = songNameLinker.get(song);
                    playTrack(songNameLinker.get(song), musicLinker.get(song), getCurPlaying().getPosition());
                }
            }
        }
    }

    private void clearSongs() {
        stopTrack();
        oncePerTrigger.clear();
        onceUntilEmpty.clear();
        removeTrack(trackToDelete, indexToDelete, playedEvents);
        this.fadingOut = false;
        this.musicLinker.clear();
        EventsClient.IMAGE_CARD = null;
        EventsClient.fadeCount = 1000;
        EventsClient.timer = 0;
        EventsClient.activated = false;
        EventsClient.ismoving = false;
        curTrack = null;
        curTrackHolder = null;
        this.cards = true;
        tempFadeIn = this.picker.curFadeIn;
    }

    private void removeTrack(String track, int index, List<String> events) {
        if(track!=null) {
            this.curTrack = null;
            this.picker.getInfo().getCurrentSongList().remove(this.picker.getInfo().getCurrentSongList().get(index));
            for (String ev : events) {
                String[] trigger = ev.split("-");
                if (trigger.length==1) trigger = (ev+"-_").split("-");
                this.handler.TriggerIdentifierMap.get(trigger[0]).get(trigger[1]).remove(track);
                if(this.handler.TriggerIdentifierMap.get(trigger[0]).get(trigger[1]).isEmpty()) {
                    this.handler.TriggerIdentifierMap.get(trigger[0]).remove(trigger[1]);
                    this.handler.TriggerInfoMap.remove(trigger[0]+"-"+trigger[1]);
                }
                if(this.handler.TriggerIdentifierMap.get(trigger[0]).isEmpty()) {
                    this.handler.TriggerIdentifierMap.remove(trigger[0]);
                    this.handler.TriggerInfoMap.remove(trigger[0]);
                }
            }
            trackToDelete=null;
            playedEvents.clear();
        }
    }

    private void parseConfigs() {
        parseRedirect(this.redirect);
        this.main.parse();
        this.transitions.parse();
        this.commands.parse();
        this.handler.registerSounds(this.main,getChannelName());
    }

    private void clearAllListsAndMaps() {
        this.main.clearMaps();
        this.transitions.clearMaps();
        this.commands.commandMap.clear();
        this.handler.clearListsAndMaps();
        this.redirect.urlMap.clear();
        this.picker.clearListsAndMaps();
        this.loadedTracks.clear();
        this.musicLinker.clear();
        this.songNameLinker.clear();
        this.triggerLinker.clear();
        this.volumeLinker.clear();
        this.pitchLinker.clear();
        this.loopLinker.clear();
        this.loopLinkerCounter.clear();
        this.oncePerTrigger.clear();
        this.onceUntilEmpty.clear();
        this.playedEvents.clear();
        this.canPlayTitle.clear();
        this.canPlayImage.clear();
        this.commandsForPacket.clear();
        this.erroredSongDownloads.clear();
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
        this.cards = true;
        this.nullFromLink = false;
        this.trackSetChanged = true;
        this.curLinkNum = "song-0";
        parseConfigs();
    }
}
