package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config.configTitleCards;
import mods.thecomputerizer.musictriggers.config.configToml;
import net.minecraftforge.client.resource.VanillaResourceType;

public class reload {

    public static void readAndReload() {
        configToml.emptyMaps();
        configTitleCards.emptyMaps();
        SoundHandler.emptyListsAndMaps();
        configToml.parse();
        configTitleCards.parse();
        SoundHandler.registerSounds();
        net.minecraftforge.fml.client.FMLClientHandler.instance().refreshResources(VanillaResourceType.SOUNDS);
    }
}
