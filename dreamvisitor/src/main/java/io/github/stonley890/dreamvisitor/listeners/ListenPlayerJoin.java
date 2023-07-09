package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.google.UserTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class ListenPlayerJoin implements Listener {
    
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {

        // Send join messages
        String chatMessage = "**" + event.getPlayer().getName() + " joined the game**";
        Bot.sendMessage(DiscCommandsManager.gameChatChannel, chatMessage);
        Bot.sendMessage(DiscCommandsManager.gameLogChannel, chatMessage);

        // Update username
        try {
            UserTracker.updateUsername(event.getPlayer().getUniqueId().toString(), event.getPlayer().getName());
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
