package io.github.stonley890;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import io.github.stonley890.commands.CmdAradio;
import io.github.stonley890.commands.CmdDiscord;
import io.github.stonley890.commands.CmdHub;
import io.github.stonley890.commands.CmdPanic;
import io.github.stonley890.commands.CmdPausebypass;
import io.github.stonley890.commands.CmdPausechat;
import io.github.stonley890.commands.CmdPlayerlimit;
import io.github.stonley890.commands.CmdRadio;
import io.github.stonley890.commands.CmdReloadbot;
import io.github.stonley890.commands.CmdSethub;
import io.github.stonley890.commands.CmdSoftwhitelist;
import io.github.stonley890.commands.CmdTagRadio;
import io.github.stonley890.commands.CmdTogglepvp;
import io.github.stonley890.commands.CmdZoop;
import io.github.stonley890.commands.DiscCommandsManager;
import io.github.stonley890.listeners.ListenEntityDamage;
import io.github.stonley890.listeners.ListenPlayerChat;
import io.github.stonley890.listeners.ListenPlayerCmdPreprocess;
import io.github.stonley890.listeners.ListenPlayerDeath;
import io.github.stonley890.listeners.ListenPlayerJoin;
import io.github.stonley890.listeners.ListenPlayerLogin;
import io.github.stonley890.listeners.ListenPlayerQuit;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

/*
 * The main ticking thread.
*/

@SuppressWarnings({"null"})
public class Main extends JavaPlugin {

    public final String version = getDescription().getVersion();

    public static Main plugin;
    public static boolean chatPaused;
    public static int playerlimit;
    public Location hubLocation;

    public static boolean botFailed = false;

    JDA jda;


    @Override
    public void onEnable() {

        // Initialize variables
        plugin = this;

        // Register listeners
        getServer().getPluginManager().registerEvents(new ListenEntityDamage(), this);
        getServer().getPluginManager().registerEvents(new ListenPlayerChat(), this);
        getServer().getPluginManager().registerEvents(new ListenPlayerCmdPreprocess(), this);
        getServer().getPluginManager().registerEvents(new ListenPlayerDeath(), this);
        getServer().getPluginManager().registerEvents(new ListenPlayerJoin(), this);
        getServer().getPluginManager().registerEvents(new ListenPlayerLogin(), this);
        getServer().getPluginManager().registerEvents(new ListenPlayerQuit(), this);

        // Initialize command executors
        getCommand("aradio").setExecutor(new CmdAradio());
        getCommand("discord").setExecutor(new CmdDiscord());
        getCommand("hub").setExecutor(new CmdHub());
        getCommand("panic").setExecutor(new CmdPanic());
        getCommand("pausebypass").setExecutor(new CmdPausebypass());
        getCommand("pausechat").setExecutor(new CmdPausechat());
        getCommand("playerlimit").setExecutor(new CmdPlayerlimit());
        getCommand("radio").setExecutor(new CmdRadio());
        getCommand("reloadbot").setExecutor(new CmdReloadbot());
        getCommand("sethub").setExecutor(new CmdSethub());
        getCommand("softwhitelist").setExecutor(new CmdSoftwhitelist());
        getCommand("tagradio").setExecutor(new CmdTagRadio());
        getCommand("togglepvp").setExecutor(new CmdTogglepvp());
        getCommand("zoop").setExecutor(new CmdZoop());

        // Create config if needed
        getDataFolder().mkdir();
        saveDefaultConfig();

        // Start message
        getLogger().log(Level.INFO, "Dreamvisitor: A plugin created by Bog for WoF:TNW to add various features.");

        // Bot
        Bot.startBot();
        jda = Bot.getJda();

        // Send server start message in log channel
        jda.getGuilds().forEach((Guild guild) -> guild.getSystemChannel()
                .sendMessage("Server has been started.\n*Dreamvisitor " + version + "*").queue());

        // Get saved data
        DiscCommandsManager.initChannelsRoles();

        // If chat was previously paused, restore and notify in console
        if (getConfig().getBoolean("chatPaused")) {
            chatPaused = true;
            Bukkit.getServer().getLogger().info(
                    "[Dreamvisitor] Chat is currently paused from last session! Use /pausechat to allow users to chat.");
        }

        // Restore player limit override
        playerlimit = getConfig().getInt("playerlimit");
    }

    public static Main getPlugin() {
        return plugin;
    }

    public static String getPlayerPath(Player player) {
        return plugin.getDataFolder().getAbsolutePath() + "/player/" + player.getUniqueId() + ".yml";
    }

    @Override
    public void onDisable() {
        // Shutdown messages
        getLogger().info("Closing bot instance.");
        jda.getGuilds()
                .forEach((Guild guild) -> guild.getSystemChannel().sendMessage("Server has been shutdown.").queue());
        // Shut down bot
        jda.shutdown();

    }

}