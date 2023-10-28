package io.github.stonley890.dreamvisitor.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import org.shanerx.mojang.Mojang;

import io.github.stonley890.dreamvisitor.Dreamvisitor;

public class CmdSoftwhitelist implements CommandExecutor {

    Dreamvisitor plugin = Dreamvisitor.getPlugin();
    String playerList = "players";

    @Override
    @SuppressWarnings({"unchecked"})
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        
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
                file.createNewFile();
            } catch (IOException e) {
                sender.sendMessage(Dreamvisitor.PREFIX +
                        ChatColor.RED + "There was a problem accessing the file. Check console for stacktrace.");
                e.printStackTrace();
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
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "There was a problem accessing the file. Check logs for error.");
            e1.printStackTrace();
        }

        // Get soft-whitelisted players
        whitelistedPlayers = (List<String>) fileConfig.get(playerList);

        try {
            if (args[0].equalsIgnoreCase("add")) {
                // Get player from UUID
                Mojang mojang = new Mojang();
                mojang.connect();
                if (mojang.getUUIDOfUsername(args[1]) != null) {
                    OfflinePlayer player = Bukkit
                            .getOfflinePlayer(UUID.fromString(getCleanUUID(args[1])));
                    // Add
                    assert whitelistedPlayers != null;
                    if (whitelistedPlayers.contains(player.getUniqueId().toString())) {
                        sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "That player is already on the whitelist.");
                    } else {
                        whitelistedPlayers.add(player.getUniqueId().toString());
                        sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Added "
                                + mojang.getPlayerProfile(player.getUniqueId().toString()).getUsername()
                                + " to the whitelist.");
                    }
                } else {
                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + args[1] + " could not be found!");
                }

            } else if (args[0].equalsIgnoreCase("remove")) {
                // Get player from UUID
                Mojang mojang = new Mojang();
                mojang.connect();

                if (mojang.getUUIDOfUsername(args[1]) != null) {
                    OfflinePlayer player = Bukkit
                            .getOfflinePlayer(UUID.fromString(getCleanUUID(args[1])));
                    // Remove
                    assert whitelistedPlayers != null;
                    if (whitelistedPlayers.contains(player.getUniqueId().toString())) {
                        whitelistedPlayers.remove(player.getUniqueId().toString());
                        sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Removed "
                                + mojang.getPlayerProfile(player.getUniqueId().toString()).getUsername()
                                + " from the whitelist.");
                    } else {
                        sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "That player is not on the whitelist.");
                    }
                } else {
                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + args[1] + " could not be found!");
                }

            } else if (args[0].equalsIgnoreCase("list")) {
                Mojang mojang = new Mojang();
                mojang.connect();

                // Build list
                StringBuilder list = new StringBuilder();

                assert whitelistedPlayers != null;
                for (String players : whitelistedPlayers) {
                    if (list.length() > 0) {
                        list.append(", ");
                    }
                    list.append(mojang.getPlayerProfile(players).getUsername());
                }
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Players soft-whitelisted: " + list);

            } else if (args[0].equalsIgnoreCase("on")) {
                // Set config
                plugin.getConfig().set("softwhitelist", true);
                plugin.saveConfig();
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Soft whitelist enabled.");
            } else if (args[0].equalsIgnoreCase("off")) {
                // Set config
                plugin.getConfig().set("softwhitelist", false);
                plugin.saveConfig();
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Soft whitelist disabled.");
            } else {
                sender.sendMessage(Dreamvisitor.PREFIX +
                        ChatColor.RED + "Incorrect arguements! /softwhitelist <add|remove|list|on|off> <player>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(Dreamvisitor.PREFIX +
                    ChatColor.RED + "Missing arguments! /softwhitelist <add|remove|list|on|off> <player>");
        }

        // Save changes
        fileConfig.set(playerList, whitelistedPlayers);
        saveFile(fileConfig, file);
        return true;

    }

    void saveFile(FileConfiguration fileConfig, File file) {
        try {
            fileConfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getCleanUUID(String playerName) {

        Mojang mojang = new Mojang().connect();
        String uuid = mojang.getUUIDOfUsername(playerName);
        return mojang.getUUIDOfUsername(playerName).replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                    "$1-$2-$3-$4-$5");
    }
    
}
