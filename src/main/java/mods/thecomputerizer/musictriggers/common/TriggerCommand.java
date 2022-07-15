package mods.thecomputerizer.musictriggers.common;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class TriggerCommand extends CommandBase {

    private String identifier = "_";

    @Override
    public String getName() {
        return "commandtrigger";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Command Trigger initiated";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length==1) this.identifier = args[0];
        else notifyCommandListener(sender, this, "Usage: '/commandtrigger identifier'");
    }

    public String getIdentifier() {
        return this.identifier;
    }
}
