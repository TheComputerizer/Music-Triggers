package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.channels.Channel;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.GuiSuperType;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public class ClientEvents {
    public static int RELOAD_COUNTER = 0;
    public static String LAST_ADVANCEMENT;
    public static boolean GAINED_NEW_ADVANCEMENT;
    public static boolean SHOULD_RENDER_DEBUG = true;
    public static final Map<String, Boolean> COMMAND_MAP = new HashMap<>();

    @SubscribeEvent
    public static void playSound(PlaySoundEvent event) {
        if (Objects.isNull(event.getSound())) return;
        ResourceLocation location = event.getSound().getLocation();
        SoundCategory category = event.getSound().getSource();
        SimpleSound silenced = new SimpleSound(location, category, 1f / 1000000f, 1f,
                false, 1, ISound.AttenuationType.NONE, 0, 0, 0,true);
        for (Map.Entry<String, HashSet<String>> modEntry : ConfigDebug.FORMATTED_BLOCKED_MODS.entrySet()) {
            if (!modEntry.getKey().matches("all") && !location.getNamespace().matches(modEntry.getKey())) continue;
            for (String categoryMatch : modEntry.getValue()) {
                if (category.getName().toLowerCase().matches(categoryMatch.toLowerCase())) {
                    if (ChannelManager.handleSoundEventOverride(event.getSound().getSound(), category)) {
                        event.setResultSound(silenced);
                        return;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent e) {
        LAST_ADVANCEMENT = e.getAdvancement().getId().toString();
        GAINED_NEW_ADVANCEMENT = true;
    }

    public static void onCommand(String identifier, boolean isCommand, boolean isReload, boolean isDebug) {
        if(isCommand && !identifier.matches("not_set")) COMMAND_MAP.put(identifier,true);
        else if(isDebug) {
            ConfigDebug.SHOW_DEBUG = !ConfigDebug.SHOW_DEBUG;
            ConfigDebug.write();
        } else if(isReload) initReload();
    }

    public static boolean commandHelper(Trigger trigger) {
        String id = trigger.getParameterString("identifier");
        return COMMAND_MAP.containsKey(id) && COMMAND_MAP.get(id);
    }

    public static void commandFinish(Trigger trigger) {
        String id = trigger.getParameterString("identifier");
        COMMAND_MAP.put(id,false);
    }

    @SubscribeEvent
    public static void clientDisconnected(PlayerEvent.PlayerLoggedOutEvent e) {
        ChannelManager.onClientLogout();
    }

    @SubscribeEvent
    public static void cancelRenders(RenderGameOverlayEvent.Pre e) {
        if(e.getType()==RenderGameOverlayEvent.ElementType.ALL && !SHOULD_RENDER_DEBUG) e.setCanceled(true);
    }

    public static void initReload() {
        IFormattableTextComponent reload = AssetUtil.genericLang(Constants.MODID,"misc","reload_start",false)
                .withStyle(TextFormatting.RED).withStyle(TextFormatting.ITALIC);
        if(Objects.nonNull(Minecraft.getInstance().player))
            Minecraft.getInstance().player.sendMessage(reload,Minecraft.getInstance().player.getUUID());
        RELOAD_COUNTER = 5;
        ChannelManager.reloading = true;
        MusicTriggers.clearLog();
        MusicTriggers.logExternally(Level.INFO,"Reloading Music...");
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent e) {
        if(Channel.GUI.isDown() && Objects.nonNull(Minecraft.getInstance().player) && ChannelManager.isButtonEnabled("gui"))
            Minecraft.getInstance().setScreen(Instance.createGui(null));
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if(event.phase==TickEvent.Phase.END) {
            if (!Minecraft.getInstance().isPaused() && !(Minecraft.getInstance().screen instanceof GuiSuperType) && !SHOULD_RENDER_DEBUG)
                SHOULD_RENDER_DEBUG = true;
            if (RELOAD_COUNTER > 0) {
                RELOAD_COUNTER -= 1;
                if (RELOAD_COUNTER == 1) {
                    ChannelManager.reloadAllChannels();
                    IFormattableTextComponent reload = AssetUtil.genericLang(Constants.MODID, "misc", "reload_finished",false)
                            .withStyle(TextFormatting.GREEN).withStyle(TextFormatting.ITALIC);
                    if (Objects.nonNull(Minecraft.getInstance().player))
                        Minecraft.getInstance().player.sendMessage(reload, Minecraft.getInstance().player.getUUID());
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public static void debugInfo(RenderGameOverlayEvent.Text e) {
        if(ConfigDebug.SHOW_DEBUG && SHOULD_RENDER_DEBUG) {
            addDebug(e,"Music Triggers Debug Information");
            for(Channel channel : ChannelManager.getOrderedChannels()) {
                String curPlaying = channel.curPlayingName();
                if (Objects.nonNull(curPlaying))
                    addDebug(e,"Channel[{}] Current Song: {}",channel.getChannelName(),curPlaying);
                if (!ConfigDebug.CURRENT_SONG_ONLY || ConfigDebug.ALLOW_TIMESTAMPS) {
                    String time = channel.formatSongTime();
                    String fadeOut = channel.formattedFadeOutTime();
                    String fadeIn = channel.formattedFadeInTime();
                    if (!time.matches("No song playing"))
                        addDebug(e,"Channel[{}] Current Song Time: {}",channel.getChannelName(),time);
                    if (Objects.nonNull(fadeOut))
                        addDebug(e,"Channel[{}] Fading Out: {}",channel.getChannelName(),fadeOut);
                    if (Objects.nonNull(fadeIn))
                        addDebug(e,"Channel[{}] Fading In: {}",channel.getChannelName(),fadeIn);
                }
                if (!ConfigDebug.CURRENT_SONG_ONLY) {
                    synchronized(channel.getPlayableTriggers()) {
                        if (!channel.getPlayableTriggers().isEmpty()) {
                            StringBuilder builder = MusicTriggers.stringBuilder("Channel[{}] Playable Events:",channel.getChannelName());
                            boolean first = true;
                            for (Trigger trigger : channel.getPlayableTriggers()) {
                                String name = trigger.getNameWithID();
                                if(!first) {
                                    if (checkStringWidth(e.getWindow(), builder + " " + name)) {
                                        addDebug(e,builder.toString());
                                        builder = MusicTriggers.stringBuilder("Channel[{}] Playable Events:",channel.getChannelName());
                                    }
                                } else first = false;
                                builder.append(" ").append(name);
                            }
                            addDebug(e,builder.toString());
                        }
                    }
                }
            }
            if (!ConfigDebug.CURRENT_SONG_ONLY) {
                StringBuilder builder = MusicTriggers.stringBuilder("Blocked Mods:");
                boolean first = true;
                for(Map.Entry<String,HashSet<String>> modEntry : ConfigDebug.FORMATTED_BLOCKED_MODS.entrySet()) {
                    String blocked = modEntry.getKey()+"["+ TextUtil.listToString(modEntry.getValue(),",")+"]";
                    if(!first) {
                        if (checkStringWidth(e.getWindow(), builder + " " + blocked)) {
                            addDebug(e,builder.toString());
                            builder = MusicTriggers.stringBuilder("Blocked Mods:");
                        }
                    } else first = false;
                    builder.append(" ").append(blocked);
                }
                addDebug(e,builder.toString());
                Minecraft mc = Minecraft.getInstance();
                ClientPlayerEntity player = mc.player;
                if(Objects.nonNull(player)) {
                    World world = player.clientLevel;
                    if(Objects.nonNull(mc.screen))
                        addDebug(e,"Current GUI Class Name: {}",mc.screen.getClass().getName());
                    addDebug(e,"Current Biome Name: {}",world.getBiome(player.blockPosition()).getRegistryName());
                    addDebug(e,"Current Biome Category: {}",world.getBiome(player.blockPosition()).getBiomeCategory().getName());
                    addDebug(e,"Current Dimension: {}",world.dimension().location());
                    addDebug(e,"Current Structure: {}",ChannelManager.CUR_STRUCT);
                    addDebug(e,"Current Total Light: {}",world.getRawBrightness(roundedPos(player), 0));
                    addDebug(e,"Current Block Light: {}",world.getBrightness(LightType.BLOCK,roundedPos(player)));
                    Set<String> effectSet = Trigger.getCachedEffects();
                    if(!effectSet.isEmpty()) {
                        builder = MusicTriggers.stringBuilder("Effect List:");
                        first = true;
                        for (String effect : effectSet) {
                            if(!first) {
                                if (checkStringWidth(e.getWindow(), builder + " " + effect)) {
                                    addDebug(e,builder.toString());
                                    builder = MusicTriggers.stringBuilder("Effect List:");
                                }
                            }
                            else first = false;
                            builder.append(" ").append(effect);
                        }
                        addDebug(e,builder.toString());
                    }
                    LivingEntity entity = getLivingFromEntity(mc.crosshairPickEntity);
                    if(Objects.nonNull(entity)) {
                        addDebug(e,"Current Entity Name: {}",entity.getName().getString());
                        addDebug(e,"Current Entity ID: {}",entity.getType().getRegistryName());
                    }
                }
            }
        }
    }
    
    private static void addDebug(RenderGameOverlayEvent.Text e, String msg, Object ... parameters) {
        e.getLeft().add(LogUtil.injectParameters(msg,parameters));
    }

    private static boolean checkStringWidth(MainWindow window, String s) {
        return (window.getGuiScaledWidth()*0.9f)<=Minecraft.getInstance().font.width(s);
    }

    private static BlockPos roundedPos(PlayerEntity p) {
        return new BlockPos((Math.round(p.getX()*2d)/2d),(Math.round(p.getY()*2d)/2d),(Math.round(p.getZ()*2d)/2d));
    }

    private static @Nullable LivingEntity getLivingFromEntity(Entity entity) {
        return entity instanceof LivingEntity ? (LivingEntity)entity : null;
    }
}
