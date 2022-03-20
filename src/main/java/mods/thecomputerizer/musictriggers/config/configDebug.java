
package mods.thecomputerizer.musictriggers.config;

import com.moandjiezana.toml.Toml;

import java.io.File;
import java.io.FileWriter;


public class configDebug {

    public static boolean ShowDebugInfo = false;
    public static boolean ShowJustCurSong = false;
    public static boolean ShowGUIName = false;
    public static String[] blockedmods = {};
    public static boolean SilenceIsBad = false;

    public static void create(File f) {
        try {
            String sb = """
                    # Show the debug info
                    showdebuginfo = "false"
                    # If ShowDebugInfo is set to true, but you only want to see the song name
                    showjustcursong = "false"
                    # Show an overlay for the name of the current GUI
                    showguiname = "false"
                    # Only silence blocked music when there is music from Music Triggers already playing
                    silenceisbad = "false"
                    # List of mod ids to remove the music from so there is not any overlap
                    blockedmods = [ ]
                    """;
            FileWriter writer = new FileWriter(f);
            writer.write(sb);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void parse(File f) {
        Toml toml = new Toml().read(f);
        ShowDebugInfo = Boolean.parseBoolean(toml.getString("showdebuginfo"));
        ShowJustCurSong = Boolean.parseBoolean(toml.getString("showjustcursong"));
        ShowGUIName = Boolean.parseBoolean(toml.getString("showguiname"));
        SilenceIsBad = Boolean.parseBoolean(toml.getString("silenceisbad"));
        blockedmods = toml.getList("blockedmods").toArray(new String[0]);
    }
}