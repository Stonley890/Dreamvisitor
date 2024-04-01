package io.github.stonley890.dreamvisitor.commands;

import io.github.stonley890.dreamvisitor.functions.ItemBanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdItemBanList implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // itembanlist

        if (sender instanceof Player player) {

            if (ItemBanList.badItems != null) {
                ItemBanList.inv.setContents(ItemBanList.badItems);
            }

            player.openInventory(ItemBanList.inv);
        }

        return true;
    }

}
