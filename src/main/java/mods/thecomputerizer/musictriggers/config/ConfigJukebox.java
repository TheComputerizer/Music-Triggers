package mods.thecomputerizer.musictriggers.config;


import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.gui.instance.Jukebox;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigJukebox {

    private final File file;
    public final Map<String, String> recordMap;

    public Jukebox copyToGui(String channel) {
        return new Jukebox(this.file, channel, this.recordMap);
    }

    public ConfigJukebox(File file) {
        boolean exists = file.exists();
        this.file = exists ? file : FileUtil.generateNestedFile(file,false);
        if(!exists) FileUtil.writeLinesToFile(this.file,headerLines(),false);
        this.recordMap = new HashMap<>();
    }

    private List<String> headerLines() {
        return Arrays.asList("Format this like name = key",
                "The key refers to a lang key in the format of record.musictriggers.key which ",
                "determines the description of the registered disc",
                "Any lines with Format in the name or = not present will not be read in",
                "Make sure each new entry is on a new line",
                "Here is an example",
                "song1 = dragon");
    }

    public void parse() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.file));
            String line = br.readLine();
            while (line != null) {
                if(!line.contains("Format") && line.contains("=") && !line.contains("==")) {
                    String[] broken = MusicTriggers.stringBreaker(line,"=");
                    recordMap.put(broken[0].trim(),broken[1].trim());
                }
                line = br.readLine();
            }
            br.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
