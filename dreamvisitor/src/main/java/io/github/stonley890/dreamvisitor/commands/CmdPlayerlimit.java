package io.github.stonley890.dreamvisitor.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;

public class CmdPlayerlimit implements DVCommand {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // playerlimit [new-limit]

        if (args.length > 0) {
            try {
                // Change config
                int result = Integer.parseInt(args[0]);
                // Dreamvisitor.getPlugin().getServer().setMaxPlayers(result);

                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (player.isOp()) {
                        player.sendMessage(ChatColor.BLUE + "Player limit override set to " + args[0]);
                    }
                }

                Dreamvisitor.playerLimit = result;
                plugin.getConfig().set("playerlimit", result);
                plugin.saveConfig();

            } catch (NumberFormatException e) {
                sender.sendMessage(Dreamvisitor.PREFIX +
                        ChatColor.RED + "Incorrect arguments! /playerlimit <number of players (set -1 to disable)>");
                return false;
            }
        } else {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Player limit override is currently set to " + Dreamvisitor.playerLimit + ".");
        }
        return true;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "playerlimit";
    }

    @Override
    public LiteralCommandNode<?> getNode() {
        return LiteralArgumentBuilder.literal(getCommandName())
                .then(RequiredArgumentBuilder.argument("limit", IntegerArgumentType.integer(0, Integer.MAX_VALUE)))
                .build();
    }
}
