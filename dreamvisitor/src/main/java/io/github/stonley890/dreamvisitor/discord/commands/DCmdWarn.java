package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.data.Infraction;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DCmdWarn implements DiscordCommand {

    public static InteractionHook lastInteraction = null;
    public static Infraction lastInfraction = null;
    public static boolean silent = false;
    public static long memberId = 0;
    public static final String buttonLimitThree = "limit_three";
    public static final String buttonExceedThree = "exceed_three";

    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("warn", "Warn a member.")
                .addOption(OptionType.USER, "member", "The member to warn", true)
                .addOption(OptionType.INTEGER, "value", "How many infractions to count this as. 0 to not record in Circle.", true)
                .addOption(OptionType.STRING, "reason", "The reason for this warn. Also used as the title of the infraction.", false)
                .addOption(OptionType.BOOLEAN, "silent", "Whether to notify the member. If true, Dreamvisitor will NOT notify.", true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        Member member = Objects.requireNonNull(event.getOption("member")).getAsMember();
        if (member == null) {
            event.reply("That member does not exist.").queue();
            return;
        }

        int value = Objects.requireNonNull(event.getOption("value")).getAsInt();
        if (value > 6) {
            event.reply("Slow down there. You cannot give an infraction a value higher than **3**.").queue();
            return;
        }

        List<Infraction> infractions;

        try {
            infractions = Infraction.getInfractions(member.getIdLong());
        } catch (IOException | InvalidConfigurationException e) {
            event.reply("An I/O error occurred! Does the server have read/write access? Cannot read infractions.yml! The warn was not recorded.").queue();
            return;
        }

        int infractionCount = Infraction.getInfractionCount(infractions, false);
        if (infractionCount + value > Infraction.BAN_POINT) {
            event.reply("You cannot add an infraction whose value exceeds the ban point of " + Infraction.BAN_POINT +
                    ". The highest value you can assign is " + (Infraction.BAN_POINT - infractionCount)).queue();
            return;
        }

        String reason = event.getOption("reason", OptionMapping::getAsString);
        if (reason == null) reason = "Unspecified.";
        reason = reason.strip();

        boolean silent = Boolean.TRUE.equals(event.getOption("silent", OptionMapping::getAsBoolean));

        boolean hasTempban;

        try {
            hasTempban = Infraction.hasTempban(member.getIdLong());
        } catch (IOException e) {
            event.reply("There was an error reading from `infractions.yml` on disk! The warn was not recorded.").queue();
            return;
        } catch (InvalidConfigurationException e) {
            event.reply("`infractions.yml` is improperly formatted and could not be parsed as YAML! The warn was not recorded.").queue();
            return;
        }

        if (infractionCount < Infraction.BAN_POINT) {
            if (infractionCount + value == Infraction.BAN_POINT) {

                ActionRow actionRow;
                if (!hasTempban) {
                    if (silent) actionRow = ActionRow.of(
                            Button.danger(Infraction.actionBan, "Yes, ban them for two weeks."),
                            Button.secondary(Infraction.actionNoBan, "No, don't auto-ban them.")
                    );
                    else actionRow = ActionRow.of(
                            Button.danger(Infraction.actionBan, "Yes, ban them for two weeks."),
                            Button.primary(Infraction.actionUserBan, "No, but mention a temp-ban in the message."),
                            Button.secondary(Infraction.actionNoBan, "No, don't mention a temp-ban.")
                    );
                } else {
                    if (silent) actionRow = ActionRow.of(
                            Button.danger(Infraction.actionBan, "Yes, permanently ban them from Minecraft."),
                            Button.danger(Infraction.actionAllBan, "Yes, permanently ban them from all."),
                            Button.secondary(Infraction.actionNoBan, "No, don't ban them.")
                    );
                    else actionRow = ActionRow.of(
                            Button.danger(Infraction.actionAllBan, "Yes, permanently ban them from Minecraft."),
                            Button.danger(Infraction.actionAllBan, "Ban them from all immediately (skip message)."),
                            Button.primary(Infraction.actionUserBan, "Mention a ban, but don't auto-ban."),
                            Button.secondary(Infraction.actionNoBan, "Don't mention a ban and don't auto-ban.")
                    );
                }

                event.reply("This will be the user's third warn. Do you want me to also give them a ban from the Minecraft server?")
                        .addActionRows(actionRow).queue(DCmdWarn::updateLastInteraction);
            } else {

                try {
                    Infraction.execute(new Infraction((byte) value, reason, LocalDateTime.now()), member, silent, Infraction.actionNoBan);
                } catch (IOException e) {
                    event.reply("An I/O error occurred! Does the server have read/write access? Cannot read infractions.yml! The warn was not recorded.").queue();
                    return;
                } catch (InvalidConfigurationException e) {
                    event.reply("Fatal error: Invalid action ID! The warn was not recorded.").queue();
                    return;
                }
                event.reply("Infraction recorded.").queue();

            }
        }
    }

    private static void updateLastInteraction(InteractionHook newInteraction) {

        List<ActionRow> actionRows = new ArrayList<>();

        lastInteraction.retrieveOriginal().queue(original -> {
            for (ActionRow actionRow : original.getActionRows()) {
                actionRows.add(actionRow.asDisabled());
            }
        });

        lastInteraction.editOriginalComponents(actionRows).queue(completion -> lastInteraction = newInteraction);
    }
}
