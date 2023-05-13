package io.github.stonley890.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import io.github.stonley890.Main;

public class ListenEntityDamage implements Listener {
    
    Main plugin = Main.getPlugin();

    @EventHandler
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {

        // If PvP is disabled and the damage type is player, cancel the event
        if ((event.getDamager().getType() == EntityType.PLAYER && event.getEntity().getType() == EntityType.PLAYER) && plugin.getConfig().getBoolean("disablepvp")) {
                event.setCancelled(true);
        }
    }
    
}
