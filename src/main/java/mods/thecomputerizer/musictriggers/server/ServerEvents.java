package mods.thecomputerizer.musictriggers.server;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.server.channels.ServerTriggerStatus;
import mods.thecomputerizer.musictriggers.server.data.PersistentTriggerDataProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.mutable.MutableInt;

@Mod.EventBusSubscriber(modid=Constants.MODID)
public class ServerEvents {
    private static final MutableInt TIMER = new MutableInt();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if(e.phase==TickEvent.Phase.END) {
            if(TIMER.incrementAndGet()>=5) {
                ServerTriggerStatus.runServerChecks();
                TIMER.setValue(0);
            }
        }
    }

    @SubscribeEvent
    public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> e) {
        if(e.getObject() instanceof ServerPlayer)
            e.addCapability(Constants.res("trigger_data_capability"),new PersistentTriggerDataProvider());
    }

    @SubscribeEvent
    public static void onEntitySpawned(EntityJoinWorldEvent e) {
        Entity entity = e.getEntity();
        if(entity instanceof LivingEntity)
            ServerTriggerStatus.checkIfBossSpawned((LivingEntity)entity);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent e) {
        if(e.getSource().getEntity() instanceof ServerPlayer playerSource && e.getEntity() instanceof ServerPlayer)
            ServerTriggerStatus.setPVP(playerSource,e.getEntity().getStringUUID());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if(e.getPlayer() instanceof ServerPlayer player) {
            PersistentTriggerDataProvider.getPlayerCapability(player).onLogin(player);
        }
    }
}
