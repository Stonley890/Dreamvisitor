package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.data.Economy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public class DCmdShop extends ListenerAdapter implements DiscordCommand {

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

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!Objects.requireNonNull(event.getButton().getId()).startsWith("purchase-")) {
            return;
        }

        String itemIdString = event.getButton().getId().substring("purchase-".length());
        int itemId;
        try {
            itemId = Integer.parseInt(itemIdString);
        } catch (NumberFormatException e) {
            event.reply("The item you selected could not be parsed.").queue();
            return;
        }
        Economy.ShopItem item = Economy.getItem(itemId);
        if (item == null) {
            event.reply("That item does not exist.").queue();
            return;
        }
        Economy.Consumer consumer = Economy.getConsumer(event.getUser().getIdLong());
        try {
            consumer.purchaseItem(itemId);
        } catch (Economy.Consumer.ItemNotEnabledException | Economy.Consumer.ItemOutOfStockException |
                 NullPointerException e) {
            event.reply(e.getMessage()).queue();
            return;
        } catch (Economy.Consumer.InsufficientFundsException e) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setDescription("You do not have sufficient funds to purchase " + item.getName() + ".\nYour balance: " + Economy.getCurrencySymbol() + consumer.getBalance() + "\nItem cost: " + Economy.getCurrencySymbol() + item.getTruePrice());
            embed.setColor(Color.RED);
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        } catch (Economy.Consumer.MaxItemQualityException e) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setDescription("You already have " + item.getMaxAllowed() + " of this item, which is as many as you can have at one time.");
            embed.setColor(Color.RED);
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }

        Economy.saveConsumer(consumer);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Purchase successful!");
        embed.setDescription("Purchased " + item.getName() + " for " + Economy.getCurrencySymbol() + item.getTruePrice() + ".");
        embed.setFooter("You now have " + consumer.getQuantityOfItem(itemId) + " of this item.\nYour new balance is " + Economy.getCurrencySymbol() + consumer.getBalance());
        embed.setColor(Color.GREEN);

        event.editMessageEmbeds(embed.build()).queue();
        event.getMessage().editMessageComponents().queue();
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (Objects.equals(event.getComponent().getId(), "purchase")) {

            String itemIdString = event.getValues().get(0);
            int itemId;
            try {
                itemId = Integer.parseInt(itemIdString);
            } catch (NumberFormatException e) {
                event.reply("The item you selected could not be parsed.").queue();
                return;
            }
            Economy.ShopItem item = Economy.getItem(itemId);
            if (item == null) {
                event.reply("That item does not exist.").queue();
                return;
            }

            Economy.Consumer consumer = Economy.getConsumer(event.getUser().getIdLong());

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(item.getName());

            StringBuilder description = new StringBuilder(item.getDescription());
            if (item.getSalePercent() == 0) description.append("\n\nThis item costs ").append(Economy.getCurrencySymbol()).append(item.getPrice());
            else {
                description.append("\n\nThis item regularly costs ").append(Economy.getCurrencySymbol()).append(item.getPrice()).append(".")
                        .append("\nIt is currently **").append(item.getSalePercent()).append("% off**, bringing the total to **").append(Economy.getCurrencySymbol()).append(item.getTruePrice()).append("**.");
            }
            if (item.getMaxAllowed() != -1) description.append("\nYou can carry up to **").append(item.getMaxAllowed()).append("** of this item at a time.");
            if (item.getQuantity() != -1) description.append("\n**").append(item.getQuantity()).append("** of this item remain.");
            embed.setDescription(description);
            embed.setFooter("Your current balance is " + Economy.getCurrencySymbol() + consumer.getBalance() + ". After purchasing this item, it would be " + Economy.getCurrencySymbol() + (consumer.getBalance() - item.getTruePrice()) + ".");

            net.dv8tion.jda.api.interactions.components.buttons.Button buyButton = Button.success("purchase-" + itemId, "Purchase for " + Economy.getCurrencySymbol() + item.getTruePrice());

            event.replyEmbeds(embed.build()).addActionRow(buyButton).setEphemeral(true).queue();

        }
    }
}
