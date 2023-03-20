package mods.thecomputerizer.musictriggers.server;

import mods.thecomputerizer.musictriggers.Constants;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid= Constants.MODID)
public class ServerEvents {
    private static int TIMER = 0;

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent e) {
        if(e.phase==TickEvent.Phase.END) {
            TIMER++;
            if(TIMER>=5) {
                ServerData.runServerChecks();
                TIMER = 0;
            }
        }
    }
}
