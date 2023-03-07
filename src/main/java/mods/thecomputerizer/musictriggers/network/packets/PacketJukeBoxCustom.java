package mods.thecomputerizer.musictriggers.network.packets;

import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class PacketJukeBoxCustom implements IPacket {
    private final BlockPos pos;
    private final boolean start;
    private final String channel;
    private final String id;

    private PacketJukeBoxCustom(FriendlyByteBuf buf) {
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

    public FriendlyByteBuf encode() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(this.start);
        NetworkUtil.writeString(buf,this.channel);
        NetworkUtil.writeString(buf,this.id);
        if(this.pos!=null) buf.writeLong(this.pos.asLong());
        return buf;
    }

    public static ClientPlayNetworking.PlayChannelHandler handle() {
        return (client, handler, buf, sender) -> {
            PacketJukeBoxCustom packet = new PacketJukeBoxCustom(buf);
            ChannelManager.playCustomJukeboxSong(packet.start, packet.channel, packet.id, packet.pos);
        };
    }
}
