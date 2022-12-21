package mods.thecomputerizer.musictriggers.config;

import com.moandjiezana.toml.Toml;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.instance.Toggles;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.TomlUtil;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ConfigToggles {
    private static boolean toggleStatus(String condition, boolean initial) {
        if(condition.matches("true")) return true;
        else if(condition.matches("false")) return false;
        return !initial;
    }
    private final File file;
    private final Map<Integer, Toggle> toggleMap;
    private final Map<Trigger, Boolean> toggledTriggers;
    private String CrashHelper;

    public Toggles copyToGui(String channelName) {
        return new Toggles(this.file,channelName,MusicTriggers.clone(this.toggleMap));
    }

    public ConfigToggles(File file) {
        boolean exists = file.exists();
        this.file = exists ? file : FileUtil.generateNestedFile(file,false);
        if(exists) FileUtil.writeLinesToFile(this.file,headerLines(),false);
        this.toggleMap = new HashMap<>();
        this.toggledTriggers = new HashMap<>();
        this.CrashHelper = "There was a problem initializing toggles!";
    }

    public List<String> headerLines() {
        return Arrays.asList("# Please refer to the wiki page located at https://github.com/TheComputerizer/Music-Triggers/wiki/The-Basics",
                "# or the discord server located at https://discord.gg/FZHXFYp8fc",
                "# for any specific questions you might have regarding toggles");
    }

    public void parse(String channel) {
        CrashHelper = "There was a problem initializing toggles in channel "+channel;
        try {
            Toml toml = new Toml().read(this.file);
            int index = 0;
            if (toml.containsTableArray("toggle"))
                for (Toml toggle : toml.getTables("toggle"))
                    index = makeToggle(channel,toggle,index);
            else if (toml.containsTable("toggle")) makeToggle(channel,toml.getTable("toggle"),index);
        } catch (Exception e) {
            throw new RuntimeException(this.CrashHelper);
        }
    }

    private int makeToggle(String channel, Toml toggleToml, int index) {
        Toggle toggle = new Toggle();
        if(toggleToml.contains("play_once")) toggle.setPlayOnce(TomlUtil.readIfExists(toggleToml,"play_once",0));
        if(toggleToml.containsTableArray("trigger")) {
            for (Toml from : toggleToml.getTables("trigger"))
                if (from.contains("condition") && from.contains("name"))
                    parseTriggerList(channel,toggle,TomlUtil.readIfExists(from,"condition",0),
                            from.getList("name"));
        }
        else if(toggleToml.containsTable("trigger")) {
            Toml from = toggleToml.getTable("trigger");
            if (from.contains("condition") && from.contains("name"))
                parseTriggerList(channel,toggle,TomlUtil.readIfExists(from,"condition",0),
                        from.getList("name"));
        }
        if(toggle.hasTriggers()) {
            if (toggleToml.containsTableArray("target"))
                for (Toml to : toggleToml.getTables("target")) {
                    if (to.contains("condition") && to.contains("name"))
                        parseTargetList(channel,toggle,to.getString("condition"),to.getList("name"));
                }
            else if (toggleToml.containsTable("target")) {
                Toml to = toggleToml.getTable("target");
                if (to.contains("condition") && to.contains("name"))
                    parseTargetList(channel,toggle,to.getString("condition"),to.getList("name"));
            }
            if(toggle.hasStuff()) toggleMap.put(index,toggle);
            else MusicTriggers.logExternally(Level.ERROR, "Toggles need at least one target block to be parsed " +
                    "correctly in channel "+channel+"!");
        } else MusicTriggers.logExternally(Level.ERROR, "Toggles need at least one trigger block to be parsed " +
                "correctly in channel "+channel+"!");
        index++;
        return index;
    }

    private void parseTriggerList(String channel, Toggle toggle, int condition, List<String> triggerNames) {
        if(condition>=1 && condition<=3) {
            AtomicBoolean broken = new AtomicBoolean(false);
            List<Trigger> triggers = triggerNames.stream()
                    .map(name -> {
                        Trigger trigger = Trigger.parseAndGetTrigger(channel, name);
                        if (Objects.isNull(trigger)) {
                            broken.set(true);
                            MusicTriggers.logExternally(Level.ERROR, "Trigger {} for toggle condition" +
                                    "\"{}\" did not exist! Condition will be skipped.", name, condition);
                        }
                        return trigger;
                    }).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            if (!broken.get()) toggle.addTriggersToCondition(condition, triggers);
        } else MusicTriggers.logExternally(Level.ERROR, "Condition {} for toggle was invalid! Accepted " +
                "conditions are 1, 2, or 3",condition);
    }

    private void parseTargetList(String channel, Toggle toggle, String condition, List<String> triggerNames) {
        if(condition.matches("true") || condition.matches("false") || condition.matches("switch")) {
            AtomicBoolean broken = new AtomicBoolean(false);
            List<Trigger> triggers = triggerNames.stream()
                    .map(name -> {
                        Trigger trigger = Trigger.parseAndGetTrigger(channel, name);
                        if (Objects.isNull(trigger)) {
                            broken.set(true);
                            MusicTriggers.logExternally(Level.ERROR, "Target {} for toggle condition" +
                                    "\"{}\" did not exist! Condition will be skipped.", name, condition);
                        }
                        return trigger;
                    }).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            if (!broken.get()) toggle.addTargetsToCondition(condition, triggers);
        } else MusicTriggers.logExternally(Level.ERROR, "Condition {} for toggle was invalid! Accepted " +
                "conditions are true, false, or switch",condition);
    }

    public void runToggle(int condition, List<Trigger> triggers) {
        List<Trigger> triggerChain = new ArrayList<>();
        for(Toggle toggle : this.toggleMap.values()) {
            if(toggle.canToggle(condition, triggers))
                for(String targetCon : toggle.targetConditions())
                    for(Trigger trigger : toggle.getTargets(targetCon)) {
                        boolean toggled = toggleStatus(targetCon,this.toggledTriggers.get(trigger));
                        this.toggledTriggers.put(trigger,toggled);
                        if(toggled) triggerChain.add(trigger);
                    }
        }
        if(!triggerChain.isEmpty()) runToggle(1,triggerChain);
    }

    public boolean getToggle(Trigger trigger, boolean startToggle) {
        this.toggledTriggers.putIfAbsent(trigger,startToggle);
        return toggledTriggers.get(trigger);
    }

    public void forceToggle(Trigger trigger, boolean on) {
        this.toggledTriggers.put(trigger,on);
    }

    public void clearMaps() {
        for(Toggle toggle : this.toggleMap.values()) toggle.clear();
        this.toggleMap.clear();
        this.toggledTriggers.clear();
    }

    public static class Toggle {
        private final HashMap<Integer, List<Trigger>> triggerConditions;
        private final HashMap<String, List<Trigger>> targetConditions;
        private int playOnce = 0;

        public Toggle() {
            this.triggerConditions = new HashMap<>();
            this.targetConditions = new HashMap<>();
        }

        public List<String> getAsTomlLines(boolean multi) {
            List<String> lines = new ArrayList<>();
            if(this.triggerConditions.isEmpty() || this.targetConditions.isEmpty()) return lines;
            lines.add(multi ? "[[toggle]]" : "[toggle]");
            if(playOnce>0) lines.add("\tplay_once = "+this.playOnce);
            lines.add(this.triggerConditions.size()>1 ? "\t[[trigger]]" : "\t[trigger]");
            for(Map.Entry<Integer, List<Trigger>> triggerEntry : this.triggerConditions.entrySet()) {
                lines.add("\t\tcondition = "+triggerEntry.getKey());
                lines.add("\t\tname = "+ TextUtil.compileCollection(triggerEntry.getValue().stream()
                        .map(Trigger::getNameWithID).distinct().collect(Collectors.toList())));
            }
            lines.add(this.targetConditions.size()>1 ? "\t[[target]]" : "\t[target]");
            for(Map.Entry<String, List<Trigger>> targetEntry : this.targetConditions.entrySet()) {
                lines.add("\t\tcondition = \""+targetEntry.getKey()+"\"");
                lines.add("\t\tname = "+ TextUtil.compileCollection(targetEntry.getValue().stream()
                        .map(Trigger::getNameWithID).distinct().collect(Collectors.toList())));
            }
            lines.add("");
            return lines;
        }

        public void setPlayOnce(int playOnce) {
            this.playOnce = playOnce;
        }

        public int getPlayOnce() {
            return this.playOnce;
        }

        public HashMap<Integer, List<Trigger>> getTriggerConditions() {
            return this.triggerConditions;
        }

        public HashMap<String, List<Trigger>> getTargetConditions() {
            return this.targetConditions;
        }

        public boolean canToggle(int condition, List<Trigger> triggers) {
            return this.triggerConditions.containsKey(condition) &&
                    new HashSet<>(triggers).containsAll(this.triggerConditions.get(condition));
        }

        public Collection<String> targetConditions() {
            return this.targetConditions.keySet();
        }

        public List<Trigger> getTargets(String condition) {
            return this.targetConditions.get(condition);
        }

        public void addTriggersToCondition(int condition, List<Trigger> triggers) {
            this.triggerConditions.putIfAbsent(condition, new ArrayList<>());
            this.triggerConditions.get(condition).addAll(triggers);
        }

        public void addTargetsToCondition(String condition, List<Trigger> triggers) {
            this.targetConditions.putIfAbsent(condition, new ArrayList<>());
            this.targetConditions.get(condition).addAll(triggers);
        }

        public boolean hasTriggers() {
            return !this.triggerConditions.isEmpty();
        }

        public boolean hasStuff() {
            return !this.triggerConditions.isEmpty() && !this.targetConditions.isEmpty();
        }

        private void clear() {
            this.triggerConditions.clear();
            this.targetConditions.clear();
        }
    }
}
