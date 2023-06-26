package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.commands.CmdItemBlacklist;
import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ListenInventoryClose implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (!player.isOp()) {

            for (ItemStack item : CmdItemBlacklist.badItems) {
                if (item != null && player.getInventory().contains(item.getType())) {
                    player.getInventory().remove(item.getType());

                    Bot.sendMessage(DiscCommandsManager.gameLogChannel, "Removed " + item.getType().name() + " (" + Objects.requireNonNull(item.getItemMeta()).getDisplayName() + ") from " + player.getName());
                }
            }
        }

        if (event.getInventory().equals(CmdItemBlacklist.inv)) {
            CmdItemBlacklist.saveItems();
        }
    }
}
