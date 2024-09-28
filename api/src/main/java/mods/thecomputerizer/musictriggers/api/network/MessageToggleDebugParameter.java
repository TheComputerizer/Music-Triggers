package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;

public class MessageToggleDebugParameter<CTX> extends MessageAPI<CTX> {
    
    final boolean client;
    final String name;
    final boolean value;
    
    public MessageToggleDebugParameter(boolean client, String name) {
        this.client = client;
        this.name = name;
        ChannelHelper.flipDebugParameter(this.client,this.name);
        this.value = ChannelHelper.getDebugBool(this.name);
    }
    
    public MessageToggleDebugParameter(ByteBuf buf) {
        this.client = !buf.readBoolean();
        this.name = NetworkHelper.readString(buf);
        this.value = buf.readBoolean();
    }
    
    @Override public void encode(ByteBuf buf) {
        buf.writeBoolean(this.client);
        NetworkHelper.writeString(buf,this.name);
        buf.writeBoolean(this.value);
    }
    
    @Override public MessageAPI<CTX> handle(CTX ctx) {
        ChannelHelper.setDebugParameter(this.client,this.name,this.value);
        return null;
    }
}