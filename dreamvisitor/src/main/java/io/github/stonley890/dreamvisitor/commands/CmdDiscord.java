package io.github.stonley890.dreamvisitor.commands;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdDiscord implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (sender instanceof Player player) {
            PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());
            memory.discordToggled = !memory.discordToggled;

            player.sendMessage(Dreamvisitor.PREFIX + "Discord visibility toggled to " + !memory.discordToggled + ".");

            PlayerUtility.setPlayerMemory(player.getUniqueId(), memory);

            return true;

        } else {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "This command must be run by a player.");
            return false;
        }

    }
    
}
