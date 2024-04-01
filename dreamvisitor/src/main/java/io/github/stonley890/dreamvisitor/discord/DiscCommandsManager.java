package io.github.stonley890.dreamvisitor.discord;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.discord.commands.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.*;

public class DiscCommandsManager extends ListenerAdapter {

    static final JDA jda = Bot.getJda();

    static final List<DiscordCommand> commands = new ArrayList<>();
    

    // Get channels and roles from config
    @SuppressWarnings({"null"})
    public static void init(@NotNull FileConfiguration config) {

        long chatChannelID = config.getLong("chatChannelID");
        long logChannelID = config.getLong("logChannelID");
        long whitelistChannelID = config.getLong("whitelistChannelID");

        Dreamvisitor.debug(String.valueOf(chatChannelID));
        Dreamvisitor.debug(String.valueOf(logChannelID));
        Dreamvisitor.debug(String.valueOf(whitelistChannelID));

        Bot.gameChatChannel = jda.getTextChannelById(chatChannelID);
        Bot.gameLogChannel = jda.getTextChannelById(logChannelID);
        Bot.whitelistChannel = jda.getTextChannelById(whitelistChannelID);

        if (Bot.gameChatChannel == null) Bukkit.getLogger().warning("The game log channel with ID " + chatChannelID + " does not exist!");
        if (Bot.gameLogChannel == null) Bukkit.getLogger().warning("The game log channel with ID " + logChannelID + " does not exist!");
        if (Bot.whitelistChannel == null) Bukkit.getLogger().warning("The game log channel with ID " + whitelistChannelID + " does not exist!");

        for (int i = 0; i < 10; i++) {
            Bot.tribeRole.add(jda.getRoleById(config.getLongList("tribeRoles").get(i)));
        }

        commands.add(new DCmdActivity());
        commands.add(new DCmdBroadcast());
        commands.add(new DCmdLink());
        commands.add(new DCmdList());
        commands.add(new DCmdMsg());
        commands.add(new DCmdPanic());
        commands.add(new DCmdResourcepackupdate());
        commands.add(new DCmdSchedulerestart());
        commands.add(new DCmdSetgamechat());
        commands.add(new DCmdSetlogchat());
        commands.add(new DCmdSetrole());
        commands.add(new DCmdSetwhitelist());
        commands.add(new DCmdToggleweb());
        commands.add(new DCmdUnwhitelist());
        commands.add(new DCmdUser());
        commands.add(new DCmdWarn());

    }

    @Override
    @SuppressWarnings({"null"})
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        for (DiscordCommand command : commands) {
            if (event.getName().equals(command.getName())) {
                command.onCommand(event);
                return;
            }
        }
        event.reply("No commands match your request. This is a fatal error.").queue();
    }

    // Register commands on ready
    @Override
    @SuppressWarnings({"null"})
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        for (DiscordCommand command : commands) {
            commandData.add(command.getCommandData());
        }

        // register commands
        event.getGuild().updateCommands().addCommands(commandData).queue();

        commandData.clear();

    }
}
