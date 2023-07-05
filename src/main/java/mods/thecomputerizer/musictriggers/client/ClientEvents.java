package mods.thecomputerizer.musictriggers.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.channels.Channel;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.GuiSuperType;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.musictriggers.server.TriggerCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
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
import net.minecraftforge.event.CommandEvent;
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
import java.util.HashMap;
import java.util.Objects;

@SuppressWarnings("deprecation")
@Mod.EventBusSubscriber(modid = Constants.MODID, value = Side.CLIENT)
public class ClientEvents {
    public static boolean IS_WORLD_RENDERED = false;
    public static int RELOAD_COUNTER = 0;
    public static String LAST_ADVANCEMENT;
    public static boolean GAINED_NEW_ADVANCEMENT;
    public static boolean SHOULD_RENDER_DEBUG = true;
    public static final HashMap<String, Boolean> COMMAND_MAP = new HashMap<>();
    public static boolean IS_DISPLAY_FOCUSED = true;

    @SubscribeEvent
    public static void playSound(PlaySoundEvent e) {
        if(Objects.isNull(e.getSound())) return;
        PositionedSoundRecord silenced = new PositionedSoundRecord(e.getSound().getSoundLocation(), e.getSound().getCategory(),
                Float.MIN_VALUE*10000, 1F, false, 1, ISound.AttenuationType.LINEAR, 0F, 0F, 0F);
        for(String s : ConfigDebug.BLOCKED_MOD_CATEGORIES) {
            String modid = s.contains(";") ? s.substring(0,s.indexOf(';')) : s;
            if(!e.getSound().getSoundLocation().getNamespace().matches(modid)) continue;
            String categoryName = s.contains(";") && s.indexOf(';')+1<s.length() ? s.substring(s.indexOf(';')+1) : "music";
            if(e.getSound().getCategory().getName().matches(categoryName)) {
                if(ChannelManager.handleSoundEventOverride(e.getSound())) {
                    e.setResultSound(silenced);
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent e) {
        LAST_ADVANCEMENT = e.getAdvancement().getId().toString();
        GAINED_NEW_ADVANCEMENT = true;
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent e) {
        if(e.getCommand() instanceof TriggerCommand) {
            TriggerCommand command = (TriggerCommand) e.getCommand();
            if(!command.getIdentifier().matches("any")) COMMAND_MAP.put(command.getIdentifier(),true);
        }
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
        IS_WORLD_RENDERED=false;
        ChannelManager.onClientLogout();
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
        Minecraft.getMinecraft().player.sendMessage(new TextComponentString(
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
            Minecraft.getMinecraft().displayGuiScreen(Instance.createGui());
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if(event.phase== TickEvent.Phase.END) {
            IS_DISPLAY_FOCUSED = Display.isCreated() && Display.isActive();
            if (!Minecraft.getMinecraft().isGamePaused() && !(Minecraft.getMinecraft().currentScreen instanceof GuiSuperType) && !SHOULD_RENDER_DEBUG)
                SHOULD_RENDER_DEBUG = true;
            if (RELOAD_COUNTER > 0) {
                RELOAD_COUNTER -= 1;
                if (RELOAD_COUNTER == 1) {
                    ChannelManager.reloadAllChannels();
                    Minecraft.getMinecraft().player.sendMessage(new TextComponentString(
                            I18n.translateToLocal("misc.musictriggers.reload_finished"))
                            .setStyle(new Style().setItalic(true).setColor(TextFormatting.GREEN)));
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
                                    if (checkStringWidth(e.getResolution(), builder + " " + name)) {
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
                        if (checkStringWidth(e.getResolution(), builder + " " + blocked)) {
                            e.getLeft().add(builder.toString());
                            builder = new StringBuilder("Blocked Mods:");
                        }
                    } else first = false;
                    builder.append(" ").append(blocked);
                }
                e.getLeft().add(builder.toString());
                Minecraft mc = Minecraft.getMinecraft();
                EntityPlayer player = mc.player;
                if(Objects.nonNull(player)) {
                    World world = player.getEntityWorld();
                    if(Objects.nonNull(mc.currentScreen))
                        e.getLeft().add("Current GUI Class Name: " + mc.currentScreen.getClass().getName());
                    e.getLeft().add("Current Biome Name: " + world.getBiome(player.getPosition()).getRegistryName());
                    e.getLeft().add("Current Biome Category: " + world.getBiome(player.getPosition()).getTempCategory());
                    e.getLeft().add("Current Dimension: " + player.dimension);
                    e.getLeft().add("Current Total Light: " + world.getLight(roundedPos(player), true));
                    e.getLeft().add("Current Block Light: " + world.getLightFor(EnumSkyBlock.BLOCK, roundedPos(player)));
                    if (Objects.nonNull(MusicPicker.EFFECT_LIST) && !MusicPicker.EFFECT_LIST.isEmpty()) {
                        builder = new StringBuilder("Effect List:");
                        first = true;
                        for (String effect : MusicPicker.EFFECT_LIST) {
                            if(!first) {
                                if (checkStringWidth(e.getResolution(), builder + " " + effect)) {
                                    e.getLeft().add(builder.toString());
                                    builder = new StringBuilder("Effect List:");
                                }
                            }
                            else first = false;
                            builder.append(" ").append(effect);
                        }
                        e.getLeft().add(builder.toString());
                    }
                    RayTraceResult res = mc.objectMouseOver;
                    if(Objects.nonNull(res)) {
                        EntityLiving entity = getLivingFromEntity(res.entityHit);
                        if (Objects.nonNull(entity)) {
                            e.getLeft().add("Current Entity Name: " + entity.getName());
                            e.getLeft().add("Current Entity ID: " + EntityList.getKey(entity));
                            String infernal = infernalChecker(entity);
                            if(Objects.nonNull(infernal))
                                e.getLeft().add("Infernal Mob Mod Name: " + infernal);
                        } else if(res.typeOfHit == RayTraceResult.Type.BLOCK) {
                            BlockPos pos = res.getBlockPos();
                            TileEntity tile = mc.world.getTileEntity(pos);
                            if(Objects.nonNull(tile))
                                e.getLeft().add("Current Tile Name: " + TileEntity.getKey(tile.getClass()));
                        }
                    }
                }
            }
        }
    }

    private static boolean checkStringWidth(ScaledResolution res, String s) {
        return (res.getScaledWidth()*0.9f)<=Minecraft.getMinecraft().fontRenderer.getStringWidth(s);
    }

    private static BlockPos roundedPos(EntityPlayer p) {
        return new BlockPos((Math.round(p.posX * 2) / 2.0), (Math.round(p.posY * 2) / 2.0), (Math.round(p.posZ * 2) / 2.0));
    }

    private static @Nullable String infernalChecker(EntityLiving entity) {
        if(!Loader.isModLoaded("infernalmobs")) return null;
        return InfernalMobsCore.getIsRareEntity(entity) ? InfernalMobsCore.getMobModifiers(entity).getModName() : null;
    }

    private static @Nullable EntityLiving getLivingFromEntity(Entity entity) {
        return entity instanceof EntityLiving ? (EntityLiving)entity : null;
    }
}
