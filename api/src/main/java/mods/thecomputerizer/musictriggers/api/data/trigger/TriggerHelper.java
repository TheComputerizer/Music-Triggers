package mods.thecomputerizer.musictriggers.api.data.trigger;

import mods.thecomputerizer.musictriggers.api.MTAPI;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.TriggerContextClient;
import mods.thecomputerizer.musictriggers.api.data.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

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

    /**
     * Finds a trigger with a matching name-identifier in the specified channel.
     * If the input channel is null all channels will be iterated through and the first match will be returned.
     * Returns null if no matches are found
     */
    public static @Nullable TriggerAPI findTrigger(@Nullable ChannelAPI channel, String name) {
        if(Objects.nonNull(channel)) {
            for(TriggerAPI trigger : channel.getData().getTriggers())
                if(trigger.getNameWithID().equals(name)) return trigger;
            return null;
        }
        return findTrigger(name);
    }

    /**
     * Iterates through all channels to find the first trigger with a matching name-identifier
     * The first match will be returned or no matches if none are found.
     */
    public static @Nullable TriggerAPI findTrigger(String name) {
        for(ChannelAPI channel : ChannelHelper.getChannels().values()) {
            TriggerAPI trigger = findTrigger(channel,name);
            if(Objects.nonNull(trigger)) return trigger;
        }
        return null;
    }

    public static boolean findTriggers(LoggableAPI logger, String channelName, Collection<TriggerAPI> triggers, Table table) {
        return findTriggers(ChannelHelper.findChannel(logger,channelName),triggers,table);
    }

    public static boolean findTriggers(@Nullable ChannelAPI channel, Collection<TriggerAPI> triggers, Table table) {
        return findTriggers(channel,triggers,table.getValOrDefault("triggers",new ArrayList<>()));
    }

    public static boolean findTriggers(@Nullable ChannelAPI channel, Collection<TriggerAPI> triggers, Collection<String> names) {
        if(Objects.isNull(channel) || Objects.isNull(triggers) || Objects.isNull(names) || names.isEmpty()) return false;
        for(String name : names) {
            TriggerAPI trigger = findTrigger(channel,name);
            if(Objects.isNull(trigger)) {
                channel.logWarn("Unknown trigger `{}` in triggers array!");
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

    public static @Nullable TriggerContextAPI<?,?> getContext(ChannelAPI channel) {
        MTAPI api = MTRef.getAPI();
        return Objects.nonNull(api) ? api.getTriggerContext(channel) : null;
    }

    public static @Nullable TriggerAPI getPriorityTrigger(@Nullable Collection<TriggerAPI> triggers) {
        if(Objects.isNull(triggers) || triggers.isEmpty()) return null;
        return ChannelHelper.getDebugBool("REVERSE_PRIORITY") ?
                Collections.min(triggers,PRIORITY_COMPARATOR) : Collections.max(triggers,PRIORITY_COMPARATOR);
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

    public static void parseTriggers(ChannelAPI channel, Collection<TriggerAPI> triggers, @Nullable Table table) {
        if(Objects.isNull(table)) return;
        for(Table triggerTable : table.getChildren().values()) {
            TriggerAPI trigger = TriggerRegistry.getTriggerInstance(channel,triggerTable.getName());
            if(checkVersion(trigger) && trigger.parse(triggerTable)) triggers.add(trigger);
        }
    }
}