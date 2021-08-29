package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.configDebug;
import net.minecraft.block.BlockJukebox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

@Mod.EventBusSubscriber(modid=MusicTriggers.MODID, value = Side.CLIENT)
public class MusicPlayer {

    public static String[] curTrackList;
    public static String[] holder;
    public static String curTrack;
    private static ISound curMusic;
    public static Random rand = new Random();
    public static Minecraft mc = Minecraft.getMinecraft();
    public static int tickCounter = 0;

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if(tickCounter%10==0) {
            boolean playing = false;
            if(MusicPicker.player!=null) {
                for (int x = MusicPicker.player.chunkCoordX - 3; x <= MusicPicker.player.chunkCoordX + 3; x++) {
                    for (int z = MusicPicker.player.chunkCoordZ - 3; z <= MusicPicker.player.chunkCoordZ + 3; z++) {
                        Map<BlockPos, TileEntity> currentChunkTE = MusicPicker.world.getChunk(x, z).getTileEntityMap();
                        for (TileEntity te : currentChunkTE.values()) {
                            if (te != null) {
                                if (te instanceof BlockJukebox.TileEntityJukebox) {
                                    if (te.getBlockMetadata()!=0) {
                                        playing = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            holder = MusicPicker.playThese();
            if (holder!=null && !Arrays.asList(holder).isEmpty() && !playing) {
                if (curTrackList == null) {
                    curTrackList = holder;
                }
                if(configDebug.FinalSongs) {
                    for (String print : curTrackList) {
                        MusicPicker.player.sendMessage(new TextComponentString(print));
                    }
                }
                if (curMusic != null) {
                    if (!mc.getSoundHandler().isSoundPlaying(curMusic)) {
                        curMusic = null;
                    }
                }
                if (!Arrays.asList(curTrackList).containsAll(Arrays.asList(holder)) && !Arrays.asList(holder).containsAll(Arrays.asList(curTrackList))) {
                    curTrackList = null;
                    mc.getSoundHandler().stopSound(curMusic);
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
                        if(!mc.getSoundHandler().isSoundPlaying(curMusic)) {
                            mc.getSoundHandler().playSound(curMusic);
                        }
                    }
                }
            }
            else {
                if(curMusic!=null) {
                    mc.getSoundHandler().stopSound(curMusic);
                    curMusic=null;
                }
            }
        }
        tickCounter++;
    }
}
