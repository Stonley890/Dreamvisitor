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
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerCmdPreprocess implements Listener {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();
    final String[] msgAliases = {"/msg ","/tell ","/whisper ","/reply ","/t ","/w ","/r ", "/mail send "};

    @EventHandler
    public void onPlayerCommandPreprocess(@NotNull PlayerCommandPreprocessEvent event) {

        String cmd = event.getMessage();
        Player player = event.getPlayer();

        // '/me' and '/rp' pass through
        if ((cmd.startsWith("/me " ) || cmd.startsWith("/rp" )) && !event.isCancelled()) {

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
                }

                // Remember bypassed players
                bypassedPlayers = fileConfig.getStringList("players");

                // If list contains player, allow
                if (bypassedPlayers.contains(player.getUniqueId().toString()) || player.isOp()) {
                    // Remove command
                    int spaceIndex = cmd.indexOf(' ');
                    if (spaceIndex == -1) return;
                    String action = cmd.substring(spaceIndex + 1);
                    String message = "**[" + Bot.escapeMarkdownFormatting(ChatColor.stripColor(player.getDisplayName())) + " **(" + player.getName()
                            + ")**]** " + ChatColor.stripColor(action);
                    // Send message
                    Bot.sendMessage(Bot.gameChatChannel, message);
                    Bot.sendMessage(Bot.gameLogChannel, message);
                } // If list does not contain player, stop the command
                else {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Chat is currently paused.");
                }
            } // If chat is not paused, allow
            else {

                // Remove command
                int spaceIndex = cmd.indexOf(' ');
                if (spaceIndex == -1) return;
                String action = cmd.substring(spaceIndex + 1);
                String message = "**[" + Bot.escapeMarkdownFormatting(ChatColor.stripColor(player.getDisplayName())) + " **(" + player.getName()
                        + ")**]** " + ChatColor.stripColor(action);
                // Send message
                Bot.sendMessage(Bot.gameChatChannel, message);
                Bot.sendMessage(Bot.gameLogChannel, message);
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
                String message = "**" + Bot.escapeMarkdownFormatting(player.getName()) + "** sent command: `" + cmd + "`";
                Bot.sendMessage(Bot.gameLogChannel, message);
            }
        }
    }
}