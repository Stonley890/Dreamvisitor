package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DCmdList implements DiscordCommand {
    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("list", "List online players.");
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        // Compile players to list unless no players online
        if (event.getChannel() == Bot.getGameChatChannel()) {

            // Create a string builder
            StringBuilder list = new StringBuilder();

            // If there are players online
            if (!Bukkit.getServer().getOnlinePlayers().isEmpty()) {

                Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();

                List<Player> countedPlayers = new ArrayList<>();

                // Iterate through each player
                for (Player player : players) {
                    PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());

                    // If player is not vanished, add to list
                    if (!memory.vanished) {
                        countedPlayers.add(player);
                    }
                }

                // If there are no listed players (may occur with vanished players), report none
                if (countedPlayers.isEmpty()) {
                    event.reply("**There are no players online.**").queue();
                } else {
                    // Create string of list
                    for (Player player : countedPlayers) {
                        if (!list.isEmpty()) {
                            list.append("`, `");
                        }
                        list.append(player.getName());
                    }
                    String playerForm = "players";
                    String isAreForm = "are";
                    if (players.size() == 1) {
                        playerForm = "player";
                        isAreForm = "is";
                    }
                    // Send list
                    event.reply("**There " + isAreForm + " " + players.size() + " out of maximum " + Dreamvisitor.playerLimit + " " + playerForm + " online:** `" + list + "`")
                            .queue();
                }

            } else {
                event.reply("**There are no players online.**").queue();
            }

        } else {
            event.reply("This command must be executed in " + Bot.getGameChatChannel().getAsMention()).setEphemeral(true)
                    .queue();
        }
    }
}
