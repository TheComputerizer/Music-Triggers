package mods.thecomputerizer.musictriggers;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;


public final class configDebug {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ShowDebugInfo;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ShowJustCurSong;
    public static final ForgeConfigSpec.ConfigValue<List<String>> blockedmods;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SilenceIsBad;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ShowGUIName;

    static {
        BUILDER.push("Debug Config");
        ShowDebugInfo = BUILDER.comment("Show the debug info").define("ShowDebugInfo",false);
        ShowJustCurSong = BUILDER.comment("If ShowDebugInfo is set to true, but you only want to see the song name").define("ShowJustCurSong",false);
        ShowGUIName = BUILDER.comment("Show an overlay for the name of the current GUI").define("ShowGUIName",false);
        blockedmods = BUILDER.comment("List of mod ids to remove the music from so there is not any overlap").define("blockedmods", new ArrayList<>());
        SilenceIsBad = BUILDER.comment("Only silence blocked music when there is music from Music Triggers already playing").define("SilenceIsBad", false);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
