package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.functions.Energy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerMoveEvent implements Listener {

    @EventHandler
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getTo() == null) return;
        if (player.isFlying() &&
                !(event.getTo().getX() == event.getFrom().getX() &&
                event.getTo().getY() == event.getFrom().getY() &&
                event.getTo().getZ() == event.getFrom().getZ())
        ) {
            // Remove energy if flying
            try {
                Integer i = Energy.energy.get(player);
                i -=2;
                if (i < 0) i = 0;
                Energy.energy.put(player, i);
            } catch (NullPointerException e) {
                Energy.energy.put(player, Energy.ENERGY_CAPACITY);
            }
        }
    }

}
