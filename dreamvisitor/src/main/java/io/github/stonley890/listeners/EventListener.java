package io.github.stonley890.listeners;

import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import io.github.stonley890.commands.CommandsManager;
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
        String username = event.getMessage().getContentStripped();
        String chatChannel = CommandsManager.getChatChannel();
        String whitelistChannel = CommandsManager.getWhitelistChannel();
        String memberRole = CommandsManager.getMemberRole();

        Pattern p = Pattern.compile("[^a-zA-Z0-9_-_]");

        if (channelId.equals(whitelistChannel) && user.isBot() == false && !p.matcher(username).find()) {
            Bukkit.getLogger().info("Finding " + username + ".");
            OfflinePlayer player = Bukkit.getOfflinePlayer(username);
            
            if (player.isWhitelisted() == false) {
                Bukkit.getLogger().info("Whitelisting " + player + ".");
                player.setWhitelisted(true);
                if (memberRole != "none") {
                try {
                    event.getGuild().addRoleToMember(event.getAuthor(),
                            event.getGuild().getRoleById(memberRole)).queue();
                } catch (HierarchyException exception) {
                    event.getChannel().sendMessage("Insufficient permissions! Bot role need higher priority!").queue();
                }
            }
                event.getMessage().addReaction(Emoji.fromFormatted("✅")).queue();
            } else if (player.isWhitelisted() == true) {
                Bukkit.getLogger().info(player + " is already whitelisted.");
                event.getMessage().addReaction(Emoji.fromFormatted("❗")).queue();
                event.getChannel().sendMessage("That user is already whitelisted!").queue();
            }

            

        } else if (channelId.equals(whitelistChannel) && user.isBot() == false) {
            event.getChannel().sendMessage("That username contains illegal characters!").queue();
            event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
        }

        if (channelId.equals(chatChannel) && user.isBot() == false) {
            Bukkit.broadcastMessage("\u00A73[Discord] \u00A77<" + event.getAuthor().getName() + "> "
                    + event.getMessage().getContentRaw());

        }
    }
}