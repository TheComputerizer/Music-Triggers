package mods.thecomputerizer.musictriggers.client;

import com.mojang.blaze3d.systems.RenderSystem;
import mods.thecomputerizer.musictriggers.MusicTriggersClient;
import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.config.configDebug;
import mods.thecomputerizer.musictriggers.config.configTitleCards;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.advancement.Advancement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.LightType;

import java.util.ArrayList;
import java.util.List;

public class eventsClient {

    public static Identifier IMAGE_CARD = null;
    public static int curImageIndex;
    public static boolean isWorldRendered;
    public static float fadeCount = 1000;
    public static float startDelayCount = 0;
    public static Boolean activated = false;
    public static int timer = 0;
    public static int GuiCounter = 0;
    private static int reloadCounter = 0;
    public static boolean ismoving;
    public static List<Identifier> pngs = new ArrayList<>();
    public static int movingcounter = 0;
    public static String lastAdvancement;
    public static boolean advancement;
    public static PlayerEntity PVPTracker;

    public static SoundInstance playSound(SoundInstance sound) {
        if (sound!=null) {
            if ((MusicPlayer.curMusic != null || MusicPlayer.curTrackList == null || MusicPlayer.curTrackList.isEmpty()) && sound.getId().getNamespace().matches(MusicTriggersCommon.MODID) && (((MusicPlayer.curMusic!=null && MinecraftClient.getInstance().getSoundManager().isPlaying(MusicPlayer.curMusic)) && sound.getId() != MusicPlayer.fromRecord.getId()) || MusicPlayer.playing)) {
                MinecraftClient.getInstance().getSoundManager().stop(sound);
                return new PositionedSoundInstance(sound.getId(), SoundCategory.MUSIC, Float.MIN_VALUE*1000, 1F, false, 0, SoundInstance.AttenuationType.LINEAR, 0,0,0, false);
            }
            for (String s : configDebug.modList) {
                if (sound.getId().getNamespace().contains(s) && sound.getCategory() == SoundCategory.MUSIC) {
                    if (!(MusicPlayer.curMusic == null && configDebug.silenceIsBad)) {
                        MinecraftClient.getInstance().getSoundManager().stop(sound);
                        return new PositionedSoundInstance(sound.getId(), SoundCategory.MUSIC, Float.MIN_VALUE*1000, 1F, false, 0, SoundInstance.AttenuationType.LINEAR, 0,0,0, false);
                    }
                }
            }
            if (sound.getId().getNamespace().contains("minecraft") && sound.getCategory() == SoundCategory.MUSIC) {
                if (!(MusicPlayer.curMusic == null && configDebug.silenceIsBad)) {
                    MinecraftClient.getInstance().getSoundManager().stop(sound);
                    return new PositionedSoundInstance(sound.getId(), SoundCategory.MUSIC, Float.MIN_VALUE*1000, 1F, false, 0, SoundInstance.AttenuationType.LINEAR, 0,0,0, false);
                }
            }
        }
        return sound;
    }

    public static void onDamage(LivingEntity e, DamageSource d) {
        if(e instanceof PlayerEntity && d.getAttacker() instanceof PlayerEntity) {
            if (e == MusicPicker.player) {
                PVPTracker = (PlayerEntity) d.getAttacker();
                MusicPicker.setPVP = true;
            }
            else if(d.getAttacker() == MusicPicker.player) {
                PVPTracker = (PlayerEntity)e;
                MusicPicker.setPVP = true;
            }
        }
    }

    public static void onAdvancement(Advancement a) {
        lastAdvancement = a.getId().toString();
        advancement = true;
    }

    public static void onCustomTick() {
        if(configTitleCards.imagecards.get(curImageIndex)!=null) {
            if (timer > configTitleCards.imagecards.get(curImageIndex).getTime()) {
                activated = false;
                timer = 0;
                ismoving = false;
                movingcounter = 0;
            }
            if (ismoving) {
                if (timer % configTitleCards.imagecards.get(curImageIndex).getDelay() == 0) {
                    movingcounter++;
                    if (movingcounter >= pngs.size()) {
                        movingcounter = 0;
                    }
                }
                IMAGE_CARD = pngs.get(movingcounter);
            }
            if (activated) {
                timer++;
                startDelayCount++;
                if (startDelayCount > 0) {
                    if (fadeCount > 1) {
                        fadeCount -= configTitleCards.imagecards.get(curImageIndex).getFadeIn();
                        if (fadeCount < 1) {
                            fadeCount = 1;
                        }
                    }
                }
            } else {
                if (fadeCount < 1000) {
                    fadeCount += configTitleCards.imagecards.get(curImageIndex).getFadeOut();
                    if (fadeCount > 1000) {
                        fadeCount = 1000;
                        ismoving = false;
                    }
                }
                startDelayCount = 0;
            }
        }
    }

    public static void imageCards(WorldRenderContext context) {
        isWorldRendered = true;
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (player != null && configTitleCards.imagecards.get(curImageIndex) != null) {
            int x = mc.getWindow().getWidth();
            int y = mc.getWindow().getHeight();
            if (fadeCount != 1000 && IMAGE_CARD != null) {
                context.matrixStack().push();

                float opacity = (int) (17 - (fadeCount / 80));
                opacity = (opacity * 1.15f) / 15;

                int sizeX = configTitleCards.imageDimensions.get(IMAGE_CARD).getWidth();
                int sizeY = configTitleCards.imageDimensions.get(IMAGE_CARD).getHeight();

                float scaleY = (0.25f * (configTitleCards.imagecards.get(curImageIndex).getScaleY() / 100f));
                float scaleX = (0.25f * (configTitleCards.imagecards.get(curImageIndex).getScaleX() / 100f));
                context.matrixStack().scale(scaleX, scaleY, 1f);

                float posY = ((y * 2f) / ((4f * (configTitleCards.imagecards.get(curImageIndex).getScaleY() / 100f)) * 3f)) + configTitleCards.imagecards.get(curImageIndex).getVertical();
                float posX = ((x * 2f) / ((configTitleCards.imagecards.get(curImageIndex).getScaleX() / 100f) * 3f)) - (sizeX / 2f) + configTitleCards.imagecards.get(curImageIndex).getHorizontal();

                MusicTriggersCommon.logger.info(IMAGE_CARD + " X: " + x + " Y: " + y + " PosX: " + posX + " PosY: " + posY);

                RenderSystem.setShaderColor(1F, 1F, 1F, Math.max(0, Math.min(0.95f, opacity)));
                RenderSystem.setShaderTexture(0, IMAGE_CARD);
                DrawableHelper.drawTexture(context.matrixStack(), (int) posX, (int) posY, 0F, 0F, sizeX, sizeY, sizeX, sizeY);
                context.matrixStack().pop();
            }
        }
    }


    public static void onKeyInput() {
        if(MusicTriggersClient.RELOAD.wasPressed()) {
            MinecraftClient.getInstance().getSoundManager().stopAll();
            Text msg = Text.of("\u00A74\u00A7oReloading Music... This may take a while!");
            MusicPicker.player.sendMessage(msg,true);
            MusicPlayer.reloading = true;
            reloadCounter = 5;
        }
    }

    public static void onTick() {
        if(reloadCounter>0) {
            reloadCounter-=1;
            if(reloadCounter==1) {
                reload.readAndReload();
                Text msg = Text.of("\u00A7a\u00A7oFinished!");
                MusicPicker.player.sendMessage(msg,true);
                IMAGE_CARD = null;
                fadeCount = 1000;
                timer = 0;
                activated = false;
                ismoving = false;
                MusicPlayer.cards = true;
                MusicPlayer.reloading = false;
            }
        }
    }

    public static void debugInfo() {
        if (configDebug.showDebug && isWorldRendered) {
            StringBuilder debug = new StringBuilder();
            if (MusicPlayer.curTrack != null) {
                debug.append("Music Triggers Current song: ").append(MusicPlayer.curTrackHolder).append("\n");
            }
            if (!configDebug.showJustCurSong) {
                if (MusicPicker.playableList != null && !MusicPicker.playableList.isEmpty()) {
                    StringBuilder s = new StringBuilder();
                    for (String ev : MusicPicker.playableList) {
                        s.append(" ").append(ev);
                    }
                    debug.append("Music Triggers Playable Triggers:").append(s).append("\n");
                }
                StringBuilder sm = new StringBuilder();
                sm.append("minecraft");
                for (String ev : configDebug.modList) {
                    sm.append(" ").append(ev);
                }
                debug.append("Music Triggers Current Blocked Mods: ").append(sm).append("\n");
                if (MusicPicker.player != null && MusicPicker.world != null) {
                    if (fromServer.curStruct != null) {
                        debug.append("Music Triggers Current Structure: ").append(fromServer.curStruct).append("\n");
                    }
                    debug.append("Music Triggers Current Biome: ").append(fromServer.curBiome).append("\n");
                    debug.append("Music Triggers Current Dimension: ").append(MusicPicker.player.world.getDimension().getEffects()).append("\n");
                    debug.append("Music Triggers Current Total Light: ").append(MusicPicker.world.getLightLevel(MusicPicker.roundedPos(MusicPicker.player))).append("\n");
                    debug.append("Music Triggers Current Block Light: ").append(MusicPicker.world.getLightLevel(LightType.BLOCK, MusicPicker.roundedPos(MusicPicker.player))).append("\n");
                    if (MusicPicker.effectList != null && !MusicPicker.effectList.isEmpty()) {
                        StringBuilder se = new StringBuilder();
                        for (String ev : MusicPicker.effectList) {
                            se.append(" ").append(ev);
                        }
                        debug.append("Music Triggers Current Effect List:").append(se).append("\n");
                    }
                    if (getLivingFromEntity(MinecraftClient.getInstance().targetedEntity) != null) {
                        debug.append("Music Triggers Current Entity Name: ").append(getLivingFromEntity(MinecraftClient.getInstance().targetedEntity).getName().getString()).append("\n");
                    }
                    if (MusicPicker.mc.currentScreen != null) {
                        debug.append("Music Triggers current GUI: ").append(MusicPicker.mc.currentScreen.toString()).append("\n");
                    }
                }
            }
            DebugRenderer.drawString(debug.toString(), 0, 0, 0, -1, 0.01f, false, 0.0f, true);
        }
    }

    private static LivingEntity getLivingFromEntity(Entity e) {
        if (e instanceof LivingEntity) {
            return (LivingEntity) e;
        } else return null;
    }
}
