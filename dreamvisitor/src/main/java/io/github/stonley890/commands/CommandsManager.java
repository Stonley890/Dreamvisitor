package io.github.stonley890.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import io.github.stonley890.App;
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
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandsManager extends ListenerAdapter {

    private static Channel gameChatChannel;
    private static Channel whitelistChannel;
    private static Role memberRole;
    private static Role step3role;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        String noPermissionError = "You do not have permission to run this command!";

        if (command.equals("setgamechat")) {
            if (event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
                gameChatChannel = event.getOption("channel", event.getChannel(), OptionMapping::getAsChannel);
                event.reply("Game chat channel set to " + gameChatChannel.getAsMention()).queue();
            } else {
                event.reply(noPermissionError);
            }
        } else if (command.equals("setwhitelist")) {
            if (event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
                whitelistChannel = event.getOption("channel", event.getChannel(), OptionMapping::getAsChannel);
                event.reply("Whitelist channel set to " + whitelistChannel.getAsMention()).queue();
            } else {
                event.reply(noPermissionError);
            }
        } else if (command.equals("setmemberrole")) {
            if (event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
                memberRole = event.getOption("role", OptionMapping::getAsRole);
                event.reply("Member role set to **" + memberRole.getName() + "**").queue();
            } else {
                event.reply(noPermissionError);
            }
        } else if (command.equals("setstep3role")) {
            if (event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
                step3role = event.getOption("role", OptionMapping::getAsRole);
                event.reply("Step 3 role set to **" + step3role.getName() + "**").queue();
            } else {
                event.reply(noPermissionError);
            }
        } else if (command.equals("list")) {
            Bukkit.getLogger().info("List command requested");
            
            if (event.getChannel() == gameChatChannel) {
                StringBuilder online = new StringBuilder();
                if (Bukkit.getServer().getOnlinePlayers().size() > 0) {
                    Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
                    for (Player player : players) {
                        if (online.length() > 0) {
                            online.append(", ");
                        }
                        online.append(player.getName());
                    }


                    event.reply("**There are " + players.size() + " player(s) online:** " + online.toString()).queue();
                } else {
                    event.reply("**There are no players online.**").queue();
                }
                
            } else {
                event.reply("This command must be executed in " + gameChatChannel.getAsMention()).setEphemeral(true).queue();
            }
        } else if (command.equals("tempban")) {
            // Chech for ADMIN permission
            if (event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
                // Get args
                String member = event.getOption("username", OptionMapping::getAsString);
                int hours = event.getOption("hours", OptionMapping::getAsInt);
                String reason = event.getOption("reason", OptionMapping::getAsString);
                // Add ban
                if (Bukkit.getServer().getPlayer(member) != null) {
                    Date date = new Date(System.currentTimeMillis() + 60*60*1000*hours);
                    Bukkit.getServer().getBanList(BanList.Type.NAME).addBan(member, reason, date, null);
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            Bukkit.getServer().getPlayer(member).kickPlayer(reason);
                            
                        }
                        
                    }.runTask(App.getPlugin());
                    
                    event.reply("**" + member + " was successfully banned for " + hours + " hours.**").queue();
                } else {
                    event.reply("**Player is offline!**").setEphemeral(true).queue();
                }
            }
        }
    }

    public static String getChatChannel() {
        if (gameChatChannel != null) {
            return gameChatChannel.getId();
        } else
            return "none";
    }

    public static String getWhitelistChannel() {
        if (whitelistChannel != null) {
            return whitelistChannel.getId();
        } else
            return "none";
    }

    public static String getMemberRole() {
        if (memberRole != null) {
            return memberRole.getId();
        } else
            return "none";
    }

    public static String getStep3Role() {
        if (step3role != null) {
            return step3role.getId();
        } else
            return "none";
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("setgamechat", "Set the channel that game chat occurs in.")
                .addOption(OptionType.CHANNEL, "channel", "The channel to set.", true, false));
        
        commandData.add(Commands.slash("setwhitelist", "Set the channel that whitelists players.")
                .addOption(OptionType.CHANNEL, "channel", "The channel to set.", true, false));
        
        commandData.add(Commands.slash("setmemberrole", "Set the member role.").addOption(OptionType.ROLE, "role",
                "The role to set.", true, false));
        
        commandData.add(Commands.slash("setstep3role", "Set the Step 3 role.").addOption(OptionType.ROLE, "role",
                "The role to set.", true, false));
        
        commandData.add(Commands.slash("list", "List online players."));

        OptionData tempbanOption1 = new OptionData(OptionType.STRING, "username", "The Minecraft member to tempban.", true);
        OptionData tempbanOption2 = new OptionData(OptionType.INTEGER, "hours", "The number of hours to enforce the tempban.", true);
        OptionData tempbanOption3 = new OptionData(OptionType.STRING, "reason", "Reason for tempban.", true);
        commandData.add(Commands.slash("tempban", "Tempban a player from the Minecraft Server.").addOptions(tempbanOption1, tempbanOption2, tempbanOption3));

        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}