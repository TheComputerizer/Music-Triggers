package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.common.objects.BlankRecord;
import mods.thecomputerizer.musictriggers.common.objects.MusicRecorder;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.util.CalculateFeatures;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Mod.EventBusSubscriber(modid= Constants.MODID)
public class EventsCommon {

    public static HashMap<UUID, HashMap<String, String>> currentChannelSongs = new HashMap<>();
    public static HashMap<BlockPos, Integer> tickCounter = new HashMap<>();
    public static HashMap<BlockPos, ItemStack> recordHolder = new HashMap<>();
    public static HashMap<BlockPos, UUID> recordUUID = new HashMap<>();
    public static HashMap<BlockPos, World> recordWorld = new HashMap<>();
    public static HashMap<UUID, List<String>> recordMenu = new HashMap<>();
    public static HashMap<UUID, HashMap<String, List<String>>> activeTriggerList = new HashMap<>();

    public static int bossTimer = 0;
    private static final Random random = new Random();

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent e) {
        if(bossTimer>1) bossTimer-=1;
        else if(bossTimer==1) {
            CalculateFeatures.bossInfo = new HashMap<>();
            bossTimer-=1;
        }
        for (String trigger : CalculateFeatures.victoryMobs.keySet()) {
            if(!CalculateFeatures.allTriggers.contains(trigger)) {
                Map<EntityLiving, Integer> tempMap = CalculateFeatures.victoryMobs.get(trigger);
                for (EntityLiving en : tempMap.keySet()) {
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
                    EntityLightningBolt lightning = new EntityLightningBolt(recordWorld.get(blockPos), blockPos.getX(), blockPos.getY(), blockPos.getZ(), true);
                    recordWorld.get(blockPos).spawnEntity(lightning);
                    tickCounter.put(blockPos, 0);
                    String randomMenuSong = "theSongWill_NOTbetHisVALUE";
                    String currentChannel = "theChannelWill_NOTbetHisVALUE";
                    if (recordMenu.get(recordUUID.get(blockPos)) != null) {
                        randomMenuSong = recordMenu.get(recordUUID.get(blockPos)).get(random.nextInt(recordMenu.get(recordUUID.get(blockPos)).size()));
                        Object[] values = currentChannelSongs.get(recordUUID.get(blockPos)).keySet().toArray();
                        currentChannel = (String) values[random.nextInt(currentChannelSongs.get(recordUUID.get(blockPos)).keySet().size())];
                    }
                    if (recordHolder.get(blockPos).getItem() instanceof BlankRecord) {
                        ItemStack stack = MusicTriggersItems.MUSIC_TRIGGERS_RECORD.getDefaultInstance();
                        NBTTagCompound tag = new NBTTagCompound();
                        tag.setString("channelFrom",currentChannel);
                        tag.setString("trackID",currentChannelSongs.get(recordUUID.get(blockPos)).get(currentChannel));
                        tag.setString("triggerID",activeTriggerList.get(recordUUID.get(blockPos)).get(currentChannel).get(random.nextInt(activeTriggerList.get(recordUUID.get(blockPos)).get(currentChannel).size())));
                        stack.setTagCompound(tag);
                        recordHolder.put(blockPos, stack);
                    } else if (recordMenu.get(recordUUID.get(blockPos)) != null && !recordMenu.get(recordUUID.get(blockPos)).isEmpty() && recordWorld.get(blockPos).getBlockState(blockPos).getValue(MusicRecorder.HAS_DISC)) {
                        ItemStack stack = MusicTriggersItems.MUSIC_TRIGGERS_RECORD.getDefaultInstance();
                        NBTTagCompound tag = new NBTTagCompound();
                        tag.setString("channelFrom",currentChannel);
                        tag.setString("trackID",randomMenuSong);
                        tag.setString("triggerID","menu");
                        stack.setTagCompound(tag);
                        recordHolder.put(blockPos, stack);
                    }
                }
            }
            else tickCounter.put(blockPos,0);
        }
    }
}
