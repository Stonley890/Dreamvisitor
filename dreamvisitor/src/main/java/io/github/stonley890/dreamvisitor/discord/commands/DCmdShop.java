package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.Economy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
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

        String currencySymbol = Economy.getCurrencySymbol();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(Economy.getShopName());
        embed.setColor(Color.yellow);
        List<Economy.ShopItem> items = Economy.getItems();
        items.removeIf(item -> !item.isEnabled());
        StringSelectMenu.Builder purchaseMenu = StringSelectMenu.create("purchase");
        if (items.isEmpty()) {
            embed.setDescription("There are no items currently for sale.");
            event.replyEmbeds(embed.build()).queue();
            return;
        }
        for (Economy.ShopItem item : items) {
            String priceString = String.valueOf(item.getPrice());
            double truePrice = item.getTruePrice();
            if (item.getSalePercent() > 0) {
                priceString = "~~".concat(priceString).concat("~~ ").concat(String.valueOf(truePrice)).concat(" (").concat(String.valueOf(item.getSalePercent())).concat("% off)");
            }
            String header = (item.getName() + " - " + currencySymbol + priceString);
            StringBuilder body = new StringBuilder();
            body.append("`").append(item.getId()).append("`");
            body.append("\n**").append(item.getDescription()).append("**");
            if (!item.isInfinite()) body.append("\n").append(item.getQuantity()).append(" of this item remain(s).");
            if (item.isGiftingEnabled()) body.append("\n").append("This item can be gifted.");
            else body.append("\n").append("This item cannot be gifted.");

            embed.addField(header, body.toString(), false);

            purchaseMenu.addOption(item.getName(), String.valueOf(item.getId()), item.getId() + " - " + truePrice);
        }

        event.reply("Here is what is currently available in the shop.").addEmbeds(embed.build()).addActionRow(purchaseMenu.build()).queue();

    }
}
