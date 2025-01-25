package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.functions.Flight;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerRespawn implements Listener {

    @EventHandler
    public void onPlayerRespawn(@NotNull PlayerRespawnEvent event) {
        Flight.setupFlight(event.getPlayer());
    }
}
