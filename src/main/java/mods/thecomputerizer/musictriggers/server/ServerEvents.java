package mods.thecomputerizer.musictriggers.server;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.server.data.ServerChannels;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid= Constants.MODID)
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
        if(e.getSource().getEntity() instanceof ServerPlayer && e.getEntity() instanceof ServerPlayer)
            ServerChannels.setPVP((ServerPlayer)e.getSource().getEntity(),e.getEntity().getUUID().toString());
    }
}
