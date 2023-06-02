package io.github.stonley890.dreamvisitor.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;

public class CmdPausechat implements CommandExecutor {

    Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @Override
    @SuppressWarnings({ "null" })
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // If chat is paused, unpause. If not, pause
        if (Dreamvisitor.chatPaused) {

            // Change settings
            Dreamvisitor.chatPaused = false;
            plugin.getConfig().set("chatPaused", Dreamvisitor.chatPaused);

            // Broadcast to server
            Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "Chat has been unpaused.");
            
            // Broadcast to chat channel
            Bot.sendMessage(DiscCommandsManager.gameChatChannel, "**Chat has been unpaused. Messages will now be sent to Minecraft**");

        } else {

            // Change settings
            Dreamvisitor.chatPaused = true;
            plugin.getConfig().set("chatPaused", Dreamvisitor.chatPaused);

            // Broadcast to server
            Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "Chat has been paused.");

            // Broadcast to chat channel
            Bot.sendMessage(DiscCommandsManager.gameChatChannel, "**Chat has been paused. Messages will not be sent to Minecraft**");

        }
        plugin.saveConfig();
        return true;
    }

}
