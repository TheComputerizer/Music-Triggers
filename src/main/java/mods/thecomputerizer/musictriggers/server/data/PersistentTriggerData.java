package mods.thecomputerizer.musictriggers.server.data;

import com.google.common.collect.ImmutableList;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.network.PacketMusicTriggersLogin;
import mods.thecomputerizer.musictriggers.server.channels.ServerChannelManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.Level;

import java.util.*;

public class PersistentTriggerData implements IPersistentTriggerData {

    private final Map<String,Map<String,Boolean>> toggleMap;
    private final Map<String,Map<String,ImmutableList<String>>> playedOnceMap;
    private int preferredSort = 1;

    public PersistentTriggerData() {
        this.toggleMap = new HashMap<>();
        this.playedOnceMap = new HashMap<>();
    }

    public void onLogin(EntityPlayerMP player) {
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
    public void setAudioPlayed(String channel, String audio, List<String> audioTriggers) {
        if(this.playedOnceMap.containsKey(channel)) {
            if (Objects.nonNull(audio))
                this.playedOnceMap.get(channel).put(audio, ImmutableList.copyOf(audioTriggers));
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
    public NBTTagCompound writeToNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("preferredSort",this.preferredSort);
        NBTTagList channelsList = new NBTTagList();
        for(String channel : this.toggleMap.keySet()) {
            NBTTagCompound channelTag = new NBTTagCompound();
            channelTag.setString("name",channel);
            NBTTagList triggersList = new NBTTagList();
            for(Map.Entry<String,Boolean> toggleStatus : this.toggleMap.get(channel).entrySet()) {
                NBTTagCompound triggerTag = new NBTTagCompound();
                triggerTag.setString("name",toggleStatus.getKey());
                triggerTag.setBoolean("isToggled",toggleStatus.getValue());
                triggersList.appendTag(triggerTag);
            }
            NBTTagList songsList = new NBTTagList();
            for(Map.Entry<String,ImmutableList<String>> playStatus : this.playedOnceMap.get(channel).entrySet()) {
                NBTTagCompound audioTag = new NBTTagCompound();
                audioTag.setString("name",playStatus.getKey());
                NBTTagList audioTriggersList = new NBTTagList();
                for(String trigger : playStatus.getValue())
                    audioTriggersList.appendTag(new NBTTagString(trigger));
                audioTag.setTag("triggers",audioTriggersList);
                songsList.appendTag(audioTag);
            }
            channelTag.setTag("toggleStatus",triggersList);
            channelTag.setTag("playOnce",songsList);
            channelsList.appendTag(channelTag);
        }
        compound.setTag("channels",channelsList);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        this.preferredSort = MathHelper.clamp(tag.getInteger("preferredSort"),1,3);
        NBTBase channelsListTest = tag.getTag("channels");
        if(channelsListTest instanceof NBTTagList) {
            NBTTagList channelsTag = (NBTTagList)channelsListTest;
            for(NBTBase channelTagTest : channelsTag) {
                if(channelTagTest instanceof NBTTagCompound) {
                    NBTTagCompound channelTag = (NBTTagCompound)channelTagTest;
                    String channel = channelTag.getString("name");
                    if(!channel.isEmpty()) {
                        this.toggleMap.put(channel,new HashMap<>());
                        this.playedOnceMap.put(channel,new HashMap<>());
                        NBTBase triggersListTest = channelTag.getTag("toggleStatus");
                        if (triggersListTest instanceof NBTTagList) {
                            NBTTagList triggersList = (NBTTagList) triggersListTest;
                            for (NBTBase triggerTagTest : triggersList) {
                                if (triggerTagTest instanceof NBTTagCompound) {
                                    NBTTagCompound triggerTag = (NBTTagCompound)triggerTagTest;
                                    String name = triggerTag.getString("name");
                                    if(!name.isEmpty())
                                        this.toggleMap.get(channel).put(name,triggerTag.getBoolean("isToggled"));
                                }
                            }
                        }
                        NBTBase songsListTest = channelTag.getTag("playOnce");
                        if (songsListTest instanceof NBTTagList) {
                            NBTTagList songsList = (NBTTagList) songsListTest;
                            for (NBTBase audioTagTest : songsList) {
                                if (audioTagTest instanceof NBTTagCompound) {
                                    NBTTagCompound audioTag = (NBTTagCompound)audioTagTest;
                                    String name = audioTag.getString("name");
                                    if(!name.isEmpty()) {
                                        NBTBase audioTriggersListTest = audioTag.getTag("triggers");
                                        if(audioTriggersListTest instanceof NBTTagList) {
                                            NBTTagList audioTriggersList = (NBTTagList)audioTriggersListTest;
                                            Set<String> triggers = new HashSet<>();
                                            for(NBTBase audioTriggerTagTest : audioTriggersList)
                                                if(audioTriggerTagTest instanceof NBTTagString)
                                                    triggers.add(((NBTTagString)audioTriggerTagTest).getString());
                                            if(!triggers.isEmpty())
                                                this.playedOnceMap.get(channel).put(name,ImmutableList.copyOf(triggers));
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
