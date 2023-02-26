package mods.thecomputerizer.musictriggers.config;

import libraries.com.moandjiezana.toml.Toml;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.gui.instance.Registration;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.TomlUtil;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigRegistry {

    public static File FILE;

    public static boolean REGISTER_DISCS = true;
    public static boolean CLIENT_SIDE_ONLY = false;

    public static Registration copyToGui() {
        Holder holder = Holder.makeEmpty();
        try {
            holder = TomlUtil.readFully(FILE);
        } catch (IOException ex) {
            MusicTriggers.logExternally(Level.ERROR, "Caught exception when reading registration config for the GUI");
            Constants.MAIN_LOG.error("Caught exception when reading registration config for the GUI",ex);
        }
        return new Registration(holder);
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

    public static void write(Holder data) {
        FileUtil.writeLinesToFile(FILE,data.toLines(),false);
    }
}
