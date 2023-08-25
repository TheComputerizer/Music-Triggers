package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;

public class ClientSync {

    private final String channel;
    private final Map<String,Boolean> triggerStatus;

    public ClientSync(String channel) {
        this.channel = channel;
        this.triggerStatus = new HashMap<>();
    }

    public ClientSync(FriendlyByteBuf buf) {
        this.channel = NetworkUtil.readString(buf);
        this.triggerStatus = NetworkUtil.readGenericMap(buf,NetworkUtil::readString, FriendlyByteBuf::readBoolean);
    }

    public void merge(ClientSync statusUpdate) {
        this.triggerStatus.putAll(statusUpdate.triggerStatus);
    }

    public String getChannel() {
        return this.channel;
    }

    public boolean isTriggerActive(Trigger trigger) {
        String name = trigger.getNameWithID();
        this.triggerStatus.putIfAbsent(name,false);
        return this.triggerStatus.get(name);
    }
}
