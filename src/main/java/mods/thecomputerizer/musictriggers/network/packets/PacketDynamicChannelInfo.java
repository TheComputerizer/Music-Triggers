package mods.thecomputerizer.musictriggers.network.packets;

import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.server.data.ServerChannels;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class PacketDynamicChannelInfo {

    private final List<Channel> clientChannels = new ArrayList<>();

    public PacketDynamicChannelInfo(FriendlyByteBuf buf) {
        ServerChannels.decodeDynamicInfo(buf);
    }

    public PacketDynamicChannelInfo(List<Channel> clientChannels) {
        this.clientChannels.addAll(clientChannels);
    }

    public static void encode(PacketDynamicChannelInfo packet, FriendlyByteBuf buf) {
        NetworkUtil.writeString(buf, Minecraft.getInstance().player.getUUID().toString());
        NetworkUtil.writeGenericList(buf,packet.clientChannels,(buf1, channel) -> channel.encodeDynamic(buf1));
    }

    public static void handle(final PacketDynamicChannelInfo packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });
        ctx.setPacketHandled(true);
    }
}
