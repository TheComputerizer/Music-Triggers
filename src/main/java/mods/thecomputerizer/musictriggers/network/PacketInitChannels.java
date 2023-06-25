package mods.thecomputerizer.musictriggers.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.server.channels.ServerTriggerStatus;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketInitChannels extends MessageImpl {

    private ServerTriggerStatus data;

    public PacketInitChannels() {}

    public PacketInitChannels(ServerTriggerStatus channelData) {
        this.data = channelData;
    }

    @Override
    public IMessage handle(MessageContext ctx) {
        return null;
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        ServerTriggerStatus.initializePlayerChannels(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        this.data.encodeForServer(buf);
    }
}
