package io.github.stonley890.dreamvisitor.commands;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.AccountLink;
import io.github.stonley890.dreamvisitor.google.UserTracker;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.shanerx.mojang.Mojang;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;

public class CmdUser implements CommandExecutor {

    Mojang mojang = new Mojang().connect();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        // Must have at least 1 argument
        if (args.length > 0) {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Searching for " + args[0]);

            // Get UUID
            String uuid = mojang.getUUIDOfUsername(args[0]);


            if /* The UUID could not be found */ (uuid == null) {
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "That user could not be found!");
            } else {

                // Get username (for proper CASE)
                String username = mojang.getPlayerProfile(uuid).getUsername();

                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Player found. Looking for tracker entry.");

                // Try to access spreadsheet
                try {
                    List<List<Object>> seenUuids = UserTracker.getRange("Users!B3:F1000");

                    if (seenUuids == null || seenUuids.isEmpty()) {
                        // Should not happen
                        sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "No data was found on specified spreadsheet.");
                    } else {
                        // For each row
                        for (int i = 0; i < seenUuids.size(); i++) {

                            // Check UUID column for matching UUID
                            if (seenUuids.get(i).get(0).equals(uuid.replaceFirst(
                                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                                    "$1-$2-$3-$4-$5"))) {

                                // Get data
                                String discordUsername = (String) seenUuids.get(i).get(1);
                                String discordID = (String) seenUuids.get(i).get(2);
                                String unbanDate = "N/A";
                                if (!seenUuids.get(i).get(3).equals("")) {
                                    unbanDate = (String) seenUuids.get(i).get(3);
                                }
                                String royaltyPosition = (String) seenUuids.get(i).get(4);

                                // Send data
                                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Data for player " + username + ":" +
                                        "\nUUID: " + uuid.replaceFirst(
                                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                                        "$1-$2-$3-$4-$5") +
                                        "\nDiscord Username: " + discordUsername +
                                        "\nDiscord ID: " + discordID +
                                        "\nDate Unbanned: " + unbanDate +
                                        "\nRoyalty Position: " + royaltyPosition
                                );
                                return true;
                            }
                        }

                        // No match found
                        // Send error and fall back to local sources
                        sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "That player was not found. Checking locally instead.");

                        // Discord ID from AccountLink.yml
                        String discordID = AccountLink.getDiscordId(uuid);
                        // Discord username from JDA
                        String discordUsername = "N/A";
                        if (discordID != null) {
                            discordUsername = Bot.getJda().retrieveUserById(discordID).complete().getName();
                        } else {
                            discordID = "N/A";
                        }
                        // Unban date from Bukkit
                        String unbanDate = "N/A";
                        if (Bukkit.getBannedPlayers().contains(Bukkit.getOfflinePlayer(UUID.fromString(uuid.replaceFirst(
                                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                                "$1-$2-$3-$4-$5"))))) {
                            if (Bukkit.getBanList(BanList.Type.NAME).getBanEntry(username).getExpiration() != null) {
                                unbanDate = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(username).getExpiration().toString();
                            } else {
                                unbanDate = "Infinite";
                            }
                        }

                        sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Local data for player " + username + ":" +
                                "\nUUID: " + uuid.replaceFirst(
                                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                                "$1-$2-$3-$4-$5") +
                                "\nDiscord Username: " + discordUsername +
                                "\nDiscord ID: " + discordID +
                                "\nDate Unbanned: " + unbanDate
                        );
                    }

                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                    // Send error and fall back to local sources
                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "The spreadsheet was unable to be accessed. Checking locally instead.");

                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Local data for player " + username + ":");
                    String discordID = AccountLink.getDiscordId(uuid);
                    Bot.getJda().retrieveUserById(discordID).queue(user -> {sender.sendMessage(ChatColor.BLUE + "\nDiscord Username: " + user.getName());});
                    sender.sendMessage(ChatColor.BLUE + "\nDiscord ID: " + discordID);
                    String unbanDate = "N/A";
                    if (Bukkit.getBannedPlayers().contains(Bukkit.getOfflinePlayer(UUID.fromString(uuid)))) {
                        if (Bukkit.getBanList(BanList.Type.NAME).getBanEntry(username).getExpiration() != null) {
                            unbanDate = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(username).getExpiration().toString();
                        } else {
                            unbanDate = "Infinite";
                        }
                    }
                }

            }
        } else {
            sender.sendMessage(Dreamvisitor.PREFIX +
                    ChatColor.RED + "Missing arguments! /user <username>");
        }

        return true;
    }
}
