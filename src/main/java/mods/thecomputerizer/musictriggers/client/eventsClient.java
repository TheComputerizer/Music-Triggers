package mods.thecomputerizer.musictriggers.client;

import com.mojang.blaze3d.systems.RenderSystem;
import mods.thecomputerizer.musictriggers.configTitleCards;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class eventsClient {

    public static ResourceLocation IMAGE_CARD = null;
    public static ISound vanilla;
    public static boolean isWorldRendered;
    public static float fadeCount = 1000;
    public static float startDelayCount = 0;
    public static Boolean activated = false;
    public static int timer = 0;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void playSound(PlaySoundEvent e) {
        if (e.getSound().getLocation().toString().contains("minecraft:music")) {
            vanilla = e.getSound();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void worldRender(RenderWorldLastEvent e) {
        isWorldRendered = true;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void clientDisconnected(PlayerEvent.PlayerLoggedOutEvent e) {
        MusicPicker.mc.getSoundManager().stop();
        isWorldRendered = false;
        MusicPicker.player = null;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void imageCards(RenderGameOverlayEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        if (e.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            if (player != null) {
                int x = mc.getWindow().getGuiScaledWidth();
                int y = mc.getWindow().getScreenHeight();
                if (timer > 200) {
                    activated = false;
                    timer = 0;
                }
                if (activated) {
                    timer++;
                    startDelayCount++;
                    if (startDelayCount > 0) {
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
                    startDelayCount = 0;
                }
                if (fadeCount != 1000) {
                    RenderSystem.pushMatrix();

                    float opacity = (int) (17 - (fadeCount / 80));
                    opacity = (opacity * 1.15f) / 15;

                    float sizeX = 64*configTitleCards.ImageSize;
                    float sizeY = 64*configTitleCards.ImageSize;

                    int posY = (y/64)+configTitleCards.ImageV;
                    int posX = ((x/2)-(int)(sizeX/2))+ configTitleCards.ImageH;

                    RenderSystem.pushTextureAttributes();

                    RenderSystem.enableAlphaTest();
                    RenderSystem.enableBlend();
                    RenderSystem.color4f(1F, 1F, 1F, Math.max(0, Math.min(0.95f, opacity)));
                    mc.getTextureManager().bind(IMAGE_CARD);
                    AbstractGui.blit(e.getMatrixStack(),posX, posY, 10, 0F, 0F, (int)sizeX, (int)sizeY, (int)sizeX, (int)sizeY);

                    RenderSystem.popAttributes();

                    RenderSystem.popMatrix();
                }
            }
        }
    }
}
