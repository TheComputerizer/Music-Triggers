package mods.thecomputerizer.musictriggers.server;

import mods.thecomputerizer.musictriggers.core.Constants;
import mods.thecomputerizer.musictriggers.network.PacketSendCommand;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;
import java.util.Objects;

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
        if(args.length>=2) {
            String playerSelector = args[1];
            EntityPlayerMP player;
            try {
                player = getPlayer(server,sender,args[1]);
            } catch (CommandException ex) {
                player = null;
                Constants.MAIN_LOG.error("There was an error finding player from selector {} in command!",playerSelector,ex);
            }
            if(Objects.nonNull(player)) {
                if (args.length == 2) {
                    switch (args[1]) {
                        case "reload": {
                            this.isReload = true;
                            send(player);
                            notifyCommandListener(sender, this, "command.musictriggers.success.reload");
                            return;
                        }
                        case "debug": {
                            this.isDebug = true;
                            send(player);
                            notifyCommandListener(sender, this, "command.musictriggers.success.debug");
                            return;
                        }
                        case "commandtrigger": {
                            notifyIssue(sender,"trigger.error");
                            return;
                        }
                        default: notifyIssue(sender,"help");
                    }
                } else {
                    if (args[1].matches("commandtrigger")) {
                        if (!args[2].matches("not_set")) {
                            this.isCommandTrigger = true;
                            this.identifier = args[2];
                            send(player);
                        } else notifyIssue(sender,"trigger.not_set");
                    } else notifyIssue(sender,"help");
                }
            } else notifyIssue(sender,"player.error");
        }
        else notifyIssue(sender,"help");
    }

    private void send(EntityPlayerMP player) {
        new PacketSendCommand(this.identifier,this.isCommandTrigger,this.isReload,this.isDebug)
                .addPlayers(player).send();
        reset();
    }

    private void notifyIssue(@Nonnull ICommandSender sender, String key) {
        notifyCommandListener(sender,this,"command.musictriggers."+key);
        reset();
    }

    private void reset() {
        this.identifier = "not_set";
        this.isCommandTrigger = false;
        this.isReload = false;
        this.isDebug = false;
    }
}
