package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.common.ModSounds;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config;
import mods.thecomputerizer.musictriggers.configTitleCards;
import net.minecraft.client.Minecraft;

import java.io.File;

public class reload {

    public static void readAndReload() {
        File configBase = new File("config/MusicTriggers");
        File mainConfig = new File(configBase,"musictriggers.txt");
        File transitionsConfig = new File(configBase,"transitions.txt");
        config.read(mainConfig);
        configTitleCards.reload(transitionsConfig);
        SoundHandler.emptyListsAndMaps();
        SoundHandler.registerSounds();
        ModSounds.reload();
        Minecraft.getInstance().reloadResourcePacks();
    }
}
