package mods.thecomputerizer.musictriggers.config;

import com.moandjiezana.toml.Toml;
import mods.thecomputerizer.musictriggers.client.gui.instance.Registration;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.TomlUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigRegistry {

    public static File FILE;

    public static boolean REGISTER_DISCS = true;
    public static boolean CLIENT_SIDE_ONLY = false;

    public static Registration copyToGui() {
        return new Registration(FILE, REGISTER_DISCS, CLIENT_SIDE_ONLY);
    }

    public static void initialize(File f) {
        FILE = FileUtil.generateNestedFile(f,false);
        read();
        write();
    }

    private static void write() {
        List<String> lines = new ArrayList<>();
        lines.add("# Register Music Discs");
        lines.add(LogUtil.injectParameters("REGISTER_DISCS = \"{}\"",REGISTER_DISCS));
        lines.add("");
        lines.add("# Client Side Only (Some triggers will not be able to activate)");
        lines.add(LogUtil.injectParameters("CLIENT_SIDE_ONLY = \"{}\"",CLIENT_SIDE_ONLY));
        FileUtil.writeLinesToFile(FILE,lines,false);
    }

    public static void read() {
        Toml toml = new Toml().read(FILE);
        REGISTER_DISCS = TomlUtil.readIfExists(toml,"REGISTER_DISCS",REGISTER_DISCS);
        CLIENT_SIDE_ONLY = TomlUtil.readIfExists(toml,"CLIENT_SIDE_ONLY",CLIENT_SIDE_ONLY);
    }

    public static void write(boolean registerDiscs, boolean clientSide) {
        List<String> lines = new ArrayList<>();
        lines.add("# Register Music Discs\n");
        lines.add(LogUtil.injectParameters("REGISTER_DISCS = \"{}\"\n",registerDiscs));
        lines.add("\n");
        lines.add("# Client Side Only (Some triggers will not be able to activate)\n");
        lines.add(LogUtil.injectParameters("CLIENT_SIDE_ONLY = \"{}\"\n",clientSide));
        FileUtil.writeLinesToFile(FILE,lines,false);
    }
}
