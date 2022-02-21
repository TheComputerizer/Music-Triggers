package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.objects.BlankRecord;
import mods.thecomputerizer.musictriggers.util.packetCurSong;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Mod.EventBusSubscriber(modid= MusicTriggers.MODID)
public class eventsCommon {

    public static HashMap<BlockPos, Integer> tickCounter = new HashMap<>();
    public static HashMap<BlockPos, ItemStack> recordHolder = new HashMap<>();
    public static HashMap<BlockPos, UUID> recordUUID = new HashMap<>();
    public static HashMap<BlockPos, World> recordWorld = new HashMap<>();

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent e) {
        for (Map.Entry<Integer, Map<EntityLiving, Integer>> integerMapEntry : calculateFeature.victoryMobs.entrySet()) {
            for (Map.Entry<EntityLiving, Integer> entityLivingIntegerEntry : calculateFeature.victoryMobs.get((integerMapEntry).getKey()).entrySet()) {
                int temp = calculateFeature.victoryMobs.get(integerMapEntry.getKey()).get(entityLivingIntegerEntry.getKey());
                if(temp>0) {
                    calculateFeature.victoryMobs.get(integerMapEntry.getKey()).put(entityLivingIntegerEntry.getKey(), temp-1);
                }
                else {
                    calculateFeature.victoryMobs.put(integerMapEntry.getKey(), new HashMap<>());
                }
            }
        }
        int randomNum = ThreadLocalRandom.current().nextInt(0, 5600);
        for (Map.Entry<BlockPos, ItemStack> blockPosItemStackEntry : recordHolder.entrySet()) {
            BlockPos blockPos = blockPosItemStackEntry.getKey();
            if(recordHolder.get(blockPos)!=null && !recordHolder.get(blockPos).isEmpty() && recordHolder.get(blockPos).getItem() instanceof BlankRecord) {
                tickCounter.put(blockPos,tickCounter.get(blockPos)+1);
                MusicTriggers.logger.info(tickCounter.get(blockPos)+" "+randomNum);
                if(randomNum+tickCounter.get(blockPos)>=6000) {
                    EntityLightningBolt lightning = new EntityLightningBolt(recordWorld.get(blockPos), blockPos.getX(),blockPos.getY(),blockPos.getZ(),true);
                    recordWorld.get(blockPos).spawnEntity(lightning);
                    tickCounter.put(blockPos,0);
                    for (Item i : MusicTriggersItems.allItems) {
                        String itemName = Objects.requireNonNull(i.getRegistryName()).toString().replaceAll("musictriggers:","");
                        if(itemName.matches(packetCurSong.curSong.get(recordUUID.get(blockPos)))) {
                            recordHolder.put(blockPos,i.getDefaultInstance());
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
