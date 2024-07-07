package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.functions.YearTracker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;

public class DCmdYear implements DiscordCommand {
    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("year", "Get the current in-universe year.")
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setDescription("The current year is " + YearTracker.getYear() + ".");
        Duration timeUntilNextYear = YearTracker.timeUntilNextYear();
        embed.addField("Real time until next year", timeUntilNextYear.toDaysPart() + " days, " + timeUntilNextYear.toHoursPart() + " hours, " + timeUntilNextYear.toMinutesPart() + " minutes.", false);
        embed.addField("How does this work?", "The in-universe year is incremented every " + YearTracker.timePerYear().toDays() + " days.", false);
        embed.setTimestamp(Instant.now());
        embed.setColor(Color.BLUE);
        event.replyEmbeds(embed.build()).queue();
    }
}
