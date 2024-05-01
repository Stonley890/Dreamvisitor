package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.data.Economy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.jetbrains.annotations.NotNull;

public class DCmdEconomy implements DiscordCommand {
    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("economy", "Manage the Discord economy.")
                .addSubcommandGroups(
                        new SubcommandGroupData("shop", "Manage the shop.").addSubcommands(
                                new SubcommandData("name","Get or set the name of the shop.")
                                        .addOption(OptionType.STRING, "new-name","The name to set.", false, false)
                        ),
                        new SubcommandGroupData("items", "Manage items.").addSubcommands(
                                new SubcommandData("list", "List all items."),
                                new SubcommandData("edit", "Edit an item.")
                                        .addOption(OptionType.INTEGER, "id", "The ID of the item to edit."),
                                new SubcommandData("add", "Add an item.")
                                        .addOption(OptionType.STRING, "name", "The name of the item.", true)
                                        .addOption(OptionType.STRING, "description", "The description of this item.", true),
                                new SubcommandData("remove", "Permanently remove an item.")
                                        .addOption(OptionType.STRING, "id", "The ID of the item to remove.")
                        ),
                        new SubcommandGroupData("users", "Manage users.").addSubcommands(
                                new SubcommandData("balance", "Get or set the balance of a user.")
                                        .addOption(OptionType.USER, "user", "The user whose balance to get.", true)
                                        .addOption(OptionType.NUMBER, "new-balance", "The balance to set.", false),
                                new SubcommandData("items", "Manage the items of a user.")
                                        .addOption(OptionType.USER, "user", "The user whose items to get.", true)
                        )
                );
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {

        String subcommandGroup = event.getSubcommandGroup();
        String subcommand = event.getSubcommandName();

        if (subcommandGroup == null || subcommand == null) {
            event.reply("Subcommand Group or Subcommand is null.").queue();
            return;
        }

        if (subcommandGroup.equals("shop")) {
            if (subcommand.equals("name")) {
                String name = event.getOption("name", OptionMapping::getAsString);
                if (name == null) {
                    event.reply("The current shop name is " + Economy.getShopName()).queue();
                } else {
                    String shopName = Economy.getShopName();
                    Economy.setShopName(name);
                    event.reply("Changed shop name from " + shopName + " to " + name + ".").queue();
                }
            } else {
                event.reply("Subcommand not found.").queue();
            }
        } else if (subcommandGroup.equals("items")) {
            if (subcommand.equals("list")) {

                EmbedBuilder embed = new EmbedBuilder();

                

            }
        } else if (subcommandGroup.equals("users")) {

        } else {
            event.reply("Subcommand group not found.").queue();
        }

    }
}
