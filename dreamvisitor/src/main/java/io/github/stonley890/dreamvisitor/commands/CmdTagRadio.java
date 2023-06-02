package io.github.stonley890.dreamvisitor.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;

public class CmdTagRadio implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (sender instanceof Player) {
                Player playerSender = (Player) sender;

                buildMessage(args, playerSender.getName(), args[0]);

            } else if (sender instanceof ConsoleCommandSender && (args.length > 0)) {
                    buildMessage(args, "Console", args[0]);   
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Missing arguments! /tagradio <tag> <message>");
            return false;
        }
        return true;
    }
    
    void buildMessage(String[] args, String name, String recieverTag) {

        // Set color of name to red if from console
        ChatColor nameColor = ChatColor.YELLOW;
        if (name.equals("Console")) {
            nameColor = ChatColor.RED;
        }

        // Build message
        StringBuilder message = new StringBuilder().append(ChatColor.RED + "[Radio] " + nameColor + "<" + name + "> " + ChatColor.WHITE);
        for (int i = 0; i != args.length; i++)
        {
            message.append(args[i] + " ");
        }

        String finalMessage = message.toString();

        // Send message
        Bukkit.getLogger().info(finalMessage);
        for (Player players : Bukkit.getServer().getOnlinePlayers())
        {
            if (players.getScoreboardTags().contains(recieverTag) || players.isOp()) {
                players.sendMessage(finalMessage);
            }
        }
        Bot.sendMessage(DiscCommandsManager.gameLogChannel, recieverTag + ": " + ChatColor.stripColor(finalMessage));
    }

}
