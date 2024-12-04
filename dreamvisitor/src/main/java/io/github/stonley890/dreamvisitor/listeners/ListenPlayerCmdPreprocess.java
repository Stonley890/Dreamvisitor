package io.github.stonley890.dreamvisitor.listeners;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import io.github.stonley890.dreamvisitor.functions.Mail;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerCmdPreprocess implements Listener {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();
    final String[] msgAliases = {"/msg ","/tell ","/whisper ","/reply ","/t ","/w ","/r ", "/mail send "};
    final String[] tpAliases = {
            "/call","/ecall","/tpa","/etpa","/tpask","/etpask",
            "/tpaccept","/etpaccept","/tpyes","/etpyes",
            "/home", "/ehome", "/homes", "/ehomes", "/claimspawn"
    };

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
                    Dreamvisitor.getPlugin().getLogger().warning("Could not load 'pauseBypass.yml' file! " + e1.getMessage());
                }

                // Remember bypassed players
                bypassedPlayers = fileConfig.getStringList("players");

                // If list contains player, allow
                if (bypassedPlayers.contains(player.getUniqueId().toString()) || player.isOp()) {
                    // Remove command
                    int spaceIndex = cmd.indexOf(' ');
                    if (spaceIndex == -1) return;
                    String action = cmd.substring(spaceIndex + 1);
                    String message = "**[" + Dreamvisitor.escapeMarkdownFormatting(ChatColor.stripColor(player.getDisplayName())) + " **(" + player.getName()
                            + ")**]** " + ChatColor.stripColor(action);
                    // Send message
                    try {
                        // TODO: Send message
                        // Bot.getGameChatChannel().sendMessage(message).queue();
                    } catch (InsufficientPermissionException e) {
                        Dreamvisitor.getPlugin().getLogger().warning("Dreamvisitor does not have sufficient permissions to send messages in game chat channel: " + e.getMessage());
                    }
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
                String message = "**[" + Dreamvisitor.escapeMarkdownFormatting(ChatColor.stripColor(player.getDisplayName())) + " **(" + player.getName()
                        + ")**]** " + ChatColor.stripColor(action);
                // Send message
                try {
                    // TODO: Send message
                    // Bot.getGameChatChannel().sendMessage(message).queue();
                } catch (InsufficientPermissionException e) {
                    Dreamvisitor.getPlugin().getLogger().warning("Dreamvisitor does not have sufficient permissions to send messages in game chat channel: " + e.getMessage());
                }
            }
        } else {
            for (String string : msgAliases) {
                if (cmd.startsWith(string)) {
                    String message = "**" + Dreamvisitor.escapeMarkdownFormatting(player.getName()) + "** sent command: `" + cmd + "`";
                    // TODO: Not needed anymore
                    return;
                }
            }
            for (String tpAlias : tpAliases) {
                if (cmd.startsWith(tpAlias)) {
                    if (Mail.isPLayerDeliverer(player)) Mail.cancel(player);
                    for (Player sandboxer : Bukkit.getOnlinePlayers()) {
                        if (PlayerUtility.getPlayerMemory(sandboxer.getUniqueId()).sandbox && cmd.contains(sandboxer.getName())) {
                            player.sendMessage(Dreamvisitor.PREFIX + "That player is currently in Sandbox Mode. Teleportation is not allowed.");
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }
}