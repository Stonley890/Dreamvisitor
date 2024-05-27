package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.data.Economy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class DCmdEcostats implements DiscordCommand {
    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("ecostats", "Get your stats.");
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {

        Economy.Consumer consumer = Economy.getConsumer(event.getUser().getIdLong());
        Economy.GameData gameData = consumer.getGameData();

        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle("Stats").setAuthor(null, event.getUser().getAvatarUrl())
                .setDescription("Here are your stats as of " + Bot.createTimestamp(LocalDateTime.now(), TimeFormat.DATE_TIME_SHORT) + ", " + event.getUser().getAsMention() + ".")
                .addField("Balance", Economy.getCurrencySymbol() + consumer.getBalance(), true)
                .addField("Daily Streak", gameData.getDailyStreak() + " days", true);

        String dailyHours = String.valueOf(gameData.timeUntilNextDaily().toHoursPart()).replaceFirst("-","");
        String dailyMinutes = String.valueOf(gameData.timeUntilNextDaily().toMinutesPart()).replaceFirst("-", "");

        String workMinutes = String.valueOf(gameData.timeUntilNextWork().toMinutes()).replaceFirst("-", "");

        embed.addField("Time Until Next Daily", dailyHours + " hours, " + dailyMinutes + " minutes.", false)
                .addField("Time Until Next Work", workMinutes + " minutes.", false);

        event.replyEmbeds(embed.build()).queue();

    }
}
