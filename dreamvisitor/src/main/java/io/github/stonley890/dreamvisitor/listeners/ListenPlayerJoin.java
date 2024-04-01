package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import io.github.stonley890.dreamvisitor.functions.Sandbox;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerJoin implements Listener {
    
    @EventHandler
    public void onPlayerJoinEvent(@NotNull PlayerJoinEvent event) {

        // Send join messages
        String chatMessage = "**" + Bot.escapeMarkdownFormatting(event.getPlayer().getName()) + " joined the game**";
        Bot.sendMessage(Bot.gameChatChannel, chatMessage);
        Bot.sendMessage(Bot.gameLogChannel, chatMessage);

        PlayerMemory memory = PlayerUtility.getPlayerMemory(event.getPlayer().getUniqueId());

        if (memory.sandbox) {
            boolean sandboxerOnline = false;
            for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("dreamvisitor.sandbox")) {
                    sandboxerOnline = true;
                    onlinePlayer.sendMessage(Dreamvisitor.PREFIX + event.getPlayer().getName() + " is currently in sandbox mode.");
                }
            }
            if (!sandboxerOnline) {
                Sandbox.disableSandbox(event.getPlayer());
            }
        }

    }
    
}
