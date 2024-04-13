package mods.thecomputerizer.musictriggers.api.client;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.MinecraftAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.event.events.*;
import mods.thecomputerizer.theimpossiblelibrary.api.common.advancement.AdvancementAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.EventHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.events.CustomTickEventWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.events.PlayerAdvancementEventWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextStyleAPI;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

import static mods.thecomputerizer.theimpossiblelibrary.api.client.event.ClientEventWrapper.ClientType.*;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.event.types.ClientOverlayEventType.OverlayType.ALL;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.event.CommonEventWrapper.CommonType.*;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.event.types.CommonTickableEventType.TickPhase.END;

public class MTClientEvents {

    private static MTDebugInfo debugInfo;
    private static AdvancementAPI<?> recentAdvancement;
    private static boolean renderOverlays = true;
    private static int ticksUntilReload = -1;

    public static void init(MTDebugInfo debug) {
        debugInfo = debug;
        EventHelper.addListener(PLAYER_ADVANCEMENT,MTClientEvents::onAdvancement);
        EventHelper.addListener(CLIENT_CONNECTED,MTClientEvents::onClientConnected);
        EventHelper.addListener(CLIENT_DISCONNECTED,MTClientEvents::onClientDisconnected);
        EventHelper.addListener(TICK_CLIENT,MTClientEvents::onClientTick);
        EventHelper.addListener(CUSTOM_TICK,MTClientEvents::onCustomTick);
        EventHelper.addListener(KEY_INPUT,MTClientEvents::onInputKey);
        EventHelper.addListener(SOUND_PLAY,MTClientEvents::onPlaySound);
        EventHelper.addListener(RENDER_OVERLAY_PRE,MTClientEvents::onRenderOverlayPre);
        EventHelper.addListener(RENDER_OVERLAY_TEXT,MTClientEvents::onRenderOverlayText);
    }

    private static boolean isMTScreenActive(MinecraftAPI mc) { //TODO Implement this
        return false;
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
            MinecraftAPI mc = wrapper.getMinecraft();
            if(!renderOverlays && mc.isUnpausedAndFocused()) renderOverlays = true;
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

    private static void onInputKey(InputKeyEventWrapper<?> wrapper) {

    }

    private static void onPlaySound(PlaySoundEventWrapper<?,?> wrapper) {

    }

    private static void onRenderOverlayPre(RenderOverlayPreEventWrapper<?> wrapper) {
        if(wrapper.isType(ALL) && !renderOverlays) wrapper.setCanceled(true);
    }

    private static void onRenderOverlayText(RenderOverlayTextEventWrapper<?> wrapper) {
        if(renderOverlays && Objects.nonNull(debugInfo)) {
            MinecraftAPI mc = wrapper.getMinecraft();
            if(Objects.nonNull(mc)) debugInfo.toLines(mc.getFont(),(int)mc.getWindow().getWidthF(),wrapper.getLeft());
        }
    }

    public static void queueReload(@Nullable MinecraftAPI mc, int ticks) { //TODO mark ChannelHelper as reloading & save log position
        if(Objects.nonNull(mc))
            mc.sendMessageToPlayer(getReloadMessage("queue",new Object[]{ticks},
                    TextStyleAPI::italics,TextStyleAPI::red));
        ChannelHelper.onReloadQueued();
        ticksUntilReload = ticks;
    }

    @SafeVarargs
    private static <S> TextAPI<?> getReloadMessage(
            String type, @Nullable Object[] args, Function<TextStyleAPI<S>,S> ... styleFuncs) {
        return MTClient.getStyledTranslated("message","reload."+type,args,styleFuncs);
    }
}