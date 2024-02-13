package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Moonglobe {

    public static final List<Moonglobe> activeMoonglobes = new ArrayList<>();
    private final static double momentumMultiplier = 0.1;

    private UUID player = null;
    private final Location origin;
    private Location currentLocation = null;
    private final double allowedDistance;
    private boolean shown = false;
    private ItemDisplay glowEntity = null;


    public Moonglobe(@NotNull UUID owner, @NotNull Location originLocation, double allowedOriginDistance) {

        player = owner;
        origin = originLocation;
        currentLocation = originLocation;
        allowedDistance = allowedOriginDistance;

        activeMoonglobes.add(this);

    }

    public void remove(@Nullable String reason) {
        activeMoonglobes.remove(this);

        if (shown) glowEntity.remove();

        Player onlinePlayer = Bukkit.getPlayer(player);
        if (onlinePlayer != null && reason != null) onlinePlayer.sendMessage(ChatColor.RED + "You moon globe was removed: " + reason);

    }

    public static void tick() {
        for (Moonglobe activeMoonglobe : activeMoonglobes) {

            Player onlinePlayer = Bukkit.getPlayer(activeMoonglobe.player);
            if (onlinePlayer != null) {

                if (!activeMoonglobe.shown) activeMoonglobe.showGlobe();

                Location targetPosition = onlinePlayer.getEyeLocation().add(-0.5, 0, -0.5);
                Vector posDifference = targetPosition.subtract(activeMoonglobe.currentLocation).toVector();
                Vector momentum = posDifference.multiply(momentumMultiplier);

                Location newLocation = activeMoonglobe.currentLocation.add(momentum);

                if (!Objects.equals(activeMoonglobe.currentLocation.getBlock(), newLocation.getBlock())) {

                    Block oldBlock = activeMoonglobe.currentLocation.getBlock();
                    Block newBlock = newLocation.getBlock();
                    
                }

                activeMoonglobe.currentLocation = newLocation;

                Dreamvisitor.debug("GLOW MOMENTUM: " + momentum.getX() + momentum.getY() + momentum.getZ());

                activeMoonglobe.glowEntity.teleport(activeMoonglobe.currentLocation);

            } else if (activeMoonglobe.shown) activeMoonglobe.hideGlobe();

            if ((!Objects.equals(activeMoonglobe.origin.getWorld(), activeMoonglobe.currentLocation.getWorld())) || (activeMoonglobe.origin.distance(activeMoonglobe.currentLocation) > activeMoonglobe.allowedDistance))
                activeMoonglobe.remove("Too far away from origin.");

        }
    }

    private void showGlobe() {

        glowEntity = (ItemDisplay) Objects.requireNonNull(currentLocation.getWorld()).spawnEntity(currentLocation, EntityType.ITEM_DISPLAY);
        glowEntity.setItemStack(new ItemStack(Material.SEA_LANTERN));
        glowEntity.setTransformation(new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(0.5f, 0.5f, 0.5f), new Quaternionf()));
        shown = true;

    }

    private void hideGlobe() {
        glowEntity.remove();
        shown = false;
    }

}
