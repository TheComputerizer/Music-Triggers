package mods.thecomputerizer.musictriggers;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = MusicTriggers.MODID, name = "MusicTriggers/titlecards")
public class configTitleCards {

    @Config.Comment("Title Cards\nFormat: Title,subtitle,event1,event2,event3,etc...\n" +
            "Example 1: The Underground,The only light is your own,underground\n" +
            "Example 2: Twilit Rain,A more ominous rain than usual,raining,dimension7\n" +
            "List of event names: menu, generic, day, night, sunrise, sunset, light, \n" +
            "underground, deepUnder, raining, storming, snowing, lowHP, dead, inVoid, \n" +
            "spectator, creative, riding, pet, high, \n" +
            "dimension(id) - Ex: dimension7, biomename - Ex: minecraft:swamp, \n" +
            "structure:(name) - Ex: structure:Fortress, mobName - Ex: Zombie, \n" +
            "stageName(true/false) - Ex: stageOnetrue")
    public static String[] TitleCards = {};

    @Config.Comment("Image Cards\nFormat: Image Title,event1,event2,event3,etc...\n"+
    "Note: The Image must be located in [config/MusicTriggers/songs/assets/musictriggers/textures] and be a png\n"+
    "Look above to the title cards to see the list of events\n"+"Example: nightimg,night\n"+
    "Example 2: imgtitle,dimension-50,deepUnder,light")
    public static String[] ImageCards = {};

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
