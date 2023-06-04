package io.github.stonley890.dreamvisitor.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;
import net.md_5.bungee.api.ChatColor;

public class CmdReloadbot implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        sender.sendMessage("Shutting down the bot instance...");

        if (!Dreamvisitor.botFailed) {
            Bot.getJda().shutdownNow();
        }

        sender.sendMessage("Starting a new bot instance...");
        Dreamvisitor.botFailed = false;
        Bukkit.getScheduler().runTask(Dreamvisitor.plugin, new Runnable() {

            @Override
            public void run() {
                Bot.startBot();
            }
            
        });
        

        if (Dreamvisitor.botFailed) {
            sender.sendMessage(ChatColor.RED + "The bot was unable to start due to an invalid login token.");
        } else {
            DiscCommandsManager.initChannelsRoles();
            sender.sendMessage("Dreamvisitor bot has been restarted.");
        }

        return true;
    }

}
