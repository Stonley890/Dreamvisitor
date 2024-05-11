package io.github.stonley890.dreamvisitor.discord.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public class DCmdDaily implements DiscordCommand {
    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("daily", "Claim your daily streak allowance.");
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {

    }
}
