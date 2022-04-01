package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.client.gui.GuiCurPlaying;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.config.configRegistry;
import mods.thecomputerizer.musictriggers.config.configTitleCards;
import mods.thecomputerizer.musictriggers.config.configToml;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.audio.SoundManipulator;
import mods.thecomputerizer.musictriggers.util.audio.setVolumeSound;
import mods.thecomputerizer.musictriggers.util.packets.CurSong;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.Source;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
public class MusicPlayer {
    public static List<String> curTrackList;
    public static List<String> holder;
    public static String curTrack;
    public static String curTrackHolder;
    public static SoundInstance curMusic;
    public static Random rand = new Random();
    public static MinecraftClient mc = MinecraftClient.getInstance();
    public static SoundManager sh;
    public static int tickCounter = 0;
    public static boolean fading = false;
    private static int tempFade = 0;
    private static float saveVol = 1;
    public static List<String> tempTitleCards = new ArrayList<>();
    public static boolean delay = false;
    public static int delayTime = 0;
    public static SoundEvent fromRecord = new SoundEvent(new Identifier("nonsensicalresourcelocation"));
    public static boolean playing = false;
    public static boolean reloading = false;
    public static boolean cards = true;
    public static boolean finish = false;
    public static HashMap<String, setVolumeSound> musicLinker = new HashMap<>();
    public static HashMap<String, String[]> triggerLinker = new HashMap<>();
    public static HashMap<String, Float> volumeLinker = new HashMap<>();
    public static HashMap<String, Map<Integer, String[]>> loopLinker = new HashMap<>();
    public static HashMap<String, Map<Integer, Integer>> loopLinkerCounter = new HashMap<>();
    public static List<String> oncePerTrigger = new ArrayList<>();
    public static List<String> onceUntilEmpty = new ArrayList<>();
    private static String trackToDelete;
    private static int indexToDelete;
    private static List<String> playedEvents = new ArrayList<>();
    private static SoundInstance playedMusic;
    public static long curMusicTimer = 0;
    public static Source curMusicSource;
    public static Map<Integer, Boolean> canPlayTitle = new HashMap<>();
    public static Map<Integer, Boolean> canPlayImage = new HashMap<>();

    public static Map<SoundInstance, Channel.SourceManager> sources;

    public static void onTick() {
        sh = mc.getSoundManager();
        if(!reloading) {
            if (MusicPicker.fishBool) MusicPicker.fishingStart++;
            if (MusicPicker.waterBool) MusicPicker.waterStart++;
            if(MusicPicker.elytraBool) MusicPicker.elytraStart++;
            for (Map.Entry<String, Integer> stringListEntry : MusicPicker.triggerPersistence.entrySet()) {
                String eventID = ((Map.Entry) stringListEntry).getKey().toString();
                MusicPicker.triggerPersistence.putIfAbsent(eventID, 0);
                if (MusicPicker.triggerPersistence.get(eventID) > 0) {
                    MusicPicker.triggerPersistence.put(eventID, MusicPicker.triggerPersistence.get(eventID) - 1);
                }
            }
            if(curTrack!=null && sh.isPlaying(curMusic) && configToml.loopPoints.containsKey(curTrack)) {
                for(String key : musicLinker.keySet()) {
                    if(loopLinker.get(key)!=null) {
                        for (int i : loopLinker.get(key).keySet()) {
                            try {
                                if (loopLinkerCounter.get(key).get(i) < Integer.parseInt(loopLinker.get(key).get(i)[0]) && Integer.parseInt(loopLinker.get(key).get(i)[2]) <= curMusicTimer) {
                                    MusicTriggersCommon.logger.info("Loop boundary passed");
                                    SoundManipulator.setMillisecondTimeForSource(musicLinker.get(key), Integer.parseInt(loopLinker.get(key).get(i)[1]));
                                    loopLinkerCounter.get(key).put(i, loopLinkerCounter.get(key).get(i) + 1);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw new RuntimeException("There was a problem while trying to loop - Check the log for details");
                            }
                        }
                    }
                }
            }
            if(curTrack!=null && sh.isPlaying(curMusic) && mc.currentScreen instanceof GuiCurPlaying) {
                ((GuiCurPlaying)mc.currentScreen).setSlider(GuiCurPlaying.getSongPosInSeconds(curMusic));
            }
            if (fading) {
                if (tempFade == 0) {
                    fading = false;
                    sh.stop(curMusic);
                    curMusicSource = null;
                    sh.updateSoundVolume(SoundCategory.MASTER, saveVol);
                    eventsClient.IMAGE_CARD = null;
                    eventsClient.fadeCount = 1000;
                    eventsClient.timer = 0;
                    eventsClient.activated = false;
                    eventsClient.ismoving = false;
                    cards = true;
                } else {
                    sh.updateSoundVolume(SoundCategory.MASTER, saveVol * (float) (((double) tempFade) / ((double) MusicPicker.curFade)));
                    tempFade -= 1;
                }
            }
            if (delay) {
                delayTime -= 1;
                if (delayTime <= 0) {
                    delay = false;
                }
            }
            if (tickCounter % 10 == 0 && !fading && !delay) {
                if (MusicPicker.player != null && (MusicPicker.player.getActiveItem().getItem() instanceof MusicTriggersRecord))
                    fromRecord = ((MusicTriggersRecord) MusicPicker.player.getActiveItem().getItem()).getSound();
                else fromRecord = new SoundEvent(new Identifier("nonsensicalresourcelocation"));
                playing = false;
                if (MusicPicker.player != null) {
                    for (int x = MusicPicker.player.getChunkPos().x - 3; x <= MusicPicker.player.getChunkPos().x  + 3; x++) {
                        for (int z = MusicPicker.player.getChunkPos().z - 3; z <= MusicPicker.player.getChunkPos().z + 3; z++) {
                            Set<BlockPos> currentChunkTEPos = MusicPicker.world.getChunk(x, z).getBlockEntityPositions();
                            for (BlockPos b : currentChunkTEPos) {
                                if (MusicPicker.world.getChunk(x, z).getBlockEntity(b) instanceof JukeboxBlockEntity te) {
                                    if (te.getCachedState().get(JukeboxBlock.HAS_RECORD)) playing = true;
                                }
                            }
                        }
                    }
                }
                holder = MusicPicker.playThese();
                if (holder != null && !holder.isEmpty() && !playing) {
                    for(int i : canPlayTitle.keySet()) {
                        if(!canPlayTitle.get(i) && !MusicPicker.playableList.containsAll(configTitleCards.titlecards.get(i).getTriggers())) canPlayTitle.put(i, true);
                    }
                    for(int i : canPlayImage.keySet()) {
                        if(!canPlayImage.get(i) && !MusicPicker.playableList.containsAll(configTitleCards.imagecards.get(i).getTriggers())) canPlayImage.put(i, true);
                    }
                    for(String playable : MusicPicker.playableList) {
                        if(!MusicPicker.titleCardEvents.contains(playable)) {
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get(playable)[34])) {
                                if(!SoundHandler.TriggerInfoMap.get(playable)[10].matches("_")) {
                                    String[] trigger = playable.split("-");
                                    SoundHandler.TriggerSongMap.get(trigger[0]).entrySet().removeIf(stringStringEntry -> stringStringEntry.getValue().matches(trigger[1]));
                                } else SoundHandler.TriggerSongMap.remove(playable);
                                SoundHandler.TriggerInfoMap.remove(playable);
                            }
                        }
                    }
                    if (curTrackList == null && !finish) curTrackList = holder;
                    if (curMusic != null) {
                        if (!sh.isPlaying(curMusic) || mc.options.getSoundVolume(SoundCategory.MUSIC) == 0 || mc.options.getSoundVolume(SoundCategory.MASTER) == 0) {
                            finish = false;
                            sh.stopAll();
                            curMusic = null;
                            curMusicSource = null;
                            delay = true;
                            delayTime = MusicPicker.curDelay;
                        }
                    }
                    if(!finish) {
                        if (MusicPicker.shouldChange || !Arrays.equals(curTrackList.toArray(new String[0]), holder.toArray(new String[0]))) {
                            removeTrack(trackToDelete,indexToDelete,playedEvents,playedMusic);
                            if(curTrackList.size()!=0) changeTrack();
                            else curTrackList = null;
                        } else if (curMusic == null && mc.options.getSoundVolume(SoundCategory.MASTER) > 0 && mc.options.getSoundVolume(SoundCategory.MUSIC) > 0) {
                            triggerLinker = new HashMap<>();
                            musicLinker = new HashMap<>();
                            eventsClient.GuiCounter = 0;
                            curTrackList = curTrackList.stream().filter(track -> !oncePerTrigger.contains(track)).collect(Collectors.toList());
                            curTrackList = curTrackList.stream().filter(track -> !onceUntilEmpty.contains(track)).collect(Collectors.toList());
                            if (curTrackList.size() >= 1) {
                                int i = ThreadLocalRandom.current().nextInt(0,curTrackList.size());
                                if (curTrackList.size() > 1 && curTrack != null) {
                                    int total = curTrackList.stream().mapToInt(s -> Integer.parseInt(configToml.otherinfo.get(s)[3])).sum();
                                    int j;
                                    for(j=0;j<1000;j++) {
                                        int r = ThreadLocalRandom.current().nextInt(1,total+1);
                                        MusicTriggersCommon.logger.debug("Random was between 1 and "+(total+1)+" "+r+" was chosen");
                                        String temp = " ";
                                        for (String s : curTrackList) {
                                            if (r < Integer.parseInt(configToml.otherinfo.get(s)[3])) {
                                                temp = s;
                                                break;
                                            }
                                            r-=Integer.parseInt(configToml.otherinfo.get(s)[3]);
                                        }
                                        if(!temp.matches(curTrack) && !temp.matches(" ")) {
                                            curTrack = temp;
                                            break;
                                        }
                                    }
                                    if(j>=1000) MusicTriggersCommon.logger.warn("Attempt to get non duplicate song passed 1000 tries! Forcing current song " + configToml.songholder.get(curTrack) + " to play.");
                                }
                                else curTrack = curTrackList.get(i);
                                MusicTriggersCommon.logger.debug(curTrack+" was chosen");
                                if (curTrack != null) {
                                    finish = Boolean.parseBoolean(configToml.otherinfo.get(curTrack)[2]);
                                    curTrackHolder = configToml.songholder.get(curTrack);
                                    MusicTriggersCommon.logger.info("Attempting to play track: " + curTrackHolder);
                                    if (configToml.triggerlinking.get(curTrack) != null) {
                                        triggerLinker.put("song-" + 0, configToml.triggerlinking.get(curTrack).get(curTrack));
                                        musicLinker.put("song-" + 0, new setVolumeSound(new Identifier(MusicTriggersCommon.MODID, "music." + curTrackHolder), SoundCategory.MUSIC, Float.parseFloat(configToml.otherinfo.get(curTrack)[4]), Float.parseFloat(configToml.otherinfo.get(curTrack)[0]), false, 1, SoundInstance.AttenuationType.NONE, 0F, 0F, 0F));
                                        volumeLinker.put("song-" + 0, Float.parseFloat(configToml.otherinfo.get(curTrack)[4]));
                                        for(int l : configToml.loopPoints.get(curTrack).keySet()) {
                                            loopLinker.putIfAbsent("song-" + 0, new HashMap<>());
                                            loopLinker.get("song-" + 0).put(l, configToml.loopPoints.get(curTrack).get(l));
                                            loopLinkerCounter.putIfAbsent("song-" + 0, new HashMap<>());
                                            loopLinkerCounter.get("song-" + 0).put(l, 0);
                                        }
                                        int linkcounter = 0;
                                        for (String song : configToml.triggerlinking.get(curTrack).keySet()) {
                                            if (!song.matches(curTrack)) {
                                                triggerLinker.put("song-" + linkcounter, configToml.triggerlinking.get(curTrack).get(song));
                                                musicLinker.put("song-" + linkcounter, new setVolumeSound(new Identifier(MusicTriggersCommon.MODID, "music." + song), SoundCategory.MUSIC, Float.parseFloat(configToml.otherinfo.get(curTrack)[4]),
                                                        Float.parseFloat(configToml.otherlinkinginfo.get(curTrack).get(song)[0]), false, 1, SoundInstance.AttenuationType.NONE, 0F, 0F, 0F));
                                                volumeLinker.put("song-" + linkcounter, Float.parseFloat(configToml.otherlinkinginfo.get(curTrack).get(song)[1]));
                                                if(configToml.linkingLoopPoints.get(curTrack)!=null && configToml.linkingLoopPoints.get(curTrack).get(song)!=null) {
                                                    for (int l : configToml.linkingLoopPoints.get(curTrack).get(song).keySet()) {
                                                        loopLinker.putIfAbsent("song-" + linkcounter, new HashMap<>());
                                                        loopLinker.get("song-" + linkcounter).put(l, configToml.linkingLoopPoints.get(curTrack).get(song).get(l));
                                                        loopLinkerCounter.putIfAbsent("song-" + linkcounter, new HashMap<>());
                                                        loopLinkerCounter.get("song-" + linkcounter).put(l, 0);
                                                    }
                                                }
                                            }
                                            linkcounter++;
                                        }
                                    } else {
                                        musicLinker.put("song-" + 0, new setVolumeSound(new Identifier(MusicTriggersCommon.MODID, "music." + curTrackHolder), SoundCategory.MUSIC, Float.parseFloat(configToml.otherinfo.get(curTrack)[4]), Float.parseFloat(configToml.otherinfo.get(curTrack)[0]), false, 1, SoundInstance.AttenuationType.NONE, 0F, 0F, 0F));
                                        for(int l : configToml.loopPoints.get(curTrack).keySet()) {
                                            loopLinker.putIfAbsent("song-" + 0, new HashMap<>());
                                            loopLinker.get("song-" + 0).put(l, configToml.loopPoints.get(curTrack).get(l));
                                            loopLinkerCounter.putIfAbsent("song-" + 0, new HashMap<>());
                                            loopLinkerCounter.get("song-" + 0).put(l, 0);
                                        }
                                    }
                                    if (MusicPicker.player != null) {
                                        if (!configRegistry.clientSideOnly)
                                            PacketHandler.sendToServer(CurSong.id, CurSong.encode(curTrackHolder, MusicPicker.player.getUuid()));
                                        else CurSong.curSong.put(MusicPicker.player.getUuid(), curTrackHolder);
                                    }
                                    sh.stopAll();
                                    if (cards) renderCards();
                                    for (Map.Entry<String, setVolumeSound> stringListEntry : musicLinker.entrySet()) {
                                        String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                                        if (!checkThis.matches("song-0")) musicLinker.get(checkThis).setVolume(Float.MIN_VALUE);
                                        else curMusic = musicLinker.get(checkThis);
                                        sh.play(musicLinker.get(checkThis));
                                    }
                                    curMusicSource = sh.soundSystem.sources.get(curMusic).source;
                                    if (Integer.parseInt(configToml.otherinfo.get(curTrack)[1])==1) onceUntilEmpty.add(curTrack);
                                    if (Integer.parseInt(configToml.otherinfo.get(curTrack)[1])==2) oncePerTrigger.add(curTrack);
                                    else if (Integer.parseInt(configToml.otherinfo.get(curTrack)[1])==3) {
                                        trackToDelete = curTrack;
                                        indexToDelete = i;
                                        playedEvents = MusicPicker.titleCardEvents;
                                        playedMusic = curMusic;
                                    }
                                } else curTrackList = null;
                            }
                            else onceUntilEmpty = new ArrayList<>();
                        }
                    }
                } else if(!finish || playing) {
                    curTrack = null;
                    curTrackHolder = null;
                    eventsClient.IMAGE_CARD = null;
                    eventsClient.fadeCount = 1000;
                    eventsClient.timer = 0;
                    eventsClient.activated = false;
                    eventsClient.ismoving = false;
                    cards = true;
                    if (curMusic != null) {
                        for(String is : musicLinker.keySet()) {
                            sh.stop(musicLinker.get(is));
                        }
                        curMusic = null;
                        curMusicSource = null;
                    }
                }
            }
        }
        tickCounter++;
    }

    public static void renderCards() {
        MusicTriggersCommon.logger.info("Finding cards to render");
        int markForDeletion = -1;
        for (int i : configTitleCards.titlecards.keySet()) {
            boolean pass = false;
            if(MusicPicker.titleCardEvents.containsAll(configTitleCards.titlecards.get(i).getTriggers()) && configTitleCards.titlecards.get(i).getTriggers().containsAll(MusicPicker.titleCardEvents)) pass=true;
            else if(configTitleCards.titlecards.get(i).getVague() && MusicPicker.playableList.containsAll(configTitleCards.titlecards.get(i).getTriggers()) && canPlayTitle.get(i)) {
                pass=true;
                canPlayTitle.put(i, false);
            }
            if (pass && mc.player != null) {
                MusicTriggersCommon.logger.info("displaying title card "+i);
                if(!configTitleCards.titlecards.get(i).getTitles().isEmpty()) mc.inGameHud.setTitle(Texts.setStyleIfAbsent(new LiteralText(configTitleCards.titlecards.get(i).getTitles().get(ThreadLocalRandom.current().nextInt(0, configTitleCards.titlecards.get(i).getTitles().size()))), Style.EMPTY.withFormatting(Formatting.valueOf(configTitleCards.titlecards.get(i).getTitlecolor()))));
                if(!configTitleCards.titlecards.get(i).getSubTitles().isEmpty()) mc.inGameHud.setSubtitle(Texts.setStyleIfAbsent(new LiteralText(configTitleCards.titlecards.get(i).getSubTitles().get(ThreadLocalRandom.current().nextInt(0, configTitleCards.titlecards.get(i).getSubTitles().size()))), Style.EMPTY.withFormatting(Formatting.valueOf(configTitleCards.titlecards.get(i).getSubtitlecolor()))));
                if(configTitleCards.titlecards.get(i).getPlayonce()) {
                    markForDeletion = i;
                }
                break;
            }
        }
        if(markForDeletion!=-1) {
            configTitleCards.titlecards.remove(markForDeletion);
            markForDeletion = -1;
        }
        for (int i : configTitleCards.imagecards.keySet()) {
            boolean pass = false;
            if(MusicPicker.titleCardEvents.containsAll(configTitleCards.imagecards.get(i).getTriggers()) && configTitleCards.imagecards.get(i).getTriggers().containsAll(MusicPicker.titleCardEvents)) pass=true;
            else if(configTitleCards.imagecards.get(i).getVague() && MusicPicker.playableList.containsAll(configTitleCards.imagecards.get(i).getTriggers()) && canPlayImage.get(i)) {
                pass=true;
                canPlayImage.put(i, false);
            }
            if (pass && mc.player != null) {
                if(configTitleCards.imagecards.get(i).getName()!=null) {
                    MusicTriggersCommon.logger.info("displaying image card " + configTitleCards.imagecards.get(i).getName());
                    if (!configTitleCards.ismoving.get(i)) {
                        eventsClient.IMAGE_CARD = new Identifier(MusicTriggersCommon.MODID, "textures/" + configTitleCards.imagecards.get(i).getName() + ".png");
                    } else {
                        eventsClient.pngs = new ArrayList<>();
                        eventsClient.ismoving = true;
                        eventsClient.movingcounter = 0;
                        File folder = new File("." + "/config/MusicTriggers/songs/assets/musictriggers/textures/" + configTitleCards.imagecards.get(i).getName());
                        File[] listOfPNG = folder.listFiles();
                        assert listOfPNG != null;
                        List<String> temp = new ArrayList<>();
                        for (File f : listOfPNG) {
                            temp.add(f.getName().replaceAll(".png", ""));
                        }
                        temp.sort(new Comparator<String>() {
                            public int compare(String o1, String o2) {
                                return extractInt(o1) - extractInt(o2);
                            }

                            int extractInt(String s) {
                                String num = s.replaceAll("\\D", "");
                                return num.isEmpty() ? 0 : Integer.parseInt(num);
                            }
                        });
                        for (int index = 0; index < temp.size(); index++) {
                            eventsClient.pngs.add(index, new Identifier(MusicTriggersCommon.MODID, "textures/" + configTitleCards.imagecards.get(i).getName() + "/" + temp.get(index) + ".png"));
                        }
                    }
                    eventsClient.curImageIndex = i;
                    eventsClient.activated = true;

                    if (configTitleCards.imagecards.get(i).getPlayonce()) {
                        markForDeletion = i;
                    }
                    break;
                }
            }
        }
        if(markForDeletion!=-1) {
            configTitleCards.imagecards.get(markForDeletion).setName(null);
        }
        cards = false;
    }

    public static String[] stringBreaker(String s, String regex) {
        return s.split(regex);
    }

    public static boolean theDecidingFactor(List<String> all, List<String> titlecard, String[] comparison) {
        List<String> updatedComparison = new ArrayList<>();
        boolean cont = false;
        for(String el : comparison) {
            if(titlecard.contains(el)) {
                updatedComparison = Arrays.stream(comparison)
                        .filter(element -> !element.matches(el))
                        .collect(Collectors.toList());
                if(updatedComparison.size()<=0) {
                    return true;
                }
                cont = true;
                break;
            }
        }
        if(cont) {
            return all.containsAll(updatedComparison);
        }
        return false;
    }

    public static String formatSongTime() {
        String ret = "No song playing";
        if(curMusic!=null && sh.soundSystem.sources.get(curMusic)!=null) {
            try {
                float milliseconds = curMusicTimer*curMusic.getPitch();
                if(milliseconds!=Integer.MAX_VALUE) {
                    if (milliseconds == -1) milliseconds = 0;
                    float seconds = milliseconds / 1000f;
                    if (seconds % 60 < 10)
                        ret = (int) (seconds / 60) + ":0" + (int) (seconds % 60) + formatMilliseconds(milliseconds);
                    else ret = (int) (seconds / 60) + ":" + (int) (seconds % 60) + formatMilliseconds(milliseconds);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    private static String formatMilliseconds(float milliseconds) {
        if(milliseconds%1000<10) return ":00"+(int)(milliseconds%1000);
        else if(milliseconds%1000<100) return ":0"+(int)(milliseconds%1000);
        else return ":"+(int)(milliseconds%1000);
    }

    private static void removeTrack(String track, int index, List<String> events, SoundInstance playing) {
        if(track!=null) {
            sh.stop(playing);
            curMusicSource=null;
            curTrackList.remove(index);
            for (String ev : events) {
                String trigger = StringUtils.substringBefore(ev, "-");
                SoundHandler.TriggerSongMap.get(trigger).remove(track);
                if(SoundHandler.TriggerSongMap.get(trigger).isEmpty()) SoundHandler.TriggerSongMap.remove(trigger);
            }
            trackToDelete=null;
            playedEvents = new ArrayList<>();
        }
    }

    public static void changeTrack() {
        curTrackList = null;
        tempTitleCards = MusicPicker.titleCardEvents;
        String songNum = null;
        for (Map.Entry<String, setVolumeSound> stringListEntry : musicLinker.entrySet()) {
            String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
            if (triggerLinker.get(checkThis) != null) {
                if (theDecidingFactor(MusicPicker.playableList, tempTitleCards, triggerLinker.get(checkThis)) && mc.player != null) {
                    songNum = checkThis;
                    break;
                }
            }
        }
        if (songNum == null) {
            oncePerTrigger = new ArrayList<>();
            onceUntilEmpty = new ArrayList<>();
            triggerLinker = new HashMap<>();
            musicLinker = new HashMap<>();
            if (MusicPicker.curFade == 0) {
                mc.getSoundManager().stop(curMusic);
                eventsClient.IMAGE_CARD = null;
                eventsClient.fadeCount = 1000;
                eventsClient.timer = 0;
                eventsClient.activated = false;
                eventsClient.ismoving = false;
                cards = true;
            } else {
                fading = true;
                tempFade = MusicPicker.curFade;
                saveVol = mc.options.getSoundVolume(SoundCategory.MASTER);
            }
        } else {
            curTrackList = null;
            eventsClient.IMAGE_CARD = null;
            eventsClient.fadeCount = 1000;
            eventsClient.timer = 0;
            eventsClient.activated = false;
            eventsClient.ismoving = false;
            cards = true;
            for (Map.Entry<String, setVolumeSound> stringListEntry : musicLinker.entrySet()) {
                String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                if (checkThis.matches(songNum)) {
                    musicLinker.get(checkThis).setVolume(volumeLinker.get(songNum));
                    if (sources.get(musicLinker.get(checkThis)) != null) {
                        String finalSongNum = songNum;
                        sources.get(musicLinker.get(checkThis)).run(sound -> sound.setVolume(volumeLinker.get(finalSongNum)));
                    }
                    curMusic = musicLinker.get(checkThis);
                    curTrackHolder = musicLinker.get(checkThis).getId().toString().replaceAll("music.", "").replaceAll("riggers:", "");
                    if (MusicPicker.player != null) {
                        if (!configRegistry.clientSideOnly) {
                            PacketHandler.sendToServer(CurSong.id, CurSong.encode(curTrackHolder, MusicPicker.player.getUuid()));
                        } else {
                            CurSong.curSong.put(MusicPicker.player.getUuid(), curTrackHolder);
                        }
                    }
                } else {
                    musicLinker.get(checkThis).setVolume(Float.MIN_VALUE*1000);
                    if (sources.get(musicLinker.get(checkThis)) != null) {
                        sources.get(musicLinker.get(checkThis)).run(sound -> sound.setVolume(Float.MIN_VALUE*1000));
                    }
                }
            }
        }
        MusicPicker.shouldChange = false;
    }
}
