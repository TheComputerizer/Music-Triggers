package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketFinishedServerInit extends MessageImpl {

    private final boolean isFinished;

    public PacketFinishedServerInit(PacketBuffer buf) {
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
    public void encode(PacketBuffer buf) {
        buf.writeBoolean(this.isFinished);
    }
}
