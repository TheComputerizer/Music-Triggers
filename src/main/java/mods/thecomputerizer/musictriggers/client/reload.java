package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config.configCommands;
import mods.thecomputerizer.musictriggers.config.configDebug;
import mods.thecomputerizer.musictriggers.config.configTitleCards;
import mods.thecomputerizer.musictriggers.config.configToml;
import net.minecraftforge.client.resource.VanillaResourceType;

import java.io.File;
import java.util.HashMap;

public class reload {

    public static void readAndReload() {
        configToml.emptyMaps();
        configTitleCards.emptyMaps();
        SoundHandler.emptyListsAndMaps();
        configCommands.commandMap = new HashMap<>();
        configToml.parse();
        configTitleCards.parse();
        configCommands.parse();
        SoundHandler.registerSounds();
        net.minecraftforge.fml.client.FMLClientHandler.instance().refreshResources(VanillaResourceType.SOUNDS);
        refreshDebug();
    }

    public static void refreshDebug() {
        configDebug.parse(new File("config/MusicTriggers/debug.toml"));
    }
}
