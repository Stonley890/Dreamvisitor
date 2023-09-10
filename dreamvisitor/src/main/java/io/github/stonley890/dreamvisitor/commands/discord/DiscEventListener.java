package io.github.stonley890.dreamvisitor.commands.discord;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.AccountLink;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.shanerx.mojang.Mojang;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class DiscEventListener extends ListenerAdapter {

    String memberButtonID = "memberSkip";

    @Override
    @SuppressWarnings({"null"})
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {


        User user = event.getAuthor();
        Channel channel = event.getChannel();
        String username = event.getMessage().getContentRaw();

        Dreamvisitor plugin = Dreamvisitor.getPlugin();

        Pattern p = Pattern.compile("[^a-zA-Z0-9_-_]");

        // If in the whitelist channel and username is "legal"
        if (channel.equals(DiscCommandsManager.whitelistChannel) && !user.isBot() && !p.matcher(username).find()) {

            // Connect to Mojang services
            Mojang mojang = new Mojang().connect();

            // Check for valid UUID
            if (mojang.getUUIDOfUsername(username) == null) {
                // username does not exist alert
                event.getChannel().sendMessage("`" + username
                        + "` **could not be found!**").queue();
                event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
            } else {

                UUID uuid = UUID.fromString(mojang.getUUIDOfUsername(username).replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                        "$1-$2-$3-$4-$5"));

                // Link accounts if not already linked
                if (AccountLink.getUuid(user.getId()) == null) {
                    AccountLink.linkAccounts(uuid.toString(), user.getId());
                }

                // Access whitelist.json file
                String whitelistPath = Bukkit.getServer().getWorldContainer().getPath() + "/whitelist.json";
                // Parse whitelist.json to string list
                List<String> lines = null;
                try {
                    lines = Files.readAllLines(new File(whitelistPath).toPath());
                } catch (IOException e) {
                    Bot.sendMessage((TextChannel) channel, "There was a problem accessing the whitelist file. Staff have been notified of the issue.");
                    Bot.sendMessage(DiscCommandsManager.gameLogChannel, "There was a problem accessing the whitelist file. Check console for stacktrace.");
                    Bukkit.getLogger().warning("There was a problem accessing the whitelist file.");
                    e.printStackTrace();
                }

                // Format string list to StringBuilder
                StringBuilder fileString = new StringBuilder();
                if (lines != null) {
                    for (String line : lines) {
                        fileString.append(line);
                    }
                }

                // Format string to JSONArray
                JSONArray whitelist = new JSONArray(fileString.toString());

                // Check if already whitelisted
                boolean whitelisted = false;
                for (Object entry : whitelist) {
                    JSONObject object = (JSONObject) entry;
                    if (object.get("uuid").equals(uuid.toString())) {
                        whitelisted = true;
                    }
                }

                if (whitelisted) {
                    event.getMessage().addReaction(Emoji.fromFormatted("☑️")).queue();
                    Bot.sendMessage((TextChannel) channel, "`" + username + "` is already whitelisted!\nCheck <#914620824332435456> for the Server Address.");
                } else {
                    Bukkit.getLogger().info("Player is not whitelisted.");

                    // Create entry
                    JSONObject whitelistEntry = new JSONObject();
                    whitelistEntry.put("uuid", uuid.toString());
                    whitelistEntry.put("name", username);

                    // Add to whitelist.json
                    whitelist.put(whitelistEntry);

                    // Write to whitelist.json file
                    try {
                        Files.write(new File(whitelistPath).toPath(), whitelist.toString(4).getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        Bot.sendMessage((TextChannel) channel, "There was a problem writing to the whitelist file. Staff have been notified of the issue.");
                        Bot.sendMessage(DiscCommandsManager.gameLogChannel, "There was a problem writing to the whitelist file. Check console for stacktrace.");
                        Bukkit.getLogger().warning("There was a problem writing to the whitelist file.");
                        e.printStackTrace();
                    }

                    // reload whitelist
                    Bukkit.reloadWhitelist();

                    // success message
                    event.getMessage().addReaction(Emoji.fromFormatted("✅")).queue();
                    Bot.sendMessage((TextChannel) channel, "`" + username + "` has been whitelisted!\nCheck <#914620824332435456> for the Server Address.");
                }
            }

        } else if (channel.equals(DiscCommandsManager.whitelistChannel) && !user.isBot()) {

            // illegal username
            event.getChannel().sendMessage("`" + username
                    + "` **contains illegal characters!**").queue();
            event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
        }

        // If in chat channel and chat is not paused, send to Minecraft
        if (channel.equals(DiscCommandsManager.gameChatChannel) && !user.isBot()
                && !Dreamvisitor.getPlugin().getConfig().getBoolean("chatPaused")) {

            // Build message
            String discName = user.getName();
            StringBuilder sb = new StringBuilder();

            Bukkit.getLogger().log(Level.INFO, "[Discord] <{0}> {1}", event.getMessage().getContentRaw());

            // Check for each player
            if (!Bukkit.getServer().getOnlinePlayers().isEmpty()) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {

                    try {
                        // Init file config
                        File file = new File(Dreamvisitor.getPlayerPath(player));
                        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);

                        // If player has discord on, build and send message
                        if (fileConfig.getBoolean("discordToggled", true)) {

                            player.sendMessage(ChatColor.BLUE + "[Discord] " + ChatColor.GRAY + "<"
                                    + discName + "> " + event.getMessage().getContentRaw());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (!event.getAuthor().isBot() && event.getChannel().equals(DiscCommandsManager.gameLogChannel) && plugin.getConfig().getBoolean("enable-log-console-commands") && plugin.getConfig().getBoolean("log-console") && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {

            Dreamvisitor.debug("Sending console command from log channel...");

            String message = event.getMessage().getContentRaw();

            // Running commands from log channel
            Runnable runCommand = new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), message);
                }
            };
            Bukkit.getScheduler().runTask(plugin, runCommand);

        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {

        // Match tribe roles when user joins sister server.
        Dreamvisitor plugin = Dreamvisitor.getPlugin();

        User user = event.getMember().getUser();

        Member mainMember = DiscCommandsManager.gameLogChannel.getGuild().getMember(user);
        Member sisterMember = event.getMember();

        if (mainMember != null) {
            List<Role> mainRoles = mainMember.getRoles();

            if (!mainRoles.isEmpty()) {
                for (Role role : mainRoles) {
                    if (DiscCommandsManager.tribeRole.contains(role)) {
                        int tribeIndex = DiscCommandsManager.tribeRole.indexOf(role);

                        Role targetRole = Bot.getJda().getRoleById(Objects.requireNonNull(plugin.getConfig().getLongList("sisterTribeRoles")).get(tribeIndex));

                        if (targetRole != null) {
                            sisterMember.getRoles().add(targetRole);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings({"null"})
    void reportError(MessageChannel channel, Exception exception) {
        channel.sendMessage("**An error has occured! Staff have been notified.**").queue();
        Bot.sendMessage(DiscCommandsManager.gameLogChannel, "There was an error: " + exception);
    }
}