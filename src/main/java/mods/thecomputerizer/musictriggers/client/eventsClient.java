package mods.thecomputerizer.musictriggers.client;

import com.mojang.blaze3d.systems.RenderSystem;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.client.gui.GuiTriggerInfo;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.musictriggers.mixin.BossBarHudAccessor;
import mods.thecomputerizer.musictriggers.mixin.InGameHudAccessor;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.packets.PacketBossInfo;
import net.minecraft.advancement.Advancement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.*;

public class EventsClient {
    public static Identifier IMAGE_CARD = null;
    public static boolean isWorldRendered;
    public static float fadeCount = 1000;
    public static Boolean activated = false;
    public static long timer=0;
    public static int GuiCounter = 0;
    public static int reloadCounter = 0;
    public static boolean ismoving;
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
    public static final HashMap<String, Boolean> commandMap = new HashMap<>();
    private static final ArrayList<PNG> renderablePngs = new ArrayList<>();

    public static SoundInstance playSound(SoundInstance sound) {
        if (sound!=null) {
            for (String s : ConfigDebug.blockedmods) {
                if (sound.getId().getNamespace().contains(s) && sound.getCategory() == SoundCategory.MUSIC && !(ChannelManager.canAnyChannelOverrideMusic() && ConfigDebug.SilenceIsBad))
                    return new PositionedSoundInstance(sound.getId(), SoundCategory.MUSIC, Float.MIN_VALUE*1000, 1F, false, 0, SoundInstance.AttenuationType.LINEAR, 0,0,0, false);
            }
        }
        return sound;
    }
    public static void onAdvancement(Advancement a) {
        lastAdvancement = a.getId().toString();
        advancement = true;
    }

    //these methods will be here until the impossible library actually functions as it is supposed to
    //-----------------------------------------------------------------------------------------------
    public static PNG initializePng(Identifier location) {
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

    public static void imageCards(MatrixStack matrix) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int x = mc.getWindow().getScaledWidth();
        int y = mc.getWindow().getScaledHeight();
        for (PNG png : renderablePngs) renderPng(png, matrix, x, y);
    }

    public static void renderPng(PNG png, MatrixStack matrix, int resolutionX, int resolutionY) {
        matrix.push();
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
        DrawableHelper.drawTexture(matrix,(int)posX, (int)posY,0,0,resolutionX,resolutionY,resolutionX,resolutionY);
        matrix.pop();
    }
    //-----------------------------------------------------------------------------------------------

    public static void onDisconnect() {
        isWorldRendered=false;
    }

    public static void onKeyInput() {
        if(MinecraftClient.getInstance().player!=null) {
            BlockPos pos = MusicPicker.roundedPos(MinecraftClient.getInstance().player);
            if(!zone) {
                //Minecraft.getInstance().displayGuiScreen(new GuiMain(ConfigObject.createFromCurrent()));
                MinecraftClient.getInstance().player.sendMessage(new TranslatableText("\u00A74\u00A7o"+ "misc.musictriggers.reload_start"),false);
                reloadCounter = 5;
                ChannelManager.reloading = true;
            }
            else if(!firstPass) {
                x1 = pos.getX();
                y1 = pos.getY();
                z1 = pos.getZ();
                firstPass = true;
                MinecraftClient.getInstance().getSoundManager().play(new PositionedSoundInstance(SoundEvents.BLOCK_ANVIL_BREAK, SoundCategory.MUSIC, 1f, 1f, pos));
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
                MinecraftClient.getInstance().getSoundManager().play(new PositionedSoundInstance(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MUSIC, 1f, 1f, pos));
                String compiledZoneCoords = x1+","+y1+","+z1+","+x2+","+y2+","+z2;
                parentScreen.holder.editTriggerInfoParameter(parentScreen.songCode, parentScreen.trigger, parentScreen.scrollingSongs.index, compiledZoneCoords);
                MinecraftClient.getInstance().setScreen(parentScreen);
            }
        }
    }

    public static void onTick() {
        if(!MinecraftClient.getInstance().isPaused() && !renderDebug) renderDebug = true;
        if(reloadCounter>0) {
            reloadCounter-=1;
            if(reloadCounter==1) {
                ChannelManager.reloadAllChannels();
                if(MinecraftClient.getInstance().player!=null) MinecraftClient.getInstance().player.sendMessage(new TranslatableText("\u00A74\u00A7o"+ "misc.musictriggers.reload_finished"),false);
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
    public static void debugInfo(MatrixStack matrix) {
        if (ConfigDebug.ShowDebugInfo && renderDebug) {
            List<String> left = new ArrayList<>();
            left.add("Music Triggers Debug Information");
            for(Channel channel : ChannelManager.getAllChannels())
                if(channel.currentSongName()!=null)
                    left.add("Channel["+channel.getChannelName()+"] Current Song: "+channel.currentSongName());
            if(!ConfigDebug.ShowJustCurSong) {
                int displayCount = 0;
                for(Channel channel : ChannelManager.getAllChannels()) {
                    if(!channel.formatSongTime().matches("No song playing")) left.add("Channel["+channel.getChannelName()+"] Current Song Time: " + channel.formatSongTime());
                    if(channel.formattedFadeOutTime()!=null) left.add("Channel["+channel.getChannelName()+"] Fading Out: "+channel.formattedFadeOutTime());
                    if(channel.formattedFadeInTime()!=null) left.add("Channel["+channel.getChannelName()+"] Fading In: "+channel.formattedFadeInTime());
                }
                for(Channel channel : ChannelManager.getAllChannels()) {
                    if (!channel.getPlayableTriggers().isEmpty()) {
                        StringBuilder s = new StringBuilder();
                        for (String trigger : channel.getPlayableTriggers()) {
                            if (MinecraftClient.getInstance().textRenderer.getWidth(s + " " + trigger) > 0.75f * MinecraftClient.getInstance().getWindow().getScaledWidth()) {
                                if (displayCount == 0) {
                                    left.add("Channel["+channel.getChannelName()+"] Playable Events: " + s);
                                    displayCount++;
                                } else left.add(s.toString());
                                s = new StringBuilder();
                            }
                            s.append(" ").append(trigger);
                        }
                        if (displayCount == 0) left.add("Channel["+channel.getChannelName()+"] Playable Events: " + s);
                        else left.add(s.toString());
                    }
                    displayCount = 0;
                }
                StringBuilder sm = new StringBuilder();
                sm.append("minecraft");
                for (String ev : ConfigDebug.blockedmods) {
                    if(MinecraftClient.getInstance().textRenderer.getWidth(sm+" "+ev)>0.75f*MinecraftClient.getInstance().getWindow().getScaledWidth()) {
                        if(displayCount==0) {
                            left.add("Blocked Mods: " + sm);
                            displayCount++;
                        } else left.add(sm.toString());
                        sm = new StringBuilder();
                    }
                    sm.append(" ").append(ev);
                }
                if(displayCount==0) left.add("Blocked Mods: " + sm);
                else left.add(sm.toString());
                displayCount=0;
                PlayerEntity player = MinecraftClient.getInstance().player;
                World world = player.world;
                if(player!=null && world!=null) {
                    left.add("Current Biome: " + world.getBiome(MusicPicker.roundedPos(player)).getKey().get().getValue().toString());
                    left.add("Current Dimension: " + player.world.getRegistryKey().getValue().toString());
                    left.add("Current Total Light: " + world.getLightLevel(MusicPicker.roundedPos(player), 0));
                    left.add("Current Block Light: " + world.getLightLevel(LightType.BLOCK, MusicPicker.roundedPos(player)));
                    if (MusicPicker.effectList != null && !MusicPicker.effectList.isEmpty()) {
                        StringBuilder se = new StringBuilder();
                        for (String ev : MusicPicker.effectList) {
                            if(MinecraftClient.getInstance().textRenderer.getWidth(se+" "+ev)>0.75f*MinecraftClient.getInstance().getWindow().getScaledWidth()) {
                                if(displayCount==0) {
                                    left.add("Effect List: " + se);
                                    displayCount++;
                                } else left.add(se.toString());
                                se = new StringBuilder();
                            }
                            se.append(" ").append(ev);
                        }
                        if(displayCount==0) left.add("Effect List: " + se);
                        else left.add(se.toString());
                    }
                    if (getLivingFromEntity(MinecraftClient.getInstance().targetedEntity) != null) left.add("Music Triggers Current Entity Name: "+getLivingFromEntity(MinecraftClient.getInstance().targetedEntity).getName().getString());
                    if (MinecraftClient.getInstance().currentScreen != null) left.add("Music Triggers current GUI: "+MinecraftClient.getInstance().currentScreen);
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
                PacketHandler.sendToServer(new PacketBossInfo(bar.getName().getString(),bar.getPercent()));
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
