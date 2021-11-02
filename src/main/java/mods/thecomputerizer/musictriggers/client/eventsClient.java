package mods.thecomputerizer.musictriggers.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.configDebug;
import mods.thecomputerizer.musictriggers.configTitleCards;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import javax.vecmath.Vector4f;

@Mod.EventBusSubscriber(modid= MusicTriggers.MODID)
public class eventsClient {
    public static ResourceLocation IMAGE_CARD = null;
    public static boolean isWorldRendered;
    public static float fadeCount = 1000;
    public static float startDelayCount = 0;
    public static Boolean activated = false;
    public static int timer=0;
    public static EntityPlayer playerHurt;
    public static EntityPlayer playerSource;

    @SubscribeEvent
    public static void playSound(PlaySoundEvent e) {
        for(String s : configDebug.blockedmods) {
            if(e.getSound().getSoundLocation().toString().contains(s) && e.getSound().getCategory()==SoundCategory.MUSIC) {
                e.setResultSound(null);
            }
        }
        if(e.getSound().getSoundLocation().toString().contains("minecraft") && e.getSound().getCategory()==SoundCategory.MUSIC) {
            e.setResultSound(null);
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent e) {
        if (e.getEntity() instanceof EntityPlayer && e.getSource().getTrueSource() instanceof EntityPlayer) {
            playerHurt = (EntityPlayer) e.getEntity();
            playerSource = (EntityPlayer) e.getSource().getTrueSource();
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

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void debugInfo(RenderGameOverlayEvent.Text e) {
        if(configDebug.ShowDebugInfo && isWorldRendered) {
            if(MusicPlayer.curTrack!=null) {
                e.getLeft().add("Music Triggers Current song: " + MusicPlayer.curTrack);
            }
            if(!configDebug.ShowJustCurSong) {
                if(MusicPicker.playableList!=null && !MusicPicker.playableList.isEmpty()) {
                    StringBuilder s = new StringBuilder();
                    for (String ev : MusicPicker.playableList) {
                        s.append(" ").append(ev);
                    }
                    e.getLeft().add("Music Triggers Playable Events:" + s);
                }
                StringBuilder sm = new StringBuilder();
                sm.append("minecraft");
                for (String ev : configDebug.blockedmods) {
                    sm.append(" ").append(ev);
                }
                e.getLeft().add("Music Triggers Current Blocked Mods: " + sm);
                if(MusicPicker.player!=null && MusicPicker.world!=null) {
                    e.getLeft().add("Music Triggers Current Biome: " + MusicPicker.world.getBiome(MusicPicker.player.getPosition()).getRegistryName());
                    e.getLeft().add("Music Triggers Current Dimension: " + MusicPicker.player.dimension);
                    e.getLeft().add("Music Triggers Current Light Level: " + MusicPicker.world.getLight(MusicPicker.roundedPos(MusicPicker.player)));
                }
                if(MusicPicker.effectList!=null && !MusicPicker.effectList.isEmpty()) {
                    StringBuilder se = new StringBuilder();
                    for (String ev : MusicPicker.effectList) {
                        se.append(" ").append(ev);
                    }
                    e.getLeft().add("Music Triggers Current Effect List:" + se);
                }
                try {
                    if(infernalChecker(getLivingFromEntity(Minecraft.getMinecraft().objectMouseOver.entityHit))!=null) {
                        e.getLeft().add("Music Triggers Infernal Mob Mod Name: " + infernalChecker(getLivingFromEntity(Minecraft.getMinecraft().objectMouseOver.entityHit)));
                    }
                } catch (NoSuchMethodError ignored) {}
            }
        }
    }

    @Optional.Method(modid = "infernalmobs")
    private static String infernalChecker(@Nullable EntityLiving m) {
        if(m==null) {
            return null;
        }
        return InfernalMobsCore.getMobModifiers(m)==null ? null : InfernalMobsCore.getMobModifiers(m).getModName();
    }

    private static EntityLiving getLivingFromEntity(Entity e) {
        if(e instanceof EntityLiving) {
            return (EntityLiving) e;
        }
        else return null;
    }
}
