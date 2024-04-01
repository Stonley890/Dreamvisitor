package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

public class DCmdSchedulerestart implements DiscordCommand {
    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("schedulerestart", "Schedule a server restart when no players are online.")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        ActionRow button = ActionRow.of(Button.primary("schedulerestart", "Undo"));

        if (Dreamvisitor.restartScheduled) {
            Dreamvisitor.restartScheduled = false;
            event.reply("✅ Canceled server restart.").addActionRows(button).queue();
        } else {
            Dreamvisitor.restartScheduled = true;
            event.reply("✅ The server will restart when there are no players online").addActionRows(button).queue();
        }
    }
}
