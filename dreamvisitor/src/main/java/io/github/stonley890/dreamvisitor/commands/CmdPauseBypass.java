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

public class CmdPauseBypass implements CommandExecutor {

    Dreamvisitor plugin = Dreamvisitor.getPlugin();
    String playerList = "players";
    List<String> bypassedPlayers = new ArrayList<>(100);

    @Override
    @SuppressWarnings({ "unchecked" })
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        // Load pauseBypass.yml
        File file = new File(plugin.getDataFolder().getAbsolutePath() + "/pauseBypass.yml");
        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);

        // If file does not exist, create one
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    sender.sendMessage(ChatColor.RED + "There was a problem accessing the file.");
                }

            } catch (IOException e) {
                sender.sendMessage(
                        ChatColor.RED + "There was a problem accessing the file. Check console for stacktrace.");
                e.printStackTrace();
            }
        }

        // If file is empty, add a player to initialize
        if (fileConfig.get(playerList) == null) {

            bypassedPlayers.add(getCleanUUID("BogTheMudWing"));
            fileConfig.set(playerList, bypassedPlayers);
            saveFile(fileConfig, file);
        }

        // Load the file
        try {
            fileConfig.load(file);
        } catch (IOException | InvalidConfigurationException e1) {
            sender.sendMessage(
                    ChatColor.RED + "There was a problem accessing the file. Check console for stacktrace.");
            e1.printStackTrace();
        }
        // Get bypassing players
        bypassedPlayers = (List<String>) fileConfig.get(playerList);

        if (args.length == 0) {
            return false;
        }

        // Adding a player
        if (args[0].equalsIgnoreCase("add")) {

            // Attempt to modify
            modifyList(true, args[1], sender);

        } // Removing a player
        else if (args[0].equalsIgnoreCase("remove")) {
            
            // Attempt to modify
            modifyList(false, args[1], sender);

        } else if (args[0].equalsIgnoreCase("list")) {

            Mojang mojang = new Mojang().connect();

            // Build list
            StringBuilder list = new StringBuilder();

            for (String players : bypassedPlayers) {
                if (list.length() > 0) {
                    list.append(", ");
                }
                list.append(mojang.getPlayerProfile(players).getUsername());
            }
            sender.sendMessage(ChatColor.GOLD + "Players bypassing: " + list.toString());

        } else {
            return false;
        }

        // Save changes
        fileConfig.set(playerList, bypassedPlayers);
        saveFile(fileConfig, file);
        return true;
    }

    String getCleanUUID(String playerName) {

        Mojang mojang = new Mojang().connect();
        return mojang.getUUIDOfUsername(playerName).replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5");
    }

    void saveFile(FileConfiguration fileConfig, File file) {
        try {
            fileConfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void modifyList(boolean add, String playerName, CommandSender sender) {

        // Get player from UUID
        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(getCleanUUID(playerName)));

        if (bypassedPlayers.contains(player.getUniqueId().toString())) {
            if (add) {
                sender.sendMessage(ChatColor.RED + "That player is already allowed.");
            } else {
                bypassedPlayers.remove(player.getUniqueId().toString());
                sender.sendMessage(ChatColor.GOLD + playerName + " is no longer bypassing.");
            }
        } else {
            if (add) {
                bypassedPlayers.add(player.getUniqueId().toString());
                sender.sendMessage(ChatColor.GOLD + playerName + " is now bypassing.");
            } else {
                sender.sendMessage(ChatColor.RED + "That player is already not allowed.");
            }
        }

    }
}
