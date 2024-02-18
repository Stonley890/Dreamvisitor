package io.github.stonley890.dreamvisitor.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.stonley890.dreamvisitor.Main;
import org.jetbrains.annotations.NotNull;

public class CmdSethub implements CommandExecutor {

    final Main plugin = Main.getPlugin();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            Main.hubLocation = player.getLocation().getBlock().getLocation()
                    .add(new Location(player.getLocation().getWorld(), 0.5, 0, 0.5));
                    plugin.getConfig().set("hubLocation", Main.hubLocation);
                    plugin.saveConfig();
            player.sendMessage(Main.PREFIX + ChatColor.WHITE + "Hub location set.");
        } else {
            sender.sendMessage(Main.PREFIX + ChatColor.RED + "This command must be executed by a player!");
        }
        return true;
    }
    
}
