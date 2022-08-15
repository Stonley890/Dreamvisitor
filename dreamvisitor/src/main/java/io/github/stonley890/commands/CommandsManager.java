package io.github.stonley890.commands;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandsManager extends ListenerAdapter {

    private static Channel gameChatChannel;
    private static Channel whitelistChannel;
    private static Role memberRole;
    private static Role step3role;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        if (command.equals("test")) {
            System.out.println("Test commanded");
            event.reply("Pong!").queue();
        } else if (command.equals("setgamechat") && event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            gameChatChannel = event.getOption("channel", event.getChannel(), OptionMapping::getAsChannel);
            event.reply("Game chat channel set to " + gameChatChannel.getAsMention()).queue();
        } 
        else if (command.equals("setwhitelist") && event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            whitelistChannel = event.getOption("channel", event.getChannel(), OptionMapping::getAsChannel);
            event.reply("Whitelist channel set to " + whitelistChannel.getAsMention()).queue();
        } 
        else if (command.equals("setmemberrole") && event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            memberRole = event.getOption("role", OptionMapping::getAsRole);
            event.reply("Member role set to **" + memberRole.getName() + "**").queue();
        } 
        else if (command.equals("setstep3role") && event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            step3role = event.getOption("role", OptionMapping::getAsRole);
            event.reply("Step 3 role set to **" + step3role.getName() + "**").queue();
        }
    }

    public static String getChatChannel() {
        if (gameChatChannel != null) {
            return gameChatChannel.getId();
        } else return "none";
    }

    public static String getWhitelistChannel() {
        if (whitelistChannel != null) {
            return whitelistChannel.getId();
        } else return "none";
    }

    public static String getMemberRole() {
        if (memberRole != null) {
            return memberRole.getId();
        } else return "none";
    }

    public static String getStep3Role() {
        if (step3role != null) {
            return step3role.getId();
        } else return "none";
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("test", "A test command."));
        commandData.add(Commands.slash("setgamechat", "Set the channel that game chat occurs in.").addOption(OptionType.CHANNEL, "channel", "The channel to set.", true, false));
        commandData.add(Commands.slash("setwhitelist", "Set the channel that whitelists players.").addOption(OptionType.CHANNEL, "channel", "The channel to set.", true, false));
        commandData.add(Commands.slash("setmemberrole", "Set the member role.").addOption(OptionType.ROLE, "role", "The role to set.", true, false));
        commandData.add(Commands.slash("setstep3role", "Set the Step 3 role.").addOption(OptionType.ROLE, "role", "The role to set.", true, false));

        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}