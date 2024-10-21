package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemBanList implements Listener {
    public static final Inventory inv = Bukkit.createInventory(null, 27, "Blacklisted Items");
    public static ItemStack[] badItems;

    public static void saveItems() {
        Dreamvisitor plugin = Dreamvisitor.getPlugin();
        badItems = inv.getContents();
        plugin.getConfig().set("itemBlacklist", badItems);
        plugin.saveConfig();
    }

    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (!player.isOp() && ItemBanList.badItems != null) {

            for (ItemStack item : ItemBanList.badItems) {
                if (item == null) continue;
                for (ItemStack content : player.getInventory().getContents()) {
                    if (content == null || !content.isSimilar(item)) continue;
                    player.getInventory().remove(item);
                }
            }
        }

        if (event.getInventory().equals(ItemBanList.inv)) {
            ItemBanList.saveItems();
        }
    }

}
