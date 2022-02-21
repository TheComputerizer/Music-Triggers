package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.common.ModSounds;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config.configTitleCards;
import mods.thecomputerizer.musictriggers.config.configToml;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.resource.VanillaResourceType;

import java.io.File;

public class reload {

    public static void readAndReload() {
        File transitionsConfig = new File("config/MusicTriggers/transitions.txt");
        configToml.emptyMaps();
        SoundHandler.emptyListsAndMaps();
        configToml.parse();
        configTitleCards.reload(transitionsConfig);
        SoundHandler.registerSounds();
        ModSounds.reload();
        ForgeHooksClient.refreshResources(Minecraft.getInstance(), VanillaResourceType.SOUNDS);
    }
}
