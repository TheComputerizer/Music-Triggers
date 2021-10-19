package mods.thecomputerizer.musictriggers;

import net.minecraftforge.common.ForgeConfigSpec;

public final class config {
    public static String[] empty = new String[0];
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> universalDelay;

    public static final ForgeConfigSpec.ConfigValue<String[]> menuSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> genericFade;
    public static final ForgeConfigSpec.ConfigValue<String[]> genericSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> zonesPriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> zonesFade;
    public static final ForgeConfigSpec.ConfigValue<String[]> zonesSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> dayPriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> dayFade;
    public static final ForgeConfigSpec.ConfigValue<String[]> daySongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> nightPriority;
    public static final ForgeConfigSpec.ConfigValue<String[]> nightSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> sunrisePriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> sunriseFade;
    public static final ForgeConfigSpec.ConfigValue<String[]> sunriseSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> sunsetPriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> sunsetFade;
    public static final ForgeConfigSpec.ConfigValue<String[]> sunsetSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> lightPriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> lightFade;
    public static final ForgeConfigSpec.ConfigValue<Integer> lightLevel;
    public static final ForgeConfigSpec.ConfigValue<String[]> lightSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> undergroundPriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> undergroundFade;
    public static final ForgeConfigSpec.ConfigValue<Integer> undergroundLevel;
    public static final ForgeConfigSpec.ConfigValue<String[]> undergroundSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> deepUnderPriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> deepUnderFade;
    public static final ForgeConfigSpec.ConfigValue<Integer> deepUnderLevel;
    public static final ForgeConfigSpec.ConfigValue<String[]> deepUnderSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> rainingPriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> rainingFade;
    public static final ForgeConfigSpec.ConfigValue<String[]> rainingSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> stormingPriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> stormingFade;
    public static final ForgeConfigSpec.ConfigValue<String[]> stormingSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> snowingPriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> snowingFade;
    public static final ForgeConfigSpec.ConfigValue<String[]> snowingSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> lowHPPriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> lowHPFade;
    public static final ForgeConfigSpec.ConfigValue<Float> lowHPLevel;
    public static final ForgeConfigSpec.ConfigValue<String[]> lowHPSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> deadPriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> deadFade;
    public static final ForgeConfigSpec.ConfigValue<String[]> deadSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> inVoidPriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> inVoidFade;
    public static final ForgeConfigSpec.ConfigValue<Integer> inVoidLevel;
    public static final ForgeConfigSpec.ConfigValue<String[]> inVoidSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> spectatorPriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> spectatorFade;
    public static final ForgeConfigSpec.ConfigValue<String[]> spectatorSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> creativePriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> creativeFade;
    public static final ForgeConfigSpec.ConfigValue<String[]> creativeSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> ridingPriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> ridingFade;
    public static final ForgeConfigSpec.ConfigValue<String[]> ridingSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> petPriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> petFade;
    public static final ForgeConfigSpec.ConfigValue<String[]> petSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> highPriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> highFade;
    public static final ForgeConfigSpec.ConfigValue<Integer> highLevel;
    public static final ForgeConfigSpec.ConfigValue<String[]> highSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> dimensionPriority;
    public static final ForgeConfigSpec.ConfigValue<String[]> dimensionSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> biomePriority;
    public static final ForgeConfigSpec.ConfigValue<String[]> biomeSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> structurePriority;
    public static final ForgeConfigSpec.ConfigValue<String[]> structureSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> mobPriority;
    public static final ForgeConfigSpec.ConfigValue<String[]> mobSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> gamestagePriority;
    public static final ForgeConfigSpec.ConfigValue<String[]> gamestageSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> bloodmoonPriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> bloodmoonFade;
    public static final ForgeConfigSpec.ConfigValue<String[]> bloodmoonSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> harvestmoonPriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> harvestmoonFade;
    public static final ForgeConfigSpec.ConfigValue<String[]> harvestmoonSongs;

    public static final ForgeConfigSpec.ConfigValue<Integer> bluemoonPriority;
    public static final ForgeConfigSpec.ConfigValue<Integer> bluemoonFade;
    public static final ForgeConfigSpec.ConfigValue<String[]> bluemoonSongs;

    static {
        BUILDER.push("All Event Triggers");

        universalDelay = BUILDER.comment("songs").define("universalDelay",0);

        BUILDER.push("Main Menu");
        menuSongs = BUILDER.comment("songs").define("menuSongs",empty);
        BUILDER.pop();

        BUILDER.push("Generic");
        genericFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("genericFade",0);
        genericSongs = BUILDER.comment("songs").define("genericSongs",empty);
        BUILDER.pop();

        BUILDER.push("Zones");
        zonesPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 10000]").define("zonesPriority",10000);
        zonesFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("zonesFade",0);
        zonesSongs = BUILDER.comment("songs").define("zonesSongs",empty);
        BUILDER.pop();

        BUILDER.push("Day");
        dayPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 1000]").define("dayPriority",1000);
        dayFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("dayFade",0);
        daySongs = BUILDER.comment("songs").define("daySongs",empty);
        BUILDER.pop();

        BUILDER.push("Night");
        nightPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 900]").define("nightPriority",900);
        nightSongs = BUILDER.comment("songs - Format: [song name,moon phase,(Optional)fade time [in ticks, default: 0]]\n" +
                "Moon Phases: 1 - Full Moon, 2 - Waning Gibbous, 3 - Third Quarter, 4 - Waning Crescent\n" +
                "5 - New Moon, 6 - Waxing Crescent, 7 - First Quarter, 8 - Waxing Gibbous\n" +
                "You can put 0 to ignore moon phase, or put multiple numbers for a song to be active during multiple phases\n" +
                "Example 1: [nighttime,1] - This will only play during a full moon\n" +
                "Example 2: [nighttime,2,3,4,6,7,8] - This will play every night except for full moons and new moons\n" +
                "Example 3: [nighttime,0] - This will play whenever it is nighttime, just like the old version of this trigger\n" +
                "Note - If the fade is not the last number it will not work properly").define("nightSongs",empty);
        BUILDER.pop();

        BUILDER.push("Sunrise");
        sunrisePriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 1111]").define("sunrisePriority",1111);
        sunriseFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("sunriseFade",0);
        sunriseSongs = BUILDER.comment("songs").define("sunriseSongs",empty);
        BUILDER.pop();

        BUILDER.push("Sunset");
        sunsetPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 1111]").define("sunsetPriority",1111);
        sunsetFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("sunsetFade",0);
        sunsetSongs = BUILDER.comment("songs").define("sunsetSongs",empty);
        BUILDER.pop();

        BUILDER.push("Light level");
        lightPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 500]").define("lightPriority",500);
        lightFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("lightFade",0);
        lightLevel = BUILDER.comment("light level - This indicates the maximum light level").define("lightLevel",7);
        lightSongs = BUILDER.comment("songs").define("lightSongs",empty);
        BUILDER.pop();

        BUILDER.push("Underground - underground with no sky visible");
        undergroundPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 1500]").define("undergroundPriority",1500);
        undergroundFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("undergroundFade",0);
        undergroundLevel = BUILDER.comment("The Y level that is considered underground [default: 55]").define("undergroundLevel",55);
        undergroundSongs = BUILDER.comment("songs").define("undergroundSongs",empty);
        BUILDER.pop();

        BUILDER.push("Deep Under - deep below the surface with no sky visible");
        deepUnderPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 2000]").define("deepUnderPriority",2000);
        deepUnderFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("deepUnderFade",0);
        deepUnderLevel = BUILDER.comment("The Y level that is considered deep underground [default: 20]").define("deepUnderLevel",20);
        deepUnderSongs = BUILDER.comment("songs").define("deepUnderSongs",empty);
        BUILDER.pop();

        BUILDER.push("Raining");
        rainingPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 1300]").define("rainingPriority",1300);
        rainingFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("rainingFade",0);
        rainingSongs = BUILDER.comment("songs").define("rainingSongs",empty);
        BUILDER.pop();

        BUILDER.push("Storming");
        stormingPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 1350]").define("stormingPriority",1350);
        stormingFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("stormingFade",0);
        stormingSongs = BUILDER.comment("songs").define("stormingSongs",empty);
        BUILDER.pop();

        BUILDER.push("Snowing");
        snowingPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 1333]").define("snowingPriority",1333);
        snowingFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("snowingFade",0);
        snowingSongs = BUILDER.comment("songs").define("snowingSongs",empty);
        BUILDER.pop();

        BUILDER.push("Low HP");
        lowHPPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 3000]").define("lowHPPriority",3000);
        lowHPFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("lowHPFade",0);
        lowHPLevel = BUILDER.comment("HP decimal percentage to activate [min: 0, max: 1, default:0.3]").define("lowHPLevel",0.3F);
        lowHPSongs = BUILDER.comment("songs").define("lowHPSongs",empty);
        BUILDER.pop();

        BUILDER.push("Dead");
        deadPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 10000]").define("deadPriority",10000);
        deadFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("deadFade",0);
        deadSongs = BUILDER.comment("songs").define("deadSongs",empty);
        BUILDER.pop();

        BUILDER.push("Void");
        inVoidPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 7777]").define("inVoidPriority",7777);
        inVoidFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("inVoidFade",0);
        inVoidLevel = BUILDER.comment("Below what y level would consider to be actually in the void? [default: 0]").define("inVoidLevel",0);
        inVoidSongs = BUILDER.comment("songs").define("inVoidSongs",empty);
        BUILDER.pop();

        BUILDER.push("Spectator");
        spectatorPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 5000]").define("spectatorPriority",5000);
        spectatorFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("spectatorFade",0);
        spectatorSongs = BUILDER.comment("songs").define("spectatorSongs",empty);
        BUILDER.pop();

        BUILDER.push("Creative");
        creativePriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 5000]").define("creativePriority",5000);
        creativeFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("creativeFade",0);
        creativeSongs = BUILDER.comment("songs").define("creativeSongs",empty);
        BUILDER.pop();

        BUILDER.push("Riding");
        ridingPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 2222]").define("ridingPriority",2222);
        ridingFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("ridingFade",0);
        ridingSongs = BUILDER.comment("songs").define("ridingSongs",empty);
        BUILDER.pop();

        BUILDER.push("Pet");
        petPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 1200]").define("petPriority",1200);
        petFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("petFade",0);
        petSongs = BUILDER.comment("songs").define("petSongs",empty);
        BUILDER.pop();

        BUILDER.push("High");
        highPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 1200]").define("highPriority",1200);
        highFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("highFade",0);
        highLevel = BUILDER.comment("Minimum Y level to activate [default: 150]").define("highLevel",150);
        highSongs = BUILDER.comment("songs").define("highSongs",empty);
        BUILDER.pop();

        BUILDER.push("Dimension");
        dimensionPriority = BUILDER.comment("General Priority [min: -99, max: 2147483647 default: 1150]\n" +
                "Note: Priorities specified for individual dimensions will override this").define("dimensionPriority",1150);
        dimensionSongs = BUILDER.comment("Songs Per Dimension [Format: DimensionID,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\n" +
                "Note: You only have to set the priority per dimension ID for 1 song\n" +
                "Example: -1,(songname),11111").define("dimensionSongs",empty);
        BUILDER.pop();

        BUILDER.push("Biome");
        biomePriority = BUILDER.comment("GGeneral Priority [min: -99, max: 2147483647 default: 1160]\n" +
                "Note: Priorities specified for individual biomes will override this").define("biomePriority",1160);
        biomeSongs = BUILDER.comment("Songs Per Biome [Format: \"BiomeResourceName,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]\n" +
                "Note: You only have to set the priority per biome name for 1 song\n" +
                "Example: minecraft:swampland,(songname),11111").define("biomeSongs",empty);
        BUILDER.pop();

        BUILDER.push("Structure");
        structurePriority = BUILDER.comment("General Priority [min: -99, max: 2147483647 default: 3333]\n" +
                "Note: Priorities specified for individual structures will override this").define("structurePriority",3333);
        structureSongs = BUILDER.comment("Songs Per Structure [Format: \"StructureName,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]\n" +
                "Note: You only have to set the priority per structure name for 1 song\n" +
                "Example: Fortress,(songname),11111").define("structureSongs",empty);
        BUILDER.pop();

        BUILDER.push("Mob (This works for both bosses and hordes!)");
        mobPriority = BUILDER.comment("General Priority [min: -99, max: 2147483647 default: 3500]\n" +
                "Note: Priorities specified for individual mobs will override this").define("mobPriority",3500);
        mobSongs = BUILDER.comment("Songs Per Mob [Format: \"MobName,number of mobs,SongName,(Optional)detection range[min: 1, max: 1000, default: 16],(Optional)Priority:[min: -99, max: 2147483647],(Optional)Fade Time:[in ticks, default: 0]\"]\n" +
                "Note: You only have to set the priority per mob name for 1 song\n" +
                "Additional Note: Putting high numbers for the mob range will cause lag! The higher it is, the more noticable that will be. Only use higher numbers for a boss that could be far away, like the Ender Dragon\n" +
                "Example: Zombie,8,(songname),16,11111\n" +
                "Special case - If you put \"MOB\" as the mob ID, it will default to any hostile mob").define("mobSongs",empty);
        BUILDER.pop();

        BUILDER.push("Gamestages (Only fires if the mod Game Stages is active)");
        gamestagePriority = BUILDER.comment("General Priority [min: -99, max: 2147483647 default: 500]\n" +
                "Note: Priorities specified for individual gamestages will override this").define("gamestagePriority",0);
        gamestageSongs = BUILDER.comment("Songs Per Gamestage [Format: \"StageName,whitelist,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]\n" +
                "Note: You only have to set the priority per gamestage name for 1 song\n" +
                "Example: StageOne,true,(songname),11111 - This will play when the player has the stage. If it were false it would play whenever the player does not have it.").define("gamestageSongs",empty);
        BUILDER.pop();

        BUILDER.push("Blood Moon (Only fires if the mod Enhanced Celestials is active)");
        bloodmoonPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 1200]").define("bloodmoonPriority",1200);
        bloodmoonFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("bloodmoonFade",0);
        bloodmoonSongs = BUILDER.comment("songs").define("bloodmoonSongs",empty);
        BUILDER.pop();

        BUILDER.push("Harvest Moon (Only fires if the mod Enhanced Celestials is active)");
        harvestmoonPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 1400]").define("harvestmoonPriority",1400);
        harvestmoonFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("harvestmoonFade",0);
        harvestmoonSongs = BUILDER.comment("songs").define("harvestmoonSongs",empty);
        BUILDER.pop();

        BUILDER.push("Blue Moon (Only fires if the mod Enhanced Celestials is active)");
        bluemoonPriority = BUILDER.comment("Priority [min: -99, max: 2147483647 default: 1500]").define("bluemoonPriority",1500);
        bluemoonFade = BUILDER.comment("Fade Time [in ticks, default: 0]").define("bluemoonFade",0);
        bluemoonSongs = BUILDER.comment("songs").define("bluemoonSongs",empty);
        BUILDER.pop();

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
