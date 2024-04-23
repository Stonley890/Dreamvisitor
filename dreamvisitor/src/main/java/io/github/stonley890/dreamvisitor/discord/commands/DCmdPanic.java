package io.github.stonley890.dreamvisitor.discord.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

public class DCmdPanic implements DiscordCommand {
    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("panic", "Kick all players from the server and set the player limit to 0.")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        EmbedBuilder replyEmbed = new EmbedBuilder();
        replyEmbed.setTitle("Are you sure?").setDescription("This will kick all players and set the player limit to 0. Click the button to confirm.");

        Button danger = Button.danger("panic", "Yes, I'm sure.");

        event.replyEmbeds(replyEmbed.build()).addActionRow(danger).queue();
    }
}
