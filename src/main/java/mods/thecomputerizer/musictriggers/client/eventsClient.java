package mods.thecomputerizer.musictriggers.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.client.gui.GuiTriggerInfo;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.packets.PacketBossInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Mod.EventBusSubscriber(modid = MusicTriggers.MODID, value = Dist.CLIENT)
public class EventsClient {
    public static ResourceLocation IMAGE_CARD = null;
    public static boolean isWorldRendered;
    public static float fadeCount = 1000;
    public static Boolean activated = false;
    public static long timer=0;
    public static int GuiCounter = 0;
    public static int reloadCounter = 0;
    public static boolean ismoving;
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
    public static final HashMap<String, Boolean> commandMap = new HashMap<>();
    private static final ArrayList<PNG> renderablePngs = new ArrayList<>();

    @SubscribeEvent
    public static void playSound(PlaySoundEvent e) {
        SimpleSoundInstance silenced = new SimpleSoundInstance(e.getSound().getLocation(), SoundSource.MUSIC, Float.MIN_VALUE, 1F, false, 0, SoundInstance.Attenuation.LINEAR, 0,0,0, false);
        for(String s : ConfigDebug.blockedmods) {
            if(e.getSound().getLocation().toString().contains(s) && e.getSound().getSource()==SoundSource.MUSIC) {
                if(!(!ChannelManager.canAnyChannelOverrideMusic() && ConfigDebug.SilenceIsBad)) e.setSound(silenced);
            }
        }
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent e) {
        lastAdvancement = e.getAdvancement().getId().toString();
        advancement = true;
    }

    //these methods will be here until the impossible library actually functions as it is supposed to
    //-----------------------------------------------------------------------------------------------
    public static PNG initializePng(ResourceLocation location) {
        try {
            return new PNG(location);
        } catch (IOException ex) {
            MusicTriggers.logger.error("Failed to initialize png at resource location "+location,ex);
        }
        return null;
    }

    public static void renderPNGToBackground(PNG png, String horizontal, String vertical, int x, int y, float scaleX, float scaleY, long millis) {
        if(!renderablePngs.contains(png)) {
            png.setHorizontal(horizontal);
            png.setVertical(vertical);
            png.setX(x);
            png.setY(y);
            png.setScaleX(scaleX);
            png.setScaleY(scaleY);
            png.setMillis(millis);
            renderablePngs.add(png);
        }
    }

    @SubscribeEvent
    public static void imageCards(RenderGameOverlayEvent.Post e) {
        if(e.getType()== RenderGameOverlayEvent.ElementType.ALL) {
            Minecraft mc = Minecraft.getInstance();
            int x = mc.getWindow().getGuiScaledWidth();
            int y = mc.getWindow().getGuiScaledHeight();
            for(PNG png : renderablePngs) renderPng(png,e.getMatrixStack(),x,y);
        }
    }

    public static void renderPng(PNG png, PoseStack matrix, int resolutionX, int resolutionY) {
        matrix.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1F, 1F, 1F, Math.min(0.95f, 1f));
        float scaleX  = (0.25f*((float)resolutionY/(float)resolutionX))*png.getScaleX();
        float scaleY = 0.25f*png.getScaleY();
        matrix.scale(scaleX,scaleY,1F);
        png.loadToManager();
        int xOffset = 0;
        int yOffset = 0;
        if(png.getHorizontal().matches("center")) xOffset = (int) ((resolutionX/2f)-((float)resolutionX*(scaleX/2f)));
        else if(png.getHorizontal().matches("right")) xOffset = (int) (resolutionX-((float)resolutionX*(scaleX/2f)));
        if(png.getVertical().matches("center")) yOffset = (int) ((resolutionY/2f)-((float)resolutionY*(scaleY/2f)));
        else if(png.getVertical().matches("top")) yOffset = (int) (resolutionY-((float)resolutionY*(scaleY/2f)));
        float posX = (xOffset*(1/scaleX))+png.getX();
        float posY = (yOffset*(1/scaleY))+png.getY();
        GuiComponent.blit(matrix,(int)posX, (int)posY,0,0,resolutionX,resolutionY,resolutionX,resolutionY);
        matrix.popPose();
    }
    //-----------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void worldRender(RenderLevelLastEvent e) {
        isWorldRendered=true;
    }

    @SubscribeEvent
    public static void clientDisconnected(PlayerEvent.PlayerLoggedOutEvent e) {
        isWorldRendered=false;
    }

    @SubscribeEvent
    public static void cancelRenders(RenderGameOverlayEvent.Pre e) {
        if(e.getType()==RenderGameOverlayEvent.ElementType.ALL && !renderDebug) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent e) {
        if(Channel.GUI.isDown() && Minecraft.getInstance().player!=null) {
            BlockPos pos = MusicPicker.roundedPos(Minecraft.getInstance().player);
            if(!zone) {
                //Minecraft.getInstance().displayGuiScreen(new GuiMain(ConfigObject.createFromCurrent()));
                Minecraft.getInstance().player.sendMessage(new TranslatableComponent("\u00A74\u00A7o"+ "misc.musictriggers.reload_start"),Minecraft.getInstance().player.getUUID());
                reloadCounter = 5;
                ChannelManager.reloading = true;
            }
            else if(!firstPass) {
                x1 = pos.getX();
                y1 = pos.getY();
                z1 = pos.getZ();
                firstPass = true;
                Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(SoundEvents.ANVIL_LAND, SoundSource.MUSIC, 1f, 1f, pos));
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
                Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(SoundEvents.ANVIL_LAND, SoundSource.MUSIC, 1f, 1f, pos));
                String compiledZoneCoords = x1+","+y1+","+z1+","+x2+","+y2+","+z2;
                parentScreen.holder.editTriggerInfoParameter(parentScreen.songCode, parentScreen.trigger, parentScreen.scrollingSongs.index, compiledZoneCoords);
                Minecraft.getInstance().setScreen(parentScreen);
            }
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if(!Minecraft.getInstance().isPaused() && !renderDebug) renderDebug = true;
        if(reloadCounter>0) {
            reloadCounter-=1;
            if(reloadCounter==1) {
                ChannelManager.reloadAllChannels();
                if(Minecraft.getInstance().player!=null) Minecraft.getInstance().player.sendMessage(new TranslatableComponent("\u00A74\u00A7o"+ "misc.musictriggers.reload_finished"),Minecraft.getInstance().player.getUUID());
                IMAGE_CARD = null;
                fadeCount = 1000;
                timer = 0;
                activated = false;
                ismoving = false;
                ChannelManager.reloading = false;
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public static void debugInfo(RenderGameOverlayEvent.Text e) {
        if(ConfigDebug.ShowDebugInfo && isWorldRendered && renderDebug) {
            e.getLeft().add("Music Triggers Debug Information");
            for(Channel channel : ChannelManager.getAllChannels())
                if(channel.currentSongName()!=null)
                    e.getLeft().add("Channel["+channel.getChannelName()+"] Current Song: "+channel.currentSongName());
            if(!ConfigDebug.ShowJustCurSong) {
                int displayCount = 0;
                for(Channel channel : ChannelManager.getAllChannels()) {
                    if(!channel.formatSongTime().matches("No song playing")) e.getLeft().add("Channel["+channel.getChannelName()+"] Current Song Time: " + channel.formatSongTime());
                    if(channel.formattedFadeOutTime()!=null) e.getLeft().add("Channel["+channel.getChannelName()+"] Fading Out: "+channel.formattedFadeOutTime());
                    if(channel.formattedFadeInTime()!=null) e.getLeft().add("Channel["+channel.getChannelName()+"] Fading In: "+channel.formattedFadeInTime());
                }
                for(Channel channel : ChannelManager.getAllChannels()) {
                    if (!channel.getPlayableTriggers().isEmpty()) {
                        StringBuilder s = new StringBuilder();
                        for (String trigger : channel.getPlayableTriggers()) {
                            if (Minecraft.getInstance().font.width(s + " " + trigger) > 0.75f * Minecraft.getInstance().getWindow().getScreenWidth()) {
                                if (displayCount == 0) {
                                    e.getLeft().add("Channel["+channel.getChannelName()+"] Playable Events: " + s);
                                    displayCount++;
                                } else e.getLeft().add(s.toString());
                                s = new StringBuilder();
                            }
                            s.append(" ").append(trigger);
                        }
                        if (displayCount == 0) e.getLeft().add("Channel["+channel.getChannelName()+"] Playable Events: " + s);
                        else e.getLeft().add(s.toString());
                    }
                    displayCount = 0;
                }
                StringBuilder sm = new StringBuilder();
                sm.append("minecraft");
                for (String ev : ConfigDebug.blockedmods) {
                    if(Minecraft.getInstance().font.width(sm+" "+ev)>0.75f*Minecraft.getInstance().getWindow().getScreenWidth()) {
                        if(displayCount==0) {
                            e.getLeft().add("Blocked Mods: " + sm);
                            displayCount++;
                        } else e.getLeft().add(sm.toString());
                        sm = new StringBuilder();
                    }
                    sm.append(" ").append(ev);
                }
                if(displayCount==0) e.getLeft().add("Blocked Mods: " + sm);
                else e.getLeft().add(sm.toString());
                displayCount=0;
                Minecraft mc = Minecraft.getInstance();
                Player player = mc.player;
                Level world = player.level;
                if(player!=null && world!=null) {
                    e.getLeft().add("Current Biome: " + world.getBiome(MusicPicker.roundedPos(player)).value().getRegistryName());
                    e.getLeft().add("Current Dimension: " + world.dimension().location());
                    e.getLeft().add("Current Total Light: " + world.getRawBrightness(MusicPicker.roundedPos(player), 0));
                    e.getLeft().add("Current Block Light: " + world.getBrightness(LightLayer.BLOCK, MusicPicker.roundedPos(player)));
                    if (MusicPicker.effectList != null && !MusicPicker.effectList.isEmpty()) {
                        StringBuilder se = new StringBuilder();
                        for (String ev : MusicPicker.effectList) {
                            if(Minecraft.getInstance().font.width(se+" "+ev)>0.75f*Minecraft.getInstance().getWindow().getScreenWidth()) {
                                if(displayCount==0) {
                                    e.getLeft().add("Effect List: " + se);
                                    displayCount++;
                                } else e.getLeft().add(se.toString());
                                se = new StringBuilder();
                            }
                            se.append(" ").append(ev);
                        }
                        if(displayCount==0) e.getLeft().add("Effect List: " + se);
                        else e.getLeft().add(se.toString());
                    }
                    if(Minecraft.getInstance().crosshairPickEntity != null) {
                        if (getLivingFromEntity(Minecraft.getInstance().crosshairPickEntity) != null)
                            e.getLeft().add("Current Entity Name: " + getLivingFromEntity(Minecraft.getInstance().crosshairPickEntity));
                        try {
                            if (infernalChecker(getLivingFromEntity(Minecraft.getInstance().crosshairPickEntity)) != null)
                                e.getLeft().add("Infernal Mob Mod Name: " + infernalChecker(getLivingFromEntity(Minecraft.getInstance().crosshairPickEntity)));
                        } catch (NoSuchMethodError ignored) { }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void renderBoss(RenderGameOverlayEvent.BossInfo e) {
        if (bossBarCounter % 11 == 0) {
            PacketHandler.sendToServer(new PacketBossInfo(e.getBossEvent().getName().getString(), e.getBossEvent().getProgress()));
            bossBarCounter = 0;
        }
        bossBarCounter++;
    }

    private static String infernalChecker(@Nullable LivingEntity m) {
        if (ModList.get().isLoaded("infernalmobs") && m != null) return InfernalMobsCore.getMobModifiers(m) == null ? null : InfernalMobsCore.getMobModifiers(m).getModName();
        return null;
    }

    private static LivingEntity getLivingFromEntity(Entity e) {
        if (e instanceof LivingEntity) return (LivingEntity) e;
        return null;
    }
}
