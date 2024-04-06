package io.github.stonley890.dreamvisitor.discord;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.discord.commands.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DiscCommandsManager extends ListenerAdapter {

    static final JDA jda = Bot.getJda();

    static final List<DiscordCommand> commands = new ArrayList<>();
    

    // Get channels and roles from config
    @SuppressWarnings({"null"})
    public static void init() {

        Dreamvisitor.debug("Initializing commands...");

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
        commands.add(new DCmdAlts());
        commands.add(new DCmdInfractions());

        Dreamvisitor.debug("Ready to add to guild.");

        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<CommandData> commandData = new ArrayList<>();
        for (DiscordCommand command : commands) {
            commandData.add(command.getCommandData());
            Dreamvisitor.debug("Added command " + command.getName());
        }

        for (Guild guild : jda.getGuilds()) {
            // register commands
            for (CommandData commandDatum : commandData) {
                guild.upsertCommand(commandDatum).queue();
            }

            Dreamvisitor.debug("Updated commands.");
        }

        commandData.clear();

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
        event.reply("No commands match your request. This is a fatal error and should not be possible.\n" +
                "*Great, everything is broken. I'm going to have to bother one of my superiors to fix this.*").queue();
    }

    // Register commands on ready
    @Override
    @SuppressWarnings({"null"})
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        for (DiscordCommand command : commands) {
            commandData.add(command.getCommandData());
            Dreamvisitor.debug("Added command " + command.getName());
        }

        // register commands
        event.getGuild().updateCommands().addCommands(commandData).queue();

        Dreamvisitor.debug("Updated commands.");

        commandData.clear();

    }
}
