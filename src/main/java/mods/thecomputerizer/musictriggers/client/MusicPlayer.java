package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.ModSounds;
import mods.thecomputerizer.musictriggers.config;
import mods.thecomputerizer.musictriggers.configDebug;
import mods.thecomputerizer.musictriggers.configTitleCards;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class MusicPlayer {

    public static List<String> curTrackList;
    public static List<String> holder;
    public static String curTrack;
    private static ISound curMusic;
    public static Random rand = new Random();
    public static Minecraft mc = Minecraft.getInstance();
    public static int tickCounter = 0;
    public static boolean fading = false;
    private static int tempFade = 0;
    private static float saveVol = 1;
    public static List<String> tempTitleCards = new ArrayList<>();
    public static boolean delay = false;
    public static int delayTime = 0;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
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
        if(delay) {
            delayTime-=1;
            if(delayTime<=0) {
                delay = false;
            }
        }
        if (tickCounter % 10 == 0 && !fading && !delay) {
            boolean playing = false;
            if (MusicPicker.player != null) {
                for (int x = MusicPicker.player.xChunk- 3; x <= MusicPicker.player.xChunk + 3; x++) {
                    for (int z = MusicPicker.player.zChunk - 3; z <= MusicPicker.player.zChunk + 3; z++) {
                        Set<BlockPos> currentChunkTEPos = MusicPicker.world.getChunk(x, z).getBlockEntitiesPos();
                        for (BlockPos b : currentChunkTEPos) {
                            if (MusicPicker.world.getChunk(x, z).getBlockEntity(b) instanceof JukeboxTileEntity) {
                                JukeboxTileEntity te = (JukeboxTileEntity)MusicPicker.world.getChunk(x, z).getBlockEntity(b);
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
                if (configDebug.FinalSongs.get() && eventsClient.isWorldRendered) {
                    for (String print : curTrackList) {
                        MusicPicker.player.sendMessage(new TranslationTextComponent(print),MusicPicker.player.getUUID());
                    }
                }
                if (curMusic != null) {
                    if (!mc.getSoundManager().isActive(curMusic)) {
                        mc.getSoundManager().stop();
                        curMusic = null;
                        delay = true;
                        delayTime = config.universalDelay;
                    }
                }
                if (MusicPicker.shouldChange || !Arrays.equals(curTrackList.toArray(new String[0]),holder.toArray(new String[0]))) {
                    eventsClient.IMAGE_CARD = null;
                    curTrackList = null;
                    tempTitleCards = MusicPicker.titleCardEvents;
                    if (MusicPicker.curFade == 0) {
                        mc.getSoundManager().stop(curMusic);
                        renderCards();
                    } else {
                        fading = true;
                        tempFade = MusicPicker.curFade;
                        saveVol = mc.options.getSoundSourceVolume(SoundCategory.MASTER);
                    }
                    MusicPicker.shouldChange = false;
                } else if (curMusic == null && mc.options.getSoundSourceVolume(SoundCategory.MASTER) > 0 && mc.options.getSoundSourceVolume(SoundCategory.MUSIC) > 0) {
                    if (curTrackList.size() >= 1) {
                        int i = rand.nextInt(curTrackList.size());
                        if (curTrackList.size() > 1 && curTrack != null) {
                            while (curTrack.equals(curTrackList.get(i))) {
                                i = rand.nextInt(curTrackList.size());
                            }
                        }
                        curTrack = curTrackList.get(i);
                        curMusic = ModSounds.playableSounds.get("music."+curTrack);
                        mc.getSoundManager().stop();
                        mc.getSoundManager().play(curMusic);
                    }
                }
            } else {
                if (curMusic != null) {
                    mc.getSoundManager().stop(curMusic);
                    curMusic = null;
                }
            }
            if (eventsClient.vanilla != null) {
                if (mc.getSoundManager().isActive(eventsClient.vanilla)) {
                    mc.getSoundManager().stop(eventsClient.vanilla);
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
}
