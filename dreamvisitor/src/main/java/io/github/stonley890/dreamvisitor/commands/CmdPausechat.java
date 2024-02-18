package io.github.stonley890.dreamvisitor.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Main;
import org.jetbrains.annotations.NotNull;

public class CmdPausechat implements CommandExecutor {

    Main plugin = Main.getPlugin();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        // If chat is paused, unpause. If not, pause
        if (Main.chatPaused) {

            // Change settings
            Main.chatPaused = false;
            plugin.getConfig().set("chatPaused", Main.chatPaused);

            // Broadcast to server
            Bukkit.getServer().broadcastMessage(ChatColor.BLUE + "Chat has been unpaused.");
            
            // Broadcast to chat channel
            Bot.sendMessage(Bot.gameChatChannel, "**Chat has been unpaused. Messages will now be sent to Minecraft**");

        } else {

            // Change settings
            Main.chatPaused = true;
            plugin.getConfig().set("chatPaused", Main.chatPaused);

            // Broadcast to server
            Bukkit.getServer().broadcastMessage(ChatColor.BLUE + "Chat has been paused.");

            // Broadcast to chat channel
            Bot.sendMessage(Bot.gameChatChannel, "**Chat has been paused. Messages will not be sent to Minecraft**");

        }
        plugin.saveConfig();
        return true;
    }

}
