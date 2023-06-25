package mods.thecomputerizer.musictriggers.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.server.channels.ServerChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketRequestServerConfig extends MessageImpl {

    private boolean sendConfig;

    public PacketRequestServerConfig() {
        this.sendConfig = true;
    }

    @Override
    public IMessage handle(MessageContext messageContext) {
        return this.sendConfig ? makeSendPacket() : null;
    }

    private PacketSendServerConfig makeSendPacket() {
        return new PacketSendServerConfig(ServerChannelManager.getChannels(),ServerChannelManager.getDisabledGuiButtons());
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.sendConfig = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.sendConfig);
    }
}
