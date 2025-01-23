package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import io.github.stonley890.dreamvisitor.functions.ScheduleRestart;
import io.github.stonley890.dreamvisitor.functions.SystemMessage;
import org.jetbrains.annotations.NotNull;

public class CmdScheduleRestart implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("schedulerestart")
                .withPermission(CommandPermission.OP)
                .withHelp("Schedule a restart.", "Schedule the server to restart when no players are online.")
                .executesNative((sender, args) -> {
                    if (ScheduleRestart.isRestartScheduled()) {
                        ScheduleRestart.setRestartScheduled(false);
                        sender.sendMessage(SystemMessage.formatPrivate("Canceled server restart. Run /schedulerestart again to cancel."));
                    } else {
                        ScheduleRestart.setRestartScheduled(true);
                        sender.sendMessage(SystemMessage.formatPrivate("The server will restart when there are no players online. Run /schedulerestart again to cancel."));
                    }
                });
    }
}
