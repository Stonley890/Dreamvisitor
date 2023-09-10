package io.github.stonley890.dreamvisitor.commands;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.AccountLink;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.shanerx.mojang.Mojang;

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

                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Player found. Looking for data.");

                // Discord ID from AccountLink.yml
                String discordID = AccountLink.getDiscordId(uuid);
                // Discord username from JDA
                String discordUsername = "N/A";
                if (discordID != null) {
                    discordUsername = Bot.getJda().retrieveUserById(discordID).complete().getName();
                } else {
                    discordID = "N/A";
                }

                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Local data for player " + username + ":" +
                        "\nUUID: " + uuid.replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                        "$1-$2-$3-$4-$5") +
                        "\nDiscord Username: " + discordUsername +
                        "\nDiscord ID: " + discordID
                );


            }
        } else {
            sender.sendMessage(Dreamvisitor.PREFIX +
                    ChatColor.RED + "Missing arguments! /user <username>");
        }

        return true;
    }
}
