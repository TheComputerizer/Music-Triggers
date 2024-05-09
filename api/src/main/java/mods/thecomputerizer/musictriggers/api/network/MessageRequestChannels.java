package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;

import java.util.Objects;

public class MessageRequestChannels<CTX> extends MessageAPI<CTX> {
    
    final String uuid;
    final boolean client;
    
    public MessageRequestChannels(String uuid, boolean client) {
        this.uuid = uuid;
        this.client = client;
    }
    
    public MessageRequestChannels(ByteBuf buf) {
        this.uuid = NetworkHelper.readString(buf);
        this.client = !buf.readBoolean();
    }
    
    @Override public void encode(ByteBuf buf) {
        NetworkHelper.writeString(buf,this.uuid);
        buf.writeBoolean(this.client);
    }
    
    @SuppressWarnings("unchecked")
    @Override public MessageAPI<CTX> handle(CTX ctx) {
        MTRef.logInfo("Handling incoming channels request for UUID {}",this.uuid);
        ChannelHelper helper = ChannelHelper.getHelper(this.uuid,this.client);
        if(Objects.nonNull(helper)) return (MessageAPI<CTX>)helper.getInitMessage();
        return null;
    }
}