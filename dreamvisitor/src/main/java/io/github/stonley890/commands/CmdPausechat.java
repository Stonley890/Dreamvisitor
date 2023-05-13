package io.github.stonley890.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.github.stonley890.Bot;
import io.github.stonley890.Main;
import net.dv8tion.jda.api.entities.TextChannel;

public class CmdPausechat implements CommandExecutor {

    Main plugin = Main.getPlugin();

    @Override
    @SuppressWarnings({ "null" })
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        TextChannel chatChannel = Bot.getJda().getTextChannelById(DiscCommandsManager.getChatChannel());
        // If chat is paused, unpause. If not, pause
        if (Main.chatPaused == true) {
            // Change settings
            Main.chatPaused = false;
            plugin.getConfig().set("chatPaused", Main.chatPaused);
            // Broadcast to server
            Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "Chat has been unpaused.");
            // Broadcast to chat channel
            if (chatChannel != null) {
                chatChannel.sendMessage("**Chat has been unpaused. Messages will now be sent to Minecraft**")
                        .queue();
            }
        } else {
            // Change settings
            Main.chatPaused = true;
            plugin.getConfig().set("chatPaused", Main.chatPaused);
            // Broadcast to server
            Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "Chat has been paused.");
            // Broadcast to chat channel
            if (chatChannel != null) {
                chatChannel.sendMessage("**Chat has been paused. Messages will not be sent to Minecraft**").queue();
            }
        }
        plugin.saveConfig();
        return true;
    }

}
