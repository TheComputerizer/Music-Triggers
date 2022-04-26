package mods.thecomputerizer.musictriggers.config;

import com.moandjiezana.toml.Toml;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigCommands {

    public static Map<String, List<String>> commandMap = new HashMap<>();

    public static void parse() {
        File file = new File("config/MusicTriggers/commands.toml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String CrashHelper = "There was a problem initializing commands";
            try {
                Toml toml = new Toml().read(file);
                if(toml.containsTable("command")) {
                    Toml command = toml.getTable("command");
                    if(command.contains("literal") && command.contains("triggers")) {
                        commandMap.put(command.getString("literal"), command.getList("triggers"));
                    }
                }
                else if(toml.containsTableArray("command")) {
                    for (Toml command : toml.getTables("command")) {
                        if (command.contains("literal") && command.contains("triggers")) {
                            commandMap.put(command.getString("literal"), command.getList("triggers"));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(CrashHelper);
            }
        }
    }
}
