package mods.thecomputerizer.musictriggers.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketFinishedServerInit extends MessageImpl {

    private boolean isFinished = true;
    @Override
    public IMessage handle(MessageContext messageContext) {
        ChannelManager.finalizeServerChannelInit();
        return null;
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.isFinished = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.isFinished);
    }
}
