package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.Bot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DCmdMsg implements DiscordCommand {
    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("msg", "Message a player on the Minecraft server.")
                .addOption(OptionType.STRING, "username", "The user you want to message.", true)
                .addOption(OptionType.STRING, "message", "The message to send.", true);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        // args
        String username = event.getOption("username", OptionMapping::getAsString);
        String msg = event.getOption("message", OptionMapping::getAsString);

        // Check for correct channel
        if (event.getChannel() == Bot.gameChatChannel) {
            // Check for player online
            assert username != null;
            if (Bukkit.getServer().getPlayer(username) != null) {

                // Send message
                Objects.requireNonNull(Bukkit.getServer().getPlayer(username)).sendMessage(
                        ChatColor.GRAY + "[" + ChatColor.DARK_AQUA + event.getUser().getName() + ChatColor.GRAY + " -> "
                                + ChatColor.DARK_AQUA + "me" + ChatColor.GRAY + "] " + ChatColor.WHITE + msg);

                // Log message
                Objects.requireNonNull(Bot.getJda().getTextChannelById(Bot.gameLogChannel.getId())).sendMessage(
                        "**Message from " + event.getUser().getAsMention() + " to **`" + username + "`**:** " + msg).queue();

                // Reply success
                event.reply("Message sent!").setEphemeral(true).queue();

            } else event.reply("`" + username + "` is not online!").setEphemeral(true).queue();
        } else
            event.reply("This command must be executed in " + Bot.gameChatChannel.getAsMention()).setEphemeral(true).queue();
    }
}
