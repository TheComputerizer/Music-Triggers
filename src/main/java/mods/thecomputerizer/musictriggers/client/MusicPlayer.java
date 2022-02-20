package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.config.configRegistry;
import mods.thecomputerizer.musictriggers.config.configTitleCards;
import mods.thecomputerizer.musictriggers.config.configToml;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.musictriggers.util.audio.setVolumeSound;
import mods.thecomputerizer.musictriggers.util.packetCurSong;
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import paulscode.sound.SoundSystem;

import java.util.*;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = MusicTriggers.MODID, value = Side.CLIENT)
public class MusicPlayer {

    public static final KeyBinding RELOAD = new KeyBinding("key.reload_musictriggers", Keyboard.KEY_R, "key.categories.musictriggers");

    public static String[] curTrackList;
    public static String[] holder;
    public static String curTrack;
    public static ISound curMusic;
    public static Random rand = new Random();
    public static Minecraft mc = Minecraft.getMinecraft();
    public static int tickCounter = 0;
    public static boolean fading = false;
    public static boolean reverseFade = false;
    private static int tempFade = 0;
    private static float saveVol = 1;
    public static List<String> tempTitleCards = new ArrayList<>();
    public static boolean delay = false;
    public static int delayTime = 0;
    public static boolean playing = false;
    public static SoundEvent fromRecord = null;
    public static boolean reloading = false;
    public static HashMap<String, setVolumeSound> musicLinker = new HashMap<>();
    public static HashMap<String, String[]> triggerLinker = new HashMap<>();

    @SuppressWarnings("rawtypes")
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
            if (fading && !reverseFade) {
                if (tempFade == 0) {
                    fading = false;
                    curTrackList = null;
                    mc.getSoundHandler().stopSound(curMusic);
                    mc.getSoundHandler().setSoundLevel(SoundCategory.MASTER, saveVol);
                    renderCards();
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
                    if (curTrackList == null) {
                        curTrackList = holder;
                    }
                    if (curMusic != null) {
                        if (!mc.getSoundHandler().isSoundPlaying(curMusic) || mc.gameSettings.getSoundLevel(SoundCategory.MUSIC) == 0 || mc.gameSettings.getSoundLevel(SoundCategory.MASTER) == 0) {
                            mc.getSoundHandler().stopSounds();
                            curMusic = null;
                            delay = true;
                            delayTime = 20;
                        }
                    }
                    if (MusicPicker.shouldChange || !Arrays.equals(curTrackList, holder)) {
                        eventsClient.GuiCounter = 1;
                        eventsClient.IMAGE_CARD = null;
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
                                curTrackList = null;
                                mc.getSoundHandler().stopSound(curMusic);
                                renderCards();
                            } else {
                                fading = true;
                                tempFade = MusicPicker.curFade;
                                saveVol = mc.gameSettings.getSoundLevel(SoundCategory.MASTER);
                            }
                        }
                        else {
                            curTrackList = null;
                            renderCards();
                            Map<String, ISound> curplaying = ObfuscationReflectionHelper.getPrivateValue(SoundManager.class,ObfuscationReflectionHelper.getPrivateValue(net.minecraft.client.audio.SoundHandler.class,mc.getSoundHandler(),"field_147694_f"),"field_148629_h");
                            SoundSystem sndSys = ObfuscationReflectionHelper.getPrivateValue(SoundManager.class,ObfuscationReflectionHelper.getPrivateValue(net.minecraft.client.audio.SoundHandler.class,Minecraft.getMinecraft().getSoundHandler(),"field_147694_f"),"field_148620_e");
                            for (Map.Entry<String, setVolumeSound> stringListEntry : musicLinker.entrySet()) {
                                String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                                String temp = curplaying.entrySet().stream().filter(entry -> entry.getValue()==musicLinker.get(checkThis)).map(Map.Entry::getKey).findFirst().orElse(null);
                                if(checkThis.matches(songNum)) {
                                    musicLinker.get(checkThis).setVolume(1F);
                                    sndSys.setVolume(temp,1F);
                                    curMusic = musicLinker.get(checkThis);
                                    curTrack = musicLinker.get(checkThis).getSoundLocation().toString().replaceAll("music.","").replaceAll("riggers:","");
                                    if (configRegistry.registry.registerDiscs && MusicPicker.player != null) {
                                        RegistryHandler.network.sendToServer(new packetCurSong.packetCurSongMessage(curTrack, MusicPicker.player.getUniqueID()));
                                    }
                                }
                                else {
                                    musicLinker.get(checkThis).setVolume(0.01F);
                                    sndSys.setVolume(temp,0.01F);
                                }
                            }
                        }
                        MusicPicker.shouldChange = false;
                    } else if (curMusic == null && mc.gameSettings.getSoundLevel(SoundCategory.MUSIC)>0 && mc.gameSettings.getSoundLevel(SoundCategory.MASTER)>0) {
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
                            if(curTrack!=null) {
                                MusicTriggers.logger.info("cur track to play: " + curTrack);
                                curTrack = configToml.songholder.get(curTrack);
                                MusicTriggers.logger.info("cur track to play: " + curTrack);
                                List<String> linkedTracks = new ArrayList<>();
                                linkedTracks.add(curTrack);
                                if (configToml.triggerlinking.get(curTrack) != null) {
                                    linkedTracks.addAll(configToml.triggerlinking.get(curTrack).keySet());
                                    for (int in = 0; in < linkedTracks.size(); in++) {
                                        triggerLinker.put("song-" + in, configToml.triggerlinking.get(curTrack).get(in));
                                        musicLinker.put("song-" + in, new setVolumeSound(new ResourceLocation(MusicTriggers.MODID, "music." + linkedTracks.get(in)), SoundCategory.MUSIC, 1F, Float.parseFloat(configToml.otherinfo.get(configToml.reversesongholder.get(in))[0]), false, 1, ISound.AttenuationType.NONE, 0F, 0F, 0F));
                                    }
                                } else {
                                    musicLinker.put("song-" + 0, new setVolumeSound(new ResourceLocation(MusicTriggers.MODID, "music." + curTrack), SoundCategory.MUSIC, 1F, Float.parseFloat(configToml.otherinfo.get(configToml.reversesongholder.get(curTrack))[0]), false, 1, ISound.AttenuationType.NONE, 0F, 0F, 0F));
                                }
                                if (configRegistry.registry.registerDiscs && MusicPicker.player != null) {
                                    RegistryHandler.network.sendToServer(new packetCurSong.packetCurSongMessage(curTrack, MusicPicker.player.getUniqueID()));
                                }
                                mc.getSoundHandler().stopSounds();
                                for (Map.Entry<String, setVolumeSound> stringListEntry : musicLinker.entrySet()) {
                                    String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                                    if (!checkThis.matches("song-0")) {
                                        musicLinker.get(checkThis).setVolume(0.01F);
                                    } else {
                                        curMusic = musicLinker.get(checkThis);
                                    }
                                    mc.getSoundHandler().playSound(musicLinker.get(checkThis));
                                }
                            }
                            else {
                                curTrackList = null;
                            }
                        }
                    }
                } else {
                    if (curMusic != null) {
                        mc.getSoundHandler().stopSound(curMusic);
                        curMusic = null;
                    }
                }
            }
            tickCounter++;
        }
    }

    public static void renderCards() {
        for (String t : configTitleCards.TitleCards) {
            String[] line = t.split(",");
            String[] temp = Arrays.copyOfRange(line, 2, line.length);
            if (tempTitleCards.containsAll(Arrays.asList(temp)) && mc.player != null) {
                mc.ingameGUI.displayTitle("\u00A74" + line[0], line[1], 5, 20, 20);
                //noinspection ConstantConditions
                mc.ingameGUI.displayTitle(null, line[1], 5, 20, 20);
            }
        }
        for (String t : configTitleCards.ImageCards) {
            String[] line = t.split(",");
            String[] temp = Arrays.copyOfRange(line, 1, line.length);
            if (tempTitleCards.containsAll(Arrays.asList(temp)) && mc.player != null) {
                eventsClient.IMAGE_CARD = new ResourceLocation(MusicTriggers.MODID, "textures/" + line[0] + ".png");
                eventsClient.activated = true;
            }
        }
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