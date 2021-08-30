package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Sound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid= MusicTriggers.MODID)
public class server {
    public static MinecraftServer mcs;
    public static ISound vanilla;
    public static boolean isWorldRendered;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void playSound(PlaySoundEvent e) {
        if(e.getSound().getSoundLocation().toString().contains("minecraft:music")) {
            vanilla = e.getSound();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void worldRender(RenderWorldLastEvent e) {
        isWorldRendered=true;
    }
}
