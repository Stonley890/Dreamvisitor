package io.github.stonley890;

import javax.security.auth.login.LoginException;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

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
        io.github.stonley890.Bot.getJDA().getTextChannelById("974114329886605433").sendMessage(chatMessage).queue();
        
    }

    @Override
    public void onDisable() {
        getLogger().info("Dreamvisitor disable process.");

    }

}