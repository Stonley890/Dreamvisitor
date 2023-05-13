package io.github.stonley890.listeners;

import java.io.File;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.shanerx.mojang.Mojang;

import io.github.stonley890.Main;
import io.github.stonley890.commands.DiscCommandsManager;
import io.github.stonley890.data.PlayerMemory;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class DiscEventListener extends ListenerAdapter {
    
    String memberButtonID = "memberSkip";

    @Override
    @SuppressWarnings({ "null" })
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {

        User user = event.getAuthor();
        Channel channel = event.getChannel();
        String username = event.getMessage().getContentRaw();

        Guild guild = event.getGuild();

        Channel chatChannel = guild.getGuildChannelById(DiscCommandsManager.getChatChannel());
        Channel whitelistChannel = guild.getGuildChannelById(DiscCommandsManager.getWhitelistChannel());
        Role memberRole = guild.getRoleById(DiscCommandsManager.getMemberRole());
        Role step3Role = guild.getRoleById(DiscCommandsManager.getStep3Role());

        Pattern p = Pattern.compile("[^a-zA-Z0-9_-_]");

        // If in whitelist channel and username is "legal"
        if (channel.equals(whitelistChannel) && !user.isBot() && !p.matcher(username).find()) {

            // Connect to Mojang services
            Mojang mojang = new Mojang().connect();

            // Check for valid UUID
            try {
                UUID uuid = UUID.fromString(mojang.getUUIDOfUsername(username).replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                        "$1-$2-$3-$4-$5"));
                // Get OfflinePlayer from username
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

                // If player is not whitelisted, add them and change roles
                if (!player.isWhitelisted()) {
                    Bukkit.getLogger().log(Level.INFO, "[Dreamvisitor] Whitelisting {0}.", username);
                    guild.getSystemChannel()
                            .sendMessage("Whitelisted `" + username + "` from user " + user.getAsMention())
                            .queue();
                    player.setWhitelisted(true);

                    // Change roles if assigned
                    addMemberRole(event.getChannel(), user, guild, memberRole, step3Role);

                    // Reply with success
                    event.getMessage().addReaction(Emoji.fromFormatted("✅")).queue();
                    event.getChannel().sendMessage("`" + username + "` has been whitelisted!").queue();

                } else if (player.isWhitelisted()) {

                    // If user is already whitelisted, send error.
                    event.getMessage().addReaction(Emoji.fromFormatted("✅")).queue();
                    event.getChannel().sendMessage("`" + username + "` is already whitelisted.").queue();
                    addMemberRole(event.getChannel(), user, guild, memberRole, step3Role);
                }
            } catch (Exception e) {

                // username does not exist alert
                event.getChannel().sendMessage("`" + username
                        + "` **could not be found!**\n*Don't have a Minecraft: Java Edition account? Press the button to get the member role.*")
                        .setActionRow(Button.primary(memberButtonID, "Continue Anyways")).queue();
                event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
            }

        } else if (channel.equals(whitelistChannel) && !user.isBot()) {

            // illegal username
            event.getChannel().sendMessage("`" + username
                    + "` **contains illegal characters!**\n*Don't have a Minecraft: Java Edition account? Press the button to get the member role.*")
                    .setActionRow(Button.primary(memberButtonID, "Continue Anyways")).queue();
            event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
        }

        // If in chat channel and chat is not paused, send to Minecraft
        if (channel.equals(chatChannel) && !user.isBot()
                && !Main.getPlugin().getConfig().getBoolean("chatPaused")) {
            
            // Build message
            String discName = user.getName();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < discName.length(); i++) {
                char c = discName.charAt(i);
                if (c == '_')
                    sb.append("\\");
                sb.append(c);
            }

            // Check for each player
            if (!Bukkit.getServer().getOnlinePlayers().isEmpty()) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    PlayerMemory memory = new PlayerMemory();
    
                    try {
                        // Init file config
                        File file = new File(Main.getPlayerPath(player));
                        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                        memory.setDiscordToggled(fileConfig.getBoolean("discordToggled", true));
    
                        // If player has discord on, build and send message
                        if (memory.isDiscordToggled()) {
    
                            player.sendMessage(ChatColor.BLUE + "[Discord] " + ChatColor.GRAY + "<"
                                    + sb.toString() + "> " + event.getMessage().getContentRaw());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    @SuppressWarnings({ "null" })
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {

        Guild guild = event.getGuild();

        User user = event.getUser();

        Role memberRole = guild.getRoleById(DiscCommandsManager.getMemberRole());
        Role step3Role = guild.getRoleById(DiscCommandsManager.getStep3Role());

        if (event.getButton().getId().equals(memberButtonID)) {
            if (memberRole != null) {
                try {
                    event.getGuild().addRoleToMember(user, memberRole).queue();
                    event.getGuild().removeRoleFromMember(user, step3Role).queue();
                } catch (HierarchyException exception) {
                    reportError(event.getChannel(), guild, exception);
                }
            }
            event.reply("You now have access to the rest of the server!").setEphemeral(true).queue();
            event.editButton(event.getButton().withDisabled(true)).queue();
        }
    }

    @SuppressWarnings({"null"})
    void addMemberRole(MessageChannel channel, User user, Guild guild, Role memberRole, Role step3Role) {
        try {
            guild.addRoleToMember(user, memberRole).queue();
            guild.removeRoleFromMember(user, step3Role).queue();
        } catch (HierarchyException exception) {
            reportError(channel, guild, exception);
        }
    }

    @SuppressWarnings({ "null" })
    void reportError(MessageChannel channel, Guild guild, Exception exception) {
        channel.sendMessage("**An error has occured! Staff have been notified.**").queue();
        guild.getSystemChannel().sendMessage("There was an error: " + exception).queue();
    }
}