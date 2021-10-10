package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.MusicPicker;
import mods.thecomputerizer.musictriggers.configTitleCards;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Vector4f;

@Mod.EventBusSubscriber(modid= MusicTriggers.MODID)
public class eventsCommon {
    public static ResourceLocation IMAGE_CARD = null;
    public static ISound vanilla;
    public static boolean isWorldRendered;
    public static float fadeCount = 1000;
    public static float startDelayCount = 0;
    public static Boolean activated = false;
    public static int timer=0;

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

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void clientDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
        MusicPicker.mc.getSoundHandler().stopSounds();
        isWorldRendered=false;
        MusicPicker.player=null;
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void imageCards(RenderGameOverlayEvent.Post e) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if(e.getType()== RenderGameOverlayEvent.ElementType.ALL) {
            ScaledResolution res = e.getResolution();
            if (player != null) {
                int x = res.getScaledWidth();
                int y = res.getScaledHeight();
                Vector4f color = new Vector4f(1, 1, 1, 1);
                if(timer>200){
                    activated=false;
                    timer=0;
                }
                if (activated) {
                    timer++;
                    startDelayCount++;
                    if(startDelayCount>0) {
                        if (fadeCount > 1) {
                            fadeCount -= 15;
                            if (fadeCount < 1) {
                                fadeCount = 1;
                            }
                        }
                    }
                } else {
                    if (fadeCount < 1000) {
                        fadeCount += 12;
                        if (fadeCount > 1000) {
                            fadeCount = 1000;
                        }
                    }
                    startDelayCount=0;
                }
                if (fadeCount != 1000) {
                    float opacity = (int) (17 - (fadeCount / 80));
                    opacity = (opacity * 1.15f) / 15;
                    GlStateManager.enableBlend();
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(0, 0, 0);
                    GlStateManager.scale(0.140625*configTitleCards.ImageSize,0.25*configTitleCards.ImageSize,1);
                    GlStateManager.color(color.getX(), color.getY(), color.getZ(), Math.max(0, Math.min(0.95f, opacity)));
                    mc.getTextureManager().bindTexture(IMAGE_CARD);
                    GuiScreen.drawModalRectWithCustomSizedTexture((3*(x+configTitleCards.ImageH)),(y+configTitleCards.ImageV)/4,x,y,x,y,x,y);
                    GlStateManager.color(1F, 1F, 1F, 1);
                    GlStateManager.popMatrix();
                }
            }
        }
    }
}
