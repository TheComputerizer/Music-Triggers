package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.objects.BlankRecord;
import mods.thecomputerizer.musictriggers.common.objects.MusicRecorder;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import mods.thecomputerizer.musictriggers.util.packets.packetCurSong;
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
    public static HashMap<UUID, List<String>> recordMenu = new HashMap<>();

    public static int bossTimer = 0;

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent e) {
        if(bossTimer>1) bossTimer-=1;
        else if(bossTimer==1) {
            calculateFeatures.bossInfo = new HashMap<>();
            bossTimer-=1;
        }
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
            if(recordHolder.get(blockPos)!=null && !recordHolder.get(blockPos).isEmpty() && (recordHolder.get(blockPos).getItem() instanceof BlankRecord || (recordHolder.get(blockPos).getItem() instanceof MusicTriggersRecord && recordWorld.get(blockPos).getBlockState(blockPos).getValue(MusicRecorder.HAS_DISC)))) {
                tickCounter.put(blockPos,tickCounter.get(blockPos)+1);
                if(randomNum+tickCounter.get(blockPos)>=6000) {
                    EntityLightningBolt lightning = new EntityLightningBolt(recordWorld.get(blockPos), blockPos.getX(),blockPos.getY(),blockPos.getZ(),true);
                    recordWorld.get(blockPos).spawnEntity(lightning);
                    tickCounter.put(blockPos,0);
                    String randomMenuSong = "theSongWill_NOTbetHisVALUE";
                    if(recordMenu.get(recordUUID.get(blockPos))!=null) randomMenuSong = recordMenu.get(recordUUID.get(blockPos)).get(new Random().nextInt(recordMenu.get(recordUUID.get(blockPos)).size()));
                    for (Item i : MusicTriggersItems.allItems) {
                        if(recordHolder.get(blockPos).getItem() instanceof BlankRecord) {
                            String itemName = Objects.requireNonNull(i.getRegistryName()).toString().replaceAll("musictriggers:", "");
                            if (itemName.matches(packetCurSong.curSong.get(recordUUID.get(blockPos)))) recordHolder.put(blockPos, i.getDefaultInstance());

                        } else if(recordMenu.get(recordUUID.get(blockPos))!=null && !recordMenu.get(recordUUID.get(blockPos)).isEmpty() && recordWorld.get(blockPos).getBlockState(blockPos).getValue(MusicRecorder.HAS_DISC)) {
                            String itemName = Objects.requireNonNull(i.getRegistryName()).toString().replaceAll("musictriggers:", "");
                            if (itemName.matches(randomMenuSong)) recordHolder.put(blockPos, i.getDefaultInstance());

                        }
                    }
                }
            }
            else tickCounter.put(blockPos,0);
        }
    }
}
