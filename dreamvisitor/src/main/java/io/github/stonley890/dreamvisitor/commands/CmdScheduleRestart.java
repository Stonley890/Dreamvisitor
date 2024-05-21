package io.github.stonley890.dreamvisitor.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CmdScheduleRestart implements DVCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // schedulerestart

        if (Dreamvisitor.restartScheduled) {
            Dreamvisitor.restartScheduled = false;
            sender.sendMessage(Dreamvisitor.PREFIX + "Canceled server restart. Run /schedulerestart again to cancel.");
        } else {
            Dreamvisitor.restartScheduled = true;
            sender.sendMessage(Dreamvisitor.PREFIX + "The server will restart when there are no players online. Run /schedulerestart again to cancel.");
        }

        return true;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "schedulerestart";
    }

    @Override
    public LiteralCommandNode<?> getNode() {
        return LiteralArgumentBuilder.literal(getCommandName()).build();
    }
}
