package io.github.stonley890.dreamvisitor.discord;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.AccountLink;
import io.github.stonley890.dreamvisitor.data.Whitelist;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.shanerx.mojang.Mojang;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class DiscEventListener extends ListenerAdapter {

    @Override
    @SuppressWarnings({"null"})
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {


        User user = event.getAuthor();
        Channel channel = event.getChannel();
        String username = event.getMessage().getContentRaw();

        Dreamvisitor plugin = Dreamvisitor.getPlugin();

        Pattern p = Pattern.compile("[^a-zA-Z0-9_-_]");

        TextChannel whitelistChannel = Bot.getJda().getTextChannelById(plugin.getConfig().getLong("whitelistChannelID"));
        TextChannel gameChatChannel = Bot.getJda().getTextChannelById(plugin.getConfig().getLong("chatChannelID"));
        TextChannel gameLogChannel = Bot.getJda().getTextChannelById(plugin.getConfig().getLong("chatChannelID"));

        // If in the whitelist channel and username is "legal"
        if (channel.equals(whitelistChannel) && !user.isBot() && !p.matcher(username).find()) {

            // Connect to Mojang services
            Mojang mojang = new Mojang().connect();
            Dreamvisitor.debug("Connected to Mojang");

            // Check for valid UUID
            Dreamvisitor.debug("Checking for valid UUID");
            if (mojang.getUUIDOfUsername(username) == null) {
                // username does not exist alert
                Dreamvisitor.debug("Username does not exist.");
                event.getChannel().sendMessage("`" + username
                        + "` **could not be found!**").queue();
                event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
                Dreamvisitor.debug("Failed whitelist.");
            } else {

                Dreamvisitor.debug("Got UUID");
                UUID uuid = UUID.fromString(mojang.getUUIDOfUsername(username).replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                        "$1-$2-$3-$4-$5"));

                // Link accounts if not already linked
                Dreamvisitor.debug("Do accounts need to be linked?");
                if (AccountLink.getUuid(user.getId()) == null) {
                    Dreamvisitor.debug("Yes, linking account.");
                    AccountLink.linkAccounts(uuid.toString(), user.getId());
                    Dreamvisitor.debug("Linked.");
                }

                try {
                    if (Whitelist.isUserWhitelisted(uuid)) {
                        Dreamvisitor.debug("Already whitelisted.");
                        event.getMessage().addReaction(Emoji.fromFormatted("☑️")).queue();
                        Bot.sendMessage((TextChannel) channel, "`" + username + "` is already whitelisted!\nCheck <#914620824332435456> for the Server Address.");
                        Dreamvisitor.debug("Resolved.");
                    } else {
                        Dreamvisitor.debug("Player is not whitelisted.");

                        Whitelist.add(username, uuid);

                        // success message
                        Dreamvisitor.debug("Success.");
                        event.getMessage().addReaction(Emoji.fromFormatted("✅")).queue();
                        Bot.sendMessage((TextChannel) channel, "`" + username + "` has been whitelisted!\nCheck <#914620824332435456> for the Server Address.");
                        TextChannel systemChannel = Bot.gameLogChannel.getGuild().getSystemChannel();
                        if (systemChannel != null) systemChannel.sendMessage("Whitelisted " + username + " from " + event.getAuthor().getAsMention() + ". Use `/unwhitelist <username>` to undo this action or `/link <username> <member>` to link this user to a different member.").queue();
                    }
                } catch (IOException e) {
                    Bot.sendMessage((TextChannel) channel, "There was a problem accessing the whitelist file. Please try again later.");
                    if (Dreamvisitor.debug) e.printStackTrace();
                }
            }

        } else if (channel.equals(whitelistChannel) && !user.isBot()) {

            // illegal username
            event.getChannel().sendMessage("`" + username
                    + "` **contains illegal characters!**").queue();
            event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
        }

        // If in the chat channel and the chat is not paused, send to Minecraft
        if (channel.equals(gameChatChannel) && !user.isBot()
                && !Dreamvisitor.getPlugin().getConfig().getBoolean("chatPaused")) {

            // Build message
            String discName = user.getName();

            Bukkit.getLogger().log(Level.INFO, "[Discord] <{0}> {1}", event.getMessage().getContentRaw());

            // Check for each player
            if (!Bukkit.getServer().getOnlinePlayers().isEmpty()) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {

                    try {
                        // Init file config
                        File file = new File(Dreamvisitor.getPlayerPath(player));
                        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);

                        // If the player has discord on, build and send the message
                        if (fileConfig.getBoolean("discordToggled", true)) {

                            player.sendMessage(ChatColor.BLUE + "[Discord] " + ChatColor.GRAY + "<"
                                    + discName + "> " + event.getMessage().getContentRaw());
                        }
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("There was a problem ");
                        if (Dreamvisitor.debug) e.printStackTrace();
                    }
                }
            }
        }

        if (!event.getAuthor().isBot() && event.getChannel().equals(gameLogChannel) && plugin.getConfig().getBoolean("enable-log-console-commands") && plugin.getConfig().getBoolean("log-console") && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {

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

}