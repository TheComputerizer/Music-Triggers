package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.config;
import mods.thecomputerizer.musictriggers.configRegistry;
import mods.thecomputerizer.musictriggers.configTitleCards;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.musictriggers.util.packetCurSong;
import mods.thecomputerizer.musictriggers.util.setVolumeSound;
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
import net.minecraftforge.fml.relauncher.ReflectionHelper;
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
                    MusicPicker.persistentBiome.put(biomeRegex, MusicPicker.lightPersistence.get(biomeRegex) - 1);
                }
            }
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.lightSongsString.entrySet()) {
                String lightName = ((Map.Entry) stringListEntry).getKey().toString();
                MusicPicker.lightPersistence.putIfAbsent(lightName, 0);
                if (MusicPicker.lightPersistence.get(lightName) > 0) {
                    MusicPicker.lightPersistence.put(lightName, MusicPicker.lightPersistence.get(lightName) - 1);
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
                            delayTime = config.universalDelay;
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
                                MusicTriggers.logger.info("Song Index: "+checkThis);
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
                            MusicTriggers.logger.info("Attempting to switch music volumes...");
                            Map<String, ISound> curplaying = ObfuscationReflectionHelper.getPrivateValue(SoundManager.class,ObfuscationReflectionHelper.getPrivateValue(net.minecraft.client.audio.SoundHandler.class,mc.getSoundHandler(),"field_147694_f"),"field_148629_h");
                            SoundSystem sndSys = ObfuscationReflectionHelper.getPrivateValue(SoundManager.class,ObfuscationReflectionHelper.getPrivateValue(net.minecraft.client.audio.SoundHandler.class,Minecraft.getMinecraft().getSoundHandler(),"field_147694_f"),"field_148620_e");
                            for (Map.Entry<String, setVolumeSound> stringListEntry : musicLinker.entrySet()) {
                                String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                                String temp = curplaying.entrySet().stream().filter(entry -> entry.getValue()==musicLinker.get(checkThis)).map(Map.Entry::getKey).findFirst().orElse(null);
                                MusicTriggers.logger.info("Found song id for "+checkThis+" to be +"+temp);
                                if(checkThis.matches(songNum)) {
                                    musicLinker.get(checkThis).setVolume(1F);
                                    sndSys.setVolume(temp,1F);
                                    MusicTriggers.logger.info("Set Volume of "+checkThis+" to 1");
                                    curMusic = musicLinker.get(checkThis);
                                    curTrack = musicLinker.get(checkThis).getSoundLocation().toString().replaceAll("music.","").replaceAll("riggers:","");
                                    if (configRegistry.registry.registerDiscs && MusicPicker.player != null) {
                                        RegistryHandler.network.sendToServer(new packetCurSong.packetCurSongMessage(curTrack, MusicPicker.player.getUniqueID()));
                                        MusicTriggers.logger.info("Song packet successfully updated!");
                                    }
                                }
                                else {
                                    musicLinker.get(checkThis).setVolume(0.01F);
                                    sndSys.setVolume(temp,0.01F);
                                    MusicTriggers.logger.info("Set Volume of "+checkThis+" to 0.01");
                                }
                            }
                        }
                        MusicPicker.shouldChange = false;
                    } else if (curMusic == null && mc.gameSettings.getSoundLevel(SoundCategory.MUSIC)>0 && mc.gameSettings.getSoundLevel(SoundCategory.MASTER)>0) {
                        eventsClient.GuiCounter = 0;
                        if (curTrackList.length >= 1) {
                            int i = rand.nextInt(curTrackList.length);
                            if (curTrackList.length > 1 && curTrack != null) {
                                while (curTrack.equals(curTrackList[i])) {
                                    i = rand.nextInt(curTrackList.length);
                                }
                            }
                            curTrack = curTrackList[i];
                            String[] linked = stringBreaker(curTrack,";");
                            musicLinker = new HashMap<>();
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
                            if (configRegistry.registry.registerDiscs && MusicPicker.player != null) {
                                RegistryHandler.network.sendToServer(new packetCurSong.packetCurSongMessage(curTrack, MusicPicker.player.getUniqueID()));
                            }
                            mc.getSoundHandler().stopSounds();
                            for (Map.Entry<String, setVolumeSound> stringListEntry : musicLinker.entrySet()) {
                                String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                                if(!checkThis.matches("song-0")) {
                                    musicLinker.get(checkThis).setVolume(0.01F);
                                }
                                else {
                                    curMusic = musicLinker.get(checkThis);
                                }
                                mc.getSoundHandler().playSound(musicLinker.get(checkThis));
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

    public static String[] stringBreaker(String s, String regex) {
        return s.split(regex);
    }

    public static boolean theDecidingFactor(List<String> all, List<String> titlecard, String[] comparison) {
        StringBuilder oldtrig = new StringBuilder("All triggers: ");
        for (String tl : all) {
            oldtrig.append(tl).append(" ");
        }
        MusicTriggers.logger.info(oldtrig);
        StringBuilder newtrig = new StringBuilder("New triggers: ");
        for (String tl : titlecard) {
            newtrig.append(tl).append(" ");
        }
        MusicTriggers.logger.info(newtrig);
        StringBuilder comptrig = new StringBuilder("Comparison triggers: ");
        for (String tl : comparison) {
            comptrig.append(tl).append(" ");
        }
        MusicTriggers.logger.info(comptrig);
        List<String> updatedComparison = new ArrayList<>();
        boolean cont = false;
        for(String el : comparison) {
            if(titlecard.contains(el)) {
                MusicTriggers.logger.info("Caught "+el);
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
            StringBuilder updcomptrig = new StringBuilder("Updated comparison triggers: ");
            for (String tl : updatedComparison) {
                updcomptrig.append(tl).append(" ");
            }
            MusicTriggers.logger.info(updcomptrig);
            return all.containsAll(updatedComparison);
        }
        return false;
    }
}
