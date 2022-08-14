package io.github.stonley890.listeners;

import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

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

        Pattern p = Pattern.compile("[^a-zA-Z0-9_-_]");

        if (channelId.equals("1008076407609507852") && user.isBot() == false && !p.matcher(username).find()) {
            Bukkit.getLogger().info("Finding " + username + ".");
            OfflinePlayer player = Bukkit.getOfflinePlayer(username);

            Bukkit.getLogger().info("Whitelisting " + player + ".");
            player.setWhitelisted(true);
            event.getMessage().addReaction(Emoji.fromFormatted("✅")).queue();
            try {
                event.getGuild().addRoleToMember(event.getAuthor(),
                        event.getGuild().getRoleById("1008234966812983367")).queue();
            } catch (HierarchyException exception) {
                event.getChannel().sendMessage("Insufficient permissions! Bot role need higher priority!").queue();
            }

        } else if(channelId.equals("1008076407609507852") && user.isBot() == false) {
            event.getChannel().sendMessage("That username contains illegal characters!").queue();
            event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
        }

        if (channelId.equals("974114329886605433") && user.isBot() == false) {
            Bukkit.broadcastMessage("\u00A73[Discord] \u00A77<" + event.getAuthor().getName() + "> " + event.getMessage().getContentRaw());
        }
    }
}