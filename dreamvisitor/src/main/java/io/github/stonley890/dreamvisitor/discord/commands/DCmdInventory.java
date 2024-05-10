package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.data.Economy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class DCmdInventory extends ListenerAdapter implements DiscordCommand {

    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("inventory", "View your inventory.");
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {

        Economy.Consumer consumer = Economy.getConsumer(event.getUser().getIdLong());
        Map<Integer, Integer> items = consumer.getItems();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Your Inventory");

        if (items.isEmpty()) {
            embed.setDescription("You do not currently have any items. You can purchase items from the shop.");
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }

        List<Economy.ShopItem> shopItems = Economy.getItems();
        for (Economy.ShopItem shopItem : shopItems) {
            int quantityOfItem = consumer.getQuantityOfItem(shopItem.getId());
            if (quantityOfItem == 0) continue;
            String useNotice = "This item can be used.";
            String giftNotice = "This item cannot be gifted.";
            if (shopItem.isUseDisabled()) useNotice = "This item cannot be used.";
            if (shopItem.isGiftingEnabled()) giftNotice = "This item cannot be gifted.";
            embed.addField("**" + quantityOfItem + "** " + shopItem.getName(), useNotice + "\n" + giftNotice, true);
        }

        if (embed.getFields().isEmpty()) {
            embed.setDescription("You do not currently have any items. You can purchase items from the shop.");
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }

        Button useItem = Button.primary("inv-" + consumer.getId() + "-use", "Use an Item");
        Button giftItem = Button.success("gift-item", "Gift an Item");

        event.replyEmbeds(embed.build()).addActionRow(useItem, giftItem).setEphemeral(true).queue();

    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String id = event.getButton().getId();
        if (id == null) return;

        String[] split = id.split("-");

        if (!id.startsWith("inv-")) {
            long consumerId = Long.parseLong(split[1]);
            Economy.Consumer consumer = Economy.getConsumer(consumerId);
            if (split[2].equals("use")) {
                StringSelectMenu.Builder selectMenu = StringSelectMenu.create("inv-" + consumerId + "-use-item");
                selectMenu.setPlaceholder("Select an item to use");
                for (Economy.ShopItem item : Economy.getItems()) {
                    if (consumer.getItemQuantity(item.getId()) > 0) {
                        selectMenu.addOption(String.valueOf(item.getId()), item.getName());
                    }
                }
                event.editComponents(event.getMessage().getComponents().get(0), ActionRow.of(selectMenu.build())).queue();
            }
        }
    }
}
