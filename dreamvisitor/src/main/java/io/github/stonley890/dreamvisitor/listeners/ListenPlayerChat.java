package io.github.stonley890.dreamvisitor.listeners;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;

public class ListenPlayerChat implements Listener {

    Dreamvisitor plugin = Dreamvisitor.getPlugin();
    
    @EventHandler
    @SuppressWarnings({"unchecked","null"})
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        /**
        Send chat messages to Discord
        IF chat is not paused AND the player is not an operator OR the player is an
        operator, send message
        **/

        String chatMessage = "**" + event.getPlayer().getName() + "**: " + event.getMessage();

        if (Dreamvisitor.chatPaused && !event.isCancelled()) {

            // Load pauseBypass file
            File file = new File(plugin.getDataFolder().getAbsolutePath() + "/pauseBypass.yml");
            FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
            List<String> bypassedPlayers;

			// Load file
            try {
                fileConfig.load(file);
            } catch (IOException | InvalidConfigurationException e1) {
                e1.printStackTrace();
            }

			// Fetch bypassed players
            bypassedPlayers = ((List<String>)fileConfig.get("players"));

            // If player is on soft whitelist or is op, allow.
            if (bypassedPlayers.contains(event.getPlayer().getUniqueId().toString())
                    || event.getPlayer().hasPermission("dreamvisitor.nopause")) {

                Bot.sendMessage(DiscCommandsManager.gameChatChannel, chatMessage.replaceAll("_", "\\_"));
                Bot.sendMessage(DiscCommandsManager.gameLogChannel, chatMessage.replaceAll("_", "\\_"));
                
            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Chat is currently paused.");

                Bot.sendMessage(DiscCommandsManager.gameLogChannel, "Blocked: " + chatMessage.replaceAll("_", "\\_"));

            }  
        } else if (!event.isCancelled()) {

            Bot.sendMessage(DiscCommandsManager.gameChatChannel, chatMessage.replaceAll("_", "\\_"));
            Bot.sendMessage(DiscCommandsManager.gameLogChannel, chatMessage.replaceAll("_", "\\_"));

        }
    }

}
