package mods.thecomputerizer.musictriggers.config;

import com.moandjiezana.toml.Toml;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.common.SoundHandler;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ConfigToggles {

    private final File file;
    private final Channel channel;
    private final SoundHandler handler;
    private final HashMap<Trigger,Target> toggleMap;
    private final HashMap<String, Boolean> toggledTriggers;
    private String CrashHelper;

    public ConfigToggles(File file, Channel channel, SoundHandler handler) {
        if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch(Exception e) {
                e.printStackTrace();
            }
            writeInformationalHeader(file);
        }
        this.file = file;
        this.channel = channel;
        this.handler = handler;
        this.toggleMap = new HashMap<>();
        this.toggledTriggers = new HashMap<>();
        this.CrashHelper = "There was a problem initializing toggles in channel "+channel.getChannelName();
    }

    public void writeInformationalHeader(File toml) {
        try {
            String header = "# Please refer to the wiki page located at https://github.com/TheComputerizer/Music-Triggers/wiki/Toggle-System\n" +
                    "# or the discord server located at https://discord.gg/FZHXFYp8fc\n"+
                    "# for any specific questions you might have regarding the toggle system";
            FileWriter writer = new FileWriter(toml);
            writer.write(header);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parse() {
        CrashHelper = "There was a problem initializing toggles in channel "+this.channel.getChannelName();
        try {
            Toml toml = new Toml().read(this.file);
            if (toml.containsTableArray("toggle")) {
                for (Toml toggle : toml.getTables("toggle")) {
                    Trigger trigger = new Trigger();
                    if(toggle.contains("play_once")) trigger.setPlayOnce(Integer.parseInt(toggle.getString("play_once")));
                    if(toggle.containsTableArray("trigger")) {
                        for (Toml from : toggle.getTables("trigger")) {
                            if (from.contains("condition") && from.contains("name"))
                                trigger.addTriggersToCondition(Integer.parseInt(from.getString("condition")), from.getList("name"));
                        }
                    }
                    else if(toggle.containsTable("trigger")) {
                        Toml from = toggle.getTable("trigger");
                        if (from.contains("condition") && from.contains("name"))
                            trigger.addTriggersToCondition(Integer.parseInt(from.getString("condition")), from.getList("name"));
                    }
                    if(trigger.hasStuff()) {
                        Target target = new Target();
                        if (toggle.containsTableArray("target"))
                            for (Toml to : toggle.getTables("target")) {
                                if (to.contains("condition") && to.contains("name"))
                                    target.addTriggersToCondition(to.getString("condition"), to.getList("name"));
                            }
                        else if (toggle.containsTable("target")) {
                            Toml to = toggle.getTable("target");
                            if (to.contains("condition") && to.contains("name"))
                                target.addTriggersToCondition(to.getString("condition"), to.getList("name"));
                        }
                        if(target.hasStuff()) toggleMap.put(trigger, target);
                        else MusicTriggers.logger.error("Toggles need at least one target block to be parsed correctly in channel "+channel.getChannelName()+"!");
                    } else MusicTriggers.logger.error("Toggles need at least one trigger block to be parsed correctly in channel "+channel.getChannelName()+"!");
                }
            } else if (toml.containsTable("toggle")) {
                Toml toggle = toml.getTable("toggle");
                Trigger trigger = new Trigger();
                if(toggle.contains("play_once")) trigger.setPlayOnce(Integer.parseInt(toggle.getString("play_once")));
                if(toggle.containsTableArray("trigger")) {
                    for (Toml from : toml.getTables("trigger")) {
                        if (from.contains("condition") && from.contains("name"))
                            trigger.addTriggersToCondition(Integer.parseInt(from.getString("condition")), from.getList("name"));
                    }
                }
                else if(toggle.containsTable("trigger")) {
                    Toml from = toggle.getTable("trigger");
                    if (from.contains("condition") && from.contains("name")) {
                        trigger.addTriggersToCondition(Integer.parseInt(from.getString("condition")), from.getList("name"));
                    }
                }
                if(trigger.hasStuff()) {
                    Target target = new Target();
                    if (toggle.containsTableArray("target"))
                        for (Toml to : toggle.getTables("target")) {
                            if (to.contains("condition") && to.contains("name"))
                                target.addTriggersToCondition(to.getString("condition"), to.getList("name"));
                        }
                    else if (toggle.containsTable("target")) {
                        Toml to = toggle.getTable("target");
                        if (to.contains("condition") && to.contains("name")) {
                            target.addTriggersToCondition(to.getString("condition"), to.getList("name"));
                        }
                    }
                    if(target.hasStuff()) toggleMap.put(trigger, target);
                    else MusicTriggers.logger.error("Toggles need at least one target block to be parsed correctly in channel "+channel.getChannelName()+"!");
                } else MusicTriggers.logger.error("Toggles need at least one trigger block to be parsed correctly in channel "+channel.getChannelName()+"!");
            }
        } catch (Exception e) {
            throw new RuntimeException(this.CrashHelper);
        }
    }

    public void runToggle(int condition, List<String> triggers) {
        List<String> triggerChain = new ArrayList<>();
        for(Trigger trigger : this.toggleMap.keySet()) {
            if(!triggers.isEmpty() && trigger.getTriggers(condition)!=null && new HashSet<>(triggers).containsAll(trigger.getTriggers(condition)))
                for(String targetCondition : this.toggleMap.get(trigger).getConditions()) {
                    if (targetCondition.matches("true"))
                        for (String target : this.toggleMap.get(trigger).getTargets(targetCondition)) {
                            this.toggledTriggers.put(target, true);
                            MusicTriggers.logger.info("Switching toggle of trigger "+target+" to static true");
                            triggerChain.add(target);
                        }
                    else if (targetCondition.matches("false"))
                        for(String target : this.toggleMap.get(trigger).getTargets(targetCondition)) {
                            this.toggledTriggers.put(target, false);
                            MusicTriggers.logger.info("Switching toggle of trigger "+target+" to static false");
                        }
                    else for(String target : this.toggleMap.get(trigger).getTargets(targetCondition)) {
                            MusicTriggers.logger.info("Switching toggle of trigger "+target+" to "+!getToggle(target));
                            this.toggledTriggers.put(target, !getToggle(target));
                            if(this.toggledTriggers.get(target)) triggerChain.add(target);
                        }
                }
        }
        if(!triggerChain.isEmpty()) runToggle(1,triggerChain);
    }

    public boolean getToggle(String trigger) {
        this.toggledTriggers.putIfAbsent(trigger,Boolean.parseBoolean(this.handler.TriggerInfoMap.get(trigger)[37]));
        return toggledTriggers.get(trigger);
    }

    public void clearMaps() {
        for(Trigger trigger : this.toggleMap.keySet()) {
            this.toggleMap.get(trigger).clear();
            trigger.clear();
        }
        this.toggleMap.clear();
        this.toggledTriggers.clear();
    }

    public static class Trigger {
        private final HashMap<Integer, List<String>> triggerConditions;
        private int playOnce = 0;

        public Trigger() {
            this.triggerConditions = new HashMap<>();
        }

        public void setPlayOnce(int playOnce) {
            MusicTriggers.logger.info("setting playOnce for a toggle");
            this.playOnce = playOnce;
        }

        public int getPlayOnce(int condition) {
            return this.playOnce;
        }

        public List<String> getTriggers(int condition) {
            return triggerConditions.get(condition);
        }

        public void addTriggersToCondition(int condition, List<String> triggers) {
            this.triggerConditions.putIfAbsent(condition, new ArrayList<>());
            for(String trigger : triggers) {
                MusicTriggers.logger.info("Adding trigger "+trigger+" to condition "+condition);
                this.triggerConditions.get(condition).add(trigger);
            }
        }

        public boolean hasStuff() {
            return !triggerConditions.isEmpty();
        }

        private void clear() {
            this.triggerConditions.clear();
        }
    }

    public static class Target {
        private final HashMap<String, List<String>> targetConditions;

        public Target() {
            this.targetConditions = new HashMap<>();
        }

        public List<String> getConditions() {
            return new ArrayList<>(targetConditions.keySet());
        }

        public List<String> getTargets(String condition) {
            this.targetConditions.putIfAbsent(condition,new ArrayList<>());
            return targetConditions.get(condition);
        }

        public void addTriggersToCondition(String condition, List<String> triggers) {
            this.targetConditions.putIfAbsent(condition, new ArrayList<>());
            for(String trigger : triggers) {
                MusicTriggers.logger.info("Adding target "+trigger+" to condition "+condition);
                this.targetConditions.get(condition).add(trigger);
            }
        }

        public boolean hasStuff() {
            return !targetConditions.isEmpty();
        }

        private void clear() {
            this.targetConditions.clear();
        }
    }
}
