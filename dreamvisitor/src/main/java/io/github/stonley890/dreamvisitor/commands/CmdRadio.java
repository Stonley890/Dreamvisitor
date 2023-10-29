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

public class CmdRadio implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "You must attach a message! /aradio <message>");
            return false;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            buildMessage(args, player.getName());
            return true;
        } else if (sender instanceof ConsoleCommandSender) {

            buildMessage(args, "Console");
            return true;
        } else {
            return false;
        }
        
    }

    void buildMessage(String[] args, String name) {

        // Set color of name to red if from console
        ChatColor nameColor = ChatColor.YELLOW;
        if (name.equals("Console")) {
            nameColor = ChatColor.RED;
        }

        // Build message
        StringBuilder message = new StringBuilder().append(ChatColor.DARK_AQUA).append("[Staff Radio] ").append(nameColor).append("<").append(name).append("> ").append(ChatColor.WHITE);
        for (int i = 0; i != args.length; i++)
        {
            message.append(args[i]).append(" ");
        }

        String finalMessage = message.toString();

        // Send message
        Bukkit.getLogger().info(ChatColor.stripColor(finalMessage));
        for (Player operator : Bukkit.getServer().getOnlinePlayers())
        {
            if (operator.isOp() || operator.hasPermission("dreamvisitor.radio"))
            {
                operator.sendMessage(finalMessage);
            }
        }
        Bot.sendMessage(Bot.gameLogChannel, ChatColor.stripColor(finalMessage));
    }
    
}
