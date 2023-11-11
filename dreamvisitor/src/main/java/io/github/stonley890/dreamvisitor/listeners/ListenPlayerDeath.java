package io.github.stonley890.dreamvisitor.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import io.github.stonley890.dreamvisitor.Bot;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerDeath implements Listener {
    
    @EventHandler
    public void onPlayerDeathEvent(@NotNull PlayerDeathEvent event) {

        // Send death messages
        String chatMessage = "**" + event.getDeathMessage() + "**";
        Bot.sendMessage(Bot.gameChatChannel, chatMessage);
        Bot.sendMessage(Bot.gameLogChannel, chatMessage);
    }
    
}
