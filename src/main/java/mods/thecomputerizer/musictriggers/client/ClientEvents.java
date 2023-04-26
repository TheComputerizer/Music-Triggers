package mods.thecomputerizer.musictriggers.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import com.mojang.blaze3d.platform.Window;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.GuiSuperType;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public class ClientEvents {
    public static boolean IS_WORLD_RENDERED;
    public static int RELOAD_COUNTER = 0;
    public static String LAST_ADVANCEMENT;
    public static boolean GAINED_NEW_ADVANCEMENT;
    public static boolean SHOULD_RENDER_DEBUG = true;
    public static final HashMap<String, Boolean> COMMAND_MAP = new HashMap<>();

    @SubscribeEvent
    public static void playSound(PlaySoundEvent e) {
        if(Objects.isNull(e.getSound())) return;
        SimpleSoundInstance silenced = new SimpleSoundInstance(e.getSound().getLocation(), e.getSound().getSource(),
                Float.MIN_VALUE*10000, 1F, RandomSource.create(), false, 0,
                SoundInstance.Attenuation.NONE, 0F, 0F, 0F,true);
        for(String s : ConfigDebug.BLOCKED_MOD_CATEGORIES) {
            String modid = s.contains(";") ? s.substring(0,s.indexOf(';')) : s;
            if(!e.getSound().getLocation().getNamespace().matches(modid)) continue;
            String categoryName = s.contains(";") && s.indexOf(';')+1<s.length() ? s.substring(s.indexOf(';')+1) : "music";
            if(e.getSound().getSource().getName().matches(categoryName)) {
                if(ChannelManager.handleSoundEventOverride(e.getSound())) {
                    e.setSound(silenced);
                    return;
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent.AdvancementEarnEvent e) {
        LAST_ADVANCEMENT = e.getAdvancement().getId().toString();
        GAINED_NEW_ADVANCEMENT = true;
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent e) {
        boolean first = true;
        boolean wasCorrect = false;
        for(String arg : e.getParseResults().getContext().getArguments().keySet()) {
            if(first) {
                if(arg.matches("triggercommand")) wasCorrect = true;
                first = false;
            } else {
                if (wasCorrect && !arg.matches("any")) COMMAND_MAP.put(arg, true);
                break;
             }
        }
    }

    public static boolean commandHelper(Trigger trigger) {
        String id = trigger.getParameter("identifier");
        return COMMAND_MAP.containsKey(id) && COMMAND_MAP.get(id);
    }

    public static void commandFinish(Trigger trigger) {
        String id = trigger.getParameter("identifier");
        COMMAND_MAP.put(id,false);
    }

    @SubscribeEvent
    public static void clientDisconnected(PlayerEvent.PlayerLoggedOutEvent e) {
        IS_WORLD_RENDERED =false;
    }

    @SubscribeEvent
    public static void cancelRenders(RenderGuiOverlayEvent.Pre e) {
        if(!IS_WORLD_RENDERED) {
            ChannelManager.initializeServerInfo();
            IS_WORLD_RENDERED = true;
        }
        if(!SHOULD_RENDER_DEBUG) e.setCanceled(true);
    }

    public static void initReload() {
        Component reload = MutableComponent.create(AssetUtil.genericLang(Constants.MODID,"misc","reload_start",false))
                .withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC);
        if(Objects.nonNull(Minecraft.getInstance().player))
            Minecraft.getInstance().player.sendSystemMessage(reload);
        RELOAD_COUNTER = 5;
        ChannelManager.reloading = true;
        MusicTriggers.clearLog();
        MusicTriggers.logExternally(Level.INFO,"Reloading Music...");
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key e) {
        if(Channel.GUI.isDown() && Minecraft.getInstance().player!=null)
            Minecraft.getInstance().setScreen(Instance.createGui());
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
                    Component reload = MutableComponent.create(AssetUtil.genericLang(Constants.MODID, "misc", "reload_finished",false))
                            .withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.ITALIC);
                    if (Objects.nonNull(Minecraft.getInstance().player))
                        Minecraft.getInstance().player.sendSystemMessage(reload);
                    ChannelManager.reloading = false;
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public static void debugInfo(RenderGuiOverlayEvent e) {
        if(ConfigDebug.SHOW_DEBUG && IS_WORLD_RENDERED && SHOULD_RENDER_DEBUG) {
            List<String> lines = new ArrayList<>();
            lines.add("Music Triggers Debug Information");
            for(Channel channel : ChannelManager.getAllChannels()) {
                if (Objects.nonNull(channel.curPlayingName()))
                    lines.add("Channel[" + channel.getChannelName() + "] Current Song: " + channel.curPlayingName());
                if (!ConfigDebug.CURRENT_SONG_ONLY || ConfigDebug.ALLOW_TIMESTAMPS) {
                    if (!channel.formatSongTime().matches("No song playing"))
                        lines.add("Channel[" + channel.getChannelName() + "] Current Song Time: " + channel.formatSongTime());
                    if (Objects.nonNull(channel.formattedFadeOutTime()))
                        lines.add("Channel[" + channel.getChannelName() + "] Fading Out: " + channel.formattedFadeOutTime());
                    if (Objects.nonNull(channel.formattedFadeInTime()))
                        lines.add("Channel[" + channel.getChannelName() + "] Fading In: " + channel.formattedFadeInTime());
                }
                if (!ConfigDebug.CURRENT_SONG_ONLY) {
                    synchronized(channel.getPlayableTriggers()) {
                        if (!channel.getPlayableTriggers().isEmpty()) {
                            StringBuilder builder = new StringBuilder("Channel[" + channel.getChannelName() + "] Playable Events:");
                            boolean first = true;
                            for (Trigger trigger : channel.getPlayableTriggers()) {
                                String name = trigger.getNameWithID();
                                if(!first) {
                                    if (checkStringWidth(e.getWindow(), builder + " " + name)) {
                                        lines.add(builder.toString());
                                        builder = new StringBuilder("Channel[" + channel.getChannelName() + "] Playable Events:");
                                    }
                                } else first = false;
                                builder.append(" ").append(name);
                            }
                            lines.add(builder.toString());
                        }
                    }
                }
            }
            if (!ConfigDebug.CURRENT_SONG_ONLY) {
                StringBuilder builder = new StringBuilder("Blocked Mods:");
                boolean first = true;
                for (String blocked : ConfigDebug.FORMATTED_BLOCKED_MODS) {
                    if(!first) {
                        if (checkStringWidth(e.getWindow(), builder + " " + blocked)) {
                            lines.add(builder.toString());
                            builder = new StringBuilder("Blocked Mods:");
                        }
                    } else first = false;
                    builder.append(" ").append(blocked);
                }
                lines.add(builder.toString());
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer player = mc.player;
                if(Objects.nonNull(player)) {
                    net.minecraft.world.level.Level world = player.clientLevel;
                    if(Objects.nonNull(mc.screen))
                        lines.add("Current GUI Class Name: " + mc.screen.getClass().getName());
                    ResourceKey<Biome> biomeKey = world.getBiome(player.blockPosition()).unwrapKey().orElse(null);
                    lines.add("Current Biome Name: " + (Objects.nonNull(biomeKey) ? biomeKey.location().toString() : "Unknown Biome"));
                    lines.add("Current Dimension: " + world.dimension().location());
                    lines.add("Current Structure: " + ChannelManager.CUR_STRUCT);
                    lines.add("Current Total Light: " +  world.getRawBrightness(roundedPos(player), 0));
                    lines.add("Current Block Light: " + world.getBrightness(LightLayer.BLOCK, roundedPos(player)));
                    if (Objects.nonNull(MusicPicker.EFFECT_LIST) && !MusicPicker.EFFECT_LIST.isEmpty()) {
                        builder = new StringBuilder("Effect List:");
                        first = true;
                        for (String effect : MusicPicker.EFFECT_LIST) {
                            if(!first) {
                                if (checkStringWidth(e.getWindow(), builder + " " + effect)) {
                                    lines.add(builder.toString());
                                    builder = new StringBuilder("Effect List:");
                                }
                            }
                            else first = false;
                            builder.append(" ").append(effect);
                        }
                        lines.add(builder.toString());
                    }
                    Entity entity = mc.crosshairPickEntity;
                    if(Objects.nonNull(entity)) {
                        LivingEntity living = getLivingFromEntity(entity);
                        if (Objects.nonNull(living)) {
                            lines.add("Current Entity Name: " + living.getName().getString());
                            Optional<? extends Registry<EntityType<?>>> reg = world.registryAccess().registry(Registry.ENTITY_TYPE_REGISTRY);
                            reg.ifPresent(entityTypes -> lines.add("Current Entity ID: " + entityTypes.getKey(living.getType())));
                            String infernal = infernalChecker(living);
                            if(Objects.nonNull(infernal))
                                lines.add("Infernal Mob Mod Name: " + infernal);
                        }
                    }
                }
            }
            int top = 2;
            for (String msg : lines) {
                GuiComponent.fill(e.getPoseStack(), 1, top-1, 2+Minecraft.getInstance().font.width(msg)+1,
                        top + Minecraft.getInstance().font.lineHeight - 1, -1873784752);
                Minecraft.getInstance().font.draw(e.getPoseStack(), msg, 2, top, 14737632);
                top += Minecraft.getInstance().font.lineHeight;
            }
        }
    }

    private static boolean checkStringWidth(Window window, String s) {
        return (window.getGuiScaledWidth()*0.9f)<=Minecraft.getInstance().font.width(s);
    }

    private static BlockPos roundedPos(LocalPlayer p) {
        return new BlockPos((Math.round(p.getX() * 2) / 2.0), (Math.round(p.getY() * 2) / 2.0), (Math.round(p.getZ() * 2) / 2.0));
    }

    private static @Nullable String infernalChecker(LivingEntity entity) {
        if(!ModList.get().isLoaded("infernalmobs")) return null;
        return InfernalMobsCore.getIsRareEntityOnline(entity) ? InfernalMobsCore.getMobModifiers(entity).getModName() : null;
    }

    private static @Nullable LivingEntity getLivingFromEntity(Entity entity) {
        return entity instanceof LivingEntity ? (LivingEntity)entity : null;
    }
}
