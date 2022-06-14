package mods.thecomputerizer.musictriggers.config;

import mods.thecomputerizer.musictriggers.MusicTriggers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

public class Redirect {

    private final File file;
    public final HashMap<String,String> urlMap = new HashMap<>();

    public Redirect(File file) {
        this.file = file;
    }

    private void create(File f) {
        try {
            if(!f.getParentFile().exists()) f.getParentFile().mkdirs();
            String sb = "Format this like name = url\n" +
                    "Any lines with Format in the name or = not in the name will not be read in\n" +
                    "Make sure each new entry is on a new line\n" +
                    "Here is an example\n" +
                    "thx = https://youtu.be/z3Q4WBpCXhs";
            FileWriter writer = new FileWriter(f);
            writer.write(sb);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parse() {
        if(!this.file.exists()) {
            try {
                this.file.createNewFile();
                create(this.file);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            MusicTriggers.logger.info("parsing redirect");
            BufferedReader br = new BufferedReader(new FileReader(this.file));
            String line = br.readLine();
            while (line != null) {
                MusicTriggers.logger.info("parsing redirect: line - "+line);
                if(!line.contains("Format") && line.contains("=")) {
                    MusicTriggers.logger.info("found viable line");
                    String[] broken = MusicTriggers.stringBreaker(line,"=");
                    urlMap.put(broken[0].trim(),broken[1].trim());
                }
                line = br.readLine();
            }
            br.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
