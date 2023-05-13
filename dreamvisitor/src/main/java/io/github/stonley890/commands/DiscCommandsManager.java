package io.github.stonley890.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import io.github.stonley890.Bot;
import io.github.stonley890.Main;
import io.github.stonley890.data.PlayerMemory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class DiscCommandsManager extends ListenerAdapter {

    private static Channel gameChatChannel;
    private static Channel whitelistChannel;
    private static Role memberRole;
    private static Role step3role;

    String channelOption = "channel";
    String usernameOption = "username";
    String activityOption = "activity";

    static JDA jda = Bot.getJda();

    // Get channels and roles from config
    @SuppressWarnings({ "null" })
    public static void initChannelsRoles() {
        FileConfiguration config = Main.getPlugin().getConfig();

        gameChatChannel = jda.getTextChannelById(config.getString("chatChannelID"));
        whitelistChannel = jda.getTextChannelById(config.getString("whitelistChannelID"));
        memberRole = jda.getRoleById(config.getString("memberRoleID"));
        step3role = jda.getRoleById(config.getString("step3RoleID"));
    }

    @Override
    @SuppressWarnings({ "null" })
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        User user = event.getUser();

        if (command.equals("setgamechat")) {

            gameChatChannel = event.getOption(channelOption, event.getChannel(), OptionMapping::getAsChannel);
            event.reply("Game chat channel set to " + gameChatChannel.getAsMention()).queue();
            Main.getPlugin().getConfig().set("chatChannelID", gameChatChannel.getId());

        } else if (command.equals("setwhitelist")) {

            whitelistChannel = event.getOption(channelOption, event.getChannel(), OptionMapping::getAsChannel);
            event.reply("Whitelist channel set to " + whitelistChannel.getAsMention()).queue();
            Main.getPlugin().getConfig().set("whitelistChannelID", whitelistChannel.getId());

        } else if (command.equals("setmemberrole")) {

            memberRole = event.getOption("role", OptionMapping::getAsRole);
            event.reply("Member role set to **" + memberRole.getName() + "**").queue();
            Main.getPlugin().getConfig().set("memberRoleID", memberRole.getId());

        } else if (command.equals("setstep3role")) {

            step3role = event.getOption("role", OptionMapping::getAsRole);
            event.reply("Step 3 role set to **" + step3role.getName() + "**").queue();
            Main.getPlugin().getConfig().set("step3RoleID", step3role.getId());

        } else if (command.equals("list")) {
            // Compile players to list unless no players online
            if (event.getChannel() == gameChatChannel) {
                StringBuilder list = new StringBuilder();
                if (!Bukkit.getServer().getOnlinePlayers().isEmpty()) {
                    Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
                    PlayerMemory memory = new PlayerMemory();
                    List<Player> countedPlayers = new ArrayList<>();

                    for (Player player : players) {
                        File file = new File(Main.getPlayerPath(player));
                        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                        memory.setVanished(fileConfig.getBoolean("vanished"));

                        if (!memory.isVanished()) {
                            countedPlayers.add(player);
                        }
                    }

                    if (countedPlayers.isEmpty()) {
                        event.reply("**There are no players online.**").queue();
                    } else {
                        for (Player player : countedPlayers) {
                            if (list.length() > 0) {
                                list.append("`, `");
                            }
                            list.append(player.getName());
                        }
                        event.reply("**There are " + players.size() + " player(s) online:** `" + list.toString() + "`")
                                .queue();
                    }

                } else {
                    event.reply("**There are no players online.**").queue();
                }

            } else {
                event.reply("This command must be executed in " + gameChatChannel.getAsMention()).setEphemeral(true)
                        .queue();
            }
        } else if (command.equals("tempban")) {
            // Get args
            String member = event.getOption(usernameOption, OptionMapping::getAsString);
            int hours = event.getOption("hours", OptionMapping::getAsInt);
            String reason = event.getOption("reason", OptionMapping::getAsString);
            // Add ban if player is online
            if (Bukkit.getServer().getPlayer(member) != null) {
                Date date = new Date(System.currentTimeMillis() + 60 * 60 * 1000 * hours);
                Bukkit.getServer().getBanList(BanList.Type.NAME).addBan(member, reason, date, null);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.getServer().getPlayer(member).kickPlayer(reason);

                    }
                }.runTask(Main.getPlugin());
                event.reply("**`" + member + "` was successfully banned for " + hours + " hours. Reason:** " + reason)
                        .queue();
            } else {
                event.reply("**Player is offline!**").setEphemeral(true).queue();
            }
            // msg command
        } else if (command.equals("msg")) {
            String username = event.getOption(usernameOption, OptionMapping::getAsString);
            String msg = event.getOption("message", OptionMapping::getAsString);
            // Check for correct channel
            if (event.getChannel() == gameChatChannel) {
                // Check for player online
                if (Bukkit.getServer().getPlayer(username) != null) {
                    Bukkit.getServer().getPlayer(username).sendMessage(
                            org.bukkit.ChatColor.GRAY + "[" + org.bukkit.ChatColor.DARK_AQUA + user.getName()
                                    + org.bukkit.ChatColor.GRAY + " -> " + org.bukkit.ChatColor.DARK_AQUA + "me"
                                    + org.bukkit.ChatColor.GRAY + "] " + org.bukkit.ChatColor.WHITE + msg);
                    event.getGuild().getSystemChannel()
                            .sendMessage(
                                    "**Message from " + user.getAsMention() + " to **`" + username + "`**:** " + msg)
                            .queue();
                    event.reply("Message sent!").setEphemeral(true).queue();
                } else {
                    event.reply("`" + username + "` is not online!").setEphemeral(true).queue();
                }
            } else {
                event.reply("This command must be executed in " + gameChatChannel.getAsMention()).setEphemeral(true)
                        .queue();
            }
        } else if (command.equals(activityOption)) {
            // Get args
            String activity = event.getOption(activityOption, OptionMapping::getAsString);
            String activityType = event.getOption("type", OptionMapping::getAsString);

            // Set activity
            ActivityType type = ActivityType.CUSTOM_STATUS;

            if (activityType.equalsIgnoreCase("COMPETING"))
                type = ActivityType.COMPETING;
            else if (activityType.equalsIgnoreCase("LISTENING"))
                type = ActivityType.LISTENING;
            else if (activityType.equalsIgnoreCase("PLAYING"))
                type = ActivityType.PLAYING;
            else if (activityType.equalsIgnoreCase("WATCHING"))
                type = ActivityType.WATCHING;
            else {
                event.reply("Invalid activity type.").queue();
            }

            if (type != ActivityType.CUSTOM_STATUS) {
                jda.getPresence().setActivity(Activity.of(type, activity));
                event.reply("Activity set!").setEphemeral(true).queue();
            }

        }
        Main.getPlugin().saveConfig();
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

    // Register commands on ready
    @Override
    @SuppressWarnings({ "null" })
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("setgamechat", "Set the channel that game chat occurs in.")
                .addOption(OptionType.CHANNEL, channelOption, "The channel to set.", true, false)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED));

        commandData.add(Commands.slash("setwhitelist", "Set the channel that whitelists players.")
                .addOption(OptionType.CHANNEL, channelOption, "The channel to set.", true, false)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED));

        commandData.add(Commands.slash("setmemberrole", "Set the member role.").addOption(OptionType.ROLE, "role",
                "The role to set.", true, false).setDefaultPermissions(DefaultMemberPermissions.DISABLED));

        commandData.add(Commands.slash("setstep3role", "Set the Step 3 role.").addOption(OptionType.ROLE, "role",
                "The role to set.", true, false).setDefaultPermissions(DefaultMemberPermissions.DISABLED));

        commandData.add(Commands.slash("list", "List online players."));

        commandData.add(Commands.slash("tempban", "Tempban a player from the Minecraft server.")
                .addOption(OptionType.STRING, usernameOption, "The Minecraft user to tempban.", true)
                .addOption(OptionType.INTEGER, "hours", "The number of hours to enforce the tempban.", true)
                .addOption(OptionType.STRING, "reason", "Reason for tempban.", true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED));

        commandData.add(
                Commands.slash("msg", "Message a player on the Minecraft server.")
                        .addOption(OptionType.STRING, usernameOption, "The user you want to message.", true)
                        .addOption(OptionType.STRING, "message", "The message to send.", true));

        commandData.add(Commands.slash(activityOption, "Set the bot activity.")
                .addOption(OptionType.STRING, "type",
                        "The type of activity; COMPETING, LISTENING, PLAYING, WATCHING", true)
                .addOption(OptionType.STRING, activityOption, "The status to display on the bot.", true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED));

        event.getGuild().updateCommands().addCommands(commandData).queue();
        commandData.clear();

    }
}
