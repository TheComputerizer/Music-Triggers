package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.common.ModSounds;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config.ConfigCommands;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.musictriggers.config.ConfigTitleCards;
import mods.thecomputerizer.musictriggers.config.ConfigToml;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.util.HashMap;

public class Reload {

    public static void readAndReload() {
        MusicPlayer.curMusicTimer=0;
        MusicPicker.emptyMapsAndLists();
        ConfigToml.emptyMaps();
        ConfigTitleCards.emptyMaps();
        SoundHandler.emptyListsAndMaps();
        ConfigCommands.commandMap = new HashMap<>();
        ConfigToml.parse(FabricLoaderImpl.INSTANCE.getEnvironmentType()== EnvType.CLIENT);
        ConfigTitleCards.parse();
        ConfigCommands.parse();
        SoundHandler.registerSounds();
        ModSounds.reload();
        MinecraftClient.getInstance().reloadResources();
        refreshDebug();
    }

    public static void refreshDebug() {
        ConfigDebug.parse(new File("config/MusicTriggers/debug.toml"));
    }
}
