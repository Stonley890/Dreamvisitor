package io.github.stonley890.dreamvisitor.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;

public class ListenPlayerQuit implements Listener {
    
    @EventHandler
    @SuppressWarnings({"null"})
    public void onPlayerQuitEvent(PlayerQuitEvent event) {

        // Send player quits to Discord
        String chatMessage = "**" + event.getPlayer().getName() + " left the game**";
        Bot.sendMessage(DiscCommandsManager.gameChatChannel, chatMessage);
        Bot.sendMessage(DiscCommandsManager.gameLogChannel, chatMessage);
        PlayerUtility.setPlayerMemory(event.getPlayer(), null);
    }

}
