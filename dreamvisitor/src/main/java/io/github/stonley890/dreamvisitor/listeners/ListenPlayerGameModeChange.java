package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import io.github.stonley890.dreamvisitor.functions.Flight;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerGameModeChange implements Listener {

    @EventHandler
    public void onPlayerGameModeChangeEvent(@NotNull PlayerGameModeChangeEvent event) {

        Player player = event.getPlayer();
        PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());

        // Re-enable flight if it gets disabled by game mode change
        if (!Flight.isFlightRestricted(player) && !Flight.isPlayerDepleted(player)) {
            Bukkit.getScheduler().runTaskLater(Dreamvisitor.getPlugin(), () -> player.setAllowFlight(true), 1);
        }

        if (memory.autoinvswap && ((player.getGameMode().equals(GameMode.SURVIVAL) && event.getNewGameMode().equals(GameMode.CREATIVE)) || (player.getGameMode().equals(GameMode.CREATIVE) && event.getNewGameMode().equals(GameMode.SURVIVAL)))) Bukkit.dispatchCommand(player, "invswap");

    }

}
