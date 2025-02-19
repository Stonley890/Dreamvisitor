package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.comms.DataSender;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import io.github.stonley890.dreamvisitor.functions.Sandbox;
import io.github.stonley890.dreamvisitor.functions.SystemMessage;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerJoin implements Listener {
    
    @EventHandler
    public void onPlayerJoinEvent(@NotNull PlayerJoinEvent event) {

        // Send join messages
        String chatMessage = "**" + Dreamvisitor.escapeMarkdownFormatting(ChatColor.stripColor(event.getPlayer().getName())) + " joined the game**";
        try {
            // TODO: Send join message
            // Bot.getGameChatChannel().sendMessage(chatMessage).queue();
        } catch (InsufficientPermissionException e) {
            Dreamvisitor.getPlugin().getLogger().warning(Dreamvisitor.TITLE + " does not have sufficient permissions to send messages in game chat channel: " + e.getMessage());
        }

        // report stats
        Bukkit.getScheduler().runTaskLaterAsynchronously(Dreamvisitor.getPlugin(), DataSender::sendPlayerCount, 1);

        PlayerMemory memory = PlayerUtility.getPlayerMemory(event.getPlayer().getUniqueId());

        if (memory.sandbox) {
            boolean sandboxerOnline = false;
            for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("dreamvisitor.sandbox")) {
                    sandboxerOnline = true;
                    onlinePlayer.sendMessage(SystemMessage.formatPrivate(event.getPlayer().getName() + " is currently in sandbox mode."));
                }
            }
            if (!sandboxerOnline) Sandbox.disableSandbox(event.getPlayer());
        }

    }
    
}
