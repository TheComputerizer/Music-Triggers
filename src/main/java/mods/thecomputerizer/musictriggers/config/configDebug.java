package mods.thecomputerizer.musictriggers.config;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = MusicTriggers.MODID, name = "MusicTriggers/debugoptions")
public class configDebug {

    @Config.Comment("Show the debug info")
    public static boolean ShowDebugInfo = false;

    @Config.Comment("If ShowDebugInfo is set to true, but you only want to see the song name")
    public static boolean ShowJustCurSong = false;

    @Config.Comment("Show an overlay for the name of the current GUI")
    public static boolean ShowGUIName = false;

    @Config.Comment("List of mod ids to remove the music from so there is not any overlap")
    public static String[] blockedmods = {};

    @Config.Comment("Only silence blocked music when there is music from Music Triggers already playing")
    public static boolean SilenceIsBad = false;

    @Config.Comment("Allows you to set variable names for songs to avoid needing multiple copies of the same song (see redirect.txt)\n"+
    "Usage: customname,songname\n"+
    "Note - All names used in the main config will still generate music discs if that option is turned on")
    public static boolean enableRedirect = true;

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
