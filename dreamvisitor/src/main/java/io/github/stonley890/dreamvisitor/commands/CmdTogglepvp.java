package io.github.stonley890.dreamvisitor.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.github.stonley890.dreamvisitor.Main;
import org.jetbrains.annotations.NotNull;

public class CmdTogglepvp implements CommandExecutor {

    final Main plugin = Main.getPlugin();
    final String pvpDisabled = "disablepvp";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // Change config
        if (plugin.getConfig().getBoolean(pvpDisabled)) {
            plugin.getConfig().set(pvpDisabled, false);
            Bukkit.getServer().broadcastMessage(ChatColor.BLUE + "PvP globally enabled.");
        } else {
            plugin.getConfig().set(pvpDisabled, true);
            Bukkit.getServer().broadcastMessage(ChatColor.BLUE + "PvP globally disabled.");
        }
        plugin.saveConfig();
        return true;
    }
    
}
