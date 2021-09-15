package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.common.eventsCommon;
import mods.thecomputerizer.musictriggers.configDebug;
import mods.thecomputerizer.musictriggers.configTitleCards;
import net.minecraft.block.BlockJukebox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.*;

@Mod.EventBusSubscriber(modid = MusicTriggers.MODID, value = Side.CLIENT)
public class MusicPlayer {

    public static String[] curTrackList;
    public static String[] holder;
    public static String curTrack;
    private static ISound curMusic;
    public static Random rand = new Random();
    public static Minecraft mc = Minecraft.getMinecraft();
    public static int tickCounter = 0;
    public static boolean fading = false;
    private static int tempFade = 0;
    private static float saveVol = 1;
    public static List<String> tempTitleCards = new ArrayList<>();

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (fading) {
            if (tempFade == 0) {
                fading = false;
                mc.getSoundHandler().stopSound(curMusic);
                mc.getSoundHandler().setSoundLevel(SoundCategory.MASTER, saveVol);
                renderCards();
            } else {
                mc.getSoundHandler().setSoundLevel(SoundCategory.MASTER, saveVol * (float) (((double) tempFade) / ((double) MusicPicker.curFade)));
                tempFade -= 1;
            }
        }
        if (tickCounter % 10 == 0 && !fading) {
            boolean playing = false;
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
                if (configDebug.FinalSongs && eventsCommon.isWorldRendered) {
                    for (String print : curTrackList) {
                        MusicPicker.player.sendMessage(new TextComponentString(print));
                    }
                }
                if (curMusic != null) {
                    if (!mc.getSoundHandler().isSoundPlaying(curMusic)) {
                        mc.getSoundHandler().stopSounds();
                        curMusic = null;
                    }
                }
                if (!Arrays.asList(curTrackList).containsAll(Arrays.asList(holder)) && !Arrays.asList(holder).containsAll(Arrays.asList(curTrackList))) {
                    eventsCommon.IMAGE_CARD = null;
                    curTrackList = null;
                    tempTitleCards = MusicPicker.titleCardEvents;
                    if (MusicPicker.curFade == 0) {
                        mc.getSoundHandler().stopSound(curMusic);
                        renderCards();
                    } else {
                        fading = true;
                        tempFade = MusicPicker.curFade;
                        saveVol = mc.gameSettings.getSoundLevel(SoundCategory.MASTER);
                    }
                } else if (curMusic == null && mc.gameSettings.getSoundLevel(SoundCategory.MASTER) > 0 && mc.gameSettings.getSoundLevel(SoundCategory.MUSIC) > 0) {
                    if (curTrackList.length >= 1) {
                        int i = rand.nextInt(curTrackList.length);
                        if (curTrackList.length > 1 && curTrack != null) {
                            while (curTrack.equals(curTrackList[i])) {
                                i = rand.nextInt(curTrackList.length);
                            }
                        }
                        curTrack = curTrackList[i];
                        curMusic = SoundHandler.songsRecords.get(curTrack);
                        mc.getSoundHandler().stopSounds();
                        mc.getSoundHandler().playSound(curMusic);
                    }
                }
            } else {
                if (curMusic != null) {
                    mc.getSoundHandler().stopSound(curMusic);
                    curMusic = null;
                }
            }
            if (eventsCommon.vanilla != null) {
                if (mc.getSoundHandler().isSoundPlaying(eventsCommon.vanilla)) {
                    mc.getSoundHandler().stopSound(eventsCommon.vanilla);
                }
            }
        }
        tickCounter++;
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
                eventsCommon.IMAGE_CARD = new ResourceLocation(MusicTriggers.MODID, "textures/" + line[0] + ".png");
                eventsCommon.activated = true;
            }
        }
    }
}
