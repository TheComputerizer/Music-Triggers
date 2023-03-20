package mods.thecomputerizer.musictriggers.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;

public class TriggerCommand extends CommandBase {

    private String identifier = "_";

    @Override
    @Nonnull
    public String getName() {
        return "commandtrigger";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "Command Trigger initiated";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args) {
        if (args.length==1) this.identifier = args[0];
        else notifyCommandListener(sender, this, "Usage: '/commandtrigger identifier'");
    }

    public String getIdentifier() {
        return this.identifier;
    }
}
