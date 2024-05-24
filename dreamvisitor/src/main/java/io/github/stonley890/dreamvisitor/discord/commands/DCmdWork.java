package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.data.Economy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Duration;

public class DCmdWork extends ListenerAdapter implements DiscordCommand {
    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("work", "Work for a paycheck!");
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        User user = event.getUser();
        Economy.Consumer consumer = Economy.getConsumer(user.getIdLong());
        Economy.GameData gameData = consumer.getGameData();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Daily Reward");

        try {
            double reward = consumer.claimDaily();
        } catch (Economy.Consumer.CoolDownException e) {
            Duration duration = gameData.timeUntilNextDaily();
            embedBuilder.setColor(Color.red).setDescription("You cannot claim your daily reward for " + duration.toString());
        }

    }
}
