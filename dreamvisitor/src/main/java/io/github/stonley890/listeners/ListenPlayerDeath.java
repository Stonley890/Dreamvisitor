package io.github.stonley890.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import io.github.stonley890.Bot;
import io.github.stonley890.commands.DiscCommandsManager;

public class ListenPlayerDeath implements Listener {
    
    @EventHandler
    @SuppressWarnings({"null"})
    public void onPlayerDeathEvent(PlayerDeathEvent event) {

        // Send death messages
        String chatMessage = "**" + event.getDeathMessage() + "**";
        String channelId = DiscCommandsManager.getChatChannel();
        if (!channelId.equals("none")) {
            Bot.getJda().getTextChannelById(channelId).sendMessage(chatMessage).queue();
        }
    }
    
}
