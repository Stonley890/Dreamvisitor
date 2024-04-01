package io.github.stonley890.dreamvisitor.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;

public class CmdPausechat implements CommandExecutor {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        // pausechat

        // If chat is paused, unpause. If not, pause
        if (Dreamvisitor.chatPaused) {

            // Change settings
            Dreamvisitor.chatPaused = false;
            plugin.getConfig().set("chatPaused", Dreamvisitor.chatPaused);

            // Broadcast to server
            Bukkit.getServer().broadcastMessage(ChatColor.BLUE + "Chat has been unpaused.");
            
            // Broadcast to chat channel
            Bot.sendMessage(Bot.getGameChatChannel(), "**Chat has been unpaused. Messages will now be sent to Minecraft**");

        } else {

            // Change settings
            Dreamvisitor.chatPaused = true;
            plugin.getConfig().set("chatPaused", Dreamvisitor.chatPaused);

            // Broadcast to server
            Bukkit.getServer().broadcastMessage(ChatColor.BLUE + "Chat has been paused.");

            // Broadcast to chat channel
            Bot.sendMessage(Bot.getGameChatChannel(), "**Chat has been paused. Messages will not be sent to Minecraft**");

        }
        plugin.saveConfig();
        return true;
    }

}
