package io.github.stonley890.dreamvisitor.commands;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.AccountLink;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdTribeUpdate implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (args.length == 0) {
            if (sender instanceof BlockCommandSender) {
                BlockCommandSender cmdblock = (BlockCommandSender) sender;

                double lastDistance = 10;
                Player closest = null;

                for (Entity entity : cmdblock.getBlock().getWorld().getNearbyEntities(cmdblock.getBlock().getLocation(), 10, 10, 10)) {
                    if (entity instanceof Player) {
                        double distance = entity.getLocation().distance(cmdblock.getBlock().getLocation());
                        if (distance < lastDistance) {
                            lastDistance = distance;
                            closest = (Player) entity;
                        }
                    }
                }

                if (closest != null) {
                    Bukkit.getLogger().info("Closest: " + closest.getName());

                    String uuid = closest.getUniqueId().toString();
                    Bukkit.getLogger().info("UUID: " + uuid);

                    String discordId = AccountLink.getDiscordId(uuid);
                    Bukkit.getLogger().info("Discord ID: " + discordId);

                    User user = Bot.getJda().retrieveUserById(discordId).complete();
                    Bukkit.getLogger().info("User: " + user.getName());

                } else {
                    sender.sendMessage(Dreamvisitor.prefix + ChatColor.RED + "No player within 10 blocks!");
                }

            } else {
                sender.sendMessage(Dreamvisitor.prefix + ChatColor.BLUE + "Must specify a player! /tribeupdate <player>");
            }
        }

        return true;
    }
}
