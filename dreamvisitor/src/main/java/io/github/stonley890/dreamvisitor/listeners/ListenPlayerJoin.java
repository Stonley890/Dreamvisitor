package io.github.stonley890.dreamvisitor.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;

public class ListenPlayerJoin implements Listener {
    
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {

        // Send join messages
        String chatMessage = "**" + event.getPlayer().getName() + " joined the game**";
        Bot.sendMessage(DiscCommandsManager.gameChatChannel, chatMessage);
        Bot.sendMessage(DiscCommandsManager.gameLogChannel, chatMessage);
    }
    
}
