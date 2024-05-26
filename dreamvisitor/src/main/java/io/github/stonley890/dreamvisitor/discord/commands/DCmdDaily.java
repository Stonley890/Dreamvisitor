package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.data.Economy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Duration;

public class DCmdDaily implements DiscordCommand {
    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("daily", "Claim your daily streak allowance.");
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        User user = event.getUser();
        Economy.Consumer consumer = Economy.getConsumer(user.getIdLong());
        Economy.GameData gameData = consumer.getGameData();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Daily Reward");

        double reward;

        try {
            reward = consumer.claimDaily();
        } catch (Economy.Consumer.CoolDownException e) {
            Duration duration = gameData.timeUntilNextDaily();
            embedBuilder.setColor(Color.red).setDescription("You cannot claim your daily reward for " + String.valueOf(duration.toHoursPart()).replaceFirst("-", "") + " hour(s), " + String.valueOf(duration.toMinutesPart()).replaceFirst("-", "") + " minute(s).");
            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
            return;
        }
        Economy.saveConsumer(consumer);

        embedBuilder.setDescription("You earned " + Economy.getCurrencySymbol() + reward + " today.\nCome back in 24 hours for your next reward.")
                .setFooter("Your new balance is " + Economy.getCurrencySymbol() + consumer.getBalance() + "\nThis brings your streak to " + gameData.getDailyStreak() + " day(s).")
                .setColor(Color.GREEN);
        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }
}
