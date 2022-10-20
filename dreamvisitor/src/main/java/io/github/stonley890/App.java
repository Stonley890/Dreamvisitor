package io.github.stonley890;

import javax.security.auth.login.LoginException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.stonley890.commands.CommandsManager;
import net.dv8tion.jda.api.entities.Guild;

public class App extends JavaPlugin implements Listener {

    private static App plugin;
    private static boolean chatPaused;

    @Override
    public void onEnable() {

        getLogger()
                .info("Dreamvisitor: A plugin created by Bog for WoF:TNW to add various features.");
        getServer().getPluginManager().registerEvents(this, this);
        // Start Discord bot
        try {
            new Bot();
        } catch (LoginException e) {
            Bukkit.getLogger().warning("ERROR: Bot login failed!");
            e.printStackTrace();
        }
        try {
            Bot.getJDA().awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Bot.getJDA().getGuilds()
                .forEach((Guild guild) -> guild.getSystemChannel().sendMessage("Server has been started.").queue());

        plugin = this;

    }

    public static App getPlugin() {
        return plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("pausechat")) {
            if (chatPaused == true) {
                chatPaused = false;
                Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "Chat has been unpaused.");
            } else {
                chatPaused = true;
                Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "Chat has been paused.");
            }

        }
        return true;
    }

    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        // Send chat messages to Discord
        if (chatPaused != true && event.getPlayer().isOp() == false || event.getPlayer().isOp() == true) {
            String chatMessage = "**" + event.getPlayer().getName() + "**: " + event.getMessage();
            String channelId = CommandsManager.getChatChannel();
            if (channelId != "none") {
                io.github.stonley890.Bot.getJDA().getTextChannelById(channelId).sendMessage(chatMessage).queue();
            }
        } else {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Chat is currently paused.");
        }

    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        // Send player joins to Discord
        String chatMessage = "**" + event.getPlayer().getName() + " joined the game**";
        String channelId = CommandsManager.getChatChannel();
        if (channelId != "none") {
            io.github.stonley890.Bot.getJDA().getTextChannelById(channelId).sendMessage(chatMessage).queue();
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        // Send player quits to Discord
        String chatMessage = "**" + event.getPlayer().getName() + " left the game**";
        String channelId = CommandsManager.getChatChannel();
        if (channelId != "none") {
            io.github.stonley890.Bot.getJDA().getTextChannelById(channelId).sendMessage(chatMessage).queue();
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        // Send death messages
        String chatMessage = "**" + event.getDeathMessage() + "**";
        String channelId = CommandsManager.getChatChannel();
        if (channelId != "none") {
            io.github.stonley890.Bot.getJDA().getTextChannelById(channelId).sendMessage(chatMessage).queue();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Dreamvisitor disable process.");
        Bot.getJDA().getGuilds()
                .forEach((Guild guild) -> guild.getSystemChannel().sendMessage("Server has been shutdown.").queue());
        // Shut down bot
        Bot.getJDA().shutdown();

    }

}