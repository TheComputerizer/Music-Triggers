package mods.thecomputerizer.musictriggers.api.client;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.gui.MTGUIScreen;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.ResourceContext;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.MinecraftAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.event.events.*;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.advancement.AdvancementAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.block.BlockStateAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.EventHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.events.CustomTickEventWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.events.PlayerAdvancementEventWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.events.PlayerInteractBlockEventWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextStyleAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.util.CustomTick;
import mods.thecomputerizer.theimpossiblelibrary.api.world.WorldAPI;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

import static mods.thecomputerizer.musictriggers.api.client.gui.MTGUIScreen.GUI_KEY;
import static mods.thecomputerizer.musictriggers.api.client.gui.MTGUIScreen.isActive;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.event.ClientEventWrapper.ClientType.*;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.event.types.ClientOverlayEventType.OverlayType.ALL;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.event.CommonEventWrapper.CommonType.*;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.event.types.CommonTickableEventType.TickPhase.END;

public class MTClientEvents {

    private static AdvancementAPI<?> recentAdvancement;
    private static int ticksUntilReload = -1;
    
    public static boolean checkAdvancement(ResourceContext ctx) {
        if(Objects.isNull(recentAdvancement)) return false;
        ResourceLocationAPI<?> id = recentAdvancement.getID();
        if(ctx.checkMatch(id.toString(),id.getPath())) {
            recentAdvancement = null;
            return true;
        }
        return false;
    }
    
    public static void handleError(@Nullable MinecraftAPI<?> mc, String channel) {
        if(Objects.nonNull(mc)) {
            mc.sendMessageToPlayer(getReloadMessage(
                    "error",new Object[]{channel},TextStyleAPI::italics,TextStyleAPI::darkRed));
            queueReload(mc,100);
        }
    }
    
    public static void init() {
        MTRef.logInfo("Initializing client event invokers {}",ClientHelper.getMinecraft());
        EventHelper.addListener(CLIENT_CONNECTED,MTClientEvents::onClientConnected);
        EventHelper.addListener(CLIENT_DISCONNECTED,MTClientEvents::onClientDisconnected);
        EventHelper.addListener(CUSTOM_TICK,MTClientEvents::onCustomTick);
        EventHelper.addListener(KEY_INPUT,MTClientEvents::onKeyPress);
        EventHelper.addListener(PLAYER_ADVANCEMENT,MTClientEvents::onAdvancement);
        EventHelper.addListener(PLAYER_INTERACT_BLOCK,MTClientEvents::onRightClickBlock);
        EventHelper.addListener(RENDER_OVERLAY_PRE,MTClientEvents::onRenderOverlayPre);
        EventHelper.addListener(RENDER_OVERLAY_TEXT,MTClientEvents::onRenderOverlayText);
        EventHelper.addListener(SOUND_PLAY,MTClientEvents::onPlaySound);
        EventHelper.addListener(TICK_CLIENT,MTClientEvents::onClientTick);
        CustomTick.addCustomTickTPS(ChannelHelper.getTickRate());
    }

    private static void onAdvancement(PlayerAdvancementEventWrapper<?> wrapper) {
        recentAdvancement = wrapper.getAdvancement();
    }

    private static void onClientConnected(ClientConnectedEventWrapper<?> wrapper) {
        ChannelHelper.onClientConnected();
    }

    private static void onClientDisconnected(ClientDisconnectedEventWrapper<?> wrapper) {
        ChannelHelper.onClientDisconnected();
    }

    private static void onClientTick(ClientTickEventWrapper<?> wrapper) {
        if(wrapper.isPhase(END)) {
            MinecraftAPI<?> mc = wrapper.getMinecraft();
            if(ticksUntilReload>=0) {
                if(ticksUntilReload==0) {
                    ChannelHelper.reload();
                    mc.sendMessageToPlayer(getReloadMessage("finished",null,
                            TextStyleAPI::italics,TextStyleAPI::green));
                }
                ticksUntilReload--;
            }
        }
    }

    private static void onCustomTick(CustomTickEventWrapper<?> wrapper) {
        ChannelHelper.tick(wrapper.getTicker());
    }
    
    public static void onKeyPress(InputKeyEventWrapper<?> wrapper) {
        if(GUI_KEY.isDown()) MTGUIScreen.open();
    }

    private static void onPlaySound(PlaySoundEventWrapper<?> wrapper) {}

    private static void onRenderOverlayPre(RenderOverlayPreEventWrapper<?> wrapper) {
        if(wrapper.isType(ALL) && isActive) wrapper.setCanceled(true);
    }

    private static void onRenderOverlayText(RenderOverlayTextEventWrapper<?> wrapper) {
        if(!isActive) {
            ChannelHelper helper = ChannelHelper.getClientHelper();
            if(Objects.nonNull(helper) && ChannelHelper.getDebugBool("enable_debug_info")) {
                MinecraftAPI<?> mc = wrapper.getMinecraft();
                helper.getDebugInfo().toLines(mc.getFont(),(int)mc.getWindow().getWidthF(),wrapper.getLeft());
            }
        }
    }
    
    private static void onRightClickBlock(PlayerInteractBlockEventWrapper<?> wrapper) {
        WorldAPI<?> world = wrapper.getPlayer().getWorld();
        if(world.isClient()) {
            BlockStateAPI<?> state = world.getStateAt(wrapper.getPos());
            if("jukebox".equals(state.getBlock().getRegistryName().getPath()) && state.getPropertyBool("has_record"))
                ChannelHelper.getClientHelper().stopJukeboxAt(wrapper.getPos());
        }
    }

    public static void queueReload(@Nullable MinecraftAPI<?> mc, int ticks) {
        ScreenHelper.open((ScreenAPI)null);
        if(ChannelHelper.getLoader().isLoading()) {
            ChannelHelper.logGlobalWarn("Tried to reload channels while they were already being reloaded");
            return;
        }
        if(Objects.nonNull(mc))
            mc.sendMessageToPlayer(getReloadMessage("queue",new Object[]{ticks},
                    TextStyleAPI::italics,TextStyleAPI::red));
        ChannelHelper.onReloadQueued(true);
        ticksUntilReload = ticks;
    }

    @SafeVarargs
    private static <S> TextAPI<?> getReloadMessage(
            String type, @Nullable Object[] args, Function<TextStyleAPI<S>,S> ... styleFuncs) {
        return MTClient.getStyledTranslated("message","reload."+type,args,styleFuncs);
    }
}