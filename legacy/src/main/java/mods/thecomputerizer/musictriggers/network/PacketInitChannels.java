package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.server.channels.ServerTriggerStatus;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

public class PacketInitChannels extends MessageImpl {

    private ServerTriggerStatus data;

    public PacketInitChannels(FriendlyByteBuf buf) {
        ServerTriggerStatus.initializePlayerChannels(buf);
    }

    public PacketInitChannels(ServerTriggerStatus channelData) {
        this.data = channelData;
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {}

    @Override
    public Dist getSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        this.data.encodeForServer(buf);
    }
}
