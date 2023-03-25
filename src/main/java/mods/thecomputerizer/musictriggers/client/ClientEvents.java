package mods.thecomputerizer.musictriggers.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.GuiSuperType;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class ClientEvents {
    public static boolean IS_WORLD_RENDERED;
    public static int RELOAD_COUNTER = 0;
    public static String LAST_ADVANCEMENT;
    public static boolean GAINED_NEW_ADVANCEMENT;
    public static boolean SHOULD_RENDER_DEBUG = true;
    public static final HashMap<String, Boolean> COMMAND_MAP = new HashMap<>();

    public static SoundInstance playSound(SoundInstance sound) {
        SimpleSoundInstance silenced = new SimpleSoundInstance(sound.getLocation(), SoundSource.MUSIC, Float.MIN_VALUE*1000, 1F,
                false, 0, SoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, true);
        for(String s : ConfigDebug.BLOCKED_MOD_MUSIC) {
            if(sound.getLocation().toString().contains(s) && sound.getSource()==SoundSource.MUSIC) {
                if(!ConfigDebug.PLAY_NORMAL_MUSIC || ChannelManager.canAnyChannelOverrideMusic()) return silenced;
            }
        }
        for(String s : ConfigDebug.BLOCKED_MOD_RECORDS) {
            if(sound.getLocation().toString().contains(s) && sound.getSource()==SoundSource.RECORDS) {
                if(!ConfigDebug.PLAY_NORMAL_MUSIC || ChannelManager.canAnyChannelOverrideMusic()) return silenced;
            }
        }
        return sound;
    }

    public static void onAdvancement(Advancement adv) {
        LAST_ADVANCEMENT = adv.getId().toString();
        GAINED_NEW_ADVANCEMENT = true;
    }

    public static boolean commandHelper(Trigger trigger) {
        String id = trigger.getParameter("identifier");
        return COMMAND_MAP.containsKey(id) && COMMAND_MAP.get(id);
    }

    public static void commandFinish(Trigger trigger) {
        String id = trigger.getParameter("identifier");
        COMMAND_MAP.put(id,false);
    }

    public static void onDisconnect() {
        IS_WORLD_RENDERED =false;
    }

    public static void initReload() {
        Component reload = AssetUtil.genericLang(Constants.MODID,"misc","reload_start",false)
                .withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC);
        if(Objects.nonNull(Minecraft.getInstance().player))
            Minecraft.getInstance().player.sendMessage(reload,Minecraft.getInstance().player.getUUID());
        RELOAD_COUNTER = 5;
        ChannelManager.reloading = true;
        MusicTriggers.savedMessages.clear();
        MusicTriggers.logExternally(Level.INFO,"Reloading Music...");
    }

    public static void onKeyInput() {
        if(Minecraft.getInstance().player!=null)
            Minecraft.getInstance().setScreen(Instance.createGui());
    }

    public static void onTick() {
        if (!Minecraft.getInstance().isPaused() && !(Minecraft.getInstance().screen instanceof GuiSuperType) && !SHOULD_RENDER_DEBUG)
            SHOULD_RENDER_DEBUG = true;
        if (RELOAD_COUNTER > 0) {
            RELOAD_COUNTER -= 1;
            if (RELOAD_COUNTER == 1) {
                ChannelManager.reloadAllChannels();
                Component reload = AssetUtil.genericLang(Constants.MODID, "misc", "reload_finished", false)
                        .withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.ITALIC);
                if (Objects.nonNull(Minecraft.getInstance().player))
                    Minecraft.getInstance().player.sendMessage(reload, Minecraft.getInstance().player.getUUID());
                ChannelManager.reloading = false;
            }
        }
    }

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    public static void debugInfo(PoseStack matrix) {
        if(!IS_WORLD_RENDERED) {
            ChannelManager.initializeServerInfo();
            IS_WORLD_RENDERED = true;
        }
        if(ConfigDebug.SHOW_DEBUG && IS_WORLD_RENDERED && SHOULD_RENDER_DEBUG) {
            List<String> lines = new ArrayList<>();
            lines.add("Music Triggers Debug Information");
            for(Channel channel : ChannelManager.getAllChannels()) {
                if (channel.curPlayingName() != null)
                    lines.add("Channel[" + channel.getChannelName() + "] Current Song: " + channel.curPlayingName());
                if (!ConfigDebug.CURRENT_SONG_ONLY) {
                    int displayCount = 0;
                    if (!channel.formatSongTime().matches("No song playing"))
                        lines.add("Channel[" + channel.getChannelName() + "] Current Song Time: " + channel.formatSongTime());
                    if (channel.formattedFadeOutTime() != null)
                        lines.add("Channel[" + channel.getChannelName() + "] Fading Out: " + channel.formattedFadeOutTime());
                    if (channel.formattedFadeInTime() != null)
                        lines.add("Channel[" + channel.getChannelName() + "] Fading In: " + channel.formattedFadeInTime());
                    synchronized(channel.getPlayableTriggers()) {
                        if (!channel.getPlayableTriggers().isEmpty()) {
                            StringBuilder s = new StringBuilder();
                            for (Trigger trigger : channel.getPlayableTriggers()) {
                                String name = trigger.getNameWithID();
                                if (Minecraft.getInstance().font.width(s + " " + name) > 0.75f * Minecraft.getInstance().getWindow().getGuiScaledWidth()) {
                                    if (displayCount == 0) {
                                        lines.add("Channel[" + channel.getChannelName() + "] Playable Events: " + s);
                                        displayCount++;
                                    } else lines.add(s.toString());
                                    s = new StringBuilder();
                                }
                                s.append(" ").append(name);
                            }
                            if (displayCount == 0)
                                lines.add("Channel[" + channel.getChannelName() + "] Playable Events: " + s);
                            else lines.add(s.toString());
                        }
                    }
                }
            }
            if (!ConfigDebug.CURRENT_SONG_ONLY) {
                int displayCount = 0;
                StringBuilder sm = new StringBuilder();
                sm.append("minecraft");
                for (String ev : ConfigDebug.BLOCKED_MOD_MUSIC) {
                    if(Minecraft.getInstance().font.width(sm+" "+ev)>0.75f*Minecraft.getInstance().getWindow().getGuiScaledWidth()) {
                        if(displayCount==0) {
                            lines.add("Blocked Mods: " + sm);
                            displayCount++;
                        } else lines.add(sm.toString());
                        sm = new StringBuilder();
                    }
                    sm.append(" ").append(ev);
                }
                if(displayCount==0) lines.add("Blocked Mods: " + sm);
                else lines.add(sm.toString());
                displayCount=0;
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer player = mc.player;
                net.minecraft.world.level.Level world = player.level;
                if(player!=null && world!=null) {
                    ResourceKey<Biome> biomeKey = world.getBiome(player.blockPosition()).unwrapKey().orElse(null);
                    lines.add("Current Biome Name: " + (Objects.nonNull(biomeKey) ? biomeKey.location().toString() : "Unknown Biome"));
                    lines.add("Current Biome Category: " + Biome.getBiomeCategory(world.getBiome(player.blockPosition())).getName());
                    lines.add("Current Dimension: " + world.dimension().location());
                    lines.add("Current Structure: " + ChannelManager.CUR_STRUCT);
                    lines.add("Current Total Light: " +  world.getRawBrightness(roundedPos(player), 0));
                    lines.add("Current Block Light: " + world.getBrightness(LightLayer.BLOCK, roundedPos(player)));
                    if (MusicPicker.effectList != null && !MusicPicker.effectList.isEmpty()) {
                        StringBuilder se = new StringBuilder();
                        for (String ev : MusicPicker.effectList) {
                            if(Minecraft.getInstance().font.width(se+" "+ev)>0.75f*Minecraft.getInstance().getWindow().getGuiScaledWidth()) {
                                if(displayCount==0) {
                                    lines.add("Effect List: " + se);
                                    displayCount++;
                                } else lines.add(se.toString());
                                se = new StringBuilder();
                            }
                            se.append(" ").append(ev);
                        }
                        if(displayCount==0) lines.add("Effect List: " + se);
                        else lines.add(se.toString());
                    }
                    if(Minecraft.getInstance().crosshairPickEntity != null) {
                        if (getLivingFromEntity(Minecraft.getInstance().crosshairPickEntity) != null)
                            lines.add("Current Entity Name: " + getLivingFromEntity(Minecraft.getInstance()
                                    .crosshairPickEntity).getName().getString());
                    }
                }
            }
            int top = 2;
            for (String msg : lines) {
                GuiComponent.fill(matrix, 1, top - 1, 2 + Minecraft.getInstance().font.width(msg) + 1,
                        top + Minecraft.getInstance().font.lineHeight - 1, -1873784752);
                Minecraft.getInstance().font.draw(matrix, msg, 2, top, 14737632);
                top += Minecraft.getInstance().font.lineHeight;
            }
        }
    }

    private static BlockPos roundedPos(Player p) {
        return new BlockPos((Math.round(p.getX() * 2) / 2.0), (Math.round(p.getY() * 2) / 2.0), (Math.round(p.getZ() * 2) / 2.0));
    }

    private static LivingEntity getLivingFromEntity(Entity e) {
        if (e instanceof LivingEntity) return (LivingEntity) e;
        return null;
    }
}
