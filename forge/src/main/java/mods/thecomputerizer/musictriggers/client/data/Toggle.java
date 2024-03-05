package mods.thecomputerizer.musictriggers.client.data;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.channels.Channel;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import net.minecraft.util.Tuple;
import org.apache.logging.log4j.Level;

import java.util.*;

public class Toggle {
    private final HashMap<Integer, List<Trigger>> fromThese;
    private final HashMap<Integer, Integer> fromPlayOnce;
    private final HashMap<Integer, Boolean> fromCanPlay;
    private final HashMap<String, Channel> toChannels;
    private final HashMap<Channel, String> channelActivations;
    private final HashMap<String, List<Trigger>> toThese;

    public Toggle(Table toggle, String defChannel) {
        this.fromThese = new HashMap<>();
        this.fromPlayOnce = new HashMap<>();
        this.fromCanPlay = new HashMap<>();
        this.toThese = new HashMap<>();
        this.channelActivations = new HashMap<>();
        this.toChannels = new HashMap<>();
        if(!toggle.hasTable("from"))
            MusicTriggers.logExternally(Level.ERROR,"Channel[{}] - Toggle needs at least 1 \"from\" table in " +
                    "order to be parsed correctly!",defChannel);
        else {
            for(Table from : toggle.getTablesByName("from")) addTrigger(from,defChannel);
            if(!toggle.hasTable("to"))
                MusicTriggers.logExternally(Level.ERROR,"Channel[{}] - Toggle needs at least 1 \"to\" table in " +
                        "order to be parsed correctly!",defChannel);
            else for(Table to : toggle.getTablesByName("to")) 
                addTarget(to,defChannel);
        }
    }

    public boolean isValid(String defChannel) {
        if(this.fromThese.isEmpty() || (this.toThese.isEmpty() && this.channelActivations.isEmpty())) {
            MusicTriggers.logExternally(Level.ERROR,"Channel[{}] - Toggle failed to register because it does  " +
                    "not have any valid \"from\" or \"to\" conditions!",defChannel);
            return false;
        }
        return true;
    }

    private void addTrigger(Table from, String defChannel) {
        if (!from.hasVar("condition"))
            MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - \"from\" table in toggle needs a " +
                    "condition in order to be parsed correctly!", defChannel);
        else {
            int condition = from.getValOrDefault("condition", 0);
            if (condition < 1 || condition > 3)
                MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - \"from\" table in toggle has " +
                        "invalid condition {}! Accepted values are 1, 2, or 3", defChannel, condition);
            else {
                if (this.fromThese.containsKey(condition)) MusicTriggers.logExternally(Level.ERROR,
                        "Channel[{}] - \"from\" condition of {} already exists for toggle! Duplicate entry " +
                                "will be skipped", defChannel, condition);
                else {
                    if (!from.hasVar("triggers"))
                        MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - \"from\" table in toggle " +
                                "needs at least 1 trigger to be parsed correctly!", defChannel);
                    else {
                        List<Trigger> parsedTriggers = new ArrayList<>();
                        Channel channel = ChannelManager.getNonDefaultChannel(defChannel);
                        for (String parsed : from.getValOrDefault("triggers", new ArrayList<String>())) {
                            boolean found = false;
                            for (Trigger trigger : channel.getRegisteredTriggers()) {
                                if (trigger.getNameWithID().matches(parsed)) {
                                    parsedTriggers.add(trigger);
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Trigger {} in " +
                                                "\"from\" table did not exist and will be skipped!",
                                        defChannel, parsed);
                                parsedTriggers = new ArrayList<>();
                                break;
                            }
                        }
                        if (!parsedTriggers.isEmpty()) {
                            this.fromThese.put(condition, parsedTriggers);
                            int playOnce = from.getValOrDefault("play_once", 0);
                            if (playOnce < 0 || playOnce > 5) {
                                MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - play_once value " +
                                        "{} in \"from\" is not within its bounds [0-3] and will be set " +
                                        "to 0!", defChannel, playOnce);
                                playOnce = 0;
                            }
                            this.fromPlayOnce.put(condition, playOnce);
                            this.fromCanPlay.put(condition,true);
                        }
                    }
                }
            }
        }
    }

    private void addTarget(Table to, String defChannel) {
        String channelTo = to.getValOrDefault("channel",defChannel);
        if(ChannelManager.channelDoesNotExist(channelTo))
            MusicTriggers.logExternally(Level.ERROR,"Channel[{}] - Channel name {} referenced under \"to\" " +
                    "table in toggle is not recognized as a valid channel!",defChannel,channelTo);
        else {
            if (!to.hasVar("condition"))
                MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - \"to\" table in toggle needs a " +
                        "condition in order to be parsed correctly!",defChannel);
            else {
                String condition = to.getValOrDefault("condition", "no");
                if (!condition.matches("true") && !condition.matches("false") && !condition.matches("switch"))
                    MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - \"to\" table in toggle has invalid " +
                            "condition {}! Accepted values are true, false, or switch",defChannel,condition);
                else {
                    if (this.toThese.containsKey(condition)) MusicTriggers.logExternally(Level.ERROR, "\"to\" " +
                            "condition of {} already exists for toggle! Duplicate entry will be skipped", condition);
                    else {
                        boolean channelActivation = to.getValOrDefault("channel_activation",false);
                        if (!to.hasVar("triggers") && !channelActivation)
                            MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - \"to\" table in toggle " +
                                    "needs at least 1 trigger or have channel_activation enabled to be parsed correctly!",defChannel);
                        else {
                            Channel channel = ChannelManager.getNonDefaultChannel(channelTo);
                            if(channelActivation) {
                                this.toChannels.put(condition,channel);
                                this.channelActivations.put(channel, condition);
                            } else {
                                List<Trigger> parsedTriggers = new ArrayList<>();
                                for (String parsed : to.getValOrDefault("triggers", new ArrayList<String>())) {
                                    boolean found = false;
                                    for (Trigger trigger : channel.getRegisteredTriggers()) {
                                        if (trigger.getNameWithID().matches(parsed)) {
                                            parsedTriggers.add(trigger);
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Trigger {} in " +
                                                        "\"to\" table did not exist in reference channel {}and will be skipped!",
                                                defChannel, parsed, channelTo);
                                        parsedTriggers = new ArrayList<>();
                                        break;
                                    }
                                }
                                if (!parsedTriggers.isEmpty()) {
                                    this.toChannels.put(condition, channel);
                                    this.toThese.put(condition, parsedTriggers);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Also handles toggling entire channels
     */
    public Map<Channel,Tuple<String,List<Trigger>>> getTargets(int condition, Set<Trigger> triggers) {
        Map<Channel,Tuple<String,List<Trigger>>> ret = new HashMap<>();
        List<Trigger> fromThis = this.fromThese.get(condition);
        if(Objects.nonNull(fromThis) && !fromThis.isEmpty()) {
            if(triggers.containsAll(fromThis) && this.fromCanPlay.get(condition)) {
                for(Map.Entry<String,Channel> targetEntry : this.toChannels.entrySet()) {
                    String targetCon = targetEntry.getKey();
                    Channel channel = targetEntry.getValue();
                    if(this.toThese.containsKey(targetCon))
                        ret.put(channel, new Tuple<>(targetCon, this.toThese.get(targetCon)));
                    else if(this.channelActivations.containsKey(channel))
                        channel.setChannelToggle(this.channelActivations.get(channel));
                }
            }
        }
        return ret;
    }
}
