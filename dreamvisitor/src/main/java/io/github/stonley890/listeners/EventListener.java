package io.github.stonley890.listeners;

import java.io.File;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.shanerx.mojang.Mojang;

import io.github.stonley890.App;
import io.github.stonley890.commands.CommandsManager;
import io.github.stonley890.data.PlayerMemory;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        User user = event.getAuthor();
        String channelId = event.getChannel().getId();
        String username = event.getMessage().getContentRaw();
        String chatChannel = CommandsManager.getChatChannel();
        String whitelistChannel = CommandsManager.getWhitelistChannel();
        String memberRole = CommandsManager.getMemberRole();
        String step3Role = CommandsManager.getStep3Role();

        Pattern p = Pattern.compile("[^a-zA-Z0-9_-_]");

        // If in whitelist channel and username is "legal"
        if (channelId.equals(whitelistChannel) && user.isBot() == false && !p.matcher(username).find()) {
            // Connect to Mojang services
            Mojang mojang = new Mojang();
            mojang.connect();
            // Check for valid UUID
            try {
                mojang.getUUIDOfUsername(username);
                // Get OfflinePlayer from username
                OfflinePlayer player = Bukkit.getOfflinePlayer(username);

                // If player is not whitelisted, add them and change roles
                if (player.isWhitelisted() == false) {
                    Bukkit.getLogger().info("[Dreamvisitor] Whitelisting " + username + ".");
                    event.getGuild().getSystemChannel().sendMessage("Whitelisted `" + username + "` from user " + event.getAuthor().getAsMention()).queue();
                    player.setWhitelisted(true);
                    // Change roles if assigned
                    if (memberRole != "none") {
                        try {
                            event.getGuild().addRoleToMember(event.getAuthor(),
                                    event.getGuild().getRoleById(memberRole)).queue();
                            event.getGuild().removeRoleFromMember(event.getAuthor(), event.getGuild().getRoleById(step3Role))
                                    .queue();
                        } catch (HierarchyException exception) {
                            event.getChannel().sendMessage("**An error has occured! Staff have been notified.**").queue();
                            event.getGuild().getSystemChannel().sendMessage("There was an error: " + exception).queue();
                        }
                    }
                    // Reply with success
                    event.getMessage().addReaction(Emoji.fromFormatted("✅")).queue();
                    event.getChannel().sendMessage("`" + username + "` has been whitelisted!").queue();
                } else if (player.isWhitelisted() == true) {
                    // If user is already whitelisted, send error.
                    event.getMessage().addReaction(Emoji.fromFormatted("❗")).queue();
                    event.getChannel().sendMessage("`" + username + "` is already whitelisted!").queue();
                    try {
                        event.getGuild().addRoleToMember(event.getAuthor(),
                                event.getGuild().getRoleById(memberRole)).queue();
                        event.getGuild().removeRoleFromMember(event.getAuthor(), event.getGuild().getRoleById(step3Role))
                                .queue();
                    } catch (HierarchyException exception) {
                        event.getChannel().sendMessage("**An error has occured! Staff have been notified.**").queue();
                        event.getGuild().getSystemChannel().sendMessage("There was an error: " + exception).queue();
                    }
                }
            } catch (Exception e) {
                // username does not exist alert
                event.getChannel().sendMessage("`" + username + "` could not be found!\n*Don't have a Minecraft: Java Edition account? Check pins to get the member role.*").queue();
                event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
            }
            

        } else if (channelId.equals(whitelistChannel) && user.isBot() == false) {
            // illegal username
            event.getChannel().sendMessage("`" + username + "` contains illegal characters!\n*Don't have a Minecraft: Java Edition account? Check pins to get the member role.*").queue();
            event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
        }

        // If in chat channel and chat is not paused, send to Minecraft
        if (channelId.equals(chatChannel) && user.isBot() == false && App.getPlugin().getConfig().getBoolean("chatPaused") == false) {
            for(Player player : Bukkit.getServer().getOnlinePlayers()) {
                PlayerMemory memory = new PlayerMemory();
                try {
                    //Init file config
                    File file = new File(App.getPlayerPath(player));
                    FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                    memory.setDiscordToggled(fileConfig.getBoolean("discordToggled"));

                    if (memory.isDiscordToggled()) {
                        String discName = event.getAuthor().getName();
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < discName.length(); i++) {
                            char c = discName.charAt(i);
                            if (c == '_')
                                sb.append("\\");
                            sb.append(c);
                        }
                        player.sendMessage(org.bukkit.ChatColor.BLUE + "[Discord] " + org.bukkit.ChatColor.GRAY + "<" + sb.toString() + "> " + event.getMessage().getContentRaw());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }
}