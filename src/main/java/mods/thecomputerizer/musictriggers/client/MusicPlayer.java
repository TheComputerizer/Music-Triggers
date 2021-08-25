package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.Random;

@Mod.EventBusSubscriber(modid=MusicTriggers.MODID)
public class MusicPlayer {

    public static String[] curTrackList;
    public static String[] holder;
    public static String curTrack;
    private static ISound curMusic;
    public static Random rand = new Random();
    public static Minecraft mc = Minecraft.getMinecraft();
    public static int tickCounter = 0;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if(tickCounter%10==0) {
            holder = MusicPicker.playThese();
            if (holder!=null && !Arrays.asList(holder).isEmpty()) {
                if (curTrackList == null) {
                    curTrackList = holder;
                }
                /* For debug purposes
                for (String print : curTrackList) {
                    System.out.print(print);
                }
                System.out.print("\n");
                 */
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
                        mc.getSoundHandler().playSound(curMusic);
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
