package io.github.stonley890.dreamvisitor;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.stonley890.dreamvisitor.commands.*;
import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;
import io.github.stonley890.dreamvisitor.listeners.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

/*
 * The main ticking thread.
*/

@SuppressWarnings({ "null" })
public class Dreamvisitor extends JavaPlugin {

    public final String version = getDescription().getVersion();

    public static Dreamvisitor plugin;
    public static boolean chatPaused;
    public static int playerlimit;
    public static Location hubLocation;

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
        getCommand("pausebypass").setExecutor(new CmdPauseBypass());
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

        if (!botFailed) {
            // Send server start message
            Bot.sendMessage(DiscCommandsManager.gameLogChannel, "Server has been started.\n*Dreamvisitor " + version + "*");

            // Get saved data
            DiscCommandsManager.initChannelsRoles();
        }

        // If chat was previously paused, restore and notify in console
        if (getConfig().getBoolean("chatPaused")) {
            chatPaused = true;
            Bukkit.getServer().getLogger().info(
                    "[Dreamvisitor] Chat is currently paused from last session! Use /pausechat to allow users to chat.");
        }

        // Restore player limit override
        playerlimit = getConfig().getInt("playerlimit");
        Bukkit.getServer().getLogger().info(
                "[Dreamvisitor] Player limit override is currently set to " + playerlimit);
    }

    public static Dreamvisitor getPlugin() {
        return plugin;
    }

    public static String getPlayerPath(Player player) {
        return plugin.getDataFolder().getAbsolutePath() + "/player/" + player.getUniqueId() + ".yml";
    }

    @Override
    public void onDisable() {
        if (!botFailed) {
            // Shutdown messages
            getLogger().info("Closing bot instance.");
            jda.getGuilds()
                    .forEach(
                            (Guild guild) -> guild.getSystemChannel().sendMessage("Server has been shutdown.").queue());
            // Shut down bot
            jda.shutdown();
        }
    }

}