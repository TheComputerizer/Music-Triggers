package mods.thecomputerizer.musictriggers.api.data.trigger;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.UniversalParameters;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

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
        MTRef.logDebug("Decoding trigger {}-{} with null channel? {}",name,id,Objects.isNull(channel));
        if(Objects.nonNull(channel)) {
            String nameWithID = name+(id.equals("not_set") ? "" : "-"+id);
            for(TriggerAPI trigger : channel.getData().getTriggers())
                if(trigger.getNameWithID().equals(nameWithID))
                    return trigger;
        }
        return null;
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean findTriggers(
            ChannelHelper helper, LoggableAPI logger, String channelName, Collection<TriggerAPI> triggers, Toml table) {
        return findTriggers(helper.findChannel(logger,channelName),triggers,table);
    }

    public static boolean findTriggers(@Nullable ChannelAPI channel, Collection<TriggerAPI> triggers, Toml table) {
        return findTriggers(channel,triggers,table.getValueArray("triggers"));
    }

    public static boolean findTriggers(@Nullable ChannelAPI channel, Collection<TriggerAPI> triggers, Collection<?> names) {
        if(Objects.isNull(channel) || Objects.isNull(triggers) || Objects.isNull(names) || names.isEmpty()) return false;
        for(Object name : names) {
            String nameStr = name.toString();
            TriggerAPI trigger = findTrigger(channel.getHelper(),channel,nameStr);
            if(Objects.isNull(trigger)) {
                if(channel.implyTrigger(nameStr)) trigger = findTrigger(channel.getHelper(),channel,nameStr);
                if(Objects.isNull(trigger)) {
                    channel.logWarn("Unknown trigger `{}` in triggers array!", nameStr);
                    return false;
                }
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
                if(Objects.isNull(universal) || !universal.parseParameters(triggerTable))
                    channel.logError("Failed to parse universal triggers");
                else channel.logInfo("Intialized universal trigger data");
            }
            else {
                TriggerAPI trigger = TriggerRegistry.getTriggerInstance(channel,triggerTable.getName());
                if (checkVersion(trigger) && trigger.parse(triggerTable)) triggers.add(trigger);
            }
        }
    }
}