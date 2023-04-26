package mods.thecomputerizer.musictriggers;

import com.mojang.blaze3d.platform.InputConstants;
import mods.thecomputerizer.musictriggers.client.ClientEvents;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.events.AdvancementEvent;
import mods.thecomputerizer.musictriggers.network.NetworkHandler;
import mods.thecomputerizer.musictriggers.registry.ItemRegistry;
import mods.thecomputerizer.musictriggers.registry.items.CustomRecord;
import mods.thecomputerizer.musictriggers.registry.items.MusicTriggersRecord;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Level;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static mods.thecomputerizer.musictriggers.MusicTriggers.configFile;

public class MusicTriggersClient implements ClientModInitializer {

    public static KeyMapping GUI;

    @Override
    public void onInitializeClient() {
        ConfigRegistry.initialize(new File(Constants.CONFIG_DIR,"registration.toml"),true);
        try {
            ChannelManager.initialize(configFile("channels", "toml"), true);
        } catch (IOException e) {
            MusicTriggers.logExternally(Level.FATAL,"Could not initialize channels!");
            throw new RuntimeException(e);
        }
        if(!ConfigRegistry.CLIENT_SIDE_ONLY) NetworkHandler.registerReceivers(true);
        setUpClientEvents();
        GUI = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.musictriggers.gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "key.categories.musictriggers"
        ));
        ItemProperties.register(ItemRegistry.MUSIC_TRIGGERS_RECORD, new ResourceLocation(Constants.MODID, "trigger"),
                (stack, level, living, id) -> {
                    if (stack.getOrCreateTag().contains("triggerID"))
                        return MusicTriggersRecord.mapTriggerToFloat(stack.getOrCreateTag().getString("triggerID"));
                    return 0f;
                });
        ItemProperties.register(ItemRegistry.CUSTOM_RECORD, new ResourceLocation(Constants.MODID, "custom_record"),
                (stack, level, living, id) -> {
                    if (stack.getOrCreateTag().contains("triggerID"))
                        return CustomRecord.mapTriggerToFloat(stack.getOrCreateTag().getString("channelFrom"),
                                stack.getOrCreateTag().getString("triggerID"));
                    return 0f;
                });
        ChannelManager.reloading = false;
    }

    private static void setUpClientEvents() {

        ClientLoginConnectionEvents.DISCONNECT.register((handler, client) -> ClientEvents.onDisconnect());

        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> ClientEvents.debugInfo(matrixStack));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (GUI.isDown()) ClientEvents.onKeyInput();
            ClientEvents.onTick();
        });

        AdvancementEvent.EVENT.register((ClientEvents::onAdvancement));

        ServerLifecycleEvents.SERVER_STARTING.register((server) -> ChannelManager.readResourceLocations());

        Runnable tickTimer = ChannelManager::tickChannels;
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(tickTimer, 0L, 50, TimeUnit.MILLISECONDS);
    }
}
