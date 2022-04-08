package mods.thecomputerizer.musictriggers.util.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.command.CommandBase;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;

public class packetExecuteCommand implements IMessageHandler<packetExecuteCommand.packetExecuteCommandMessage, IMessage> {

    @Override
    public IMessage onMessage(packetExecuteCommand.packetExecuteCommandMessage message, MessageContext ctx)
    {
        if(message.getLiteralCommand()==null) return null;

        if(ctx.side.isServer()) {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            for(String command : server.getCommandManager().getCommands().keySet()) {
                if(server.getCommandManager().getCommands().get(command).getAliases().contains(message.getCommandName())) {
                    if(((CommandBase)server.getCommandManager().getCommands().get(command)).getRequiredPermissionLevel()<=message.getOpLevel()) {
                        server.commandManager.executeCommand(server, message.getLiteralCommand());
                    }
                }
            }
        }
        return null;
    }

    public static class packetExecuteCommandMessage implements IMessage {
        String s;

        public packetExecuteCommandMessage() {}

        public packetExecuteCommandMessage(String cmd, int level) {
            this.s = cmd+","+level;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeCharSequence(s, StandardCharsets.UTF_8);
        }

        public String getLiteralCommand() {
            if(s==null) {
                return null;
            }
            return stringBreaker(s)[0];
        }

        public int getOpLevel() {
            return Integer.parseInt(stringBreaker(s)[1]);
        }

        public String getCommandName() {
            return stringBreaker(s)[0].split(" ")[0].replaceAll("/","");
        }

        public static String[] stringBreaker(String s) {
            return s.split(",");
        }
    }
}
