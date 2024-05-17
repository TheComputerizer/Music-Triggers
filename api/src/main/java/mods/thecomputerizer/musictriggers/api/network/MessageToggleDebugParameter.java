package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;

public class MessageToggleDebugParameter<CTX> extends MessageAPI<CTX> {
    
    final boolean client;
    final String name;
    
    public MessageToggleDebugParameter(boolean client, String name) {
        this.client = client;
        this.name = name;
        ChannelHelper.flipDebugParameter(this.client,this.name);
    }
    
    public MessageToggleDebugParameter(ByteBuf buf) {
        this.client = !buf.readBoolean();
        this.name = NetworkHelper.readString(buf);
    }
    
    @Override
    public void encode(ByteBuf buf) {
        buf.writeBoolean(this.client);
        NetworkHelper.writeString(buf,this.name);
    }
    
    @Override
    public MessageAPI<CTX> handle(CTX ctx) {
        ChannelHelper.flipDebugParameter(this.client,this.name);
        return null;
    }
}