package mods.thecomputerizer.musictriggers.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Environment(EnvType.CLIENT)
public class ClientEvents {
    public static int RELOAD_COUNTER = 0;
    public static String LAST_ADVANCEMENT;
    public static boolean GAINED_NEW_ADVANCEMENT;
    public static boolean SHOULD_RENDER_DEBUG = true;
    public static final Map<String,Boolean> COMMAND_MAP = new HashMap<>();
    private static final List<Class<? extends Screen>> ERRORED_GUI_CLASSES = new ArrayList<>();

    public static SoundInstance playSound(SoundInstance sound) {
        if(Objects.nonNull(sound)) {
            ResourceLocation location = sound.getLocation();
            SoundSource category = sound.getSource();
            SimpleSoundInstance silenced = new SimpleSoundInstance(location,category,1f/1000000f,1f,
                    false,1,SoundInstance.Attenuation.NONE,0,0,0,true);
            for(Map.Entry<String,Set<String>> modEntry : ConfigDebug.FORMATTED_BLOCKED_MODS.entrySet()) {
                if(!modEntry.getKey().matches("all") && !location.getNamespace().matches(modEntry.getKey())) continue;
                for(String categoryMatch : modEntry.getValue())
                    if(category.getName().toLowerCase().matches(categoryMatch.toLowerCase()) &&
                            ChannelManager.handleSoundEventOverride(sound.getSound(),category)) return silenced;

            }
        }
        return sound;
    }

    public static void onAdvancement(ResourceLocation advLocation) {
        LAST_ADVANCEMENT = advLocation.toString();
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

    public static void clientDisconnected() {
        ChannelManager.onClientLogout();
    }

    public static void initReload() {
        Component reload = AssetUtil.genericLang(Constants.MODID,"misc","reload_start",false)
                .withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC);
        LocalPlayer player = Minecraft.getInstance().player;
        if(Objects.nonNull(player)) player.sendMessage(reload,player.getUUID());
        RELOAD_COUNTER = 5;
        ChannelManager.reloading = true;
        MusicTriggers.clearLog();
        MusicTriggers.logExternally(Level.INFO,"Reloading Music...");
    }

    public static void onKeyInput() {
        if(Objects.nonNull(Minecraft.getInstance().player) && ChannelManager.isButtonEnabled("gui"))
            Minecraft.getInstance().setScreen(Instance.createGui(null));
    }

    public static void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if(!mc.isPaused() && !(mc.screen instanceof GuiSuperType) && !SHOULD_RENDER_DEBUG)
            SHOULD_RENDER_DEBUG = true;
        if(RELOAD_COUNTER>0) {
            RELOAD_COUNTER -= 1;
            if(RELOAD_COUNTER==1) {
                ChannelManager.reloadAllChannels();
                Component reload = AssetUtil.genericLang(Constants.MODID,"misc", "reload_finished", false)
                        .withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.ITALIC);
                if(Objects.nonNull(mc.player)) mc.player.sendMessage(reload,mc.player.getUUID());
            }
        }
    }

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    public static void debugInfo(PoseStack matrix) {
        if(ConfigDebug.SHOW_DEBUG && SHOULD_RENDER_DEBUG) {
            List<String> lines = new ArrayList<>();
            addDebug(lines,"Music Triggers Debug Information");
            Window window = Minecraft.getInstance().getWindow();
            for(Channel channel : ChannelManager.getOrderedChannels()) {
                String curPlaying = channel.curPlayingName();
                if(Objects.nonNull(curPlaying))
                    addDebug(lines,channel.getLogMessage("Current Song: {}"),channel.getChannelName(),curPlaying);
                if(!ConfigDebug.CURRENT_SONG_ONLY || ConfigDebug.ALLOW_TIMESTAMPS) {
                    String time = channel.formatSongTime();
                    String fadeOut = channel.formattedFadeOutTime();
                    String fadeIn = channel.formattedFadeInTime();
                    if(!time.matches("No song playing"))
                        addDebug(lines,channel.getLogMessage("Current Song Time: {}"),channel.getChannelName(),time);
                    if(Objects.nonNull(fadeOut))
                        addDebug(lines,channel.getLogMessage("Fading Out: {}"),channel.getChannelName(),fadeOut);
                    if(Objects.nonNull(fadeIn))
                        addDebug(lines,channel.getLogMessage("Fading In: {}"),channel.getChannelName(),fadeIn);
                }
                if(!ConfigDebug.CURRENT_SONG_ONLY) {
                    synchronized(channel.getPlayableTriggers()) {
                        if(!channel.getPlayableTriggers().isEmpty()) {
                            StringBuilder builder = new StringBuilder(channel.getLogMessage("Playable Events:"));
                            boolean first = true;
                            for(Trigger trigger : channel.getPlayableTriggers()) {
                                String name = trigger.getNameWithID();
                                if(!first) {
                                    if(checkStringWidth(window,builder+" "+name)) {
                                        addDebug(lines,builder.toString());
                                        builder = new StringBuilder(channel.getLogMessage("Playable Events:"));
                                    }
                                } else first = false;
                                builder.append(" ").append(name);
                            }
                            addDebug(lines,builder.toString());
                        }
                    }
                }
            }
            if(!ConfigDebug.CURRENT_SONG_ONLY) {
                StringBuilder builder = new StringBuilder("Blocked Mods:");
                boolean first = true;
                for(Map.Entry<String,Set<String>> modEntry : ConfigDebug.FORMATTED_BLOCKED_MODS.entrySet()) {
                    String blocked = modEntry.getKey()+"["+ TextUtil.listToString(modEntry.getValue(),",")+"]";
                    if(!first) {
                        if(checkStringWidth(window,builder+" "+blocked)) {
                            addDebug(lines,builder.toString());
                            builder = new StringBuilder("Blocked Mods:");
                        }
                    } else first = false;
                    builder.append(" ").append(blocked);
                }
                addDebug(lines,builder.toString());
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer player = mc.player;
                if(Objects.nonNull(player)) {
                    ClientLevel level = player.clientLevel;
                    tryGuiClassDebug(lines,mc.screen);
                    ResourceKey<Biome> biomeKey = level.getBiome(player.blockPosition()).unwrapKey().orElse(null);
                    addDebug(lines,"Current Biome Name: {}",(Objects.nonNull(biomeKey) ? biomeKey.location().toString() : "Unknown Biome"));
                    addDebug(lines,"Current Biome Category: {}",Biome.getBiomeCategory(level.getBiome(player.blockPosition())).getName());
                    addDebug(lines,"Current Dimension: {}",level.dimension().location());
                    addDebug(lines,"Current Structure: {}",ChannelManager.CUR_STRUCT);
                    addDebug(lines,"Current Total Light: {}",level.getRawBrightness(roundedPos(player), 0));
                    addDebug(lines,"Current Block Light: {}",level.getBrightness(LightLayer.BLOCK,roundedPos(player)));
                    Set<String> effectSet = Trigger.getCachedEffects();
                    if(!effectSet.isEmpty()) {
                        builder = new StringBuilder("Effect List:");
                        first = true;
                        for(String effect : effectSet) {
                            if(!first) {
                                if(checkStringWidth(window,builder+" "+effect)) {
                                    addDebug(lines,builder.toString());
                                    builder = new StringBuilder("Effect List:");
                                }
                            }
                            else first = false;
                            builder.append(" ").append(effect);
                        }
                        addDebug(lines,builder.toString());
                    }
                    LivingEntity entity = getLivingFromEntity(mc.crosshairPickEntity);
                    if(Objects.nonNull(entity)) {
                        addDebug(lines,"Current Entity Name: {}",entity.getName().getString());
                        Registry<EntityType<?>> reg = level.registryAccess().registry(Registry.ENTITY_TYPE_REGISTRY).orElse(null);
                        if(Objects.nonNull(reg)) addDebug(lines,"Current Entity ID: {}",reg.getKey(entity.getType()));
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

    private static void tryGuiClassDebug(List<String> lines, @Nullable Screen screen) {
        if(Objects.nonNull(screen) && !ERRORED_GUI_CLASSES.contains(screen.getClass())) {
            try {
                addDebug(lines,"Current GUI Class Name: {}",screen.getClass().getName());
            } catch(IllegalArgumentException ignored) {
                MusicTriggers.logBoth(Level.ERROR, "There was an error displaying the name for GUI "+
                        "class '{}' in the debug info! Class will be ignored",null,screen.getClass());
                ERRORED_GUI_CLASSES.add(screen.getClass());
            }
        }
    }
    
    private static void addDebug(List<String> lines, String msg, Object ... parameters) {
        lines.add(LogUtil.injectParameters(msg,parameters));
    }

    private static boolean checkStringWidth(Window window, String s) {
        return (window.getGuiScaledWidth()*0.9f)<=Minecraft.getInstance().font.width(s);
    }

    private static BlockPos roundedPos(Player p) {
        return new BlockPos((Math.round(p.getX()*2d)/2d),(Math.round(p.getY()*2d)/2d),(Math.round(p.getZ()*2d)/2d));
    }

    private static @Nullable LivingEntity getLivingFromEntity(Entity entity) {
        return entity instanceof LivingEntity ? (LivingEntity)entity : null;
    }
}
