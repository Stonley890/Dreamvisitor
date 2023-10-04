package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import io.github.stonley890.dreamvisitor.Bot;

import java.util.HexFormat;

public class ListenPlayerJoin implements Listener {
    
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {

        event.getPlayer().setResourcePack(Bukkit.getResourcePack(), HexFormat.of().parseHex(Dreamvisitor.resourcePackHash));

        // Send join messages
        String chatMessage = "**" + event.getPlayer().getName() + " joined the game**";
        Bot.sendMessage(Bot.gameChatChannel, chatMessage);
        Bot.sendMessage(Bot.gameLogChannel, chatMessage);

    }
    
}
