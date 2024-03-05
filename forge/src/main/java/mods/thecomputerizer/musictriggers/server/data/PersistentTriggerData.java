package mods.thecomputerizer.musictriggers.server.data;

import com.google.common.collect.ImmutableList;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.network.PacketMusicTriggersLogin;
import mods.thecomputerizer.musictriggers.server.channels.ServerChannelManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import org.apache.logging.log4j.Level;

import java.util.*;

public class PersistentTriggerData implements IPersistentTriggerData {

    private final Map<String,Map<String,Boolean>> toggleMap;
    private final Map<String,Map<String,Tuple<ImmutableList<String>,Integer>>> playedOnceMap;
    private int preferredSort = 1;

    public PersistentTriggerData() {
        this.toggleMap = new HashMap<>();
        this.playedOnceMap = new HashMap<>();
    }

    @Override
    public void of(PersistentTriggerData data) {
        this.toggleMap.clear();
        this.toggleMap.entrySet().addAll(data.toggleMap.entrySet());
        this.playedOnceMap.clear();
        this.playedOnceMap.entrySet().addAll(data.playedOnceMap.entrySet());
        this.preferredSort = data.preferredSort;
    }

    public void onLogin(ServerPlayer player) {
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
    public CompoundTag writeToNBT() {
        CompoundTag compound = new CompoundTag();
        compound.putInt("preferredSort",this.preferredSort);
        ListTag channelsList = new ListTag();
        for(String channel : this.toggleMap.keySet()) {
            CompoundTag channelTag = new CompoundTag();
            channelTag.putString("name",channel);
            ListTag triggersList = new ListTag();
            for(Map.Entry<String,Boolean> toggleStatus : this.toggleMap.get(channel).entrySet()) {
                CompoundTag triggerTag = new CompoundTag();
                triggerTag.putString("name",toggleStatus.getKey());
                triggerTag.putBoolean("isToggled",toggleStatus.getValue());
                triggersList.add(triggerTag);
            }
            ListTag songsList = new ListTag();
            for(Map.Entry<String,Tuple<ImmutableList<String>,Integer>> playStatus : this.playedOnceMap.get(channel).entrySet()) {
                CompoundTag audioTag = new CompoundTag();
                audioTag.putString("name",playStatus.getKey());
                ListTag audioTriggersList = new ListTag();
                for(String trigger : playStatus.getValue().getA())
                    audioTriggersList.add(StringTag.valueOf(trigger));
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
    public void readFromNBT(CompoundTag tag) {
        this.preferredSort = Mth.clamp(tag.getInt("preferredSort"),1,3);
        Tag channelsListTest = tag.get("channels");
        if(channelsListTest instanceof ListTag channelsTag) {
            for(Tag channelTagTest : channelsTag) {
                if(channelTagTest instanceof CompoundTag channelTag) {
                    String channel = channelTag.getString("name");
                    if(!channel.isEmpty()) {
                        this.toggleMap.put(channel,new HashMap<>());
                        this.playedOnceMap.put(channel,new HashMap<>());
                        Tag triggersListTest = channelTag.get("toggleStatus");
                        if (triggersListTest instanceof ListTag triggersList) {
                            for (Tag triggerTagTest : triggersList) {
                                if (triggerTagTest instanceof CompoundTag triggerTag) {
                                    String name = triggerTag.getString("name");
                                    if(!name.isEmpty())
                                        this.toggleMap.get(channel).put(name,triggerTag.getBoolean("isToggled"));
                                }
                            }
                        }
                        Tag songsListTest = channelTag.get("playOnce");
                        if (songsListTest instanceof ListTag songsList) {
                            for (Tag audioTagTest : songsList) {
                                if (audioTagTest instanceof CompoundTag audioTag) {
                                    String name = audioTag.getString("name");
                                    int timesPlayed = audioTag.getInt("timesPlayed");
                                    if(!name.isEmpty()) {
                                        Tag audioTriggersListTest = audioTag.get("triggers");
                                        if(audioTriggersListTest instanceof ListTag audioTriggersList) {
                                            Set<String> triggers = new HashSet<>();
                                            for(Tag audioTriggerTagTest : audioTriggersList)
                                                if(audioTriggerTagTest instanceof StringTag)
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
