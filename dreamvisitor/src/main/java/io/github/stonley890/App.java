package io.github.stonley890;

import javax.security.auth.login.LoginException;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.stonley890.commands.CommandsManager;

public class App extends JavaPlugin implements Listener {

    String token = "BOT_TOKEN";

    @Override
    public void onEnable() {

        getLogger().info("Dreamvisitor: A plugin created by Bog for WoF:TNW to automate tasks and bring back features.");
        getServer().getPluginManager().registerEvents(this, this);
        try {
            Bot dreamvisitor = new Bot();
        } catch (LoginException e) {
            System.out.println("ERROR: Bot login failed!");
            e.printStackTrace();
        }

    }

    @EventHandler
    public void onPlayerChatEvent (PlayerChatEvent event) {
        String chatMessage = "**" + event.getPlayer().getName() + "**: " + event.getMessage();
        String channelId = CommandsManager.getChatChannel();
        if (channelId != "none") {
            io.github.stonley890.Bot.getJDA().getTextChannelById(channelId).sendMessage(chatMessage).queue();
        }
        
        
    }

    @EventHandler
    public void onPlayerJoinEvent (PlayerJoinEvent event) {
        String chatMessage = "**" + event.getPlayer().getName() + " joined the game**";
        String channelId = CommandsManager.getChatChannel();
        if (channelId != "none") {
            io.github.stonley890.Bot.getJDA().getTextChannelById(channelId).sendMessage(chatMessage).queue();
        }
    }

    @EventHandler
    public void onPlayerQuitEvent (PlayerQuitEvent event) {
        String chatMessage = "**" + event.getPlayer().getName() + " left the game**";
        String channelId = CommandsManager.getChatChannel();
        if (channelId != "none") {
            io.github.stonley890.Bot.getJDA().getTextChannelById(channelId).sendMessage(chatMessage).queue();
        }
    }

    @EventHandler
    public void onPlayerDeathEvent (PlayerDeathEvent event) {
        String chatMessage = "**" + event.getDeathMessage() + "**";
        String channelId = CommandsManager.getChatChannel();
        if (channelId != "none") {
            io.github.stonley890.Bot.getJDA().getTextChannelById(channelId).sendMessage(chatMessage).queue();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Dreamvisitor disable process.");

    }

}