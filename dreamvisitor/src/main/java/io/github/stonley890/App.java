package io.github.stonley890;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.security.auth.login.LoginException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;
import org.shanerx.mojang.Mojang;

import io.github.stonley890.commands.CommandsManager;
import io.github.stonley890.data.PlayerMemory;
import io.github.stonley890.data.PlayerUtility;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class App extends JavaPlugin implements Listener {

    private static App plugin;
    private static boolean chatPaused;
    private static boolean botFailed = false;
    private static int playerlimit;

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
            Bukkit.getLogger().warning("ERROR: Unable to await bot ready.");
            e.printStackTrace();
        }

        // Send server start message in log channel
        Bot.getJDA().getGuilds()
                .forEach((Guild guild) -> guild.getSystemChannel().sendMessage("Server has been started.").queue());

        // Get saved data
        CommandsManager.initChannelsRoles();

        // If chat was previously paused, restore and notify in console
        if (getConfig().getBoolean("chatPaused")) {
            chatPaused = true;
            Bukkit.getServer().getLogger()
                    .info("[Dreamvisitor] Chat is currently paused from last session! Use /pausechat to allow users to chat.");
        }

        // Restore player limit override
        playerlimit = getConfig().getInt("playerlimit");
    }

    public static App getPlugin() {
        return plugin;
    }

    public static String getPlayerPath(Player player) {
        return plugin.getDataFolder().getAbsolutePath() + "/player/" + player.getUniqueId() + ".yml";
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String cmd = event.getMessage();
        Player ply = event.getPlayer();
        if (event.getMessage().startsWith("/me")) {

            if (chatPaused) {
                File file = new File(getDataFolder().getAbsolutePath() + "/pauseBypass.yml");
                FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                List<String> bypassedPlayers = new ArrayList<>(100);

                try {
                    fileConfig.load(file);
                } catch (IOException | InvalidConfigurationException e1) {
                    e1.printStackTrace();
                }

                bypassedPlayers = (List<String>) fileConfig.get("players");

                if (bypassedPlayers.contains(event.getPlayer().getUniqueId().toString())
                        || event.getPlayer().isOp()) {
                    TextChannel chatChannel = Bot.getJDA().getTextChannelById(CommandsManager.getChatChannel());
                    String action = cmd.replaceFirst("/me ", "");
                    chatChannel.sendMessage("**[" + ChatColor.stripColor(ply.getDisplayName()) + " **(" + ply.getName()
                            + ")**]** " + ChatColor.stripColor(action)).queue();
                } else {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "Chat is currently paused.");
                }
            }
        }
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
                    chatChannel.sendMessage("**Chat has been unpaused. Messages will now be sent to Minecraft**")
                            .queue();
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

        } else if (label.equalsIgnoreCase("aradio")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length > 0) {
                    String message = "";
                    for (int i = 0; i != args.length; i++) {
                        message += args[i] + " ";
                    }
                    Bukkit.getLogger().info(ChatColor.DARK_AQUA + "[Admin Radio] " + ChatColor.YELLOW + "<"
                            + player.getName() + "> " + ChatColor.WHITE + message);
                    for (Player operator : Bukkit.getServer().getOnlinePlayers()) {
                        if (operator.isOp()) {
                            operator.sendMessage(ChatColor.DARK_AQUA + "[Admin] " + ChatColor.YELLOW + "<"
                                    + player.getName() + "> " + ChatColor.WHITE + message);
                        }
                    }
                }
            } else if (sender instanceof ConsoleCommandSender) {
                if (args.length > 0) {
                    String message = "";
                    for (int i = 0; i != args.length; i++) {
                        message += args[i] + " ";
                    }
                    Bukkit.getLogger().info(ChatColor.DARK_AQUA + "[Admin Radio] " + ChatColor.YELLOW + "<"
                            + ChatColor.RED + "Console" + ChatColor.YELLOW + "> " + ChatColor.WHITE + message);
                    for (Player operator : Bukkit.getServer().getOnlinePlayers()) {
                        if (operator.isOp()) {
                            operator.sendMessage(ChatColor.DARK_AQUA + "[Admin] " + ChatColor.YELLOW + "<"
                                    + ChatColor.RED + "Console" + ChatColor.YELLOW + ">" + ChatColor.WHITE + message);
                        }
                    }
                }
            }
        } else if (label.equalsIgnoreCase("radio")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length > 0 && player.getScoreboardTags().contains("Staff")) {
                    String message = "";
                    for (int i = 0; i != args.length; i++) {
                        message += args[i] + " ";
                    }
                    Bukkit.getLogger().info(ChatColor.DARK_AQUA + "[Staff Radio] " + ChatColor.YELLOW + "<"
                            + player.getName() + "> " + ChatColor.WHITE + message);
                    for (Player staff : Bukkit.getServer().getOnlinePlayers()) {
                        if (staff.getScoreboardTags().contains("Staff")) {
                            staff.sendMessage(ChatColor.DARK_AQUA + "[Staff] " + ChatColor.YELLOW + "<"
                                    + player.getName() + "> " + ChatColor.WHITE + message);
                        }
                    }
                }
            } else if (sender instanceof ConsoleCommandSender) {
                if (args.length > 0) {
                    String message = "";
                    for (int i = 0; i != args.length; i++) {
                        message += args[i] + " ";
                    }
                    Bukkit.getLogger().info(ChatColor.DARK_AQUA + "[Staff Radio] " + ChatColor.YELLOW + "<"
                            + ChatColor.YELLOW + "Console> " + message);
                    for (Player staff : Bukkit.getServer().getOnlinePlayers()) {
                        if (staff.getScoreboardTags().contains("Staff")) {
                            staff.sendMessage(ChatColor.DARK_AQUA + "[Staff] " + ChatColor.YELLOW + "<" + ChatColor.RED
                                    + "Console" + ChatColor.YELLOW + "> " + ChatColor.WHITE + message);
                        }
                    }
                }
            }
        } else if (label.equalsIgnoreCase("discord")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                PlayerMemory memory = new PlayerMemory();
                try {
                    // Init file config
                    File file = new File(getPlayerPath(player));
                    FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                    memory.setDiscordToggled(fileConfig.getBoolean("discordToggled"));

                    // Change data
                    if (memory.isDiscordToggled()) {
                        memory.setDiscordToggled(false);
                    } else {
                        memory.setDiscordToggled(true);
                    }

                    // Save data
                    fileConfig.set("discordToggled", memory.isDiscordToggled());
                    fileConfig.save(file);

                    player.sendMessage(
                            ChatColor.DARK_AQUA + "Discord visibility toggled to " + memory.isDiscordToggled() + ".");
                } catch (Exception e) {
                    Bukkit.getLogger().warning("ERROR: Unable to access player memory!");
                    player.sendMessage(
                            ChatColor.RED + "There was a problem accessing player memory. Check logs for stacktrace.");
                    e.printStackTrace();
                }

            }
        } else if (label.equalsIgnoreCase("zoop")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                PlayerMemory memory = new PlayerMemory();
                try {
                    // Init file config
                    File file = new File(getPlayerPath(player));
                    FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                    memory.setVanished(fileConfig.getBoolean("vanished"));

                    // Change data
                    if (memory.isVanished()) {
                        memory.setVanished(false);
                        String chatMessage = "**" + player.getName() + " joined the game**";
                        String channelId = CommandsManager.getChatChannel();
                        if (channelId != "none") {
                            io.github.stonley890.Bot.getJDA().getTextChannelById(channelId).sendMessage(chatMessage)
                                    .queue();
                        }
                    } else {
                        memory.setVanished(true);
                        String chatMessage = "**" + player.getName() + " left the game**";
                        String channelId = CommandsManager.getChatChannel();
                        if (channelId != "none") {
                            io.github.stonley890.Bot.getJDA().getTextChannelById(channelId).sendMessage(chatMessage)
                                    .queue();
                        }
                    }

                    // Save data
                    fileConfig.set("vanished", memory.isVanished());
                    fileConfig.save(file);

                    player.sendMessage("\u00a79Discord vanished toggled to " + memory.isVanished() + ".");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } else if (label.equalsIgnoreCase("softwhitelist")) {

            // Load softWhitelist.yml
            File file = new File(getDataFolder().getAbsolutePath() + "/softWhitelist.yml");
            FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);

            // Init saved players
            List<String> whitelistedPlayers = new ArrayList<>(100);

            // If file does not exist, create one
            if (file.exists() == false) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.RED + "There was a problem accessing the file. Check logs for error.");
                    e.printStackTrace();
                }
            }

            // If file is empty, add a player to initialize
            if (fileConfig.get("players") == null) {
                Mojang mojang = new Mojang();
                mojang.connect();

                whitelistedPlayers.add(mojang.getUUIDOfUsername("BogTheMudWing").replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                        "$1-$2-$3-$4-$5"));
                fileConfig.set("players", whitelistedPlayers);
                try {
                    fileConfig.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Load the file
            try {
                fileConfig.load(file);
            } catch (IOException | InvalidConfigurationException e1) {
                sender.sendMessage(ChatColor.RED + "There was a problem accessing the file. Check logs for error.");
                e1.printStackTrace();
            }
            // Get soft-whitelisted players
            whitelistedPlayers = (List<String>) fileConfig.get("players");

            try {
                if (args[0].equalsIgnoreCase("add")) {
                    // Get player from UUID
                    Mojang mojang = new Mojang();
                    mojang.connect();

                    OfflinePlayer player = Bukkit
                            .getOfflinePlayer(UUID.fromString(mojang.getUUIDOfUsername(args[1]).replaceFirst(
                                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                                    "$1-$2-$3-$4-$5")));
                    // Add
                    if (whitelistedPlayers.contains(player.getUniqueId().toString())) {
                        sender.sendMessage(ChatColor.RED + "That player is already whitelisted.");
                    } else {
                        whitelistedPlayers.add(player.getUniqueId().toString());
                        sender.sendMessage(ChatColor.GOLD + "Added "
                                + mojang.getPlayerProfile(player.getUniqueId().toString()).getUsername()
                                + " to the whitelist.");
                    }
                } else if (args[0].equalsIgnoreCase("remove")) {
                    // Get player from UUID
                    Mojang mojang = new Mojang();
                    mojang.connect();

                    OfflinePlayer player = Bukkit
                            .getOfflinePlayer(UUID.fromString(mojang.getUUIDOfUsername(args[1]).replaceFirst(
                                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                                    "$1-$2-$3-$4-$5")));
                    // Remove
                    if (whitelistedPlayers.contains(player.getUniqueId().toString())) {
                        whitelistedPlayers.remove(player.getUniqueId().toString());
                        sender.sendMessage(ChatColor.GOLD + "Removed "
                                + mojang.getPlayerProfile(player.getUniqueId().toString()).getUsername()
                                + " from the whitelist.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "That player is not whitelisted.");
                    }
                } else if (args[0].equalsIgnoreCase("list")) {
                    Mojang mojang = new Mojang();
                    mojang.connect();

                    // Build list
                    StringBuilder list = new StringBuilder();
                    ;
                    for (String players : whitelistedPlayers) {
                        if (list.length() > 0) {
                            list.append(", ");
                        }
                        list.append(mojang.getPlayerProfile(players).getUsername());
                    }
                    sender.sendMessage(ChatColor.GOLD + "Players soft-whitelisted: " + list.toString());

                } else if (args[0].equalsIgnoreCase("on")) {
                    // Set config
                    getConfig().set("softwhitelist", true);
                    saveConfig();
                    sender.sendMessage(ChatColor.GOLD + "Soft whitelist enabled.");
                } else if (args[0].equalsIgnoreCase("off")) {
                    // Set config
                    getConfig().set("softwhitelist", false);
                    saveConfig();
                    sender.sendMessage(ChatColor.GOLD + "Soft whitelist disabled.");
                } else {
                    sender.sendMessage(
                            ChatColor.RED + "Incorrect arguements! /softwhitelist <add|remove|list|on|off> <player>");

                }

                // Save changes
                fileConfig.set("players", whitelistedPlayers);
                try {
                    fileConfig.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(
                        ChatColor.RED + "Missing arguments! /softwhitelist <add|remove|list|on|off> <player>");
            }
        } else if (label.equalsIgnoreCase("playerlimit")) {
            try {
                int result = Integer.parseInt(args[0]);
                getConfig().set("playerlimit", result);
                saveConfig();
                if (args[0].equals("-1")) {
                    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                        if (player.isOp()) {
                            player.sendMessage(ChatColor.GOLD + "Player limit override disabled");
                        }
                    }
                } else if (result > -1) {
                    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                        if (player.isOp()) {
                            player.sendMessage(ChatColor.GOLD + "Player limit override set to " + args[0]);
                        }
                    }
                } else {
                    sender.sendMessage(
                            ChatColor.RED + "Missing arguments! /playerlimit <number of players (set -1 to disable)>");
                }

            } catch (NumberFormatException e) {
                sender.sendMessage(
                        ChatColor.RED + "Missing arguments! /playerlimit <number of players (set -1 to disable)>");
            }
        } else if (label.equalsIgnoreCase("togglepvp")) {
            if (getConfig().getBoolean("disablepvp")) {
                getConfig().set("disablepvp", false);
                Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "PvP globally enabled.");
            } else {
                getConfig().set("disablepvp", true);
                Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "PvP globally disabled.");
            }
            saveConfig();
        } else if (label.equalsIgnoreCase("pausebypass")) {

            // Load pauseBypass.yml
            File file = new File(getDataFolder().getAbsolutePath() + "/pauseBypass.yml");
            FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);

            // Init saved players
            List<String> bypassedPlayers = new ArrayList<>(100);

            // If file does not exist, create one
            if (file.exists() == false) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.RED + "There was a problem accessing the file. Check logs for error.");
                    e.printStackTrace();
                }
            }

            // If file is empty, add a player to initialize
            if (fileConfig.get("players") == null) {
                Mojang mojang = new Mojang();
                mojang.connect();

                bypassedPlayers.add(mojang.getUUIDOfUsername("BogTheMudWing").replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                        "$1-$2-$3-$4-$5"));
                fileConfig.set("players", bypassedPlayers);
                try {
                    fileConfig.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Load the file
            try {
                fileConfig.load(file);
            } catch (IOException | InvalidConfigurationException e1) {
                sender.sendMessage(ChatColor.RED + "There was a problem accessing the file. Check logs for error.");
                e1.printStackTrace();
            }
            // Get soft-whitelisted players
            bypassedPlayers = (List<String>) fileConfig.get("players");

            try {
                if (args[0].equalsIgnoreCase("add")) {
                    // Get player from UUID
                    Mojang mojang = new Mojang();
                    mojang.connect();

                    OfflinePlayer player = Bukkit
                            .getOfflinePlayer(UUID.fromString(mojang.getUUIDOfUsername(args[1]).replaceFirst(
                                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                                    "$1-$2-$3-$4-$5")));
                    // Add
                    if (bypassedPlayers.contains(player.getUniqueId().toString())) {
                        sender.sendMessage(ChatColor.RED + "That player is already allowed.");
                    } else {
                        bypassedPlayers.add(player.getUniqueId().toString());
                        sender.sendMessage(ChatColor.GOLD
                                + mojang.getPlayerProfile(player.getUniqueId().toString()).getUsername()
                                + " is now bypassing.");
                    }
                } else if (args[0].equalsIgnoreCase("remove")) {
                    // Get player from UUID
                    Mojang mojang = new Mojang();
                    mojang.connect();

                    OfflinePlayer player = Bukkit
                            .getOfflinePlayer(UUID.fromString(mojang.getUUIDOfUsername(args[1]).replaceFirst(
                                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                                    "$1-$2-$3-$4-$5")));
                    // Remove
                    if (bypassedPlayers.contains(player.getUniqueId().toString())) {
                        bypassedPlayers.remove(player.getUniqueId().toString());
                        sender.sendMessage(ChatColor.GOLD
                                + mojang.getPlayerProfile(player.getUniqueId().toString()).getUsername()
                                + " is no longer bypassing.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "That player is not allowed.");
                    }
                } else if (args[0].equalsIgnoreCase("list")) {
                    Mojang mojang = new Mojang();
                    mojang.connect();

                    // Build list
                    StringBuilder list = new StringBuilder();
                    ;
                    for (String players : bypassedPlayers) {
                        if (list.length() > 0) {
                            list.append(", ");
                        }
                        list.append(mojang.getPlayerProfile(players).getUsername());
                    }
                    sender.sendMessage(ChatColor.GOLD + "Players bypassing: " + list.toString());

                } else {
                    sender.sendMessage(
                            ChatColor.RED + "Incorrect arguements! /pausebypass <add|remove|list> <player>");

                }

                // Save changes
                fileConfig.set("players", bypassedPlayers);
                try {
                    fileConfig.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(
                        ChatColor.RED + "Missing arguments! /pausebypass <add|remove|list> <player>");
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        // Send chat messages to Discord
        // IF chat is not paused AND the player is not an operator OR the player is an
        // operator, send message
        if (chatPaused == true) {
            File file = new File(getDataFolder().getAbsolutePath() + "/pauseBypass.yml");
            FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
            List<String> bypassedPlayers = new ArrayList<>(100);

            try {
                fileConfig.load(file);
            } catch (IOException | InvalidConfigurationException e1) {
                e1.printStackTrace();
            }

            bypassedPlayers = (List<String>) fileConfig.get("players");

            // If player is on soft whitelist or is op, allow. If not, kick player.
            if (bypassedPlayers.contains(event.getPlayer().getUniqueId().toString())
                    || event.getPlayer().isOp()) {
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
    }

    @EventHandler
    public void onPlayerLoginEvent(PlayerLoginEvent event) {
        if (getConfig().getInt("playerlimit") != -1) {
            if (Bukkit.getOnlinePlayers().size() >= getConfig().getInt("playerlimit")) {
                event.disallow(Result.KICK_FULL, "Server full!");
            } else {
                if (getConfig().getBoolean("softwhitelist")) {
                    File file = new File(getDataFolder().getAbsolutePath() + "/softWhitelist.yml");
                    FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                    List<String> whitelistedPlayers = new ArrayList<>(100);

                    try {
                        fileConfig.load(file);
                    } catch (IOException | InvalidConfigurationException e1) {
                        e1.printStackTrace();
                    }

                    whitelistedPlayers = (List<String>) fileConfig.get("players");

                    // If player is on soft whitelist or is op, allow. If not, kick player.
                    if (whitelistedPlayers.contains(event.getPlayer().getUniqueId().toString())
                            || event.getPlayer().isOp()) {
                        if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
                            event.allow();
                        }
                        // Send player joins to Discord
                        String chatMessage = "**" + event.getPlayer().getName() + " joined the game**";
                        String channelId = CommandsManager.getChatChannel();
                        if (channelId != "none") {
                            io.github.stonley890.Bot.getJDA().getTextChannelById(channelId).sendMessage(chatMessage)
                                    .queue();
                        }
                        // Remind bot login failure to ops
                        if (botFailed && event.getPlayer().isOp()) {
                            event.getPlayer().sendMessage(
                                    "\u00a71[Dreamvisitor] \u00a7aBot login failed on server start! You may need a new login token.");
                        }
                    } else {
                        event.disallow(Result.KICK_OTHER, "You are not allowed at this time.");
                    }

                } else {
                    event.allow();
                }

            }
        } else {
            if (getConfig().getBoolean("softwhitelist")) {
                File file = new File(getDataFolder().getAbsolutePath() + "/softWhitelist.yml");
                FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                List<String> whitelistedPlayers = new ArrayList<>(100);

                try {
                    fileConfig.load(file);
                } catch (IOException | InvalidConfigurationException e1) {
                    e1.printStackTrace();
                }

                whitelistedPlayers = (List<String>) fileConfig.get("players");

                // If player is on soft whitelist or is op, allow. If not, kick player.
                if (whitelistedPlayers.contains(event.getPlayer().getUniqueId().toString())
                        || event.getPlayer().isOp()) {
                    // Send player joins to Discord
                    String chatMessage = "**" + event.getPlayer().getName() + " joined the game**";
                    String channelId = CommandsManager.getChatChannel();
                    if (channelId != "none") {
                        io.github.stonley890.Bot.getJDA().getTextChannelById(channelId).sendMessage(chatMessage)
                                .queue();
                    }
                    // Remind bot login failure to ops
                    if (botFailed && event.getPlayer().isOp()) {
                        event.getPlayer().sendMessage(
                                "\u00a71[Dreamvisitor] \u00a7aBot login failed on server start! You may need a new login token.");
                    }
                } else {
                    event.disallow(Result.KICK_FULL, "You are not allowed at this time.");
                }

            }
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

    @EventHandler
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        if (getConfig().getBoolean("disablepvp")) {
            if (event.getDamager().getType() == EntityType.PLAYER && event.getEntity().getType() == EntityType.PLAYER) {
                event.setCancelled(true);
            }
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