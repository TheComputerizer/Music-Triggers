package mods.thecomputerizer.musictriggers;

import com.mojang.blaze3d.platform.InputConstants;
import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.common.MusicTriggersItems;
import mods.thecomputerizer.musictriggers.common.objects.CustomRecord;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.config.ConfigChannels;
import mods.thecomputerizer.musictriggers.util.CustomTick;
import mods.thecomputerizer.musictriggers.util.events.AdvancementEvent;
import mods.thecomputerizer.musictriggers.util.events.LivingDamageEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;

import java.io.File;

public class MusicTriggersClient implements ClientModInitializer {

    public static KeyMapping GUI;

    @Override
    public void onInitializeClient() {
        ChannelManager.createJukeboxChannel();
        ConfigChannels.initialize(new File(Constants.CONFIG_DIR, "channels.toml"));
        for (ConfigChannels.ChannelInfo info : ConfigChannels.CHANNELS)
            ChannelManager.createChannel(info);
        ChannelManager.parseConfigFiles();
        setUpClientEvents();
        GUI = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.musictriggers.gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "key.categories.musictriggers"
        ));
        ItemProperties.register(MusicTriggersItems.MUSIC_TRIGGERS_RECORD, new ResourceLocation(Constants.MODID, "trigger"),
                (stack, level, living, id) -> {
                    if (stack.getOrCreateTag().contains("triggerID"))
                        return MusicTriggersRecord.mapTriggerToFloat(stack.getOrCreateTag().getString("triggerID"));
                    return 0f;
        });
        ItemProperties.register(MusicTriggersItems.CUSTOM_RECORD, new ResourceLocation(Constants.MODID, "custom_record"),
                (stack, level, living, id) -> {
                    if (stack.getOrCreateTag().contains("triggerID"))
                        return CustomRecord.mapTriggerToFloat(stack.getOrCreateTag().getString("channelFrom"),
                                stack.getOrCreateTag().getString("triggerID"));
                    return 0f;
        });
        ChannelManager.reloading = false;
    }

    private static void setUpClientEvents() {

        ClientLoginConnectionEvents.DISCONNECT.register((handler, client) -> EventsClient.onDisconnect());

        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
            EventsClient.debugInfo(matrixStack);
            EventsClient.renderBoss();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (GUI.isDown()) EventsClient.onKeyInput();
            EventsClient.onTick();
        });

        LivingDamageEvent.EVENT.register(((entity, damageSource) -> {
            //EventsClient.onDamage(entity, damageSource);
            return InteractionResult.PASS;
        }));

        AdvancementEvent.EVENT.register(((advancement) -> {
            EventsClient.onAdvancement(advancement);
            return InteractionResult.PASS;
        }));

        ServerLifecycleEvents.SERVER_STARTING.register((server) -> ChannelManager.readResourceLocations());

        CustomTick.addCustomTickEvent(20);
    }
}
