package mods.thecomputerizer.musictriggers.client;

import com.mojang.blaze3d.systems.RenderSystem;
import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.client.gui.GuiMain;
import mods.thecomputerizer.musictriggers.client.gui.GuiTriggerInfo;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.musictriggers.config.ConfigObject;
import mods.thecomputerizer.musictriggers.config.ConfigTitleCards;
import mods.thecomputerizer.musictriggers.mixin.BossBarHudAccessor;
import mods.thecomputerizer.musictriggers.mixin.InGameHudAccessor;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.packets.BossInfo;
import net.minecraft.advancement.Advancement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

import java.util.*;

public class EventsClient {

    public static Identifier IMAGE_CARD = null;
    public static int curImageIndex;
    public static float fadeCount = 1000;
    public static float startDelayCount = 0;
    public static Boolean activated = false;
    public static int timer = 0;
    public static int GuiCounter = 0;
    public static int reloadCounter = 0;
    public static boolean ismoving;
    public static List<Identifier> pngs = new ArrayList<>();
    public static int movingcounter = 0;
    public static String lastAdvancement;
    public static boolean advancement;
    public static PlayerEntity PVPTracker;
    public static boolean renderDebug = true;
    public static boolean zone = false;
    public static boolean firstPass = false;
    public static GuiTriggerInfo parentScreen = null;
    public static int x1 = 0;
    public static int y1 = 0;
    public static int z1 = 0;
    public static int x2 = 0;
    public static int y2 = 0;
    public static int z2 = 0;
    private static int bossBarCounter = 0;

    public static SoundInstance playSound(SoundInstance sound) {
        if (sound!=null) {
            for (String s : ConfigDebug.blockedmods) {
                if (sound.getId().getNamespace().contains(s) && sound.getCategory() == SoundCategory.MUSIC && !(MusicPlayer.curMusic == null && ConfigDebug.SilenceIsBad))
                    return new PositionedSoundInstance(sound.getId(), SoundCategory.MUSIC, Float.MIN_VALUE*1000, 1F, false, 0, SoundInstance.AttenuationType.LINEAR, 0,0,0, false);
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

    public static void onDisconnect() {
        MusicPicker.mc.getSoundManager().stopAll();
        MusicPlayer.fadingOut = false;
        MusicPlayer.fadingIn = false;
        MusicPlayer.linkedFadingIn = new HashMap<>();
        MusicPlayer.linkedFadingOut = new HashMap<>();
        MusicPlayer.curMusic = null;
        MusicPlayer.fadeOutList = null;
    }

    public static void onCustomTick() {
        if(MusicPlayer.curMusic!=null
                && MinecraftClient.getInstance().getSoundManager().soundSystem.sources.get(MusicPlayer.curMusic)!=null
                && MinecraftClient.getInstance().getSoundManager().soundSystem.sources.get(MusicPlayer.curMusic).source!=null
                && Objects.requireNonNull(MinecraftClient.getInstance().getSoundManager().soundSystem.sources.get(MusicPlayer.curMusic).source).getSourceState()!=0x1013)
            MusicPlayer.curMusicTimer+=20;
        if(ConfigTitleCards.imagecards.get(curImageIndex)!=null) {
            if (timer > ConfigTitleCards.imagecards.get(curImageIndex).getTime()) {
                activated = false;
                timer = 0;
                ismoving = false;
                movingcounter = 0;
            }
            if (ismoving) {
                if (timer % ConfigTitleCards.imagecards.get(curImageIndex).getDelay() == 0) {
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
                        fadeCount -= ConfigTitleCards.imagecards.get(curImageIndex).getFadeIn();
                        if (fadeCount < 1) {
                            fadeCount = 1;
                        }
                    }
                }
            } else {
                if (fadeCount < 1000) {
                    fadeCount += ConfigTitleCards.imagecards.get(curImageIndex).getFadeOut();
                    if (fadeCount > 1000) {
                        fadeCount = 1000;
                        ismoving = false;
                    }
                }
                startDelayCount = 0;
            }
        }
    }

    public static void imageCards(MatrixStack matrix) {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (player != null && ConfigTitleCards.imagecards.get(curImageIndex) != null) {
            int x = mc.getWindow().getScaledWidth();
            int y = mc.getWindow().getScaledHeight();
            if (fadeCount != 1000 && IMAGE_CARD != null) {
                matrix.push();

                float opacity = (int) (17 - (fadeCount / 80));
                opacity = (opacity * 1.15f) / 15;

                float scaleY = 0.25f*(ConfigTitleCards.imagecards.get(curImageIndex).getScaleY()/100f);
                float scaleX = (0.25f*((float)y/(float)x))*(ConfigTitleCards.imagecards.get(curImageIndex).getScaleX()/100f);
                matrix.scale(scaleX, scaleY, 1f);

                float posY = (y*(1f/scaleY))/8f+(ConfigTitleCards.imagecards.get(curImageIndex).getHorizontal()*(1/scaleY));
                float posX = ((x*(1f/scaleX))/2f)-(x/2f)+(ConfigTitleCards.imagecards.get(curImageIndex).getHorizontal()*(1/scaleX));

                MusicTriggersCommon.logger.info(IMAGE_CARD + " X: " + x + " Y: " + y + " PosX: " + posX + " PosY: " + posY);

                RenderSystem.setShaderColor(1F, 1F, 1F, Math.max(0, Math.min(0.95f, opacity)));
                RenderSystem.setShaderTexture(0, IMAGE_CARD);
                DrawableHelper.drawTexture(matrix, (int) posX, (int) posY, 0F, 0F, x, y, x, y);
                matrix.pop();
            }
        }
    }


    public static void onKeyInput() {
        if(MinecraftClient.getInstance().player!=null) {
            BlockPos pos = MusicPicker.roundedPos(MinecraftClient.getInstance().player);
            if(!zone) MinecraftClient.getInstance().setScreen(new GuiMain(ConfigObject.createFromCurrent()));
            else if(!firstPass) {
                x1 = pos.getX();
                y1 = pos.getY();
                z1 = pos.getZ();
                firstPass = true;
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ambient(SoundEvents.BLOCK_ANVIL_LAND));
            } else {
                x2 = pos.getX();
                y2 = pos.getY();
                z2 = pos.getZ();
                int temp;
                if(x1>x2) {
                    temp=x1;
                    x1=x2;
                    x2=temp;
                }
                if(y1>y2) {
                    temp=y1;
                    y1=y2;
                    y2=temp;
                }
                if(z1>z2) {
                    temp=z1;
                    z1=z2;
                    z2=temp;
                }
                firstPass = false;
                zone = false;
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ambient(SoundEvents.BLOCK_ANVIL_BREAK));
                String compiledZoneCoords = x1+","+y1+","+z1+","+x2+","+y2+","+z2;
                parentScreen.holder.editTriggerInfoParameter(parentScreen.songCode, parentScreen.trigger, parentScreen.scrollingSongs.index, compiledZoneCoords);
                MinecraftClient.getInstance().setScreen(parentScreen);
            }
        }
    }

    public static void onTick() {
        if(!MinecraftClient.getInstance().isPaused() && !renderDebug) renderDebug = true;
        MinecraftClient.getInstance().options.hudHidden = !renderDebug;
        if(reloadCounter>0) {
            reloadCounter-=1;
            if(reloadCounter==1) {
                Reload.readAndReload();
                Text msg = Text.of("\u00A7a\u00A7oFinished!");
                MusicPicker.player.sendMessage(msg,false);
                IMAGE_CARD = null;
                fadeCount = 1000;
                timer = 0;
                activated = false;
                ismoving = false;
                MusicPlayer.fadingIn=false;
                MusicPlayer.fadingOut = false;
                MusicPlayer.curMusic = null;
                MusicPlayer.curTrack = null;
                MusicPlayer.curTrackList = null;
                MusicPlayer.cards = true;
                MusicPlayer.reloading = false;
                MusicPlayer.fadeOutList = null;
            }
        }
    }

    public static void debugInfo(MatrixStack matrix) {
        if (ConfigDebug.ShowDebugInfo && renderDebug) {
            List<String> left = new ArrayList<>();
            if (MusicPlayer.curTrack != null) left.add("Music Triggers Current song: "+ MusicPlayer.curTrackHolder);
            if (!ConfigDebug.ShowJustCurSong) {
                int displayCount = 0;
                if(!MusicPlayer.formatSongTime().matches("No song playing")) left.add("Music Triggers Current Song Time: " + MusicPlayer.formatSongTime());
                if(MusicPlayer.fadingOut) left.add("Music Triggers Fading Out: "+ MusicPlayer.formattedTimeFromMilliseconds(MusicPlayer.tempFadeOut*50));
                if(MusicPlayer.fadingIn) left.add("Music Triggers Fading In: "+ MusicPlayer.formattedTimeFromMilliseconds(MusicPlayer.tempFadeIn*50));
                if(MusicPicker.playableList!=null && !MusicPicker.playableList.isEmpty()) {
                    StringBuilder s = new StringBuilder();
                    for (String ev : MusicPicker.playableList) {
                        if(MinecraftClient.getInstance().textRenderer.getWidth(s+" "+ev)>0.75f*MinecraftClient.getInstance().getWindow().getScaledWidth()) {
                            if(displayCount==0) {
                                left.add("Music Triggers Playable Events: " + s);
                                displayCount++;
                            } else left.add(s.toString());
                            s = new StringBuilder();
                        }
                        s.append(" ").append(ev);
                    }
                    if(displayCount==0) left.add("Music Triggers Playable Events: " + s);
                    else left.add(s.toString());
                }
                displayCount=0;
                StringBuilder sm = new StringBuilder();
                sm.append("minecraft");
                for (String ev : ConfigDebug.blockedmods) {
                    if(MinecraftClient.getInstance().textRenderer.getWidth(sm+" "+ev)>0.75f*MinecraftClient.getInstance().getWindow().getScaledWidth()) {
                        if(displayCount==0) {
                            left.add("Music Triggers Blocked Mods: " + sm);
                            displayCount++;
                        } else left.add(sm.toString());
                        sm = new StringBuilder();
                    }
                    sm.append(" ").append(ev);
                }
                if(displayCount==0) left.add("Music Triggers Blocked Mods: " + sm);
                else left.add(sm.toString());
                displayCount=0;
                if (MusicPicker.player != null && MusicPicker.world != null) {
                    if (FromServer.curStruct != null) left.add("Music Triggers Current Structure: "+ FromServer.curStruct);
                    left.add("Music Triggers Current Biome: "+ FromServer.curBiome);
                    left.add("Music Triggers Current Dimension: "+MusicPicker.player.world.getDimension().getEffects());
                    left.add("Music Triggers Current Total Light: "+MusicPicker.world.getLightLevel(MusicPicker.roundedPos(MusicPicker.player)));
                    left.add("Music Triggers Current Block Light: "+MusicPicker.world.getLightLevel(LightType.BLOCK, MusicPicker.roundedPos(MusicPicker.player)));
                    if (MusicPicker.effectList != null && !MusicPicker.effectList.isEmpty()) {
                        StringBuilder se = new StringBuilder();
                        for (String ev : MusicPicker.effectList) {
                            if(MinecraftClient.getInstance().textRenderer.getWidth(se+" "+ev)>0.75f*MinecraftClient.getInstance().getWindow().getScaledWidth()) {
                                if(displayCount==0) {
                                    left.add("Music Triggers Effect List: " + se);
                                    displayCount++;
                                } else left.add(se.toString());
                                se = new StringBuilder();
                            }
                            se.append(" ").append(ev);
                        }
                        if(displayCount==0) left.add("Music Triggers Effect List: " + se);
                        else left.add(se.toString());
                    }
                    if (getLivingFromEntity(MinecraftClient.getInstance().targetedEntity) != null) left.add("Music Triggers Current Entity Name: "+getLivingFromEntity(MinecraftClient.getInstance().targetedEntity).getName().getString());
                    if (MusicPicker.mc.currentScreen != null) left.add("Music Triggers current GUI: "+MusicPicker.mc.currentScreen);
                }
            }
            int top = 2;
            for (String msg : left)
            {
                DrawableHelper.fill(matrix, 1, top - 1, 2 + MinecraftClient.getInstance().textRenderer.getWidth(msg) + 1, top + MinecraftClient.getInstance().textRenderer.fontHeight - 1, -1873784752);
                MinecraftClient.getInstance().textRenderer.draw(matrix, msg, 2, top, 14737632);
                top += MinecraftClient.getInstance().textRenderer.fontHeight;
            }
        }
    }

    public static void renderBoss() {
        if (bossBarCounter % 11 == 0) {
            Map<UUID, ClientBossBar> bossbars = ((BossBarHudAccessor) ((InGameHudAccessor) MinecraftClient.getInstance().inGameHud).getBossBarHud()).getBossBars();
            for (UUID u : bossbars.keySet()) {
                ClientBossBar bar = bossbars.get(u);
                PacketHandler.sendToServer(BossInfo.id, BossInfo.encode(bar.getName().getString(), bar.getPercent()));
            }
            bossBarCounter = 0;
        }
        bossBarCounter++;
    }

    private static LivingEntity getLivingFromEntity(Entity e) {
        if (e instanceof LivingEntity) return (LivingEntity) e;
        return null;
    }
}
