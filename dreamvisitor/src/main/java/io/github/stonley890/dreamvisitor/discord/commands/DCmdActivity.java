package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.Bot;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public class DCmdActivity implements DiscordCommand {
    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("activity", "Set the bot activity.")
                .addOptions(new OptionData(OptionType.STRING, "type",
                        "The type of activity.", true)
                        .setAutoComplete(false)
                        .addChoice("COMPETING", "COMPETING")
                        .addChoice("LISTENING", "LISTENING")
                        .addChoice("PLAYING", "PLAYING")
                        .addChoice("WATCHING", "WATCHING")
                )
                .addOption(OptionType.STRING, "activity", "The status to display on the bot.", true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        // Get args
        String activity = event.getOption("activity", OptionMapping::getAsString);
        String activityType = event.getOption("type", OptionMapping::getAsString);

        // Set activity
        Activity.ActivityType type = Activity.ActivityType.CUSTOM_STATUS;

        assert activityType != null;
        if (activityType.equalsIgnoreCase("COMPETING"))
            type = Activity.ActivityType.COMPETING;
        else if (activityType.equalsIgnoreCase("LISTENING"))
            type = Activity.ActivityType.LISTENING;
        else if (activityType.equalsIgnoreCase("PLAYING"))
            type = Activity.ActivityType.PLAYING;
        else if (activityType.equalsIgnoreCase("WATCHING"))
            type = Activity.ActivityType.WATCHING;
        else {
            event.reply("Invalid activity type.").queue();
        }

        // Set presence
        if (type != Activity.ActivityType.CUSTOM_STATUS) {
            assert activity != null;
            Bot.getJda().getPresence().setActivity(Activity.of(type, activity));
            event.reply("Activity set!").setEphemeral(true).queue();
        }
    }
}
