package mods.thecomputerizer.musictriggers.server.data;

import com.google.common.collect.ImmutableList;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.network.PacketMusicTriggersLogin;
import mods.thecomputerizer.musictriggers.server.channels.ServerChannelManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.Level;

import java.util.*;

public class PersistentTriggerData implements IPersistentTriggerData {

    private final Map<String, Map<String,Boolean>> toggleMap;
    private final Map<String,Map<String, Tuple<ImmutableList<String>,Integer>>> playedOnceMap;
    private int preferredSort = 1;

    public PersistentTriggerData() {
        this.toggleMap = new HashMap<>();
        this.playedOnceMap = new HashMap<>();
    }

    public void onLogin(ServerPlayerEntity player) {
        new PacketMusicTriggersLogin(ServerChannelManager.hasConfig(),this.toggleMap,this.playedOnceMap,this.preferredSort)
                .addPlayers(player).send();
    }

    public void writePreferredSort(int preferredSort) {
        this.preferredSort = preferredSort;
    }

    public void initChannel(String channel) {
        this.toggleMap.putIfAbsent(channel,new HashMap<>());
        this.playedOnceMap.putIfAbsent(channel,new HashMap<>());
    }

    @Override
    public void writeToggleStatus(String channel, String trigger, boolean isToggled) {
        if(this.toggleMap.containsKey(channel)) {
            if(Objects.nonNull(trigger)) this.toggleMap.get(channel).put(trigger, isToggled);
            else MusicTriggers.logExternally(Level.WARN,"Could not set toggle status for null trigger in " +
                    "channel {}!",channel);
        } else MusicTriggers.logExternally(Level.WARN,"Could not set toggle status for trigger {} in " +
                "unknown channel {}!",trigger,channel);
    }

    @Override
    public void setAudioPlayed(String channel, String audio, List<String> audioTriggers, int timesPlayed) {
        if(this.playedOnceMap.containsKey(channel)) {
            if (Objects.nonNull(audio))
                this.playedOnceMap.get(channel).put(audio,new Tuple<>(ImmutableList.copyOf(audioTriggers),timesPlayed));
            else MusicTriggers.logExternally(Level.WARN, "Could not set play status for null audio in " +
                    "channel {}!", channel);
        } else MusicTriggers.logExternally(Level.WARN,"Could not set play status for audio {} in " +
                "unknown channel {}!", audio,channel);
    }

    @Override
    public void clearChannelData(String channel) {
        if(this.toggleMap.containsKey(channel)) {
            this.toggleMap.get(channel).clear();
            this.playedOnceMap.get(channel).clear();
        } else MusicTriggers.logExternally(Level.WARN,"Could not clear data for unknown channel {}!",channel);
    }

    @Override
    public void clearAllData() {
        for(String channel : this.toggleMap.keySet())
            clearChannelData(channel);
    }

    @Override
    public CompoundNBT writeToNBT() {
        CompoundNBT compound = new CompoundNBT();
        compound.putInt("preferredSort",this.preferredSort);
        ListNBT channelsList = new ListNBT();
        for(String channel : this.toggleMap.keySet()) {
            CompoundNBT channelTag = new CompoundNBT();
            channelTag.putString("name",channel);
            ListNBT triggersList = new ListNBT();
            for(Map.Entry<String,Boolean> toggleStatus : this.toggleMap.get(channel).entrySet()) {
                CompoundNBT triggerTag = new CompoundNBT();
                triggerTag.putString("name",toggleStatus.getKey());
                triggerTag.putBoolean("isToggled",toggleStatus.getValue());
                triggersList.add(triggerTag);
            }
            ListNBT songsList = new ListNBT();
            for(Map.Entry<String,Tuple<ImmutableList<String>,Integer>> playStatus : this.playedOnceMap.get(channel).entrySet()) {
                CompoundNBT audioTag = new CompoundNBT();
                audioTag.putString("name",playStatus.getKey());
                ListNBT audioTriggersList = new ListNBT();
                for(String trigger : playStatus.getValue().getA())
                    audioTriggersList.add(StringNBT.valueOf(trigger));
                audioTag.put("triggers",audioTriggersList);
                audioTag.putInt("timesPlayed",playStatus.getValue().getB());
                songsList.add(audioTag);
            }
            channelTag.put("toggleStatus",triggersList);
            channelTag.put("playOnce",songsList);
            channelsList.add(channelTag);
        }
        compound.put("channels",channelsList);
        return compound;
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        this.preferredSort = MathHelper.clamp(tag.getInt("preferredSort"),1,3);
        INBT channelsListTest = tag.get("channels");
        if(channelsListTest instanceof ListNBT) {
            ListNBT channelsTag = (ListNBT)channelsListTest;
            for(INBT channelTagTest : channelsTag) {
                if(channelTagTest instanceof CompoundNBT) {
                    CompoundNBT channelTag = (CompoundNBT)channelTagTest;
                    String channel = channelTag.getString("name");
                    if(!channel.isEmpty()) {
                        this.toggleMap.put(channel,new HashMap<>());
                        this.playedOnceMap.put(channel,new HashMap<>());
                        INBT triggersListTest = channelTag.get("toggleStatus");
                        if (triggersListTest instanceof ListNBT) {
                            ListNBT triggersList = (ListNBT) triggersListTest;
                            for (INBT triggerTagTest : triggersList) {
                                if (triggerTagTest instanceof CompoundNBT) {
                                    CompoundNBT triggerTag = (CompoundNBT)triggerTagTest;
                                    String name = triggerTag.getString("name");
                                    if(!name.isEmpty())
                                        this.toggleMap.get(channel).put(name,triggerTag.getBoolean("isToggled"));
                                }
                            }
                        }
                        INBT songsListTest = channelTag.get("playOnce");
                        if (songsListTest instanceof ListNBT) {
                            ListNBT songsList = (ListNBT) songsListTest;
                            for (INBT audioTagTest : songsList) {
                                if (audioTagTest instanceof CompoundNBT) {
                                    CompoundNBT audioTag = (CompoundNBT)audioTagTest;
                                    String name = audioTag.getString("name");
                                    int timesPlayed = audioTag.getInt("timesPlayed");
                                    if(!name.isEmpty()) {
                                        INBT audioTriggersListTest = audioTag.get("triggers");
                                        if(audioTriggersListTest instanceof ListNBT) {
                                            ListNBT audioTriggersList = (ListNBT)audioTriggersListTest;
                                            Set<String> triggers = new HashSet<>();
                                            for(INBT audioTriggerTagTest : audioTriggersList)
                                                if(audioTriggerTagTest instanceof StringNBT)
                                                    triggers.add(audioTriggerTagTest.getAsString());
                                            if(!triggers.isEmpty())
                                                this.playedOnceMap.get(channel).put(name,
                                                        new Tuple<>(ImmutableList.copyOf(triggers),timesPlayed));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
