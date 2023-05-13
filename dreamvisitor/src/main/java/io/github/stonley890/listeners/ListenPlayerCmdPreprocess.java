package io.github.stonley890.listeners;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import io.github.stonley890.Bot;
import io.github.stonley890.Main;
import io.github.stonley890.commands.DiscCommandsManager;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.ChatColor;

public class ListenPlayerCmdPreprocess implements Listener {
    
    Main plugin = Main.getPlugin();

    @EventHandler
    @SuppressWarnings({"unchecked", "null"})
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    // Occurs when player tries to execute command.
    {
        String cmd = event.getMessage();
        Player player = event.getPlayer();

        if (event.getMessage().startsWith("/me ") && !event.isCancelled())
        // '/me' passthrough
        {
            if (Main.chatPaused)
            // IF chatPaused stop /me unless bypassing
            {
                // Init bypassed players file
                File file = new File(plugin.getDataFolder().getAbsolutePath() + "/pauseBypass.yml");
                FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                List<String> bypassedPlayers;

                // Load file
                try
                {
                    fileConfig.load(file);
                } catch (IOException | InvalidConfigurationException e1)
                {
                    Bukkit.getLogger().warning("Could not load 'pauseBypass.yml' file! Restart to reinitialize.");
                    e1.printStackTrace();
                }

                // Remember bypassed players
                bypassedPlayers = (List<String>) fileConfig.get("players");

                // If list contains player, allow
                if (bypassedPlayers.contains(player.getUniqueId().toString()) || player.isOp())
                {
                    // Fetch chat channel
                    TextChannel chatChannel = Bot.getJda().getTextChannelById(DiscCommandsManager.getChatChannel());
                    // Remove '/me '
                    String action = cmd.replaceFirst("/me ", "");
                    // Send message
                    chatChannel.sendMessage("**[" + ChatColor.stripColor(player.getDisplayName()) + " **(" + player.getName() + ")**]** " + ChatColor.stripColor(action)).queue();
                } else
                // If list does not contain player, stop the command
                {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Chat is currently paused.");
                }
            } else
            // If chat is not paused, allow
            {
                // Fetch chat channel
                TextChannel chatChannel = Bot.getJda().getTextChannelById(DiscCommandsManager.getChatChannel());
                // Remove '/me '
                String action = cmd.replaceFirst("/me ", "");
                // Send message
                chatChannel.sendMessage("**[" + ChatColor.stripColor(player.getDisplayName()) + " **(" + player.getName() + ")**]** " + ChatColor.stripColor(action)).queue();
            }
        }
    }
    
}
