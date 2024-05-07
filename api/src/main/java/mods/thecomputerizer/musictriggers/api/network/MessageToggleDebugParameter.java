package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;

public class MessageToggleDebugParameter<CTX> extends MessageAPI<CTX> {
    
    final String name;
    
    public MessageToggleDebugParameter(String name) {
        this.name = name;
    }
    
    public MessageToggleDebugParameter(ByteBuf buf) {
        this.name = NetworkHelper.readString(buf);
    }
    
    @Override
    public void encode(ByteBuf buf) {
        NetworkHelper.writeString(buf,this.name);
    }
    
    @Override
    public MessageAPI<CTX> handle(CTX ctx) {
        ChannelHelper.forEach(helper -> helper.flipDebugParameter(this.name));
        return null;
    }
}