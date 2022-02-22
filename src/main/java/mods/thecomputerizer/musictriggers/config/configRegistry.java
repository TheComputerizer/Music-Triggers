package mods.thecomputerizer.musictriggers.config;

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

    public static boolean registerDiscs = true;
    public static boolean clientSideOnly = false;

    public static void build(File f) {
        fb.add("Registry Config");
        fb.add("");
        fb.add("\tMusic Discs");
        fb.add("\tRegister=true");
        fb.add("");
        fb.add("\tClient Side Only");
        fb.add("\tIf this is set to true, Music Disc registration will automatically be set to false");
        fb.add("\tThe following will not work: Structure trigger, Biome Trigger on servers, Mob Trigger targetting parameter");
        fb.add("\tClient Side=false");
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
                    if(stringBreakerRegex(line,"=").length!=0) {
                        registerDiscs = Boolean.parseBoolean(stringBreakerRegex(line, "=")[1]);
                    }
                }
                if (line.contains("Client Side=")) {
                    line = line.replaceAll("\t","");
                    line = line.replaceAll(" ","");
                    if(stringBreakerRegex(line,"=").length!=0) {
                        clientSideOnly = Boolean.parseBoolean(stringBreakerRegex(line, "=")[1]);
                    }
                }
            }
            if(clientSideOnly) {
                registerDiscs = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void update(File f) {
        read(f);
        fb.add("Registry Config");
        fb.add("");
        fb.add("\tMusic Discs");
        fb.add("\tRegister="+registerDiscs);
        fb.add("");
        fb.add("\tClient Side Only");
        fb.add("\tIf this is set to true, Music Disc registration will automatically be set to false");
        fb.add("\tThe following will not work: Structure trigger, Biome Trigger on servers, Mob Trigger targetting parameter");
        fb.add("\tClient Side="+clientSideOnly);
        try {
            Files.write(Paths.get(f.getPath()), fb, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String[] stringBreakerRegex(String s, String regex) {
        return s.split(regex);
    }
}
