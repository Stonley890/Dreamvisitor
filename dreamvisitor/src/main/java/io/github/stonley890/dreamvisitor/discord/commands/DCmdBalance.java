package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.Economy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public class DCmdBalance implements DiscordCommand {
    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("balance", "Get your current balance.")
                .addOption(OptionType.USER, "member", "[Optional] The member whose balance to fetch", false, false);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {

        String message;

        User targetUser = event.getOption("member", OptionMapping::getAsUser);
        if (targetUser != null) {
            Economy.Consumer consumer = Economy.getConsumer(targetUser.getIdLong());
            double balance;
            balance = consumer.getBalance();
            message = targetUser.getAsMention() + " has " + Dreamvisitor.getPlugin().getConfig().getString("currencyIcon") + balance + ".";
        } else {
            Economy.Consumer consumer = Economy.getConsumer(event.getUser().getIdLong());
            double balance;
            balance = consumer.getBalance();
            message = "You have " + Economy.getCurrencySymbol() + balance + ".";
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setDescription(message);

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();

    }
}
