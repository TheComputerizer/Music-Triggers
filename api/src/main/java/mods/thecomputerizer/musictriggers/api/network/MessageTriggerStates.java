package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI.State;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.iterator.IterableHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MessageTriggerStates<CTX> extends ChannelHelperMessage<CTX> {
    
    private final Map<ChannelAPI,Map<TriggerAPI,State>> triggerMap;

    public MessageTriggerStates(ChannelHelper helper) {
        super(helper);
        this.triggerMap = new HashMap<>();
    }

    public MessageTriggerStates(ByteBuf buf) {
        super(buf);
        this.triggerMap = NetworkHelper.readMapEntries(buf,() -> {
            ChannelAPI channel = this.helper.findChannel(ChannelHelper.getGlobalData(),NetworkHelper.readString(buf));
            Map<TriggerAPI,State> stateMap = NetworkHelper.readMap(buf,() -> {
                String name = NetworkHelper.readString(buf);
                String id = NetworkHelper.readString(buf);
                return TriggerHelper.decodeTrigger(channel,name,id);
            },() -> State.valueOf(NetworkHelper.readString(buf)));
            return IterableHelper.getMapEntry(channel,stateMap);
        });
    }
    
    public void addStates(Collection<TriggerAPI> triggers) {
        for(TriggerAPI trigger : triggers) {
            ChannelAPI channel = trigger.getChannel();
            this.triggerMap.putIfAbsent(channel,new HashMap<>());
            this.triggerMap.get(channel).put(trigger,trigger.getState());
        }
    }

    @Override public void encode(ByteBuf buf) {
        super.encode(buf);
        NetworkHelper.writeMap(buf,this.triggerMap,channel -> NetworkHelper.writeString(buf,channel.getName()),
                               stateMap -> NetworkHelper.writeMap(buf,stateMap,trigger -> trigger.encode(buf),
                                       state -> NetworkHelper.writeString(buf,state.name())));
    }

    @Override public MessageAPI<CTX> handle(CTX ctx) {
        this.triggerMap.forEach(ChannelAPI::updateSyncedState);
        return null;
    }
    
    public boolean readyToSend() {
        if(!this.triggerMap.isEmpty() && this.helper.isSyncable())
            for(Map<TriggerAPI,State> stateMap : this.triggerMap.values())
                if(!stateMap.isEmpty()) return true;
        return false;
    }
}