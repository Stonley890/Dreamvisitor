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

import io.github.stonley890.App;
import io.github.stonley890.Bot;
import io.github.stonley890.data.PlayerMemory;
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

    // Get channels and roles from config
    public static void initChannelsRoles() {
        FileConfiguration config = App.getPlugin().getConfig();
        gameChatChannel = Bot.getJDA().getTextChannelById(config.getString("chatChannelID"));
        whitelistChannel = Bot.getJDA().getTextChannelById(config.getString("whitelistChannelID"));
        memberRole = Bot.getJDA().getRoleById(config.getString("memberRoleID"));
        step3role = Bot.getJDA().getRoleById(config.getString("step3RoleID"));
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        String noPermissionError = "You do not have permission to run this command!";

        if (command.equals("setgamechat")) {
            if (event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {

                gameChatChannel = event.getOption("channel", event.getChannel(), OptionMapping::getAsChannel);
                event.reply("Game chat channel set to " + gameChatChannel.getAsMention()).queue();
                App.getPlugin().getConfig().set("chatChannelID", gameChatChannel.getId());

            } else {
                event.reply(noPermissionError).setEphemeral(true).queue();
            }
        } else if (command.equals("setwhitelist")) {
            if (event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {

                whitelistChannel = event.getOption("channel", event.getChannel(), OptionMapping::getAsChannel);
                event.reply("Whitelist channel set to " + whitelistChannel.getAsMention()).queue();
                App.getPlugin().getConfig().set("whitelistChannelID", whitelistChannel.getId());

            } else {
                event.reply(noPermissionError).setEphemeral(true).queue();
            }
        } else if (command.equals("setmemberrole")) {
            if (event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {

                memberRole = event.getOption("role", OptionMapping::getAsRole);
                event.reply("Member role set to **" + memberRole.getName() + "**").queue();
                App.getPlugin().getConfig().set("memberRoleID", memberRole.getId());

            } else {
                event.reply(noPermissionError).setEphemeral(true).queue();
            }
        } else if (command.equals("setstep3role")) {
            if (event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {

                step3role = event.getOption("role", OptionMapping::getAsRole);
                event.reply("Step 3 role set to **" + step3role.getName() + "**").queue();
                App.getPlugin().getConfig().set("step3RoleID", step3role.getId());

            } else {
                event.reply(noPermissionError).setEphemeral(true).queue();
            }
        } else if (command.equals("list")) {
            // Compile players to list unless no players online
            if (event.getChannel() == gameChatChannel) {
                StringBuilder list = new StringBuilder();
                if (Bukkit.getServer().getOnlinePlayers().size() > 0) {
                    Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
                    PlayerMemory memory = new PlayerMemory();
                    List<Player> countedPlayers = new ArrayList<Player>();

                    for (Player player : players) {

                        File file = new File(App.getPlayerPath(player));
                        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                        memory.setVanished(fileConfig.getBoolean("vanished"));

                        if (memory.isVanished() == false) {
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
            // Chech for ADMIN permission
            if (event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
                // Get args
                String member = event.getOption("username", OptionMapping::getAsString);
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

                    }.runTask(App.getPlugin());

                    event.reply(
                            "**`" + member + "` was successfully banned for " + hours + " hours. Reason:** " + reason)
                            .queue();
                } else {
                    event.reply("**Player is offline!**").setEphemeral(true).queue();
                }
            }
            // msg command
        } else if (command.equals("msg")) {
            String username = event.getOption("username", OptionMapping::getAsString);
            String msg = event.getOption("message", OptionMapping::getAsString);
            // Check for correct channel
            if (event.getChannel() == gameChatChannel) {
                // Check for player online
                if (Bukkit.getServer().getPlayer(username) != null) {
                    Bukkit.getServer().getPlayer(username).sendMessage(org.bukkit.ChatColor.GRAY + "[" + org.bukkit.ChatColor.DARK_AQUA + event.getUser().getName()
                            + org.bukkit.ChatColor.GRAY + " -> " + org.bukkit.ChatColor.DARK_AQUA + "me" + org.bukkit.ChatColor.GRAY + "] " + org.bukkit.ChatColor.WHITE + msg);
                    event.getGuild().getSystemChannel()
                            .sendMessage(
                                    "**Message from " + event.getUser().getAsMention() + " to **`" + username + "`**:** " + msg)
                            .queue();
                    event.reply("Message sent!").setEphemeral(true).queue();
                } else {
                    event.reply("`" + username + "` is not online!").setEphemeral(true).queue();
                }
            } else {
                event.reply("This command must be executed in " + gameChatChannel.getAsMention()).setEphemeral(true)
                        .queue();
            }
        }
        App.getPlugin().saveConfig();
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

        OptionData tempbanOption1 = new OptionData(OptionType.STRING, "username", "The Minecraft user to tempban.",
                true);
        OptionData tempbanOption2 = new OptionData(OptionType.INTEGER, "hours",
                "The number of hours to enforce the tempban.", true);
        OptionData tempbanOption3 = new OptionData(OptionType.STRING, "reason", "Reason for tempban.", true);
        commandData.add(Commands.slash("tempban", "Tempban a player from the Minecraft server.")
                .addOptions(tempbanOption1, tempbanOption2, tempbanOption3));

        OptionData msgOption1 = new OptionData(OptionType.STRING, "username", "The user you want to message.", true);
        OptionData msgOption2 = new OptionData(OptionType.STRING, "message", "The message to send.", true);
        commandData.add(
                Commands.slash("msg", "Message a player on the Minecraft server.").addOptions(msgOption1, msgOption2));

        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}
