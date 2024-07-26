package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DCmdSchedulerestart extends ListenerAdapter implements DiscordCommand {
    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("schedulerestart", "Schedule a server restart when no players are online.")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        Button button = Button.primary("schedulerestart", "Undo");

        if (Dreamvisitor.restartScheduled) {
            Dreamvisitor.restartScheduled = false;
            event.reply("✅ Canceled server restart.").addActionRow(button).queue();
        } else {
            Dreamvisitor.restartScheduled = true;
            event.reply("✅ The server will restart when there are no players online").addActionRow(button).queue();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!Objects.equals(event.getButton().getId(), "schedulerestart")) {
            return;
        }

        Button undoButton = Button.primary("schedulerestart", "Undo");

        if (Dreamvisitor.restartScheduled) {
            Dreamvisitor.restartScheduled = false;
            event.reply("✅ Canceled server restart.").addActionRow(undoButton).queue();
        } else {
            Dreamvisitor.restartScheduled = true;
            event.reply("✅ The server will restart when there are no players online").addActionRow(undoButton).queue();
        }

    }
}
