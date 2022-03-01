package mods.thecomputerizer.musictriggers;

import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configRegistry;
import mods.thecomputerizer.musictriggers.config.configTitleCards;
import mods.thecomputerizer.musictriggers.config.configToml;
import mods.thecomputerizer.musictriggers.util.events.AdvancementEvent;
import mods.thecomputerizer.musictriggers.util.events.LivingDamageEvent;
import mods.thecomputerizer.musictriggers.util.events.PlaySoundEvent;
import mods.thecomputerizer.musictriggers.util.json;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MusicTriggersClient implements ClientModInitializer {

    public static KeyBinding RELOAD;

    public static File songsDir;
    public static File texturesDir;

    @Override
    public void onInitializeClient() {
        File configDir = new File("config", "MusicTriggers");
        if (!configDir.exists()) {
            configDir.mkdir();
        }
        songsDir = new File(configDir.getPath(), "songs");
        if (!songsDir.exists()) {
            songsDir.mkdir();
        }
        File assetsDir = new File(songsDir.getPath(), "assets");
        if (!assetsDir.exists()) {
            assetsDir.mkdir();
        }
        File musictriggersDir = new File(assetsDir.getPath(), "musictriggers");
        if (!musictriggersDir.exists()) {
            musictriggersDir.mkdir();
        }
        File soundsDir = new File(musictriggersDir.getPath(), "sounds");
        if (!soundsDir.exists()) {
            soundsDir.mkdir();
        }
        File musicDir = new File(soundsDir.getPath(), "music");
        if (!musicDir.exists()) {
            musicDir.mkdir();
        }
        texturesDir = new File(musictriggersDir.getPath(), "textures");
        if (!texturesDir.exists()) {
            texturesDir.mkdir();
        }
        File mcmeta = new File(songsDir.getPath() + "/pack.mcmeta");
        if (!mcmeta.exists()) {
            try {
                mcmeta.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            List<String> lines = Arrays.asList("{", "\t\"pack\": {", "\t\t\"pack_format\": 8,", "\t\t\"description\": \"Can you believe this was generated automatically?\"", "\t}", "}");
            Path file = Paths.get(mcmeta.getPath());
            try {
                Files.write(file, lines, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        File jjson = new File(musictriggersDir.getPath() + "/sounds.json");
        if (!jjson.exists() && json.collector() != null) {
            try {
                jjson.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        File langDir = new File(musictriggersDir.getPath(), "lang");
        if (!langDir.exists()) {
            langDir.mkdir();
        }
        File lang = new File(langDir.getPath() + "/en_us.json");
        if (!lang.exists()) {
            try {
                lang.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        makeSoundsJson();
        makeDiscLang();
        configToml.parse();
        configTitleCards.parse();
        if (json.collector() != null) {
            File pack = new File(configDir,"songs");
            if (pack.isDirectory() && new File(pack, "pack.mcmeta").isFile()) {
                MusicTriggersCommon.logger.info("found pack :)");
                addPack(pack);
            }
        }
        setUpClientEvents();
        RELOAD = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.reload_musictriggers",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "key.categories.musictriggers"
        ));
    }

    public static void makeSoundsJson() {
        File sj = new File("config/MusicTriggers/songs/assets/musictriggers/sounds.json");
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
            File sj = new File("config/MusicTriggers/songs/assets/musictriggers/lang/en_us.json");
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

    public void addPack(File f) {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(MusicTriggersCommon.MODID, "my_resources");
            }

            @Override
            public void reload(ResourceManager manager) {

                for(Identifier id : manager.findResources(f.getPath(), null)) {
                    try(InputStream stream = manager.getResource(id).getInputStream()) {

                    } catch(Exception e) {
                        MusicTriggersCommon.logger.error("Error occurred while loading resource " + id.toString(), e);
                    }
                }
            }
        });
    }

    private static void setUpClientEvents() {
        PlaySoundEvent.EVENT.register(eventsClient::playSound);
        ScreenKeyboardEvents.afterKeyPress(MinecraftClient.getInstance().currentScreen).register((screen, key, scancode, modifiers) -> eventsClient.onKeyInput());

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
