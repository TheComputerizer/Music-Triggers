package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.common.objects.BlankRecord;
import mods.thecomputerizer.musictriggers.common.objects.MusicRecorder;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.util.CalculateFeatures;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Mod.EventBusSubscriber(modid= Constants.MODID)
public class EventsCommon {

    public static HashMap<UUID, HashMap<String, String>> currentChannelSongs = new HashMap<>();
    public static HashMap<BlockPos, Integer> tickCounter = new HashMap<>();
    public static HashMap<BlockPos, ItemStack> recordHolder = new HashMap<>();
    public static HashMap<BlockPos, UUID> recordUUID = new HashMap<>();
    public static HashMap<BlockPos, Boolean> recordIsCustom = new HashMap<>();
    public static HashMap<BlockPos, World> recordWorld = new HashMap<>();
    public static HashMap<UUID, List<String>> recordMenu = new HashMap<>();
    public static HashMap<UUID, HashMap<String, List<String>>> activeTriggerList = new HashMap<>();

    public static HashMap<UUID, MutableInt> bossTimers = new HashMap<>();
    private static final Random random = new Random();

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        TriggerCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent e) {
        if(e.phase==TickEvent.Phase.END) {
            for (UUID playerUUID : bossTimers.keySet()) {
                bossTimers.get(playerUUID).decrement();
                if (bossTimers.get(playerUUID).getValue() <= 0 || playerIsNotLoggedIn(playerUUID))
                    CalculateFeatures.perPlayerBossInfo.get(playerUUID).clear();
            }
            bossTimers.entrySet().removeIf(entry -> entry.getValue().getValue() <= 0 || playerIsNotLoggedIn(entry.getKey()));
            for (String trigger : CalculateFeatures.victoryMobs.keySet()) {
                if (!CalculateFeatures.allTriggers.contains(trigger)) {
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
                if (recordHolder.get(blockPos) != null && !recordHolder.get(blockPos).isEmpty() && (recordHolder.get(blockPos).getItem() instanceof BlankRecord || (recordHolder.get(blockPos).getItem() instanceof MusicTriggersRecord && recordWorld.get(blockPos).getBlockState(blockPos).getValue(MusicRecorder.HAS_DISC)))) {
                    tickCounter.put(blockPos, tickCounter.get(blockPos) + 1);
                    if (randomNum + tickCounter.get(blockPos) >= 6000) {
                        LightningBoltEntity lightning = EntityType.LIGHTNING_BOLT.create(recordWorld.get(blockPos));
                        if (lightning != null) {
                            lightning.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                            recordWorld.get(blockPos).addFreshEntity(lightning);
                        }
                        tickCounter.put(blockPos, 0);
                        String randomMenuSong = "theSongWill_NOTbetHisVALUE";
                        String currentChannel = "theChannelWill_NOTbetHisVALUE";
                        if (recordMenu.get(recordUUID.get(blockPos)) != null) {
                            randomMenuSong = recordMenu.get(recordUUID.get(blockPos)).get(random.nextInt(recordMenu.get(recordUUID.get(blockPos)).size()));
                            Object[] values = currentChannelSongs.get(recordUUID.get(blockPos)).keySet().toArray();
                            currentChannel = (String) values[random.nextInt(currentChannelSongs.get(recordUUID.get(blockPos)).keySet().size())];
                        }
                        if (recordHolder.get(blockPos).getItem() instanceof BlankRecord) {
                            ItemStack stack = MusicTriggersItems.MUSIC_TRIGGERS_RECORD.get().getDefaultInstance();

                            stack.getOrCreateTag().putString("channelFrom", currentChannel);
                            stack.getOrCreateTag().putString("trackID", currentChannelSongs.get(recordUUID.get(blockPos)).get(currentChannel));
                            stack.getOrCreateTag().putString("triggerID", activeTriggerList.get(recordUUID.get(blockPos)).get(currentChannel).get(random.nextInt(activeTriggerList.get(recordUUID.get(blockPos)).get(currentChannel).size())));
                            recordHolder.put(blockPos, stack);
                        } else if (recordMenu.get(recordUUID.get(blockPos)) != null && !recordMenu.get(recordUUID.get(blockPos)).isEmpty() && recordWorld.get(blockPos).getBlockState(blockPos).getValue(MusicRecorder.HAS_DISC)) {
                            ItemStack stack;
                            if (recordIsCustom.get(blockPos))
                                stack = MusicTriggersItems.CUSTOM_RECORD.get().getDefaultInstance();
                            else stack = MusicTriggersItems.MUSIC_TRIGGERS_RECORD.get().getDefaultInstance();
                            stack.getOrCreateTag().putString("channelFrom", currentChannel);
                            stack.getOrCreateTag().putString("trackID", randomMenuSong);
                            stack.getOrCreateTag().putString("triggerID", "menu");
                            recordHolder.put(blockPos, stack);
                        }
                    }
                } else tickCounter.put(blockPos, 0);
            }
        }
    }

    private static boolean playerIsNotLoggedIn(UUID playerUUID) {
        return Objects.isNull(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID));
    }
}
