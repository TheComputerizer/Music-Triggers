package mods.thecomputerizer.musictriggers.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.server.channels.ServerChannel;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

public class PacketSendServerConfig extends MessageImpl {

    private List<ServerChannel> serverChannels;
    private List<String> disabledGuiButtons;

    public PacketSendServerConfig() {}

    public PacketSendServerConfig(List<ServerChannel> serverChannels, List<String> disabledGuiButtons) {
        this.serverChannels = serverChannels;
        this.disabledGuiButtons = disabledGuiButtons;
    }

    @Override
    public IMessage handle(MessageContext messageContext) {
        ChannelManager.addServerChannels(this.serverChannels,this.disabledGuiButtons);
        return null;
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.serverChannels = NetworkUtil.readGenericList(buf,ServerChannel::new);
        this.disabledGuiButtons = NetworkUtil.readGenericList(buf,NetworkUtil::readString);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkUtil.writeGenericList(buf,this.serverChannels,(buf1, channel) -> channel.encode(buf1));
        NetworkUtil.writeGenericList(buf,this.disabledGuiButtons,NetworkUtil::writeString);
    }
}
