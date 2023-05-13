package io.github.stonley890.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.github.stonley890.Bot;

public class CmdReloadbot implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("Shutting down the bot instance...");
        Bot.getJda().shutdown();
        sender.sendMessage("Starting a new bot instance...");
        Bot.startBot();
        sender.sendMessage("Dreamvisitor bot has been restarted.");
        return true;
    }
    
}
