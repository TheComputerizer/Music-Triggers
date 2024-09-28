package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.client.MTClientEvents;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.TILRef;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;

public class MessageReload<CTX> extends MessageAPI<CTX> {

    final int ticks;
    
    public MessageReload(int ticks) {
        this.ticks = ticks;
    }

    public MessageReload(ByteBuf buf) {
        this.ticks = buf.readInt();
    }

    @Override public void encode(ByteBuf buf) {
        buf.writeInt(this.ticks);
    }

    @Override public MessageAPI<CTX> handle(CTX ctx) {
        MTClientEvents.queueReload(TILRef.getClientSubAPI(ClientAPI::getMinecraft),this.ticks);
        return null;
    }
}