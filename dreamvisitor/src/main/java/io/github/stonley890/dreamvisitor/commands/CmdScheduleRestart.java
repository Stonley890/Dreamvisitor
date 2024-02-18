package io.github.stonley890.dreamvisitor.commands;

import io.github.stonley890.dreamvisitor.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CmdScheduleRestart implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (Main.restartScheduled) {
            Main.restartScheduled = false;
            sender.sendMessage(Main.PREFIX + "Canceled server restart. Run /schedulerestart again to cancel.");
        } else {
            Main.restartScheduled = true;
            sender.sendMessage(Main.PREFIX + "The server will restart when there are no players online. Run /schedulerestart again to cancel.");
        }

        return true;
    }
}
