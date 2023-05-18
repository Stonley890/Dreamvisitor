package io.github.stonley890.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import io.github.stonley890.Bot;
import io.github.stonley890.commands.DiscCommandsManager;
import io.github.stonley890.data.PlayerUtility;

public class ListenPlayerQuit implements Listener {
    
    @EventHandler
    @SuppressWarnings({"null"})
    public void onPlayerQuitEvent(PlayerQuitEvent event) {

        // Send player quits to Discord
        String chatMessage = "**" + event.getPlayer().getName() + " left the game**";
        String channelId = DiscCommandsManager.getChatChannel();
        if (!channelId.equals("none")) {
            Bot.getJda().getTextChannelById(channelId).sendMessage(chatMessage).queue();
        }
        PlayerUtility.setPlayerMemory(event.getPlayer(), null);
    }

}
