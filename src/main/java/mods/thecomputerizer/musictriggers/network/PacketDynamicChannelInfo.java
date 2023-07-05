package mods.thecomputerizer.musictriggers.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.client.channels.Channel;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.musictriggers.server.channels.ServerTriggerStatus;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

public class PacketDynamicChannelInfo extends MessageImpl {

    private final List<Channel> clientChannels = new ArrayList<>();

    public PacketDynamicChannelInfo() {
    }

    public PacketDynamicChannelInfo(List<Channel> clientChannels) {
        this.clientChannels.addAll(clientChannels);
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
        ServerTriggerStatus.decodeDynamicInfo(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkUtil.writeString(buf, Minecraft.getMinecraft().player.getUniqueID().toString());
        NetworkUtil.writeGenericList(buf,this.clientChannels,(buf1, channel) -> channel.encodeDynamic(buf1));
        buf.writeInt(Instance.getPreferredSort());
    }
}
