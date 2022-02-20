package mods.thecomputerizer.musictriggers.config;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = MusicTriggers.MODID, name = "MusicTriggers/registry")
public class configRegistry {
    @Config.Comment("Registry")
    public static Registry registry = new Registry(true,false);

    public static class Registry {
        @Config.Comment("Music Discs")
        public boolean registerDiscs;

        @Config.Comment("Client Side Only\n" +
                "The following will not work: Structure trigger on servers, Mob Trigger targetting parameter")
        public boolean clientSideOnly;

        public Registry(final boolean registerDiscs, final boolean clientSideOnly) {
            this.registerDiscs = registerDiscs;
            this.clientSideOnly = clientSideOnly;
        }
    }

    @Mod.EventBusSubscriber(modid = MusicTriggers.MODID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(MusicTriggers.MODID)) {
                ConfigManager.sync(MusicTriggers.MODID, Config.Type.INSTANCE);
            }
        }
    }
}
