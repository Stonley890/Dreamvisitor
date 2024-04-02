package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.AltFamily;
import io.github.stonley890.dreamvisitor.data.Infraction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class DCmdInfractions implements DiscordCommand {
    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("infractions", "Get the infractions of a user.")
                .addOption(OptionType.USER, "user", "The user whose infractions to get.", true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        User user = event.getOption("user", OptionMapping::getAsUser);
        if (user == null) {
            event.reply("Null option! Invalid!").queue();
            return;
        }

        try {
            long parent = AltFamily.getParent(user.getIdLong());
            if (parent != user.getIdLong()) {
                Objects.requireNonNull(event.getGuild()).retrieveMemberById(parent).queue(parentMember -> event.reply("That user is the child of " + parentMember.getAsMention() + ". Search their infractions instead.").queue());
                return;
            }
        } catch (IOException | InvalidConfigurationException e) {
            event.getHook().editOriginal("An I/O error occurred! Does the server have read/write access? Cannot read alts.yml! The warn was not recorded.").queue();
            if (Dreamvisitor.debugMode) e.printStackTrace();
            return;
        }

        List<Infraction> infractions;
        try {
            infractions = Infraction.getInfractions(user.getIdLong());
        } catch (IOException | InvalidConfigurationException e) {
            event.reply("Something went wrong while trying to read infractions.yml.").queue();
            if (Dreamvisitor.debugMode) e.printStackTrace();
            return;
        }

        if (infractions.isEmpty()) {
            event.reply(user.getName() + " has no recorded infractions.").queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();

        ActionRow buttons = ActionRow.of(
                Button.primary("infraction-expire-"+ user.getId(), "Expire a warn"),
                Button.danger("infraction-remove-" + user.getId(), "Remove a warn")
        );

        for (Infraction infraction : infractions) {
            String expire = "Valid";
            if (infraction.isExpired()) expire = "Expired";
            embed.addField(
                    infraction.getTime().format(DateTimeFormatter.ofPattern("M/d/u H:m")) + " " + ZoneId.systemDefault().getId(),
                    "*Value: " + infraction.getValue() + ", " + expire + "\n**Reason:** " + infraction.getReason(),
                    false);
        }

        embed
                .setTitle("Infractions of " + user.getAsMention())
                .setAuthor(user.getName(), null, user.getAvatarUrl())
                .setFooter("The total value of valid infractions is " + Infraction.getInfractionCount(infractions, false) + ".\n" +
                        "The total value of infractions " + Infraction.getInfractionCount(infractions, true) + ".");

        event.replyEmbeds(embed.build()).addActionRows(buttons).queue();
    }
}
