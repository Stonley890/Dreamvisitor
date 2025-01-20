package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ListenPlayerToggleFlightEvent implements Listener {

    private static final HashMap<Player, BukkitTask> wingFlapSoundTask = new HashMap<>();

    @EventHandler
    public void onToggleFlight(@NotNull PlayerToggleFlightEvent event) {

        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            if (event.isFlying()) {
                event.getPlayer().setGliding(false);
                event.getPlayer().setFlySpeed(0.05f);
                event.getPlayer().setFlying(true);

                wingFlapSoundTask.put(event.getPlayer(), Bukkit.getScheduler().runTaskTimer(Dreamvisitor.getPlugin(), () -> {
                    if (!event.getPlayer().isOnline() || !event.getPlayer().isFlying()) {
                        // remove this task
                        cancelWingFlapSoundTask(event.getPlayer());

                    } else {
                        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 0.5f, 1.2f);
                    }
                }, 1, 15));


            } else {
                event.getPlayer().setFlying(false);
                event.getPlayer().setGliding(true);
            }
        } else {
            if (event.isFlying()) {
                event.getPlayer().setFlySpeed(0.1f);
                event.getPlayer().setFlying(true);
            } else {
                event.getPlayer().setFlying(false);
            }
        }


    }

    private static void cancelWingFlapSoundTask(Player player) {
        wingFlapSoundTask.get(player).cancel();
    }

}
