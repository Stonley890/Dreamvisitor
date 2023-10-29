package io.github.stonley890.dreamvisitor.commands;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import io.github.stonley890.dreamvisitor.Bot;
import org.jetbrains.annotations.NotNull;

public class CmdTagRadio implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (args.length < 2) {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Missing arguements! /tagradio <tag> <message>");
            return false;
        }

        if (sender instanceof Player) {
            Player playerSender = (Player) sender;

            buildMessage(args, playerSender.getName(), args[0]);

        } else if (sender instanceof ConsoleCommandSender) {
            buildMessage(args, "Console", args[0]);
        }
        return true;
    }
    
    void buildMessage(String[] args, String name, String receiverTag) {

        // Set color of name to red if from console
        ChatColor nameColor = ChatColor.YELLOW;
        if (name.equals("Console")) {
            nameColor = ChatColor.RED;
        }

        // Build message
        StringBuilder message = new StringBuilder().append(ChatColor.RED).append("[Radio] ").append(nameColor).append("<").append(name).append("> ").append(ChatColor.WHITE);
        for (int i = 0; i != args.length; i++)
        {
            message.append(args[i]).append(" ");
        }

        String finalMessage = message.toString();

        // Send message
        Bukkit.getLogger().info(ChatColor.stripColor(finalMessage));
        for (Player players : Bukkit.getServer().getOnlinePlayers())
        {
            if (players.getScoreboardTags().contains(receiverTag) || players.isOp()) {
                players.sendMessage(finalMessage);
            }
        }
        Bot.sendMessage(Bot.gameLogChannel, receiverTag + ": " + ChatColor.stripColor(finalMessage));
    }

}
