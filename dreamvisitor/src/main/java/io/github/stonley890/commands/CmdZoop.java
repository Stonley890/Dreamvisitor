package io.github.stonley890.commands;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import io.github.stonley890.Bot;
import io.github.stonley890.Main;
import io.github.stonley890.data.PlayerMemory;

public class CmdZoop implements CommandExecutor {

    Main plugin = Main.getPlugin();

    @Override
    @SuppressWarnings({ "null" })
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerMemory memory = new PlayerMemory();
            try {
                // Init file config
                File file = new File(Main.getPlayerPath(player));
                FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                memory.setVanished(fileConfig.getBoolean("vanished"));

                // Change data
                if (memory.isVanished()) {
                    memory.setVanished(false);
                    String chatMessage = "**" + player.getName() + " joined the game**";
                    String channelId = DiscCommandsManager.getChatChannel();
                    if (!channelId.equals("none")) {
                        Bot.getJda().getTextChannelById(channelId).sendMessage(chatMessage)
                                .queue();
                    }
                } else {
                    memory.setVanished(true);
                    String chatMessage = "**" + player.getName() + " left the game**";
                    String channelId = DiscCommandsManager.getChatChannel();
                    if (!channelId.equals("none")) {
                        Bot.getJda().getTextChannelById(channelId).sendMessage(chatMessage)
                                .queue();
                    }
                }

                // Save data
                fileConfig.set("vanished", memory.isVanished());
                fileConfig.save(file);

                player.sendMessage("\u00a79Discord vanished toggled to " + memory.isVanished() + ".");
            } catch (Exception e) {
                Bukkit.getLogger().warning("ERROR: Unable to access player memory!");
                player.sendMessage(
                        ChatColor.RED + "There was a problem accessing player memory. Check logs for stacktrace.");
                e.printStackTrace();
            }

        } else {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player!");
        }
        return true;
    }

}
