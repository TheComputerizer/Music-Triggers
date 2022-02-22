package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.config.configRegistry;
import mods.thecomputerizer.musictriggers.config.configTitleCards;
import mods.thecomputerizer.musictriggers.config.configToml;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.packets.CurSong;
import mods.thecomputerizer.musictriggers.util.audio.setVolumeSound;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ChannelManager;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundEngine;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static mods.thecomputerizer.musictriggers.util.packets.CurSong.curSong;

@SuppressWarnings("rawtypes")
@Mod.EventBusSubscriber(modid = MusicTriggers.MODID, value = Dist.CLIENT)
public class MusicPlayer {
    public static final KeyBinding RELOAD = new KeyBinding("key.reload_musictriggers", KeyConflictContext.UNIVERSAL, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.musictriggers");

    public static List<String> curTrackList;
    public static List<String> holder;
    public static String curTrack;
    public static String curTrackHolder;
    public static ISound curMusic;
    public static Random rand = new Random();
    public static Minecraft mc = Minecraft.getInstance();
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
    public static HashMap<String, setVolumeSound> musicLinker = new HashMap<>();
    public static HashMap<String, String[]> triggerLinker = new HashMap<>();

    @SubscribeEvent
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
            if (fading) {
                if (tempFade == 0) {
                    fading = false;
                    mc.getSoundManager().stop(curMusic);
                    mc.getSoundManager().updateSourceVolume(SoundCategory.MASTER, saveVol);
                    cards = true;
                } else {
                    mc.getSoundManager().updateSourceVolume(SoundCategory.MASTER, saveVol * (float) (((double) tempFade) / ((double) MusicPicker.curFade)));
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
                if (MusicPicker.player != null && (MusicPicker.player.getMainHandItem().getItem() instanceof MusicTriggersRecord)) {
                    fromRecord = ((MusicTriggersRecord) MusicPicker.player.getMainHandItem().getItem()).getSound();
                } else {
                    fromRecord = new SoundEvent(new ResourceLocation("nonsensicalresourcelocation"));
                }
                playing = false;
                if (MusicPicker.player != null) {
                    for (int x = MusicPicker.player.xChunk - 3; x <= MusicPicker.player.xChunk + 3; x++) {
                        for (int z = MusicPicker.player.zChunk - 3; z <= MusicPicker.player.zChunk + 3; z++) {
                            Set<BlockPos> currentChunkTEPos = MusicPicker.world.getChunk(x, z).getBlockEntitiesPos();
                            for (BlockPos b : currentChunkTEPos) {
                                if (MusicPicker.world.getChunk(x, z).getBlockEntity(b) instanceof JukeboxTileEntity) {
                                    JukeboxTileEntity te = (JukeboxTileEntity) MusicPicker.world.getChunk(x, z).getBlockEntity(b);
                                    assert te != null;
                                    if (te.getBlockState().getValue(JukeboxBlock.HAS_RECORD)) {
                                        playing = true;
                                    }
                                }
                            }
                        }
                    }
                }
                holder = MusicPicker.playThese();
                if (holder != null && !holder.isEmpty() && !playing) {
                    if (curTrackList == null) {
                        curTrackList = holder;
                    }
                    if (curMusic != null) {
                        if (!mc.getSoundManager().isActive(curMusic) || mc.options.getSoundSourceVolume(SoundCategory.MUSIC) == 0 || mc.options.getSoundSourceVolume(SoundCategory.MASTER) == 0) {
                            mc.getSoundManager().stop();
                            curMusic = null;
                            delay = true;
                            delayTime = 20;
                        }
                    }
                    if (MusicPicker.shouldChange || !Arrays.equals(curTrackList.toArray(new String[0]), holder.toArray(new String[0]))) {
                        eventsClient.IMAGE_CARD = null;
                        curTrackList = null;
                        tempTitleCards = MusicPicker.titleCardEvents;
                        String songNum = null;
                        for (Map.Entry<String, setVolumeSound> stringListEntry : musicLinker.entrySet()) {
                            String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                            if(triggerLinker.get(checkThis)!=null) {
                                if (theDecidingFactor(MusicPicker.playableList,tempTitleCards,triggerLinker.get(checkThis)) && mc.player != null) {
                                    songNum = checkThis;
                                    break;
                                }
                            }
                        }
                        if(songNum==null) {
                            triggerLinker = new HashMap<>();
                            musicLinker = new HashMap<>();
                            if (MusicPicker.curFade == 0) {
                                mc.getSoundManager().stop(curMusic);
                                cards = true;
                            } else {
                                fading = true;
                                tempFade = MusicPicker.curFade;
                                saveVol = mc.options.getSoundSourceVolume(SoundCategory.MASTER);
                            }
                        }
                        else {
                            curTrackList = null;
                            cards = true;
                            Map<ISound, ChannelManager.Entry>  curplaying = ObfuscationReflectionHelper.getPrivateValue(SoundEngine.class,ObfuscationReflectionHelper.getPrivateValue(net.minecraft.client.audio.SoundHandler.class,mc.getSoundManager(),"field_147694_f"),"field_217942_m");
                            for (Map.Entry<String, setVolumeSound> stringListEntry : musicLinker.entrySet()) {
                                String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                                if(checkThis.matches(songNum)) {
                                    musicLinker.get(checkThis).setVolume(1F);
                                    assert curplaying != null;
                                    if(curplaying.get(musicLinker.get(checkThis))!=null) {
                                        curplaying.get(musicLinker.get(checkThis)).execute(sound -> sound.setVolume(1F));
                                    }
                                    curMusic = musicLinker.get(checkThis);
                                    curTrackHolder = musicLinker.get(checkThis).getLocation().toString().replaceAll("music.","").replaceAll("riggers:","");
                                    if (MusicPicker.player != null) {
                                        if (!configRegistry.clientSideOnly) {
                                            PacketHandler.sendToServer(new CurSong(curTrackHolder, MusicPicker.player.getUUID()));
                                        } else {
                                            curSong.put(MusicPicker.player.getUUID(), curTrackHolder);
                                        }
                                    }
                                }
                                else {
                                    musicLinker.get(checkThis).setVolume(0.01F);
                                    assert curplaying != null;
                                    if(curplaying.get(musicLinker.get(checkThis))!=null) {
                                        curplaying.get(musicLinker.get(checkThis)).execute(sound -> sound.setVolume(0.01F));
                                    }
                                }
                            }
                        }
                        MusicPicker.shouldChange = false;
                    } else if (curMusic == null && mc.options.getSoundSourceVolume(SoundCategory.MASTER) > 0 && mc.options.getSoundSourceVolume(SoundCategory.MUSIC) > 0) {
                        triggerLinker = new HashMap<>();
                        musicLinker = new HashMap<>();
                        eventsClient.GuiCounter = 0;
                        if (curTrackList.size() >= 1) {
                            int i = rand.nextInt(curTrackList.size());
                            if (curTrackList.size() > 1 && curTrack != null) {
                                while (curTrack.equals(curTrackList.get(i))) {
                                    i = rand.nextInt(curTrackList.size());
                                }
                            }
                            curTrack = curTrackList.get(i);
                            if(curTrack!=null) {
                                curTrackHolder = configToml.songholder.get(curTrack);
                                MusicTriggers.logger.info("Attempting to play track: " + curTrackHolder);
                                if (configToml.triggerlinking.get(curTrack) != null) {
                                    triggerLinker.put("song-" + 0, configToml.triggerlinking.get(curTrack).get(curTrack));
                                    musicLinker.put("song-" + 0, new setVolumeSound(new ResourceLocation(MusicTriggers.MODID, "music." + curTrackHolder), SoundCategory.MUSIC, 1F, Float.parseFloat(configToml.otherinfo.get(curTrack)[0]), false, 1, ISound.AttenuationType.NONE, 0F, 0F, 0F));
                                    int linkcounter = 0;
                                    for (String song : configToml.triggerlinking.get(curTrack).keySet()) {
                                        if(!song.matches(curTrack)) {
                                            triggerLinker.put("song-" + linkcounter, configToml.triggerlinking.get(curTrack).get(song));
                                            musicLinker.put("song-" + linkcounter, new setVolumeSound(new ResourceLocation(MusicTriggers.MODID, "music." + song), SoundCategory.MUSIC, 1F,
                                                    Float.parseFloat(configToml.otherlinkinginfo.get(curTrack).get(song)[0]), false, 1, ISound.AttenuationType.NONE, 0F, 0F, 0F));
                                        }
                                        linkcounter++;
                                    }
                                } else {
                                    musicLinker.put("song-" + 0, new setVolumeSound(new ResourceLocation(MusicTriggers.MODID, "music." + curTrackHolder), SoundCategory.MUSIC, 1F, Float.parseFloat(configToml.otherinfo.get(curTrack)[0]), false, 1, ISound.AttenuationType.NONE, 0F, 0F, 0F));
                                }
                                if (MusicPicker.player != null) {
                                    if (!configRegistry.clientSideOnly) {
                                        PacketHandler.sendToServer(new CurSong(curTrackHolder, MusicPicker.player.getUUID()));
                                    } else {
                                        curSong.put(MusicPicker.player.getUUID(), curTrackHolder);
                                    }
                                }
                                mc.getSoundManager().stop();
                                if(cards) {
                                    renderCards();
                                }
                                for (Map.Entry<String, setVolumeSound> stringListEntry : musicLinker.entrySet()) {
                                    String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                                    if (!checkThis.matches("song-0")) {
                                        musicLinker.get(checkThis).setVolume(0.01F);
                                    } else {
                                        curMusic = musicLinker.get(checkThis);
                                    }
                                    mc.getSoundManager().play(musicLinker.get(checkThis));
                                }
                            }
                            else {
                                curTrackList = null;
                            }
                        }
                    }
                } else {
                    curTrack = null;
                    curTrackHolder = null;
                    if (curMusic != null) {
                        mc.getSoundManager().stop(curMusic);
                        curMusic = null;
                    }
                }
            }
            tickCounter++;
        }
    }

    public static void renderCards() {
        MusicTriggers.logger.info("Finding cards to render");
        for (int i : configTitleCards.titlecards.keySet()) {
            if (MusicPicker.titleCardEvents.containsAll(configTitleCards.titlecards.get(i).getTriggers()) && mc.player != null) {
                MusicTriggers.logger.info("displaying title card "+i);
                mc.gui.setTitles(new TranslationTextComponent(configTitleCards.titlecards.get(i).getTitle()).withStyle(TextFormatting.DARK_RED), new TranslationTextComponent(configTitleCards.titlecards.get(i).getSubTitle()), 5, 20, 20);
                mc.gui.setTitles(null, new TranslationTextComponent(configTitleCards.titlecards.get(i).getSubTitle()), 5, 20, 20);
            }
        }
        for (int i : configTitleCards.imagecards.keySet()) {
            if (MusicPicker.titleCardEvents.containsAll(configTitleCards.imagecards.get(i).getTriggers()) && mc.player != null) {
                MusicTriggers.logger.info("displaying image card "+configTitleCards.imagecards.get(i).getName());
                if(!configTitleCards.ismoving.get(i)) {
                    eventsClient.IMAGE_CARD = new ResourceLocation(MusicTriggers.MODID, "textures/" + configTitleCards.imagecards.get(i).getName() + ".png");
                }
                else {
                    if(configTitleCards.imagecards.get(i).getName()!=null) {
                        eventsClient.pngs = new ArrayList<>();
                        eventsClient.ismoving = true;
                        eventsClient.movingcounter = 0;
                        File folder = new File("." + "/config/MusicTriggers/songs/assets/musictriggers/textures/" + configTitleCards.imagecards.get(i).getName());
                        MusicTriggers.logger.info(folder.getName() + " with path " + folder.getPath());
                        File[] listOfPNG = folder.listFiles();
                        for (File f : listOfPNG) {
                            eventsClient.pngs.add(new ResourceLocation(MusicTriggers.MODID, "textures/" + configTitleCards.imagecards.get(i).getName() + "/" + f.getName()));
                        }
                    }
                }
                eventsClient.curImageIndex = i;
                eventsClient.activated = true;
            }
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
