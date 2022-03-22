package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.common.objects.BlankRecord;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import mods.thecomputerizer.musictriggers.util.packets.CurSong;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class eventsCommon {

    public static HashMap<BlockPos, Integer> tickCounter = new HashMap<>();
    public static HashMap<BlockPos, ItemStack> recordHolder = new HashMap<>();
    public static HashMap<BlockPos, UUID> recordUUID = new HashMap<>();
    public static HashMap<BlockPos, World> recordWorld = new HashMap<>();

    public static int bossTimer = 0;

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent e) {
        if(bossTimer>1) bossTimer-=1;
        else if(bossTimer==1) calculateFeatures.bossInfo = new HashMap<>();
        for (String trigger : calculateFeatures.victoryMobs.keySet()) {
            if(!calculateFeatures.allTriggers.contains(trigger)) {
                Map<UUID, Integer> tempMap = calculateFeatures.victoryMobs.get(trigger);
                for (UUID u : tempMap.keySet()) {
                    int temp = tempMap.get(u);
                    if (temp > 0) calculateFeatures.victoryMobs.get(trigger).put(u, temp - 1);
                    else calculateFeatures.victoryMobs.put(trigger, new HashMap<>());
                }
            }
        }
        for (String trigger : calculateFeatures.victoryBosses.keySet()) {
            if(!calculateFeatures.allTriggers.contains(trigger)) {
                Map<String, Integer> tempMap = calculateFeatures.victoryBosses.get(trigger);
                for (int j = 0; j < tempMap.keySet().size(); j++) {
                    int temp = tempMap.get(j);
                    if (temp > 0) calculateFeatures.victoryBosses.get(trigger).put(new ArrayList<>(tempMap.keySet()).get(j), temp - 1);
                    else calculateFeatures.victoryBosses.put(trigger, new HashMap<>());
                }
            }
        }
        int randomNum = ThreadLocalRandom.current().nextInt(0, 5600);
        for (Map.Entry<BlockPos, ItemStack> blockPosItemStackEntry : recordHolder.entrySet()) {
            BlockPos blockPos = blockPosItemStackEntry.getKey();
            if(recordHolder.get(blockPos)!=null && !recordHolder.get(blockPos).isEmpty() && recordHolder.get(blockPos).getItem() instanceof BlankRecord) {
                tickCounter.put(blockPos,tickCounter.get(blockPos)+1);
                if(randomNum+tickCounter.get(blockPos)>=6000) {
                    recordWorld.get(blockPos).playSound(null,blockPos, new SoundEvent(new ResourceLocation("minecraft","item.trident.thunder")), SoundCategory.MASTER,1F,1F);
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
