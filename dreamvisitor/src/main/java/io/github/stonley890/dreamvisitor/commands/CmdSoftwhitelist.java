package io.github.stonley890.dreamvisitor.commands;

import io.github.stonley890.dreamvisitor.Main;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class CmdSoftwhitelist implements CommandExecutor {

    Main plugin = Main.getPlugin();
    String playerList = "players";

    @Override
    @SuppressWarnings({"unchecked"})
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        
        if (args.length == 0) {
            return false;
        }

        // Load softWhitelist.yml
        File file = new File(plugin.getDataFolder().getAbsolutePath() + "/softWhitelist.yml");
        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);

        // Init saved players
        List<String> whitelistedPlayers;

        // If file does not exist, create one
        if (!file.exists()) {
            try {
                boolean fileCreated = file.createNewFile();
                if (!fileCreated) sender.sendMessage(Main.PREFIX +
                        ChatColor.RED + "There was a problem creating the file. Check console for stacktrace.");
            } catch (IOException e) {
                sender.sendMessage(Main.PREFIX +
                        ChatColor.RED + "There was a problem accessing the file. Check console for stacktrace.");
                throw new RuntimeException();
            }
        }

        /*// If file is empty, add a player to initialize
        if (fileConfig.get(playerList) == null) {
            Mojang mojang = new Mojang();
            mojang.connect();

            whitelistedPlayers.add(getCleanUUID("BogTheMudWing"));
            fileConfig.set(playerList, whitelistedPlayers);
            try {
                fileConfig.save(file);
            } catch (IOException e) {
                sender.sendMessage(Dreamvisitor.PREFIX +
                        ChatColor.RED + "There was a problem accessing the file. Check console for stacktrace.");
                e.printStackTrace();
            }
        }*/

        // Load the file
        try {
            fileConfig.load(file);
        } catch (IOException | InvalidConfigurationException e1) {
            sender.sendMessage(Main.PREFIX + ChatColor.RED + "There was a problem accessing the file.");
            throw new RuntimeException();
        }

        // Get soft-whitelisted players
        whitelistedPlayers = (List<String>) fileConfig.get(playerList);

        try {
            if (args[0].equalsIgnoreCase("add")) {
                // Get player from UUID
                if (PlayerUtility.getUUIDOfUsername(args[1]) != null) {
                    OfflinePlayer player = Bukkit
                            .getOfflinePlayer(UUID.fromString(PlayerUtility.formatUuid(args[1])));
                    // Add
                    assert whitelistedPlayers != null;
                    if (whitelistedPlayers.contains(player.getUniqueId().toString())) {
                        sender.sendMessage(Main.PREFIX + ChatColor.RED + "That player is already on the whitelist.");
                    } else {
                        whitelistedPlayers.add(player.getUniqueId().toString());
                        sender.sendMessage(Main.PREFIX + ChatColor.WHITE + "Added "
                                + PlayerUtility.getUsernameOfUuid(player.getUniqueId())
                                + " to the whitelist.");
                    }
                } else {
                    sender.sendMessage(Main.PREFIX + ChatColor.RED + args[1] + " could not be found!");
                }

            } else if (args[0].equalsIgnoreCase("remove")) {
                // Get player from UUID

                if (PlayerUtility.getUUIDOfUsername(args[1]) != null) {
                    OfflinePlayer player = Bukkit
                            .getOfflinePlayer(UUID.fromString(PlayerUtility.formatUuid(args[1])));
                    // Remove
                    assert whitelistedPlayers != null;
                    if (whitelistedPlayers.contains(player.getUniqueId().toString())) {
                        whitelistedPlayers.remove(player.getUniqueId().toString());
                        sender.sendMessage(Main.PREFIX + ChatColor.WHITE + "Removed "
                                + PlayerUtility.getUsernameOfUuid(player.getUniqueId())
                                + " from the whitelist.");
                    } else {
                        sender.sendMessage(Main.PREFIX + ChatColor.RED + "That player is not on the whitelist.");
                    }
                } else {
                    sender.sendMessage(Main.PREFIX + ChatColor.RED + args[1] + " could not be found!");
                }

            } else if (args[0].equalsIgnoreCase("list")) {

                // Build list
                StringBuilder list = new StringBuilder();

                assert whitelistedPlayers != null;
                for (String players : whitelistedPlayers) {
                    if (!list.isEmpty()) {
                        list.append(", ");
                    }
                    list.append(PlayerUtility.getUsernameOfUuid(players));
                }
                sender.sendMessage(Main.PREFIX + ChatColor.WHITE + "Players soft-whitelisted: " + list);

            } else if (args[0].equalsIgnoreCase("on")) {
                // Set config
                plugin.getConfig().set("softwhitelist", true);
                plugin.saveConfig();
                sender.sendMessage(Main.PREFIX + ChatColor.WHITE + "Soft whitelist enabled.");
            } else if (args[0].equalsIgnoreCase("off")) {
                // Set config
                plugin.getConfig().set("softwhitelist", false);
                plugin.saveConfig();
                sender.sendMessage(Main.PREFIX + ChatColor.WHITE + "Soft whitelist disabled.");
            } else {
                sender.sendMessage(Main.PREFIX +
                        ChatColor.RED + "Incorrect arguements! /softwhitelist <add|remove|list|on|off> <player>");
            }

        } catch (Exception e) {
            sender.sendMessage(Main.PREFIX +
                    ChatColor.RED + "Missing arguments! /softwhitelist <add|remove|list|on|off> <player>");
        }

        // Save changes
        fileConfig.set(playerList, whitelistedPlayers);
        saveFile(fileConfig, file);
        return true;

    }

    void saveFile(@NotNull FileConfiguration fileConfig, File file) {
        try {
            fileConfig.save(file);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

}
