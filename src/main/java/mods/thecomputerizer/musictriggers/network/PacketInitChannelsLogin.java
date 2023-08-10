package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.server.channels.ServerTriggerStatus;
import net.minecraft.network.FriendlyByteBuf;

public class PacketInitChannelsLogin extends PacketInitChannels {

    public PacketInitChannelsLogin(FriendlyByteBuf buf) {
        super(buf);
    }

    public PacketInitChannelsLogin(ServerTriggerStatus channelData) {
        super(channelData);
    }
}
