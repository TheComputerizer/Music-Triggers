package mods.thecomputerizer.musictriggers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class readRedirect {
    public static String[] songs;
    public readRedirect(File f) {
        List<String> temp = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                temp.add(line.trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        songs = temp.toArray(new String[0]);
    }
}
