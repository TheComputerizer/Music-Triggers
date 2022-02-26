package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.config.configRegistry;
import mods.thecomputerizer.musictriggers.config.configTitleCards;
import mods.thecomputerizer.musictriggers.config.configToml;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.musictriggers.util.audio.setVolumeSound;
import mods.thecomputerizer.musictriggers.util.packets.packetCurSong;
import net.minecraft.block.BlockJukebox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import paulscode.sound.SoundSystem;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = MusicTriggers.MODID, value = Side.CLIENT)
public class MusicPlayer {

    public static final KeyBinding RELOAD = new KeyBinding("key.reload_musictriggers", Keyboard.KEY_R, "key.categories.musictriggers");

    public static String[] curTrackList;
    public static String[] holder;
    public static String curTrack;
    public static String curTrackHolder;
    public static ISound curMusic;
    public static Random rand = new Random();
    public static Minecraft mc = Minecraft.getMinecraft();
    public static int tickCounter = 0;
    public static boolean fading = false;
    public static boolean reverseFade = false;
    private static int tempFade = 0;
    private static float saveVol = 1;
    public static boolean delay = false;
    public static int delayTime = 0;
    public static boolean playing = false;
    public static SoundEvent fromRecord = null;
    public static boolean reloading = false;
    public static boolean cards = true;
    public static boolean finish = false;
    public static HashMap<String, setVolumeSound> musicLinker = new HashMap<>();
    public static HashMap<String, String[]> triggerLinker = new HashMap<>();

    @SuppressWarnings("rawtypes")
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onTick(TickEvent.ClientTickEvent event) {
        if(!reloading) {
            if(MusicPicker.fishBool) {
                MusicPicker.fishingStart++;
            }
            for (Map.Entry<String, Integer> stringListEntry : MusicPicker.triggerPersistence.entrySet()) {
                String eventID = ((Map.Entry) stringListEntry).getKey().toString();
                MusicPicker.triggerPersistence.putIfAbsent(eventID, 0);
                if (MusicPicker.triggerPersistence.get(eventID) > 0) {
                    MusicPicker.triggerPersistence.put(eventID, MusicPicker.triggerPersistence.get(eventID) - 1);
                }
            }
            if (fading && !reverseFade) {
                if (tempFade == 0) {
                    fading = false;
                    curTrackList = null;
                    mc.getSoundHandler().stopSound(curMusic);
                    mc.getSoundHandler().setSoundLevel(SoundCategory.MASTER, saveVol);
                    eventsClient.IMAGE_CARD = null;
                    eventsClient.fadeCount = 1000;
                    eventsClient.timer = 0;
                    eventsClient.activated = false;
                    eventsClient.ismoving = false;
                    cards = true;
                } //else if((MusicPicker.curFade-tempFade)%10==0) {
                //if(Arrays.equals(curTrackList,holder)) {
                //MusicTriggers.logger.info("beginning reverse fading");
                //reverseFade = true;
                //}
                //}
                else {
                    mc.getSoundHandler().setSoundLevel(SoundCategory.MASTER, saveVol * (float) (((double) tempFade) / ((double) MusicPicker.curFade)));
                    tempFade -= 1;
                }
            } else if (reverseFade) {
                if (tempFade >= MusicPicker.curFade) {
                    fading = false;
                    reverseFade = false;
                    mc.getSoundHandler().setSoundLevel(SoundCategory.MASTER, saveVol);
                } else {
                    mc.getSoundHandler().setSoundLevel(SoundCategory.MASTER, saveVol * (float) (((double) tempFade) / ((double) MusicPicker.curFade)));
                    tempFade += 1;
                }
            }
            if (delay) {
                delayTime -= 1;
                if (delayTime <= 0) {
                    delay = false;
                }
            }
            if (tickCounter % 10 == 0 && !fading && !delay) {
                if (MusicPicker.player != null && (MusicPicker.player.getHeldItemMainhand().getItem() instanceof MusicTriggersRecord)) {
                    fromRecord = ((MusicTriggersRecord) MusicPicker.player.getHeldItemMainhand().getItem()).getSound();
                } else {
                    fromRecord = null;
                }
                playing = false;
                if (MusicPicker.player != null) {
                    for (int x = MusicPicker.player.chunkCoordX - 3; x <= MusicPicker.player.chunkCoordX + 3; x++) {
                        for (int z = MusicPicker.player.chunkCoordZ - 3; z <= MusicPicker.player.chunkCoordZ + 3; z++) {
                            Map<BlockPos, TileEntity> currentChunkTE = MusicPicker.world.getChunk(x, z).getTileEntityMap();
                            for (TileEntity te : currentChunkTE.values()) {
                                if (te != null) {
                                    if (te instanceof BlockJukebox.TileEntityJukebox) {
                                        if (te.getBlockMetadata() != 0) {
                                            playing = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                holder = MusicPicker.playThese();
                if (holder != null && !Arrays.asList(holder).isEmpty() && !playing) {
                    if (curTrackList == null && !finish) {
                        curTrackList = holder;
                    }
                    if (curMusic != null) {
                        if (!mc.getSoundHandler().isSoundPlaying(curMusic) || mc.gameSettings.getSoundLevel(SoundCategory.MUSIC) == 0 || mc.gameSettings.getSoundLevel(SoundCategory.MASTER) == 0) {
                            finish = false;
                            mc.getSoundHandler().stopSounds();
                            curMusic = null;
                            delay = true;
                            delayTime = MusicPicker.curDelay;
                        }
                    }
                    if(!finish) {
                        if (MusicPicker.shouldChange || !Arrays.equals(curTrackList, holder)) {
                            eventsClient.GuiCounter = 1;
                            String songNum = null;
                            for (Map.Entry<String, setVolumeSound> stringListEntry : musicLinker.entrySet()) {
                                String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                                if (triggerLinker.get(checkThis) != null) {
                                    if (theDecidingFactor(MusicPicker.playableList, MusicPicker.titleCardEvents, triggerLinker.get(checkThis)) && mc.player != null) {
                                        songNum = checkThis;
                                        break;
                                    }
                                }
                            }
                            if (songNum == null) {
                                triggerLinker = new HashMap<>();
                                musicLinker = new HashMap<>();
                                if (MusicPicker.curFade == 0) {
                                    curTrackList = null;
                                    mc.getSoundHandler().stopSound(curMusic);
                                    eventsClient.IMAGE_CARD = null;
                                    eventsClient.fadeCount = 1000;
                                    eventsClient.timer = 0;
                                    eventsClient.activated = false;
                                    eventsClient.ismoving = false;
                                    cards = true;
                                } else {
                                    fading = true;
                                    tempFade = MusicPicker.curFade;
                                    saveVol = mc.gameSettings.getSoundLevel(SoundCategory.MASTER);
                                }
                            } else {
                                curTrackList = null;
                                eventsClient.IMAGE_CARD = null;
                                eventsClient.fadeCount = 1000;
                                eventsClient.timer = 0;
                                eventsClient.activated = false;
                                eventsClient.ismoving = false;
                                cards = true;
                                Map<String, ISound> curplaying = ObfuscationReflectionHelper.getPrivateValue(SoundManager.class, ObfuscationReflectionHelper.getPrivateValue(net.minecraft.client.audio.SoundHandler.class, mc.getSoundHandler(), "field_147694_f"), "field_148629_h");
                                SoundSystem sndSys = ObfuscationReflectionHelper.getPrivateValue(SoundManager.class, ObfuscationReflectionHelper.getPrivateValue(net.minecraft.client.audio.SoundHandler.class, Minecraft.getMinecraft().getSoundHandler(), "field_147694_f"), "field_148620_e");
                                for (Map.Entry<String, setVolumeSound> stringListEntry : musicLinker.entrySet()) {
                                    String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                                    String temp = curplaying.entrySet().stream().filter(entry -> entry.getValue() == musicLinker.get(checkThis)).map(Map.Entry::getKey).findFirst().orElse(null);
                                    if (checkThis.matches(songNum)) {
                                        musicLinker.get(checkThis).setVolume(1F);
                                        sndSys.setVolume(temp, 1F);
                                        curMusic = musicLinker.get(checkThis);
                                        curTrackHolder = musicLinker.get(checkThis).getSoundLocation().toString().replaceAll("music.", "").replaceAll("riggers:", "");
                                        if (configRegistry.registry.registerDiscs && MusicPicker.player != null) {
                                            RegistryHandler.network.sendToServer(new packetCurSong.packetCurSongMessage(curTrack, MusicPicker.player.getUniqueID()));
                                        }
                                    } else {
                                        musicLinker.get(checkThis).setVolume(0.01F);
                                        sndSys.setVolume(temp, 0.01F);
                                    }
                                }
                            }
                            MusicPicker.shouldChange = false;
                        } else if (curMusic == null && mc.gameSettings.getSoundLevel(SoundCategory.MUSIC) > 0 && mc.gameSettings.getSoundLevel(SoundCategory.MASTER) > 0) {
                            triggerLinker = new HashMap<>();
                            musicLinker = new HashMap<>();
                            eventsClient.GuiCounter = 0;
                            if (curTrackList.length >= 1) {
                                int i = rand.nextInt(curTrackList.length);
                                if (curTrackList.length > 1 && curTrack != null) {
                                    while (curTrack.equals(curTrackList[i])) {
                                        i = rand.nextInt(curTrackList.length);
                                    }
                                }
                                curTrack = curTrackList[i];
                                if (curTrack != null) {
                                    finish = Boolean.parseBoolean(configToml.otherinfo.get(curTrack)[2]);
                                    curTrackHolder = configToml.songholder.get(curTrack);
                                    MusicTriggers.logger.info("Attempting to play track: " + curTrackHolder);
                                    if (configToml.triggerlinking.get(curTrack) != null) {
                                        triggerLinker.put("song-" + 0, configToml.triggerlinking.get(curTrack).get(curTrack));
                                        musicLinker.put("song-" + 0, new setVolumeSound(new ResourceLocation(MusicTriggers.MODID, "music." + curTrackHolder), SoundCategory.MUSIC, 1F, Float.parseFloat(configToml.otherinfo.get(curTrack)[0]), false, 1, ISound.AttenuationType.NONE, 0F, 0F, 0F));
                                        int linkcounter = 0;
                                        for (String song : configToml.triggerlinking.get(curTrack).keySet()) {
                                            if (!song.matches(curTrack)) {
                                                triggerLinker.put("song-" + linkcounter, configToml.triggerlinking.get(curTrack).get(song));
                                                musicLinker.put("song-" + linkcounter, new setVolumeSound(new ResourceLocation(MusicTriggers.MODID, "music." + song), SoundCategory.MUSIC, 1F,
                                                        Float.parseFloat(configToml.otherlinkinginfo.get(curTrack).get(song)[0]), false, 1, ISound.AttenuationType.NONE, 0F, 0F, 0F));
                                            }
                                            linkcounter++;
                                        }
                                    } else {
                                        musicLinker.put("song-" + 0, new setVolumeSound(new ResourceLocation(MusicTriggers.MODID, "music." + curTrackHolder), SoundCategory.MUSIC, 1F, Float.parseFloat(configToml.otherinfo.get(curTrack)[0]), false, 1, ISound.AttenuationType.NONE, 0F, 0F, 0F));
                                    }
                                    if (configRegistry.registry.registerDiscs && MusicPicker.player != null) {
                                        RegistryHandler.network.sendToServer(new packetCurSong.packetCurSongMessage(curTrackHolder, MusicPicker.player.getUniqueID()));
                                    }
                                    mc.getSoundHandler().stopSounds();
                                    if (cards) {
                                        renderCards();
                                    }
                                    for (Map.Entry<String, setVolumeSound> stringListEntry : musicLinker.entrySet()) {
                                        String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                                        if (!checkThis.matches("song-0")) {
                                            musicLinker.get(checkThis).setVolume(0.01F);
                                        } else {
                                            curMusic = musicLinker.get(checkThis);
                                        }
                                        mc.getSoundHandler().playSound(musicLinker.get(checkThis));
                                    }
                                    if (Boolean.parseBoolean(configToml.otherinfo.get(curTrack)[1])) {
                                        configToml.songholder.remove(curTrack);
                                        configToml.triggerlinking.remove(curTrack);
                                        configToml.triggerholder.remove(curTrack);
                                        configToml.otherinfo.remove(curTrack);
                                        configToml.otherlinkinginfo.remove(curTrack);
                                        curTrackList = ArrayUtils.remove(curTrackList,i);
                                        for (String ev : MusicPicker.titleCardEvents) {
                                            SoundHandler.TriggerSongMap.get(StringUtils.substringBefore(ev, "-")).remove(curTrack);
                                        }
                                    }
                                } else {
                                    curTrackList = null;
                                }
                            }
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
                            mc.getSoundHandler().stopSound(musicLinker.get(is));
                        }
                        curMusic = null;
                    }
                }
            }
            tickCounter++;
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void renderCards() {
        MusicTriggers.logger.info("Finding cards to render");
        int markForDeletion = -1;
        for (int i : configTitleCards.titlecards.keySet()) {
            if (MusicPicker.titleCardEvents.containsAll(configTitleCards.titlecards.get(i).getTriggers()) && configTitleCards.titlecards.get(i).getTriggers().containsAll(MusicPicker.titleCardEvents) && mc.player != null) {
                MusicTriggers.logger.info("displaying title card "+i);
                mc.ingameGUI.displayTitle("\u00A74" + configTitleCards.titlecards.get(i).getTitle(), configTitleCards.titlecards.get(i).getSubTitle(), 5, 20, 20);
                mc.ingameGUI.displayTitle(null, configTitleCards.titlecards.get(i).getSubTitle(), 5, 20, 20);
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
                        eventsClient.timer = Minecraft.getSystemTime();
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