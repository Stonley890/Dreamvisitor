package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.comms.DataSender;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.functions.Sandbox;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ListenPlayerQuit implements Listener {
    
    @EventHandler
    @SuppressWarnings({"null"})
    public void onPlayerQuitEvent(@NotNull PlayerQuitEvent event) {

        Player player = event.getPlayer();

        // Send player quits to Discord
        String chatMessage = "**" + Dreamvisitor.escapeMarkdownFormatting(ChatColor.stripColor(player.getName())) + " left the game**";
        // TODO: Send quit message
        // Bot.getGameChatChannel().sendMessage(chatMessage).queue();

        // report stats
        Bukkit.getScheduler().runTaskLaterAsynchronously(Dreamvisitor.getPlugin(), DataSender::sendPlayerCount, 1);

        PlayerMemory memory = PlayerUtility.getPlayerMemory(event.getPlayer().getUniqueId());

        if (memory.sandbox) {
            for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("dreamvisitor.sandbox")) {
                    onlinePlayer.sendMessage(Dreamvisitor.PREFIX + event.getPlayer().getName() + " left while in sandbox mode.");
                }
            }
        }

        try {
            PlayerUtility.savePlayerMemory(player.getUniqueId());
            PlayerUtility.clearPlayerMemory(player.getUniqueId());
        } catch (IOException e) {
            Bukkit.getLogger().severe("Unable to save player memory! Does the server have write access? Player memory will remain in memory. " + e.getMessage());
        }

        Dreamvisitor.debug("Checking sandbox.");

        Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> {

            Dreamvisitor.debug("Task start.");

            // Check for sandboxed players
            boolean moderatorOnline = false;
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                Dreamvisitor.debug("Is " + onlinePlayer.getName() + " moderator?");
                if (onlinePlayer.hasPermission("dreamvisitor.sandbox")) {
                    Dreamvisitor.debug("Yes! All good.");
                    moderatorOnline = true;
                    break;
                }
            }
            if (!moderatorOnline) {
                Dreamvisitor.debug("No mods online! Gotta disable sandboxed.");
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    Dreamvisitor.debug("Is " + onlinePlayer + " sandboxed?");
                    if (PlayerUtility.getPlayerMemory(onlinePlayer.getUniqueId()).sandbox) {
                        Dreamvisitor.debug("Yes. Disabling.");
                        Sandbox.disableSandbox(onlinePlayer);
                        onlinePlayer.sendMessage("You are no longer in Sandbox Mode because there are no sandbox managers available.");
                    }
                }
            }
        });



    }

}
