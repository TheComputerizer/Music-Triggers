package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.common.ModSounds;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config.configCommands;
import mods.thecomputerizer.musictriggers.config.configDebug;
import mods.thecomputerizer.musictriggers.config.configTitleCards;
import mods.thecomputerizer.musictriggers.config.configToml;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.HashMap;

public class reload {

    public static void readAndReload() {
        MusicPlayer.curMusicTimer=0;
        MusicPicker.emptyMapsAndLists();
        configToml.emptyMaps();
        configTitleCards.emptyMaps();
        SoundHandler.emptyListsAndMaps();
        configCommands.commandMap = new HashMap<>();
        configToml.parse();
        configTitleCards.parse();
        configCommands.parse();
        SoundHandler.registerSounds();
        ModSounds.reload();
        Minecraft.getInstance().reloadResourcePacks();
        refreshDebug();
    }

    public static void refreshDebug() {
        configDebug.parse(new File("config/MusicTriggers/debug.toml"));
    }
}
