package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.client.MTClientEvents;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.TILRef;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;

public class MessageReload<CTX> extends MessageAPI<CTX> {

    public MessageReload() {}

    public MessageReload(ByteBuf ignored) {}

    @Override
    public void encode(ByteBuf buf) {}

    @Override
    public MessageAPI<CTX> handle(CTX ctx) {
        MTClientEvents.queueReload(TILRef.getClientSubAPI(ClientAPI::getMinecraft),5);
        return null;
    }
}
