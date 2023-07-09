package io.github.stonley890.dreamvisitor.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;

public class CmdPlayerlimit implements CommandExecutor {

    Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length > 0) {
            try {
                // Change config
                int result = Integer.parseInt(args[0]);
                Dreamvisitor.playerlimit = result;
                plugin.getConfig().set("playerlimit", result);
                plugin.saveConfig();
                if (args[0].equals("-1")) {
                    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                        if (player.isOp()) {
                            player.sendMessage(ChatColor.BLUE + "Player limit override disabled");
                        }
                    }
                } else if (result > -1) {
                    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                        if (player.isOp()) {
                            player.sendMessage(ChatColor.BLUE + "Player limit override set to " + args[0]);
                        }
                    }
                } else {
                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED
                            + "Incorrect arguments! /playerlimit <number of players (set -1 to disable)>");
                    return false;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(Dreamvisitor.PREFIX +
                        ChatColor.RED + "Incorrect arguments! /playerlimit <number of players (set -1 to disable)>");
                return false;
            }
        } else {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Player limit override is currently set to " + Dreamvisitor.playerlimit + ".");
        }
        return true;
    }
    
}
