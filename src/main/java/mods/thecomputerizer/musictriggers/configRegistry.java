package mods.thecomputerizer.musictriggers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class configRegistry {
    public static List<String> fb = new ArrayList<>();

    public static boolean registerDiscs;

    public static void build(File f) {
        fb.add("Registry Config");
        fb.add("");
        fb.add("\tMusic Discs");
        fb.add("\tRegister=true");
        try {
            Files.write(Paths.get(f.getPath()), fb, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void read(File f) {
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("Register=")) {
                    line = line.replaceAll("\t","");
                    line = line.replaceAll(" ","");
                    registerDiscs = Boolean.parseBoolean(stringBreakerRegex(line,"=")[1]);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String[] stringBreakerRegex(String s, String regex) {
        return s.split(regex);
    }
}
