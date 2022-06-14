package mods.thecomputerizer.musictriggers.util.packets;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.common.ServerChannelData;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class PacketSyncServerInfo implements IMessageHandler<PacketSyncServerInfo.PacketSyncServerInfoMessage, IMessage> {

    @Override
    public IMessage onMessage(PacketSyncServerInfo.PacketSyncServerInfoMessage message, MessageContext ctx) {
        for(ClientSync sync : message.clientReturnInfo) ChannelManager.syncInfoFromServer(sync);
        return null;
    }

    public static class PacketSyncServerInfoMessage implements IMessage {
        private final List<ServerChannelData> serverChannelData = new ArrayList<>();
        private final List<ClientSync> clientReturnInfo = new ArrayList<>();

        public PacketSyncServerInfoMessage() {
        }

        public PacketSyncServerInfoMessage(List<ServerChannelData> serverChannelData) {
            this.serverChannelData.addAll(serverChannelData);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            int size = buf.readInt();
            for(int i=0;i<size;i++) this.clientReturnInfo.add(new ClientSync(buf));
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(this.serverChannelData.size());
            for (ServerChannelData channel : this.serverChannelData) channel.encode(buf);
        }
    }
}
