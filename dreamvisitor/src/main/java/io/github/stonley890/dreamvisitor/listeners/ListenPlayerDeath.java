package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.functions.Mail;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import io.github.stonley890.dreamvisitor.Bot;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerDeath implements Listener {
    
    @EventHandler
    public void onPlayerDeathEvent(@NotNull PlayerDeathEvent event) {

        Player player = event.getEntity().getPlayer();
        if (player != null && Mail.isPLayerDeliverer(player)) Mail.cancel(player);

        if (event.getDeathMessage() == null) return;

        // Send death messages
        String chatMessage = "**" + Bot.escapeMarkdownFormatting(event.getDeathMessage()) + "**";
        try {
            Bot.getGameChatChannel().sendMessage(chatMessage).queue();
        } catch (InsufficientPermissionException e) {
            Bukkit.getLogger().warning("Dreamvisitor does not have sufficient permissions to send messages in game chat channel: " + e.getMessage());
        }
        Bot.sendLog(chatMessage);
    }
    
}
