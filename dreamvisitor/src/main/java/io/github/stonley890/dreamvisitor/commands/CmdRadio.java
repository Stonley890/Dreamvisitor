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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {

        if (args.length == 0) {
            if (command.getName().equals("tagradio")) sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "You must attach a message! /" + label + " <message>");
            else sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "You must attach a message! /" + label + "<tag> <message>");
            return false;
        }

        if (sender instanceof Player player) {
            buildMessage(args, player.getName(), command);
            return true;
        } else if (sender instanceof ConsoleCommandSender) {
            buildMessage(args, "Console", command);
            return true;
        } else {
            return false;
        }
        
    }

    static void buildMessage(String[] args, @NotNull String name, @NotNull Command command) {

        // Set color of name to red if from console
        ChatColor nameColor = ChatColor.YELLOW;
        if (name.equals("Console")) {
            nameColor = ChatColor.RED;
        }

        // Build message
        String radioType = "[Staff Radio]";
        if (command.getName().equals("aradio")) radioType = "[Admin Radio]";
        else if (command.getName().equals("tagradio")) radioType = "[Radio]";

        StringBuilder message = new StringBuilder().append(ChatColor.DARK_AQUA).append(radioType).append(nameColor).append(" <").append(name).append("> ").append(ChatColor.WHITE);
        int i;
        if (command.getName().equals("tagradio")) i = 1;
        else i = 0;
        for (; i != args.length; i++) message.append(args[i]).append(" ");

        String finalMessage = message.toString();

        // Send message
        Bukkit.getLogger().info(ChatColor.stripColor(finalMessage));
        for (Player operator : Bukkit.getServer().getOnlinePlayers())
        {
            if (command.getName().equals("radio")) if (operator.isOp() || operator.hasPermission("dreamvisitor.radio")) operator.sendMessage(finalMessage);
            else if (command.getName().equals("aradio")) if (operator.isOp()) operator.sendMessage(finalMessage);
            else if (command.getName().equals("tagradio")) if (operator.getScoreboardTags().contains(args[0])) operator.sendMessage(finalMessage);

        }
        Bot.sendMessage(Bot.gameLogChannel, ChatColor.stripColor(finalMessage));
    }
    
}
