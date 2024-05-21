package io.github.stonley890.dreamvisitor.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.*;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CmdTribeUpdate implements DVCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        // tribeupdate [<playerTargets>]

        List<Player> targets = new ArrayList<>();

        if (args.length == 0) {
            // If no arguments, do self (if player)
            if (sender instanceof Player player) {
                targets.add(player);
            } else {
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Missing arguments! /tribeupdate <targets>");
                return true;
            }
        } else if (args.length == 1) {

            // Use vanilla target selector args
            List<Entity> entities;
            try {
                entities = Bukkit.selectEntities(sender, args[0]);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Incorrect arguments! /tribeupdate <targets>");
                return true;
            }

            // Check if empty
            if (entities.isEmpty()) {
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "No players were selected.");
                return true;
            }

            // Check for non-players
            for (Entity entity : entities) {
                if (entity instanceof Player player) {
                    targets.add(player);
                } else {
                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "This command is only applicable to players.");
                    return true;
                }
            }

        } else {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Too many arguments! /tribeupdate <targets>");
            return true;
        }

        // Target selection is good

        // This may take some time
        sender.sendMessage(Dreamvisitor.PREFIX + "Please wait...");

        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();

        // Run async
        Bukkit.getScheduler().runTaskAsynchronously(Dreamvisitor.getPlugin(), () -> {

            List<String> tribeRoles = Dreamvisitor.getPlugin().getConfig().getStringList("tribeRoles");

            for (Player player : targets) {

                UUID uuid = player.getUniqueId();

                // Get stored Discord ID
                long discordId;
                try {
                    discordId = AccountLink.getDiscordId(uuid);
                } catch (NullPointerException e) {
                    sender.sendMessage(Dreamvisitor.PREFIX + player.getName() + " does not have an associated Discord ID. Skipping...");
                    continue;
                }

                Dreamvisitor.debug(player.getUniqueId().toString());
                Dreamvisitor.debug(String.valueOf(discordId));

                // Retrieve user from JDA
                User user;
                try {
                    user = Bot.getJda().retrieveUserById(discordId).complete();
                } catch (Exception e) {
                    sender.sendMessage(Dreamvisitor.PREFIX + player.getName() + "'s associated Discord ID is invalid. Skipping...");
                    continue;
                }

                PlayerTribe.updateTribeOfPlayer(uuid);

                // Get team
                Team playerTeam = scoreboard.getEntryTeam(player.getName());

                if (playerTeam != null) {

                    Tribe tribe = TribeUtil.parse(playerTeam.getName());
                    if (tribe != null) {

                        try {
                            // Remove roles
                            for (String roleId : tribeRoles) {
                                Bot.getGameLogChannel().getGuild().removeRoleFromMember(user, Objects.requireNonNull(Bot.getJda().getRoleById(roleId))).queue();
                            }

                            Role targetRole = Bot.getJda().getRoleById(tribeRoles.get(TribeUtil.indexOf(tribe)));

                            if (targetRole == null) {
                                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Could not find role for " + playerTeam.getName());
                                break;
                            }

                            // Add role
                            Bot.getGameLogChannel().getGuild().addRoleToMember(user, targetRole).queue();
                        } catch (InsufficientPermissionException e) {
                            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Dreamvisitor Bot is missing permission MANAGE_ROLES. Skipping...");
                        } catch (NullPointerException e) {
                            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "One or more tribe roles  Skipping...");
                        }
                    }
                }
            }

            Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> sender.sendMessage(Dreamvisitor.PREFIX + "Updated " + targets.size() + " players."));

        });

        return true;

    }

    @NotNull
    @Override
    public String getCommandName() {
        return "tribeupdate";
    }

    @Override
    public LiteralCommandNode<?> getNode() {
        return LiteralArgumentBuilder.literal(getCommandName())
                .then(LiteralArgumentBuilder.literal("@a"))
                .then(LiteralArgumentBuilder.literal("@e"))
                .then(LiteralArgumentBuilder.literal("@p"))
                .then(LiteralArgumentBuilder.literal("@r"))
                .then(LiteralArgumentBuilder.literal("@s"))
                .then(RequiredArgumentBuilder.argument("selector", StringArgumentType.word()))
                .build();
    }
}
