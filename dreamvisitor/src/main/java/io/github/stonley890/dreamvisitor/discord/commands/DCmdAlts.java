package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.data.AccountLink;
import io.github.stonley890.dreamvisitor.data.AltFamily;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.jetbrains.annotations.NotNull;

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
            User parent = event.getOption("parent", OptionMapping::getAsUser);
            User child = event.getOption("child", OptionMapping::getAsUser);

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
                    event.getJDA().retrieveUserById(recordedParentOfChild).queue(user -> event.reply("The child account is already linked to " + user.getAsMention()).queue());
                    return;
                }
                AltFamily.setAlt(parent.getIdLong(), child.getIdLong());
                event.reply("Alts recorded successfully!").queue();

            } catch (AltFamily.NotParentException e) {
                event.getJDA().retrieveUserById(parent.getIdLong()).queue(user -> event.reply("The parent account is already linked to " + user.getAsMention() + " as a child account.").queue());
            }

        } else if (subcommand.equals("get")) {
            User user = event.getOption("user", OptionMapping::getAsUser);
            if (user == null) {
                event.reply("That member could not be found.").queue();
                return;
            }

            AltFamily altFamily;

            altFamily = AltFamily.getFamily(user.getIdLong());

            if (altFamily.getChildren().isEmpty()) {
                event.reply("There are no alts linked to this account.").queue();
            } else {
                UUID parentUuid;
                parentUuid = AccountLink.getUuid(altFamily.getParent());
                String parentUsername;
                if (parentUuid != null) parentUsername = PlayerUtility.getUsernameOfUuid(parentUuid);
                else parentUsername = null;

                event.getJDA().retrieveUserById(altFamily.getParent()).queue(parentUser -> {

                    String parentEmbed = parentUser.getAsMention();
                    if (parentUuid != null && parentUsername != null) parentEmbed = parentEmbed.concat(" (" + parentUsername + "/`" + parentUuid + "`)");

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("Alts of " + user.getName()).addField("Parent Account", parentEmbed, false);

                    StringBuilder childrenEmbed = new StringBuilder();

                    SelectMenu.Builder selectMenu = SelectMenu.create("alts-remove-" + parentUser.getId());
                    selectMenu.setPlaceholder("Remove an alt");

                    Objects.requireNonNull(event.getGuild()).retrieveMembersByIds(altFamily.getChildren()).onSuccess(childMembers -> {
                        for (Member member : childMembers) {

                            UUID childUuid;
                            childUuid = AccountLink.getUuid(member.getIdLong());
                            String childUsername;
                            if (childUuid != null) childUsername = PlayerUtility.getUsernameOfUuid(childUuid);
                            else childUsername = null;

                            selectMenu.addOption(member.getEffectiveName(), member.getEffectiveName());
                            childrenEmbed.append("- ").append(member.getAsMention()).append(" (").append(childUsername).append("/`").append(childUuid).append("`)\n");
                        }

                        embed.addField("Children", childrenEmbed.toString(), false);

                        event.replyEmbeds(embed.build()).addActionRows(ActionRow.of(selectMenu.build())).queue();
                    });
                });
            }
        }
    }
}
