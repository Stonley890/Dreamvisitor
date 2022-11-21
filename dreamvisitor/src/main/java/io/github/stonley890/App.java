package io.github.stonley890;

import java.io.File;
import java.io.IOException;

import javax.security.auth.login.LoginException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import io.github.stonley890.commands.CommandsManager;
import io.github.stonley890.data.PlayerMemory;
import io.github.stonley890.data.PlayerUtility;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class App extends JavaPlugin implements Listener {

    private static App plugin;
    private static boolean chatPaused;
    private static boolean botFailed = false;

    @Override
    public void onEnable() {

        plugin = this;

        // Start message & register events
        getLogger()
                .info("Dreamvisitor: A plugin created by Bog for WoF:TNW to add various features.");
        getServer().getPluginManager().registerEvents(this, this);

        // Create config if needed
        getDataFolder().mkdir();
        saveDefaultConfig();

        // Start Discord bot
        try {
            new Bot();
        } catch (LoginException e) {
            Bukkit.getLogger().warning("ERROR: Bot login failed! Get new bot token and add it to the config!");
            botFailed = true;
        }

        // Wait for bot ready
        try {
            Bot.getJDA().awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Send server start message in log channel
        Bot.getJDA().getGuilds()
                .forEach((Guild guild) -> guild.getSystemChannel().sendMessage("Server has been started.").queue());

        // Get saved channels and roles
        CommandsManager.initChannelsRoles();

        // If chat was previously paused, restore and notify in console
        if (getConfig().getBoolean("chatPaused")) {
            chatPaused = true;
            Bukkit.getServer().getLogger().info("[Dreamvisitor] Chat is currently paused! Use /pausechat to allow users to chat.");
        }
    }

    public static App getPlugin() {
        return plugin;
    }

    public static String getPlayerPath(Player player) {
        return plugin.getDataFolder().getAbsolutePath() + "/player/" + player.getUniqueId() + ".yml";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // pausechat command
        if (label.equalsIgnoreCase("pausechat")) {
            TextChannel chatChannel = Bot.getJDA().getTextChannelById(CommandsManager.getChatChannel());
            // If chat is paused, unpause. If not, pause
            if (chatPaused == true) {
                chatPaused = false;
                getConfig().set("chatPaused", false);
                Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "Chat has been unpaused.");
                if (chatChannel != null) {
                    chatChannel.sendMessage("**Chat has been unpaused. Messages will now be sent to Minecraft**").queue();
                }
            } else {
                chatPaused = true;
                getConfig().set("chatPaused", true);
                Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "Chat has been paused.");
                if (chatChannel != null) {
                    chatChannel.sendMessage("**Chat has been paused. Messages will not be sent to Minecraft**").queue();
                }
            }
            saveConfig();

        } else if (label.equalsIgnoreCase("radio")) {
            if (sender instanceof Player) {

            } else if (sender instanceof ConsoleCommandSender) {

            }
        } else if (label.equalsIgnoreCase("discord")) {
            if (sender instanceof Player) {

            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        // Send chat messages to Discord
        // IF chat is not paused AND the player is not an operator OR the player is an operator, send message
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
        // Remind bot login failure
        if (botFailed && event.getPlayer().isOp()) {
            event.getPlayer().sendMessage(
                    "\u00a71[Dreamvisitor] \u00a7aBot login failed on server start! You may need a new login token.");
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
        PlayerUtility.setPlayerMemory(event.getPlayer(), null);
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
        // Shutdown messages
        getLogger().info("Closing bot instance.");
        Bot.getJDA().getGuilds()
                .forEach((Guild guild) -> guild.getSystemChannel().sendMessage("Server has been shutdown.").queue());
        // Shut down bot
        Bot.getJDA().shutdown();

    }

}