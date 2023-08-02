package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.server.channels.ServerTriggerStatus;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketInitChannels extends MessageImpl {

    private ServerTriggerStatus data;

    public PacketInitChannels(PacketBuffer buf) {
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
    public void encode(PacketBuffer buf) {
        this.data.encodeForServer(buf);
    }
}
