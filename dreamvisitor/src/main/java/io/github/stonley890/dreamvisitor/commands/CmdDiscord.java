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

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import org.jetbrains.annotations.NotNull;

public class CmdDiscord implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerMemory memory = new PlayerMemory();
            try {
                // Init file config
                File file = new File(Dreamvisitor.getPlayerPath(player));
                FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                memory.setDiscordToggled(fileConfig.getBoolean("discordToggled"));

                // Change data
                memory.setDiscordToggled(!memory.isDiscordToggled());

                // Save data
                fileConfig.set("discordToggled", memory.isDiscordToggled());
                fileConfig.save(file);

                player.sendMessage(Dreamvisitor.PREFIX +
                        ChatColor.WHITE + "Discord visibility toggled to " + memory.isDiscordToggled() + ".");
            } catch (Exception e) {
                Bukkit.getLogger().warning("ERROR: Unable to access player memory!");
                player.sendMessage(Dreamvisitor.PREFIX +
                        ChatColor.RED + "There was a problem accessing player memory. Check logs for stacktrace.");
                e.printStackTrace();
            }
            return true;

        } else {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "This command must be run by a player.");
            return false;
        }

    }
    
}
