package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.client.MTClient;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;

public class MessageNBTCheck<CTX> extends MessageAPI<CTX> {
    
    private final boolean placebo; //Some loaders/versions complain about empty packets
    
    public MessageNBTCheck() {
        this.placebo = true;
    }
    
    public MessageNBTCheck(ByteBuf buf) {
        this.placebo = buf.readBoolean();
    }
    
    @Override public void encode(ByteBuf buf) {
        buf.writeBoolean(this.placebo);
    }
    
    @Override public MessageAPI<CTX> handle(CTX ctx) {
        MTClient.runNBTCheck();
        return null;
    }
}
