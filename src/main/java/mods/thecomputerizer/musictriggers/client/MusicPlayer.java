package mods.thecomputerizer.musictriggers.client;

import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.platform.InputConstants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.openal.AL10;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static mods.thecomputerizer.musictriggers.util.packets.CurSong.curSong;

@SuppressWarnings("rawtypes")
@Mod.EventBusSubscriber(modid = MusicTriggers.MODID, value = Dist.CLIENT)
public class MusicPlayer {
    public static final KeyMapping RELOAD = new KeyMapping("key.musictriggers.menu", KeyConflictContext.UNIVERSAL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.musictriggers");

    public static List<String> curTrackList;
    public static List<String> holder;
    public static String curTrack;
    public static String curTrackHolder;
    public static SoundInstance curMusic;
    public static Random rand = new Random();
    public static Minecraft mc = Minecraft.getInstance();
    public static net.minecraft.client.sounds.SoundManager sh;
    public static int tickCounter = 0;
    public static boolean fading = false;
    private static int tempFade = 0;
    private static float saveVol = 1;
    public static List<String> tempTitleCards = new ArrayList<>();
    public static boolean delay = false;
    public static int delayTime = 0;
    public static SoundEvent fromRecord = new SoundEvent(new ResourceLocation("nonsensicalresourcelocation"));
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
    public static Channel curMusicSource;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    public static Map<Integer, Boolean> canPlayTitle = new HashMap<>();
    public static Map<Integer, Boolean> canPlayImage = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onTick(TickEvent.ClientTickEvent event) {
        sh = mc.getSoundManager();
        if(!reloading && tickCounter % 2 == 0) {
            if (MusicPicker.fishBool) MusicPicker.fishingStart++;
            if (MusicPicker.waterBool) MusicPicker.waterStart++;
            for (Map.Entry<String, Integer> stringListEntry : MusicPicker.triggerPersistence.entrySet()) {
                String eventID = ((Map.Entry) stringListEntry).getKey().toString();
                MusicPicker.triggerPersistence.putIfAbsent(eventID, 0);
                if (MusicPicker.triggerPersistence.get(eventID) > 0) {
                    MusicPicker.triggerPersistence.put(eventID, MusicPicker.triggerPersistence.get(eventID) - 1);
                }
            }
            if(curTrack!=null && sh.isActive(curMusic) && configToml.loopPoints.containsKey(curTrack)) {
                for(String key : musicLinker.keySet()) {
                    if(loopLinker.get(key)!=null) {
                        for (int i : loopLinker.get(key).keySet()) {
                            try {
                                if (loopLinkerCounter.get(key).get(i) < Integer.parseInt(loopLinker.get(key).get(i)[0]) && Integer.parseInt(loopLinker.get(key).get(i)[2]) <= curMusicTimer) {
                                    MusicTriggers.logger.info("Loop boundary passed");
                                    //executor.execute(() ->{
                                        SoundManipulator.setMillisecondTimeForSource(musicLinker.get(key), Integer.parseInt(loopLinker.get(key).get(i)[1]));
                                        loopLinkerCounter.get(key).put(i, loopLinkerCounter.get(key).get(i) + 1);
                                    //});
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw new RuntimeException("There was a problem while trying to loop - Check the log for details");
                            }
                        }
                    }
                }
            }
            if(curTrack!=null && sh.isActive(curMusic) && mc.screen instanceof GuiCurPlaying) {
                ((GuiCurPlaying)mc.screen).setSlider(GuiCurPlaying.getSongPosInSeconds(curMusic));
            }
            if (fading) {
                if (tempFade == 0) {
                    fading = false;
                    sh.stop(curMusic);
                    curMusicSource = null;
                    sh.updateSourceVolume(SoundSource.MASTER, saveVol);
                    eventsClient.IMAGE_CARD = null;
                    eventsClient.fadeCount = 1000;
                    eventsClient.timer = 0;
                    eventsClient.activated = false;
                    eventsClient.ismoving = false;
                    cards = true;
                } else {
                    sh.updateSourceVolume(SoundSource.MASTER, saveVol * (float) (((double) tempFade) / ((double) MusicPicker.curFade)));
                    tempFade -= 1;
                }
            }
            if (delay) {
                delayTime -= 1;
                if (delayTime <= 0) delay = false;
            }
            if (tickCounter % 10 == 0 && !fading && !delay) {
                if (MusicPicker.player != null && (MusicPicker.player.getMainHandItem().getItem() instanceof MusicTriggersRecord)) fromRecord = ((MusicTriggersRecord) MusicPicker.player.getMainHandItem().getItem()).getSound();
                else fromRecord = new SoundEvent(new ResourceLocation("nonsensicalresourcelocation"));
                playing = false;
                if (MusicPicker.player != null) {
                    for (int x = MusicPicker.player.chunkPosition().x - 3; x <= MusicPicker.player.chunkPosition().x  + 3; x++) {
                        for (int z = MusicPicker.player.chunkPosition().z - 3; z <= MusicPicker.player.chunkPosition().z + 3; z++) {
                            Set<BlockPos> currentChunkTEPos = MusicPicker.world.getChunk(x, z).getBlockEntitiesPos();
                            for (BlockPos b : currentChunkTEPos) {
                                if (MusicPicker.world.getChunk(x, z).getBlockEntity(b) instanceof JukeboxBlockEntity te) {
                                    if (te.getBlockState().getValue(JukeboxBlock.HAS_RECORD)) playing = true;
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
                        if (!sh.isActive(curMusic) || mc.options.getSoundSourceVolume(SoundSource.MUSIC) == 0 || mc.options.getSoundSourceVolume(SoundSource.MASTER) == 0) {
                            finish = false;
                            sh.stop();
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
                        } else if (curMusic == null && mc.options.getSoundSourceVolume(SoundSource.MASTER) > 0 && mc.options.getSoundSourceVolume(SoundSource.MUSIC) > 0) {
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
                                        MusicTriggers.logger.debug("Random was between 1 and "+(total+1)+" "+r+" was chosen");
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
                                    if(j>=1000) MusicTriggers.logger.warn("Attempt to get non duplicate song passed 1000 tries! Forcing current song " + configToml.songholder.get(curTrack) + " to play.");
                                }
                                else {
                                    curTrack = curTrackList.get(i);
                                }
                                MusicTriggers.logger.debug(curTrack+" was chosen");
                                if (curTrack != null) {
                                    finish = Boolean.parseBoolean(configToml.otherinfo.get(curTrack)[2]);
                                    curTrackHolder = configToml.songholder.get(curTrack);
                                    MusicTriggers.logger.info("Attempting to play track: " + curTrackHolder);
                                    if (configToml.triggerlinking.get(curTrack) != null) {
                                        triggerLinker.put("song-" + 0, configToml.triggerlinking.get(curTrack).get(curTrack));
                                        musicLinker.put("song-" + 0, new setVolumeSound(new ResourceLocation(MusicTriggers.MODID, "music." + curTrackHolder), SoundSource.MUSIC, Float.parseFloat(configToml.otherinfo.get(curTrack)[4]), Float.parseFloat(configToml.otherinfo.get(curTrack)[0]), false, 1, SoundInstance.Attenuation.NONE, 0F, 0F, 0F));
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
                                                musicLinker.put("song-" + linkcounter, new setVolumeSound(new ResourceLocation(MusicTriggers.MODID, "music." + song), SoundSource.MUSIC, Float.parseFloat(configToml.otherlinkinginfo.get(curTrack).get(song)[1]),
                                                        Float.parseFloat(configToml.otherlinkinginfo.get(curTrack).get(song)[0]), false, 1, SoundInstance.Attenuation.NONE, 0F, 0F, 0F));
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
                                        musicLinker.put("song-" + 0, new setVolumeSound(new ResourceLocation(MusicTriggers.MODID, "music." + curTrackHolder), SoundSource.MUSIC, Float.parseFloat(configToml.otherinfo.get(curTrack)[4]), Float.parseFloat(configToml.otherinfo.get(curTrack)[0]), false, 1, SoundInstance.Attenuation.NONE, 0F, 0F, 0F));
                                        for(int l : configToml.loopPoints.get(curTrack).keySet()) {
                                            loopLinker.putIfAbsent("song-" + 0, new HashMap<>());
                                            loopLinker.get("song-" + 0).put(l, configToml.loopPoints.get(curTrack).get(l));
                                            loopLinkerCounter.putIfAbsent("song-" + 0, new HashMap<>());
                                            loopLinkerCounter.get("song-" + 0).put(l, 0);
                                        }
                                    }
                                    if (MusicPicker.player != null) {
                                        if (!configRegistry.clientSideOnly) PacketHandler.sendToServer(new CurSong(curTrackHolder, MusicPicker.player.getUUID()));
                                        else curSong.put(MusicPicker.player.getUUID(), curTrackHolder);
                                    }
                                    sh.stop();
                                    if (cards) renderCards();
                                    for (Map.Entry<String, setVolumeSound> stringListEntry : musicLinker.entrySet()) {
                                        String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                                        if (!checkThis.matches("song-0")) musicLinker.get(checkThis).setVolume(Float.MIN_VALUE);
                                        else curMusic = musicLinker.get(checkThis);
                                        sh.play(musicLinker.get(checkThis));
                                    }
                                    curMusicSource = sh.soundEngine.instanceToChannel.get(curMusic).channel;
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
                            mc.getSoundManager().stop(musicLinker.get(is));
                        }
                        curMusic = null;
                    }
                }
            }
        }
        tickCounter++;
    }

    public static void renderCards() {
        MusicTriggers.logger.info("Finding cards to render");
        int markForDeletion = -1;
        for (int i : configTitleCards.titlecards.keySet()) {
            boolean pass = false;
            if(MusicPicker.titleCardEvents.containsAll(configTitleCards.titlecards.get(i).getTriggers()) && configTitleCards.titlecards.get(i).getTriggers().containsAll(MusicPicker.titleCardEvents)) pass=true;
            else if(configTitleCards.titlecards.get(i).getVague() && MusicPicker.playableList.containsAll(configTitleCards.titlecards.get(i).getTriggers()) && canPlayTitle.get(i)) {
                pass=true;
                canPlayTitle.put(i, false);
            }
            if (pass && mc.player != null) {
                MusicTriggers.logger.info("displaying title card "+i);
                if(!configTitleCards.titlecards.get(i).getTitles().isEmpty()) mc.gui.setTitle(Component.nullToEmpty(Objects.requireNonNull(ChatFormatting.getByName(configTitleCards.titlecards.get(i).getTitlecolor()))+configTitleCards.titlecards.get(i).getTitles().get(ThreadLocalRandom.current().nextInt(0, configTitleCards.titlecards.get(i).getTitles().size()))));
                if(!configTitleCards.titlecards.get(i).getSubTitles().isEmpty()) mc.gui.setSubtitle(Component.nullToEmpty(Objects.requireNonNull(ChatFormatting.getByName(configTitleCards.titlecards.get(i).getSubtitlecolor()))+configTitleCards.titlecards.get(i).getSubTitles().get(ThreadLocalRandom.current().nextInt(0, configTitleCards.titlecards.get(i).getSubTitles().size()))));
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
                    MusicTriggers.logger.info("displaying image card " + configTitleCards.imagecards.get(i).getName());
                    if (!configTitleCards.ismoving.get(i)) {
                        eventsClient.IMAGE_CARD = new ResourceLocation(MusicTriggers.MODID, "textures/" + configTitleCards.imagecards.get(i).getName() + ".png");
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
                            eventsClient.pngs.add(index, new ResourceLocation(MusicTriggers.MODID, "textures/" + configTitleCards.imagecards.get(i).getName() + "/" + temp.get(index) + ".png"));
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
        if(curMusic!=null && sh.soundEngine.instanceToChannel.get(curMusic)!=null) {
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
                saveVol = mc.options.getSoundSourceVolume(SoundSource.MASTER);
            }
        } else {
            curTrackList = null;
            eventsClient.IMAGE_CARD = null;
            eventsClient.fadeCount = 1000;
            eventsClient.timer = 0;
            eventsClient.activated = false;
            eventsClient.ismoving = false;
            cards = true;
            Map<SoundInstance, ChannelAccess.ChannelHandle> curplaying = ObfuscationReflectionHelper.getPrivateValue(SoundEngine.class, ObfuscationReflectionHelper.getPrivateValue(net.minecraft.client.sounds.SoundManager.class, mc.getSoundManager(), "f_120349_"), "f_120226_");
            for (Map.Entry<String, setVolumeSound> stringListEntry : musicLinker.entrySet()) {
                String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                if (checkThis.matches(songNum)) {
                    musicLinker.get(checkThis).setVolume(volumeLinker.get(songNum));
                    assert curplaying != null;
                    if (curplaying.get(musicLinker.get(checkThis)) != null) {
                        String finalSongNum = songNum;
                        curplaying.get(musicLinker.get(checkThis)).execute(sound -> sound.setVolume(volumeLinker.get(finalSongNum)));
                    }
                    curMusic = musicLinker.get(checkThis);
                    curTrackHolder = musicLinker.get(checkThis).getLocation().toString().replaceAll("music.", "").replaceAll("riggers:", "");
                    if (MusicPicker.player != null) {
                        if (!configRegistry.clientSideOnly) {
                            PacketHandler.sendToServer(new CurSong(curTrackHolder, MusicPicker.player.getUUID()));
                        } else {
                            curSong.put(MusicPicker.player.getUUID(), curTrackHolder);
                        }
                    }
                } else {
                    musicLinker.get(checkThis).setVolume(Float.MIN_VALUE);
                    assert curplaying != null;
                    if (curplaying.get(musicLinker.get(checkThis)) != null) {
                        curplaying.get(musicLinker.get(checkThis)).execute(sound -> sound.setVolume(Float.MIN_VALUE));
                    }
                }
            }
        }
        MusicPicker.shouldChange = false;
    }
}
