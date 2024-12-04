package io.github.stonley890.dreamvisitor;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandTree;
import io.github.stonley890.dreamvisitor.commands.*;
import io.github.stonley890.dreamvisitor.comms.DataSender;
import io.github.stonley890.dreamvisitor.comms.RequestHandler;
import io.github.stonley890.dreamvisitor.data.*;
import io.github.stonley890.dreamvisitor.commands.CmdChatback;
import io.github.stonley890.dreamvisitor.functions.*;
import io.github.stonley890.dreamvisitor.listeners.*;
import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.misc.DoubleAverageInfo;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import me.lucko.spark.api.statistic.types.GenericStatistic;
import net.luckperms.api.LuckPerms;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
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
    public static LuckPerms luckperms;
    public static String MOTD = null;
    public static boolean chatPaused;
    public static int playerLimit;
    public static Location hubLocation;
    public static boolean debugMode;
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
            Dreamvisitor.getPlugin().getLogger().info("DEBUG: " + message);
        }
    }

    @NotNull
    public static LuckPerms getLuckPerms() throws NullPointerException {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        LuckPerms luckPerms;
        if (provider != null) luckPerms = provider.getProvider();
        else {
            throw new NullPointerException("LuckPerms cannot be found.");
        }
        return luckPerms;
    }

    @NotNull
    public static Spark getSpark() throws NullPointerException {
        return SparkProvider.get();
    }

    /**
     * Escapes all Discord markdown elements in a {@link String}.
     * @param string the {@link String} to format.
     * @return the formatted {@link String}.
     */
    public static @NotNull String escapeMarkdownFormatting(@NotNull String string) {
        return string.isEmpty() ? string : string.replaceAll("_","\\\\_").replaceAll("\\*","\\\\*").replaceAll("\\|","\\\\|");
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

            List<DVCommand> commands = new ArrayList<>();
            commands.add(new CmdAdminRadio());
            commands.add(new CmdDiscord());
            commands.add(new CmdHub());
            commands.add(new CmdPanic());
            commands.add(new CmdPauseBypass());
            commands.add(new CmdPausechat());
            commands.add(new CmdPlayerlimit());
            commands.add(new CmdRadio());
            commands.add(new CmdSethub());
            commands.add(new CmdSoftwhitelist());
            commands.add(new CmdTagRadio());
            commands.add(new CmdTogglepvp());
            commands.add(new CmdZoop());
            commands.add(new CmdItemBanList());
            commands.add(new CmdTribeUpdate());
            commands.add(new CmdUnwax());
            commands.add(new CmdScheduleRestart());
            commands.add(new CmdInvSwap());
            commands.add(new CmdDvset());
            commands.add(new CmdSetmotd());
            commands.add(new CmdSynctime());
            commands.add(new CmdSandbox());
            commands.add(new CmdMoonglobe());
            commands.add(new CmdSetback());
            commands.add(new CmdParcel());
            commands.add(new CmdDreamvisitor());
            commands.add(new CmdChatback());

            debug("Initializing commands...");
            CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(!debugMode));
            CommandAPI.onEnable();
            registerCommands(commands);

            debug("Creating data folder...");
            // Create config if needed
            boolean directoryCreated = getDataFolder().mkdir();
            if (!directoryCreated) debug("Dreamvisitor did not create a data folder. It may already exist.");
            saveDefaultConfig();

            // Init mail
            debug("Initializing mail.yml");
            Mail.init();

            // Start message
            getLogger().log(Level.INFO, "Dreamvisitor: A plugin created by Bog for WOFTNW to add various features.");

            // If chat was previously paused, restore and notify in console\
            debug("Restoring chat pause...");
            if (getConfig().getBoolean("chatPaused")) {
                chatPaused = true;
                getLogger().info(PREFIX +
                        "Chat is currently paused from last session! Use /pausechat to allow users to chat.");
            }

            // Restore player limit override
            debug("Restoring player limit override...");
            playerLimit = getConfig().getInt("playerlimit");
            getLogger().info(PREFIX +
                    "Player limit override is currently set to " + playerLimit);
            DataSender.sendMaxPlayerCount();

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

            try {
                new RequestHandler();  // Start the embedded HTTP server
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Send resource pack info to web app
            try {
                DataSender.sendResourcePack();
            } catch (IOException e) {
                getLogger().warning("Could not check for resource pack update.");
            }

            Runnable scheduledRestarts = new BukkitRunnable() {
                @Override
                public void run() {
                    // Restart if requested and no players are online
                    if (ScheduleRestart.isRestartScheduled() && Bukkit.getOnlinePlayers().isEmpty()) {
                        getLogger().info(PREFIX + "Restarting the server as scheduled.");
                        getServer().spigot().restart();
                    }

                    // also check if memory usage is high and schedule restart
                    long maxMemory = Runtime.getRuntime().maxMemory();
                    long freeMemory = Runtime.getRuntime().freeMemory();
                    double freeMemoryPercent = ((double) freeMemory / maxMemory) * 100;
                    if (freeMemoryPercent <= 10) {
                        ScheduleRestart.setRestartScheduled(true);
                        getLogger().info("Dreamvisitor scheduled a restart because free memory usage is at or less than 10%.");
                    }
                }
            };

            Runnable tick = new BukkitRunnable() {
                @Override
                public void run() {
                    Moonglobe.tick();
                }
            };

            Runnable checkBannedItems = new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!player.isOp() && ItemBanList.badItems != null) {

                            for (ItemStack item : ItemBanList.badItems) {
                                if (item == null) continue;
                                for (ItemStack content : player.getInventory().getContents()) {
                                    if (content == null || !content.isSimilar(item)) continue;
                                    player.getInventory().remove(item);
                                }
                            }
                        }
                    }
                }
            };

            Runnable reportStats = new BukkitRunnable() {
                @Override
                public void run() {
                    Spark spark = getSpark();
                    // Get the TPS statistic
                    DoubleStatistic<StatisticWindow.TicksPerSecond> tps = spark.tps();
                    assert tps != null;
                    DataSender.sendPost(DataSender.STATS, new JSONObject().put("tps", tps.poll(StatisticWindow.TicksPerSecond.SECONDS_5)));

                    // Get the MSPT statistic
                    GenericStatistic<DoubleAverageInfo, StatisticWindow.MillisPerTick> mspt = spark.mspt();
                    assert mspt != null;
                    DataSender.sendPost(DataSender.STATS, new JSONObject().put("mspt", mspt.poll(StatisticWindow.MillisPerTick.MINUTES_1).median()));
                }
            };

            Bukkit.getScheduler().runTaskTimer(this, tick, 0, 0);

            // Check for scheduled restart every minute
            Bukkit.getScheduler().runTaskTimer(this, scheduledRestarts, 200, 1200);

            // Check for banned items every ten seconds
            Bukkit.getScheduler().runTaskTimer(this, checkBannedItems, 40, 20*10);

            // Report stats every second
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, reportStats, 20, 20);

            debug("Enable finished.");
        } catch (Exception e) {

            getLogger().severe("Dreamvisitor was unable to start :(\nPlease notify Bog with the following stack trace:");
            e.printStackTrace();

            Bukkit.getPluginManager().disablePlugin(this);
            throw new RuntimeException();

        }
    }

    private void checkConfig() {
        if (getConfig().getInt("playerlimit") < -1) getConfig().set("playerlimit", -1);
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
        getServer().getPluginManager().registerEvents(new ListenTimeSkip(), this);
        getServer().getPluginManager().registerEvents(new ListenSignChangeEvent(), this);
    }

    private void registerCommands(@NotNull List<DVCommand> commands) throws NullPointerException {
        for (DVCommand command : commands) {
            if (command.getCommand() instanceof CommandAPICommand apiCommand) {
                apiCommand.register(this);
            } else if (command.getCommand() instanceof CommandTree apiCommand) {
                apiCommand.register(this);
            }
        }
    }

    @Override
    public void onDisable() {

        // remove moon globes
        for (Moonglobe moonglobe : Moonglobe.activeMoonglobes) moonglobe.remove(null);

        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                PlayerUtility.savePlayerMemory(player.getUniqueId());
                PlayerUtility.clearPlayerMemory(player.getUniqueId());
            } catch (IOException e) {
                getLogger().severe("Unable to save player memory! Does the server have write access?");
                if (Dreamvisitor.debugMode) throw new RuntimeException();
            }
        }

        CommandAPI.onDisable();

        logger.removeAppender(appender);
    }

}