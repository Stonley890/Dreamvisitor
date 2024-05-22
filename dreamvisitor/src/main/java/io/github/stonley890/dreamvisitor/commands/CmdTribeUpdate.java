package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
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

import java.util.*;

public class CmdTribeUpdate implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("tribeupdate")
                .withHelp("Update a player's tribe.", "Update the roles of a player based on their tribe.")
                .withPermission(CommandPermission.fromString("dreamvisitor.tribeupdate"))
                .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                .executesNative((sender, args) -> {
                    Collection<Player> players = (Collection<Player>) args.get("players");
                    assert players != null;

                    // This may take some time
                    sender.sendMessage(Dreamvisitor.PREFIX + "Please wait...");

                    Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();

                    // Run async
                    Bukkit.getScheduler().runTaskAsynchronously(Dreamvisitor.getPlugin(), () -> {

                        List<String> tribeRoles = Dreamvisitor.getPlugin().getConfig().getStringList("tribeRoles");

                        for (Player player : players) {

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

                        Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> sender.sendMessage(Dreamvisitor.PREFIX + "Updated " + players.size() + " players."));

                    });
                });
    }
}
