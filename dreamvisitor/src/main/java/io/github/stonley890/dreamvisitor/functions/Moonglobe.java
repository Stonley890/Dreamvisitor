package io.github.stonley890.dreamvisitor.functions;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Moonglobe {

    private static final List<Moonglobe> activeMoonglobes = new ArrayList<>();

    private UUID player = null;
    private Location origin = null;
    private double allowedDistance = 256;

    public Moonglobe(@NotNull UUID owner, @NotNull Location originLocation, double allowedOriginDistance) {

        player = owner;
        origin = originLocation;
        allowedDistance = allowedOriginDistance;

        activeMoonglobes.add(this);

    }

    public void remove(@Nullable String reason) {
        activeMoonglobes.remove(this);

        Player onlinePlayer = Bukkit.getPlayer(player);
        if (onlinePlayer != null && reason != null) onlinePlayer.sendMessage(ChatColor.RED + "You moon globe was removed: " + reason);

    }

    public void tick() {
        for (Moonglobe activeMoonglobe : activeMoonglobes) {

            if ((origin.getWorld().equals(origin.getWorld())) || ())
                activeMoonglobe.remove("Too far away from origin.");

        }
    }

}
