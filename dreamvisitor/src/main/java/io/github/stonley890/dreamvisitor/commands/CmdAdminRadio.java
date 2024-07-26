package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.ExecutableCommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.functions.Radio;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdAdminRadio implements DVCommand {

    @NotNull
    @Override
    public ExecutableCommand<?, ?> getCommand() {
        return new CommandAPICommand("aradio")
                .withPermission(CommandPermission.OP)
                .withHelp("Use the admin radio.", "Sends a message to all operators.")
                .withArguments(new GreedyStringArgument("message"))
                .executesNative(((sender, args) -> {
                    String message = (String) args.get("message");

                    Dreamvisitor.debug(sender.getClass().getName());

                    CommandSender callee = sender.getCallee();
                    if (callee instanceof Player player) {
                        Radio.buildMessage(message, player.getName(), getCommand().getName(), null);
                    } else if (callee instanceof ConsoleCommandSender) {
                        Radio.buildMessage(message, "Console",  getCommand().getName(), null);
                    } else {
                        throw CommandAPI.failWithString("This command can only be executed by a player or the console!");
                    }
                }));
    }
}
