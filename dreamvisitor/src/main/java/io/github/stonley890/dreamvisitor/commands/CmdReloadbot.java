package io.github.stonley890.dreamvisitor.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

public class CmdReloadbot implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        
        sender.sendMessage(Dreamvisitor.prefix + ChatColor.BLUE + "Shutting down the bot instance...");

        if (!Dreamvisitor.botFailed) {
            Bot.getJda().shutdown();
        }

        sender.sendMessage(Dreamvisitor.prefix + ChatColor.BLUE + "Starting a new bot instance...");
        Dreamvisitor.botFailed = false;
        Bukkit.getScheduler().runTask(Dreamvisitor.plugin, new Runnable() {

            @Override
            public void run() {
                Bot.startBot();
            }
            
        });
        

        if (Dreamvisitor.botFailed) {
            sender.sendMessage(Dreamvisitor.prefix + ChatColor.RED + "The bot was unable to start due to an invalid login token.");
        } else {
            DiscCommandsManager.initChannelsRoles();
            sender.sendMessage(Dreamvisitor.prefix + ChatColor.BLUE + "Dreamvisitor bot has been restarted.");
        }

        return true;
    }

}
