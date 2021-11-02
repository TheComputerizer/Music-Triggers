package mods.thecomputerizer.musictriggers;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = MusicTriggers.MODID, name = "MusicTriggers/registry")
public class configRegistry {
    @Config.Comment("Registry")
    public static Registry registry = new Registry(true);

    public static class Registry {
        @Config.Comment("Music Discs")
        public boolean registerDiscs;

        public Registry(final boolean registerDiscs) {
            this.registerDiscs = registerDiscs;
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
