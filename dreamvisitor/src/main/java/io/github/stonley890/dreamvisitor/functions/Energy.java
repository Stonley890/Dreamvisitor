package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Energy {
    public static final int ENERGY_CAPACITY = 400;
    public static final Map<Player, Integer> energy = new HashMap<>();

    public static void init() {
        Bukkit.getScheduler().runTaskTimer(Dreamvisitor.getPlugin(), () -> {
                    // if Player is flying, remove one energy
                    for (Player player : Bukkit.getOnlinePlayers()) {

                        energy.putIfAbsent(player, ENERGY_CAPACITY);

                        // Remove energy if flying
                        if (player.isFlying()) {
//                            // Remove energy if flying
//                            try {
//                                Integer i = energy.get(player);
//                                i -=2;
//                                if (i < 0) i = 0;
//                                energy.put(player, i);
//                            } catch (NullPointerException e) {
//                                energy.put(player, ENERGY_CAPACITY);
//                            }
                        } else if (energy.get(player) < ENERGY_CAPACITY) {
                            // Regenerate energy if not flying
                            try {
                                energy.compute(player, (k, i) -> i + 1);
                            } catch (NullPointerException e) {
                                energy.put(player, ENERGY_CAPACITY);
                            }
                        }

                        // Get bossbar (even if it's null)
                        NamespacedKey namespacedKey = NamespacedKey.fromString("dreamvisitor:" + player.getUniqueId().toString().toLowerCase() + "-energy", Dreamvisitor.getPlugin());
                        assert namespacedKey != null;
                        KeyedBossBar bossBar = Bukkit.getBossBar(namespacedKey);

                        if (energy.get(player) < ENERGY_CAPACITY) {
                            if (bossBar == null) { // Create bossbar if it's null
                                bossBar = Bukkit.createBossBar(namespacedKey, "Energy", BarColor.PURPLE, BarStyle.SEGMENTED_10);
                                bossBar.addPlayer(player);
                                bossBar.setVisible(true);
                            }
                            // Set progress
                            bossBar.setProgress((double) energy.get(player) / ENERGY_CAPACITY);
                        } else if (bossBar != null) {
                            // Remove bossbar if it's full
                            Bukkit.removeBossBar(namespacedKey);
                        }

                        // Remove player from flight if energy runs out
                        if (energy.get(player) == 0 && player.isFlying()) {
                            player.setFlying(false);
                            player.setGliding(true);
                        }

                    }
                }, 0, 0);
    }
}
