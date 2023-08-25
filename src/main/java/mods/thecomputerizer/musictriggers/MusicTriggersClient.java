package mods.thecomputerizer.musictriggers;

import com.mojang.blaze3d.platform.InputConstants;
import mods.thecomputerizer.musictriggers.client.ClientEvents;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.registry.ItemRegistry;
import mods.thecomputerizer.musictriggers.registry.items.CustomRecord;
import mods.thecomputerizer.musictriggers.registry.items.MusicTriggersRecord;
import mods.thecomputerizer.theimpossiblelibrary.events.AdvancementEvents;
import mods.thecomputerizer.theimpossiblelibrary.events.CustomTick;
import mods.thecomputerizer.theimpossiblelibrary.events.PlaySoundEvent;
import mods.thecomputerizer.theimpossiblelibrary.events.ResourcesLoadedEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.item.ItemProperties;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;

import static mods.thecomputerizer.musictriggers.MusicTriggers.configFile;

public class MusicTriggersClient implements ClientModInitializer {

    public static KeyMapping GUI;

    @Override
    public void onInitializeClient() {
        setUpClientEvents();
        GUI = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.musictriggers.gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "key.categories.musictriggers"
        ));
        ItemProperties.register(ItemRegistry.MUSIC_TRIGGERS_RECORD, Constants.res("trigger"),
                (stack, level, living, id) -> {
                    if (stack.getOrCreateTag().contains("triggerID"))
                        return MusicTriggersRecord.mapTriggerToFloat(stack.getOrCreateTag().getString("triggerID"));
                    return 0f;
                });
        ItemProperties.register(ItemRegistry.CUSTOM_RECORD, Constants.res("custom_record"),
                (stack, level, living, id) -> {
                    if (stack.getOrCreateTag().contains("triggerID"))
                        return CustomRecord.mapTriggerToFloat(stack.getOrCreateTag().getString("channelFrom"),
                                stack.getOrCreateTag().getString("triggerID"));
                    return 0f;
                });
    }

    public void onResourcesLoaded() {
        try {
            ChannelManager.initClient(configFile("channels", "toml"), true);
            ChannelManager.readResourceLocations();
            ChannelManager.reloading = false;
        } catch (IOException ex) {
            throw new RuntimeException("Caught a fatal error in Music Triggers configuration registration! Please " +
                    "report this and make sure to include the full crash report.",ex);
        }
    }

    private void setUpClientEvents() {
        ResourcesLoadedEvent.EVENT.register(this::onResourcesLoaded);
        PlaySoundEvent.EVENT.register(ClientEvents::playSound);
        AdvancementEvents.CLIENT_GRANTED.register(ClientEvents::onAdvancement);
        ClientLoginConnectionEvents.DISCONNECT.register((handler,minecraft) -> ClientEvents.clientDisconnected());
        HudRenderCallback.EVENT.register((matrix,partialTick) -> ClientEvents.debugInfo(matrix));
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            if (GUI.isDown()) ClientEvents.onKeyInput();
            ClientEvents.onTick();
        });
        CustomTick.registerMillis(50,ChannelManager::tickChannels);
    }
}