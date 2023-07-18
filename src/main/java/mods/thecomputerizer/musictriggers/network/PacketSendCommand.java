package mods.thecomputerizer.musictriggers.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.client.ClientEvents;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketSendCommand extends MessageImpl {

    private String identifier = "not_set";
    private boolean isCommandTrigger = false;
    private boolean isReload = false;
    private boolean isDebug = false;

    public PacketSendCommand() {}

    public PacketSendCommand(String identifier, boolean command, boolean reload, boolean debug) {
        this.identifier = identifier;
        this.isCommandTrigger = command;
        this.isReload = reload;
        this.isDebug = debug;
    }

    @Override
    public IMessage handle(MessageContext messageContext) {
        ClientEvents.onCommand(this.identifier,this.isCommandTrigger,this.isReload,this.isDebug);
        return null;
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.identifier = NetworkUtil.readString(buf);
        this.isCommandTrigger = buf.readBoolean();
        this.isReload = buf.readBoolean();
        this.isDebug = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkUtil.writeString(buf,this.identifier);
        buf.writeBoolean(this.isCommandTrigger);
        buf.writeBoolean(this.isReload);
        buf.writeBoolean(this.isDebug);
    }
}
