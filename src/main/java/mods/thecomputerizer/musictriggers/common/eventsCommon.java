package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.common.objects.BlankRecord;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import mods.thecomputerizer.musictriggers.util.packets.CurSong;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
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
        for (Map.Entry<Integer, Map<LivingEntity, Integer>> integerMapEntry : calculateFeatures.victoryMobs.entrySet()) {
            Map<LivingEntity, Integer> tempMap = calculateFeatures.victoryMobs.get(integerMapEntry.getKey());
            for (Map.Entry<LivingEntity, Integer> entityLivingIntegerEntry : tempMap.entrySet()) {
                int temp = calculateFeatures.victoryMobs.get(integerMapEntry.getKey()).get(entityLivingIntegerEntry.getKey());
                if(temp>0) {
                    calculateFeatures.victoryMobs.get(integerMapEntry.getKey()).put(entityLivingIntegerEntry.getKey(), temp-1);
                }
                else {
                    calculateFeatures.victoryMobs.put(integerMapEntry.getKey(), new HashMap<>());
                }
            }
        }
        for (Map.Entry<Integer, Map<ServerBossEvent, Integer>> integerMapEntry : calculateFeatures.victoryBosses.entrySet()) {
            Map<ServerBossEvent, Integer> tempMap = calculateFeatures.victoryBosses.get(integerMapEntry.getKey());
            for (Map.Entry<ServerBossEvent, Integer> bossInfoServerIntegerEntry : tempMap.entrySet()) {
                int temp = calculateFeatures.victoryBosses.get(integerMapEntry.getKey()).get(bossInfoServerIntegerEntry.getKey());
                if(temp>0) {
                    calculateFeatures.victoryBosses.get(integerMapEntry.getKey()).put(bossInfoServerIntegerEntry.getKey(), temp-1);
                }
                else {
                    calculateFeatures.victoryBosses.put(integerMapEntry.getKey(), new HashMap<>());
                }
            }
        }
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
