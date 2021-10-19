package mods.thecomputerizer.musictriggers;

import net.minecraftforge.common.ForgeConfigSpec;


public final class configDebug {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> PlayableEvents;
    public static final ForgeConfigSpec.ConfigValue<Boolean> BiomeChecker;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DimensionChecker;
    public static final ForgeConfigSpec.ConfigValue<Boolean> FinalSongs;
    public static final ForgeConfigSpec.ConfigValue<Boolean> blockedmods;

    static {
        BUILDER.push("Debug Config");
        PlayableEvents = BUILDER.comment("Spam the player with currently playable events in chat").define("PlayableEvents",false);
        BiomeChecker = BUILDER.comment("Spam the player with the current biome in chat").define("BiomeChecker",false);
        DimensionChecker = BUILDER.comment("Spam the player with the current dimension id in chat").define("DimensionChecker",false);
        FinalSongs = BUILDER.comment("Spam the player with the current list of songs that are playing in chat").define("FinalSongs",false);
        blockedmods = BUILDER.comment("List of mod ids to remove the music from so there is not any overlap\n" +
                "CURRENTLY UNIMPLEMENTED").define("blockedmods",false);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
