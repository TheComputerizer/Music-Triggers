package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.config.configRegistry;
import mods.thecomputerizer.musictriggers.config.configTitleCards;
import mods.thecomputerizer.musictriggers.config.configToml;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.audio.setVolumeSound;
import mods.thecomputerizer.musictriggers.util.packets.CurSong;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundInstance;
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

    public static Map<SoundInstance, Channel.SourceManager> sources;

    public static void onTick() {
        if(!reloading) {
            if(MusicPicker.fishBool) {
                MusicPicker.fishingStart++;
            }
            if(MusicPicker.elytraBool) {
                MusicPicker.elytraStart++;
            }
            for (Map.Entry<String, Integer> stringListEntry : MusicPicker.triggerPersistence.entrySet()) {
                String eventID = ((Map.Entry) stringListEntry).getKey().toString();
                MusicPicker.triggerPersistence.putIfAbsent(eventID, 0);
                if (MusicPicker.triggerPersistence.get(eventID) > 0) {
                    MusicPicker.triggerPersistence.put(eventID, MusicPicker.triggerPersistence.get(eventID) - 1);
                }
            }
            if (fading) {
                if (tempFade == 0) {
                    fading = false;
                    mc.getSoundManager().stop(curMusic);
                    mc.getSoundManager().updateSoundVolume(SoundCategory.MASTER, saveVol);
                    eventsClient.IMAGE_CARD = null;
                    eventsClient.fadeCount = 1000;
                    eventsClient.timer = 0;
                    eventsClient.activated = false;
                    eventsClient.ismoving = false;
                    cards = true;
                } else {
                    mc.getSoundManager().updateSoundVolume(SoundCategory.MASTER, saveVol * (float) (((double) tempFade) / ((double) MusicPicker.curFade)));
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
                if (MusicPicker.player != null && (MusicPicker.player.getActiveItem().getItem() instanceof MusicTriggersRecord)) {
                    fromRecord = ((MusicTriggersRecord) MusicPicker.player.getActiveItem().getItem()).getSound();
                } else {
                    fromRecord = new SoundEvent(new Identifier("nonsensicalresourcelocation"));
                }
                playing = false;
                if (MusicPicker.player != null) {
                    for (int x = MusicPicker.player.getChunkPos().x - 3; x <= MusicPicker.player.getChunkPos().x  + 3; x++) {
                        for (int z = MusicPicker.player.getChunkPos().z - 3; z <= MusicPicker.player.getChunkPos().z + 3; z++) {
                            Set<BlockPos> currentChunkTEPos = MusicPicker.world.getChunk(x, z).getBlockEntityPositions();
                            for (BlockPos b : currentChunkTEPos) {
                                if (MusicPicker.world.getChunk(x, z).getBlockEntity(b) instanceof JukeboxBlockEntity te) {
                                    if (te.getCachedState().get(JukeboxBlock.HAS_RECORD)) {
                                        playing = true;
                                    }
                                }
                            }
                        }
                    }
                }
                holder = MusicPicker.playThese();
                if (holder != null && !holder.isEmpty() && !playing) {
                    if (curTrackList == null && !finish) {
                        curTrackList = holder;
                    }
                    if (curMusic != null) {
                        if (!mc.getSoundManager().isPlaying(curMusic) || mc.options.getSoundVolume(SoundCategory.MUSIC) == 0 || mc.options.getSoundVolume(SoundCategory.MASTER) == 0) {
                            finish = false;
                            mc.getSoundManager().stopAll();
                            curMusic = null;
                            delay = true;
                            delayTime = MusicPicker.curDelay;
                        }
                    }
                    if(!finish) {
                        if (MusicPicker.shouldChange || !Arrays.equals(curTrackList.toArray(new String[0]), holder.toArray(new String[0]))) {
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
                        } else if (curMusic == null && mc.options.getSoundVolume(SoundCategory.MASTER) > 0 && mc.options.getSoundVolume(SoundCategory.MUSIC) > 0) {
                            triggerLinker = new HashMap<>();
                            musicLinker = new HashMap<>();
                            eventsClient.GuiCounter = 0;
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
                                else {
                                    curTrack = curTrackList.get(i);
                                }
                                MusicTriggersCommon.logger.debug(curTrack+" was chosen");
                                if (curTrack != null) {
                                    finish = Boolean.parseBoolean(configToml.otherinfo.get(curTrack)[2]);
                                    curTrackHolder = configToml.songholder.get(curTrack);
                                    MusicTriggersCommon.logger.info("Attempting to play track: " + curTrackHolder);
                                    if (configToml.triggerlinking.get(curTrack) != null) {
                                        triggerLinker.put("song-" + 0, configToml.triggerlinking.get(curTrack).get(curTrack));
                                        musicLinker.put("song-" + 0, new setVolumeSound(new Identifier(MusicTriggersCommon.MODID, "music." + curTrackHolder), SoundCategory.MUSIC, Float.parseFloat(configToml.otherinfo.get(curTrack)[4]), Float.parseFloat(configToml.otherinfo.get(curTrack)[0]), false, 1, SoundInstance.AttenuationType.NONE, 0F, 0F, 0F));
                                        volumeLinker.put("song-" + 0, Float.parseFloat(configToml.otherinfo.get(curTrack)[4]));
                                        int linkcounter = 0;
                                        for (String song : configToml.triggerlinking.get(curTrack).keySet()) {
                                            if (!song.matches(curTrack)) {
                                                triggerLinker.put("song-" + linkcounter, configToml.triggerlinking.get(curTrack).get(song));
                                                musicLinker.put("song-" + linkcounter, new setVolumeSound(new Identifier(MusicTriggersCommon.MODID, "music." + song), SoundCategory.MUSIC, Float.parseFloat(configToml.otherinfo.get(curTrack)[4]),
                                                        Float.parseFloat(configToml.otherlinkinginfo.get(curTrack).get(song)[0]), false, 1, SoundInstance.AttenuationType.NONE, 0F, 0F, 0F));
                                                volumeLinker.put("song-" + linkcounter, Float.parseFloat(configToml.otherlinkinginfo.get(curTrack).get(song)[1]));

                                            }
                                            linkcounter++;
                                        }
                                    } else {
                                        musicLinker.put("song-" + 0, new setVolumeSound(new Identifier(MusicTriggersCommon.MODID, "music." + curTrackHolder), SoundCategory.MUSIC, Float.parseFloat(configToml.otherinfo.get(curTrack)[4]), Float.parseFloat(configToml.otherinfo.get(curTrack)[0]), false, 1, SoundInstance.AttenuationType.NONE, 0F, 0F, 0F));
                                    }
                                    if (MusicPicker.player != null) {
                                        if (!configRegistry.clientSideOnly) {
                                            PacketHandler.sendToServer(CurSong.id, CurSong.encode(curTrackHolder, MusicPicker.player.getUuid()));
                                        } else {
                                            CurSong.curSong.put(MusicPicker.player.getUuid(), curTrackHolder);
                                        }
                                    }
                                    mc.getSoundManager().stopAll();
                                    if (cards) {
                                        renderCards();
                                    }
                                    for (Map.Entry<String, setVolumeSound> stringListEntry : musicLinker.entrySet()) {
                                        String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                                        if (!checkThis.matches("song-0")) {
                                            musicLinker.get(checkThis).setVolume(Float.MIN_VALUE);
                                        } else {
                                            curMusic = musicLinker.get(checkThis);
                                        }
                                        mc.getSoundManager().play(musicLinker.get(checkThis));
                                    }
                                    if (Boolean.parseBoolean(configToml.otherinfo.get(curTrack)[1])) {
                                        configToml.songholder.remove(curTrack);
                                        configToml.triggerlinking.remove(curTrack);
                                        configToml.triggerholder.remove(curTrack);
                                        configToml.otherinfo.remove(curTrack);
                                        configToml.otherlinkinginfo.remove(curTrack);
                                        curTrackList.remove(curTrack);
                                        for (String ev : MusicPicker.titleCardEvents) {
                                            SoundHandler.TriggerSongMap.get(StringUtils.substringBefore(ev, "-")).remove(curTrack);
                                        }
                                    }
                                } else {
                                    curTrackList = null;
                                }
                            }
                            MusicTriggersCommon.logger.info("track list too smol");
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
            tickCounter++;
        }
    }

    public static void renderCards() {
        MusicTriggersCommon.logger.info("Finding cards to render");
        int markForDeletion = -1;
        for (int i : configTitleCards.titlecards.keySet()) {
            if (MusicPicker.titleCardEvents.containsAll(configTitleCards.titlecards.get(i).getTriggers()) && configTitleCards.titlecards.get(i).getTriggers().containsAll(MusicPicker.titleCardEvents) && mc.player != null) {
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
            if (MusicPicker.titleCardEvents.containsAll(configTitleCards.imagecards.get(i).getTriggers()) && configTitleCards.imagecards.get(i).getTriggers().containsAll(MusicPicker.titleCardEvents) && mc.player != null) {
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
}
