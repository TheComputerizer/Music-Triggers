package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.nio.charset.StandardCharsets;

public class PacketJukeBoxCustom implements IPacket {

    private static final Identifier id = new Identifier(MusicTriggers.MODID, "packet_jukebox_custom");
    private final BlockPos pos;
    private final boolean start;
    private final String channel;
    private final String trackID;

    private PacketJukeBoxCustom(PacketByteBuf buf) {
        this.start = buf.readBoolean();
        this.channel = (String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8);
        this.trackID = (String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8);
        if(this.start) this.pos = BlockPos.fromLong(buf.readLong());
        else this.pos = null;
    }

    public PacketJukeBoxCustom(BlockPos pos, String channel, String id) {
        this.start = pos!=null;
        this.pos = pos;
        this.channel = channel;
        this.trackID = id;
    }

    @Override
    public PacketByteBuf encode() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(this.start);
        buf.writeInt(this.channel.length());
        buf.writeCharSequence(this.channel, StandardCharsets.UTF_8);
        buf.writeInt(this.trackID.length());
        buf.writeCharSequence(this.trackID, StandardCharsets.UTF_8);
        if(this.pos!=null) buf.writeLong(this.pos.asLong());
        return buf;
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(id,(client, handler, buf, responseSender) -> {
            PacketJukeBoxCustom packet = new PacketJukeBoxCustom(buf);
            ChannelManager.playCustomJukeboxSong(packet.start, packet.channel, packet.trackID, packet.pos);
        });
    }

    @Override
    public Identifier getID() {
        return id;
    }
}
