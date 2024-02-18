package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.GameMode;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Sandbox implements Listener {

    private static final String[] disallowedCommands = {"tpaccept", "tpa", "hub", "etpa", "etpaccept", "home", "ehome"};

    /**
     * Enable sandbox mode for the given {@link Player}. If they are not already in sandbox mode, they will be put into creative mode and their inventory will be swapped.
     * @param player the player to enable sandbox mode for.
     */
    public static void enableSandbox(@NotNull Player player) {
        PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());

        if (memory.sandbox) return;

        memory.sandbox = true;
        InvSwap.swapInventories(player);
        player.setGameMode(GameMode.CREATIVE);

        ComponentBuilder messageBuilder = new ComponentBuilder();
        messageBuilder.append("You are now in sandbox mode.\n").bold(true)
                .append("A staff member put you into sandbox mode. You are now in creative mode. " +
                        "Your inventory has been cleared and stored for later restore. " +
                        "In sandbox mode, the following limitations are imposed:").bold(false)
                .append("- You cannot access containers.\n")
                .append("- You cannot drop items.\n")
                .append("- You cannot use spawn eggs.\n")
                .append("- You cannot teleport.\n")
                .append("Please notify a staff member if you require assistance with any of these rules.");
        player.spigot().sendMessage(messageBuilder.create());

    }

    /**
     * Disable sandbox mode for the given {@link Player}. If they are still in sandbox mode, they will be put into survival mode and their inventory will be swapped.
     * @param player the player to disable sandbox mode for.
     */
    public static void disableSandbox(@NotNull Player player) {
        PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());

        if (!memory.sandbox) return;

        memory.sandbox = false;
        InvSwap.swapInventories(player);
        player.setGameMode(GameMode.SURVIVAL);

        player.sendMessage(ChatColor.BOLD + "You are no longer in sandbox mode.");
    }

    @EventHandler
    public void onPlayerDropItem(@NotNull PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());
        if (memory.sandbox) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());
        if (memory.sandbox && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (Objects.requireNonNull(event.getClickedBlock()).getState() instanceof Container || (event.getItem() != null && event.getItem().getItemMeta() instanceof SpawnEggMeta))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(@NotNull PlayerCommandPreprocessEvent event) {

        Player player = event.getPlayer();
        PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());
        if (memory.sandbox) {
            for (String disallowedCommand : disallowedCommands) {
                if (event.getMessage().contains(disallowedCommand)) event.setCancelled(true);
            }
        }
    }

}
