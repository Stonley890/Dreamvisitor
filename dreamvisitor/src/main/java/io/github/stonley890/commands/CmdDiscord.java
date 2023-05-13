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

import io.github.stonley890.Main;
import io.github.stonley890.data.PlayerMemory;

public class CmdDiscord implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerMemory memory = new PlayerMemory();
            try {
                // Init file config
                File file = new File(Main.getPlayerPath(player));
                FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                memory.setDiscordToggled(fileConfig.getBoolean("discordToggled"));

                // Change data
                if (memory.isDiscordToggled()) {
                    memory.setDiscordToggled(false);
                } else {
                    memory.setDiscordToggled(true);
                }

                // Save data
                fileConfig.set("discordToggled", memory.isDiscordToggled());
                fileConfig.save(file);

                player.sendMessage(
                        ChatColor.DARK_AQUA + "Discord visibility toggled to " + memory.isDiscordToggled() + ".");
            } catch (Exception e) {
                Bukkit.getLogger().warning("ERROR: Unable to access player memory!");
                player.sendMessage(
                        ChatColor.RED + "There was a problem accessing player memory. Check logs for stacktrace.");
                e.printStackTrace();
            }

        }
        return true;
    }
    
}
