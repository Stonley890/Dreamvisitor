package io.github.stonley890.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.stonley890.Main;

public class CmdSethub implements CommandExecutor {

    Main plugin = Main.getPlugin();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            plugin.hubLocation = player.getLocation().getBlock().getLocation()
                    .add(new Location(player.getLocation().getWorld(), 0.5, 0, 0.5));
                    plugin.getConfig().set("hubLocation", plugin.hubLocation);
                    plugin.saveConfig();
            player.sendMessage(ChatColor.GOLD + "Hub location set.");
        } else {
            sender.sendMessage(ChatColor.RED + "This command must be executed by a player!");
        }
        return true;
    }
    
}
