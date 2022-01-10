package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.common.objects.BlankRecord;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.util.packets.CurSong;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class eventsCommon {

    public static HashMap<BlockPos, Integer> tickCounter = new HashMap<>();
    public static HashMap<BlockPos, ItemStack> recordHolder = new HashMap<>();
    public static HashMap<BlockPos, UUID> recordUUID = new HashMap<>();
    public static HashMap<BlockPos, Level> recordWorld = new HashMap<>();

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent e) {
        int randomNum = ThreadLocalRandom.current().nextInt(0, 5600);
        for (Map.Entry<BlockPos, ItemStack> blockPosItemStackEntry : recordHolder.entrySet()) {
            BlockPos blockPos = blockPosItemStackEntry.getKey();
            if(recordHolder.get(blockPos)!=null && !recordHolder.get(blockPos).isEmpty() && recordHolder.get(blockPos).getItem() instanceof BlankRecord) {
                tickCounter.put(blockPos,tickCounter.get(blockPos)+1);
                if(randomNum+tickCounter.get(blockPos)>=6000) {
                    recordWorld.get(blockPos).playSound(null,blockPos, new SoundEvent(new ResourceLocation("minecraft","item.trident.thunder")), SoundSource.MASTER,1F,1F);
                    tickCounter.put(blockPos,0);
                    for (SoundEvent s : SoundHandler.allSoundEvents) {
                        String songName = Objects.requireNonNull(s.getRegistryName()).toString().replaceAll("musictriggers:","");
                        if(songName.matches(CurSong.curSong.get(recordUUID.get(blockPos)))) {
                            recordHolder.put(blockPos, Objects.requireNonNull(MusicTriggersRecord.getBySound(s)).getDefaultInstance());
                        }
                    }
                }
            }
            else {
                tickCounter.put(blockPos,0);
            }
        }
    }
}
