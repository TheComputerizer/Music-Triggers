package mods.thecomputerizer.musictriggers.config;

import mods.thecomputerizer.musictriggers.MusicTriggers;
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

    @Config.Comment("Image Card Size\n" +
            "This act as a percentage for how big the image cards are.\n" +
            "1.00 = 100%\n" +
            "This value must be a float")
    public static float ImageSize = 1.0F;

    @Config.Comment("Image Card Horizontal\n" +
            "Negative numbers will move the image cards to the left while positive numbers will move them to the right\n" +
            "A value of 0 means the image will be centered\n" +
            "This value must be an integer")
    public static int ImageH = 0;

    @Config.Comment("Image Card Vertical\n" +
            "Negative numbers will move the image cards upwards while positive numbers will move them downwards\n" +
            "A value of 0 means the image will be right above where the title cards show up, given the image size is still set to 1.0\n" +
            "This value must be an integer")
    public static int ImageV = 0;

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
