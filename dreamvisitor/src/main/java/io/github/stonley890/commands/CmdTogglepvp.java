package io.github.stonley890.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.github.stonley890.Main;

public class CmdTogglepvp implements CommandExecutor {

    Main plugin = Main.getPlugin();
    String pvpDisabled = "disablepvp";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Change config
        if (plugin.getConfig().getBoolean(pvpDisabled)) {
            plugin.getConfig().set(pvpDisabled, false);
            Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "PvP globally enabled.");
        } else {
            plugin.getConfig().set(pvpDisabled, true);
            Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "PvP globally disabled.");
        }
        plugin.saveConfig();
        return true;
    }
    
}
