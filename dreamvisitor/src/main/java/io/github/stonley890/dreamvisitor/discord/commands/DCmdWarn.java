package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.data.AltFamily;
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

    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("warn", "Warn a member.")
                .addOption(OptionType.USER, "member", "The member to warn", true)
                .addOption(OptionType.INTEGER, "value", "How many infractions to count this as.", true)
                .addOption(OptionType.BOOLEAN, "silent", "Whether to notify the member. If true, Dreamvisitor will NOT notify.", true)
                .addOption(OptionType.STRING, "reason", "The reason for this warn.", false)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply().queue(DCmdWarn::updateLastInteraction);

        Member member = Objects.requireNonNull(event.getOption("member")).getAsMember();
        if (member == null) {
            event.getHook().editOriginal("That member does not exist.").queue();
            return;
        }

        long parent = AltFamily.getParent(member.getIdLong());
        if (parent != member.getIdLong()) {
            Objects.requireNonNull(event.getGuild()).retrieveMemberById(parent).queue(parentMember -> event.getHook().editOriginal("That user is the child of " + parentMember.getAsMention() + ". Warn them instead.").queue());
            return;
        }

        int value = Objects.requireNonNull(event.getOption("value")).getAsInt();
        if (value > 6) {
            event.getHook().editOriginal("Slow down there. You cannot give an infraction a value higher than **3**.").queue();
            return;
        }

        List<Infraction> infractions;

        infractions = Infraction.getInfractions(member.getIdLong());

        int infractionCount = Infraction.getInfractionCount(infractions, false);
        if (infractionCount + value > Infraction.BAN_POINT) {
            event.getHook().editOriginal("You cannot add an infraction whose value exceeds the ban point of " + Infraction.BAN_POINT +
                    ". The highest value you can assign is " + (Infraction.BAN_POINT - infractionCount)).queue();
            return;
        }

        String reason = event.getOption("reason", OptionMapping::getAsString);
        if (reason == null) reason = "Unspecified.";
        reason = reason.strip();

        boolean silent = Boolean.TRUE.equals(event.getOption("silent", OptionMapping::getAsBoolean));

        boolean hasTempban;

        hasTempban = Infraction.hasTempban(member.getIdLong());

        if (infractionCount < Infraction.BAN_POINT) {
            if (infractionCount + value == Infraction.BAN_POINT) {

                ActionRow actionRow;
                if (!hasTempban) {
                    if (silent) actionRow = ActionRow.of(
                            Button.danger(Infraction.actionBan, "Ban them for two weeks."),
                            Button.secondary(Infraction.actionNoBan, "Don't auto-ban them.")
                    );
                    else actionRow = ActionRow.of(
                            Button.danger(Infraction.actionBan, "Ban them for two weeks."),
                            Button.primary(Infraction.actionUserBan, "No, but mention a temp-ban in the message."),
                            Button.secondary(Infraction.actionNoBan, "No, don't mention a temp-ban.")
                    );
                } else {
                    if (silent) actionRow = ActionRow.of(
                            Button.danger(Infraction.actionBan, "Permanently ban from Minecraft."),
                            Button.danger(Infraction.actionAllBan, "Permanently ban from all."),
                            Button.secondary(Infraction.actionNoBan, "No, don't ban them.")
                    );
                    else actionRow = ActionRow.of(
                            Button.danger(Infraction.actionBan, "Permanently ban from Minecraft."),
                            Button.danger(Infraction.actionAllBan, "Ban from all immediately (skip message)."),
                            Button.primary(Infraction.actionUserBan, "Mention a ban, but don't auto-ban."),
                            Button.secondary(Infraction.actionNoBan, "Don't mention a ban and don't auto-ban.")
                    );
                }

                memberId = member.getIdLong();

                lastInfraction = new Infraction((byte) value, reason, LocalDateTime.now());
                event.getHook().editOriginal("This will be the user's third warn. Do you want me to also give them a ban from the Minecraft server?")
                        .setActionRows(actionRow).queue();
            } else {

                try {
                    Infraction.execute(new Infraction((byte) value, reason, LocalDateTime.now()), member, silent, Infraction.actionNoBan);
                } catch (IOException e) {
                    event.getHook().editOriginal("An I/O error occurred! Does the server have read/write access? Cannot read infractions.yml! The warn was not recorded.").queue();
                    return;
                }
                event.getHook().editOriginal("Infraction recorded.").queue();

            }
        }
    }

    private static void updateLastInteraction(InteractionHook newInteraction) {

        List<ActionRow> actionRows = new ArrayList<>();

        if (lastInteraction != null) {
            lastInteraction.retrieveOriginal().queue(original -> {
                for (ActionRow actionRow : original.getActionRows()) {
                    actionRows.add(actionRow.asDisabled());
                }
            });
            lastInteraction.editOriginalComponents(actionRows).queue(completion -> lastInteraction = newInteraction);
            return;
        }

        lastInteraction = newInteraction;

    }
}
