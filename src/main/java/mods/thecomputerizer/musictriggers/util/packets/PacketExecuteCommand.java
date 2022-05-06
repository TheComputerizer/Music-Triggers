package mods.thecomputerizer.musictriggers.util.packets;

import com.ibm.icu.impl.ReplaceableUCharacterIterator;
import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.command.CommandBase;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;

public class PacketExecuteCommand implements IMessageHandler<PacketExecuteCommand.packetExecuteCommandMessage, IMessage> {

    @Override
    public IMessage onMessage(PacketExecuteCommand.packetExecuteCommandMessage message, MessageContext ctx)
    {
        if(message.s==null) return null;

        if(ctx.side.isServer()) {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            FMLCommonHandler.instance().getMinecraftServerInstance().commandManager.executeCommand(server, message.s);
        }
        return null;
    }

    public static class packetExecuteCommandMessage implements IMessage {
        String s;

        public packetExecuteCommandMessage() {}

        public packetExecuteCommandMessage(String cmd) {
            this.s = cmd;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeCharSequence(s, StandardCharsets.UTF_8);
        }

        public static String[] stringBreaker(String s) {
            return s.split(",");
        }
    }
}
