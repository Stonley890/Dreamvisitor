package io.github.stonley890.dreamvisitor.listeners;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.TimeSkipEvent;

import java.util.Objects;

public class ListenTimeSkip implements Listener {

    @EventHandler
    public void onTimeSkipEvent(TimeSkipEvent event) {
        // Sync time skips
        for (World world : Bukkit.getWorlds()) if (!Objects.equals(world, event.getWorld())) new TimeSkipEvent(world, event.getSkipReason(), event.getSkipAmount());

    }

}
