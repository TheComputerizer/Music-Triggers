package mods.thecomputerizer.musictriggers.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Objects;

public class PacketJukeBoxCustom extends MessageImpl {

    private BlockPos pos;
    private boolean start;
    private String channel;
    private String id;

    public PacketJukeBoxCustom() {

    }

    public PacketJukeBoxCustom(BlockPos pos, String channel, String id) {
        this.start = Objects.nonNull(pos);
        this.pos = pos;
        this.channel = channel;
        this.id = id;
    }

    @Override
    public IMessage handle(MessageContext ctx) {
        ChannelManager.playCustomJukeboxSong(this.start, this.channel, this.id, this.pos);
        return null;
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.start = buf.readBoolean();
        this.channel = NetworkUtil.readString(buf);
        this.id = NetworkUtil.readString(buf);
        if(this.start) this.pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.start);
        NetworkUtil.writeString(buf,this.channel);
        NetworkUtil.writeString(buf,this.id);
        if(Objects.nonNull(this.pos)) buf.writeLong(this.pos.toLong());
    }
}
