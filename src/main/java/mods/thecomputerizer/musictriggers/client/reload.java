package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.common.SoundHandler;
import net.minecraftforge.client.resource.VanillaResourceType;

public class reload {

    public static void readAndReload() {
        SoundHandler.emptyListsAndMaps();
        SoundHandler.registerSounds();
        net.minecraftforge.fml.client.FMLClientHandler.instance().refreshResources(VanillaResourceType.SOUNDS);
    }
}
