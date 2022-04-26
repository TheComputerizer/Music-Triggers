package mods.thecomputerizer.musictriggers.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import com.mojang.blaze3d.systems.RenderSystem;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.gui.GuiMain;
import mods.thecomputerizer.musictriggers.client.gui.GuiTriggerInfo;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.musictriggers.config.ConfigObject;
import mods.thecomputerizer.musictriggers.config.ConfigTitleCards;
import mods.thecomputerizer.musictriggers.util.CustomTick;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.packets.BossInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class EventsClient {

    public static ResourceLocation IMAGE_CARD = null;
    public static int curImageIndex;
    public static boolean isWorldRendered;
    public static float fadeCount = 1000;
    public static float startDelayCount = 0;
    public static Boolean activated = false;
    public static int timer = 0;
    public static int GuiCounter = 0;
    public static int reloadCounter = 0;
    public static boolean ismoving;
    public static List<ResourceLocation> pngs = new ArrayList<>();
    public static int movingcounter = 0;
    public static String lastAdvancement;
    public static boolean advancement;
    public static Player PVPTracker;
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

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void playSound(PlaySoundEvent e) {
        if (e.getSound()!=null) {
            if ((MusicPlayer.curMusic != null || MusicPlayer.curTrackList == null || MusicPlayer.curTrackList.isEmpty()) && e.getSound().getLocation().getNamespace().matches(MusicTriggers.MODID) && (((MusicPlayer.curMusic!=null && e.getEngine().isActive(MusicPlayer.curMusic)) && e.getSound().getLocation() != MusicPlayer.fromRecord.getLocation()) || MusicPlayer.playing)) {
                e.setSound(new SimpleSoundInstance(e.getSound().getLocation(), SoundSource.MUSIC, Float.MIN_VALUE, 1F, false, 0, SoundInstance.Attenuation.LINEAR, 0,0,0, false));
            }
            for (String s : ConfigDebug.blockedmods) {
                if (e.getSound().getLocation().getNamespace().contains(s) && e.getSound().getSource() == SoundSource.MUSIC) {
                    if (!(MusicPlayer.curMusic == null && ConfigDebug.SilenceIsBad)) {
                        e.setSound(new SimpleSoundInstance(e.getSound().getLocation(), SoundSource.MUSIC, Float.MIN_VALUE, 1F, false, 0, SoundInstance.Attenuation.LINEAR, 0,0,0, false));
                    }
                }
            }
            if (e.getSound().getLocation().getNamespace().contains("minecraft") && e.getSound().getSource() == SoundSource.MUSIC) {
                if (!(MusicPlayer.curMusic == null && ConfigDebug.SilenceIsBad)) {
                    e.setSound(new SimpleSoundInstance(e.getSound().getLocation(), SoundSource.MUSIC, Float.MIN_VALUE, 1F, false, 0, SoundInstance.Attenuation.LINEAR, 0,0,0, false));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent e) {
        if(e.getEntityLiving() instanceof Player && e.getSource().getEntity() instanceof Player) {
            if (e.getEntityLiving() == MusicPicker.player) {
                PVPTracker = (Player) e.getSource().getEntity();
                MusicPicker.setPVP = true;
            }
            else if(e.getSource().getEntity() == MusicPicker.player) {
                PVPTracker = (Player)e.getEntityLiving();
                MusicPicker.setPVP = true;
            }
        }
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent e) {
        lastAdvancement = e.getAdvancement().getId().toString();
        advancement = true;
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
        MusicPlayer.fadingOut = false;
        MusicPlayer.fadingIn = false;
        MusicPlayer.linkedFadingIn = new HashMap<>();
        MusicPlayer.linkedFadingOut = new HashMap<>();
        MusicPlayer.curMusic = null;
        MusicPlayer.fadeOutList = null;
        isWorldRendered = false;
        MusicPicker.player = null;
    }

    @SubscribeEvent
    public static void cancelRenders(RenderGameOverlayEvent.Pre e) {
        if(e.getType()==RenderGameOverlayEvent.ElementType.ALL && !renderDebug) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void customTick(CustomTick ev) {
        if(MusicPlayer.curMusic!=null
                && Minecraft.getInstance().getSoundManager().soundEngine.instanceToChannel.get(MusicPlayer.curMusic)!=null
                && Minecraft.getInstance().getSoundManager().soundEngine.instanceToChannel.get(MusicPlayer.curMusic).channel!=null
                && Objects.requireNonNull(Minecraft.getInstance().getSoundManager().soundEngine.instanceToChannel.get(MusicPlayer.curMusic).channel).getState()!=0x1013)
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

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void imageCards(RenderGameOverlayEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (e.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            if (player != null && ConfigTitleCards.imagecards.get(curImageIndex)!=null) {
                int x = mc.getWindow().getScreenWidth();
                int y = mc.getWindow().getScreenHeight();
                if (fadeCount != 1000 && IMAGE_CARD!=null) {
                    e.getMatrixStack().pushPose();

                    float opacity = (int) (17 - (fadeCount / 80));
                    opacity = (opacity * 1.15f) / 15;

                    int sizeX = ConfigTitleCards.imageDimensions.get(IMAGE_CARD).getWidth();
                    int sizeY = ConfigTitleCards.imageDimensions.get(IMAGE_CARD).getHeight();

                    float scaleY = (0.25f*(ConfigTitleCards.imagecards.get(curImageIndex).getScaleY()/100f));
                    float scaleX = (0.25f*(ConfigTitleCards.imagecards.get(curImageIndex).getScaleX()/100f));
                    e.getMatrixStack().scale(scaleX,scaleY,1f);

                    float posY = ((y*2f)/((4f*(ConfigTitleCards.imagecards.get(curImageIndex).getScaleY()/100f))*3f))+ ConfigTitleCards.imagecards.get(curImageIndex).getVertical();
                    float posX = ((x*2f)/((ConfigTitleCards.imagecards.get(curImageIndex).getScaleX()/100f)*3f))-(sizeX/2f)+ ConfigTitleCards.imagecards.get(curImageIndex).getHorizontal();

                    RenderSystem.setShaderColor(1F, 1F, 1F, Math.max(0, Math.min(0.95f, opacity)));
                    RenderSystem.setShaderTexture(0, IMAGE_CARD);
                    mc.gui.blit(e.getMatrixStack(), (int)posX, (int)posY, 0F, 0F, sizeX, sizeY, sizeX, sizeY);

                    e.getMatrixStack().popPose();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent e) {
        if(MusicPlayer.RELOAD.isDown() && Minecraft.getInstance().player!=null) {
            BlockPos pos = MusicPicker.roundedPos(Minecraft.getInstance().player);
            if(!zone) Minecraft.getInstance().setScreen(new GuiMain(ConfigObject.createFromCurrent()));
            else if(!firstPass) {
                x1 = pos.getX();
                y1 = pos.getY();
                z1 = pos.getZ();
                firstPass = true;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_LAND, 1f));
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
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_BREAK, 1f));
                String compiledZoneCoords = x1+","+y1+","+z1+","+x2+","+y2+","+z2;
                parentScreen.holder.editTriggerInfoParameter(parentScreen.songCode, parentScreen.trigger, parentScreen.scrollingSongs.index, compiledZoneCoords);
                Minecraft.getInstance().setScreen(parentScreen);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if(reloadCounter>0) {
            reloadCounter-=1;
            if(reloadCounter==1) {
                Reload.readAndReload();
                TextComponent msg = new TextComponent("\u00A7a\u00A7oFinished!");
                MusicPicker.player.sendMessage(msg,MusicPicker.player.getUUID());
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

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void debugInfo(RenderGameOverlayEvent.Text e) {
        if (ConfigDebug.ShowDebugInfo && isWorldRendered) {
            if (MusicPlayer.curTrack != null) {
                e.getLeft().add("Music Triggers Current song: " + MusicPlayer.curTrackHolder);
            }
            if (!ConfigDebug.ShowJustCurSong) {
                int displayCount = 0;
                if(!MusicPlayer.formatSongTime().matches("No song playing")) e.getLeft().add("Music Triggers Current Song Time: " + MusicPlayer.formatSongTime());
                if(MusicPlayer.fadingOut) e.getLeft().add("Music Triggers Fading Out: "+MusicPlayer.formattedTimeFromMilliseconds(MusicPlayer.tempFadeOut*50));
                if(MusicPlayer.fadingIn) e.getLeft().add("Music Triggers Fading In: "+MusicPlayer.formattedTimeFromMilliseconds(MusicPlayer.tempFadeIn*50));
                if(MusicPicker.playableList!=null && !MusicPicker.playableList.isEmpty()) {
                    StringBuilder s = new StringBuilder();
                    for (String ev : MusicPicker.playableList) {
                        if(Minecraft.getInstance().font.width(s+" "+ev)>0.75f*Minecraft.getInstance().getWindow().getScreenWidth()) {
                            if(displayCount==0) {
                                e.getLeft().add("Music Triggers Playable Events: " + s);
                                displayCount++;
                            } else e.getLeft().add(s.toString());
                            s = new StringBuilder();
                        }
                        s.append(" ").append(ev);
                    }
                    if(displayCount==0) {
                        e.getLeft().add("Music Triggers Playable Events: " + s);
                    } else e.getLeft().add(s.toString());
                }
                displayCount=0;
                StringBuilder sm = new StringBuilder();
                sm.append("minecraft");
                for (String ev : ConfigDebug.blockedmods) {
                    if(Minecraft.getInstance().font.width(sm+" "+ev)>0.75f*Minecraft.getInstance().getWindow().getScreenWidth()) {
                        if(displayCount==0) {
                            e.getLeft().add("Music Triggers Blocked Mods: " + sm);
                            displayCount++;
                        } else e.getLeft().add(sm.toString());
                        sm = new StringBuilder();
                    }
                    sm.append(" ").append(ev);
                }
                if(displayCount==0) {
                    e.getLeft().add("Music Triggers Blocked Mods: " + sm);
                } else e.getLeft().add(sm.toString());
                displayCount=0;
                if (MusicPicker.player != null && MusicPicker.world != null) {
                    if (FromServer.curStruct != null) {
                        e.getLeft().add("Music Triggers Current Structure: " + FromServer.curStruct);
                    }
                    e.getLeft().add("Music Triggers Current Biome: " + FromServer.curBiome);
                    e.getLeft().add("Music Triggers Current Dimension: " + MusicPicker.player.level.dimension().location());
                    e.getLeft().add("Music Triggers Current Total Light: " + MusicPicker.world.getRawBrightness(MusicPicker.roundedPos(MusicPicker.player), 0));
                    e.getLeft().add("Music Triggers Current Block Light: " + MusicPicker.world.getBrightness(LightLayer.BLOCK, MusicPicker.roundedPos(MusicPicker.player)));
                    if (MusicPicker.effectList != null && !MusicPicker.effectList.isEmpty()) {
                        StringBuilder se = new StringBuilder();
                        for (String ev : MusicPicker.effectList) {
                            if(Minecraft.getInstance().font.width(se+" "+ev)>0.75f*Minecraft.getInstance().getWindow().getScreenWidth()) {
                                if(displayCount==0) {
                                    e.getLeft().add("Music Triggers Effect List: " + se);
                                    displayCount++;
                                } else e.getLeft().add(se.toString());
                                se = new StringBuilder();
                            }
                            se.append(" ").append(ev);
                        }
                        if(displayCount==0) {
                            e.getLeft().add("Music Triggers Effect List: " + se);
                        } else e.getLeft().add(se.toString());
                    }
                    if (MusicPicker.mc.screen != null) {
                        e.getLeft().add("Music Triggers current GUI: " + MusicPicker.mc.screen.toString());
                    }
                    if (getLivingFromEntity(Minecraft.getInstance().crosshairPickEntity) != null) {
                        e.getLeft().add("Music Triggers Current Entity Name: " + getLivingFromEntity(Minecraft.getInstance().crosshairPickEntity).getName().getString());
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

    @SubscribeEvent
    public static void renderBoss(RenderGameOverlayEvent.BossInfo e) {
        if (bossBarCounter % 11 == 0) {
            PacketHandler.sendToServer(new BossInfo(e.getBossEvent().getName().getString(), e.getBossEvent().getProgress()));
            bossBarCounter = 0;
        }
        bossBarCounter++;
    }

    private static String infernalChecker(@Nullable LivingEntity m) {
        if (ModList.get().isLoaded("infernalmobs") && m != null)
            return InfernalMobsCore.getMobModifiers(m) == null ? null : InfernalMobsCore.getMobModifiers(m).getModName();
        return null;
    }

    private static LivingEntity getLivingFromEntity(Entity e) {
        if (e instanceof LivingEntity) return (LivingEntity) e;
        return null;
    }
}
