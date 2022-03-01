package mods.thecomputerizer.musictriggers;

import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configRegistry;
import mods.thecomputerizer.musictriggers.util.events.AdvancementEvent;
import mods.thecomputerizer.musictriggers.util.events.LivingDamageEvent;
import mods.thecomputerizer.musictriggers.util.events.PlaySoundEvent;
import mods.thecomputerizer.musictriggers.util.json;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.FileResourcePackProvider;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MusicTriggersClient implements ClientModInitializer {

    public static KeyBinding RELOAD;

    @Override
    public void onInitializeClient() {
        setUpClientEvents();
        RELOAD = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.reload_musictriggers",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "key.categories.musictriggers"
        ));
    }

    public static void makeSoundsJson() {
        File sj = new File(FabricLoaderImpl.INSTANCE.getConfigDir().toString(),"MusicTriggers/songs/assets/musictriggers/sounds.json");
        if (sj.exists()) {
            sj.delete();
        }
        List<String> writeThis = json.create();
        if (writeThis != null) {
            try {
                sj.createNewFile();
                FileWriter writer = new FileWriter(sj);
                for (String str : writeThis) {
                    writer.write(str + System.lineSeparator());
                }
                writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void makeDiscLang() {
        if(configRegistry.registerDiscs) {
            File sj = new File(FabricLoaderImpl.INSTANCE.getConfigDir().toString(),"MusicTriggers/songs/assets/musictriggers/lang/en_us.json");
            if (sj.exists()) {
                sj.delete();
            }
            List<String> writeThis = json.create();
            assert writeThis != null;
            writeThis.clear();
            writeThis = json.lang();
            if (writeThis != null) {
                try {
                    sj.createNewFile();
                    FileWriter writer = new FileWriter(sj);
                    for (String str : writeThis) {
                        writer.write(str + System.lineSeparator());
                    }
                    writer.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private static void setUpClientEvents() {
        PlaySoundEvent.EVENT.register(eventsClient::playSound);
        //ScreenKeyboardEvents.afterKeyPress(MinecraftClient.getInstance().currentScreen).register((screen, key, scancode, modifiers) -> eventsClient.onKeyInput());

        WorldRenderEvents.LAST.register((context) -> {
            eventsClient.imageCards(context);
            eventsClient.debugInfo();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            MusicPlayer.onTick();
            eventsClient.onTick();
        });

        LivingDamageEvent.EVENT.register(((entity, damageSource) -> {
            eventsClient.onDamage(entity,damageSource);
            return ActionResult.PASS;
        }));

        AdvancementEvent.EVENT.register(((advancement) -> {
            eventsClient.onAdvancement(advancement);
            return ActionResult.PASS;
        }));
    }
}
