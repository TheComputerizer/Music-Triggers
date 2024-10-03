package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;

@Getter
public class MessageRequestChannels<CTX> extends PlayerMessage<CTX> {
    
    //final CompoundTagAPI<?> tag;
    final boolean client;
    
    public MessageRequestChannels(String uuid, boolean client) {
        super(uuid);
        //this.tag = client ? TagHelper.makeCompoundTag() : NBTHelper.readWorldData(uuid);
        this.client = client;
    }
    
    public MessageRequestChannels(ByteBuf buf) {
        super(buf);
        //this.tag = NetworkHelper.readTag(buf);
        this.client = !buf.readBoolean();
    }
    
    @Override public void encode(ByteBuf buf) {
        super.encode(buf);
        //this.tag.write(buf);
        buf.writeBoolean(this.client);
    }
    
    @SuppressWarnings("unchecked")
    @Override public MessageAPI<CTX> handle(CTX ctx) {
        MTRef.logInfo("Handling incoming channels request for UUID {}",this.uuid);
        return (MessageAPI<CTX>)ChannelHelper.processChannelsRequest(this);
    }
}