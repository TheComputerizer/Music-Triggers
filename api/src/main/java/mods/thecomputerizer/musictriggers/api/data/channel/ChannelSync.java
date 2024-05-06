package mods.thecomputerizer.musictriggers.api.data.channel;

import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.network.MessageTriggerStates;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;

import java.util.Collection;
import java.util.HashSet;

public class ChannelSync extends ChannelElement {

    private final Collection<TriggerAPI> triggersToSync;

    public ChannelSync(ChannelAPI channel) {
        super(channel,"channel_sync");
        this.triggersToSync = new HashSet<>();
    }

    private <DIR> DIR getSyncDirection(boolean login) {
        return login ? (this.channel.isClientChannel() ? NetworkHelper.getDirToServerLogin() :
                NetworkHelper.getDirToClientLogin()) : (this.channel.isClientChannel() ?
                NetworkHelper.getDirToServer() : NetworkHelper.getDirToClient());
    }

    @Override
    public boolean isResource() {
        return false;
    }

    public void queueTriggerSync(TriggerAPI trigger) {
        if(!trigger.isSynced()) this.triggersToSync.add(trigger);
    }

    public void send() {
        //NetworkHelper.wrapMessage(getSyncDirection(false),new MessageTriggerStates<>(this.channel,this.triggersToSync)).send();
        this.triggersToSync.clear();
    }

    @Override
    public void close() {
        this.triggersToSync.clear();
    }
}