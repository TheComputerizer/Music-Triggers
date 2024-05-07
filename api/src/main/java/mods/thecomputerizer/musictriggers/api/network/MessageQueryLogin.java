package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;

import java.util.Objects;

public class MessageQueryLogin<CTX> extends MessageAPI<CTX> {
    
    final String uuid;
    
    public MessageQueryLogin(String uuid) {
        this.uuid = uuid;
    }
    
    public MessageQueryLogin(ByteBuf buf) {
        this.uuid = NetworkHelper.readString(buf);
    }
    
    @Override public void encode(ByteBuf buf) {
        NetworkHelper.writeString(buf,this.uuid);
    }
    
    @Override public MessageAPI<CTX> handle(CTX ctx) {
        ChannelHelper helper = ChannelHelper.getClientHelper(this.uuid);
        if(Objects.nonNull(helper)) helper.sendInitMessage(true);
        return null;
    }
}