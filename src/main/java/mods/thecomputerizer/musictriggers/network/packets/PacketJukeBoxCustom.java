package mods.thecomputerizer.musictriggers.network.packets;

import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketJukeBoxCustom {
    private final BlockPos pos;
    private final boolean start;
    private final String channel;
    private final String id;

    public PacketJukeBoxCustom(FriendlyByteBuf buf) {
        this.start = buf.readBoolean();
        this.channel = NetworkUtil.readString(buf);
        this.id = NetworkUtil.readString(buf);
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
        NetworkUtil.writeString(buf,packet.channel);
        NetworkUtil.writeString(buf,packet.id);
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
