package io.github.stonley890.dreamvisitor.commands;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;

public class CmdZoop implements CommandExecutor {

    Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // If cmd executor is player
        if (sender instanceof Player) {

            Player player = (Player) sender;
            PlayerMemory memory = new PlayerMemory();

            try {
                // Init file config
                File file = new File(Dreamvisitor.getPlayerPath(player));
                FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                memory.setVanished(fileConfig.getBoolean("vanished"));

                // Change data
                if (memory.isVanished()) {

                    memory.setVanished(false);
                    String chatMessage = "**" + player.getName() + " joined the game**";
                    Bot.sendMessage(DiscCommandsManager.gameChatChannel, chatMessage);
                    Bot.sendMessage(DiscCommandsManager.gameLogChannel, chatMessage);

                } else {
                    memory.setVanished(true);
                    String chatMessage = "**" + player.getName() + " left the game**";
                    Bot.sendMessage(DiscCommandsManager.gameChatChannel, chatMessage);
                    Bot.sendMessage(DiscCommandsManager.gameLogChannel, chatMessage);

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
