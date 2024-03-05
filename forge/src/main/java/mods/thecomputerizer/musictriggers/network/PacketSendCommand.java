package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.client.ClientEvents;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

public class PacketSendCommand extends MessageImpl {

    private final String identifier;
    private final boolean isCommandTrigger;
    private final boolean isReload;
    private final boolean isDebug;

    public PacketSendCommand(FriendlyByteBuf buf) {
        this.identifier = NetworkUtil.readString(buf);
        this.isCommandTrigger = buf.readBoolean();
        this.isReload = buf.readBoolean();
        this.isDebug = buf.readBoolean();
    }

    public PacketSendCommand(String identifier, boolean command, boolean reload, boolean debug) {
        this.identifier = identifier;
        this.isCommandTrigger = command;
        this.isReload = reload;
        this.isDebug = debug;
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        ClientEvents.onCommand(this.identifier,this.isCommandTrigger,this.isReload,this.isDebug);
    }

    @Override
    public Dist getSide() {
        return Dist.CLIENT;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        NetworkUtil.writeString(buf,this.identifier);
        buf.writeBoolean(this.isCommandTrigger);
        buf.writeBoolean(this.isReload);
        buf.writeBoolean(this.isDebug);
    }
}
