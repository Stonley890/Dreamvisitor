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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CmdPauseBypass implements CommandExecutor {

    final Main plugin = Main.getPlugin();
    final String playerList = "players";
    List<String> bypassedPlayers = new ArrayList<>(100);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        // Load pauseBypass.yml
        File file = new File(plugin.getDataFolder().getAbsolutePath() + "/pauseBypass.yml");
        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);

        // If file does not exist, create one
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    sender.sendMessage(Main.PREFIX + ChatColor.RED + "There was a problem accessing the file.");
                    return false;
                }

            } catch (IOException e) {
                sender.sendMessage(Main.PREFIX +
                        ChatColor.RED + "There was a problem accessing the file. Check console for stacktrace.");
                throw new RuntimeException();
            }
        }

        // Load the file
        try {
            fileConfig.load(file);
        } catch (IOException | InvalidConfigurationException e1) {
            sender.sendMessage(Main.PREFIX +
                    ChatColor.RED + "There was a problem accessing the file. Check console for stacktrace.");
            throw new RuntimeException();
        }
        // Get bypassing players
        bypassedPlayers = fileConfig.getStringList(playerList);

        if (args.length == 0) {
            sender.sendMessage(Main.PREFIX + ChatColor.RED + "Missing arguments!");
            return false;
        }

        // Adding a player
        if (args[0].equalsIgnoreCase("add")) {

            if (args.length > 1) {
                // Attempt to modify
                modifyList(true, args[1], sender);
            } else {
                sender.sendMessage(Main.PREFIX + ChatColor.RED + "You must include a player!");
            }


        } // Removing a player
        else if (args[0].equalsIgnoreCase("remove")) {

            if (args.length > 1) {
                // Attempt to modify
                modifyList(false, args[1], sender);
            } else {
                sender.sendMessage(Main.PREFIX + ChatColor.RED + "You must include a player!");
            }


        } else if (args[0].equalsIgnoreCase("list")) {

            // Build list
            StringBuilder list = new StringBuilder();

            for (String players : bypassedPlayers) {
                if (!list.isEmpty()) {
                    list.append(", ");
                }
                list.append(PlayerUtility.getUsernameOfUuid(players));
            }
            sender.sendMessage(Main.PREFIX + ChatColor.WHITE + "Players bypassing: " + list);

        } else {
            sender.sendMessage(Main.PREFIX + ChatColor.RED + "Incorrect arguments!");
            return false;
        }

        // Save changes
        fileConfig.set(playerList, bypassedPlayers);
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

    void modifyList(boolean add, String playerName, CommandSender sender) {

        // Get player from UUID
        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(PlayerUtility.formatUuid(playerName)));

        if (bypassedPlayers.contains(player.getUniqueId().toString())) {
            if (add) {
                sender.sendMessage(Main.PREFIX + ChatColor.RED + "That player is already allowed.");
            } else {
                bypassedPlayers.remove(player.getUniqueId().toString());
                sender.sendMessage(Main.PREFIX + ChatColor.WHITE + playerName + " is no longer bypassing.");
            }
        } else {
            if (add) {
                bypassedPlayers.add(player.getUniqueId().toString());
                sender.sendMessage(Main.PREFIX + ChatColor.WHITE + playerName + " is now bypassing.");
            } else {
                sender.sendMessage(Main.PREFIX + ChatColor.RED + "That player is already not allowed.");
            }
        }

    }
}
