package io.github.stonley890.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import io.github.stonley890.Bot;
import io.github.stonley890.commands.DiscCommandsManager;

public class ListenPlayerJoin implements Listener {
    
    @EventHandler
    @SuppressWarnings({"null"})
    public void onPlayerJoinEvent(PlayerJoinEvent event) {

        // Send join messages
        String chatMessage = "**" + event.getPlayer().getName() + " joined the game**";
        String channelId = DiscCommandsManager.getChatChannel();
        if (!channelId.equals("none")) {
            Bot.getJda().getTextChannelById(channelId).sendMessage(chatMessage).queue();
        }
    }
    
}
