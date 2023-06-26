package io.github.stonley890.dreamvisitor.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;

public class CmdSethub implements CommandExecutor {

    Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Dreamvisitor.hubLocation = player.getLocation().getBlock().getLocation()
                    .add(new Location(player.getLocation().getWorld(), 0.5, 0, 0.5));
                    plugin.getConfig().set("hubLocation", Dreamvisitor.hubLocation);
                    plugin.saveConfig();
            player.sendMessage(Dreamvisitor.prefix + ChatColor.BLUE + "Hub location set.");
        } else {
            sender.sendMessage(Dreamvisitor.prefix + ChatColor.RED + "This command must be executed by a player!");
        }
        return true;
    }
    
}
