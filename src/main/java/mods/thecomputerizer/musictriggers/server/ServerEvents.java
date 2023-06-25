package mods.thecomputerizer.musictriggers.server;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.server.channels.ServerTriggerStatus;
import mods.thecomputerizer.musictriggers.server.data.PersistentTriggerDataProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid=Constants.MODID)
public class ServerEvents {
    private static int TIMER = 0;

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent e) {
        if(e.phase==TickEvent.Phase.END) {
            TIMER++;
            if(TIMER>=5) {
                ServerTriggerStatus.runServerChecks();
                TIMER = 0;
            }
        }
    }

    @SubscribeEvent
    public static void livingDamage(LivingHurtEvent e) {
        if(e.getSource().getTrueSource() instanceof EntityPlayerMP && e.getEntity() instanceof EntityPlayerMP)
            ServerTriggerStatus.setPVP((EntityPlayerMP)e.getSource().getTrueSource(),e.getEntity().getUniqueID().toString());
    }

    @SubscribeEvent
    public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof EntityPlayerMP)
            event.addCapability(PersistentDataHandler.PERSISTANCE_TRIGGER_DATA, new PersistentTriggerDataProvider());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if(e.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)e.player;
            PersistentDataHandler.getDataCapability(player).onLogin(player);
        }
    }



    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        if(e.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)e.player;
        }
    }
}
