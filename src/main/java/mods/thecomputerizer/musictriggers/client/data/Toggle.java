package mods.thecomputerizer.musictriggers.client.data;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import org.apache.logging.log4j.Level;

import java.util.*;

public class Toggle {

    private final HashMap<Integer, List<Trigger>> fromThese;
    private final HashMap<String, List<Trigger>> toThese;

    public Toggle(Table toggle, List<Trigger> triggers) {
        this.fromThese = new HashMap<>();
        this.toThese = new HashMap<>();
        if(!toggle.hasTable("from"))
            MusicTriggers.logExternally(Level.ERROR,"Toggle needs at least 1 \"from\" table in order to be " +
                    "parsed correctly!");
        else {
            for(Table from : toggle.getTablesByName("from")) addTrigger(from,triggers);
            if(!toggle.hasTable("to"))
                MusicTriggers.logExternally(Level.ERROR,"Toggle needs at least 1 \"to\" table in order to be " +
                        "parsed correctly!");
            else for(Table to : toggle.getTablesByName("to")) addTarget(to,triggers);
        }
    }

    private void addTrigger(Table from, List<Trigger> triggers) {
        if(!from.hasVar("condition"))
            MusicTriggers.logExternally(Level.ERROR,"\"from\" table in toggle needs a condition in order to be " +
                    "parsed correctly!");
        else {
            int condition = from.getValOrDefault("condition",0);
            if(condition<1 || condition>3)
                MusicTriggers.logExternally(Level.ERROR,"\"from\" table in toggle has invalid condition! " +
                        "Accepted values are 1, 2, or 3");
            else {
                if(this.fromThese.containsKey(condition)) MusicTriggers.logExternally(Level.ERROR,"\"from\" " +
                        "condition of {} already exists for toggle! Duplicate entry will be skipped",condition);
                else {
                    if(!from.hasVar("triggers"))
                        MusicTriggers.logExternally(Level.ERROR, "\"from\" table in toggle needs at least 1 trigger to " +
                                "be parsed correctly!");
                    else {
                        List<Trigger> parsedTriggers = new ArrayList<>();
                        for(String parsed : from.getValOrDefault("triggers", new ArrayList<String>())) {
                            boolean found = false;
                            for(Trigger trigger : triggers) {
                                if(trigger.getNameWithID().matches(parsed)) {
                                    parsedTriggers.add(trigger);
                                    found = true;
                                    break;
                                }
                            }
                            if(!found) {
                                MusicTriggers.logExternally(Level.ERROR, "Trigger {} in \"from\" table did not " +
                                        "exist and will be skipped!", parsed);
                                parsedTriggers = new ArrayList<>();
                                break;
                            }
                        }
                        if(!parsedTriggers.isEmpty()) this.fromThese.put(condition, parsedTriggers);
                    }
                }
            }
        }
    }

    private void addTarget(Table to, List<Trigger> triggers) {
        if(!to.hasVar("condition"))
            MusicTriggers.logExternally(Level.ERROR,"\"to\" table in toggle needs a condition in order to be " +
                    "parsed correctly!");
        else {
            String condition = to.getValOrDefault("condition","no");
            if(!condition.matches("true") || !condition.matches("false") || !condition.matches("switch"))
                MusicTriggers.logExternally(Level.ERROR,"\"to\" table in toggle has invalid condition! " +
                        "Accepted values are true, false, or switch");
            else {
                if(this.toThese.containsKey(condition)) MusicTriggers.logExternally(Level.ERROR,"\"to\" " +
                        "condition of {} already exists for toggle! Duplicate entry will be skipped",condition);
                else {
                    if(!to.hasVar("triggers"))
                        MusicTriggers.logExternally(Level.ERROR, "\"to\" table in toggle needs at least 1 trigger to " +
                                "be parsed correctly!");
                    else {
                        List<Trigger> parsedTriggers = new ArrayList<>();
                        for(String parsed : to.getValOrDefault("triggers", new ArrayList<String>())) {
                            boolean found = false;
                            for(Trigger trigger : triggers) {
                                if(trigger.getNameWithID().matches(parsed)) {
                                    parsedTriggers.add(trigger);
                                    found = true;
                                    break;
                                }
                            }
                            if(!found) {
                                MusicTriggers.logExternally(Level.ERROR, "Trigger {} in \"to\" table did not " +
                                        "exist and will be skipped!", parsed);
                                parsedTriggers = new ArrayList<>();
                                break;
                            }
                        }
                        if(!parsedTriggers.isEmpty()) this.toThese.put(condition, parsedTriggers);
                    }
                }
            }
        }
    }

    public boolean isValid() {
        return !this.fromThese.isEmpty() && !this.toThese.isEmpty();
    }

    public void runToggle(int condition, List<Trigger> triggers) {
        if(this.fromThese.containsKey(condition))
            if(new HashSet<>(triggers).containsAll(this.fromThese.get(condition)))
                for(Map.Entry<String, List<Trigger>> entry : this.toThese.entrySet())
                    for(Trigger trigger : entry.getValue())
                        trigger.setToggle(entry.getKey().matches("true") || entry.getKey().matches("false") ?
                            Boolean.parseBoolean(entry.getKey()) : !trigger.isToggled());
    }
}
