package mods.thecomputerizer.musictriggers.config;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Redirect {

    private final File file;
    public final HashMap<String,String> urlMap = new HashMap<>();
    public final HashMap<String, Identifier> resourceLocationMap = new HashMap<>();
    public Redirect(File file) {
        this.file = file;
    }

    private void create(File f) {
        try {
            if(!f.getParentFile().exists()) f.getParentFile().mkdirs();
            String sb = """
                    Format this like name = url
                    Any lines with Format in the name or = not in the name will not be read in
                    Make sure each new entry is on a new line
                    Here are 2 examples
                    thx = https://youtu.be/z3Q4WBpCXhs
                    title == minecraft:sounds/music/menu/menu1.ogg""";
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
            BufferedReader br = new BufferedReader(new FileReader(this.file));
            String line = br.readLine();
            while (line != null) {
                if(!line.contains("Format") && line.contains("=") && !line.contains("==")) {
                    String[] broken = MusicTriggers.stringBreaker(line,"=");
                    urlMap.put(broken[0].trim(),broken[1].trim());
                } else if (!line.contains("Format") && line.contains("==")) {
                    String[] broken = MusicTriggers.stringBreaker(line,"==");
                    try {
                        resourceLocationMap.put(broken[0].trim(), new Identifier(broken[1].trim()));
                    } catch (Exception ignored) {
                        MusicTriggers.logger.error("Resource location "+broken[1].trim()+" was invalid!");
                    }
                }
                line = br.readLine();
            }
            br.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
