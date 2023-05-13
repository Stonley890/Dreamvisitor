package io.github.stonley890.listeners;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import io.github.stonley890.Bot;
import io.github.stonley890.Main;
import io.github.stonley890.commands.DiscCommandsManager;

public class ListenPlayerChat implements Listener {

    Main plugin = Main.getPlugin();
    
    @EventHandler
    @SuppressWarnings({"unchecked","null"})
    public void onPlayerChatEvent(AsyncPlayerChatEvent event)
	{
        /**
        Send chat messages to Discord
        IF chat is not paused AND the player is not an operator OR the player is an
        operator, send message
        **/

        if (Main.chatPaused && !event.isCancelled())
		{
            File file = new File(plugin.getDataFolder().getAbsolutePath() + "/pauseBypass.yml");
            FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
            List<String> bypassedPlayers;

			// Load file
            try
			{
                fileConfig.load(file);
            } catch (IOException | InvalidConfigurationException e1) {
                e1.printStackTrace();
            }

			// Fetch bypassed players
            bypassedPlayers = ((List<String>)fileConfig.get("players"));

            // If player is on soft whitelist or is op, allow. If not, kick player.
            if (bypassedPlayers.contains(event.getPlayer().getUniqueId().toString())
                    || event.getPlayer().isOp()) {
                String chatMessage = "**" + event.getPlayer().getName() + "**: " + event.getMessage();
                String channelId = DiscCommandsManager.getChatChannel();

                if (channelId.equals("none")) {
                    channelId = "2";
                    Bot.getJda().getTextChannelById(channelId).sendMessage(chatMessage).queue();
                }
                
            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Chat is currently paused.");
            }  
        } else if (!event.isCancelled())
        {
            String chatMessage = "**" + event.getPlayer().getName() + "**: " + event.getMessage();
            String channelId = DiscCommandsManager.getChatChannel();
            if (!channelId.equals("none")) {
                Bot.getJda().getTextChannelById(channelId).sendMessage(chatMessage).queue();
            }
        }
    }

}
