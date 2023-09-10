package io.github.stonley890.dreamvisitor.commands;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;
import io.github.stonley890.dreamvisitor.data.AccountLink;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CmdTribeUpdate implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            if (sender instanceof BlockCommandSender cmdblock) {

                // Find the closest player
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

                    String uuid = closest.getUniqueId().toString();

                    String discordId = AccountLink.getDiscordId(uuid);

                    User user = Bot.getUser(discordId);

                    Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
                    Team playerTeam = scoreboard.getEntryTeam(closest.getName());

                    if (playerTeam != null) {

                        // Iterate through team names to get index
                        for (int i = 0; i < DiscCommandsManager.TRIBE_NAMES.length; i++) {
                            if (playerTeam.getName().equals(DiscCommandsManager.TRIBE_NAMES[i])) {

                                // Remove roles
                                for (String roleId : Dreamvisitor.getPlugin().getConfig().getStringList("tribeRoles")) {
                                    DiscCommandsManager.gameLogChannel.getGuild().removeRoleFromMember(user, Objects.requireNonNull(Bot.getJda().getRoleById(roleId))).queue();
                                }

                                Role targetRole = Bot.getJda().getRoleById(Dreamvisitor.getPlugin().getConfig().getStringList("tribeRoles").get(i));
                                // Role sisterRole = Bot.getJda().getRoleById(Dreamvisitor.getPlugin().getConfig().getStringList("sisterTribeRoles").get(i));

                                if (targetRole == null) {
                                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Could not find role for " + playerTeam.getName());
                                    return true;
                                }

                                /*
                                if (sisterRole == null) {
                                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Could not find sister role for " + playerTeam.getName());
                                    return true;
                                }
                                 */

                                // Add role
                                DiscCommandsManager.gameLogChannel.getGuild().addRoleToMember(user, targetRole).queue();

                                /*
                                Guild sisterGuild = Bot.getJda().getGuildById(Dreamvisitor.getPlugin().getConfig().getInt("tribeGuildID"));
                                if (sisterGuild != null) {
                                    sisterGuild.addRoleToMember(user, sisterRole).queue();
                                }
                                 */


                            }
                        }

                    }

                } else {
                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "No player within 10 blocks!");
                }
            } else {
                sender.sendMessage(Dreamvisitor.PREFIX + "Must specify a player! /tribeupdate <player>");
            }
        } else {
            if (Bukkit.getPlayer(args[0]) != null) {
                Player target = Bukkit.getPlayer(args[0]);

                assert target != null;
                String uuid = target.getUniqueId().toString();

                String discordId = AccountLink.getDiscordId(uuid);

                if (discordId == null) {
                    sender.sendMessage(Dreamvisitor.PREFIX + "No Discord user associated with this player.");
                    return true;
                } else {
                    User user = Bot.getUser(discordId);

                    Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
                    Team playerTeam = scoreboard.getEntryTeam(target.getName());

                    if (playerTeam != null) {

                        // Iterate through team names to get index
                        for (int i = 0; i < DiscCommandsManager.TRIBE_NAMES.length; i++) {
                            if (playerTeam.getName().equals(DiscCommandsManager.TRIBE_NAMES[i])) {

                                // Remove roles
                                for (String roleId : Dreamvisitor.getPlugin().getConfig().getStringList("tribeRoles")) {
                                    DiscCommandsManager.gameLogChannel.getGuild().removeRoleFromMember(user, Objects.requireNonNull(Bot.getJda().getRoleById(roleId))).queue();
                                }

                                Role targetRole = Bot.getJda().getRoleById(Dreamvisitor.getPlugin().getConfig().getStringList("tribeRoles").get(i));

                                if (targetRole == null) {
                                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Could not find role for " + playerTeam.getName());
                                    return true;
                                }

                                // Add role
                                DiscCommandsManager.gameLogChannel.getGuild().addRoleToMember(user, targetRole).queue();

                                sender.sendMessage(Dreamvisitor.PREFIX + "Roles updated successfully.");
                            }
                        }

                    }
                }

            }
        }

        return true;
    }

}
