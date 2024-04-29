package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.Economy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

public class DCmdShop implements DiscordCommand {

    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("shop", "Access the shop.");
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {

        String currencyIcon = Dreamvisitor.getPlugin().getConfig().getString("currencyIcon");

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.yellow);
        List<Economy.ShopItem> items = Economy.getItems();
        items.removeIf(item -> !item.isEnabled());
        if (items.isEmpty()) {
            embed.setDescription("There are no items currently for sale.");
            event.replyEmbeds(embed.build()).queue();
            return;
        }
        for (Economy.ShopItem item : items) {
            embed.setTitle(item.getName() + " - " + currencyIcon + item.getPrice());
        }

    }
}
