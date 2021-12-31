package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.config;
import mods.thecomputerizer.musictriggers.configRegistry;
import mods.thecomputerizer.musictriggers.configTitleCards;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.packets.CurSong;
import mods.thecomputerizer.musictriggers.util.setVolumeSound;
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
    public static HashMap<String, setVolumeSound> musicLinker = new HashMap<>();
    public static HashMap<String, String[]> triggerLinker = new HashMap<>();

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if(!reloading) {
            if (MusicPicker.persistentPVP > 0) {
                MusicPicker.persistentPVP -= 1;
            }
            for (Map.Entry<Integer, Integer> integerListEntry : MusicPicker.persistentVictory.entrySet()) {
                int victoryID = integerListEntry.getKey();
                MusicPicker.persistentVictory.putIfAbsent(victoryID, 0);
                if (MusicPicker.persistentVictory.get(victoryID) > 0) {
                    MusicPicker.persistentVictory.put(victoryID, MusicPicker.persistentVictory.get(victoryID) - 1);
                }
            }
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.mobSongsString.entrySet()) {
                String mobName = ((Map.Entry) stringListEntry).getKey().toString();
                MusicPicker.persistentMob.putIfAbsent(mobName, 0);
                if (MusicPicker.persistentMob.get(mobName) > 0) {
                    MusicPicker.persistentMob.put(mobName, MusicPicker.persistentMob.get(mobName) - 1);
                }
            }
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.biomeSongsString.entrySet()) {
                String biomeRegex = ((Map.Entry) stringListEntry).getKey().toString();
                MusicPicker.persistentBiome.putIfAbsent(biomeRegex, 0);
                if (MusicPicker.persistentBiome.get(biomeRegex) > 0) {
                    MusicPicker.persistentBiome.put(biomeRegex, MusicPicker.persistentBiome.get(biomeRegex) - 1);
                }
            }
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.lightSongsString.entrySet()) {
                String lightName = ((Map.Entry) stringListEntry).getKey().toString();
                MusicPicker.lightPersistence.putIfAbsent(lightName, 0);
                if (MusicPicker.lightPersistence.get(lightName) > 0) {
                    MusicPicker.lightPersistence.put(lightName, MusicPicker.lightPersistence.get(lightName) - 1);
                }
            }
            if (fading) {
                if (tempFade == 0) {
                    fading = false;
                    mc.getSoundManager().stop(curMusic);
                    mc.getSoundManager().updateSourceVolume(SoundCategory.MASTER, saveVol);
                    renderCards();
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
                            delayTime = config.universalDelay;
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
                                renderCards();
                            } else {
                                fading = true;
                                tempFade = MusicPicker.curFade;
                                saveVol = mc.options.getSoundSourceVolume(SoundCategory.MASTER);
                            }
                        }
                        else {
                            curTrackList = null;
                            renderCards();
                            Map<ISound, ChannelManager.Entry>  curplaying = ObfuscationReflectionHelper.getPrivateValue(SoundEngine.class,ObfuscationReflectionHelper.getPrivateValue(net.minecraft.client.audio.SoundHandler.class,mc.getSoundManager(),"field_147694_f"),"field_217942_m");
                            for (Map.Entry<String, setVolumeSound> stringListEntry : musicLinker.entrySet()) {
                                String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                                if(checkThis.matches(songNum)) {
                                    musicLinker.get(checkThis).setVolume(1F);
                                    if(curplaying.get(musicLinker.get(checkThis))!=null) {
                                        curplaying.get(musicLinker.get(checkThis)).execute(sound -> sound.setVolume(1F));
                                    }
                                    curMusic = musicLinker.get(checkThis);
                                    curTrack = musicLinker.get(checkThis).getLocation().toString().replaceAll("music.","").replaceAll("riggers:","");
                                    if (MusicPicker.player != null) {
                                        if (!configRegistry.clientSideOnly) {
                                            PacketHandler.sendToServer(new CurSong(curTrack, MusicPicker.player.getUUID()));
                                        } else {
                                            curSong.put(MusicPicker.player.getUUID(), curTrack);
                                        }
                                    }
                                }
                                else {
                                    musicLinker.get(checkThis).setVolume(0.01F);
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
                            String[] linked = stringBreaker(curTrack,";");
                            for(int index=0;index<linked.length;index++) {
                                String[] tempTriggers = stringBreaker(linked[index],"/");
                                float pitch = 1F;
                                if(tempTriggers.length>1) {
                                    pitch = Float.parseFloat(stringBreaker(linked[index], "/")[1]);
                                    triggerLinker.put("song-" + index, Arrays.copyOfRange(tempTriggers, 2, tempTriggers.length));
                                }
                                musicLinker.put("song-"+index, new setVolumeSound(new ResourceLocation(MusicTriggers.MODID, "music." +stringBreaker(linked[index],"/")[0]), SoundCategory.MUSIC, 1F, pitch, false, 1, ISound.AttenuationType.NONE, 0F, 0F, 0F));
                            }
                            curTrack = stringBreaker(stringBreaker(curTrack,";")[0],"/")[0];
                            if (MusicPicker.player != null) {
                                if (!configRegistry.clientSideOnly) {
                                    PacketHandler.sendToServer(new CurSong(curTrack, MusicPicker.player.getUUID()));
                                } else {
                                    curSong.put(MusicPicker.player.getUUID(), curTrack);
                                }
                            }
                            mc.getSoundManager().stop();
                            for (Map.Entry<String, setVolumeSound> stringListEntry : musicLinker.entrySet()) {
                                String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                                if(!checkThis.matches("song-0")) {
                                    musicLinker.get(checkThis).setVolume(0.01F);
                                }
                                else {
                                    curMusic = musicLinker.get(checkThis);
                                }
                                mc.getSoundManager().play(musicLinker.get(checkThis));
                            }
                        }
                    }
                } else {
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
        for (String t : configTitleCards.TitleCards) {
            String[] line = t.split(",");
            String[] temp = Arrays.copyOfRange(line, 2, line.length);
            if (tempTitleCards.containsAll(Arrays.asList(temp)) && mc.player != null) {
                mc.gui.setTitles(new TranslationTextComponent(line[0]).withStyle(TextFormatting.DARK_RED), new TranslationTextComponent(line[1]), 5, 20, 20);
                mc.gui.setTitles(null, new TranslationTextComponent(line[1]), 5, 20, 20);
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
