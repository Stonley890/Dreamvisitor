package io.github.stonley890.dreamvisitor.commands.discord;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.function.Predicate;

import io.github.stonley890.dreamvisitor.data.AccountLink;
import io.github.stonley890.dreamvisitor.google.UserTracker;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
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
import org.shanerx.mojang.Mojang;

public class DiscCommandsManager extends ListenerAdapter {

    public static TextChannel gameChatChannel;
    public static TextChannel gameLogChannel;
    public static TextChannel whitelistChannel;
    public static Role memberRole;
    public static Role step3role;
    public static List<Role> tribeRole = new ArrayList<>();
    public static final String[] TRIBE_NAMES = {"HiveWing", "IceWing", "LeafWing", "MudWing", "NightWing", "RainWing", "SandWing", "SeaWing", "SilkWing", "SkyWing"};
    public static final String[] TRIBES = {"hive", "ice", "leaf", "mud", "night", "rain", "sand", "sea", "silk", "sky"};


    String channelOption = "channel";
    String usernameOption = "username";
    String activityOption = "activity";

    static JDA jda = Bot.getJda();

    Dreamvisitor plugin = Dreamvisitor.getPlugin();

    // Get channels and roles from config
    @SuppressWarnings({"null"})
    public static void initChannelsRoles() {
        FileConfiguration config = Dreamvisitor.getPlugin().getConfig();

        if (config.getString("chatChannelID") != null) {
            gameChatChannel = jda.getTextChannelById(Objects.requireNonNull(config.getString("chatChannelID")));
        }
        if (config.getString("logChannelID") != null) {
            gameLogChannel = jda.getTextChannelById(Objects.requireNonNull(config.getString("logChannelID")));
        }
        if (config.getString("whitelistChannelID") != null) {
            whitelistChannel = jda.getTextChannelById(Objects.requireNonNull(config.getString("whitelistChannelID")));
        }
        if (config.getString("memberRoleID") != null) {
            memberRole = jda.getRoleById(Objects.requireNonNull(config.getString("memberRoleID")));
        }
        if (config.getString("step3RoleID") != null) {
            step3role = jda.getRoleById(Objects.requireNonNull(config.getString("step3RoleID")));
        }
        if (config.getString("tribeRoles") != null) {
            for (int i = 0; i < 10; i++) {
                tribeRole.add(jda.getRoleById(config.getStringList("tribeRoles").get(i)));
            }
        }

    }

    @Override
    @SuppressWarnings({"null"})
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        User user = event.getUser();

        if (command.equals("setgamechat")) {

            // Get channel from args
            gameChatChannel = (TextChannel) event.getOption(channelOption, event.getChannel(), OptionMapping::getAsChannel);
            // Reply success
            event.reply("Game chat channel set to " + gameChatChannel.getAsMention()).queue();
            // Update config
            Dreamvisitor.getPlugin().getConfig().set("chatChannelID", gameChatChannel.getId());

        } else if (command.equals("setlogchat")) {

            // Get channel from args
            gameLogChannel = (TextChannel) event.getOption(channelOption, event.getChannel(), OptionMapping::getAsChannel);
            // Reply success
            event.reply("Log chat channel set to " + gameLogChannel.getAsMention()).queue();
            // Update config
            Dreamvisitor.getPlugin().getConfig().set("logChannelID", gameLogChannel.getId());

        } else if (command.equals("setwhitelist")) {

            // Get channel from args
            whitelistChannel = (TextChannel) event.getOption(channelOption, event.getChannel(), OptionMapping::getAsChannel);
            // Reply success
            event.reply("Whitelist channel set to " + whitelistChannel.getAsMention()).queue();
            // Update config
            Dreamvisitor.getPlugin().getConfig().set("whitelistChannelID", whitelistChannel.getId());

        } else if (command.equals("setrole")) {

            // Get role to set
            String targetRole = Objects.requireNonNull(event.getOption("type")).getAsString();
            Role role = Objects.requireNonNull(event.getOption("role")).getAsRole();

            if (targetRole.equals("Member")) {

                memberRole = role;
                plugin.getConfig().set("memberRoleID", memberRole.getId());

            } else if (targetRole.equals("Step 3")) {

                step3role = role;
                plugin.getConfig().set("step3roleID", step3role.getId());

            } else if (Arrays.stream(TRIBE_NAMES).anyMatch(Predicate.isEqual(targetRole))) {

                // If one of the tribe names, find the index, get the list from config, and set the specified item
                int index = Arrays.binarySearch(TRIBE_NAMES, targetRole);
                List<String> tribeRoles = plugin.getConfig().getStringList("tribeRoles");

                if (tribeRoles.isEmpty()) {
                    for (int i = 0; i < 10; i++) {
                        tribeRoles.add("none");
                    }
                }

                tribeRoles.set(index, role.getId());
                plugin.getConfig().set("tribeRoles", tribeRoles);

            } else {
                event.reply("The target role must match a specified name!").setEphemeral(true).queue();
            }
            event.reply("**" + targetRole + " set to " + role.getName() + "**").queue();

        } else if (command.equals("list")) {

            // Compile players to list unless no players online
            if (event.getChannel() == gameChatChannel) {

                // Create a stringbuilder
                StringBuilder list = new StringBuilder();

                // If there are players online
                if (!Bukkit.getServer().getOnlinePlayers().isEmpty()) {

                    Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
                    PlayerMemory memory = new PlayerMemory();
                    List<Player> countedPlayers = new ArrayList<>();

                    // Iterate through each player
                    for (Player player : players) {
                        File file = new File(Dreamvisitor.getPlayerPath(player));
                        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                        memory.setVanished(fileConfig.getBoolean("vanished"));

                        // If player is not vanished, add to list
                        if (!memory.isVanished()) {
                            countedPlayers.add(player);
                        }
                    }

                    // If there are no listed players (may occur with vanished players), report none
                    if (countedPlayers.isEmpty()) {
                        event.reply("**There are no players online.**").queue();
                    } else {
                        // Create string of list
                        for (Player player : countedPlayers) {
                            if (list.length() > 0) {
                                list.append("`, `");
                            }
                            list.append(player.getName());
                        }
                        // Send list
                        event.reply("**There are " + players.size() + " player(s) online:** `" + list + "`")
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
            assert member != null;
            if (Bukkit.getServer().getPlayer(member) != null) {

                // Create timestamp
                Date date = new Date(System.currentTimeMillis() + 60L * 60 * 1000 * hours);
                // Add to banlist
                Bukkit.getServer().getBanList(BanList.Type.NAME).addBan(member, reason, date, null);
                // Kick player
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Objects.requireNonNull(Bukkit.getServer().getPlayer(member)).kickPlayer(reason);

                    }
                }.runTask(Dreamvisitor.getPlugin());
                // Success message
                event.reply("**`" + member + "` was successfully banned for " + hours + " hours. Reason:** " + reason)
                        .queue();

            } else {
                event.reply("**Player is offline!**").setEphemeral(true).queue();
            }

        } else if (command.equals("msg")) {

            // args
            String username = event.getOption(usernameOption, OptionMapping::getAsString);
            String msg = event.getOption("message", OptionMapping::getAsString);

            // Check for correct channel
            if (event.getChannel() == gameChatChannel) {
                // Check for player online
                assert username != null;
                if (Bukkit.getServer().getPlayer(username) != null) {

                    // Send message
                    Objects.requireNonNull(Bukkit.getServer().getPlayer(username)).sendMessage(
                            ChatColor.GRAY + "[" + ChatColor.DARK_AQUA + user.getName() + ChatColor.GRAY + " -> "
                                    + ChatColor.DARK_AQUA + "me" + ChatColor.GRAY + "] " + ChatColor.WHITE + msg);

                    // Log message
                    Objects.requireNonNull(jda.getTextChannelById(gameLogChannel.getId())).sendMessage(
                            "**Message from " + user.getAsMention() + " to **`" + username + "`**:** " + msg).queue();

                    // Reply success
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

            assert activityType != null;
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

            // Set presence
            if (type != ActivityType.CUSTOM_STATUS) {
                assert activity != null;
                jda.getPresence().setActivity(Activity.of(type, activity));
                event.reply("Activity set!").setEphemeral(true).queue();
            }

        } else if (command.equals("broadcast")) {

            // Get args
            String message = event.getOption("message", OptionMapping::getAsString);

            assert message != null;
            if (message.length() < 351) {
                // Send message
                Bukkit.broadcastMessage(ChatColor.DARK_BLUE + "[" + ChatColor.WHITE + "Broadcast" + ChatColor.DARK_BLUE + "] " + ChatColor.RESET + message);
                Bot.sendMessage(gameChatChannel, "**[Broadcast]** " + message);
                Bot.sendMessage(gameLogChannel, "**[Broadcast]** " + message);

                // Reply
                event.reply("Broadcast sent.").queue();
            } else {
                event.reply("Message too long! " + message.length() + " > 350").setEphemeral(true).queue();
            }
        } else if (command.equals("panic")) {

            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                        if (!player.isOp()) {
                            player.kickPlayer("Panic!");
                        }
                    }
                }
            });
            Dreamvisitor.playerlimit = 0;
            plugin.getConfig().set("playerlimit", 0);
            plugin.saveConfig();
            Bukkit.getServer().broadcastMessage(
                    ChatColor.RED + "Panicked by " + user.getName() + ".\nPlayer limit override set to 0.");
            Bot.sendMessage(DiscCommandsManager.gameLogChannel, "**Panicked by " + user.getName());
            event.reply("Panicked!").queue();

        } else if (command.equals("link")) {

            User targetUser = event.getOption("user").getAsUser();
            String username = event.getOption("username").getAsString();

            Mojang mojang = new Mojang().connect();
            String uuid = mojang.getUUIDOfUsername(username);

            if (uuid != null) {
                AccountLink.linkAccounts(uuid, targetUser.getId());
                try {
                    UserTracker.linkAccount(uuid, targetUser);
                    event.reply(targetUser.getAsMention() + " is now linked to `" + username + "`!").queue();
                } catch (GeneralSecurityException | IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                event.reply("`" + username + "` could not be found!").queue();
                return;
            }



        } else if (command.equals("user")) {

            User targetUser = event.getOption("user").getAsUser();

            Dreamvisitor.debug("Target user: " + targetUser.getId());

            try {
                List<List<Object>> seenIds = UserTracker.getRange("Users!A3:F1000");

                if (seenIds == null || seenIds.isEmpty()) {
                    // Should not happen
                    event.reply("No data was found on specified spreadsheet.").queue();
                } else {
                    // For each row
                    for (int i = 0; i < seenIds.size(); i++) {

                        Dreamvisitor.debug("ID: " + seenIds.get(i).get(3));

                        // Check ID column for matching ID
                        if (targetUser.getId().equals(seenIds.get(i).get(3))) {

                            // Get data
                            String minecraftUsername = (String) seenIds.get(i).get(0);
                            String uuid = (String) seenIds.get(i).get(1);
                            String unbanDate = "N/A";
                            if (!seenIds.get(i).get(4).equals("")) {
                                unbanDate = (String) seenIds.get(i).get(4);
                            }
                            String royaltyPosition = (String) seenIds.get(i).get(5);

                            // Send data
                            event.reply("Data for user **" + targetUser.getName() + "**:" +
                                    "\n**ID:** `" + user.getId() +
                                    "`\n**Minecraft Username:** `" + minecraftUsername +
                                    "`\n**UUID:** `" + uuid.replaceFirst(
                                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                                    "$1-$2-$3-$4-$5") +
                                    "`\n**Date Unbanned:** " + unbanDate +
                                    "\n**Royalty Position:** " + royaltyPosition
                            ).queue();

                            return;
                        }
                    }

                    // Not found
                    // Fall back to local sources

                    Mojang mojang = new Mojang().connect();

                    // UUID from AccountLink.yml
                    String uuid = AccountLink.getUuid(targetUser.getId());
                    // Minecraft username from Mojang
                    String username = "N/A";

                    String unbanDate = "N/A";
                    if (uuid != null) {
                        username = mojang.getPlayerProfile(uuid).getUsername();
                        if (Bukkit.getBannedPlayers().contains(Bukkit.getOfflinePlayer(UUID.fromString(uuid)))) {
                            if (Bukkit.getBanList(BanList.Type.NAME).getBanEntry(username).getExpiration() != null) {
                                unbanDate = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(username).getExpiration().toString();
                            } else {
                                unbanDate = "Infinite";
                            }
                        }
                    } else {
                        uuid = "N/A";
                    }

                    // Send data
                    event.reply("Local data for user **" + targetUser.getName() + "**:" +
                            "\n**ID:** `" + user.getId() +
                            "`\n**Minecraft Username:** `" + username +
                            "`\n**UUID:** `" + uuid.replaceFirst(
                            "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                            "$1-$2-$3-$4-$5") +
                            "`\n**Date Unbanned:** " + unbanDate
                    ).queue();

                }

            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Save configuration
        Dreamvisitor.getPlugin().saveConfig();
    }

    // Register commands on ready
    @Override
    @SuppressWarnings({"null"})
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("setgamechat", "Set the channel that game chat occurs in.")
                .addOption(OptionType.CHANNEL, channelOption, "The channel to set.", true, false)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED));

        commandData.add(Commands.slash("setlogchat", "Set the channel that logs Minecraft activity.")
                .addOption(OptionType.CHANNEL, channelOption, "The channel to set.", true, false)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED));

        commandData.add(Commands.slash("setwhitelist", "Set the channel that whitelists players.")
                .addOption(OptionType.CHANNEL, channelOption, "The channel to set.", true, false)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED));

        commandData.add(Commands.slash("setrole", "Set a role").addOptions(
                        new OptionData(OptionType.STRING, "type", "The role you want to set.", true)
                                .setAutoComplete(false)
                                .addChoice("Step 3", "Step 3")
                                .addChoice("Member", "Member")
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
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED));

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

        commandData.add(Commands.slash("broadcast", "Broadcast a message to the Minecraft server.")
                .addOption(OptionType.STRING, "message", "The message to broadcast.", true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED));

        commandData.add(Commands.slash("panic", "Kick all players from the server and set the player limit to 0.")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED));

        commandData.add(Commands.slash("link", "Link a Discord account to a Minecraft account.")
                .addOption(OptionType.USER, "user", "The Discord user to register.", true)
                .addOption(OptionType.STRING, "username", "The Minecraft account to connect", true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED));

        commandData.add(Commands.slash("user", "Get the details of a user.")
                .addOption(OptionType.USER, "user", "The user to search for.", true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED));


        event.getGuild().updateCommands().addCommands(commandData).queue();
        commandData.clear();

    }
}
