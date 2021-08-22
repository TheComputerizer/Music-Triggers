package mods.thecomputerizer.musictriggers.common;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.MusicPicker;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid= MusicTriggers.MODID)
public class CommonEvents {

    //@SideOnly(Side.CLIENT)
    //@SubscribeEvent
    //public static void worldLoad(WorldEvent.Load event) {
        //MusicPicker.world = event.getWorld();
    //}

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        MusicPicker.player = event.player;
        MusicPicker.world = event.player.getEntityWorld();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void clientDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        MusicPicker.player = null;
        MusicPicker.world = null;
    }
}