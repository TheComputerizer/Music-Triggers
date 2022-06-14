package mods.thecomputerizer.musictriggers.util.packets;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.common.ServerChannelData;
import mods.thecomputerizer.musictriggers.util.CalculateFeatures;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketQueryServerInfo implements IMessageHandler<PacketQueryServerInfo.PacketQueryServerInfoMessage, IMessage> {

    @Override
    public IMessage onMessage(PacketQueryServerInfo.PacketQueryServerInfoMessage message, MessageContext ctx) {
        if(!message.serverChannelData.isEmpty()) {
            List<ServerChannelData> serverChannelData = new ArrayList<>();
            EventsCommon.currentSongs.put(message.playerUUID(),new ArrayList<>());
            for(ServerChannelData data : message.serverChannelData) {
                serverChannelData.add(CalculateFeatures.calculateServerTriggers(data));
            }
            RegistryHandler.network.sendTo(new PacketSyncServerInfo.PacketSyncServerInfoMessage(serverChannelData), FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(message.playerUUID()));
        }
        return null;
    }

    public static class PacketQueryServerInfoMessage implements IMessage {
        private final List<Channel> clientChannels = new ArrayList<>();
        private final List<ServerChannelData> serverChannelData = new ArrayList<>();
        private UUID playerUUID;

        public PacketQueryServerInfoMessage() {
        }

        public PacketQueryServerInfoMessage(List<Channel> clientChannels) {
            this.clientChannels.addAll(clientChannels);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            int size = buf.readInt();
            for(int i=0;i<size;i++) this.serverChannelData.add(ServerChannelData.decode(buf));
            this.playerUUID = this.serverChannelData.get(0).getPlayerUUID();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(this.clientChannels.size());
            for (Channel channel : this.clientChannels) channel.encode(buf);
        }

        public List<ServerChannelData> serverData() {
            return this.serverChannelData;
        }

        public UUID playerUUID() {
            return this.playerUUID;
        }
    }
}
