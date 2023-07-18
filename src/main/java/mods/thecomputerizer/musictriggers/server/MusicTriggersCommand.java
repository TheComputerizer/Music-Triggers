package mods.thecomputerizer.musictriggers.server;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.network.PacketSendCommand;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;

public class MusicTriggersCommand extends CommandBase {

    private String identifier = "not_set";
    private boolean isCommandTrigger = false;
    private boolean isReload = false;
    private boolean isDebug = false;

    @Override
    @Nonnull
    public String getName() {
        return Constants.MODID;
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "Music Triggers commands initiated";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args) {
        if(args.length>=1) {
            if (args.length==1) {
                switch (args[0]) {
                    case "reload" : {
                        this.isReload = true;
                        send(sender);
                        return;
                    }
                    case "debug" : {
                        this.isDebug = true;
                        send(sender);
                        return;
                    }
                    case "commandtrigger" : {
                        notifyCommandListener(sender, this, "command.musictriggers.trigger.error");
                        send(sender);
                        return;
                    }
                    default : {
                        notifyCommandListener(sender, this, "command.musictriggers.help");
                    }
                }
            } else {
                if(args[0].matches("commandtrigger")) {
                    if(!args[1].matches("not_set")) {
                        this.isCommandTrigger = true;
                        this.identifier = args[1];
                        send(sender);
                    } else notifyCommandListener(sender, this, "command.musictriggers.trigger.not_set");
                    return;
                }
                notifyCommandListener(sender, this, "command.musictriggers.help");
            }
        }
        else notifyCommandListener(sender, this, "command.musictriggers.help");
    }

    private void send(ICommandSender sender) {
        if(sender instanceof EntityPlayerMP)
            new PacketSendCommand(this.identifier,this.isCommandTrigger,this.isReload,this.isDebug)
                    .addPlayers((EntityPlayerMP)sender).send();
        this.identifier = "not_set";
        this.isCommandTrigger = false;
        this.isReload = false;
        this.isDebug = false;
    }
}
