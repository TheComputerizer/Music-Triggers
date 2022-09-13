package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class PacketJukeBoxCustom {
    private final BlockPos pos;
    private final boolean start;
    private final String channel;
    private final String id;

    public PacketJukeBoxCustom(FriendlyByteBuf buf) {
        this.start = buf.readBoolean();
        this.channel = (String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8);
        this.id = (String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8);
        if(this.start) this.pos = BlockPos.of(buf.readLong());
        else this.pos = null;
    }

    public PacketJukeBoxCustom(BlockPos pos, String channel, String id) {
        this.start = pos!=null;
        this.pos = pos;
        this.channel = channel;
        this.id = id;
    }

    public static void encode(PacketJukeBoxCustom packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.start);
        buf.writeInt(packet.channel.length());
        buf.writeCharSequence(packet.channel, StandardCharsets.UTF_8);
        buf.writeInt(packet.id.length());
        buf.writeCharSequence(packet.id, StandardCharsets.UTF_8);
        if(packet.pos!=null) buf.writeLong(packet.pos.asLong());
    }

    public static void handle(final PacketJukeBoxCustom packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        ChannelManager.playCustomJukeboxSong(packet.start, packet.channel, packet.id, packet.pos);

        ctx.setPacketHandled(true);
    }
}
