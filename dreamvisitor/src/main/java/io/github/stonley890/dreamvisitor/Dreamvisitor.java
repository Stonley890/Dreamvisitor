package io.github.stonley890.dreamvisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

import io.github.stonley890.dreamvisitor.data.AccountLink;
import io.github.stonley890.dreamvisitor.google.UserTracker;
import net.dv8tion.jda.api.entities.User;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.stonley890.dreamvisitor.commands.*;
import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;
import io.github.stonley890.dreamvisitor.commands.tabcomplete.TabPauseBypass;
import io.github.stonley890.dreamvisitor.commands.tabcomplete.TabSoftWhitelist;
import io.github.stonley890.dreamvisitor.listeners.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

/*
 * The main ticking thread.
*/

@SuppressWarnings({ "null" })
public class Dreamvisitor extends JavaPlugin {

    public final String VERSION = getDescription().getVersion();
    public static final String PREFIX = ChatColor.DARK_BLUE + "[" + ChatColor.WHITE + "DV" + ChatColor.DARK_BLUE + "] " + ChatColor.RESET;

    public static Dreamvisitor plugin;
    private static final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
    private static ConsoleLogger appender;
    public static boolean chatPaused;
    public static int playerlimit;
    public static Location hubLocation;

    public static boolean botFailed = false;

    JDA jda;

    @Override
    public void onEnable() {

        // Initialize variables
        plugin = this;

        debug("Registering listeners...");
        // Register listeners
        getServer().getPluginManager().registerEvents(new ListenEntityDamage(), this);
        getServer().getPluginManager().registerEvents(new ListenPlayerChat(), this);
        getServer().getPluginManager().registerEvents(new ListenPlayerCmdPreprocess(), this);
        getServer().getPluginManager().registerEvents(new ListenPlayerDeath(), this);
        getServer().getPluginManager().registerEvents(new ListenPlayerJoin(), this);
        getServer().getPluginManager().registerEvents(new ListenPlayerLogin(), this);
        getServer().getPluginManager().registerEvents(new ListenPlayerQuit(), this);
        getServer().getPluginManager().registerEvents(new ListenInventoryClose(), this);

        debug("Initializing command executors...");
        // Initialize command executors
        Objects.requireNonNull(getCommand("aradio")).setExecutor(new CmdAradio());
        Objects.requireNonNull(getCommand("discord")).setExecutor(new CmdDiscord());
        Objects.requireNonNull(getCommand("hub")).setExecutor(new CmdHub());
        Objects.requireNonNull(getCommand("panic")).setExecutor(new CmdPanic());
        Objects.requireNonNull(getCommand("pausebypass")).setExecutor(new CmdPauseBypass());
        Objects.requireNonNull(getCommand("pausechat")).setExecutor(new CmdPausechat());
        Objects.requireNonNull(getCommand("playerlimit")).setExecutor(new CmdPlayerlimit());
        Objects.requireNonNull(getCommand("radio")).setExecutor(new CmdRadio());
        // getCommand("reloadbot").setExecutor(new CmdReloadbot());
        Objects.requireNonNull(getCommand("sethub")).setExecutor(new CmdSethub());
        Objects.requireNonNull(getCommand("softwhitelist")).setExecutor(new CmdSoftwhitelist());
        Objects.requireNonNull(getCommand("tagradio")).setExecutor(new CmdTagRadio());
        Objects.requireNonNull(getCommand("togglepvp")).setExecutor(new CmdTogglepvp());
        Objects.requireNonNull(getCommand("zoop")).setExecutor(new CmdZoop());
        Objects.requireNonNull(getCommand("itemblacklist")).setExecutor(new CmdItemBlacklist());
        Objects.requireNonNull(getCommand("tribeupdate")).setExecutor(new CmdTribeUpdate());
        Objects.requireNonNull(getCommand("user")).setExecutor(new CmdUser());

        debug("Initializing tab completers...");
        // Initialize command tab completers
        Objects.requireNonNull(getCommand("pausebypass")).setTabCompleter(new TabPauseBypass());
        Objects.requireNonNull(getCommand("softwhitelist")).setTabCompleter(new TabSoftWhitelist());

        debug("Creating data folder...");
        // Create config if needed
        getDataFolder().mkdir();
        saveDefaultConfig();

        debug("Initializing accountLink.txt");
        AccountLink.init();

        // Start message
        getLogger().log(Level.INFO, "Dreamvisitor: A plugin created by Bog for WoF:TNW to add various features.");

        // Bot
        debug("Starting Dreamvisitor bot...");
        Bot.startBot();
        jda = Bot.getJda();

        if (!botFailed) {
            // Get saved data
            debug("Fetching recorded channels and roles from config.");
            DiscCommandsManager.initChannelsRoles();

            // Send server start message
            Bot.sendMessage(DiscCommandsManager.gameLogChannel, "Server has been started.\n*Dreamvisitor " + VERSION + "*");
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

        // Create item blacklist if empty
        if (plugin.getConfig().get("itemBlacklist") != null ) {
            ArrayList<ItemStack> itemList = (ArrayList<ItemStack>) plugin.getConfig().get("itemBlacklist");
            if (itemList != null) {
                debug("Item blacklist is null. Creating an empty blacklist...");
                CmdItemBlacklist.badItems = itemList.toArray(new ItemStack[0]);
            }
        }

        appender = new ConsoleLogger();
        logger.addAppender(appender);

        debug("Enable finished.");

    }

    public static Dreamvisitor getPlugin() {
        return plugin;
    }

    public static String getPlayerPath(Player player) {
        return plugin.getDataFolder().getAbsolutePath() + "/player/" + player.getUniqueId() + ".yml";
    }

    public static void debug(String message) {
        if (plugin.getConfig().getBoolean("debug")){
            Bukkit.getLogger().info("DEBUG: " + message);
        }
    }

    @Override
    public void onDisable() {

        if (!botFailed) {
            // Shutdown messages
            getLogger().info("Closing bot instance.");
            Bot.sendMessage(DiscCommandsManager.gameLogChannel, "Server has been shut down.");
            Bot.getJda().shutdownNow();
        }

        logger.removeAppender(appender);
    }

}