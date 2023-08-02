package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;

public class PacketJukeBoxCustom extends MessageImpl {

    private final BlockPos pos;
    private final boolean start;
    private final String channel;
    private final String id;

    public PacketJukeBoxCustom(PacketBuffer buf) {
        this.start = buf.readBoolean();
        this.channel = NetworkUtil.readString(buf);
        this.id = NetworkUtil.readString(buf);
        this.pos = this.start ? BlockPos.of(buf.readLong()) : null;
    }

    public PacketJukeBoxCustom(BlockPos pos, String channel, String id) {
        this.start = Objects.nonNull(pos);
        this.pos = pos;
        this.channel = channel;
        this.id = id;
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        ChannelManager.playCustomJukeboxSong(this.start, this.channel, this.id, this.pos);
    }

    @Override
    public Dist getSide() {
        return Dist.CLIENT;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeBoolean(this.start);
        NetworkUtil.writeString(buf,this.channel);
        NetworkUtil.writeString(buf,this.id);
        if(Objects.nonNull(this.pos)) buf.writeLong(this.pos.asLong());
    }
}
