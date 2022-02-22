package mods.thecomputerizer.musictriggers.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import com.mojang.blaze3d.systems.RenderSystem;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.config.configDebug;
import mods.thecomputerizer.musictriggers.config.configTitleCards;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
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
    public static Player playerHurt;
    public static Player playerSource;
    public static int GuiCounter = 0;
    private static int reloadCounter = 0;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void playSound(PlaySoundEvent e) {
        if (e.getSound()!=null) {
            if ((MusicPlayer.curMusic != null || MusicPlayer.curTrackList == null || MusicPlayer.curTrackList.isEmpty()) && e.getSound().getLocation().getNamespace().matches(MusicTriggers.MODID) && (((MusicPlayer.curMusic!=null && e.getEngine().isActive(MusicPlayer.curMusic)) && e.getSound().getLocation() != MusicPlayer.fromRecord.getLocation()) || MusicPlayer.playing)) {
                e.setSound(new SimpleSoundInstance(e.getSound().getLocation(), SoundSource.MUSIC, 0F, 1F, false, 0, SoundInstance.Attenuation.LINEAR, 0,0,0, false));
            }
            for (String s : configDebug.blockedmods.get()) {
                if (e.getSound().getLocation().getNamespace().contains(s) && e.getSound().getSource() == SoundSource.MUSIC) {
                    if (!(MusicPlayer.curMusic == null && configDebug.SilenceIsBad.get())) {
                        e.setSound(new SimpleSoundInstance(e.getSound().getLocation(), SoundSource.MUSIC, 0F, 1F, false, 0, SoundInstance.Attenuation.LINEAR, 0,0,0, false));
                    }
                }
            }
            if (e.getSound().getLocation().getNamespace().contains("minecraft") && e.getSound().getSource() == SoundSource.MUSIC) {
                if (!(MusicPlayer.curMusic == null && configDebug.SilenceIsBad.get())) {
                    e.setSound(new SimpleSoundInstance(e.getSound().getLocation(), SoundSource.MUSIC, 0F, 1F, false, 0, SoundInstance.Attenuation.LINEAR, 0,0,0, false));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent e) {
        if (e.getEntity() instanceof Player && e.getSource().getEntity() instanceof Player) {
            playerHurt = (Player) e.getEntity();
            playerSource = (Player) e.getSource().getEntity();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void worldRender(RenderLevelLastEvent e) {
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
        Player player = mc.player;
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
                    e.getMatrixStack().pushPose();

                    float opacity = (int) (17 - (fadeCount / 80));
                    opacity = (opacity * 1.15f) / 15;

                    float sizeX = 64 * configTitleCards.ImageSize;
                    float sizeY = 64 * configTitleCards.ImageSize;
                    int posY = (y / 64) + configTitleCards.ImageV;
                    int posX = ((x / 2) - (int) (sizeX / 2)) + configTitleCards.ImageH;

                    RenderSystem.setShaderColor(1F, 1F, 1F, Math.max(0, Math.min(0.95f, opacity)));
                    mc.getTextureManager().bindForSetup(IMAGE_CARD);
                    GuiComponent.blit(e.getMatrixStack(), posX, posY, 10, 0F, 0F, (int) sizeX, (int) sizeY, (int) sizeX, (int) sizeY);

                    e.getMatrixStack().popPose();
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent e) {
        if(MusicPlayer.RELOAD.isDown()) {
            Minecraft.getInstance().getSoundManager().stop();
            TextComponent msg = new TextComponent("\u00A74\u00A7oReloading Music... This may take a while!");
            MusicPicker.player.sendMessage(msg,MusicPicker.player.getUUID());
            MusicPlayer.reloading = true;
            reloadCounter = 5;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if(reloadCounter>0) {
            reloadCounter-=1;
            if(reloadCounter==1) {
                reload.readAndReload();
                TextComponent msg = new TextComponent("\u00A7a\u00A7oFinished!");
                MusicPicker.player.sendMessage(msg,MusicPicker.player.getUUID());
                MusicPlayer.reloading = false;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void debugInfo(RenderGameOverlayEvent.Text e) {
        if (configDebug.ShowDebugInfo.get() && isWorldRendered) {
            if (MusicPlayer.curTrack != null) {
                e.getLeft().add("Music Triggers Current song: " + MusicPlayer.curTrackHolder);
            }
            if (!configDebug.ShowJustCurSong.get()) {
                if (MusicPicker.playableList != null && !MusicPicker.playableList.isEmpty()) {
                    StringBuilder s = new StringBuilder();
                    for (String ev : MusicPicker.playableList) {
                        s.append(" ").append(ev);
                    }
                    e.getLeft().add("Music Triggers Playable Triggers:" + s);
                }
                StringBuilder sm = new StringBuilder();
                sm.append("minecraft");
                for (String ev : configDebug.blockedmods.get()) {
                    sm.append(" ").append(ev);
                }
                e.getLeft().add("Music Triggers Current Blocked Mods: " + sm);
                if (MusicPicker.player != null && MusicPicker.world != null) {
                    if (fromServer.curStruct != null) {
                        e.getLeft().add("Music Triggers Current Structure: " + fromServer.curStruct);
                    }
                    e.getLeft().add("Music Triggers Current Biome: " + fromServer.curBiome);
                    e.getLeft().add("Music Triggers Current Dimension: " + MusicPicker.player.level.dimension().location());
                    e.getLeft().add("Music Triggers Current Total Light: " + MusicPicker.world.getRawBrightness(MusicPicker.roundedPos(MusicPicker.player), 0));
                    e.getLeft().add("Music Triggers Current Block Light: " + MusicPicker.world.getBrightness(LightLayer.BLOCK, MusicPicker.roundedPos(MusicPicker.player)));
                    if (MusicPicker.effectList != null && !MusicPicker.effectList.isEmpty()) {
                        StringBuilder se = new StringBuilder();
                        for (String ev : MusicPicker.effectList) {
                            se.append(" ").append(ev);
                        }
                        e.getLeft().add("Music Triggers Current Effect List:" + se);
                    }
                    if (getLivingFromEntity(Minecraft.getInstance().crosshairPickEntity) != null) {
                        e.getLeft().add("Music Triggers Current Entity Name: " + getLivingFromEntity(Minecraft.getInstance().crosshairPickEntity).getName().getString());
                    }
                    if (MusicPicker.mc.screen != null) {
                        e.getLeft().add("Music Triggers current GUI: " + MusicPicker.mc.screen.toString());
                    }
                    try {
                        if (infernalChecker(getLivingFromEntity(Minecraft.getInstance().crosshairPickEntity)) != null) {
                            e.getLeft().add("Music Triggers Infernal Mob Mod Name: " + infernalChecker(getLivingFromEntity(Minecraft.getInstance().crosshairPickEntity)));
                        }
                    } catch (NoSuchMethodError ignored) {
                    }
                }
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
        if (e instanceof LivingEntity) {
            return (LivingEntity) e;
        } else return null;
    }
}
