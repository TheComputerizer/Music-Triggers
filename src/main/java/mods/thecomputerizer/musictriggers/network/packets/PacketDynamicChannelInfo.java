package mods.thecomputerizer.musictriggers.network.packets;

import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.server.ServerData;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

import java.util.*;

@SuppressWarnings("unused")
public class PacketDynamicChannelInfo implements IPacket {

    private final List<Channel> clientChannels = new ArrayList<>();

    private PacketDynamicChannelInfo(FriendlyByteBuf buf) {
        ServerData.decodeDynamicInfo(buf);
    }

    public PacketDynamicChannelInfo(List<Channel> clientChannels) {
        this.clientChannels.addAll(clientChannels);
    }

    public FriendlyByteBuf encode() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        NetworkUtil.writeString(buf, Minecraft.getInstance().player.getUUID().toString());
        NetworkUtil.writeGenericList(buf,this.clientChannels,(buf1, channel) -> channel.encodeDynamic(buf1));
        return buf;
    }

    public static ServerPlayNetworking.PlayChannelHandler handle() {
        return (server, player, handler, buf, sender) -> new PacketDynamicChannelInfo(buf);
    }
}
