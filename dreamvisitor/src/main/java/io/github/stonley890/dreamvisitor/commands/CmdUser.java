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

import java.util.UUID;

public class CmdUser implements CommandExecutor {

    Mojang mojang = new Mojang().connect();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        // Must have at least 1 argument
        if (args.length > 0) {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Searching for " + args[0]);

            // Get UUID
            String uuidString = mojang.getUUIDOfUsername(args[0]);

            if /* The UUID could not be found */ (uuidString == null) {
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "That user could not be found!");
            } else {

                UUID uuid = UUID.fromString(uuidString);

                // Get username (for proper CASE)
                String username = mojang.getPlayerProfile(uuid.toString()).getUsername();

                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Player found. Looking for data.");

                String discordID;
                String discordUsername = "N/A";

                // Discord ID from AccountLink.yml
                try {
                    long discord = AccountLink.getDiscordId(uuid);
                    discordID = String.valueOf(discord);
                } catch (NullPointerException e) {
                    discordID = "N/A";
                    // Discord username from JDA
                    discordUsername = Bot.getJda().retrieveUserById(discordID).complete().getName();
                }

                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Local data for player " + username + ":" +
                        "\nUUID: " + uuid.toString() +
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
