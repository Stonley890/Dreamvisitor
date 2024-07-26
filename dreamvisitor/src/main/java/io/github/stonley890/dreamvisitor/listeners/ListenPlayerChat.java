package io.github.stonley890.dreamvisitor.listeners;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerChat implements Listener {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();
    
    @EventHandler
    @SuppressWarnings({"null"})
    public void onPlayerChatEvent(@NotNull AsyncPlayerChatEvent event) {

        if (event.getPlayer().hasPermission("dreamvisitor.set.autoradio")) {
            PlayerMemory memory = PlayerUtility.getPlayerMemory(event.getPlayer().getUniqueId());

            if (memory.autoRadio) {
                event.setCancelled(true);
                Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> Bukkit.dispatchCommand(event.getPlayer(), "radio " + event.getMessage()));
                return;
            }
        }
        
        /*
        Send chat messages to Discord
        IF chat is not paused AND the player is not an operator OR the player is an
        operator, send message
        */

        String chatMessage = "**" + Bot.escapeMarkdownFormatting(event.getPlayer().getName()) + "**: " + event.getMessage();

        if (!Dreamvisitor.chatPaused || event.isCancelled()) {
            if (event.isCancelled()) return;

            try {
                Bot.getGameChatChannel().sendMessage(chatMessage).queue();
            } catch (InsufficientPermissionException e) {
                Bukkit.getLogger().warning("Dreamvisitor does not have sufficient permissions to send messages in game chat channel: " + e.getMessage());
            }
            Bot.sendLog(chatMessage);

        } else {

            // Load pauseBypass file
            File file = new File(plugin.getDataFolder().getAbsolutePath() + "/pauseBypass.yml");
            FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
            List<String> bypassedPlayers;

			// Load file
            try {
                fileConfig.load(file);
            } catch (IOException | InvalidConfigurationException ignored) {
            }

			// Fetch bypassed players
            bypassedPlayers = (fileConfig.getStringList("players"));

            // If player is on soft whitelist or is op, allow.
            if (bypassedPlayers.contains(event.getPlayer().getUniqueId().toString())
                    || event.getPlayer().hasPermission("dreamvisitor.nopause")) {

                try {
                    Bot.getGameChatChannel().sendMessage(chatMessage).queue();
                } catch (InsufficientPermissionException e) {
                    Bukkit.getLogger().warning("Dreamvisitor does not have sufficient permissions to send messages in game chat channel: " + e.getMessage());
                }
                Bot.sendLog(chatMessage);

            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Chat is currently paused.");

                Bot.sendLog("Blocked: " + chatMessage);

            }
        }
    }

}
