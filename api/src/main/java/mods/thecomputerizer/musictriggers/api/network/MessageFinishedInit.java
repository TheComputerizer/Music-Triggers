package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;

public class MessageFinishedInit<CTX> extends ChannelHelperMessage<CTX> {
    
    public MessageFinishedInit(ChannelHelper helper) {
        super(helper);
    }
    
    public MessageFinishedInit(ByteBuf buf) {
        super(buf);
    }
    
    @Override public MessageAPI<CTX> handle(CTX ctx) {
        this.helper.setSyncable(true);
        return null;
    }
}
