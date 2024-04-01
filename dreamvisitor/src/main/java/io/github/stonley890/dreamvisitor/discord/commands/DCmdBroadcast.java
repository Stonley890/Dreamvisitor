package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public class DCmdBroadcast implements DiscordCommand {
    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("broadcast", "Broadcast a message to the Minecraft server.")
                .addOption(OptionType.STRING, "message", "The message to broadcast.", true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        // Get args
        String message = event.getOption("message", OptionMapping::getAsString);

        assert message != null;
        if (message.length() < 351) {
            // Send message
            Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> Bukkit.broadcastMessage(ChatColor.DARK_BLUE + "[" + ChatColor.WHITE + "Broadcast" + ChatColor.DARK_BLUE + "] " + ChatColor.BLUE + message));

            EmbedBuilder builder = new EmbedBuilder();

            builder.setAuthor("Staff Broadcast");
            builder.setTitle(message);

            Bot.getGameChatChannel().sendMessageEmbeds(builder.build()).queue();
            Bot.getGameLogChannel().sendMessageEmbeds(builder.build()).queue();

            // Reply
            event.reply("Broadcast sent.").queue();
        } else {
            event.reply("Message too long! " + message.length() + " > 350").setEphemeral(true).queue();
        }
    }
}
