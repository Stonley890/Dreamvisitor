package io.github.stonley890.dreamvisitor.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;

public class ListenPlayerDeath implements Listener {
    
    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {

        // Send death messages
        String chatMessage = "**" + event.getDeathMessage() + "**";
        Bot.sendMessage(DiscCommandsManager.gameChatChannel, chatMessage);
        Bot.sendMessage(DiscCommandsManager.gameLogChannel, chatMessage);
    }
    
}
