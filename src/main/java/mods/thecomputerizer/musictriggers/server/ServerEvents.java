package mods.thecomputerizer.musictriggers.server;

import mods.thecomputerizer.musictriggers.core.Constants;
import mods.thecomputerizer.musictriggers.server.channels.ServerTriggerStatus;
import mods.thecomputerizer.musictriggers.server.data.PersistentTriggerDataProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
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
        if(e.getObject() instanceof EntityPlayerMP)
            e.addCapability(PersistentDataHandler.PERSISTANCE_TRIGGER_DATA, new PersistentTriggerDataProvider());
    }

    @SubscribeEvent
    public static void onEntitySpawned(EntityJoinWorldEvent e) {
        Entity entity = e.getEntity();
        if(entity instanceof EntityLivingBase)
            ServerTriggerStatus.checkIfBossSpawned((EntityLivingBase)entity);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent e) {
        if(e.getSource().getTrueSource() instanceof EntityPlayerMP && e.getEntity() instanceof EntityPlayerMP)
            ServerTriggerStatus.setPVP((EntityPlayerMP)e.getSource().getTrueSource(),e.getEntity().getUniqueID().toString());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if(e.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)e.player;
            PersistentDataHandler.getDataCapability(player).onLogin(player);
        }
    }
}
