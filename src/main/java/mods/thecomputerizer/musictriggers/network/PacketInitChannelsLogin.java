package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.server.channels.ServerTriggerStatus;
import net.minecraft.network.PacketBuffer;

public class PacketInitChannelsLogin extends PacketInitChannels {

    public PacketInitChannelsLogin(PacketBuffer buf) {
        super(buf);
    }

    public PacketInitChannelsLogin(ServerTriggerStatus channelData) {
        super(channelData);
    }
}
