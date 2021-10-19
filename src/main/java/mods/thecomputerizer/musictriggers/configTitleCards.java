package mods.thecomputerizer.musictriggers;

import net.minecraftforge.common.ForgeConfigSpec;

public class configTitleCards {
    public static String[] empty = new String[0];
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<String[]> TitleCards;
    public static final ForgeConfigSpec.ConfigValue<String[]> ImageCards;
    public static final ForgeConfigSpec.ConfigValue<Integer> ImageH;
    public static final ForgeConfigSpec.ConfigValue<Integer> ImageV;
    public static final ForgeConfigSpec.ConfigValue<Float> ImageSize;

    static {
        BUILDER.push("Title Card & Image Card Config");
        TitleCards = BUILDER.comment("Title Cards\n" +
                "Format: Title,subtitle,event1,event2,event3,etc...\n" +
                "Example 1: The Underground,The only light is your own,underground\n" +
                "Example 2: Twilit Rain,A more ominous rain than usual,raining,dimension7\n" +
                "List of event names: menu, generic, day, night, sunrise, sunset, light, \n" +
                "underground, deepUnder, raining, storming, snowing, lowHP, dead, inVoid, \n" +
                "spectator, creative, riding, pet, high, \n" +
                "dimension(id) - Ex: dimension7, biomename - Ex: minecraft:swamp, \n" +
                "structure:(name) - Ex: structure:Fortress, mobName - Ex: Zombie, \n" +
                "stageName(true/false) - Ex: stageOnetrue").define("TitleCards",empty);
        ImageCards = BUILDER.comment("Image Cards\n" +
                "Format: Image Title,event1,event2,event3,etc...\n" +
                "Note: The Image must be located in [config/MusicTriggers/songs/assets/musictriggers/textures] and be a png\n" +
                "Look above to the title cards to see the list of events\n" +
                "Example: nightimg,night\n" +
                "Example 2: imgtitle,dimension-50,deepUnder,light\n"+
                "CURRENTLY UNIMPLEMENTED").define("ImageCards",empty);
        ImageH = BUILDER.comment("Image Card Horizontal\n" +
                "Negative numbers will move the image cards to the left while positive numbers will move them to the right\n" +
                "A value of 0 means the image will be centered\n" +
                "This value must be an integer\n"+
                "CURRENTLY UNIMPLEMENTED").define("ImageH",0);
        ImageV = BUILDER.comment("Image Card Vertical\n" +
                "Negative numbers will move the image cards upwards while positive numbers will move them downwards\n" +
                "A value of 0 means the image will be right above where the title cards show up, given the image size is still set to 1.0\n" +
                "This value must be an integer\n"+
                "CURRENTLY UNIMPLEMENTED").define("ImageV",0);
        ImageSize = BUILDER.comment("Image Card Size\n" +
                "This act as a percentage for how big the image cards are.\n" +
                "1.00 = 100%\n" +
                "This value must be a float\n" +
                "CURRENTLY UNIMPLEMENTED").define("ImageSize",1F);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
