package mods.thecomputerizer.musictriggers.network.packets;

import mods.thecomputerizer.musictriggers.client.ClientEvents;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public class PacketReceiveCommand implements IPacket {

    private final String identifier;

    private PacketReceiveCommand(FriendlyByteBuf buf) {
        this.identifier = NetworkUtil.readString(buf);
    }

    public PacketReceiveCommand(String identifier) {
        this.identifier = identifier;
    }

    public FriendlyByteBuf encode() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        NetworkUtil.writeString(buf,this.identifier);
        return buf;
    }

    public static ClientPlayNetworking.PlayChannelHandler handle() {
        return (client, handler, buf, sender) -> {
            PacketReceiveCommand packet = new PacketReceiveCommand(buf);
            ClientEvents.COMMAND_MAP.put(packet.identifier, true);
        };
    }
}
