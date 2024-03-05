package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

public class PacketFinishedServerInit extends MessageImpl {

    private final boolean isFinished;

    public PacketFinishedServerInit(FriendlyByteBuf buf) {
        this.isFinished = buf.readBoolean();
    }

    public PacketFinishedServerInit() {
        this.isFinished = true;
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        ChannelManager.finalizeServerChannelInit();
    }

    @Override
    public Dist getSide() {
        return Dist.CLIENT;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.isFinished);
    }
}
