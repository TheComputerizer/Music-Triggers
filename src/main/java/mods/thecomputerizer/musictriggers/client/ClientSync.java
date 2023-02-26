package mods.thecomputerizer.musictriggers.client;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;

import java.util.HashMap;
import java.util.Map;

public class ClientSync {

    private final String channel;
    private final Map<String,Boolean> triggerStatus;

    public ClientSync(String channel) {
        this.channel = channel;
        this.triggerStatus = new HashMap<>();
    }

    public ClientSync(ByteBuf buf) {
        this.channel = NetworkUtil.readString(buf);
        this.triggerStatus = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,ByteBuf::readBoolean);
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
