package io.github.stonley890.dreamvisitor.discord.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public interface DiscordCommand {

    @NotNull SlashCommandData getCommandData();

    @NotNull
    default String getName() {
        return getCommandData().getName();
    }

    void onCommand(@NotNull SlashCommandInteractionEvent event);
    
}
