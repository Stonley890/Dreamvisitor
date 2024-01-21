package io.github.stonley890.dreamvisitor;

import io.github.stonley890.dreamvisitor.commands.*;
import io.github.stonley890.dreamvisitor.commands.tabcomplete.TabHub;
import io.github.stonley890.dreamvisitor.commands.tabcomplete.TabPauseBypass;
import io.github.stonley890.dreamvisitor.commands.tabcomplete.TabSoftWhitelist;
import io.github.stonley890.dreamvisitor.commands.tabcomplete.TabTribeUpdate;
import io.github.stonley890.dreamvisitor.data.AccountLink;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import io.github.stonley890.dreamvisitor.data.Whitelist;
import io.github.stonley890.dreamvisitor.discord.DiscCommandsManager;
import io.github.stonley890.dreamvisitor.listeners.*;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

/*
 * The main ticking thread.
*/

@SuppressWarnings({ "null" })
public class Dreamvisitor extends JavaPlugin {

    // public
    public static Dreamvisitor plugin;
    public static String MOTD = null;
    public final String VERSION = getDescription().getVersion();
    public static final String PREFIX = ChatColor.DARK_BLUE + "[" + ChatColor.WHITE + "DV" + ChatColor.DARK_BLUE + "] " + ChatColor.RESET;
    public static boolean chatPaused;
    public static int playerLimit;
    public static Location hubLocation;
    public static String resourcePackHash;
    public static boolean webWhitelistEnabled;
    public static boolean debug;
    public static boolean restartScheduled;
    public static boolean botFailed = false;

    // private
    private static final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
    private static ConsoleLogger appender;

    @Override
    public void onEnable() {

        try {
            // Initialize variables
            plugin = this;

            debug = getConfig().getBoolean("debug");

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
            getServer().getPluginManager().registerEvents(new ListenPlayerGameModeChange(), this);
            getServer().getPluginManager().registerEvents(new ListenServerPing(), this);

            debug("Initializing command executors...");
            // Initialize command executors
            Objects.requireNonNull(getCommand("aradio")).setExecutor(new CmdRadio());
            Objects.requireNonNull(getCommand("discord")).setExecutor(new CmdDiscord());
            Objects.requireNonNull(getCommand("hub")).setExecutor(new CmdHub());
            Objects.requireNonNull(getCommand("panic")).setExecutor(new CmdPanic());
            Objects.requireNonNull(getCommand("pausebypass")).setExecutor(new CmdPauseBypass());
            Objects.requireNonNull(getCommand("pausechat")).setExecutor(new CmdPausechat());
            Objects.requireNonNull(getCommand("playerlimit")).setExecutor(new CmdPlayerlimit());
            Objects.requireNonNull(getCommand("radio")).setExecutor(new CmdRadio());
            Objects.requireNonNull(getCommand("sethub")).setExecutor(new CmdSethub());
            Objects.requireNonNull(getCommand("softwhitelist")).setExecutor(new CmdSoftwhitelist());
            Objects.requireNonNull(getCommand("tagradio")).setExecutor(new CmdRadio());
            Objects.requireNonNull(getCommand("togglepvp")).setExecutor(new CmdTogglepvp());
            Objects.requireNonNull(getCommand("zoop")).setExecutor(new CmdZoop());
            Objects.requireNonNull(getCommand("itemblacklist")).setExecutor(new CmdItemBlacklist());
            Objects.requireNonNull(getCommand("user")).setExecutor(new CmdUser());
            Objects.requireNonNull(getCommand("tribeupdate")).setExecutor(new CmdTribeUpdate());
            Objects.requireNonNull(getCommand("unwax")).setExecutor(new CmdUnwax());
            Objects.requireNonNull(getCommand("schedulerestart")).setExecutor(new CmdScheduleRestart());
            Objects.requireNonNull(getCommand("invswap")).setExecutor(new CmdInvSwap());
            Objects.requireNonNull(getCommand("dvset")).setExecutor(new CmdDvset());
            Objects.requireNonNull(getCommand("setmotd")).setExecutor(new CmdSetmotd());
            Objects.requireNonNull(getCommand("synctime")).setExecutor(new CmdSynctime());
            Objects.requireNonNull(getCommand("synctime")).setExecutor(new CmdSandbox());

            debug("Initializing tab completers...");
            // Initialize command tab completers
            Objects.requireNonNull(getCommand("pausebypass")).setTabCompleter(new TabPauseBypass());
            Objects.requireNonNull(getCommand("softwhitelist")).setTabCompleter(new TabSoftWhitelist());
            Objects.requireNonNull(getCommand("hub")).setTabCompleter(new TabHub());
            Objects.requireNonNull(getCommand("tribeupdate")).setTabCompleter(new TabTribeUpdate());

            debug("Creating data folder...");
            // Create config if needed
            if (!getDataFolder().mkdir()) {
                Bukkit.getLogger().warning("Dreamvisitor could not create a data folder!");
            }
            saveDefaultConfig();

            // Initialize account link
            debug("Initializing accountLink.txt");
            AccountLink.init();

            // Start message
            getLogger().log(Level.INFO, "Dreamvisitor: A plugin created by Bog for WoF:TNW to add various features.");

            // Bot
            debug("Starting Dreamvisitor bot...");
            Bot.startBot();

            if (!botFailed) {
                // Get saved data
                debug("Fetching recorded channels and roles from config.");
                DiscCommandsManager.initChannelsRoles(getConfig());

                // Send server start message
                Bot.gameLogChannel.sendMessage("Server has been started.\n*Dreamvisitor " + VERSION + "*").queue();
            }

            // If chat was previously paused, restore and notify in console\
            debug("Restoring chat pause...");
            if (getConfig().getBoolean("chatPaused")) {
                chatPaused = true;
                Bukkit.getServer().getLogger().info(PREFIX +
                        "Chat is currently paused from last session! Use /pausechat to allow users to chat.");
            }

            // Restore player limit override
            debug("Restoring player limit override...");
            playerLimit = getConfig().getInt("playerlimit");
            getServer().getLogger().info(PREFIX +
                    "Player limit override is currently set to " + playerLimit);
            // getServer().setMaxPlayers(playerlimit);

            // Create item blacklist if empty
            debug("Restoring item blacklist...");
            if (plugin.getConfig().get("itemBlacklist") != null ) {
                ArrayList<ItemStack> itemList = (ArrayList<ItemStack>) plugin.getConfig().get("itemBlacklist");
                if (itemList != null) {
                    debug("Item blacklist is null. Creating an empty blacklist...");
                    CmdItemBlacklist.badItems = itemList.toArray(new ItemStack[0]);
                }
            }

            // Get resource pack hash
            debug("Getting resource pack hash...");
            try (InputStream input = Files.newInputStream(Paths.get("server.properties"))) {
                java.util.Properties prop = new java.util.Properties();
                prop.load(input);
                resourcePackHash = prop.getProperty("resource-pack-sha1");
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Console logging
            debug("Setting up console logging...");
            appender = new ConsoleLogger();
            logger.addAppender(appender);

            // Set up web whitelist if enabled
            webWhitelistEnabled = getConfig().getBoolean("web-whitelist");
            if (webWhitelistEnabled) Whitelist.startWeb();

            Runnable pushConsole = new BukkitRunnable() {
                // Push console log to Discord every 2 seconds
                @Override
                public void run() {
                    if (Dreamvisitor.getPlugin().getConfig().getBoolean("log-console")) {

                        // If there are messages in the queue, send them!
                        if (ConsoleLogger.messageBuilder != null && !ConsoleLogger.messageBuilder.isEmpty()) {

                            Bot.gameLogChannel.sendMessage(ConsoleLogger.messageBuilder.toString()).queue();
                            ConsoleLogger.messageBuilder.delete(0, ConsoleLogger.messageBuilder.length());

                            // If there are overflow messages, build and send those too
                            if (ConsoleLogger.overFlowMessages != null && !ConsoleLogger.overFlowMessages.isEmpty()) {

                                StringBuilder overFlowMessageBuilder = new StringBuilder();
                                // First is safe, so add now
                                overFlowMessageBuilder.append(ConsoleLogger.overFlowMessages.get(0));

                                // For each message in overflow
                                for (int i = 1; i < ConsoleLogger.overFlowMessages.size(); i++) {

                                    // Check that it fits
                                    if (overFlowMessageBuilder.length() + ConsoleLogger.overFlowMessages.get(i).length() + "\n".length() >= 2000) {
                                        // if not, queue current message and clear string builder
                                        Bot.gameLogChannel.sendMessage(overFlowMessageBuilder.toString().replaceAll("_","\\_")).queue();
                                        overFlowMessageBuilder = new StringBuilder();

                                    } else {
                                        overFlowMessageBuilder.append(ConsoleLogger.overFlowMessages.get(i)).append("\n");
                                    }
                                }

                                ConsoleLogger.overFlowMessages.clear();

                            }
                        }
                    }
                }
            };

            Runnable scheduledRestarts = new BukkitRunnable() {
                @Override
                public void run() {
                    // Restart if requested and no players are online
                    if (restartScheduled && Bukkit.getOnlinePlayers().isEmpty()) {
                        Bukkit.getLogger().info(PREFIX + "Restarting the server as scheduled.");
                        Bot.sendMessage(Bot.gameLogChannel, "**Restarting the server as scheduled.**");
                        getServer().spigot().restart();
                    }

                    // also check for missing channels
                    if (Bot.gameLogChannel == null || Bot.gameChatChannel == null || Bot.whitelistChannel == null) DiscCommandsManager.initChannelsRoles(getConfig());
                }
            };

            if (!botFailed) {
                // Push console every two seconds
                Bukkit.getScheduler().runTaskTimer(this,pushConsole,0,40);
            }

            // Check for scheduled restart every minute
            Bukkit.getScheduler().runTaskTimer(this, scheduledRestarts, 200, 1200);

            debug("Enable finished.");
        } catch (Exception e) {

            Bukkit.getLogger().severe("Dreamvisitor was unable to start :(\nPlease notify Bog with the following stack trace:");
            e.printStackTrace();

            if (!botFailed) {
                // Send startup crashes.
                StringBuilder builder = new StringBuilder();

                builder.append(e.getMessage());

                for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                    builder.append("\n").append(stackTraceElement.toString());
                }

                Bot.getJda().retrieveUserById(505833634134228992L).complete().openPrivateChannel().complete().sendMessage(builder.toString()).complete();
                Bukkit.getPluginManager().disablePlugin(this);
            }

        }


    }

    public static Dreamvisitor getPlugin() {
        return plugin;
    }

    public static @NotNull String getPlayerPath(UUID uuid) {
        return plugin.getDataFolder().getAbsolutePath() + "/player/" + uuid + ".yml";
    }

    public static void debug(String message) {
        if (getPlugin().getConfig().getBoolean("debug")){
            Bukkit.getLogger().info("DEBUG: " + message);
        }
    }

    @Override
    public void onDisable() {

        if (!botFailed) {
            // Shutdown messages
            getLogger().info("Closing bot instance.");
            int requestsCanceled = Bot.getJda().cancelRequests();
            if (requestsCanceled > 0) getLogger().info(requestsCanceled + " queued bot requests were canceled for shutdown.");
            Bot.gameLogChannel.sendMessage("*Server has been shut down.*").complete();
            Bot.getJda().shutdownNow();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                PlayerUtility.savePlayerMemory(player.getUniqueId());
                PlayerUtility.clearPlayerMemory(player.getUniqueId());
            } catch (IOException e) {
                Bukkit.getLogger().severe("Unable to save player memory! Does the server have write access?");
                if (Dreamvisitor.debug) e.printStackTrace();
            }
        }

        try {
            AccountLink.saveFile();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Unable to save accountLink.txt!");
        }

        logger.removeAppender(appender);
    }

}