package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class DCmdSetrole implements DiscordCommand {
    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("setrole", "Set a role").addOptions(
                        new OptionData(OptionType.STRING, "type", "The role you want to set.", true)
                                .setAutoComplete(false)
                                .addChoice("HiveWing", "HiveWing")
                                .addChoice("IceWing", "IceWing")
                                .addChoice("LeafWing", "LeafWing")
                                .addChoice("MudWing", "MudWing")
                                .addChoice("NightWing", "NightWing")
                                .addChoice("RainWing", "RainWing")
                                .addChoice("SandWing", "SandWing")
                                .addChoice("SeaWing", "SeaWing")
                                .addChoice("SilkWing", "SilkWing")
                                .addChoice("SkyWing", "SkyWing")
                )
                .addOption(OptionType.ROLE, "role", "The role to associate.", true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        // Get role to set
        String targetRole = Objects.requireNonNull(event.getOption("type")).getAsString();
        Role role = Objects.requireNonNull(event.getOption("role")).getAsRole();

        if (Arrays.stream(Bot.TRIBE_NAMES).anyMatch(Predicate.isEqual(targetRole))) {

            // If one of the tribe names, find the index, get the list from config, and set the specified item
            int index = Arrays.binarySearch(Bot.TRIBE_NAMES, targetRole);
            List<Long> tribeRoles = Dreamvisitor.getPlugin().getConfig().getLongList("tribeRoles");

            if (tribeRoles.isEmpty()) {
                for (int i = 0; i < 10; i++) {
                    tribeRoles.add(0L);
                }
            }

            tribeRoles.set(index, role.getIdLong());
            Dreamvisitor.getPlugin().getConfig().set("tribeRoles", tribeRoles);

        } else {
            event.reply("The target role must match a specified name!").setEphemeral(true).queue();
        }
        event.reply("**" + targetRole + " set to " + role.getName() + "**").queue();
        Dreamvisitor.getPlugin().saveConfig();
    }
}
