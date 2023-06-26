package io.github.stonley890.dreamvisitor.listeners;

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

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;
import net.md_5.bungee.api.ChatColor;

public class ListenPlayerCmdPreprocess implements Listener {

    Dreamvisitor plugin = Dreamvisitor.getPlugin();
    String[] msgAliases = {"/msg ","/tell ","/whisper ","/reply ","/t ","/w ","/r ", "/mail send "};

    @EventHandler
    @SuppressWarnings({ "unchecked"})
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {

        String cmd = event.getMessage();
        Player player = event.getPlayer();

        // '/me' passthrough
        if (cmd.startsWith("/me ") && !event.isCancelled()) {

            // IF chatPaused stop /me unless bypassing
            if (Dreamvisitor.chatPaused) {

                // Init bypassed players file
                File file = new File(plugin.getDataFolder().getAbsolutePath() + "/pauseBypass.yml");
                FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                List<String> bypassedPlayers;

                // Load file
                try {
                    fileConfig.load(file);
                } catch (IOException | InvalidConfigurationException e1) {
                    Bukkit.getLogger().warning("Could not load 'pauseBypass.yml' file! Restart to reinitialize.");
                    e1.printStackTrace();
                }

                // Remember bypassed players
                bypassedPlayers = (List<String>) fileConfig.get("players");

                // If list contains player, allow
                if (bypassedPlayers.contains(player.getUniqueId().toString()) || player.isOp()) {
                    // Remove '/me '
                    String action = cmd.replaceFirst("/me ", "");
                    String message = "**[" + ChatColor.stripColor(player.getDisplayName()) + " **(" + player.getName()
                            + ")**]** " + ChatColor.stripColor(action);
                    // Send message
                    Bot.sendMessage(DiscCommandsManager.gameChatChannel, message);
                    Bot.sendMessage(DiscCommandsManager.gameLogChannel, message);
                } // If list does not contain player, stop the command
                else {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Chat is currently paused.");
                }
            } // If chat is not paused, allow
            else {

                // Remove '/me '
                String action = cmd.replaceFirst("/me ", "");
                String message = "**[" + ChatColor.stripColor(player.getDisplayName()) + " **(" + player.getName()
                        + ")**]** " + ChatColor.stripColor(action);
                // Send message
                Bot.sendMessage(DiscCommandsManager.gameChatChannel, message);
                Bot.sendMessage(DiscCommandsManager.gameLogChannel, message);
            }
        } else {
            boolean isMsg = false;
            for (String string : msgAliases) {
                if (cmd.startsWith(string)) {
                    isMsg = true;
                    break;
                }
            }

            if (isMsg) {
                String message = "**" + player.getName() + "** sent command: `" + cmd + "`";
                Bot.sendMessage(DiscCommandsManager.gameLogChannel, message);
            }
        }
    }
}