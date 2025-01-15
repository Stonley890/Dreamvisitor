package io.github.stonley890.dreamvisitor.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerToggleFlightEvent implements Listener {

    @EventHandler
    public void onToggleFlight(@NotNull PlayerToggleFlightEvent event) {

        if (event.isFlying()) {
            event.getPlayer().setGliding(false);
            event.getPlayer().setFlySpeed(0.05f);
            event.getPlayer().setFlying(true);
        } else {
            event.getPlayer().setFlying(false);
            event.getPlayer().setGliding(true);
        }

    }

}
