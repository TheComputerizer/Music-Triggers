package mods.thecomputerizer.musictriggers.config;

import com.moandjiezana.toml.Toml;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.instance.Commands;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ConfigCommands {

    private final File file;
    public final Map<String, List<Trigger>> commandMap = new HashMap<>();

    public Commands copyToGui(String channelName) {
        return new Commands(this.file,channelName,MusicTriggers.clone(this.commandMap));
    }

    public ConfigCommands(File file) {
        boolean exists = file.exists();
        this.file = exists ? file : FileUtil.generateNestedFile(file,false);
        if(exists) FileUtil.writeLinesToFile(this.file,headerLines(),false);
    }

    public List<String> headerLines() {
        return Arrays.asList("# Please refer to the wiki page located at https://github.com/TheComputerizer/Music-Triggers/wiki/The-Basics",
                "# or the discord server located at https://discord.gg/FZHXFYp8fc",
                "# for any specific questions you might have regarding commands");
    }

    public void parse(String channel) {
        String CrashHelper = "There was a problem initializing commands";
        try {
            Toml toml = new Toml().read(this.file);
            if (toml.containsTable("command")) {
                Toml command = toml.getTable("command");
                if (command.contains("literal") && command.contains("triggers")) {
                    List<String> triggerNames = command.getList("triggers");
                    AtomicBoolean broken  = new AtomicBoolean(false);
                    List<Trigger> triggers = triggerNames.stream()
                            .map(name -> {
                                Trigger trigger = Trigger.parseAndGetTrigger(channel,name);
                                if(Objects.isNull(trigger)) {
                                    broken.set(true);
                                    MusicTriggers.logExternally(Level.ERROR, "Trigger {} for command literal" +
                                                    "\"{}\" did not exist! Command will be skipped.", name,
                                            command.getString("literal"));
                                }
                                return trigger;
                            }).filter(Objects::nonNull).distinct().collect(Collectors.toList());
                    if(!broken.get())
                        this.commandMap.put(command.getString("literal"), triggers);
                }
            } else if (toml.containsTableArray("command")) {
                for (Toml command : toml.getTables("command")) {
                    if (command.contains("literal") && command.contains("triggers")) {
                        List<String> triggerNames = command.getList("triggers");
                        AtomicBoolean broken  = new AtomicBoolean(false);
                        List<Trigger> triggers = triggerNames.stream()
                                .map(name -> {
                                    Trigger trigger = Trigger.parseAndGetTrigger(channel,name);
                                    if(Objects.isNull(trigger)) {
                                        broken.set(true);
                                        MusicTriggers.logExternally(Level.ERROR, "Trigger {} for command literal" +
                                                        "\"{}\" did not exist! Command will be skipped.", name,
                                                command.getString("literal"));
                                    }
                                    return trigger;
                                }).filter(Objects::nonNull).distinct().collect(Collectors.toList());
                        if(!broken.get())
                            this.commandMap.put(command.getString("literal"), triggers);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(CrashHelper);
        }
    }
}
