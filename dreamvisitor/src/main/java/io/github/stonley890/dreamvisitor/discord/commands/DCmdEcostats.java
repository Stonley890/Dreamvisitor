package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.data.Economy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public class DCmdEcostats implements DiscordCommand {
    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("ecostats", "Get your stats.");
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {

        Economy.Consumer consumer = Economy.getConsumer(event.getUser().getIdLong());

        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle("Stats").setAuthor(null, event.getUser().getAvatarUrl())
                .setDescription("Here are your stats, " + event.getUser().getAsMention() + ".");

        embed.addField("Balance", Economy.getCurrencySymbol() + consumer.getBalance(), true)
                .addField("Daily Streak", consumer.getGameData().getDailyStreak() + " days", true)
                .addField("Time Until Next Daily", consumer.getGameData().timeUntilNextDaily().toString(), true)
                .addField("Time Until Next Work", consumer.getGameData().timeUntilNextWork().toString(), true);


    }
}
