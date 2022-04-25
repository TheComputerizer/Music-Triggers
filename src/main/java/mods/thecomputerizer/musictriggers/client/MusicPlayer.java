package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.gui.GuiCurPlaying;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.config.ConfigCommands;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.config.ConfigTitleCards;
import mods.thecomputerizer.musictriggers.config.ConfigToml;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.musictriggers.util.audio.SetVolumeSound;
import mods.thecomputerizer.musictriggers.util.audio.SoundManipulator;
import mods.thecomputerizer.musictriggers.util.packets.PacketCurSong;
import mods.thecomputerizer.musictriggers.util.packets.PacketExecuteCommand;
import net.minecraft.block.BlockJukebox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import paulscode.sound.SoundSystem;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = MusicTriggers.MODID, value = Side.CLIENT)
public class MusicPlayer {

    public static final KeyBinding RELOAD = new KeyBinding("key.reload_musictriggers", Keyboard.KEY_R, "key.categories.musictriggers");

    public static String[] curTrackList;
    public static String[] fadeOutList;
    public static String[] holder;
    public static String[] reverseTrackList;
    public static String curTrack;
    public static String curTrackHolder;
    public static SetVolumeSound curMusic;
    public static Random rand = new Random();
    public static Minecraft mc = Minecraft.getMinecraft();
    public static net.minecraft.client.audio.SoundHandler sh;
    public static int tickCounter = 0;
    public static boolean fadingIn = false;
    public static boolean fadingOut = false;
    public static boolean reverseFade = false;
    public static int tempFadeIn = 0;
    public static int tempFadeOut = 0;
    private static int savedFadeOut = 0;
    private static float saveVolIn = 1;
    private static float saveVolOut = 1;
    public static boolean delay = false;
    public static int delayTime = 0;
    public static boolean playing = false;
    public static SoundEvent fromRecord = null;
    public static boolean reloading = false;
    public static boolean cards = true;
    public static boolean finish = false;
    public static HashMap<String, SetVolumeSound> musicLinker = new HashMap<>();
    public static HashMap<String, String[]> triggerLinker = new HashMap<>();
    public static HashMap<String, Float> volumeLinker = new HashMap<>();
    public static HashMap<String, Integer> fadeInLinker = new HashMap<>();
    public static HashMap<String, Integer> fadeOutLinker = new HashMap<>();
    public static HashMap<String, Integer> fadeInLinkerMax = new HashMap<>();
    public static HashMap<String, Integer> fadeOutLinkerMax = new HashMap<>();
    public static HashMap<String, Boolean> linkedFadingIn = new HashMap<>();
    public static HashMap<String, Boolean> linkedFadingOut = new HashMap<>();
    public static HashMap<String, Map<Integer, String[]>> loopLinker = new HashMap<>();
    public static HashMap<String, Map<Integer, Integer>> loopLinkerCounter = new HashMap<>();
    public static List<String> oncePerTrigger = new ArrayList<>();
    public static List<String> onceUntilEmpty = new ArrayList<>();
    private static String trackToDelete;
    private static int indexToDelete;
    private static List<String> playedEvents = new ArrayList<>();
    private static ISound playedMusic;
    public static Map<Integer, Boolean> canPlayTitle = new HashMap<>();
    public static Map<Integer, Boolean> canPlayImage = new HashMap<>();
    public static boolean paused = false;
    private static String prevPlayingLink;
    private static String curLinkNum;
    private static boolean nullFromLink = false;

    @SuppressWarnings("rawtypes")
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onTick(TickEvent.ClientTickEvent event) {
        sh = Minecraft.getMinecraft().getSoundHandler();
        if(curMusic!=null && sh.sndManager.invPlayingSounds.get(curMusic)!=null) {
            if (!Display.isActive() && ((SoundSystem) sh.sndManager.sndSystem).playing(sh.sndManager.invPlayingSounds.get(curMusic))) {
                for (String is : musicLinker.keySet()) {
                    if(sh.sndManager.invPlayingSounds.get(musicLinker.get(is))!=null)
                        ((SoundSystem)sh.sndManager.sndSystem).pause(sh.sndManager.invPlayingSounds.get(musicLinker.get(is)));
                }
                paused = true;
            } else if (paused && Display.isActive() && !playing && !mc.isGamePaused()) {
                for (String is : musicLinker.keySet()) {
                    if(sh.sndManager.invPlayingSounds.get(musicLinker.get(is))!=null)
                        ((SoundSystem)sh.sndManager.sndSystem).play(sh.sndManager.invPlayingSounds.get(musicLinker.get(is)));
                }
                paused = false;
            } else if (paused && playing && ((SoundSystem)sh.sndManager.sndSystem).playing(sh.sndManager.invPlayingSounds.get(curMusic))) {
                for (String is : musicLinker.keySet()) {
                    if(sh.sndManager.invPlayingSounds.get(musicLinker.get(is))!=null)
                        ((SoundSystem) sh.sndManager.sndSystem).pause(sh.sndManager.invPlayingSounds.get(musicLinker.get(is)));
                }
            }
        }
        if(playing && tickCounter % 10 == 0) {
            if (MusicPicker.player != null && (MusicPicker.player.getHeldItemMainhand().getItem() instanceof MusicTriggersRecord)) fromRecord = ((MusicTriggersRecord) MusicPicker.player.getHeldItemMainhand().getItem()).getSound();
            else fromRecord = null;
            playing = false;
            if (MusicPicker.player != null) {
                for (int x = MusicPicker.player.chunkCoordX - 3; x <= MusicPicker.player.chunkCoordX + 3; x++) {
                    for (int z = MusicPicker.player.chunkCoordZ - 3; z <= MusicPicker.player.chunkCoordZ + 3; z++) {
                        Map<BlockPos, TileEntity> currentChunkTE = MusicPicker.world.getChunkFromChunkCoords(x, z).getTileEntityMap();
                        for (TileEntity te : currentChunkTE.values()) {
                            if (te != null) {
                                if (te instanceof BlockJukebox.TileEntityJukebox) {
                                    if (te.getBlockMetadata() != 0) playing = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        if(paused && Display.isActive() && !playing && !mc.isGamePaused()) paused = false;
        if(!reloading && tickCounter % 2 == 0 && !paused && Display.isActive()) {
            if (MusicPicker.fishBool) MusicPicker.fishingStart++;
            if (MusicPicker.waterBool) MusicPicker.waterStart++;
            for (Map.Entry<String, Integer> stringListEntry : MusicPicker.triggerPersistence.entrySet()) {
                String eventID = ((Map.Entry) stringListEntry).getKey().toString();
                MusicPicker.triggerPersistence.putIfAbsent(eventID, 0);
                if (MusicPicker.triggerPersistence.get(eventID) > 0) MusicPicker.triggerPersistence.put(eventID, MusicPicker.triggerPersistence.get(eventID) - 1);
            }
            if(curTrack!=null && sh.isSoundPlaying(curMusic) && ConfigToml.loopPoints.containsKey(curTrack)) {
                for(String key : musicLinker.keySet()) {
                    if(loopLinker.get(key)!=null) {
                        for (int i : loopLinker.get(key).keySet()) {
                            try {
                                if (loopLinkerCounter.get(key).get(i) < Integer.parseInt(loopLinker.get(key).get(i)[0]) && Integer.parseInt(loopLinker.get(key).get(i)[2]) <= SoundManipulator.getMillisecondTimeForSource(sh.sndManager.sndSystem, sh.sndManager.invPlayingSounds.get(musicLinker.get(key)))) {
                                    MusicTriggers.logger.debug("Loop boundary passed");
                                    SoundManipulator.setMillisecondTimeForSource(sh.sndManager.sndSystem, sh.sndManager.invPlayingSounds.get(musicLinker.get(key)), Integer.parseInt(loopLinker.get(key).get(i)[1]));
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
            if(curTrack!=null && sh.isSoundPlaying(curMusic) && mc.currentScreen instanceof GuiCurPlaying) {
                ((GuiCurPlaying)mc.currentScreen).setSlider(GuiCurPlaying.getSongPosInSeconds(curMusic));
            }
            for(String key : linkedFadingIn.keySet()) {
                String temp = sh.sndManager.invPlayingSounds.get(musicLinker.get(key));
                if(linkedFadingIn.get(key) && ((SoundSystem)sh.sndManager.sndSystem).playing(temp)) {
                    if(fadeInLinker.get(key)==0) {
                        linkedFadingIn.put(key, false);
                        fadeInLinker.put(key, fadeInLinkerMax.get(key));
                        musicLinker.get(key).setVolume(volumeLinker.get(key));
                        ((SoundSystem)sh.sndManager.sndSystem).setVolume(temp, volumeLinker.get(key)*mc.gameSettings.getSoundLevel(SoundCategory.MUSIC));
                    } else {
                        float calculatedVolume = volumeLinker.get(key)*(((float)(fadeInLinkerMax.get(key)-fadeInLinker.get(key)))/((float)fadeInLinkerMax.get(key)));
                        musicLinker.get(key).setVolume(calculatedVolume);
                        calculatedVolume = calculatedVolume*mc.gameSettings.getSoundLevel(SoundCategory.MUSIC);
                        ((SoundSystem)sh.sndManager.sndSystem).setVolume(temp, calculatedVolume);
                        fadeInLinker.put(key, fadeInLinker.get(key)-1);
                    }
                }
            }
            for(String key : linkedFadingOut.keySet()) {
                String temp = sh.sndManager.invPlayingSounds.get(musicLinker.get(key));
                if(linkedFadingOut.get(key) && ((SoundSystem)sh.sndManager.sndSystem).playing(temp)) {
                    if(fadeOutLinker.get(key)==0) {
                        linkedFadingOut.put(key, false);
                        fadeOutLinker.put(key, fadeOutLinkerMax.get(key));
                        musicLinker.get(key).setVolume(Float.MIN_VALUE*1000);
                        ((SoundSystem)sh.sndManager.sndSystem).setVolume(temp, Float.MIN_VALUE*1000);
                    } else {
                        float calculatedVolume = volumeLinker.get(key)*(((float)fadeOutLinker.get(key))/((float)fadeOutLinkerMax.get(key)));
                        musicLinker.get(key).setVolume(calculatedVolume);
                        calculatedVolume = calculatedVolume*mc.gameSettings.getSoundLevel(SoundCategory.MUSIC);
                        ((SoundSystem)sh.sndManager.sndSystem).setVolume(temp, calculatedVolume);
                        fadeOutLinker.put(key, fadeOutLinker.get(key)-1);
                    }
                }
            }
            if(fadingIn&&!fadingOut) {
                reverseFade = false;
                String temp = sh.sndManager.invPlayingSounds.get(curMusic);
                if(tempFadeIn==0) {
                    fadingIn = false;
                    curMusic.setVolume(saveVolIn);
                    ((SoundSystem)sh.sndManager.sndSystem).setVolume(temp, saveVolIn*mc.gameSettings.getSoundLevel(SoundCategory.MUSIC));
                }
                else {
                    float calculatedVolume = saveVolIn*(float) (((double)(MusicPicker.curFadeIn-tempFadeIn))/((double)MusicPicker.curFadeIn));
                    curMusic.setVolume(calculatedVolume);
                    calculatedVolume = calculatedVolume*mc.gameSettings.getSoundLevel(SoundCategory.MUSIC);
                    ((SoundSystem)sh.sndManager.sndSystem).setVolume(temp,calculatedVolume);
                    tempFadeIn-=1;
                }
            }
            if (fadingOut && !reverseFade) {
                String temp = sh.sndManager.invPlayingSounds.get(curMusic);
                tempFadeIn = 0;
                fadingIn = false;
                if (tempFadeOut == 0) {
                    fadeOutList = null;
                    removeTrack(trackToDelete,indexToDelete,playedEvents,playedMusic);
                    fadingOut = false;
                    curTrackList = null;
                    reverseTrackList = null;
                    for(String sound : musicLinker.keySet()) sh.stopSound(musicLinker.get(sound));
                    musicLinker = new HashMap<>();
                    EventsClient.IMAGE_CARD = null;
                    EventsClient.fadeCount = 1000;
                    EventsClient.timer = 0;
                    EventsClient.activated = false;
                    EventsClient.ismoving = false;
                    curMusic = null;
                    curTrack = null;
                    curTrackHolder = null;
                    cards = true;
                    tempFadeIn = MusicPicker.curFadeIn;
                }
                else {
                    if(curMusic==null) tempFadeOut = 0;
                    else {
                        float calculatedVolume = saveVolOut * (float)(((double)tempFadeOut)/((double)savedFadeOut));
                        curMusic.setVolume(calculatedVolume);
                        calculatedVolume = calculatedVolume*mc.gameSettings.getSoundLevel(SoundCategory.MUSIC);
                        ((SoundSystem) sh.sndManager.sndSystem).setVolume(temp, calculatedVolume);
                        tempFadeOut -= 1;
                        if(holder!=null && fadeOutList!=null && Arrays.equals(holder, fadeOutList)) {
                            reverseFade = true;
                        }
                    }
                }
            } else if(fadingOut) {
                String temp = sh.sndManager.invPlayingSounds.get(curMusic);
                if (tempFadeOut >= savedFadeOut) {
                    fadingOut = false;
                    reverseFade = false;
                    curMusic.setVolume(saveVolOut);
                    ((SoundSystem)sh.sndManager.sndSystem).setVolume(temp, saveVolOut*mc.gameSettings.getSoundLevel(SoundCategory.MUSIC));
                    tempFadeOut = 0;
                } else {
                    float calculatedVolume = saveVolOut * (float)(((double)tempFadeOut)/((double)savedFadeOut));
                    curMusic.setVolume(calculatedVolume);
                    calculatedVolume = calculatedVolume*mc.gameSettings.getSoundLevel(SoundCategory.MUSIC);
                    ((SoundSystem) sh.sndManager.sndSystem).setVolume(temp, calculatedVolume);
                    tempFadeOut += 1;
                }
            }
            if (delay) {
                delayTime -= 1;
                if (delayTime <= 0) delay = false;
            }
            if (tickCounter % 10 == 0 && !delay) {
                if (MusicPicker.player != null && (MusicPicker.player.getHeldItemMainhand().getItem() instanceof MusicTriggersRecord)) fromRecord = ((MusicTriggersRecord) MusicPicker.player.getHeldItemMainhand().getItem()).getSound();
                else fromRecord = null;
                playing = false;
                if (MusicPicker.player != null) {
                    for (int x = MusicPicker.player.chunkCoordX - 3; x <= MusicPicker.player.chunkCoordX + 3; x++) {
                        for (int z = MusicPicker.player.chunkCoordZ - 3; z <= MusicPicker.player.chunkCoordZ + 3; z++) {
                            Map<BlockPos, TileEntity> currentChunkTE = MusicPicker.world.getChunkFromChunkCoords(x, z).getTileEntityMap();
                            for (TileEntity te : currentChunkTE.values()) {
                                if (te != null) {
                                    if (te instanceof BlockJukebox.TileEntityJukebox) {
                                        if (te.getBlockMetadata() != 0) playing = true;
                                    }
                                }
                            }
                        }
                    }
                }
                holder = MusicPicker.playThese();
                if (holder != null && !Arrays.asList(holder).isEmpty() && !playing) {
                    boolean startQuiet = false;
                    for(int i : canPlayTitle.keySet()) {
                        if(!canPlayTitle.get(i) && !MusicPicker.playableList.containsAll(ConfigTitleCards.titlecards.get(i).getTriggers())) canPlayTitle.put(i, true);
                    }
                    for(int i : canPlayImage.keySet()) {
                        if(!canPlayImage.get(i) && !MusicPicker.playableList.containsAll(ConfigTitleCards.imagecards.get(i).getTriggers())) canPlayImage.put(i, true);
                    }
                    for(String playable : MusicPicker.playableList) {
                        if(!MusicPicker.titleCardEvents.contains(playable)) {
                            if(Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get(playable)[34])) {
                                SoundHandler.TriggerIdentifierMap.get(playable.split("-")[0]).remove(SoundHandler.TriggerInfoMap.get(playable)[10]);
                                SoundHandler.TriggerInfoMap.remove(playable);
                                if(SoundHandler.TriggerIdentifierMap.get(playable.split("-")[0]).isEmpty()) {
                                    SoundHandler.TriggerIdentifierMap.remove(playable.split("-")[0]);
                                    SoundHandler.TriggerInfoMap.remove(playable.split("-")[0]);
                                }
                            }
                        }
                    }
                    if (curTrackList == null && !finish) {
                        curTrackList = holder;
                        if(fadeOutList==null) fadeOutList = holder;
                        if(!nullFromLink) {
                            fadingIn = true;
                            startQuiet = true;
                            tempFadeIn = MusicPicker.curFadeIn;
                            savedFadeOut = MusicPicker.curFadeOut;
                        }
                        nullFromLink = false;
                        for(String command : ConfigCommands.commandMap.keySet()) {
                            if(Arrays.asList(curTrackList).containsAll(ConfigCommands.commandMap.get(command)) && ConfigCommands.commandMap.get(command).containsAll(Arrays.asList(curTrackList))) {
                                RegistryHandler.network.sendToServer(new PacketExecuteCommand.packetExecuteCommandMessage(command, mc.player.getPermissionLevel()));
                            }
                        }
                    }
                    if (curMusic != null) {
                        if (!sh.isSoundPlaying(curMusic) || mc.gameSettings.getSoundLevel(SoundCategory.MUSIC) == 0 || mc.gameSettings.getSoundLevel(SoundCategory.MASTER) == 0) {
                            finish = false;
                            for(String sound : musicLinker.keySet()) sh.stopSound(musicLinker.get(sound));
                            if(fadingOut) {
                                fadingOut = false;
                                fadingIn = true;
                                startQuiet = true;
                            }
                            curMusic = null;
                            delay = true;
                            delayTime = MusicPicker.curDelay;
                            removeTrack(trackToDelete,indexToDelete,playedEvents,playedMusic);
                        }
                    }
                    if (!finish) {
                        if (MusicPicker.shouldChange || !Arrays.equals(curTrackList, holder)) {
                            removeTrack(trackToDelete,indexToDelete,playedEvents,playedMusic);
                            if(curTrackList.length!=0) changeTrack();
                            else curTrackList = null;
                        } else if (curMusic == null && mc.gameSettings.getSoundLevel(SoundCategory.MUSIC) > 0 && mc.gameSettings.getSoundLevel(SoundCategory.MASTER) > 0) {
                            triggerLinker = new HashMap<>();
                            if(musicLinker!=null) {
                                for(String sound : musicLinker.keySet()) sh.stopSound(musicLinker.get(sound));
                            }
                            musicLinker = new HashMap<>();
                            EventsClient.GuiCounter = 0;
                            curTrackList = Arrays.stream(curTrackList).filter(track -> !oncePerTrigger.contains(track)).toArray(String[]::new);
                            curTrackList = Arrays.stream(curTrackList).filter(track -> !onceUntilEmpty.contains(track)).toArray(String[]::new);
                            if (curTrackList.length >= 1) {
                                int i = ThreadLocalRandom.current().nextInt(0, curTrackList.length);
                                if (curTrackList.length > 1 && curTrack != null) {
                                    int total = Arrays.stream(curTrackList).mapToInt(s -> Integer.parseInt(ConfigToml.otherinfo.get(s)[3])).sum();
                                    int j;
                                    for (j = 0; j < 1000; j++) {
                                        int r = ThreadLocalRandom.current().nextInt(1, total + 1);
                                        MusicTriggers.logger.debug("Random was between 1 and " + (total + 1) + " " + r + " was chosen");
                                        String temp = " ";
                                        for (String s : curTrackList) {
                                            if (r < Integer.parseInt(ConfigToml.otherinfo.get(s)[3])) {
                                                temp = s;
                                                break;
                                            }
                                            r -= Integer.parseInt(ConfigToml.otherinfo.get(s)[3]);
                                        }
                                        if (!temp.matches(curTrack) && !temp.matches(" ")) {
                                            curTrack = temp;
                                            break;
                                        }
                                    }
                                    if (j >= 1000) MusicTriggers.logger.warn("Attempt to get non duplicate song passed 1000 tries! Forcing current song " + ConfigToml.songholder.get(curTrack) + " to play.");
                                } else curTrack = curTrackList[i];
                                if (curTrack != null) {
                                    curTrack = curTrack.replaceAll("@","").replaceAll("#","");
                                    MusicTriggers.logger.debug(curTrack + " was chosen");
                                    finish = Boolean.parseBoolean(ConfigToml.otherinfo.get(curTrack)[2]);
                                    curTrackHolder = ConfigToml.songholder.get(curTrack);
                                    MusicTriggers.logger.info("Attempting to play track: " + curTrackHolder);
                                    if (ConfigToml.triggerlinking.get(curTrack) != null) {
                                        triggerLinker.put("song-" + 0, ConfigToml.triggerlinking.get(curTrack).get(curTrack));
                                        musicLinker.put("song-" + 0, new SetVolumeSound(new ResourceLocation(MusicTriggers.MODID, "music." + curTrackHolder), SoundCategory.MUSIC, Float.parseFloat(ConfigToml.otherinfo.get(curTrack)[4]), Float.parseFloat(ConfigToml.otherinfo.get(curTrack)[0]), false, 1, ISound.AttenuationType.NONE, 0F, 0F, 0F));
                                        volumeLinker.put("song-" + 0, Float.parseFloat(ConfigToml.otherinfo.get(curTrack)[4]));
                                        saveVolIn = Float.parseFloat(ConfigToml.otherinfo.get(curTrack)[4]);
                                        fadeInLinker.put("song-" + 0,Integer.parseInt(ConfigToml.otherinfo.get(curTrack)[5]));
                                        fadeInLinkerMax.put("song-" + 0,Integer.parseInt(ConfigToml.otherinfo.get(curTrack)[5]));
                                        fadeOutLinker.put("song-" + 0,Integer.parseInt(ConfigToml.otherinfo.get(curTrack)[6]));
                                        fadeOutLinkerMax.put("song-" + 0,Integer.parseInt(ConfigToml.otherinfo.get(curTrack)[6]));
                                        for(int l : ConfigToml.loopPoints.get(curTrack).keySet()) {
                                            loopLinker.putIfAbsent("song-" + 0, new HashMap<>());
                                            loopLinker.get("song-" + 0).put(l, ConfigToml.loopPoints.get(curTrack).get(l));
                                            loopLinkerCounter.putIfAbsent("song-" + 0, new HashMap<>());
                                            loopLinkerCounter.get("song-" + 0).put(l, 0);
                                        }
                                        int linkcounter = 1;
                                        for (String song : ConfigToml.triggerlinking.get(curTrack).keySet()) {
                                            if (!song.matches(curTrack)) {
                                                triggerLinker.put("song-" + linkcounter, ConfigToml.triggerlinking.get(curTrack).get(song));
                                                musicLinker.put("song-" + linkcounter, new SetVolumeSound(new ResourceLocation(MusicTriggers.MODID, "music." + song), SoundCategory.MUSIC, Float.parseFloat(ConfigToml.otherlinkinginfo.get(curTrack).get(song)[1]),
                                                        Float.parseFloat(ConfigToml.otherlinkinginfo.get(curTrack).get(song)[0]), false, 1, ISound.AttenuationType.NONE, 0F, 0F, 0F));
                                                volumeLinker.put("song-" + linkcounter, Float.parseFloat(ConfigToml.otherlinkinginfo.get(curTrack).get(song)[1]));
                                                fadeInLinker.put("song-" + linkcounter,Integer.parseInt(ConfigToml.otherlinkinginfo.get(curTrack).get(song)[2]));
                                                fadeInLinkerMax.put("song-" + linkcounter,Integer.parseInt(ConfigToml.otherlinkinginfo.get(curTrack).get(song)[2]));
                                                fadeOutLinker.put("song-" + linkcounter,Integer.parseInt(ConfigToml.otherlinkinginfo.get(curTrack).get(song)[3]));
                                                fadeOutLinkerMax.put("song-" + linkcounter,Integer.parseInt(ConfigToml.otherlinkinginfo.get(curTrack).get(song)[3]));
                                                if(ConfigToml.linkingLoopPoints.get(curTrack)!=null && ConfigToml.linkingLoopPoints.get(curTrack).get(song)!=null) {
                                                    for (int l : ConfigToml.linkingLoopPoints.get(curTrack).get(song).keySet()) {
                                                        loopLinker.putIfAbsent("song-" + linkcounter, new HashMap<>());
                                                        loopLinker.get("song-" + linkcounter).put(l, ConfigToml.linkingLoopPoints.get(curTrack).get(song).get(l));
                                                        loopLinkerCounter.putIfAbsent("song-" + linkcounter, new HashMap<>());
                                                        loopLinkerCounter.get("song-" + linkcounter).put(l, 0);
                                                    }
                                                }
                                            }
                                            linkcounter++;
                                        }
                                    } else {
                                        musicLinker.put("song-" + 0, new SetVolumeSound(new ResourceLocation(MusicTriggers.MODID, "music." + curTrackHolder), SoundCategory.MUSIC, Float.parseFloat(ConfigToml.otherinfo.get(curTrack)[4]), Float.parseFloat(ConfigToml.otherinfo.get(curTrack)[0]), false, 1, ISound.AttenuationType.NONE, 0F, 0F, 0F));
                                        saveVolIn = Float.parseFloat(ConfigToml.otherinfo.get(curTrack)[4]);
                                        volumeLinker.put("song-" + 0, Float.parseFloat(ConfigToml.otherinfo.get(curTrack)[4]));
                                        for(int l : ConfigToml.loopPoints.get(curTrack).keySet()) {
                                            loopLinker.putIfAbsent("song-" + 0, new HashMap<>());
                                            loopLinker.get("song-" + 0).put(l, ConfigToml.loopPoints.get(curTrack).get(l));
                                            loopLinkerCounter.putIfAbsent("song-" + 0, new HashMap<>());
                                            loopLinkerCounter.get("song-" + 0).put(l, 0);
                                        }
                                    }
                                    if (ConfigRegistry.registerDiscs && MusicPicker.player != null)
                                        RegistryHandler.network.sendToServer(new PacketCurSong.packetCurSongMessage(curTrackHolder, MusicPicker.player.getUniqueID()));
                                    if (cards) renderCards();
                                    for (Map.Entry<String, SetVolumeSound> stringListEntry : musicLinker.entrySet()) {
                                        String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                                        musicLinker.get(checkThis).setVolume(Float.MIN_VALUE*1000);
                                        if (checkThis.matches("song-0")) {
                                            if(!startQuiet) musicLinker.get(checkThis).setVolume(saveVolIn);
                                            curMusic = musicLinker.get(checkThis);
                                            prevPlayingLink = "song-" + 0;
                                            curLinkNum = "song-" + 0;
                                            sh.playSound(curMusic);
                                        }
                                        else {
                                            sh.playSound(musicLinker.get(checkThis));
                                        }
                                    }
                                    if (Integer.parseInt(ConfigToml.otherinfo.get(curTrack)[1])==1) onceUntilEmpty.add(curTrack);
                                    if (Integer.parseInt(ConfigToml.otherinfo.get(curTrack)[1])==2) oncePerTrigger.add(curTrack);
                                    else if (Integer.parseInt(ConfigToml.otherinfo.get(curTrack)[1])==3) {
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
                } else if (!finish) {
                    if (!playing) {
                        if (curMusic != null) {
                            if (!fadingOut) {
                                fadingOut = true;
                                tempFadeOut = MusicPicker.curFadeOut;
                                saveVolOut = volumeLinker.get(curLinkNum);
                            }
                        } else if (curTrack!=null) {
                            curTrack = null;
                            EventsClient.IMAGE_CARD = null;
                            EventsClient.fadeCount = 1000;
                            EventsClient.timer = 0;
                            EventsClient.activated = false;
                            EventsClient.ismoving = false;
                            cards = true;
                        }
                    } else {
                        EventsClient.IMAGE_CARD = null;
                        EventsClient.fadeCount = 1000;
                        EventsClient.timer = 0;
                        EventsClient.activated = false;
                        EventsClient.ismoving = false;
                        if (curMusic != null) {
                            if(((SoundSystem)sh.sndManager.sndSystem).playing(sh.sndManager.invPlayingSounds.get(curMusic))) {
                                for (String is : musicLinker.keySet()) {
                                    ((SoundSystem)sh.sndManager.sndSystem).pause(sh.sndManager.invPlayingSounds.get(musicLinker.get(is)));
                                }
                                paused = true;
                            }
                        }
                    }
                }
            }
        }
        tickCounter++;
        if(tickCounter==100) tickCounter=10;
    }

    @SuppressWarnings("ConstantConditions")
    public static void renderCards() {
        MusicTriggers.logger.debug("Finding cards to render");
        int markForDeletion = -1;
        for (int i : ConfigTitleCards.titlecards.keySet()) {
            boolean pass = false;
            if(MusicPicker.titleCardEvents.containsAll(ConfigTitleCards.titlecards.get(i).getTriggers()) && ConfigTitleCards.titlecards.get(i).getTriggers().containsAll(MusicPicker.titleCardEvents)) pass=true;
            else if(ConfigTitleCards.titlecards.get(i).getVague() && MusicPicker.playableList.containsAll(ConfigTitleCards.titlecards.get(i).getTriggers()) && canPlayTitle.get(i)) {
                pass=true;
                canPlayTitle.put(i, false);
            }
            if (pass && mc.player != null) {
                MusicTriggers.logger.info("displaying title card "+i);
                if(!ConfigTitleCards.titlecards.get(i).getTitles().isEmpty()) mc.ingameGUI.displayTitle(TextFormatting.getValueByName(ConfigTitleCards.titlecards.get(i).getTitlecolor()).toString()+ ConfigTitleCards.titlecards.get(i).getTitles().get(ThreadLocalRandom.current().nextInt(0, ConfigTitleCards.titlecards.get(i).getTitles().size())), null, 5, 20, 20);
                if(!ConfigTitleCards.titlecards.get(i).getSubTitles().isEmpty()) mc.ingameGUI.displayTitle(null, TextFormatting.getValueByName(ConfigTitleCards.titlecards.get(i).getSubtitlecolor()).toString()+ ConfigTitleCards.titlecards.get(i).getSubTitles().get(ThreadLocalRandom.current().nextInt(0, ConfigTitleCards.titlecards.get(i).getSubTitles().size())), 5, 20, 20);
                if(ConfigTitleCards.titlecards.get(i).getPlayonce()) markForDeletion = i;
                break;
            }
        }
        if(markForDeletion!=-1) {
            ConfigTitleCards.titlecards.remove(markForDeletion);
            markForDeletion = -1;
        }
        for (int i : ConfigTitleCards.imagecards.keySet()) {
            boolean pass = false;
            if(MusicPicker.titleCardEvents.containsAll(ConfigTitleCards.imagecards.get(i).getTriggers()) && ConfigTitleCards.imagecards.get(i).getTriggers().containsAll(MusicPicker.titleCardEvents)) pass=true;
            else if(ConfigTitleCards.imagecards.get(i).getVague() && MusicPicker.playableList.containsAll(ConfigTitleCards.imagecards.get(i).getTriggers()) && canPlayImage.get(i)) {
                pass=true;
                canPlayImage.put(i, false);
            }
            if (pass && mc.player != null) {
                if(ConfigTitleCards.imagecards.get(i).getName()!=null) {
                    MusicTriggers.logger.info("displaying image card " + ConfigTitleCards.imagecards.get(i).getName());
                    if (!ConfigTitleCards.ismoving.get(i)) {
                        EventsClient.IMAGE_CARD = new ResourceLocation(MusicTriggers.MODID, "textures/" + ConfigTitleCards.imagecards.get(i).getName() + ".png");
                    } else {
                        EventsClient.pngs = new ArrayList<>();
                        EventsClient.ismoving = true;
                        EventsClient.movingcounter = 0;
                        File folder = new File("." + "/config/MusicTriggers/songs/assets/musictriggers/textures/" + ConfigTitleCards.imagecards.get(i).getName());
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
                            EventsClient.pngs.add(index, new ResourceLocation(MusicTriggers.MODID, "textures/" + ConfigTitleCards.imagecards.get(i).getName() + "/" + temp.get(index) + ".png"));
                        }
                        EventsClient.timer = Minecraft.getSystemTime();
                    }
                    EventsClient.curImageIndex = i;
                    EventsClient.activated = true;

                    if (ConfigTitleCards.imagecards.get(i).getPlayonce()) markForDeletion = i;
                    break;
                }
            }
        }
        if(markForDeletion!=-1) ConfigTitleCards.imagecards.get(markForDeletion).setName(null);
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
        if(cont) return all.containsAll(updatedComparison);
        return false;
    }

    public static String formatSongTime() {
        String ret = "No song playing";
        if(curMusic!=null && ((SoundSystem) sh.sndManager.sndSystem).playing(sh.sndManager.invPlayingSounds.get(curMusic))) {
            try {
                ret = formattedTimeFromMilliseconds(SoundManipulator.getMillisecondTimeForSource(sh.sndManager.sndSystem, sh.sndManager.invPlayingSounds.get(curMusic)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static String formattedTimeFromMilliseconds(float milliseconds) {
        if(milliseconds!=Integer.MAX_VALUE) {
            if (milliseconds == -1) milliseconds = 0;
            float seconds = milliseconds / 1000f;
            if (seconds % 60 < 10) return (int) (seconds / 60) + ":0" + (int) (seconds % 60) + formatMilliseconds(milliseconds);
            else return(int) (seconds / 60) + ":" + (int) (seconds % 60) + formatMilliseconds(milliseconds);
        }
        return "No song playing";
    }

    private static String formatMilliseconds(float milliseconds) {
        if(milliseconds%1000<10) return ":00"+(int)(milliseconds%1000);
        else if(milliseconds%1000<100) return ":0"+(int)(milliseconds%1000);
        else return ":"+(int)(milliseconds%1000);
    }

    private static void removeTrack(String track, int index, List<String> events, ISound playing) {
        if(track!=null) {
            curTrack = null;
            sh.stopSound(playing);
            curTrackList = ArrayUtils.remove(curTrackList, index);
            for (String ev : events) {
                String[] trigger = ev.split("-");
                if (trigger.length==1) trigger = (ev+"-_").split("-");
                SoundHandler.TriggerIdentifierMap.get(trigger[0]).get(trigger[1]).remove(track);
                if(SoundHandler.TriggerIdentifierMap.get(trigger[0]).get(trigger[1]).isEmpty()) {
                    SoundHandler.TriggerIdentifierMap.get(trigger[0]).remove(trigger[1]);
                    SoundHandler.TriggerInfoMap.remove(trigger[0]+"-"+trigger[1]);
                }
                if(SoundHandler.TriggerIdentifierMap.get(trigger[0]).isEmpty()) {
                    SoundHandler.TriggerIdentifierMap.remove(trigger[0]);
                    SoundHandler.TriggerInfoMap.remove(trigger[0]);
                }
            }
            trackToDelete=null;
            playedEvents = new ArrayList<>();
        }
    }

    private static void changeTrack() {
        EventsClient.GuiCounter = 1;
        String songNum = null;
        for (Map.Entry<String, SetVolumeSound> stringListEntry : musicLinker.entrySet()) {
            String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
            if (triggerLinker.get(checkThis) != null) {
                if (theDecidingFactor(MusicPicker.playableList, MusicPicker.titleCardEvents, triggerLinker.get(checkThis)) && mc.player != null) {
                    songNum = checkThis;
                    break;
                }
            }
        }
        if (songNum == null) {
            if(curLinkNum==null) {
                MusicTriggers.logger.warn("Index of current music was null! Falling back to default fade out volume. You should report this");
                curLinkNum = "song-"+0;
            }
            oncePerTrigger = new ArrayList<>();
            onceUntilEmpty = new ArrayList<>();
            triggerLinker = new HashMap<>();
            loopLinker = new HashMap<>();
            loopLinkerCounter = new HashMap<>();
            if(!fadingOut) {
                fadingOut = true;
                tempFadeOut = MusicPicker.curFadeOut;
                if (curMusic != null) saveVolOut = volumeLinker.get(curLinkNum);
                else tempFadeOut = 0;
            }
        } else {
            nullFromLink = true;
            curTrackList = null;
            EventsClient.IMAGE_CARD = null;
            EventsClient.fadeCount = 1000;
            EventsClient.timer = 0;
            EventsClient.activated = false;
            EventsClient.ismoving = false;
            cards = true;
            String prev = null;
            for (Map.Entry<String, SetVolumeSound> stringListEntry : musicLinker.entrySet()) {
                String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                String temp = sh.sndManager.invPlayingSounds.get(musicLinker.get(checkThis));
                if(loopLinkerCounter.get(checkThis)!=null) {
                    for (int l : loopLinkerCounter.get(checkThis).keySet()) {
                        loopLinkerCounter.get(checkThis).put(l, 0);
                    }
                }
                if (checkThis.matches(songNum)) {
                    prev = checkThis;
                    linkedFadingIn.put(checkThis,true);
                    curMusic = musicLinker.get(checkThis);
                    curLinkNum = checkThis;
                    curTrackHolder = musicLinker.get(checkThis).getSoundLocation().toString().replaceAll("music.", "").replaceAll("riggers:", "");
                    if (ConfigRegistry.registerDiscs && MusicPicker.player != null) {
                        RegistryHandler.network.sendToServer(new PacketCurSong.packetCurSongMessage(curTrack, MusicPicker.player.getUniqueID()));
                    }
                } else if(checkThis.matches(prevPlayingLink)) {
                    linkedFadingOut.put(checkThis,true);
                }
                else {
                    musicLinker.get(checkThis).setVolume(Float.MIN_VALUE*1000);
                    ((SoundSystem)sh.sndManager.sndSystem).setVolume(temp, Float.MIN_VALUE*1000);
                }
            }
            if(prev!=null) prevPlayingLink = prev;
        }
        MusicPicker.shouldChange = false;
    }
}