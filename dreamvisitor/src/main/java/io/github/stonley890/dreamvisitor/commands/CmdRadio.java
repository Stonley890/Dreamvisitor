package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import io.github.stonley890.dreamvisitor.functions.Radio;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

public class CmdRadio implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("radio")
                .withPermission("dreamvisitor.radio")
                .withHelp("Send a message using the radio.", "Send a message to all other players who can access the radio.")
                .withArguments(new GreedyStringArgument("message"))
                .executesNative((sender, args) -> {

                    String message = (String) args.get("message");

                    CommandSender callee = sender.getCallee();
                    if (callee instanceof Player player) {
                        Radio.buildMessage(message, player.getName(), Radio.Type.STANDARD, null);
                    } else if (callee instanceof ConsoleCommandSender) {
                        Radio.buildMessage(message, "Console", Radio.Type.STANDARD, null);
                    } else {
                        throw CommandAPI.failWithString("This command can only be executed by a player or the console!");
                    }
                });
    }
}
