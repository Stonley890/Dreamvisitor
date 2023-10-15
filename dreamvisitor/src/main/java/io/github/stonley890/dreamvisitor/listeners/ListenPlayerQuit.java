package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;

import java.io.IOException;

public class ListenPlayerQuit implements Listener {
    
    @EventHandler
    @SuppressWarnings({"null"})
    public void onPlayerQuitEvent(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        // Send player quits to Discord
        String chatMessage = "**" + player.getName() + " left the game**";
        Bot.sendMessage(Bot.gameChatChannel, chatMessage);
        Bot.sendMessage(Bot.gameLogChannel, chatMessage);
        try {
            PlayerUtility.savePlayerMemory(player.getUniqueId());
            PlayerUtility.clearPlayerMemory(player.getUniqueId());
        } catch (IOException e) {
            Bukkit.getLogger().severe("Unable to save player memory! Does the server have write access? Player memory will remain in memory.");
            if (Dreamvisitor.debug) e.printStackTrace();
        }

    }

}
