package mods.thecomputerizer.musictriggers;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = MusicTriggers.MODID, name = "MusicTriggers/debugoptions")
public class configDebug {

    @Config.Comment("Show the debug info in the F3 menu")
    public static boolean ShowDebugInfo = true;

    @Config.Comment("If ShowDebugInfo is set to true, but you only want to see the song name")
    public static boolean ShowJustCurSong = false;

    @Config.Comment("List of mod ids to remove the music from so there is not any overlap")
    public static String[] blockedmods = {};

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
