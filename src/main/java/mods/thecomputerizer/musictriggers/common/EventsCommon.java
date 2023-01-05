package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.common.objects.BlankRecord;
import mods.thecomputerizer.musictriggers.common.objects.MusicRecorder;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.util.CalculateFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EventsCommon {

    public static HashMap<UUID, HashMap<String, String>> currentChannelSongs = new HashMap<>();
    public static HashMap<BlockPos, Integer> tickCounter = new HashMap<>();
    public static HashMap<BlockPos, ItemStack> recordHolder = new HashMap<>();
    public static HashMap<BlockPos, UUID> recordUUID = new HashMap<>();
    public static HashMap<BlockPos, Boolean> recordIsCustom = new HashMap<>();
    public static HashMap<BlockPos, Level> recordWorld = new HashMap<>();
    public static HashMap<UUID, List<String>> recordMenu = new HashMap<>();
    public static HashMap<UUID, HashMap<String, List<String>>> activeTriggerList = new HashMap<>();
    public static HashMap<UUID, MutableInt> bossTimers = new HashMap<>();
    private static final Random random = new Random();

    public static void onServerTick() {
        for (UUID playerUUID : bossTimers.keySet()) {
            bossTimers.get(playerUUID).decrement();
            if (bossTimers.get(playerUUID).getValue() <= 0)
                CalculateFeatures.perPlayerBossInfo.get(playerUUID).clear();
        }
        bossTimers.entrySet().removeIf(entry -> entry.getValue().getValue()<=0);
        for (String trigger : CalculateFeatures.victoryMobs.keySet()) {
            if(!CalculateFeatures.allTriggers.contains(trigger)) {
                Map<LivingEntity, Integer> tempMap = CalculateFeatures.victoryMobs.get(trigger);
                for (LivingEntity en : tempMap.keySet()) {
                    int temp = tempMap.get(en);
                    if (temp > 0) CalculateFeatures.victoryMobs.get(trigger).put(en, temp - 1);
                    else CalculateFeatures.victoryMobs.put(trigger, new HashMap<>());
                }
            }
        }
        int randomNum = ThreadLocalRandom.current().nextInt(0, 5600);
        for (Map.Entry<BlockPos, ItemStack> blockPosItemStackEntry : recordHolder.entrySet()) {
            BlockPos blockPos = blockPosItemStackEntry.getKey();
            if(recordHolder.get(blockPos)!=null && !recordHolder.get(blockPos).isEmpty() && (recordHolder.get(blockPos).getItem() instanceof BlankRecord || (recordHolder.get(blockPos).getItem() instanceof MusicTriggersRecord && recordWorld.get(blockPos).getBlockState(blockPos).getValue(MusicRecorder.HAS_DISC)))) {
                tickCounter.put(blockPos,tickCounter.get(blockPos)+1);
                if(randomNum+tickCounter.get(blockPos)>=6000) {
                    LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(recordWorld.get(blockPos));
                    if(lightning!=null) {
                        lightning.setPosRaw(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                        recordWorld.get(blockPos).addFreshEntity(lightning);
                    }
                    tickCounter.put(blockPos, 0);
                    String randomMenuSong = "theSongWill_NOTbetHisVALUE";
                    if(recordMenu.get(recordUUID.get(blockPos))!=null) randomMenuSong = recordMenu.get(recordUUID.get(blockPos)).get(new Random().nextInt(recordMenu.get(recordUUID.get(blockPos)).size()));
                    String currentChannel = "theChannelWill_NOTbetHisVALUE";
                    if (recordMenu.get(recordUUID.get(blockPos)) != null) {
                        randomMenuSong = recordMenu.get(recordUUID.get(blockPos)).get(random.nextInt(recordMenu.get(recordUUID.get(blockPos)).size()));
                        Object[] values = currentChannelSongs.get(recordUUID.get(blockPos)).keySet().toArray();
                        currentChannel = (String) values[random.nextInt(currentChannelSongs.get(recordUUID.get(blockPos)).keySet().size())];
                    }
                    if (recordHolder.get(blockPos).getItem() instanceof BlankRecord) {
                        ItemStack stack = MusicTriggersItems.MUSIC_TRIGGERS_RECORD.getDefaultInstance();
                        stack.getOrCreateTag().putString("channelFrom",currentChannel);
                        stack.getOrCreateTag().putString("trackID",currentChannelSongs.get(recordUUID.get(blockPos)).get(currentChannel));
                        stack.getOrCreateTag().putString("triggerID",activeTriggerList.get(recordUUID.get(blockPos)).get(currentChannel).get(random.nextInt(activeTriggerList.get(recordUUID.get(blockPos)).get(currentChannel).size())));
                        recordHolder.put(blockPos, stack);
                    } else if (recordMenu.get(recordUUID.get(blockPos)) != null && !recordMenu.get(recordUUID.get(blockPos)).isEmpty() && recordWorld.get(blockPos).getBlockState(blockPos).getValue(MusicRecorder.HAS_DISC)) {
                        ItemStack stack;
                        if (recordIsCustom.get(blockPos))
                            stack = MusicTriggersItems.CUSTOM_RECORD.getDefaultInstance();
                        else stack = MusicTriggersItems.MUSIC_TRIGGERS_RECORD.getDefaultInstance();
                        stack.getOrCreateTag().putString("channelFrom",currentChannel);
                        stack.getOrCreateTag().putString("trackID",randomMenuSong);
                        stack.getOrCreateTag().putString("triggerID","menu");
                        recordHolder.put(blockPos, stack);
                    }
                }
            }
            else tickCounter.put(blockPos,0);
        }
    }
}
