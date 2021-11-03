package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.ModSounds;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.config;
import mods.thecomputerizer.musictriggers.configTitleCards;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.packets.CurSong;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

@SuppressWarnings("rawtypes")
public class MusicPlayer {

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
    public static SoundEvent fromRecord = null;
    public static boolean playing = false;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if(MusicPicker.persistentPVP>0) {
            MusicPicker.persistentPVP -= 1;
        }
        for (Map.Entry<Integer, Integer> integerListEntry : MusicPicker.persistentVictory.entrySet()) {
            int victoryID = integerListEntry.getKey();
            MusicPicker.persistentVictory.putIfAbsent(victoryID,0);
            if(MusicPicker.persistentVictory.get(victoryID)>0) {
                MusicPicker.persistentVictory.put(victoryID,MusicPicker.persistentVictory.get(victoryID)-1);
            }
        }
        for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.mobSongsString.entrySet()) {
            String mobName = ((Map.Entry) stringListEntry).getKey().toString();
            MusicPicker.persistentMob.putIfAbsent(mobName,0);
            if(MusicPicker.persistentMob.get(mobName)>0) {
                MusicPicker.persistentMob.put(mobName,MusicPicker.persistentMob.get(mobName)-1);
            }
        }
        for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.biomeSongsString.entrySet()) {
            String biomeRegex = ((Map.Entry) stringListEntry).getKey().toString();
            MusicPicker.persistentBiome.putIfAbsent(biomeRegex,0);
            if(MusicPicker.persistentBiome.get(biomeRegex)>0) {
                MusicPicker.persistentBiome.put(biomeRegex,MusicPicker.lightPersistence.get(biomeRegex)-1);
            }
        }
        for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.lightSongsString.entrySet()) {
            String lightName = ((Map.Entry) stringListEntry).getKey().toString();
            MusicPicker.lightPersistence.putIfAbsent(lightName,0);
            if(MusicPicker.lightPersistence.get(lightName)>0) {
                MusicPicker.lightPersistence.put(lightName,MusicPicker.lightPersistence.get(lightName)-1);
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
        if(delay) {
            delayTime-=1;
            if(delayTime<=0) {
                delay = false;
            }
        }
        if (tickCounter % 10 == 0 && !fading && !delay) {
            if(MusicPicker.player!=null && (MusicPicker.player.getMainHandItem().getItem() instanceof MusicTriggersRecord)) {
                fromRecord = ((MusicTriggersRecord)MusicPicker.player.getMainHandItem().getItem()).getSound();
            }
            else {
                fromRecord = null;
            }
            playing = false;
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
                if (curMusic != null) {
                    if (!mc.getSoundManager().isActive(curMusic) || mc.options.getSoundSourceVolume(SoundCategory.MUSIC)==0 || mc.options.getSoundSourceVolume(SoundCategory.MASTER)==0) {
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
                        if(MusicPicker.player!=null) {
                            PacketHandler.sendToServer(new CurSong(curTrack, MusicPicker.player.getUUID()));
                        }
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
