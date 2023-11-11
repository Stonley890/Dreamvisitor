package io.github.stonley890.dreamvisitor.discord;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.Utils;
import io.github.stonley890.dreamvisitor.data.AccountLink;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import io.github.stonley890.dreamvisitor.data.Whitelist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class DiscCommandsManager extends ListenerAdapter {

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
            Bot.gameChatChannel = jda.getTextChannelById(Objects.requireNonNull(config.getString("chatChannelID")));
        }
        if (config.getString("logChannelID") != null) {
            Bot.gameLogChannel = jda.getTextChannelById(Objects.requireNonNull(config.getString("logChannelID")));
        }
        if (config.getString("whitelistChannelID") != null) {
            Bot.whitelistChannel = jda.getTextChannelById(Objects.requireNonNull(config.getString("whitelistChannelID")));
        }
        if (config.getString("tribeRoles") != null) {
            for (int i = 0; i < 10; i++) {
                Bot.tribeRole.add(jda.getRoleById(config.getLongList("tribeRoles").get(i)));
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
            Bot.gameChatChannel = (TextChannel) event.getOption(channelOption, event.getChannel(), OptionMapping::getAsChannel);
            // Reply success
            event.reply("Game chat channel set to " + Bot.gameChatChannel.getAsMention()).queue();
            // Update config
            Dreamvisitor.getPlugin().getConfig().set("chatChannelID", Bot.gameChatChannel.getId());

        } else if (command.equals("setlogchat")) {

            // Get channel from args
            Bot.gameLogChannel = (TextChannel) event.getOption(channelOption, event.getChannel(), OptionMapping::getAsChannel);
            // Reply success
            event.reply("Log chat channel set to " + Bot.gameLogChannel.getAsMention()).queue();
            // Update config
            Dreamvisitor.getPlugin().getConfig().set("logChannelID", Bot.gameLogChannel.getId());

        } else if (command.equals("setwhitelist")) {

            // Get channel from args
            Bot.whitelistChannel = (TextChannel) event.getOption(channelOption, event.getChannel(), OptionMapping::getAsChannel);
            // Reply success
            event.reply("Whitelist channel set to " + Bot.whitelistChannel.getAsMention()).queue();
            // Update config
            Dreamvisitor.getPlugin().getConfig().set("whitelistChannelID", Bot.whitelistChannel.getId());

        } else if (command.equals("setrole")) {

            // Get role to set
            String targetRole = Objects.requireNonNull(event.getOption("type")).getAsString();
            Role role = Objects.requireNonNull(event.getOption("role")).getAsRole();

            if (Arrays.stream(Bot.TRIBE_NAMES).anyMatch(Predicate.isEqual(targetRole))) {

                // If one of the tribe names, find the index, get the list from config, and set the specified item
                int index = Arrays.binarySearch(Bot.TRIBE_NAMES, targetRole);
                List<Long> tribeRoles = plugin.getConfig().getLongList("tribeRoles");

                if (tribeRoles.isEmpty()) {
                    for (int i = 0; i < 10; i++) {
                        tribeRoles.add(0L);
                    }
                }

                tribeRoles.set(index, role.getIdLong());
                plugin.getConfig().set("tribeRoles", tribeRoles);

            } else {
                event.reply("The target role must match a specified name!").setEphemeral(true).queue();
            }
            event.reply("**" + targetRole + " set to " + role.getName() + "**").queue();

        } else if (command.equals("list")) {

            // Compile players to list unless no players online
            if (event.getChannel() == Bot.gameChatChannel) {

                // Create a stringbuilder
                StringBuilder list = new StringBuilder();

                // If there are players online
                if (!Bukkit.getServer().getOnlinePlayers().isEmpty()) {

                    Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();

                    List<Player> countedPlayers = new ArrayList<>();

                    // Iterate through each player
                    for (Player player : players) {
                        PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());

                        // If player is not vanished, add to list
                        if (!memory.vanished) {
                            countedPlayers.add(player);
                        }
                    }

                    // If there are no listed players (may occur with vanished players), report none
                    if (countedPlayers.isEmpty()) {
                        event.reply("**There are no players online.**").queue();
                    } else {
                        // Create string of list
                        for (Player player : countedPlayers) {
                            if (!list.isEmpty()) {
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
                event.reply("This command must be executed in " + Bot.gameChatChannel.getAsMention()).setEphemeral(true)
                        .queue();
            }

        } else if (command.equals("msg")) {

            // args
            String username = event.getOption(usernameOption, OptionMapping::getAsString);
            String msg = event.getOption("message", OptionMapping::getAsString);

            // Check for correct channel
            if (event.getChannel() == Bot.gameChatChannel) {
                // Check for player online
                assert username != null;
                if (Bukkit.getServer().getPlayer(username) != null) {

                    // Send message
                    Objects.requireNonNull(Bukkit.getServer().getPlayer(username)).sendMessage(
                            ChatColor.GRAY + "[" + ChatColor.DARK_AQUA + user.getName() + ChatColor.GRAY + " -> "
                                    + ChatColor.DARK_AQUA + "me" + ChatColor.GRAY + "] " + ChatColor.WHITE + msg);

                    // Log message
                    Objects.requireNonNull(jda.getTextChannelById(Bot.gameLogChannel.getId())).sendMessage(
                            "**Message from " + user.getAsMention() + " to **`" + username + "`**:** " + msg).queue();

                    // Reply success
                    event.reply("Message sent!").setEphemeral(true).queue();

                } else {
                    event.reply("`" + username + "` is not online!").setEphemeral(true).queue();
                }
            } else {
                event.reply("This command must be executed in " + Bot.gameChatChannel.getAsMention()).setEphemeral(true)
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
                Bukkit.broadcastMessage(ChatColor.DARK_BLUE + "[" + ChatColor.WHITE + "Broadcast" + ChatColor.DARK_BLUE + "] " + ChatColor.BLUE + message);

                EmbedBuilder builder = new EmbedBuilder();

                builder.setAuthor("Staff Broadcast");
                builder.setTitle(message);

                Bot.gameChatChannel.sendMessageEmbeds(builder.build()).queue();
                Bot.gameLogChannel.sendMessageEmbeds(builder.build()).queue();

                // Reply
                event.reply("Broadcast sent.").queue();
            } else {
                event.reply("Message too long! " + message.length() + " > 350").setEphemeral(true).queue();
            }
        } else if (command.equals("panic")) {

            EmbedBuilder replyEmbed = new EmbedBuilder();
            replyEmbed.setTitle("Are you sure?").setDescription("This will kick all players and set the player limit to 0. Click the button to confirm.");

            ActionRow actionRow = ActionRow.of(Button.danger("panic", "Yes, I'm sure."));

            event.replyEmbeds(replyEmbed.build()).addActionRows(actionRow).queue();

        } else if (command.equals("link")) {

            Dreamvisitor.debug("Command requested.");
            User targetUser = event.getOption("user").getAsUser();
            Dreamvisitor.debug("Got user.");
            String username = event.getOption("username").getAsString();
            Dreamvisitor.debug("Got username.");

            UUID uuid = Utils.getUUIDOfUsername(username);
            Dreamvisitor.debug("Command requested.");

            if (uuid == null) {
                event.reply("`" + username + "` could not be found!").queue();
                return;
            }

            AccountLink.linkAccounts(uuid, targetUser.getIdLong());
            event.reply(targetUser.getAsMention() + " is now linked to `" + username + "`!").queue();


        } else if (command.equals("user")) {

            Dreamvisitor.debug("Command requested.");
            User targetUser = event.getOption("user").getAsUser();
            Dreamvisitor.debug("Target user: " + targetUser.getId());

            // UUID from AccountLink.yml
            UUID uuid = AccountLink.getUuid(targetUser.getIdLong());
            String stringUuid = "N/A";
            String username = "N/A";

            if (uuid != null) {
                username = Utils.getUsernameOfUuid(uuid);
                stringUuid = uuid.toString();
            }

            // Send data
            EmbedBuilder builder = new EmbedBuilder();

            builder.setColor(Color.BLUE);
            builder.setAuthor(targetUser.getName(), targetUser.getAvatarUrl(), targetUser.getAvatarUrl());

            builder.addField("ID", user.getId(), false);
            builder.addField("Minecraft Username", username, false);
            builder.addField("UUID", stringUuid, false);

            event.replyEmbeds(builder.build()).queue();

        } else if (command.equals("resourcepackupdate")) {

            String resourcePackURL = null;

            try (InputStream input = new FileInputStream("server.properties")) {
                java.util.Properties prop = new java.util.Properties();
                prop.load(input);
                resourcePackURL = prop.getProperty("resource-pack");
            } catch (IOException e) {
                if (Dreamvisitor.debug) e.printStackTrace();
            }

            if (resourcePackURL != null) {

                event.deferReply().queue();

                try {
                    URL url = new URL(resourcePackURL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(10000); // timeout
                    connection.connect();

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream is = connection.getInputStream();
                        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
                        byte[] buffer = new byte[1024];
                        int bytesRead;

                        while ((bytesRead = is.read(buffer)) != -1) {
                            sha1.update(buffer, 0, bytesRead);
                        }

                        byte[] hashBytes = sha1.digest();
                        StringBuilder hash = new StringBuilder();

                        for (byte hashByte : hashBytes) {
                            hash.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
                        }

                        String newHash = hash.toString();

                        try (InputStream input = new FileInputStream("server.properties")) {
                            java.util.Properties prop = new java.util.Properties();
                            prop.load(input);

                            prop.setProperty("resource-pack-sha1", newHash); // Update the hash property

                            try (OutputStream output = new FileOutputStream("server.properties")) {
                                prop.store(output, null);
                                event.getHook().editOriginal("Hash updated to " + newHash + "!").queue();
                                Dreamvisitor.resourcePackHash = newHash;
                                if (!Bukkit.getOnlinePlayers().isEmpty()) {
                                    for (Player player : Bukkit.getOnlinePlayers()) {
                                        PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());

                                        player.setResourcePack(Bukkit.getResourcePack(), HexFormat.of().parseHex(Dreamvisitor.resourcePackHash));
                                        memory.resourcePackHash = Dreamvisitor.resourcePackHash;
                                        PlayerUtility.setPlayerMemory(player.getUniqueId(), memory);
                                    }
                                }

                            }
                        } catch (IOException e) {
                            if (Dreamvisitor.debug) e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    if (Dreamvisitor.debug) e.printStackTrace();
                }


            } else {
                event.reply("Could not get URL of resource pack.").queue();
            }

        } else if (command.equals("unwhitelist")) {

            OptionMapping usernameOption = event.getOption("username");
            String username;
            if (usernameOption != null) username = usernameOption.getAsString();
            else {
                event.reply("Option `username` could not be found.").queue();
                return;
            }

            Pattern p = Pattern.compile("[^a-zA-Z0-9_-_]");

            if (p.matcher(username).find()) {
                event.reply("`" + username + "` contains illegal characters!").queue();
                return;
            }

            UUID uuid = Utils.getUUIDOfUsername(username);

            if (uuid == null) {
                event.reply("`" + username + "` could not be found!").queue();
                return;
            }

            try {
                Whitelist.remove(username, uuid);
            } catch (IOException e) {
                event.reply("There was a problem accessing the whitelist file.").queue();
                return;
            }

            event.reply("Removed " + username + " from the whitelist.").queue();

        } else if (command.equals("toggleweb")) {

            if (!Dreamvisitor.webWhitelist) {
                Whitelist.startWeb();
                Dreamvisitor.webWhitelist = true;
                event.reply("Web whitelist enabled.").queue();
            } else {
                Whitelist.stopWeb();
                Dreamvisitor.webWhitelist = false;
                event.reply("Web whitelist disabled.").queue();
            }

            Dreamvisitor.getPlugin().getConfig().set("web-whitelist", Dreamvisitor.webWhitelist);

        } else if (command.equals("schedulerestart")) {

            ActionRow button = ActionRow.of(Button.primary("schedulerestart", "Undo"));

            if (Dreamvisitor.restartScheduled) {
                Dreamvisitor.restartScheduled = false;
                event.reply("✅ Canceled server restart.").addActionRows(button).queue();
            } else {
                Dreamvisitor.restartScheduled = true;
                event.reply("✅ The server will restart when there are no players online").addActionRows(button).queue();
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

        commandData.add(Commands.slash("resourcepackupdate", "Update the resource pack hash to prompt clients to download the pack.")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED));

        commandData.add(Commands.slash("unwhitelist", "Remove a user from the whitelist.")
                .addOption(OptionType.STRING, "username", "The username to remove.", true)
                .addOption(OptionType.BOOLEAN, "ban", "Whether to ban the user from the server.", false)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED));

        commandData.add(Commands.slash("toggleweb", "Toggle the web whitelist system on or off.")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED));

        commandData.add(Commands.slash("schedulerestart", "Schedule a server restart when no players are online.")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED));

        // register commands
        event.getGuild().updateCommands().addCommands(commandData).queue();

        commandData.clear();

    }
}
