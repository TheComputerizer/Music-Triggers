package mods.thecomputerizer.musictriggers.server;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.server.data.ServerChannels;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid=Constants.MODID)
public class ServerEvents {
    private static int TIMER = 0;

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent e) {
        if(e.phase==TickEvent.Phase.END) {
            TIMER++;
            if(TIMER>=5) {
                ServerChannels.runServerChecks();
                TIMER = 0;
            }
        }
    }

    @SubscribeEvent
    public static void livingDamage(LivingHurtEvent e) {
        if(e.getSource().getTrueSource() instanceof EntityPlayerMP && e.getEntity() instanceof EntityPlayerMP)
            ServerChannels.setPVP((EntityPlayerMP)e.getSource().getTrueSource(),e.getEntity().getUniqueID().toString());
    }
}
