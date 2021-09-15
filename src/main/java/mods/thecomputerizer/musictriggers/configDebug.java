package mods.thecomputerizer.musictriggers;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = MusicTriggers.MODID, name = "MusicTriggers/debugoptions")
public class configDebug {

    @Config.Comment("Spam the player with currently playable events in chat")
    public static boolean PlayableEvents = false;

    @Config.Comment("Spam the player with the current biome in chat")
    public static boolean BiomeChecker = false;

    @Config.Comment("Spam the player with the current dimension id in chat")
    public static boolean DimensionChecker = false;

    @Config.Comment("Spam the player with the current list of songs that are playing in chat")
    public static boolean FinalSongs = false;

    @Config.Comment("List of mod ids to remove the music from so there is not any overlap")
    public static boolean blockedmods = false;

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
