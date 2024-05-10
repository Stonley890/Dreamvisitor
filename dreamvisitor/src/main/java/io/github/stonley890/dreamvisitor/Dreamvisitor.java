package io.github.stonley890.dreamvisitor;

import io.github.stonley890.dreamvisitor.commands.*;
import io.github.stonley890.dreamvisitor.commands.tabcomplete.*;
import io.github.stonley890.dreamvisitor.data.*;
import io.github.stonley890.dreamvisitor.discord.DiscCommandsManager;
import io.github.stonley890.dreamvisitor.functions.ConsoleLogger;
import io.github.stonley890.dreamvisitor.functions.ItemBanList;
import io.github.stonley890.dreamvisitor.functions.Moonglobe;
import io.github.stonley890.dreamvisitor.functions.Sandbox;
import io.github.stonley890.dreamvisitor.listeners.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

/*
 * The main ticking thread.
*/

@SuppressWarnings({ "null" })
public class Dreamvisitor extends JavaPlugin {

    public static final String PREFIX = ChatColor.DARK_BLUE + "[" + ChatColor.WHITE + "DV" + ChatColor.DARK_BLUE + "] " + ChatColor.RESET;
    // private
    private static final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
    // public
    public static Dreamvisitor PLUGIN;
    public static String MOTD = null;
    public static boolean chatPaused;
    public static int playerLimit;
    public static Location hubLocation;
    public static boolean webWhitelistEnabled;
    public static boolean debugMode;
    public static boolean restartScheduled = false;
    public static boolean botFailed = true;
    private static ConsoleLogger appender;
    public final String VERSION = getDescription().getVersion();

    public static Dreamvisitor getPlugin() {
        return PLUGIN;
    }

    public static @NotNull String getPlayerPath(@NotNull UUID uuid) {
        return PLUGIN.getDataFolder().getAbsolutePath() + "/player/" + uuid + ".yml";
    }

    public static void debug(String message) {
        if (getPlugin().getConfig().getBoolean("debug")){
            Bukkit.getLogger().info("DEBUG: " + message);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onEnable() {

        try {
            // Initialize variables
            PLUGIN = this;

            debugMode = getConfig().getBoolean("debug");

            checkConfig();

            debug("Registering listeners...");
            registerListeners();

            debug("Initializing command executors...");
            registerCommands();

            debug("Initializing tab completers...");
            registerTabCompletion();

            debug("Creating data folder...");
            // Create config if needed
            boolean directoryCreated = getDataFolder().mkdir();
            if (!directoryCreated) debug("Dreamvisitor did not create a data folder. It may already exist.");
            saveDefaultConfig();

            // Initialize account link
            debug("Initializing accountLink.txt");
            AccountLink.init();

            // Initialize infractions
            debug("Initializing infractions.yml");
            Infraction.init();

            // Init alts
            debug("Initializing alts.yml");
            AltFamily.init();

            // Init eco
            debug("Initializing economy.yml");
            Economy.init();

            // Start message
            getLogger().log(Level.INFO, "Dreamvisitor: A plugin created by Bog for WoF:TNW to add various features.");

            // Bot
            debug("Starting Dreamvisitor bot...");
            Bot.startBot(getConfig());

            if (!botFailed) {
                // Get saved data
                debug("Fetching recorded channels and roles from config.");
                DiscCommandsManager.init();

                // Send server start message
                try {
                    Bot.getGameLogChannel().sendMessage("Server has been started.\n*Dreamvisitor " + VERSION + "*").queue();
                } catch (InsufficientPermissionException e) {
                    Bukkit.getLogger().severe("Dreamvisitor Bot does not have permission to send messages in the game log channel!");
                    throw e;
                }

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

            // Create item banlist if empty
            debug("Restoring item banlist...");
            if (PLUGIN.getConfig().get("itemBlacklist") != null ) {
                ArrayList<ItemStack> itemList = (ArrayList<ItemStack>) PLUGIN.getConfig().getList("itemBlacklist");
                if (itemList != null) {
                    debug("Item banlist is null. Creating an empty banlist...");
                    ItemBanList.badItems = itemList.toArray(new ItemStack[0]);
                }
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

                        // If there are no messages in the queue, return
                        if (ConsoleLogger.messageBuilder.isEmpty()) return;

                        try {
                            Bot.getGameLogChannel().sendMessage(ConsoleLogger.messageBuilder.toString()).queue(); // send message
                        } catch (InsufficientPermissionException e) {
                            Bukkit.getLogger().warning("Dreamvisitor Bot does not have the necessary permissions to send messages in game log channel.");
                        } catch (IllegalArgumentException e) {
                            Bukkit.getLogger().severe("Console logger tried to send an invalid message!");
                        }

                        ConsoleLogger.messageBuilder.delete(0, ConsoleLogger.messageBuilder.length()); // delete queued messages

                        // If there are no overflow messages, return
                        if (ConsoleLogger.overFlowMessages.isEmpty()) return;

                        StringBuilder overFlowMessageBuilder = new StringBuilder();
                        // First is safe, so add now
                        overFlowMessageBuilder.append(ConsoleLogger.overFlowMessages.get(0));

                        // For each message in overflow
                        for (int i = 1; i < ConsoleLogger.overFlowMessages.size(); i++) {

                            // Check that it fits
                            if ((overFlowMessageBuilder.toString().length() + ConsoleLogger.overFlowMessages.get(i).length() + "\n".length()) >= 2000) {
                                // if not, queue current message and clear string builder
                                try {
                                    Bot.getGameLogChannel().sendMessage(overFlowMessageBuilder.toString()).queue();
                                } catch (InsufficientPermissionException e) {
                                    Bukkit.getLogger().warning("Dreamvisitor Bot does not have the necessary permissions to send messages in game log channel.");
                                } catch (IllegalArgumentException e) {
                                    Bukkit.getLogger().severe("Console logger tried to send an invalid message!");
                                }
                                overFlowMessageBuilder = new StringBuilder();

                            } else overFlowMessageBuilder.append(ConsoleLogger.overFlowMessages.get(i)).append("\n");
                        }

                        ConsoleLogger.overFlowMessages.clear();

                    }
                }
            };

            Runnable scheduledRestarts = new BukkitRunnable() {
                @Override
                public void run() {
                    // Restart if requested and no players are online
                    if (restartScheduled && Bukkit.getOnlinePlayers().isEmpty()) {
                        Bukkit.getLogger().info(PREFIX + "Restarting the server as scheduled.");
                        Bot.sendLog("**Restarting the server as scheduled.**");
                        getServer().spigot().restart();
                    }

                    // also check if memory usage is high and schedule restart
                    long maxMemory = Runtime.getRuntime().maxMemory();
                    long freeMemory = Runtime.getRuntime().freeMemory();
                    double freeMemoryPercent = ((double) freeMemory / maxMemory) * 100;
                    if (freeMemoryPercent <= 10) {
                        restartScheduled = true;
                        Bukkit.getLogger().info("Dreamvisitor scheduled a restart because free memory usage is at or less than 10%.");
                    }
                }
            };

            Runnable tick = new BukkitRunnable() {
                @Override
                public void run() {
                    Moonglobe.tick();
                }
            };

            Bukkit.getScheduler().runTaskTimer(this, tick, 0, 0);

            // Push console every two seconds
            if (!botFailed) Bukkit.getScheduler().runTaskTimer(this,pushConsole,0,40);

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

                for (StackTraceElement stackTraceElement : e.getStackTrace()) builder.append("\n").append(stackTraceElement.toString());

                Bot.getJda().retrieveUserById(505833634134228992L).complete().openPrivateChannel().complete().sendMessage(builder.toString()).complete();
            }

            Bukkit.getPluginManager().disablePlugin(this);
            throw new RuntimeException();

        }
    }

    private void checkConfig() throws InvalidConfigurationException {
        if (getConfig().getLongList("triberoles").size() != 10) throw new InvalidConfigurationException("triberoles must contain exactly 10 entries.");
        if (getConfig().getInt("playerlimit") < -1) getConfig().set("playerlimit", -1);
        if (getConfig().getInt("infraction-expire-time-days") < 1) throw new InvalidConfigurationException("infraction-expire-time-days must be at least 1.");
        saveConfig();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ListenEntityDamage(), this);
        getServer().getPluginManager().registerEvents(new ListenPlayerChat(), this);
        getServer().getPluginManager().registerEvents(new ListenPlayerCmdPreprocess(), this);
        getServer().getPluginManager().registerEvents(new ListenPlayerDeath(), this);
        getServer().getPluginManager().registerEvents(new ListenPlayerJoin(), this);
        getServer().getPluginManager().registerEvents(new ListenPlayerLogin(), this);
        getServer().getPluginManager().registerEvents(new ListenPlayerQuit(), this);
        getServer().getPluginManager().registerEvents(new ItemBanList(), this);
        getServer().getPluginManager().registerEvents(new ListenPlayerGameModeChange(), this);
        getServer().getPluginManager().registerEvents(new ListenServerPing(), this);
        getServer().getPluginManager().registerEvents(new Sandbox(), this);
    }

    private void registerCommands() throws NullPointerException {
        try {
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
            Objects.requireNonNull(getCommand("itembanlist")).setExecutor(new CmdItemBanList());
            Objects.requireNonNull(getCommand("user")).setExecutor(new CmdUser());
            Objects.requireNonNull(getCommand("tribeupdate")).setExecutor(new CmdTribeUpdate());
            Objects.requireNonNull(getCommand("unwax")).setExecutor(new CmdUnwax());
            Objects.requireNonNull(getCommand("schedulerestart")).setExecutor(new CmdScheduleRestart());
            Objects.requireNonNull(getCommand("invswap")).setExecutor(new CmdInvSwap());
            Objects.requireNonNull(getCommand("dvset")).setExecutor(new CmdDvset());
            Objects.requireNonNull(getCommand("setmotd")).setExecutor(new CmdSetmotd());
            Objects.requireNonNull(getCommand("synctime")).setExecutor(new CmdSynctime());
            Objects.requireNonNull(getCommand("sandbox")).setExecutor(new CmdSandbox());
            Objects.requireNonNull(getCommand("moonglobe")).setExecutor(new CmdMoonglobe());
            Objects.requireNonNull(getCommand("setback")).setExecutor(new CmdSetback());
        } catch (NullPointerException e) {
            throw new NullPointerException("One or more Minecraft commands intended to be registered does not exist.");
        }

    }

    private void registerTabCompletion() throws NullPointerException {
        try {
            Objects.requireNonNull(getCommand("pausebypass")).setTabCompleter(new TabPauseBypass());
            Objects.requireNonNull(getCommand("softwhitelist")).setTabCompleter(new TabSoftWhitelist());
            Objects.requireNonNull(getCommand("hub")).setTabCompleter(new TabHub());
            Objects.requireNonNull(getCommand("tribeupdate")).setTabCompleter(new TabTribeUpdate());
            Objects.requireNonNull(getCommand("moonglobe")).setTabCompleter(new TabMoonglobe());
            Objects.requireNonNull(getCommand("setback")).setTabCompleter(new TabSetback());
        } catch (NullPointerException e) {
            throw new NullPointerException("One or more Minecraft commands intended to be registered does not exist.");
        }
    }

    @Override
    public void onDisable() {

        if (!botFailed) {
            // Shutdown messages
            getLogger().info("Closing bot instance.");
            int requestsCanceled = Bot.getJda().cancelRequests();
            if (requestsCanceled > 0) getLogger().info(requestsCanceled + " queued bot requests were canceled for shutdown.");
            Bot.getGameLogChannel().sendMessage("*Server has been shut down.*").complete();
            Bot.getJda().shutdownNow();
        }

        // remove moon globes
        for (Moonglobe moonglobe : Moonglobe.activeMoonglobes) moonglobe.remove(null);

        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                PlayerUtility.savePlayerMemory(player.getUniqueId());
                PlayerUtility.clearPlayerMemory(player.getUniqueId());
            } catch (IOException e) {
                Bukkit.getLogger().severe("Unable to save player memory! Does the server have write access?");
                if (Dreamvisitor.debugMode) throw new RuntimeException();
            }
        }

        logger.removeAppender(appender);
    }

}