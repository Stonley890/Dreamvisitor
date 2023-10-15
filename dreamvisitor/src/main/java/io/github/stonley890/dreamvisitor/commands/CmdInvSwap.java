package io.github.stonley890.dreamvisitor.commands;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CmdInvSwap implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player player) {

            PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());

            ItemStack[] invContents;

            if (memory.creative) {
                memory.creativeInv = player.getInventory().getContents();
                if (memory.survivalInv == null) invContents = null;
                else invContents = memory.survivalInv;
            } else {
                memory.survivalInv = player.getInventory().getContents();
                if (memory.creativeInv == null) invContents = null;
                else invContents = memory.creativeInv;
            }

            if (invContents == null) player.getInventory().clear();
            else player.getInventory().setContents(invContents);
            memory.creative = !memory.creative;

            PlayerUtility.setPlayerMemory(player.getUniqueId(), memory);

            sender.sendMessage(Dreamvisitor.PREFIX + "Inventory transferred.");

        } else {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "This command must be run by a player!");
        }

        return true;
    }
}
