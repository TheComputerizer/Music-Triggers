package mods.thecomputerizer.musictriggers.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import mods.thecomputerizer.musictriggers.core.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.channels.Channel;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.GuiSuperType;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.Display;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("deprecation")
@Mod.EventBusSubscriber(modid = Constants.MODID, value = Side.CLIENT)
public class ClientEvents {
    public static int RELOAD_COUNTER = 0;
    public static String LAST_ADVANCEMENT;
    public static boolean GAINED_NEW_ADVANCEMENT;
    public static boolean SHOULD_RENDER_DEBUG = true;
    public static final Map<String, Boolean> COMMAND_MAP = new HashMap<>();
    public static boolean IS_DISPLAY_FOCUSED = true;
    private static final List<Class<? extends GuiScreen>> ERRORED_GUI_CLASSES = new ArrayList<>();

    @SubscribeEvent
    public static void playSound(PlaySoundEvent event) {
        if(Objects.isNull(event.getSound())) return;
        ResourceLocation location = event.getSound().getSoundLocation();
        SoundCategory category = event.getSound().getCategory();
        PositionedSoundRecord silenced = new PositionedSoundRecord(location,category,
                1f/1000000f,1f,false,1,ISound.AttenuationType.LINEAR,0,0,0);
        for(Map.Entry<String,HashSet<String>> modEntry : ConfigDebug.FORMATTED_BLOCKED_MODS.entrySet()) {
            if(!modEntry.getKey().matches("all") && !location.getNamespace().matches(modEntry.getKey())) continue;
            for(String categoryMatch : modEntry.getValue()) {
                if(category.getName().toLowerCase().matches(categoryMatch.toLowerCase())) {
                    if(ChannelManager.handleSoundEventOverride(event.getSound().getSound(),category)) {
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
    public static void clientDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
        ChannelManager.onClientLogout();
    }

    @SubscribeEvent
    public static void cancelRenders(RenderGameOverlayEvent.Pre e) {
        if(e.getType()==RenderGameOverlayEvent.ElementType.ALL && !SHOULD_RENDER_DEBUG) e.setCanceled(true);
    }

    public static void initReload() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if(Objects.nonNull(player)) player.sendMessage(new TextComponentString(
                I18n.translateToLocal("misc.musictriggers.reload_start"))
                .setStyle(new Style().setItalic(true).setColor(TextFormatting.RED)));
        RELOAD_COUNTER = 5;
        ChannelManager.reloading = true;
        MusicTriggers.clearLog();
        MusicTriggers.logExternally(Level.INFO,"Reloading Music...");
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent e) {
        if(Channel.GUI.isKeyDown() && ChannelManager.isButtonEnabled("gui"))
            Minecraft.getMinecraft().displayGuiScreen(Instance.createGui(null));
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if(event.phase==TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getMinecraft();
            IS_DISPLAY_FOCUSED = Display.isCreated() && Display.isActive();
            if(!mc.isGamePaused() && !(mc.currentScreen instanceof GuiSuperType) && !SHOULD_RENDER_DEBUG)
                SHOULD_RENDER_DEBUG = true;
            if(RELOAD_COUNTER>0) {
                RELOAD_COUNTER -= 1;
                if(RELOAD_COUNTER == 1) {
                    ChannelManager.reloadAllChannels();
                    if(Objects.nonNull(mc.player)) mc.player.sendMessage(new TextComponentString(
                            I18n.translateToLocal("misc.musictriggers.reload_finished"))
                            .setStyle(new Style().setItalic(true).setColor(TextFormatting.GREEN)));
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
                if(Objects.nonNull(curPlaying))
                    addDebug(e,channel.getLogMessage("Current Song: {}"),curPlaying);
                if(!ConfigDebug.CURRENT_SONG_ONLY || ConfigDebug.ALLOW_TIMESTAMPS) {
                    String time = channel.formatSongTime();
                    String fadeOut = channel.formattedFadeOutTime();
                    String fadeIn = channel.formattedFadeInTime();
                    if(!time.matches("No song playing"))
                        addDebug(e,channel.getLogMessage("Current Song Time: {}"),time);
                    if(Objects.nonNull(fadeOut))
                        addDebug(e,channel.getLogMessage("Fading Out: {}"),fadeOut);
                    if(Objects.nonNull(fadeIn))
                        addDebug(e,channel.getLogMessage("Fading In: {}"),fadeIn);
                }
                if(!ConfigDebug.CURRENT_SONG_ONLY) {
                    synchronized(channel.getPlayableTriggers()) {
                        if(!channel.getPlayableTriggers().isEmpty()) {
                            StringBuilder builder = new StringBuilder(channel.getLogMessage("Playable Events:"));
                            boolean first = true;
                            for(Trigger trigger : channel.getPlayableTriggers()) {
                                String name = trigger.getNameWithID();
                                if(!first) {
                                    if(checkStringWidth(e.getResolution(),builder+" "+name)) {
                                        addDebug(e,builder.toString());
                                        builder = new StringBuilder(channel.getLogMessage("Playable Events:"));
                                    }
                                } else first = false;
                                builder.append(" ").append(name);
                            }
                            addDebug(e,builder.toString());
                        }
                    }
                }
            }
            if(!ConfigDebug.CURRENT_SONG_ONLY) {
                StringBuilder builder = new StringBuilder("Blocked Mods:");
                boolean first = true;
                for(Map.Entry<String,HashSet<String>> modEntry : ConfigDebug.FORMATTED_BLOCKED_MODS.entrySet()) {
                    String blocked = modEntry.getKey()+"["+ TextUtil.listToString(modEntry.getValue(),",")+"]";
                    if(!first) {
                        if (checkStringWidth(e.getResolution(), builder + " " + blocked)) {
                            addDebug(e,builder.toString());
                            builder = new StringBuilder("Blocked Mods:");
                        }
                    } else first = false;
                    builder.append(" ").append(blocked);
                }
                addDebug(e,builder.toString());
                Minecraft mc = Minecraft.getMinecraft();
                EntityPlayerSP player = mc.player;
                if(Objects.nonNull(player)) {
                    World world = player.getEntityWorld();
                    tryGuiClassDebug(e,mc.currentScreen);
                    addDebug(e,"Current Biome Name: {}",world.getBiome(player.getPosition()).getRegistryName());
                    addDebug(e,"Current Biome Category: {}",world.getBiome(player.getPosition()).getRegistryName());
                    addDebug(e,"Current Dimension: {}",player.dimension);
                    addDebug(e,"Current Total Light: {}",world.getLight(roundedPos(player), true));
                    addDebug(e,"Current Block Light: {}",world.getLightFor(EnumSkyBlock.BLOCK, roundedPos(player)));
                    Set<String> effectSet = Trigger.getCachedEffects();
                    if(!effectSet.isEmpty()) {
                        builder = new StringBuilder("Effect List:");
                        first = true;
                        for(String effect : effectSet) {
                            if(!first) {
                                if(checkStringWidth(e.getResolution(),builder+" "+effect)) {
                                    addDebug(e,builder.toString());
                                    builder = new StringBuilder("Effect List:");
                                }
                            }
                            else first = false;
                            builder.append(" ").append(effect);
                        }
                        addDebug(e,builder.toString());
                    }
                    RayTraceResult res = mc.objectMouseOver;
                    if(Objects.nonNull(res)) {
                        EntityLiving entity = getLivingFromEntity(res.entityHit);
                        if (Objects.nonNull(entity)) {
                            addDebug(e,"Current Entity Name: " + entity.getName());
                            addDebug(e,"Current Entity ID: " + EntityList.getKey(entity));
                            String infernal = infernalChecker(entity);
                            if(Objects.nonNull(infernal))
                                addDebug(e,"Infernal Mob Mod Name: " + infernal);
                        } else if(res.typeOfHit == RayTraceResult.Type.BLOCK) {
                            BlockPos pos = res.getBlockPos();
                            TileEntity tile = mc.world.getTileEntity(pos);
                            if(Objects.nonNull(tile))
                                addDebug(e,"Current Tile Name: " + TileEntity.getKey(tile.getClass()));
                        }
                    }
                }
            }
        }
    }

    private static void tryGuiClassDebug(RenderGameOverlayEvent.Text e, @Nullable GuiScreen screen) {
        if(Objects.nonNull(screen) && !ERRORED_GUI_CLASSES.contains(screen.getClass())) {
            try {
                addDebug(e,"Current GUI Class Name: {}",screen.getClass().getName());
            } catch(IllegalArgumentException ignored) {
                MusicTriggers.logBoth(Level.ERROR, "There was an error displaying the name for GUI "+
                        "class '{}' in the debug info! Class will be ignored",null,screen.getClass());
                ERRORED_GUI_CLASSES.add(screen.getClass());
            }
        }
    }

    private static void addDebug(RenderGameOverlayEvent.Text e, String msg, Object ... parameters) {
        e.getLeft().add(LogUtil.injectParameters(msg,parameters));
    }

    private static boolean checkStringWidth(ScaledResolution res, String s) {
        return (res.getScaledWidth()*0.9f)<=Minecraft.getMinecraft().fontRenderer.getStringWidth(s);
    }

    private static BlockPos roundedPos(EntityPlayer p) {
        return new BlockPos((Math.round(p.posX*2d)/2f),(Math.round(p.posY*2d)/2f),(Math.round(p.posZ*2d)/2f));
    }

    private static @Nullable String infernalChecker(EntityLiving entity) {
        if(!Loader.isModLoaded("infernalmobs")) return null;
        return InfernalMobsCore.getIsRareEntity(entity) ? InfernalMobsCore.getMobModifiers(entity).getModName() : null;
    }

    private static @Nullable EntityLiving getLivingFromEntity(Entity entity) {
        return entity instanceof EntityLiving ? (EntityLiving)entity : null;
    }
}
