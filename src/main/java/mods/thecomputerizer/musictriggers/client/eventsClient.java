package mods.thecomputerizer.musictriggers.client;

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
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.*;

public class EventsClient {
    public static Identifier IMAGE_CARD = null;
    public static int curImageIndex;
    public static boolean isWorldRendered;
    public static float fadeCount = 1000;
    public static float startDelayCount = 0;
    public static Boolean activated = false;
    public static long timer=0;
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
    public static final HashMap<String, Boolean> commandMap = new HashMap<>();

    public static SoundInstance playSound(SoundInstance sound) {
        if (sound!=null) {
            for (String s : ConfigDebug.blockedmods) {
                if (sound.getId().getNamespace().contains(s) && sound.getCategory() == SoundCategory.MUSIC && !(ChannelManager.canAnyChannelOverrideMusic() && ConfigDebug.SilenceIsBad))
                    return new PositionedSoundInstance(sound.getId(), SoundCategory.MUSIC, Float.MIN_VALUE*1000, 1F, false, 0, SoundInstance.AttenuationType.LINEAR, 0,0,0, false);
            }
        }
        return sound;
    }

    /*
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent e) {
        if(e.getEntityLiving() instanceof EntityPlayer && e.getSource().getTrueSource() instanceof EntityPlayer) {
            if (e.getEntityLiving() == MusicPicker.player) {
                PVPTracker = (EntityPlayer)e.getSource().getTrueSource();
                MusicPicker.setPVP = true;
            }
            else if(e.getSource().getTrueSource() == MusicPicker.player) {
                PVPTracker = (EntityPlayer)e.getEntityLiving();
                MusicPicker.setPVP = true;
            }
        }
    }
     */

    public static void onAdvancement(Advancement a) {
        lastAdvancement = a.getId().toString();
        advancement = true;
    }

    public static void onDisconnect() {
        isWorldRendered=false;
    }

    /*
    @SubscribeEvent
    public static void customTick(CustomTick ev) {
        if(ConfigTransitions.imagecards.get(curImageIndex)!=null) {
            if (timer > ConfigTransitions.imagecards.get(curImageIndex).getTime()) {
                activated = false;
                timer = 0;
                ismoving = false;
                movingcounter = 0;
            }
            if (ismoving) {
                if (timer % ConfigTransitions.imagecards.get(curImageIndex).getDelay() == 0) {
                    movingcounter++;
                    if (movingcounter >= pngs.size()) movingcounter = 0;
                }
                IMAGE_CARD = pngs.get(movingcounter);
            }
            if (activated) {
                timer++;
                startDelayCount++;
                if (startDelayCount > 0) {
                    if (fadeCount > 1) {
                        fadeCount -= ConfigTransitions.imagecards.get(curImageIndex).getFadeIn();
                        if (fadeCount < 1) fadeCount = 1;
                    }
                }
            } else {
                if (fadeCount < 1000) {
                    fadeCount += ConfigTransitions.imagecards.get(curImageIndex).getFadeOut();
                    if (fadeCount > 1000) {
                        fadeCount = 1000;
                        ismoving = false;
                    }
                }
                startDelayCount = 0;
            }
        }
    }

    @SubscribeEvent
    public static void imageCards(RenderGameOverlayEvent.Post e) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if(e.getType()== RenderGameOverlayEvent.ElementType.ALL) {
            ScaledResolution res = e.getResolution();
            if (player != null && ConfigTransitions.imagecards.get(curImageIndex)!=null) {
                int x = res.getScaledWidth();
                int y = res.getScaledHeight();
                Vector4f color = new Vector4f(1, 1, 1, 1);
                if (fadeCount != 1000 && IMAGE_CARD!=null) {
                    GlStateManager.enableBlend();
                    GlStateManager.pushMatrix();
                    mc.getTextureManager().bindTexture(IMAGE_CARD);

                    float opacity = (int) (17 - (fadeCount / 80));
                    opacity = (opacity * 1.15f) / 15;
                    GlStateManager.color(color.getX(), color.getY(), color.getZ(), Math.max(0, Math.min(0.95f, opacity)));

                    float scale_x = (0.25f*((float)y/(float)x))*(ConfigTransitions.imagecards.get(curImageIndex).getScaleX()/100f);
                    float scale_y = 0.25f*(ConfigTransitions.imagecards.get(curImageIndex).getScaleY()/100f);
                    GlStateManager.scale(scale_x,scale_y,1f);

                    float posX = ((x*(1f/scale_x))/2f)-(x/2f);
                    float posY = (y*(1f/scale_y))/8f;
                    GuiScreen.drawModalRectWithCustomSizedTexture((int)((posX)+(ConfigTransitions.imagecards.get(curImageIndex).getHorizontal()*(1/scale_x))),
                            (int)((posY)+(ConfigTransitions.imagecards.get(curImageIndex).getVertical()*(1/scale_y))),x,y,x,y,x,y);

                    GlStateManager.color(1F, 1F, 1F, 1);
                    GlStateManager.popMatrix();
                }
            }
        }
    }
     */

    public static void onKeyInput() {
        if(MinecraftClient.getInstance().player!=null) {
            BlockPos pos = MusicPicker.roundedPos(MinecraftClient.getInstance().player);
            if(!zone) {
                //Minecraft.getInstance().displayGuiScreen(new GuiMain(ConfigObject.createFromCurrent()));
                MinecraftClient.getInstance().player.sendMessage(new LiteralText("\u00A74\u00A7oReloading Music! This won't take long..."),false);
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
                if(MinecraftClient.getInstance().player!=null) MinecraftClient.getInstance().player.sendMessage(new LiteralText("\u00A7a\u00A7oFinished!"),false);
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
                PacketHandler.sendToServer(PacketBossInfo.id,PacketBossInfo.encode(new PacketBossInfo(bar.getName().getString(),bar.getPercent())));
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
