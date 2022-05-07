package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.common.ModSounds;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config.ConfigCommands;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.musictriggers.config.ConfigTitleCards;
import mods.thecomputerizer.musictriggers.config.ConfigToml;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.resource.VanillaResourceType;

import java.io.File;
import java.util.HashMap;

public class Reload {

    public static void readAndReload() {
        MusicPlayer.curMusicTimer = 0;
        MusicPicker.emptyMapsAndLists();
        ConfigToml.emptyMaps();
        ConfigTitleCards.emptyMaps();
        SoundHandler.emptyListsAndMaps();
        ConfigCommands.commandMap = new HashMap<>();
        ConfigToml.parse(FMLEnvironment.dist == Dist.CLIENT);
        ConfigTitleCards.parse();
        ConfigCommands.parse();
        SoundHandler.registerSounds();
        ModSounds.reload();
        ForgeHooksClient.refreshResources(Minecraft.getInstance(), VanillaResourceType.SOUNDS);
        refreshDebug();
    }

    public static void refreshDebug() {
        ConfigDebug.parse(new File("config/MusicTriggers/debug.toml"));
    }
}
