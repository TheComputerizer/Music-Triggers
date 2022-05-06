package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config.ConfigCommands;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.musictriggers.config.ConfigTitleCards;
import mods.thecomputerizer.musictriggers.config.ConfigToml;
import mods.thecomputerizer.musictriggers.util.Json;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.File;
import java.util.HashMap;

public class Reload {

    public static void readAndReload() {
        ConfigToml.emptyMaps();
        ConfigTitleCards.emptyMaps();
        SoundHandler.emptyListsAndMaps();
        ConfigCommands.commandMap = new HashMap<>();
        Json.collector();
        ConfigToml.parse(true);
        ConfigTitleCards.parse();
        ConfigCommands.parse();
        SoundHandler.registerSounds();
        FMLClientHandler.instance().refreshResources(VanillaResourceType.SOUNDS);
        refreshDebug();
    }

    public static void refreshDebug() {
        ConfigDebug.parse(new File("config/MusicTriggers/debug.toml"));
    }
}
