package mods.thecomputerizer.musictriggers.api.data.channel;

import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.network.MTNetwork;
import mods.thecomputerizer.musictriggers.api.network.MessageTriggerStates;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.MinecraftAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.TILRef;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class ChannelSync extends ChannelElement {

    private final Collection<TriggerAPI> triggersToSync;

    public ChannelSync(ChannelAPI channel) {
        super(channel,"channel_sync");
        this.triggersToSync = new HashSet<>();
    }

    @Override
    public boolean isResource() {
        return false;
    }

    public void queueTriggerSync(TriggerAPI trigger) {
        if(!trigger.isSynced()) this.triggersToSync.add(trigger);
    }

    public void send() {
        if(this.triggersToSync.isEmpty()) return;
        MessageTriggerStates<?> msg = new MessageTriggerStates<>(this.channel,this.triggersToSync);
        if(this.channel.isClientChannel()) {
            MinecraftAPI mc = TILRef.getClientSubAPI(ClientAPI::getMinecraft);
            if(Objects.nonNull(mc) && Objects.nonNull(mc.getPlayer()))
                MTNetwork.sendToServer(msg,false);
        }
        else MTNetwork.sendToClient(msg,false,this.channel.getPlayerEntity());
        this.triggersToSync.clear();
    }

    @Override
    public void close() {
        this.triggersToSync.clear();
    }
}