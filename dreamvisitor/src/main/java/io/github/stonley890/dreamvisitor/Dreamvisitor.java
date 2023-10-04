package io.github.stonley890.dreamvisitor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

import io.github.stonley890.dreamvisitor.data.AccountLink;
import io.github.stonley890.dreamvisitor.data.Whitelist;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.stonley890.dreamvisitor.commands.*;
import io.github.stonley890.dreamvisitor.discord.DiscCommandsManager;
import io.github.stonley890.dreamvisitor.commands.tabcomplete.TabPauseBypass;
import io.github.stonley890.dreamvisitor.commands.tabcomplete.TabSoftWhitelist;
import io.github.stonley890.dreamvisitor.listeners.*;
import net.dv8tion.jda.api.JDA;
import org.bukkit.scheduler.BukkitRunnable;
import org.shanerx.mojang.Mojang;
import spark.Spark;

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
    public static String resourcePackHash;
    public static boolean debug;
    public static boolean botFailed = false;

    JDA jda;

    @Override
    public void onEnable() {

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
        Objects.requireNonNull(getCommand("user")).setExecutor(new CmdUser());
        Objects.requireNonNull(getCommand("tribeupdate")).setExecutor(new CmdTribeUpdate());
        Objects.requireNonNull(getCommand("unwax")).setExecutor(new CmdUnwax());

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
        playerlimit = getConfig().getInt("playerlimit");
        Bukkit.getServer().getLogger().info(PREFIX +
                "Player limit override is currently set to " + playerlimit);

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
        try (InputStream input = new FileInputStream("server.properties")) {
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

        // Web whitelist server

        Spark.port(4567); // Choose a port for your API
        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "*");
            response.header("Access-Control-Allow-Headers", "*");
        });
        Spark.post("/process-username", (request, response) -> {
            String username = request.queryParams("username");

            Dreamvisitor.debug("Username from web form: " + username);

            // Process the username (your logic here)
            boolean success = processUsername(username);

            Dreamvisitor.debug("Processed. Success: " + success);

            // Send a response back to the web page
            // response.header("Access-Control-Allow-Origin", "http://0.0.0.0:80/" /* getConfig().getString("website-url") */);
            Dreamvisitor.debug("response.header");
            response.type("application/json");
            Dreamvisitor.debug("response.type");
            return "{\"success\": " + success + "}";
        });

        Runnable pushConsole = new BukkitRunnable() {
            // Push console log to Discord every 2 seconds
            @Override
            public void run() {
                if (Dreamvisitor.getPlugin().getConfig().getBoolean("log-console")) {

                    // If there are messages in the queue, send them!
                    if (ConsoleLogger.messageBuilder != null && ConsoleLogger.messageBuilder.length() > 0) {

                        Bot.gameLogChannel.sendMessage(ConsoleLogger.messageBuilder.toString().replaceAll("_","\\_")).queue();
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

        if (!botFailed) {
            Bukkit.getScheduler().runTaskTimer(this,pushConsole,0,40);
        }

        debug("Enable finished.");

    }

    private boolean processUsername(String username) throws IOException {
        // Connect to Mojang services
        Mojang mojang = new Mojang().connect();
        Dreamvisitor.debug("Connected to Mojang");

        // Check for valid UUID
        Dreamvisitor.debug("Checking for valid UUID");
        if (mojang.getUUIDOfUsername(username) == null) {
            // username does not exist alert
            Dreamvisitor.debug("Username does not exist.");
            Dreamvisitor.debug("Failed whitelist.");
        } else {

            Dreamvisitor.debug("Got UUID");
            UUID uuid = UUID.fromString(mojang.getUUIDOfUsername(username).replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                    "$1-$2-$3-$4-$5"));

            // No account to link

            // Check if already whitelisted
            Dreamvisitor.debug("Is user already whitelisted?");

            if (Whitelist.isUserWhitelisted(uuid)) {
                Dreamvisitor.debug("Already whitelisted.");
                Dreamvisitor.debug("Resolved.");

                return true;
            } else {
                Dreamvisitor.debug("Player is not whitelisted.");

                Whitelist.add(username, uuid);

                // success message
                Dreamvisitor.debug("Success.");

                TextChannel systemChannel = Bot.gameLogChannel.getGuild().getSystemChannel();
                if (systemChannel != null) systemChannel.sendMessage("Whitelisted " + username + " from web whitelist. Use `/unwhitelist <username>` to undo this action or `/toggleweb` to disable web whitelisting.").queue();

                return true;
            }
        }
        return false;
    }

    public static Dreamvisitor getPlugin() {
        return plugin;
    }

    public static String getPlayerPath(Player player) {
        return plugin.getDataFolder().getAbsolutePath() + "/player/" + player.getUniqueId() + ".yml";
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
            Bot.sendMessage(Bot.gameLogChannel, "Server has been shut down.");
            Bot.getJda().shutdownNow();
        }

        logger.removeAppender(appender);
    }

}