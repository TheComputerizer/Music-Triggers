package mods.thecomputerizer.musictriggers.api.data.trigger;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelData;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.parameter.UniversalParameters;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Misc;

import javax.annotation.Nullable;
import java.util.*;

public class TriggerHelper {

    public static Comparator<TriggerAPI> PRIORITY_COMPARATOR = Comparator.comparingInt(trigger -> trigger.getParameterAsInt("priority"));

    /**
     * TODO Verify that the trigger can be loaded on the current version with the current mods.
     * Also ensures the trigger is not null;
     */
    private static boolean checkVersion(@Nullable TriggerAPI trigger) {
        return Objects.nonNull(trigger);
    }

    public static TriggerAPI decodeTrigger(@Nullable ChannelAPI channel, String name, String id) {
        if(Objects.nonNull(channel)) {
            channel.logDebug("Decoding trigger from {}-{}",name,id);
            ChannelData data = channel.getData();
            String nameWithID = Misc.equalsAny(id,null,"null","not_set") ? name : name+"-"+id;
            switch(nameWithID) {
                case "generic": return data.getGenericTrigger();
                case "loading": return data.getLoadingTrigger();
                case "menu": return data.getMenuTrigger();
                default: {
                    for(TriggerAPI trigger : data.getTriggers())
                        if(trigger.getNameWithID().equals(nameWithID)) return trigger;
                    break;
                }
            }
        }
        return null;
    }
    
    public static TriggerAPI decodeTrigger(ChannelAPI channel, CompoundTagAPI<?> tag) {
        TriggerAPI decoded = null;
        if(tag.contains("name")) {
            String name = tag.getString("name");
            if("combination".equals(name)) {
                List<TriggerAPI> triggers = new ArrayList<>();
                tag.getListTag("triggers").forEach(based -> {
                    TriggerAPI trigger = decodeTrigger(channel,based.asCompoundTag());
                    if(Objects.nonNull(trigger)) triggers.add(trigger);
                });
                if(!triggers.isEmpty()) {
                    for(TriggerAPI trigger : channel.getData().getTriggerEventMap().keySet()) {
                        if(trigger.matches(triggers)) {
                            decoded = trigger;
                            break;
                        }
                    }
                }
            } else decoded = decodeTrigger(channel,name,tag.contains("id") ? tag.getString("id") : null);
        }
        return decoded;
    }

    /**
     * Finds a trigger with a matching name-identifier in the specified channel.
     * If the input channel is null, all channels will be iterated through and the first match will be returned.
     * Returns null if no matches are found
     */
    public static @Nullable TriggerAPI findTrigger(ChannelHelper helper, @Nullable ChannelAPI channel, String name) {
        if(Objects.nonNull(channel)) {
            for(TriggerAPI trigger : channel.getData().getTriggers())
                if(trigger.getNameWithID().equals(name)) return trigger;
            return null;
        }
        return findTrigger(helper,name);
    }

    /**
     * Iterates through all channels to find the first trigger with a matching name-identifier
     * The first match will be returned or no matches if none are found.
     */
    public static @Nullable TriggerAPI findTrigger(ChannelHelper helper, String name) {
        for(ChannelAPI channel : helper.getChannels().values()) {
            TriggerAPI trigger = findTrigger(helper,channel,name);
            if(Objects.nonNull(trigger)) return trigger;
        }
        return null;
    }

    public static boolean findTriggers(boolean implyMissing, @Nullable ChannelAPI channel,
            Collection<TriggerAPI> triggers, Collection<?> names) {
        if(Objects.isNull(channel) || Objects.isNull(triggers)) return false;
        if(Objects.isNull(names) || names.isEmpty()) return true;
        for(Object name : names) {
            String nameStr = name.toString();
            TriggerAPI trigger = findTrigger(channel.getHelper(),channel,nameStr);
            if(Objects.isNull(trigger) && implyMissing && channel.implyTrigger(nameStr))
                trigger = findTrigger(channel.getHelper(),channel,nameStr);
            if(Objects.isNull(trigger)) {
                channel.logWarn("Unknown trigger `{}` in triggers array!", nameStr);
                return false;
            }
            triggers.add(trigger);
        }
        return true;
    }

    public static TriggerCombination getCombination(ChannelAPI channel, Collection<TriggerAPI> triggers) {
       TriggerCombination combo = new TriggerCombination(channel);
       for(TriggerAPI child : triggers) combo.addChild(child);
       return combo;
    }

    public static @Nullable TriggerAPI getPriorityTrigger(
            ChannelHelper helper, @Nullable Collection<TriggerAPI> triggers) {
        if(Objects.isNull(triggers) || triggers.isEmpty()) return null;
        return helper.getDebugBool("reverse_priority") ? Collections.min(triggers,PRIORITY_COMPARATOR) :
                Collections.max(triggers,PRIORITY_COMPARATOR);
    }

    public static boolean matchesAll(Collection<TriggerAPI> triggers, Collection<TriggerAPI> others) {
        if(triggers.size()!=others.size()) return false;
        for(TriggerAPI trigger : triggers)
            if(!matchesAny(others,trigger)) return false;
        return true;
    }

    public static boolean matchesAny(Collection<TriggerAPI> triggers, Collection<TriggerAPI> others) {
        for(TriggerAPI other : others)
            if(matchesAny(triggers,other)) return true;
        return false;
    }

    public static boolean matchesAny(Collection<TriggerAPI> triggers, TriggerAPI other) {
        for(TriggerAPI trigger : triggers)
            if(trigger.matches(other)) return true;
        return false;
    }

    public static void parseTriggers(ChannelAPI channel, Collection<TriggerAPI> triggers, @Nullable Toml table) {
        if(Objects.isNull(table)) return;
        for(Toml triggerTable : table.getAllTables()) {
            if(triggerTable.getName().equals("universal")) {
                UniversalParameters universal = channel.getData().getUniversals(TriggerAPI.class);
                if(Objects.isNull(universal) || !universal.parse(triggerTable))
                    channel.logError("Failed to parse universal triggers");
                else channel.logInfo("Intialized universal trigger data");
            }
            else {
                TriggerAPI trigger = TriggerRegistry.getTriggerInstance(channel,triggerTable.getName());
                if(checkVersion(trigger) && trigger.parse(triggerTable)) triggers.add(trigger);
            }
        }
    }
}