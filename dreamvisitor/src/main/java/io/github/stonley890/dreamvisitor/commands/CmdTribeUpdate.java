package io.github.stonley890.dreamvisitor.commands;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;
import io.github.stonley890.dreamvisitor.data.AccountLink;
import io.github.stonley890.dreamvisitor.google.UserTracker;
import net.dv8tion.jda.api.JDA;
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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class CmdTribeUpdate implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (args.length == 0) {
            if (sender instanceof BlockCommandSender) {
                BlockCommandSender cmdblock = (BlockCommandSender) sender;

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

                    User user = Bot.getJda().retrieveUserById(discordId).complete();

                    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                    Team playerTeam = scoreboard.getEntryTeam(closest.getName());

                    if (playerTeam != null) {

                        // Iterate through team names to get index
                        for (int i = 0; i < DiscCommandsManager.TRIBE_NAMES.length; i++) {
                            if (playerTeam.getName().equals(DiscCommandsManager.TRIBE_NAMES[i])) {

                                // Remove roles
                                for (String roleId : Dreamvisitor.getPlugin().getConfig().getStringList("tribeRoles")) {
                                    DiscCommandsManager.gameLogChannel.getGuild().removeRoleFromMember(user, Bot.getJda().getRoleById(roleId)).queue();
                                }

                                Role targetRole = Bot.getJda().getRoleById(Dreamvisitor.getPlugin().getConfig().getStringList("tribeRoles").get(i));
                                Dreamvisitor.debug("Role to apply: " + targetRole.getName());
                                Dreamvisitor.debug("User to apply to: " + user.getName());

                                // Add role
                                DiscCommandsManager.gameLogChannel.getGuild().addRoleToMember(user, targetRole).queue();

                                // Edit User Tracker
                                try {
                                    UserTracker.updateTribe(uuid, i);
                                } catch (GeneralSecurityException | IOException e) {
                                    throw new RuntimeException(e);
                                }

                            }
                        }

                    }

                } else {
                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "No player within 10 blocks!");
                }
            } else {
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.BLUE + "Must specify a player! /tribeupdate <player>");
            }
        } else {
            if (Bukkit.getPlayer(args[0]) != null) {
                Player target = Bukkit.getPlayer(args[0]);

                String uuid = target.getUniqueId().toString();

                String discordId = AccountLink.getDiscordId(uuid);

                User user = Bot.getJda().retrieveUserById(discordId).complete();

                Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                Team playerTeam = scoreboard.getEntryTeam(target.getName());

                if (playerTeam != null) {

                    // Iterate through team names to get index
                    for (int i = 0; i < DiscCommandsManager.TRIBE_NAMES.length; i++) {
                        if (playerTeam.getName().equals(DiscCommandsManager.TRIBE_NAMES[i])) {

                            // Remove roles
                            for (String roleId : Dreamvisitor.getPlugin().getConfig().getStringList("tribeRoles")) {
                                DiscCommandsManager.gameLogChannel.getGuild().removeRoleFromMember(user, Bot.getJda().getRoleById(roleId)).queue();
                            }

                            Role targetRole = Bot.getJda().getRoleById(Dreamvisitor.getPlugin().getConfig().getStringList("tribeRoles").get(i));
                            Dreamvisitor.debug("Role to apply: " + targetRole.getName());
                            Dreamvisitor.debug("User to apply to: " + user.getName());

                            // Add role
                            DiscCommandsManager.gameLogChannel.getGuild().addRoleToMember(user, targetRole).queue();

                            // Edit User Tracker
                            try {
                                UserTracker.updateTribe(uuid, i);
                            } catch (GeneralSecurityException | IOException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    }

                }
            }
        }

        return true;
    }

    private void teamToRole(Player player, User user, int tribeIndex) {

        JDA jda = user.getJDA();
        Dreamvisitor plugin = Dreamvisitor.getPlugin();

        // Get list of role IDs from config.yml
        List<String> tribeRoleIDs = plugin.getConfig().getStringList("tribeRoles");

        // Init tribeRoleIDs if empty
        if (tribeRoleIDs.isEmpty()) {
            for (int i = 0; i < 10; i++) {
                tribeRoleIDs.add("none");
            }
        }

        // Iterate through tribeRoleIDs
        for (int i = 0; i < tribeRoleIDs.size(); i++) {

            if (tribeRoleIDs.get(i).equals("none")) {
                // If role is not set, notify as logger
                Bukkit.getLogger().warning(DiscCommandsManager.TRIBE_NAMES[i] + "does not have a set role! Use /setrole in Discord!");
            } else if (tribeIndex == i) {
                // Add role if matching index
                DiscCommandsManager.gameLogChannel.getGuild().addRoleToMember(user, jda.getRoleById(tribeRoleIDs.get(i)));
            }
        }

        // Add data to User Tracker
        try {
            UserTracker.updateTribe(player.getUniqueId().toString(), tribeIndex);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
