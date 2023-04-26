package mods.thecomputerizer.musictriggers.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.GuiSuperType;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public class ClientEvents {
    public static boolean IS_WORLD_RENDERED = false;
    public static int RELOAD_COUNTER = 0;
    public static String LAST_ADVANCEMENT;
    public static boolean GAINED_NEW_ADVANCEMENT;
    public static boolean SHOULD_RENDER_DEBUG = true;
    public static final HashMap<String, Boolean> COMMAND_MAP = new HashMap<>();

    @SubscribeEvent
    public static void playSound(PlaySoundEvent e) {
        if(Objects.isNull(e.getSound())) return;
        SimpleSound silenced = new SimpleSound(e.getSound().getLocation(), e.getSound().getSource(),
                Float.MIN_VALUE*10000, 1F, false, 0, ISound.AttenuationType.NONE,
                0F, 0F, 0F,true);
        for(String s : ConfigDebug.BLOCKED_MOD_CATEGORIES) {
            String modid = s.contains(";") ? s.substring(0,s.indexOf(';')) : s;
            if(!e.getSound().getLocation().getNamespace().matches(modid)) continue;
            String categoryName = s.contains(";") && s.indexOf(';')+1<s.length() ? s.substring(s.indexOf(';')+1) : "music";
            if(e.getSound().getLocation().getPath().matches(categoryName))
                if(ChannelManager.handleSoundEventOverride(e.getSound()))
                    e.setResultSound(silenced);
        }
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent e) {
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
    public static void cancelRenders(RenderGameOverlayEvent.Pre e) {
        if(!IS_WORLD_RENDERED) {
            ChannelManager.initializeServerInfo();
            IS_WORLD_RENDERED = true;
        }
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
                    IFormattableTextComponent reload = AssetUtil.genericLang(Constants.MODID, "misc", "reload_finished",false)
                            .withStyle(TextFormatting.GREEN).withStyle(TextFormatting.ITALIC);
                    if (Objects.nonNull(Minecraft.getInstance().player))
                        Minecraft.getInstance().player.sendMessage(reload, Minecraft.getInstance().player.getUUID());
                    ChannelManager.reloading = false;
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public static void debugInfo(RenderGameOverlayEvent.Text e) {
        if(ConfigDebug.SHOW_DEBUG && IS_WORLD_RENDERED && SHOULD_RENDER_DEBUG) {
            e.getLeft().add("Music Triggers Debug Information");
            for(Channel channel : ChannelManager.getAllChannels()) {
                if (Objects.nonNull(channel.curPlayingName()))
                    e.getLeft().add("Channel[" + channel.getChannelName() + "] Current Song: " + channel.curPlayingName());
                if (!ConfigDebug.CURRENT_SONG_ONLY || ConfigDebug.ALLOW_TIMESTAMPS) {
                    if (!channel.formatSongTime().matches("No song playing"))
                        e.getLeft().add("Channel[" + channel.getChannelName() + "] Current Song Time: " + channel.formatSongTime());
                    if (Objects.nonNull(channel.formattedFadeOutTime()))
                        e.getLeft().add("Channel[" + channel.getChannelName() + "] Fading Out: " + channel.formattedFadeOutTime());
                    if (Objects.nonNull(channel.formattedFadeInTime()))
                        e.getLeft().add("Channel[" + channel.getChannelName() + "] Fading In: " + channel.formattedFadeInTime());
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
                                        e.getLeft().add(builder.toString());
                                        builder = new StringBuilder("Channel[" + channel.getChannelName() + "] Playable Events:");
                                    }
                                } else first = false;
                                builder.append(" ").append(name);
                            }
                            e.getLeft().add(builder.toString());
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
                            e.getLeft().add(builder.toString());
                            builder = new StringBuilder("Blocked Mods:");
                        }
                    } else first = false;
                    builder.append(" ").append(blocked);
                }
                e.getLeft().add(builder.toString());
                Minecraft mc = Minecraft.getInstance();
                ClientPlayerEntity player = mc.player;
                if(Objects.nonNull(player)) {
                    World world = player.clientLevel;
                    if(Objects.nonNull(mc.screen))
                        e.getLeft().add("Current GUI Class Name: " + mc.screen.getClass().getName());
                    e.getLeft().add("Current Biome Name: " + world.getBiome(player.blockPosition()).getRegistryName());
                    e.getLeft().add("Current Biome Category: " + world.getBiome(player.blockPosition()).getBiomeCategory().getName());
                    e.getLeft().add("Current Dimension: " + world.dimension().location());
                    e.getLeft().add("Current Structure: " + ChannelManager.CUR_STRUCT);
                    e.getLeft().add("Current Total Light: " +  world.getRawBrightness(roundedPos(player), 0));
                    e.getLeft().add("Current Block Light: " + world.getBrightness(LightType.BLOCK, roundedPos(player)));
                    if (Objects.nonNull(MusicPicker.EFFECT_LIST) && !MusicPicker.EFFECT_LIST.isEmpty()) {
                        builder = new StringBuilder("Effect List:");
                        first = true;
                        for (String effect : MusicPicker.EFFECT_LIST) {
                            if(!first) {
                                if (checkStringWidth(e.getWindow(), builder + " " + effect)) {
                                    e.getLeft().add(builder.toString());
                                    builder = new StringBuilder("Effect List:");
                                }
                            }
                            else first = false;
                            builder.append(" ").append(effect);
                        }
                        e.getLeft().add(builder.toString());
                    }
                    Entity entity = mc.crosshairPickEntity;
                    if(Objects.nonNull(entity)) {
                        LivingEntity living = getLivingFromEntity(entity);
                        if (Objects.nonNull(living)) {
                            e.getLeft().add("Current Entity Name: " + living.getName().getString());
                            e.getLeft().add("Current Entity ID: " + ForgeRegistries.ENTITIES.getKey(entity.getType()));
                            String infernal = infernalChecker(living);
                            if(Objects.nonNull(infernal))
                                e.getLeft().add("Infernal Mob Mod Name: " + infernal);
                        }
                    }
                }
            }
        }
    }

    private static boolean checkStringWidth(MainWindow window, String s) {
        return (window.getGuiScaledWidth()*0.9f)<=Minecraft.getInstance().font.width(s);
    }

    private static BlockPos roundedPos(PlayerEntity p) {
        return new BlockPos((Math.round(p.getX() * 2) / 2.0), (Math.round(p.getY() * 2) / 2.0), (Math.round(p.getZ() * 2) / 2.0));
    }

    private static @Nullable String infernalChecker(LivingEntity entity) {
        if(!ModList.get().isLoaded("infernalmobs")) return null;
        return InfernalMobsCore.getIsRareEntity(entity) ? InfernalMobsCore.getMobModifiers(entity).getModName() : null;
    }

    private static @Nullable LivingEntity getLivingFromEntity(Entity entity) {
        return entity instanceof LivingEntity ? (LivingEntity)entity : null;
    }
}
