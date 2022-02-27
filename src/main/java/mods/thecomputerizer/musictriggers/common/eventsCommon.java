package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.objects.BlankRecord;
import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import mods.thecomputerizer.musictriggers.util.packets.packetCurSong;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Mod.EventBusSubscriber(modid= MusicTriggers.MODID)
public class eventsCommon {

    public static HashMap<BlockPos, Integer> tickCounter = new HashMap<>();
    public static HashMap<BlockPos, ItemStack> recordHolder = new HashMap<>();
    public static HashMap<BlockPos, UUID> recordUUID = new HashMap<>();
    public static HashMap<BlockPos, World> recordWorld = new HashMap<>();

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent e) {
        for (Map.Entry<Integer, Map<EntityLiving, Integer>> integerMapEntry : calculateFeatures.victoryMobs.entrySet()) {
            Map<EntityLiving, Integer> tempMap = calculateFeatures.victoryMobs.get(integerMapEntry.getKey());
            for (Map.Entry<EntityLiving, Integer> entityLivingIntegerEntry : tempMap.entrySet()) {
                int temp = calculateFeatures.victoryMobs.get(integerMapEntry.getKey()).get(entityLivingIntegerEntry.getKey());
                if(temp>0) {
                    calculateFeatures.victoryMobs.get(integerMapEntry.getKey()).put(entityLivingIntegerEntry.getKey(), temp-1);
                }
                else {
                    calculateFeatures.victoryMobs.put(integerMapEntry.getKey(), new HashMap<>());
                }
            }
        }
        for (Map.Entry<Integer, Map<BossInfoServer, Integer>> integerMapEntry : calculateFeatures.victoryBosses.entrySet()) {
            Map<BossInfoServer, Integer> tempMap = calculateFeatures.victoryBosses.get(integerMapEntry.getKey());
            for (Map.Entry<BossInfoServer, Integer> bossInfoServerIntegerEntry : tempMap.entrySet()) {
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
