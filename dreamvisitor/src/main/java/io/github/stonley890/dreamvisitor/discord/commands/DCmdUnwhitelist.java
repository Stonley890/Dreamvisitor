package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import io.github.stonley890.dreamvisitor.data.Whitelist;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

public class DCmdUnwhitelist implements DiscordCommand {
    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("unwhitelist", "Remove a user from the whitelist.")
                .addOption(OptionType.STRING, "username", "The username to remove.", true)
                .addOption(OptionType.BOOLEAN, "ban", "Whether to ban the user from the server.", false)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        OptionMapping usernameOption = event.getOption("username");
        String username;
        if (usernameOption != null) username = usernameOption.getAsString();
        else {
            event.reply("Option `username` could not be found.").queue();
            return;
        }

        Pattern p = Pattern.compile("[^a-zA-Z0-9_-_]");

        if (p.matcher(username).find()) {
            event.reply("`" + username + "` contains illegal characters!").queue();
            return;
        }

        UUID uuid = PlayerUtility.getUUIDOfUsername(username);

        if (uuid == null) {
            event.reply("`" + username + "` could not be found!").queue();
            return;
        }

        try {
            Whitelist.remove(username, uuid);
        } catch (IOException e) {
            event.reply("There was a problem accessing the whitelist file.").queue();
            return;
        }

        event.reply("Removed " + username + " from the whitelist.").queue();
    }
}
