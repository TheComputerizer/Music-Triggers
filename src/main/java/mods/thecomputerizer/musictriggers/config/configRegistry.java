package mods.thecomputerizer.musictriggers.config;

import com.moandjiezana.toml.Toml;

import java.io.File;
import java.io.FileWriter;

public class ConfigRegistry {
    public static boolean registerDiscs = true;
    public static boolean clientSideOnly = false;

    public static void create(File f) {
        try {
            String sb = "# Music Discs\n" + "registerdiscs = \"true\"\n" +
                    "# Client Side Only (Some triggers will not be able to trigger)\n" + "clientsideonly = \"false\"\n";
            FileWriter writer = new FileWriter(f);
            writer.write(sb);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void parse(File f) {
        Toml toml = new Toml().read(f);
        registerDiscs = Boolean.parseBoolean(toml.getString("registerdiscs"));
        clientSideOnly = Boolean.parseBoolean(toml.getString("clientSideOnly"));
    }
}