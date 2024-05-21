package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.IntegerArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;

public class CmdPlayerlimit implements DVCommand {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("playerlimit")
                .withPermission(CommandPermission.fromString("dreamvisitor.playerlimit"))
                .withHelp("Set the player limit.", "Override the server player limit")
                .withArguments(new IntegerArgument("newLimit", -1))
                .executes((sender, args) -> {

                    Object newLimitArg = args.get("newLimit");
                    if (newLimitArg == null) {
                        sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Player limit override is currently set to " + Dreamvisitor.playerLimit + ".");
                    } else {
                        try {
                            // Change config
                            int result = (int) newLimitArg;
                            // Dreamvisitor.getPlugin().getServer().setMaxPlayers(result);

                            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                                if (player.isOp()) {
                                    player.sendMessage(ChatColor.BLUE + "Player limit override set to " + result);
                                }
                            }

                            Dreamvisitor.playerLimit = result;
                            plugin.getConfig().set("playerlimit", result);
                            plugin.saveConfig();

                        } catch (NumberFormatException e) {
                            sender.sendMessage(Dreamvisitor.PREFIX +
                                    ChatColor.RED + "Incorrect arguments! /playerlimit <number of players (set -1 to disable)>");
                        }
                    }

                });
    }
}
