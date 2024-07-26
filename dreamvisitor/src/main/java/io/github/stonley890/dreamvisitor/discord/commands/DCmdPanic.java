package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DCmdPanic extends ListenerAdapter implements DiscordCommand {
    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("panic", "Kick all players from the server and set the player limit to 0.")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        EmbedBuilder replyEmbed = new EmbedBuilder();
        replyEmbed.setTitle("Are you sure?").setDescription("This will kick all players and set the player limit to 0. Click the button to confirm.");

        Button danger = Button.danger("panic", "Yes, I'm sure.");

        event.replyEmbeds(replyEmbed.build()).setEphemeral(true).addActionRow(danger).queue();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!Objects.equals(event.getButton().getId(), "panic")) {
            return;
        }
        Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) if (!player.isOp()) player.kickPlayer("Panic!");
        });
        Dreamvisitor.playerLimit = 0;
        Dreamvisitor.getPlugin().getConfig().set("playerlimit", 0);
        Dreamvisitor.getPlugin().saveConfig();
        Bukkit.getServer().broadcastMessage(
                ChatColor.RED + "Panicked by " + event.getUser().getName() + ".\nPlayer limit override set to 0.");
        Bot.sendLog("**Panicked by " + event.getUser().getName());
        event.reply("Panicked!").queue();

        // Disable button after use
        event.editButton(event.getButton().asDisabled()).queue();
    }
}
