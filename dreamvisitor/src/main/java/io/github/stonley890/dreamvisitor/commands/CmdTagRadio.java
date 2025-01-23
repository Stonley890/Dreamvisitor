package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.stonley890.dreamvisitor.functions.Radio;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdTagRadio implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("tagradio")
                .withPermission("dreamvisitor.tagradio")
                .withHelp("Send a message to all players with a tag.", "Send a message to all players with a given tag.")
                .withArguments(new StringArgument("tag"))
                .withArguments(new GreedyStringArgument("message"))
                .executesNative((sender, args) -> {
                    String tag = (String) args.get("tag");
                    if (tag == null) throw CommandAPI.failWithString("You must specify a tag!");
                    String message = (String) args.get("message");
                    if (message == null) throw CommandAPI.failWithString("You cannot send an empty message!");

                    CommandSender callee = sender.getCallee();
                    if (callee instanceof Player player) {
                        Radio.buildMessage(message, player.getName(), Radio.Type.TAG, tag);
                    } else if (callee instanceof ConsoleCommandSender) {
                        Radio.buildMessage(message, "Console", Radio.Type.TAG, tag);
                    } else {
                        throw CommandAPI.failWithString("This can only be run by players or the console!");
                    }
                });
    }
}
