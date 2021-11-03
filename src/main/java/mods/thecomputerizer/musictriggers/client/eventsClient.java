package mods.thecomputerizer.musictriggers.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import com.mojang.blaze3d.systems.RenderSystem;
import mods.thecomputerizer.musictriggers.configDebug;
import mods.thecomputerizer.musictriggers.configTitleCards;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;

public class eventsClient {

    public static ResourceLocation IMAGE_CARD = null;
    public static boolean isWorldRendered;
    public static float fadeCount = 1000;
    public static float startDelayCount = 0;
    public static Boolean activated = false;
    public static int timer = 0;
    public static PlayerEntity playerHurt;
    public static PlayerEntity playerSource;
    public static String curStruct;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void playSound(PlaySoundEvent e) {
        for(String s : configDebug.blockedmods.get()) {
            if(e.getSound().getLocation().toString().contains(s) && e.getSound().getSource()== SoundCategory.MUSIC) {
                e.setResultSound(null);
            }
        }
        if(e.getSound().getLocation().toString().contains("minecraft") && e.getSound().getSource()==SoundCategory.MUSIC) {
            e.setResultSound(null);
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent e) {
        if (e.getEntity() instanceof PlayerEntity && e.getSource().getEntity() instanceof PlayerEntity) {
            playerHurt = (PlayerEntity) e.getEntity();
            playerSource = (PlayerEntity) e.getSource().getEntity();
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

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void debugInfo(RenderGameOverlayEvent.Text e) {
        if(configDebug.ShowDebugInfo.get() && isWorldRendered) {
            if(MusicPlayer.curTrack!=null) {
                e.getLeft().add("Music Triggers Current song: " + MusicPlayer.curTrack);
            }
            if(!configDebug.ShowJustCurSong.get()) {
                if(MusicPicker.playableList!=null && !MusicPicker.playableList.isEmpty()) {
                    StringBuilder s = new StringBuilder();
                    for (String ev : MusicPicker.playableList) {
                        s.append(" ").append(ev);
                    }
                    e.getLeft().add("Music Triggers Playable Events:" + s);
                }
                StringBuilder sm = new StringBuilder();
                sm.append("minecraft");
                for (String ev : configDebug.blockedmods.get()) {
                    sm.append(" ").append(ev);
                }
                e.getLeft().add("Music Triggers Current Blocked Mods: " + sm);
                if(MusicPicker.player!=null && MusicPicker.world!=null) {
                    if(curStruct!=null) {
                        e.getLeft().add("Music Triggers Current Structure: " +curStruct);
                    }
                    e.getLeft().add("Music Triggers Current Biome: " + MusicPicker.world.getBiome(MusicPicker.roundedPos(MusicPicker.player)).getRegistryName());
                    e.getLeft().add("Music Triggers Current Dimension: " + MusicPicker.player.level.dimension().location());
                    e.getLeft().add("Music Triggers Current Total Light: " + MusicPicker.world.getRawBrightness(MusicPicker.roundedPos(MusicPicker.player), 0));
                    e.getLeft().add("Music Triggers Current Block Light: " + MusicPicker.world.getBrightness(LightType.BLOCK,MusicPicker.roundedPos(MusicPicker.player)));
                }
                if(MusicPicker.effectList!=null && !MusicPicker.effectList.isEmpty()) {
                    StringBuilder se = new StringBuilder();
                    for (String ev : MusicPicker.effectList) {
                        se.append(" ").append(ev);
                    }
                    e.getLeft().add("Music Triggers Current Effect List:" + se);
                }
                try {
                    if(infernalChecker(getLivingFromEntity(Minecraft.getInstance().crosshairPickEntity))!=null) {
                        e.getLeft().add("Music Triggers Infernal Mob Mod Name: " + infernalChecker(getLivingFromEntity(Minecraft.getInstance().crosshairPickEntity)));
                    }
                } catch (NoSuchMethodError ignored) {}
            }
        }
    }

    private static String infernalChecker(@Nullable LivingEntity m) {
        if (ModList.get().isLoaded("infernalmobs")) {
            if (m == null) {
                return null;
            }
            return InfernalMobsCore.getMobModifiers(m) == null ? null : InfernalMobsCore.getMobModifiers(m).getModName();
        }
        return null;
    }

    private static LivingEntity getLivingFromEntity(Entity e) {
        if(e instanceof LivingEntity) {
            return (LivingEntity) e;
        }
        else return null;
    }
}
