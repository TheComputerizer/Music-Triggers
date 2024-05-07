package mods.thecomputerizer.musictriggers.api.data.channel;

import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerCombination;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerMerged;
import mods.thecomputerizer.musictriggers.api.network.MTNetwork;
import mods.thecomputerizer.musictriggers.api.network.MessageTriggerStates;

import java.util.Collection;
import java.util.HashSet;

public class ChannelSync extends ChannelElement {

    private final Collection<TriggerAPI> triggersToSync;

    public ChannelSync(ChannelAPI channel) {
        super(channel,"channel_sync");
        this.triggersToSync = new HashSet<>();
    }
    
    @Override protected String getSubTypeName() {
        return "Sync";
    }
    
    @Override
    public boolean isResource() {
        return false;
    }

    public void queueTriggerSync(TriggerAPI trigger) {
        if(!trigger.isSynced() && !(trigger instanceof TriggerCombination) && !(trigger instanceof TriggerMerged))
            this.triggersToSync.add(trigger);
    }

    public void send() {
        if(this.triggersToSync.isEmpty() || !this.channel.getHelper().isSyncable()) return;
        MessageTriggerStates<?> msg = new MessageTriggerStates<>(this.channel,this.triggersToSync);
        if(this.channel.isClientChannel()) MTNetwork.sendToServer(msg,false);
        else MTNetwork.sendToClient(msg,false,this.channel.getPlayerEntity());
        this.triggersToSync.clear();
    }

    @Override
    public void close() {
        this.triggersToSync.clear();
    }
    
    @Override protected TableRef getReferenceData() {
        return null;
    }
    
    @Override protected Class<? extends ParameterWrapper> getTypeClass() {
        return ChannelSync.class;
    }
}