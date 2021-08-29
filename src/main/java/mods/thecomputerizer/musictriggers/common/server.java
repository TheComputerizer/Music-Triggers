package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

@Mod.EventBusSubscriber(modid= MusicTriggers.MODID)
public class server {
    public static MinecraftServer mcs;

    @SubscribeEvent
    public static void serverTick(FMLNetworkEvent.ServerConnectionFromClientEvent e) {
    }
}
