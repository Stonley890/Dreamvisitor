package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.data.AccountLink;
import io.github.stonley890.dreamvisitor.data.AltFamily;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class DCmdAlts implements DiscordCommand {
    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("alts", "Manage the alt tracker.").addSubcommands(
                new SubcommandData("link", "Link two accounts as alts.").addOptions(
                        new OptionData(OptionType.USER, "parent", "The parent or main account.", true),
                        new OptionData(OptionType.USER, "child", "The child or alt account.", true)
                ),
                new SubcommandData("get", "Get the alts of an account.").addOptions(
                        new OptionData(OptionType.USER, "user", "The user to get the alts of.", true)
                )
        );
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {

        String subcommand = event.getSubcommandName();

        if (subcommand == null) {
            event.reply("No subcommand given.").queue();
        } else if (subcommand.equals("link")) {
            Member parent = event.getOption("parent", OptionMapping::getAsMember);
            Member child = event.getOption("child", OptionMapping::getAsMember);

            if (parent == null) {
                event.reply("Parent account could not be found.").queue();
                return;
            }
            if (child == null) {
                event.reply("Child account could not be found.").queue();
                return;
            }

            try {
                long recordedParentOfChild = AltFamily.getParent(child.getIdLong());
                if (recordedParentOfChild != child.getIdLong()) {
                    Objects.requireNonNull(event.getGuild()).retrieveMemberById(recordedParentOfChild).queue(member -> event.reply("The child account is already linked to " + member.getAsMention()).queue());
                    return;
                }
                long recordedParentOfParent = AltFamily.getParent(parent.getIdLong());
                if (recordedParentOfParent == child.getIdLong()) {
                    Objects.requireNonNull(event.getGuild()).retrieveMemberById(recordedParentOfChild).queue(member -> event.reply("The parent account is already linked to " + member.getAsMention() + " as a child account.").queue());
                    return;
                }
                AltFamily.setAlt(parent.getIdLong(), child.getIdLong());
                event.reply("Alts recorded successfully!").queue();

            } catch (IOException | InvalidConfigurationException e) {
                event.reply(e.getMessage()).queue();
            }

        } else if (subcommand.equals("get")) {
            Member user = event.getOption("user", OptionMapping::getAsMember);
            if (user == null) {
                event.reply("That member could not be found.").queue();
                return;
            }

            AltFamily altFamily;

            try {
                altFamily = AltFamily.getFamily(user.getIdLong());
            } catch (IOException | InvalidConfigurationException e) {
                event.reply(e.getMessage()).queue();
                return;
            }

            if (altFamily.getChildren().isEmpty()) {
                event.reply("There are no alts linked to this account.").queue();
            } else {
                UUID parentUuid;
                try {
                    parentUuid = AccountLink.getUuid(altFamily.getParent());
                } catch (IOException e) {
                    event.reply("Unable to fetch AccountLink maps from disk.").queue();
                    return;
                }
                String parentUsername;
                if (parentUuid != null) parentUsername = PlayerUtility.getUsernameOfUuid(parentUuid);
                else {
                    parentUsername = null;
                }

                Objects.requireNonNull(event.getGuild()).retrieveMemberById(altFamily.getParent()).queue(parentUser -> {

                    String parentEmbed = parentUser.getAsMention();
                    if (parentUuid != null && parentUsername != null) parentEmbed = parentEmbed.concat(" (" + parentUsername + "/`" + parentUuid + "`)");

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("Alts of " + user.getEffectiveName()).addField("Parent Account", parentEmbed, false);

                    StringBuilder childrenEmbed = new StringBuilder();

                    Objects.requireNonNull(event.getGuild()).retrieveMembersByIds(altFamily.getChildren()).onSuccess(childMembers -> {
                        for (Member member : childMembers) {

                            UUID childUuid;
                            try {
                                childUuid = AccountLink.getUuid(member.getIdLong());
                            } catch (IOException e) {
                                event.reply("Unable to fetch AccountLink maps from disk.").queue();
                                return;
                            }
                            String childUsername;
                            if (childUuid != null) childUsername = PlayerUtility.getUsernameOfUuid(childUuid);
                            else {
                                childUsername = null;
                            }

                            childrenEmbed.append("- ").append(member.getAsMention()).append(" (").append(childUsername).append("/`").append(childUuid).append("`)\n");
                        }

                        embed.addField("Children", childrenEmbed.toString(), false);

                        event.replyEmbeds(embed.build()).queue();
                    });
                });
            }
        }
    }
}
