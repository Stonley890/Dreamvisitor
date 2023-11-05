package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import io.github.stonley890.dreamvisitor.Bot;

import java.util.HexFormat;

public class ListenPlayerJoin implements Listener {
    
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {

        PlayerMemory memory = PlayerUtility.getPlayerMemory(event.getPlayer().getUniqueId());

        // Prompt resource pack if hash has changed
        if (memory.resourcePackHash == null || !memory.resourcePackHash.equals(Dreamvisitor.resourcePackHash)) {
            event.getPlayer().setResourcePack(Bukkit.getResourcePack(), HexFormat.of().parseHex(Dreamvisitor.resourcePackHash));
            memory.resourcePackHash = Dreamvisitor.resourcePackHash;
            PlayerUtility.setPlayerMemory(event.getPlayer().getUniqueId(), memory);
        }

        // Send join messages
        String chatMessage = "**" + event.getPlayer().getName() + " joined the game**";
        Bot.sendMessage(Bot.gameChatChannel, chatMessage);
        Bot.sendMessage(Bot.gameLogChannel, chatMessage);

    }
    
}
