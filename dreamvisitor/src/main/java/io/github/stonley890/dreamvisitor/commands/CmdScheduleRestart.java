package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;

public class CmdScheduleRestart implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("schedulerestart")
                .withPermission(CommandPermission.OP)
                .withHelp("Schedule a restart.", "Schedule the server to restart when no players are online.")
                .executesNative((sender, args) -> {
                    if (Dreamvisitor.restartScheduled) {
                        Dreamvisitor.restartScheduled = false;
                        sender.sendMessage(Dreamvisitor.PREFIX + "Canceled server restart. Run /schedulerestart again to cancel.");
                    } else {
                        Dreamvisitor.restartScheduled = true;
                        sender.sendMessage(Dreamvisitor.PREFIX + "The server will restart when there are no players online. Run /schedulerestart again to cancel.");
                    }
                });
    }
}
