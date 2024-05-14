package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;

public abstract class ChannelHelperMessage<CTX> extends PlayerMessage<CTX> {
    
    protected final ChannelHelper helper;
    
    protected ChannelHelperMessage(ChannelHelper helper) {
        super(helper.getPlayerID());
        this.helper = helper;
    }
    
    protected ChannelHelperMessage(ByteBuf buf) {
        super(buf);
        this.helper = ChannelHelper.getHelper(this.uuid,!buf.readBoolean());
    }
    
    @Override public void encode(ByteBuf buf) {
        super.encode(buf);
        buf.writeBoolean(this.helper.isClient());
    }
}
