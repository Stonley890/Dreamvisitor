package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerJoin implements Listener {
    
    @EventHandler
    public void onPlayerJoinEvent(@NotNull PlayerJoinEvent event) {

        // Send join messages
        String chatMessage = "**" + Utils.escapeMarkdownFormatting(event.getPlayer().getName()) + " joined the game**";
        Bot.sendMessage(Bot.gameChatChannel, chatMessage);
        Bot.sendMessage(Bot.gameLogChannel, chatMessage);

    }
    
}
