package mods.thecomputerizer.musictriggers;

import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.File;

@Mod.EventBusSubscriber(modid = MusicTriggers.MODID, value = Dist.CLIENT)
public class packFinder {
    private static RepositorySource source;

    packFinder(File p) {
        if(p!=null) {
            source = new FolderRepositorySource(p, PackSource.BUILT_IN);
        }
        else {
            source = null;
        }
    }

    @SubscribeEvent
    public static void addPack(AddPackFindersEvent ev) {
        if(source!=null) {
            ev.addRepositorySource(source);
        }
    }
}
