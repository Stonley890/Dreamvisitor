package io.github.stonley890.dreamvisitor.commands;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.AccountLink;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CmdUser implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        // user <name>

        // Must have at least 1 argument
        if (args.length > 0) {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Searching for " + args[0]);

            // Get UUID
            UUID uuid = PlayerUtility.getUUIDOfUsername(args[0]);

            if /* The UUID could not be found */ (uuid == null) {
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "That user could not be found!");
            } else {

                // Get username (for proper CASE)
                String username = PlayerUtility.getUsernameOfUuid(uuid);

                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Player found. Looking for data.");

                String discordID;
                String discordUsername = "N/A";
                long discord;

                // Discord ID from AccountLink.yml
                try {
                    discord = AccountLink.getDiscordId(uuid);
                    discordID = String.valueOf(discord);
                    discordUsername = Bot.getJda().retrieveUserById(discord).complete().getName();
                } catch (NullPointerException e) {
                    discordID = "N/A";
                    // Discord username from JDA

                }

                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Local data for player " + username + ":" +
                        "\nUUID: " + uuid +
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
